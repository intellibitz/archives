window.__ModuleLoader__.load({
  id: '@local/balance-dashboard',
  factory(require) {
    const React = require('react');
    const h = React.createElement;
    const NS = 'balance-dashboard';

    const DICTS = {
      en: {
        'panel.label': 'Balances',
        'page.title': 'Balances & billing',
        'page.subtitle':
          'Budget headroom, provider balances, every routed model and the spend recorded for each.',
        'page.refresh': 'Refresh',
        'page.updated': 'Updated',
        'page.loading': 'Loading balances…',
        'page.error': 'Could not load balances',
        'page.today': 'Spend today',
        'page.week': 'Last 7 days',
        'page.total': 'All time',
        'page.calls': 'Calls',
        'page.balance': 'Balance',
        'page.usedToday': 'Today',
        'page.used7d': '7 days',
        'page.usedTotal': 'All time',
        'page.tokens': 'Tokens',
        'page.models': 'Spend by model',
        'page.model': 'Model',
        'page.provider': 'Provider',
        'page.cost': 'Cost',
        'page.spend': 'Spend, last 14 days',
        'page.noBalance': 'No balance endpoint',
        'page.notConfigured': 'Key not configured',
        'page.configured': 'Key configured',
        'page.errorBadge': 'Error',
        'page.empty': 'No recorded spend yet.',
        'page.ledger': 'Ledger',
        'page.records': 'calls recorded',
        'page.currency': 'Reported in',
        'page.budget': 'Budget headroom',
        'page.dailyBudget': 'Daily budget',
        'page.monthlyBudget': 'Monthly budget',
        'page.remaining': 'Remaining',
        'page.circuit': 'Circuit breaker',
        'page.cachedSavings': 'Cached savings',
        'page.available': 'Available',
        'page.idle': 'idle',
        'page.modelCount': 'Models available',
        'page.allModels': 'All models',
        'page.budgetDown': 'Budget governor unreachable',
      },
      zh: {
        'panel.label': '余额',
        'page.title': '余额与账单',
        'page.subtitle': 'DeepSeek、Gemini 与 Claude 的余额与已记录支出。',
        'page.refresh': '刷新',
        'page.updated': '更新于',
        'page.loading': '正在加载余额…',
        'page.error': '无法加载余额',
        'page.today': '今日支出',
        'page.week': '最近 7 天',
        'page.total': '累计',
        'page.calls': '调用次数',
        'page.balance': '余额',
        'page.usedToday': '今日',
        'page.used7d': '7 天',
        'page.usedTotal': '累计',
        'page.tokens': 'Token',
        'page.models': '按模型支出',
        'page.model': '模型',
        'page.provider': '供应商',
        'page.cost': '费用',
        'page.spend': '最近 14 天支出',
        'page.noBalance': '接口不提供余额',
        'page.notConfigured': '未配置密钥',
        'page.configured': '已配置密钥',
        'page.errorBadge': '错误',
        'page.empty': '暂无已记录的支出。',
        'page.ledger': '账本',
        'page.records': '条调用记录',
        'page.currency': '币种',
        'page.budget': '预算余量',
        'page.dailyBudget': '每日预算',
        'page.monthlyBudget': '每月预算',
        'page.remaining': '剩余',
        'page.circuit': '熔断器',
        'page.cachedSavings': '缓存节省',
        'page.available': '可用',
        'page.idle': '未使用',
        'page.modelCount': '可用模型',
        'page.allModels': '全部模型',
        'page.budgetDown': '预算治理服务不可达',
      },
    };

    const money = (value) => {
      const number = Number(value);
      if (!Number.isFinite(number)) return '—';
      if (number !== 0 && Math.abs(number) < 0.01) return '<$0.01';
      return `$${number.toFixed(number >= 100 ? 0 : 2)}`;
    };

    const amount = (balance, key) => {
      const value = balance?.[key];
      return typeof value === 'number' ? value : null;
    };

    const iconStyle = (size) => ({ display: 'block', width: size, height: size });

    function WalletIcon({ size = 18, active = false }) {
      const stroke = active ? 'var(--dsw-alias-brand-primary)' : 'currentColor';
      return h(
        'svg',
        {
          viewBox: '0 0 24 24',
          width: size,
          height: size,
          fill: 'none',
          stroke,
          strokeWidth: 1.7,
          strokeLinecap: 'round',
          strokeLinejoin: 'round',
          'aria-hidden': true,
          style: iconStyle(size),
        },
        h('path', {
          key: 'body',
          d: 'M3.5 7.5A2.5 2.5 0 0 1 6 5h11.5a1 1 0 0 1 1 1v1.5',
        }),
        h('rect', {
          key: 'wallet',
          x: 3.5,
          y: 7.5,
          width: 17,
          height: 11,
          rx: 2.4,
        }),
        h('path', { key: 'slot', d: 'M14.5 13h3' }),
      );
    }

    const card = {
      border: '1px solid var(--dsw-alias-border-l1)',
      borderRadius: 10,
      background: 'var(--dsw-alias-bg-layer-2)',
      padding: '14px 16px',
      minWidth: 0,
    };

    const label = {
      fontSize: 11,
      letterSpacing: '.06em',
      textTransform: 'uppercase',
      color: 'var(--dsw-alias-label-secondary)',
    };

    const button = {
      font: 'inherit',
      fontSize: 12,
      color: 'var(--dsw-alias-label-primary)',
      background: 'var(--dsw-alias-bg-layer-2)',
      border: '1px solid var(--dsw-alias-border-l2)',
      borderRadius: 6,
      padding: '5px 10px',
      cursor: 'pointer',
    };

    function StatusDot({ ok, warn }) {
      const colour = ok
        ? 'var(--dsw-alias-state-success-primary)'
        : warn
          ? 'var(--dsw-alias-state-warn-primary)'
          : 'var(--dsw-alias-state-error-primary)';
      return h('span', {
        key: 'dot',
        style: {
          display: 'inline-block',
          width: 7,
          height: 7,
          borderRadius: '50%',
          background: colour,
          flex: '0 0 auto',
        },
      });
    }

    function ProviderCard({ provider, t }) {
      const balance = provider.balance;
      const usage = provider.usage ?? {};
      const hasBalance = balance && amount(balance, 'total') !== null;
      const errored = Boolean(provider.balanceError);
      const rows = [
        [t('page.usedToday'), money(usage.costToday)],
        [t('page.used7d'), money(usage.cost7d)],
        [t('page.usedTotal'), money(usage.costTotal)],
        [
          t('page.calls'),
          `${Number(usage.calls ?? 0).toLocaleString()} · ${Number(usage.input ?? 0).toLocaleString()} in / ${Number(usage.output ?? 0).toLocaleString()} out`,
        ],
      ];
      const notes = [];
      if (provider.balanceNote) notes.push(provider.balanceNote);
      if (!provider.configured) notes.push(t('page.notConfigured'));
      if (provider.adminNote?.cents !== undefined) {
        notes.push(`${provider.adminNote.note} $${(provider.adminNote.cents / 100).toFixed(2)}`);
      }
      return h(
        'div',
        { style: card },
        h(
          'div',
          {
            style: {
              display: 'flex',
              alignItems: 'center',
              gap: 8,
              marginBottom: 10,
            },
          },
          h(StatusDot, { ok: provider.configured && !errored, warn: !hasBalance && !errored }),
          h(
            'strong',
            { style: { fontSize: 13, letterSpacing: '.02em' } },
            provider.name,
          ),
          h(
            'span',
            {
              style: {
                marginLeft: 'auto',
                fontSize: 10,
                color: 'var(--dsw-alias-label-secondary)',
              },
            },
            errored
              ? t('page.errorBadge')
              : hasBalance
                ? balance.currency
                : provider.configured
                  ? t('page.configured')
                  : t('page.notConfigured'),
          ),
        ),
        h(
          'div',
          {
            style: {
              display: 'flex',
              alignItems: 'baseline',
              gap: 8,
              marginBottom: 2,
            },
          },
          h(
            'span',
            {
              style: {
                fontSize: 26,
                fontWeight: 600,
                letterSpacing: '-.02em',
                color: hasBalance
                  ? 'var(--dsw-alias-label-primary)'
                  : 'var(--dsw-alias-label-secondary)',
              },
            },
            hasBalance ? money(amount(balance, 'total')) : '—',
          ),
          h('span', { style: { ...label, textTransform: 'none', letterSpacing: 0 } }, t('page.balance')),
        ),
        hasBalance &&
          h(
            'div',
            {
              style: {
                fontSize: 11,
                color: 'var(--dsw-alias-label-secondary)',
                marginBottom: 8,
              },
            },
            `granted ${money(amount(balance, 'granted'))} · topped up ${money(amount(balance, 'toppedUp'))}${balance.available === false ? ' · account unavailable' : ''}`,
          ),
        !hasBalance &&
          !errored &&
          h(
            'div',
            {
              style: {
                fontSize: 11,
                color: 'var(--dsw-alias-label-secondary)',
                marginBottom: 8,
              },
            },
            t('page.noBalance'),
          ),
        provider.balanceError &&
          h(
            'div',
            {
              style: {
                fontSize: 11,
                color: 'var(--dsw-alias-state-error-primary)',
                marginBottom: 8,
                wordBreak: 'break-word',
              },
            },
            provider.balanceError,
          ),
        h(
          'div',
          {
            style: {
              display: 'grid',
              gridTemplateColumns: '1fr auto',
              rowGap: 4,
              columnGap: 8,
              borderTop: '1px solid var(--dsw-alias-border-l1)',
              paddingTop: 8,
              marginTop: 4,
            },
          },
          rows.flatMap(([key, value]) => [
            h(
              'span',
              { key: `${key}-k`, style: { fontSize: 11, color: 'var(--dsw-alias-label-secondary)' } },
              key,
            ),
            h(
              'span',
              {
                key: `${key}-v`,
                style: { fontSize: 11, textAlign: 'right', fontVariantNumeric: 'tabular-nums' },
              },
              value,
            ),
          ]),
        ),
        notes.length > 0 &&
          h(
            'div',
            {
              style: {
                marginTop: 8,
                fontSize: 10.5,
                lineHeight: 1.5,
                color: 'var(--dsw-alias-label-secondary)',
              },
            },
            notes.join(' '),
          ),
      );
    }

    function SpendChart({ days, t }) {
      const list = Array.isArray(days) ? days : [];
      const max = Math.max(1e-9, ...list.map((day) => Number(day.total) || 0));
      return h(
        'div',
        { style: { ...card, marginTop: 16 } },
        h('div', { style: { ...label, marginBottom: 10 } }, t('page.spend')),
        h(
          'div',
          {
            style: {
              display: 'flex',
              alignItems: 'flex-end',
              gap: 4,
              height: 110,
            },
          },
          list.map((day) =>
            h(
              'div',
              {
                key: day.day,
                title: `${day.day}: ${money(day.total)}`,
                style: {
                  flex: '1 1 0',
                  display: 'flex',
                  flexDirection: 'column-reverse',
                  height: '100%',
                  minWidth: 6,
                },
              },
              Object.entries(day.byProvider ?? {}).map(([provider, value]) =>
                h('div', {
                  key: provider,
                  style: {
                    height: `${Math.max(0, (Number(value) / max) * 100)}%`,
                    background:
                      provider === 'deepseek'
                        ? 'var(--dsw-alias-brand-primary)'
                        : provider === 'anthropic'
                          ? 'var(--dsw-alias-state-warn-primary)'
                          : provider === 'google'
                            ? 'var(--dsw-alias-state-success-primary)'
                            : 'var(--dsw-alias-state-idle-primary)',
                    borderRadius: 2,
                    marginTop: 1,
                    minHeight: Number(value) > 0 ? 2 : 0,
                  },
                }),
              ),
            ),
          ),
        ),
        h(
          'div',
          {
            style: {
              display: 'flex',
              justifyContent: 'space-between',
              marginTop: 6,
              fontSize: 10,
              color: 'var(--dsw-alias-label-secondary)',
            },
          },
          h('span', null, list[0]?.day ?? ''),
          h('span', null, list[list.length - 1]?.day ?? ''),
        ),
      );
    }

    function BudgetCards({ budget, totals, t }) {
      if (!budget?.up) {
        return h(
          'div',
          {
            style: {
              ...card,
              marginTop: 16,
              padding: '10px 14px',
              fontSize: 11.5,
              color: 'var(--dsw-alias-state-warn-primary)',
            },
          },
          `${t('page.budgetDown')}${budget?.error ? `: ${budget.error}` : ''}`,
        );
      }
      const items = [];
      if (budget.daily) {
        items.push({
          key: t('page.dailyBudget'),
          value: money(budget.daily.remaining),
          sub: `${money(budget.daily.spent)} / ${money(budget.daily.budget)}`,
          pct: budget.daily.usedPct,
        });
      }
      if (budget.monthly) {
        items.push({
          key: t('page.monthlyBudget'),
          value: money(budget.monthly.remaining),
          sub: `${money(budget.monthly.spent)} / ${money(budget.monthly.budget)}`,
          pct: budget.monthly.usedPct,
        });
      }
      items.push({
        key: t('page.modelCount'),
        value: String(totals?.modelsAvailable ?? 0),
        sub: `${totals?.modelsUsed ?? 0} ${t('page.idle')}`,
        pct: null,
      });
      items.push({
        key: t('page.cachedSavings'),
        value: money(budget.cachedSavings ?? 0),
        sub: `${Number(budget.cacheHitRate ?? 0).toFixed(1)}%`,
        pct: null,
      });
      items.push({
        key: t('page.circuit'),
        value: budget.circuit?.state ?? '—',
        sub: budget.circuit?.enabled
          ? budget.circuit.reason || 'enabled'
          : 'disabled',
        pct: null,
      });
      return h(
        'div',
        {
          style: {
            marginTop: 16,
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))',
            gap: 10,
          },
        },
        items.map((item) =>
          h(
            'div',
            { key: item.key, style: { ...card, padding: '12px 14px' } },
            h('div', { style: label }, item.key),
            h(
              'div',
              {
                style: {
                  fontSize: 20,
                  fontWeight: 600,
                  fontVariantNumeric: 'tabular-nums',
                  marginTop: 2,
                },
              },
              item.value,
            ),
            h(
              'div',
              {
                style: {
                  marginTop: 2,
                  fontSize: 10.5,
                  color: 'var(--dsw-alias-label-secondary)',
                },
              },
              item.sub,
            ),
            item.pct === null || item.pct === undefined
              ? null
              : h(
                  'div',
                  {
                    style: {
                      marginTop: 8,
                      height: 4,
                      borderRadius: 2,
                      background: 'var(--dsw-alias-border-l1)',
                      overflow: 'hidden',
                    },
                  },
                  h('div', {
                    style: {
                      width: `${Math.max(0, Math.min(100, item.pct))}%`,
                      height: '100%',
                      background: 'var(--dsw-alias-brand-primary)',
                    },
                  }),
                ),
          ),
        ),
      );
    }

    function ModelTable({ models, t }) {
      const rows = [...(models ?? [])];
      return h(
        'div',
        { style: { ...card, marginTop: 16, padding: 0, overflow: 'hidden' } },
        h(
          'div',
          { style: { ...label, padding: '12px 16px 8px' } },
          `${t('page.allModels')} · ${rows.length}`,
        ),
        rows.length === 0
          ? h(
              'div',
              {
                style: {
                  padding: '4px 16px 14px',
                  fontSize: 12,
                  color: 'var(--dsw-alias-label-secondary)',
                },
              },
              t('page.empty'),
            )
          : h(
              'table',
              {
                style: {
                  width: '100%',
                  borderCollapse: 'collapse',
                  fontSize: 12,
                },
              },
              h(
                'thead',
                null,
                h(
                  'tr',
                  { style: { color: 'var(--dsw-alias-label-secondary)', textAlign: 'left' } },
                  [
                    t('page.model'),
                    t('page.provider'),
                    t('page.available'),
                    t('page.calls'),
                    t('page.usedToday'),
                    t('page.total'),
                  ].map((heading, index) =>
                    h(
                      'th',
                      {
                        key: heading,
                        style: {
                          fontWeight: 500,
                          padding: '4px 16px 6px',
                          textAlign: index >= 3 ? 'right' : 'left',
                        },
                      },
                      heading,
                    ),
                  ),
                ),
              ),
              h(
                'tbody',
                null,
                rows.map((row) =>
                  h(
                    'tr',
                    {
                      key: row.id,
                      style: { borderTop: '1px solid var(--dsw-alias-border-l1)' },
                    },
                    h('td', { style: { padding: '6px 16px' } }, row.id),
                    h(
                      'td',
                      { style: { padding: '6px 16px', color: 'var(--dsw-alias-label-secondary)' } },
                      row.provider,
                    ),
                    h(
                      'td',
                      { style: { padding: '6px 16px' } },
                      h(
                        'span',
                        {
                          style: {
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: 6,
                            fontSize: 11,
                            color: row.available
                              ? 'var(--dsw-alias-state-success-primary)'
                              : 'var(--dsw-alias-label-secondary)',
                          },
                        },
                        h('span', {
                          style: {
                            width: 6,
                            height: 6,
                            borderRadius: 3,
                            background: row.available
                              ? 'var(--dsw-alias-state-success-primary)'
                              : 'var(--dsw-alias-border-l2)',
                          },
                        }),
                        row.available ? t('page.available') : t('page.idle'),
                      ),
                    ),
                    h(
                      'td',
                      { style: { padding: '6px 16px', textAlign: 'right', fontVariantNumeric: 'tabular-nums' } },
                      Number(row.calls ?? 0).toLocaleString(),
                    ),
                    h(
                      'td',
                      { style: { padding: '6px 16px', textAlign: 'right', fontVariantNumeric: 'tabular-nums' } },
                      money(row.costToday),
                    ),
                    h(
                      'td',
                      { style: { padding: '6px 16px', textAlign: 'right', fontVariantNumeric: 'tabular-nums' } },
                      money(row.costTotal),
                    ),
                  ),
                ),
              ),
            ),
      );
    }

    function BalancePage() {
      const t = BalancePage.translate;
      const [state, setState] = React.useState({ status: 'loading', data: null, error: null });
      const [generation, setGeneration] = React.useState(0);

      React.useEffect(() => {
        let cancelled = false;
        const controller = new AbortController();
        const load = async () => {
          try {
            // The page's loopback route answers without the API credential;
            // the authenticated route stays as a fallback for deployments that
            // gate loopback reads.
            let response = await fetch('/balance-dashboard/data', {
              headers: { accept: 'application/json' },
              credentials: 'same-origin',
              signal: controller.signal,
            });
            if (response.status === 404 || response.status === 403) {
              response = await fetch('/api/balance-dashboard', {
                headers: { accept: 'application/json' },
                credentials: 'same-origin',
                signal: controller.signal,
              });
            }
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            const data = await response.json();
            if (!cancelled) setState({ status: 'ready', data, error: null });
          } catch (error) {
            if (!cancelled && error?.name !== 'AbortError') {
              setState((previous) => ({
                status: previous.data ? 'ready' : 'error',
                data: previous.data,
                error: error instanceof Error ? error.message : String(error),
              }));
            }
          }
        };
        setState((previous) => ({ ...previous, status: previous.data ? 'ready' : 'loading' }));
        load();
        const timer = window.setInterval(load, 60_000);
        return () => {
          cancelled = true;
          controller.abort();
          window.clearInterval(timer);
        };
      }, [generation]);

      const data = state.data;
      const totals = data?.totals ?? {};
      const updated = data?.generatedAt ? new Date(data.generatedAt) : null;

      return h(
        'div',
        {
          style: {
            display: 'flex',
            flexDirection: 'column',
            height: '100%',
            minHeight: 0,
            background: 'var(--dsw-alias-bg-base)',
            color: 'var(--dsw-alias-label-primary)',
          },
        },
        h(
          'div',
          {
            style: {
              display: 'flex',
              alignItems: 'center',
              gap: 10,
              padding: '12px 20px',
              borderBottom: '1px solid var(--dsw-alias-border-l1)',
              flex: '0 0 auto',
            },
          },
          h(
            'div',
            { style: { minWidth: 0 } },
            h('div', { style: { fontSize: 15, fontWeight: 600 } }, t('page.title')),
            h(
              'div',
              { style: { fontSize: 11.5, color: 'var(--dsw-alias-label-secondary)' } },
              t('page.subtitle'),
            ),
          ),
          h('span', { style: { marginLeft: 'auto' } }),
          updated &&
            h(
              'span',
              {
                style: {
                  fontSize: 11,
                  color: 'var(--dsw-alias-label-secondary)',
                  whiteSpace: 'nowrap',
                },
              },
              `${t('page.updated')} ${updated.toLocaleTimeString()}`,
            ),
          h(
            'button',
            {
              type: 'button',
              onClick: () => setGeneration((value) => value + 1),
              style: button,
            },
            t('page.refresh'),
          ),
        ),
        h(
          'div',
          { style: { flex: '1 1 auto', minHeight: 0, overflow: 'auto', padding: 20 } },
          state.status === 'loading' &&
            h(
              'div',
              { style: { fontSize: 12, color: 'var(--dsw-alias-label-secondary)' } },
              t('page.loading'),
            ),
          state.status === 'error' &&
            h(
              'div',
              { style: { fontSize: 12, color: 'var(--dsw-alias-state-error-primary)' } },
              `${t('page.error')}: ${state.error ?? ''}`,
            ),
          data &&
            h(
              React.Fragment,
              null,
              h(
                'div',
                {
                  style: {
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
                    gap: 10,
                  },
                },
                [
                  [t('page.today'), money(totals.costToday)],
                  [t('page.week'), money(totals.cost7d)],
                  [t('page.total'), money(totals.costTotal)],
                  [t('page.calls'), Number(totals.calls ?? 0).toLocaleString()],
                ].map(([key, value]) =>
                  h(
                    'div',
                    { key, style: { ...card, padding: '12px 14px' } },
                    h('div', { style: label }, key),
                    h(
                      'div',
                      {
                        style: {
                          fontSize: 20,
                          fontWeight: 600,
                          fontVariantNumeric: 'tabular-nums',
                          marginTop: 2,
                        },
                      },
                      value,
                    ),
                  ),
                ),
              ),
              h(
                'div',
                {
                  style: {
                    marginTop: 14,
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
                    gap: 12,
                  },
                },
                (data.providers ?? []).map((provider) =>
                  h(ProviderCard, { key: provider.id, provider, t }),
                ),
              ),
              h(SpendChart, { days: data.days, t }),
              h(BudgetCards, { budget: data.budget, totals: data.totals, t }),
              h(ModelTable, { models: data.models, t }),
              data.ledger?.error &&
                h(
                  'div',
                  {
                    style: {
                      marginTop: 12,
                      fontSize: 11,
                      color: 'var(--dsw-alias-state-warn-primary)',
                    },
                  },
                  `${t('page.ledger')}: ${data.ledger.error}`,
                ),
              data.ledger?.path &&
                h(
                  'div',
                  {
                    style: {
                      marginTop: 12,
                      fontSize: 10.5,
                      color: 'var(--dsw-alias-label-secondary)',
                      wordBreak: 'break-all',
                    },
                  },
                  `${t('page.ledger')}: ${data.ledger.path} · ${Number(data.ledger.records ?? 0).toLocaleString()} ${t('page.records')}`,
                ),
            ),
        ),
      );
    }

    return {
      inject: ['slots', 'locale'],
      apply(ctx) {
        const t = ctx.locale.bind(NS);
        BalancePage.translate = t;
        ctx.effect(() => ctx.locale.register(NS, 'en', DICTS.en));
        ctx.effect(() => ctx.locale.register(NS, 'zh', DICTS.zh));
        ctx.slots.inject('sidebar.panellist', () =>
          ctx.slots.register(
            {
              name: 'sidebar.panellist',
              id: 'balances',
              order: 22,
              label: () => t('panel.label'),
            },
            (props) => h(WalletIcon, props),
          ),
        );
        ctx.slots.inject('main', () =>
          ctx.slots.register({ name: 'main', key: 'balances' }, BalancePage),
        );
      },
    };
  },
});
