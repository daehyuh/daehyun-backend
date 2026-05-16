# Zero-Downtime Deployment

This project uses a blue-green deployment flow for production:

- `nginx` exposes the public API port.
- `app-blue` and `app-green` are interchangeable Spring Boot containers.
- The deploy script pulls a GHCR image tagged with the commit SHA, starts the inactive app, waits for `/actuator/health`, switches Nginx, then stops the old app.
- MariaDB stays on the same persistent Docker volume and is not recreated during app deployments.

## First-Time Setup

Create the environment file from the repository root:

```bash
cp .env.example .env
vi .env
```

For manual server deploys, copy it to the runtime path used by the deploy scripts:

```bash
cp .env deploy/.env.production
```

For CI/CD deployments, store the full file contents in the GitHub Actions secret `DEPLOY_ENV_FILE`. The workflow uses the SSH values for deployment and writes the remaining production values to `deploy/.env.production` on the server before running deployment scripts.

Set real values for:

- `SPRING_DATASOURCE_PASSWORD`
- `MARIADB_ROOT_PASSWORD`
- `PUBLIC_PORT`

CI/CD passes `APP_IMAGE` and `APP_IMAGE_TAG` automatically. Add them to the env file only for manual deployments or custom registries.

The host Nginx values also have production defaults for `api.xn--vk1b177d.com`. Add these only when the server path or domain differs:

- `HOST_NGINX_SERVER_NAME`
- `HOST_NGINX_SSL_CERT`
- `HOST_NGINX_SSL_KEY`
- `HOST_NGINX_CONF_PATH`

The production compose file binds MariaDB to `127.0.0.1` by default:

```bash
DB_BIND_ADDRESS=127.0.0.1
```

This keeps the DB reachable through SSH tunneling while avoiding public `0.0.0.0:3306` exposure.

## First Adoption From the Current Single-Container Setup

The current production app container already owns host port `8080`. A containerized Nginx proxy cannot bind the same port until that old container releases it.

Recommended adoption path:

1. Set `PUBLIC_PORT=18080` in `DEPLOY_ENV_FILE` or `deploy/.env.production`.
2. Run `bash deploy/deploy-bluegreen.sh`.
3. Verify the blue-green stack on `http://127.0.0.1:18080/actuator/health`.
4. Run `bash deploy/apply-host-nginx.sh` to point host Nginx to `127.0.0.1:18080`.
5. After traffic is flowing through Nginx, stop the old single `app` container.

If the API is exposed directly on `8080` with no host reverse proxy in front, the first migration needs a short controlled cutover to free port `8080`. After that first cutover, later deployments are zero-downtime blue-green swaps.

## Deploy

Run this from the repository root on the server:

```bash
bash deploy/deploy-bluegreen.sh
bash deploy/apply-host-nginx.sh
bash deploy/deploy-monitoring.sh
```

The script will:

1. Start MariaDB if needed.
2. Pull the inactive app image from GHCR.
3. Start the inactive app.
4. Wait for `/actuator/health`.
5. Reload Nginx to route traffic to the healthy app.
6. Stop the old app after a short drain period.

CI/CD passes the exact merge commit SHA as `APP_IMAGE_TAG`. For manual deploys you can set it directly:

```bash
APP_IMAGE=ghcr.io/daehyuh/daehyun-backend APP_IMAGE_TAG=<commit-sha> bash deploy/deploy-bluegreen.sh
```

If the GHCR package is private, set `GHCR_USERNAME` and `GHCR_TOKEN` in `DEPLOY_ENV_FILE` so the server can pull images.

`deploy/apply-host-nginx.sh` applies the host Nginx config, runs `nginx -t`, and reloads Nginx only if the config is valid. It does not issue or renew SSL certificates. Existing Let's Encrypt certificates stay managed by Certbot.

## Host Nginx

The recommended production flow is:

```text
Internet
-> host Nginx on 443 with Let's Encrypt
-> 127.0.0.1:18080 Docker Nginx
-> app-blue or app-green
```

Example values:

```bash
PUBLIC_PORT=18080
APPLY_HOST_NGINX=true
HOST_NGINX_SERVER_NAME=api.xn--vk1b177d.com
HOST_NGINX_UPSTREAM=http://127.0.0.1:18080
HOST_NGINX_SSL_CERT=/etc/letsencrypt/live/api.xn--vk1b177d.com/fullchain.pem
HOST_NGINX_SSL_KEY=/etc/letsencrypt/live/api.xn--vk1b177d.com/privkey.pem
HOST_NGINX_CONF_PATH=/etc/nginx/conf.d/api.xn--vk1b177d.com.conf
```

If the current config already lives under `/etc/nginx/sites-available`, set `HOST_NGINX_CONF_PATH` to that exact file path to avoid duplicate server blocks.

## Rollback

If a deployment passes health checks but you need to roll back manually, rerun the script after checking out the previous commit:

```bash
APP_IMAGE=ghcr.io/daehyuh/daehyun-backend APP_IMAGE_TAG=<previous-good-commit-sha> bash deploy/deploy-bluegreen.sh
```

## Useful Checks

```bash
docker compose -p daehyun-backend -f docker-compose.bluegreen.yml ps
curl -fsS http://127.0.0.1:18080/nginx-health
curl -fsS http://127.0.0.1:18080/actuator/health
```
