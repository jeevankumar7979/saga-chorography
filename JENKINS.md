# Jenkins CI/CD

Each push to `main` can build and deploy the complete application:

1. Compile all three Spring Boot services.
2. Build Docker images.
3. Push commit-tagged images to Docker Hub.
4. Apply the Kubernetes resources from the separate files in `k8s/`.
5. Update the three application deployments to the pushed image tag.
6. Wait for all three rollouts to complete.

## Jenkins agent requirements

The Jenkins agent must have:

- Docker CLI and permission to access the Docker daemon.
- Java 21 and Maven (the pipeline uses each service's Maven wrapper).
- `kubectl`.
- Network access to Docker Hub and the Kubernetes API server.

## Jenkins credentials

Create these credentials in **Manage Jenkins → Credentials → Global**:

| ID | Type | Value |
|---|---|---|
| `dockerhub-credentials` | Username with password | Docker Hub username `jeevan7979` and a Docker Hub access token |
| `saga-kubeconfig` | Secret file | Kubeconfig for the target cluster |

Never commit either credential to GitHub. The pipeline uses the commit SHA as the image tag, so every deployment is traceable and reproducible.

## Create the Jenkins job

Create a **Pipeline** job and configure:

- **Pipeline definition:** Pipeline script from SCM
- **SCM:** Git
- **Repository URL:** `git@github.com:Jeevankumar7979/saga-chorography.git`
- **Credentials:** an SSH private key credential with access to the repository
- **Branch:** `*/main`
- **Script path:** `Jenkinsfile`

Build the Jenkins controller image from `jenkins/Dockerfile` before creating
the job. Mount `/var/run/docker.sock` into the Jenkins container so the
pipeline can build Docker images.

For automatic builds, add this GitHub webhook:

```text
https://<your-jenkins-host>/github-webhook/
```

Enable **GitHub hook trigger for GITScm polling** in the job. The Jenkins host must be reachable by GitHub; use a public HTTPS URL or a secure tunnel for local Jenkins.

## Docker Hub repositories

The first push requires these Docker Hub repositories to exist, unless the account allows Jenkins to create them:

```text
jeevan7979/saga-chorography-order-service
jeevan7979/saga-chorography-payment-service
jeevan7979/saga-chorography-inventory-service
```
