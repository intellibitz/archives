#

    https://caddyserver.com/

Caddy 2 is a powerful, enterprise-ready, open source web server with automatic HTTPS written in Go

1-LINERS
--

These commands are production-ready. When given a domain name, Caddy will use HTTPS by default, which provisions and renews certificates for you.*

- Requires domain's public A/AAAA DNS records pointed at your machine.

Quick, local file server

    caddy file-server

Public file server over HTTPS

    caddy file-server --domain example.com

HTTPS reverse proxy

    caddy reverse-proxy --from example.com --to localhost:9000

Run server with Caddyfile in working directory (if present)

    caddy run

THE CADDYFILE
--

A config file that's human-readable and easy to write by hand. Perfect for most common and manual configurations.

Local file server with template evaluation

    localhost

    templates
    file_server

HTTPS reverse proxy with custom load balancing and active health checks

    example.com # Your site's domain name

    # Load balance between three backends with custom health checks
    reverse_proxy 10.0.0.1:9000 10.0.0.2:9000 10.0.0.3:9000 {
        lb_policy       random_choose 2
        health_path     /ok
        health_interval 10s
    }

HTTPS site with clean URLs, reverse proxying, compression, and templates

    example.com

    # Templates give static sites some dynamic features
    templates

    # Compress responses according to Accept-Encoding headers
    encode gzip zstd

    # Make HTML file extension optional
    try_files {path}.html {path}

    # Send API requests to backend
    reverse_proxy /api/* localhost:9005

    # Serve everything else from the file system
    file_server

CONFIG API
--

Caddy is dynamically configurable with a RESTful JSON API. Config updates are graceful, even on Windows.

Using JSON gives you absolute control over the edge of your compute platform, and is perfect for dynamic and automated deployments.
Set a new configuration

    POST /config/

    {
    "apps": {
        "http": {
        "servers": {
            "example": {
            "listen": ["127.0.0.1:2080"],
            "routes": [{
                "@id": "demo",
                "handle": [{
                "handler": "file_server",
                "browse": {}
                }]
            }]
            }
        }
        }
    }
    }

Export current configuration

    GET /config/

Change only a specific part of the config

    PUT /id/demo/handle/0

    {"handler": "templates"}

All changes made through the API are persisted to disk so they can continue to be used after restarts.

Web Protocols
--

HTTP/1.1
Still commonly used in plaintext, development, and debug environments, Caddy has solid support for HTTP/1.1.

HTTP/2
It's time for a faster web. Caddy uses HTTP/2 right out of the box. No thought required. HTTP/1.1 is still used when clients don't support HTTP/2.

HTTP/3
With the IETF-standard-draft version of QUIC, sites load faster and connections aren't dropped when switching networks.

WEBSOCKETS
Caddy proxies WebSocket upgrades with ease.

IPV6
Caddy supports both IPv4 and IPv6. In fact, Caddy runs full well in an IPv6 environment without extra configuration.

FASTCGI
Serve your PHP site behind Caddy securely with just one simple line of configuration. You can even specify multiple backends.

Reverse Proxy
BASIC PROXYING
Caddy can act as a reverse proxy for HTTP requests. You can also proxy transparently (preserve the original Host header) with one line of config.

LOAD BALANCING
Proxy to multiple backends using a load balancing policy of your choice: random, least connections, round robin, IP hash, or header.

SSL TERMINATION
Caddy is frequently used as a TLS terminator because of its powerful TLS features.

WEBSOCKET PROXY
Caddy's proxy middleware is capable of proxying websocket connections to backends as well.

HEALTH CHECKS
Caddy marks backends in trouble as unhealthy, and you can configure health check paths, intervals, and timeouts for optimal performance.

RETRIES
When a request to a backend fails to connect, Caddy will try the request with other backends until one that is online accepts the connection.

HEADER CONTROLS
By default, most headers will be carried through, but you can control which headers flow upstream and downstream.

DYNAMIC BACKENDS
Proxy to arbitrary backends based on request parameters such as parts of the domain name or header values.

Amenities
CLEAN URIS
Elegantly serve files without needing the extension present in the URL. These look nicer to visitors and are easy to configure.

REWRITES
Caddy has powerful request URI rewriting capabilities that support regular expressions, conditionals, and dynamic values.

RESPONSE STATUS CODES
Send a certain status code for certain requests.

COMPRESSION
Compress content on-the-fly using gzip, Zstandard, or brotli.

#
