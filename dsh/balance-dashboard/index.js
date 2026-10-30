/**
 * Host half of the balance dashboard.
 *
 * One exact JSON route — `/api/balance-dashboard` — answers the page. It is the
 * only place credentials are read: the browser never sees one, and every number
 * the page shows is computed here.
 *
 * Sources, in order of trust:
 *   1. The provider itself, where it exposes a balance endpoint (DeepSeek
 *      `/user/balance`; Anthropic's organization cost report when an admin key
 *      is configured). Google AI Studio, SiliconFlow, DeepInfra, Moonshot and
 *      Zhipu expose no balance endpoint, so their cards say so instead of
 *      inventing a number.
 *   2. The LiteLLM gateway catalog (`/v1/models`): every model this machine can
 *      actually route to right now.
 *   3. The budget governor (`COCKPIT/api/budget`): the daily and monthly caps,
 *      spend so far, remaining budget and circuit-breaker state.
 *   4. The metered ledger this machine already writes for every model call
 *      (micro-USD per record), aggregated per provider, per day and per model.
 *
 * A provider that fails is reported as a per-provider error; the page still
 * renders the rest.
 */
import { access, readFile } from 'node:fs/promises'
import { homedir } from 'node:os'
import path from 'node:path'

const ROUTE = '/api/balance-dashboard'
/** The page's own read path. Loopback-only and credential-free by design: the
 * browser's same-origin policy already stops a web page on another origin from
 * reading it (no CORS headers are sent), and any process that could read this
 * dashboard locally could read the ledger file itself. */
const PAGE_ROUTE = '/balance-dashboard/data'
const CACHE_MS = 30_000
const REQUEST_MS = 8_000
const DAY_MS = 86_400_000

const GATEWAY_BASE = (process.env.LITELLM_BASE_URL ?? 'http://127.0.0.1:4000/v1').replace(/\/+$/, '')
const GOVERNOR_BASE = (process.env.COCKPIT_BASE_URL ?? 'http://127.0.0.1:8080').replace(/\/+$/, '')

const LEDGER_CANDIDATES = [
  process.env.BALANCE_LEDGER_PATH,
  'F:/src/github.com/intellibitz/susi/out/live/ledger.jsonl',
  path.join(homedir(), '.susi', 'ledger.jsonl'),
].filter(Boolean)

/**
 * Every provider this machine routes to. `matches` maps a routed model id to its
 * provider; the order matters only for overlapping prefixes.
 */
const PROVIDERS = [
  {
    id: 'deepseek',
    name: 'DeepSeek',
    colour: 'var(--dsw-alias-brand-primary)',
    keyNames: ['DEEPSEEK_API_KEY'],
    matches: (model) => model.startsWith('deepseek'),
    balanceNote: null,
  },
  {
    id: 'anthropic',
    name: 'Claude',
    colour: 'var(--dsw-alias-state-warn-primary)',
    keyNames: ['ANTHROPIC_API_KEY'],
    matches: (model) => model.startsWith('claude') || model.includes('anthropic'),
    balanceNote:
      'Claude has no per-key balance endpoint: credits live in the Console and the API exposes them only through an Anthropic admin key (organization cost report).',
  },
  {
    id: 'google',
    name: 'Gemini',
    colour: 'var(--dsw-alias-state-success-primary)',
    keyNames: ['GEMINI_API_KEY', 'GOOGLE_API_KEY'],
    matches: (model) => model.startsWith('gemini') || model.startsWith('google'),
    balanceNote:
      'Google AI Studio does not expose a balance through the Gemini API; billing lives in Google Cloud, which needs OAuth credentials this plugin does not hold.',
  },
  {
    id: 'siliconflow',
    name: 'SiliconFlow',
    colour: 'var(--dsw-alias-state-info-primary)',
    keyNames: ['SILICONFLOW_API_KEY'],
    matches: (model) => model.startsWith('siliconflow'),
    balanceNote:
      'SiliconFlow exposes no balance endpoint; usage is metered below from this machine’s call ledger.',
  },
  {
    id: 'deepinfra',
    name: 'DeepInfra',
    colour: 'var(--dsw-alias-state-info-primary)',
    keyNames: ['DEEPINFRA_API_KEY'],
    matches: (model) => model.startsWith('deepinfra'),
    balanceNote:
      'DeepInfra exposes no balance endpoint through its OpenAI-compatible API; usage is metered below.',
  },
  {
    id: 'moonshot',
    name: 'Kimi / Moonshot',
    colour: 'var(--dsw-alias-state-success-primary)',
    keyNames: ['MOONSHOT_API_KEY', 'KIMI_API_KEY', 'MOONSHOTAI_API_KEY'],
    matches: (model) => model.startsWith('kimi') || model.startsWith('moonshot'),
    balanceNote:
      'Moonshot exposes a balance endpoint only on its console; usage is metered below.',
  },
  {
    id: 'zhipu',
    name: 'Zhipu GLM',
    colour: 'var(--dsw-alias-state-warn-primary)',
    keyNames: ['ZHIPU_API_KEY', 'ZAI_API_KEY'],
    matches: (model) => model.startsWith('glm') || model.startsWith('zhipu'),
    balanceNote:
      'Zhipu exposes no balance endpoint through its OpenAI-compatible API; usage is metered below.',
  },
  {
    id: 'local',
    name: 'Local (Ollama)',
    colour: 'var(--dsw-alias-state-success-primary)',
    keyNames: [],
    matches: (model) => model.startsWith('local') || model.startsWith('ollama'),
    balanceNote: 'Local inference is free: it only costs electricity and the GPU you already own.',
  },
  {
    id: 'other',
    name: 'Other',
    colour: 'var(--dsw-alias-text-2)',
    keyNames: [],
    matches: () => false,
    balanceNote: null,
  },
]

