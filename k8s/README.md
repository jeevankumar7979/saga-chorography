# Kubernetes deployment

Build the application images in the cluster's image runtime, then deploy the stack:

```bash
docker build -t saga/order-service:latest ./order-service
docker build -t saga/payment-service:latest ./payment-service
docker build -t saga/inventory-service:latest ./inventory-service
kubectl apply -f k8s
```

The manifests use `LoadBalancer` services for the three APIs and Kafka UI.
With Docker Desktop, the APIs are available on ports 8081, 8082, and 8083,
and Kafka UI is available on port 8080. With kind or minikube, expose the
services using that platform's load-balancer command.

The database and Kafka workloads use `PersistentVolumeClaim`s. Configure a
storage class suitable for the target cluster before deploying to production.
