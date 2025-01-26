# DAPR Pub/Sub Tutorial with Quarkus on Local Kubernetes

This tutorial demonstrates how to build a pub/sub system using DAPR and Quarkus running on a local Kubernetes cluster.

## Prerequisites

- Java 21
- Docker Desktop with Kubernetes enabled (or Minikube/Kind)
- DAPR CLI
- Helm

Note: Gradle is not required to be installed as we use the Gradle wrapper (`gradlew`).

## Project Structure

```
.
├── components/
│   ├── config.yaml      # DAPR observability configuration
│   └── pubsub.yaml      # Redis pub/sub component
├── k8s/
│   ├── publisher.yaml   # Publisher service deployment
│   └── subscriber.yaml  # Subscriber service deployment
├── publisher-service/   # Publisher Quarkus application
├── subscriber-service/  # Subscriber Quarkus application
└── gradle files...
```

## Setup Steps

1. Install DAPR on your Kubernetes cluster:
```bash
# Add and update Helm repo
helm repo add dapr https://dapr.github.io/helm-charts/
helm repo update

# Install DAPR with development components (Redis + Zipkin)
dapr init -k --dev
```

2. Build the services:
```bash
./gradlew clean build
```

3. Build Docker images:
```bash
# From publisher-service directory
docker build -t publisher:latest .

# From subscriber-service directory
docker build -t subscriber:latest .
```

4. Apply DAPR components and configurations:
```bash
kubectl apply -f components/
```

5. Deploy services:
```bash
kubectl apply -f k8s/
```

6. Verify the deployment:
```bash
kubectl get pods
```

Expected output:
```
NAME                         READY   STATUS    RESTARTS   AGE
publisher-xxx-xxx           2/2     Running   0          1m
subscriber-xxx-xxx          2/2     Running   0          1m
dapr-dev-redis-master-0     1/1     Running   0          5m
dapr-dev-zipkin-xxx-xxx     1/1     Running   0          5m
```

## Testing the Pub/Sub System

1. Port-forward the publisher service:
```bash
kubectl port-forward deployment/publisher 8080:8080
```

2. Send a test message:
```bash
curl -X POST http://localhost:8080/api/messages \
     -H "Content-Type: application/json" \
     -d '{"content":"Hello DAPR!"}'
```

3. Check subscriber logs:
```bash
kubectl logs -l app=subscriber -c subscriber
```

## Observability

View traces in Zipkin:
```bash
kubectl port-forward svc/zipkin 9411:9411
```
Then open http://localhost:9411 in your browser.

## Cleanup

```bash
# Remove application deployments
kubectl delete -f k8s/
kubectl delete -f components/

# Uninstall DAPR
dapr uninstall -k
``` 