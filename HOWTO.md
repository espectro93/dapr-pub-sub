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

1. Start with a clean slate:
```bash
# Delete any existing deployments
kubectl delete -f k8s/ 2>/dev/null || true
kubectl delete -f components/ 2>/dev/null || true

# Remove DAPR and its components
dapr uninstall -k --all

# Remove any leftover Redis and Zipkin resources
kubectl delete deployment,service,secret -l app=dapr-dev-zipkin 2>/dev/null || true
kubectl delete deployment,service,secret -l app=dapr-dev-redis 2>/dev/null || true
kubectl delete statefulset,service,secret -l app=dapr-dev-redis 2>/dev/null || true
kubectl delete pvc -l app=dapr-dev-redis 2>/dev/null || true

# Delete all resources in default namespace with specific labels
kubectl delete all,secrets,configmaps -l app=publisher -n default 2>/dev/null || true
kubectl delete all,secrets,configmaps -l app=subscriber -n default 2>/dev/null || true
kubectl delete all,secrets,configmaps -l app=dapr-dev-redis -n default 2>/dev/null || true
kubectl delete all,secrets,configmaps -l app=dapr-dev-zipkin -n default 2>/dev/null || true

# Check for any remaining Helm releases and remove if found
helm ls -A | grep -E 'dapr|redis|zipkin' | awk '{print $1}' | xargs -r helm uninstall
```

2. Install DAPR on your Kubernetes cluster:
```bash
# Install DAPR with development components (Redis + Zipkin)
dapr init -k --dev

# Verify DAPR system is running (wait for all pods to be ready)
kubectl get pods -n dapr-system
```

3. Build the services:
```bash
./gradlew clean build
```

4. Build Docker images:
```bash
# From publisher-service directory
cd publisher-service
docker build -t publisher:latest .
cd -
# From subscriber-service directory
cd subscriber-service
docker build -t subscriber:latest .
cd -
```

5. Apply DAPR components:
```bash
kubectl apply -f components/
```

6. Deploy services:
```bash
kubectl apply -f k8s/
```

7. Verify the deployment:
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

Note: The `2/2` in READY column indicates both the application and DAPR sidecar are running.

## Testing the Pub/Sub System

1. Port-forward the publisher service:
```bash
kubectl port-forward svc/publisher 8080:8080
```

2. Send a test message:
```bash
curl -v -X POST http://localhost:8080/api/messages \
     -H "Content-Type: application/json" \
     -d '{"content":"Hello DAPR!"}'
```

3. Check subscriber logs:
```bash
# Check application logs
kubectl logs -l app=subscriber -c subscriber

# Check DAPR sidecar logs if needed
kubectl logs -l app=subscriber -c daprd
```

## Observability

View traces in Zipkin:
```bash
# Port forward the Zipkin service
kubectl port-forward svc/dapr-dev-zipkin 9411:9411
```
Then open http://localhost:9411 in your browser.

You should see the following in Zipkin:
- Service names: publisher, subscriber
- Operations: 
  - /api/messages (Publisher)
  - /messages (Subscriber)
- Trace information showing the message flow from publisher to subscriber

## Troubleshooting

1. If pods show `1/1` instead of `2/2`:
   - Verify DAPR system is running: `kubectl get pods -n dapr-system`
   - Check pod events: `kubectl describe pod -l app=publisher`
   - Check DAPR sidecar logs: `kubectl logs -l app=publisher -c daprd`

2. If Redis authentication fails:
   - The pubsub.yaml is configured to use the Redis password from Kubernetes secrets
   - Verify Redis is running: `kubectl get pods -l app=dapr-dev-redis-master`

3. If messages aren't being received:
   - Check subscriber logs: `kubectl logs -l app=subscriber -c subscriber`
   - Check DAPR sidecar logs: `kubectl logs -l app=subscriber -c daprd`
   - Verify Redis connection: `kubectl logs -l app=dapr-dev-redis-master`