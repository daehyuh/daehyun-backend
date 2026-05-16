# Monitoring and Disk Protection

This stack adds host and container monitoring for the Oracle Cloud VM.

## What It Shows

- Root disk usage and free space
- Host CPU and memory usage
- Docker container memory usage
- Spring Boot HTTP request rate
- Prometheus scrape target health

Prometheus collects metrics. Grafana displays them. `node-exporter` exposes VM metrics, including disk usage. `cAdvisor` exposes Docker container metrics.

## First-Time Setup

Monitoring uses the same production environment file as the app:

```bash
cp .env.example .env
vi .env
```

For CI/CD deployments, store the full file contents in the GitHub Actions secret `DEPLOY_ENV_FILE`. The workflow writes the non-SSH values to `.env` on the server before running deployment scripts.

The minimal file only needs:

```text
ENABLE_MONITORING=true
GRAFANA_ADMIN_PASSWORD=<strong-password>
```

All other monitoring values have defaults. Add them only when you need to override the bind address, port, retention, or Docker log rotation.

## Start Monitoring

Run the monitoring stack with the blue-green production stack:

```bash
bash deploy/deploy-monitoring.sh
```

Grafana and Prometheus bind to `127.0.0.1` by default, so they are not public.

CI/CD also runs `deploy/deploy-monitoring.sh` after the application deployment. It only starts the stack when `ENABLE_MONITORING=true`.

## Access Grafana

Open an SSH tunnel from your local machine:

```bash
ssh -L 3000:127.0.0.1:3000 ubuntu@SERVER_IP
```

Then open:

```text
http://localhost:3000
```

The dashboard is provisioned under `Daehyun / Daehyun Host Overview`.

## Access Prometheus

```bash
ssh -L 9090:127.0.0.1:9090 ubuntu@SERVER_IP
```

Then open:

```text
http://localhost:9090
```

## Log Rotation

All Docker Compose services now use json-file log rotation by default:

```text
DOCKER_LOG_MAX_SIZE=10m
DOCKER_LOG_MAX_FILE=5
```

This prevents Docker container logs from growing without a limit.

Existing containers need to be recreated before the new logging policy applies.

Override these values in `DEPLOY_ENV_FILE` only if the defaults are not enough.

## Alerts

Prometheus includes alert rules for:

- Root disk usage over 80%
- Root disk usage over 90%
- Spring app scrape failures
- Spring 5xx response rate

Alertmanager is included with a no-op receiver. Wire it to Discord, Slack, email, or Telegram before relying on notifications.