let cached = null

function nowMs() {
  return Date.now()
}

function dayKey(ms) {
  const date = new Date(ms)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

async function exists(file) {
  try {
    await access(file)
    return true
  } catch {
    return false
  }
}

async function resolveLedger() {
  for (const candidate of LEDGER_CANDIDATES) {
    if (await exists(candidate)) return candidate
  }
  return LEDGER_CANDIDATES[0] ?? null
}

const cloudEnvCache = { at: 0, values: {} }

/** Credential files, highest precedence first. The desktop app's own process
 * environment is a snapshot from boot, so a key added later (the gateway master
 * key) is picked up from here instead of requiring an app restart. */
const ENV_FILES = [
  path.join(homedir(), '.secrets', '.env'),
  path.join(homedir(), '.susi', 'cloud.env'),
]

async function cloudEnv() {
  if (nowMs() - cloudEnvCache.at < CACHE_MS) return cloudEnvCache.values
  const values = {}
  for (const file of ENV_FILES) {
    let text
    try {
      text = await readFile(file, 'utf8')
    } catch {
      // No key file is a normal state: the env may carry the keys instead.
      continue
    }
    for (const line of text.split(/\r?\n/)) {
      const match = /^\s*([A-Z0-9_]+)\s*=\s*(.*)\s*$/.exec(line)
      if (!match) continue
      const value = match[2].replace(/^["']|["']$/g, '').trim()
      if (value && !values[match[1]]) values[match[1]] = value
    }
  }
  cloudEnvCache.at = nowMs()
  cloudEnvCache.values = values
  return values
}

async function keyFor(names) {
  for (const name of names) {
    if (process.env[name]) return { name, value: process.env[name] }
  }
  const file = await cloudEnv()
  for (const name of names) {
    if (file[name]) return { name, value: file[name] }
  }
  return null
}

function usd(micro) {
  return Math.round(micro) / 1_000_000
}

function numberFrom(value) {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : null
}

async function fetchDeepSeekBalance(credential) {
  if (!credential) return { configured: false, balance: null }
  const response = await fetch('https://api.deepseek.com/user/balance', {
    headers: {
      authorization: `Bearer ${credential.value}`,
      accept: 'application/json',
    },
    signal: AbortSignal.timeout(REQUEST_MS),
  })
  if (!response.ok) {
    throw new Error(`DeepSeek balance returned HTTP ${response.status}`)
  }
  const body = await response.json()
  const info = Array.isArray(body.balance_infos) ? body.balance_infos[0] : null
  if (!info) return { configured: true, balance: null }
  return {
    configured: true,
    balance: {
      available: body.is_available !== false,
      currency: info.currency ?? 'USD',
      total: numberFrom(info.total_balance),
      granted: numberFrom(info.granted_balance),
      toppedUp: numberFrom(info.topped_up_balance),
    },
  }
}

/**
 * Anthropic's organization cost report needs an admin key (`sk-ant-admin…`),
 * which is a different credential from the inference key. Without one the card
 * reports the note; with one, the last 30 days come back in cents.
 */
async function fetchAnthropicSpend(credential) {
  if (!credential) return null
  const ending = new Date()
  const starting = new Date(ending.getTime() - 30 * DAY_MS)
  const query = new URLSearchParams({
    starting_at: starting.toISOString(),
    ending_at: ending.toISOString(),
    bucket_width: '1d',
    limit: '31',
  })
  const response = await fetch(
    `https://api.anthropic.com/v1/organizations/cost_report?${query.toString()}`,
    {
      headers: {
        'x-api-key': credential.value,
        'anthropic-version': '2023-06-01',
        accept: 'application/json',
      },
      signal: AbortSignal.timeout(REQUEST_MS),
    },
  )
  if (!response.ok) {
    throw new Error(`Anthropic cost report returned HTTP ${response.status}`)
  }
  const body = await response.json()
  const buckets = Array.isArray(body.data) ? body.data : []
  let cents = 0
  for (const bucket of buckets) {
    const results = Array.isArray(bucket.results) ? bucket.results : []
    for (const result of results) {
      const amount = numberFrom(result.amount)
      if (amount !== null) cents += amount
    }
  }
  return {
    cents: Math.round(cents),
    buckets: buckets.length,
    note: 'Organization cost report, last 30 days (admin key).',
  }
}

/** Every model group the LiteLLM gateway can route right now. */
async function fetchCatalog() {
  const credential = await keyFor(['LITELLM_MASTER_KEY', 'LITELLM_API_KEY'])
  const headers = { accept: 'application/json' }
  if (credential) headers.authorization = `Bearer ${credential.value}`
  try {
    const response = await fetch(`${GATEWAY_BASE}/models`, {
      headers,
      signal: AbortSignal.timeout(REQUEST_MS),
    })
    if (!response.ok) {
      return { up: false, error: `Gateway returned HTTP ${response.status}`, models: [] }
    }
    const body = await response.json()
    const models = (Array.isArray(body.data) ? body.data : [])
      .map((entry) => String(entry?.id ?? ''))
      .filter(Boolean)
      .sort((left, right) => left.localeCompare(right))
    return { up: true, error: null, models }
  } catch (error) {
    return {
      up: false,
      error: `Gateway unreachable: ${error instanceof Error ? error.message : String(error)}`,
      models: [],
    }
  }
}

/** Daily/monthly budget caps and remaining headroom from the CEO Cockpit governor. */
async function fetchBudget() {
  try {
    const response = await fetch(`${GOVERNOR_BASE}/api/budget`, {
      headers: { accept: 'application/json' },
      signal: AbortSignal.timeout(REQUEST_MS),
    })
    if (!response.ok) {
      return { up: false, error: `Governor returned HTTP ${response.status}`, daily: null, monthly: null, circuit: null }
    }
    const body = await response.json()
    const dailyBudget = numberFrom(body.daily_budget_usd) ?? 0
    const dailySpent = numberFrom(body.today_spend_usd) ?? 0
    const monthlyBudget = numberFrom(body.monthly_budget_usd) ?? 0
    const monthlySpent = numberFrom(body.month_spend_usd) ?? 0
    return {
      up: true,
      error: null,
      daily: {
        budget: dailyBudget,
        spent: dailySpent,
        remaining: Math.max(0, dailyBudget - dailySpent),
        usedPct: dailyBudget > 0 ? Math.min(100, (dailySpent / dailyBudget) * 100) : 0,
      },
      monthly: {
        budget: monthlyBudget,
        spent: monthlySpent,
        remaining: Math.max(0, monthlyBudget - monthlySpent),
        usedPct: monthlyBudget > 0 ? Math.min(100, (monthlySpent / monthlyBudget) * 100) : 0,
      },
      cachedSavings: numberFrom(body.cached_savings_usd) ?? 0,
      requests: numberFrom(body.total_requests) ?? 0,
      cacheHitRate: numberFrom(body.cache_hit_rate_pct) ?? 0,
      circuit: {
        state: body.circuit_breaker_state ?? 'UNKNOWN',
        enabled: Boolean(body.circuit_breaker_enabled),
        reason: body.circuit_breaker_trip_reason ?? '',
      },
    }
  } catch (error) {
    return {
      up: false,
      error: `Governor unreachable: ${error instanceof Error ? error.message : String(error)}`,
      daily: null,
      monthly: null,
      circuit: null,
    }
  }
}

function providerFor(model) {
  const lower = String(model ?? '').toLowerCase()
  for (const provider of PROVIDERS) {
    if (provider.matches(lower)) return provider.id
  }
  return 'other'
}

async function collectLedger(ledgerPath) {
  const result = {
    path: ledgerPath,
    records: 0,
    error: null,
    byProvider: {},
    byModel: new Map(),
    daily: {},
  }
  if (!ledgerPath) {
    result.error = 'No ledger path configured.'
    return result
  }
  let text
  try {
    text = await readFile(ledgerPath, 'utf8')
  } catch (error) {
    result.error = `Ledger not readable: ${error instanceof Error ? error.message : String(error)}`
    return result
  }
  const today = dayKey(nowMs())
  const weekAgo = nowMs() - 7 * DAY_MS
  for (const line of text.split(/\r?\n/)) {
    if (!line.trim()) continue
    let record
    try {
      record = JSON.parse(line)
    } catch {
      continue
    }
    const ended = numberFrom(record.ended_unix_ms) ?? numberFrom(record.started_unix_ms) ?? 0
    if (!ended) continue
    const cost = numberFrom(record.cost) ?? 0
    const model = String(record.model ?? 'unknown')
    const provider = providerFor(model)
    const aggregate = (result.byProvider[provider] ??= {
      calls: 0,
      cost: 0,
      costToday: 0,
      cost7d: 0,
      input: 0,
      output: 0,
    })
    aggregate.calls += 1
    aggregate.cost += cost
    if (dayKey(ended) === today) aggregate.costToday += cost
    if (ended >= weekAgo) aggregate.cost7d += cost
    const usage = record.usage ?? {}
    aggregate.input += numberFrom(usage.input) ?? 0
    aggregate.output += numberFrom(usage.output) ?? 0

    let perModel = result.byModel.get(model)
    if (!perModel) {
      perModel = {
        model,
        provider,
        calls: 0,
        cost: 0,
        costToday: 0,
        cost7d: 0,
      }
      result.byModel.set(model, perModel)
    }
    perModel.calls += 1
    perModel.cost += cost
    if (dayKey(ended) === today) perModel.costToday += cost
    if (ended >= weekAgo) perModel.cost7d += cost

    result.records += 1
    const day = dayKey(ended)
    const daily = (result.daily[day] ??= {})
    daily[provider] = (daily[provider] ?? 0) + cost
  }
  return result
}

function providerUsage(ledger, providerId) {
  const aggregate = ledger.byProvider[providerId]
  if (!aggregate) {
    return { calls: 0, costToday: 0, cost7d: 0, costTotal: 0, input: 0, output: 0, models: [] }
  }
  return {
    calls: aggregate.calls,
    costToday: usd(aggregate.costToday),
    cost7d: usd(aggregate.cost7d),
    costTotal: usd(aggregate.cost),
    input: aggregate.input,
    output: aggregate.output,
    models: [],
  }
}

/**
 * Every routed model, whether or not it has been used: the gateway catalog is
 * the authority on availability, the ledger adds spend. Models seen only in the
 * ledger (a provider this machine used directly) are appended as unavailable.
 */
function buildModelRows(catalog, ledger) {
  const rows = new Map()
  for (const id of catalog.models) {
    rows.set(id, {
      id,
      provider: providerFor(id),
      available: catalog.up,
      calls: 0,
      costToday: 0,
      cost7d: 0,
      costTotal: 0,
    })
  }
  for (const entry of ledger.byModel.values()) {
    const row = rows.get(entry.model) ?? {
      id: entry.model,
      provider: entry.provider,
      available: false,
      calls: 0,
      costToday: 0,
      cost7d: 0,
      costTotal: 0,
    }
    row.calls += entry.calls
    row.costToday += entry.costToday
    row.cost7d += entry.cost7d
    row.costTotal += entry.cost
    rows.set(entry.model, row)
  }
  return [...rows.values()]
    .map((row) => ({
      ...row,
      costToday: usd(row.costToday),
      cost7d: usd(row.cost7d),
      costTotal: usd(row.costTotal),
    }))
    .sort((left, right) => right.costTotal - left.costTotal || left.id.localeCompare(right.id))
}

async function collect() {
  const ledgerPath = await resolveLedger()
  const ledger = await collectLedger(ledgerPath)
  const [catalog, budget] = await Promise.all([fetchCatalog(), fetchBudget()])

  const adminCredential = await keyFor(['ANTHROPIC_ADMIN_KEY', 'ANTHROPIC_ADMIN_API_KEY'])

  const providers = []
  for (const definition of PROVIDERS) {
    const credential = await keyFor(definition.keyNames)
    const entry = {
      id: definition.id,
      name: definition.name,
      colour: definition.colour,
      configured: definition.keyNames.length === 0 ? true : Boolean(credential),
      credentialName: credential?.name ?? null,
      balance: null,
      balanceError: null,
      balanceNote: definition.balanceNote,
      adminNote: null,
      usage: providerUsage(ledger, definition.id),
    }
    if (definition.id === 'deepseek') {
      try {
        const deepseek = await fetchDeepSeekBalance(credential)
        entry.configured = deepseek.configured
        entry.balance = deepseek.balance
      } catch (error) {
        entry.balanceError = error instanceof Error ? error.message : String(error)
      }
    }
    if (definition.id === 'anthropic' && adminCredential) {
      try {
        entry.adminNote = await fetchAnthropicSpend(adminCredential)
      } catch (error) {
        entry.balanceError = error instanceof Error ? error.message : String(error)
      }
    }
    providers.push(entry)
  }

  const days = []
  for (let offset = 13; offset >= 0; offset -= 1) {
    const key = dayKey(nowMs() - offset * DAY_MS)
    const day = ledger.daily[key] ?? {}
    days.push({
      day: key,
      total: usd(Object.values(day).reduce((sum, value) => sum + value, 0)),
      byProvider: Object.fromEntries(
        Object.entries(day).map(([provider, value]) => [provider, usd(value)]),
      ),
    })
  }

  const used = providers.filter((provider) => provider.usage.calls > 0)
  const totals = {
    costToday: providers.reduce((sum, provider) => sum + provider.usage.costToday, 0),
    cost7d: providers.reduce((sum, provider) => sum + provider.usage.cost7d, 0),
    costTotal: providers.reduce((sum, provider) => sum + provider.usage.costTotal, 0),
    calls: providers.reduce((sum, provider) => sum + provider.usage.calls, 0),
    currencies: [...new Set(providers.filter((p) => p.balance).map((p) => p.balance.currency))],
    modelsAvailable: catalog.models.length,
    modelsUsed: used.length,
  }

  const models = buildModelRows(catalog, ledger)

  return {
    generatedAt: new Date().toISOString(),
    ledger: {
      path: ledger.path,
      records: ledger.records,
      error: ledger.error,
    },
    catalog: {
      up: catalog.up,
      error: catalog.error,
      count: catalog.models.length,
      baseURL: GATEWAY_BASE,
    },
    budget: { ...budget, baseURL: GOVERNOR_BASE },
    providers,
    models,
    days,
    totals,
  }
}

async function payload() {
  if (cached && nowMs() - cached.at < CACHE_MS) return cached.value
  const value = await collect()
  cached = { at: nowMs(), value }
  return value
}

function send(res, status, body) {
  const text = JSON.stringify(body)
  res.writeHead(status, {
    'content-type': 'application/json; charset=utf-8',
    'cache-control': 'no-store',
    'content-length': Buffer.byteLength(text),
    'x-balance-dashboard': '1',
  })
  res.end(text)
}

export const name = 'balance-dashboard'

export const inject = ['webServer']

function isLoopbackHost(value) {
  if (!value) return false
  const host = String(value).replace(/^\[|\]$/g, '').split(':')[0].toLowerCase()
  return host === '127.0.0.1' || host === 'localhost' || host === '::1'
}

export function apply(ctx) {
  // Both routes answer on loopback alone. A same-origin browser GET does not
  // carry the API credential, and the response is readable only from this
  // origin (no CORS headers), so the browser's same-origin policy is the
  // boundary; a non-loopback Host is refused. No credential is read or echoed.
  const handler = async (req, res) => {
    if (!isLoopbackHost(req.headers.host)) {
      send(res, 403, { error: 'loopback only' })
      return
    }
    if (req.method !== 'GET' && req.method !== 'HEAD') {
      send(res, 405, { error: 'GET only' })
      return
    }
    try {
      send(res, 200, await payload())
    } catch (error) {
      send(res, 500, {
        error: error instanceof Error ? error.message : String(error),
      })
    }
  }

  ctx.effect(() => ctx.webServer.register({ kind: 'exact', path: ROUTE, handler }))
  ctx.effect(() =>
    ctx.webServer.register({ kind: 'exact', path: PAGE_ROUTE, handler }),
  )
}
