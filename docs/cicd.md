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
```
