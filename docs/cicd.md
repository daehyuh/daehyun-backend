# CI/CD

GitHub Actions runs two stages:

- Pull requests to `main`: build the Spring Boot jar.
- Pushes to `main`: build the jar, then deploy to production when production deploy is enabled.

## CI

The build job uses Java 21 and runs:

```bash
./gradlew bootJar --no-daemon
```

The workflow also runs `chmod +x ./gradlew` because Linux runners require the Gradle wrapper to be executable.

## CD

Production deploys are intentionally gated. To enable them, add this repository variable:

```text
ENABLE_PROD_DEPLOY=true
```

Then add these repository secrets:

```text
PROD_SSH_HOST
PROD_SSH_USER
PROD_SSH_PRIVATE_KEY
PROD_APP_DIR
```

Optional:

```text
PROD_SSH_PORT
```

`PROD_APP_DIR` must point to the repository directory on the production server. The server must already have `deploy/.env.production` configured because that file contains production secrets and is ignored by Git.

After a merge to `main`, the deploy job connects to the production server, fast-forwards `main`, and runs:

```bash
bash deploy/deploy-bluegreen.sh
bash deploy/apply-host-nginx.sh
bash deploy/deploy-monitoring.sh
```

Host Nginx deployment is controlled by `deploy/.env.production`.

```bash
APPLY_HOST_NGINX=true
HOST_NGINX_SERVER_NAME=api.xn--vk1b177d.com
HOST_NGINX_UPSTREAM=http://127.0.0.1:18080
HOST_NGINX_CONF_PATH=/etc/nginx/conf.d/api.xn--vk1b177d.com.conf
```

The script validates the config with `nginx -t` before reloading. SSL certificates are not reissued during CI/CD.

Monitoring deployment is controlled by `deploy/.env.monitoring`.

```bash
ENABLE_MONITORING=true
GRAFANA_ADMIN_PASSWORD=<strong-password>
```

When enabled, CI/CD starts Prometheus, Grafana, Alertmanager, node-exporter, and cAdvisor after the app deployment.
