# Jenkins container

Build the Jenkins image:

```bash
docker build -t saga-jenkins:latest ./jenkins
```

Run it with access to the host Docker daemon:

```bash
docker run -d --name jenkins \
  -p 8088:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  --user root \
  saga-jenkins:latest
```

The Docker socket is required because the Jenkinsfile builds and pushes images.
The example runs Jenkins as root inside the container so it can access the
mounted Docker socket. Use a matching Docker group instead for production.
For a remote Kubernetes cluster, add the cluster kubeconfig to Jenkins as the
`saga-kubeconfig` Secret file credential.
