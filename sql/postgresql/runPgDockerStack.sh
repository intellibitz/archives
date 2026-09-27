#!/usr/bin/env sh
# https://github.com/docker-library/docs/blob/master/postgres/README.md

# Deploy as a Docker Swarm stack
docker stack deploy -c stack.yml postgres

# Or run using docker compose
# docker compose -f stack.yml up -d

# Wait for containers to initialize completely, then visit Adminer at:
# http://localhost:8080 or http://<host-ip>:8080
