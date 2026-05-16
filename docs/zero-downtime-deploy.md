# Zero-Downtime Deployment

This project uses a blue-green deployment flow for production:

- `nginx` exposes the public API port.
- `app-blue` and `app-green` are interchangeable Spring Boot containers.
- The deploy script starts the inactive app, waits for `/actuator/health`, switches Nginx, then stops the old app.
- MariaDB stays on the same persistent Docker volume and is not recreated during app deployments.

## First-Time Setup

Create the production environment file on the server:

```bash
cp deploy/.env.production.example deploy/.env.production
vi deploy/.env.production
```

Set real values for:

- `SPRING_DATASOURCE_PASSWORD`
- `MARIADB_ROOT_PASSWORD`
- `PUBLIC_PORT`

The production compose file binds MariaDB to `127.0.0.1` by default:

```bash
DB_BIND_ADDRESS=127.0.0.1
```

This keeps the DB reachable through SSH tunneling while avoiding public `0.0.0.0:3306` exposure.

## First Adoption From the Current Single-Container Setup

The current production app container already owns host port `8080`. A containerized Nginx proxy cannot bind the same port until that old container releases it.

Recommended adoption path:

1. Set `PUBLIC_PORT=18080` in `deploy/.env.production`.
2. Run `bash deploy/deploy-bluegreen.sh`.
3. Verify the blue-green stack on `http://127.0.0.1:18080/actuator/health`.
4. Point the existing host reverse proxy or load balancer to `127.0.0.1:18080`.
5. After traffic is flowing through Nginx, stop the old single `app` container.

If the API is exposed directly on `8080` with no host reverse proxy in front, the first migration needs a short controlled cutover to free port `8080`. After that first cutover, later deployments are zero-downtime blue-green swaps.

## Deploy

Run this from the repository root on the server:

```bash
bash deploy/deploy-bluegreen.sh
```

The script will:

1. Start MariaDB if needed.
2. Build the inactive app container.
3. Start the inactive app.
4. Wait for `/actuator/health`.
5. Reload Nginx to route traffic to the healthy app.
6. Stop the old app after a short drain period.

## Rollback

If a deployment passes health checks but you need to roll back manually, rerun the script after checking out the previous commit:

```bash
git checkout <previous-good-commit>
bash deploy/deploy-bluegreen.sh
```

## Useful Checks

```bash
docker compose -p daehyun-backend -f docker-compose.bluegreen.yml ps
curl -fsS http://127.0.0.1:8080/nginx-health
curl -fsS http://127.0.0.1:8080/actuator/health
```
