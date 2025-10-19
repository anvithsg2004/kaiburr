How to run with Docker (optional)
# build image
docker build -t taskrun:1.0.0 .

# run with local Mongo
docker run --rm -p 8080:8080 \
  -e SPRING_DATA_MONGODB_URI=mongodb://host.docker.internal:27017/taskrun \
  taskrun:1.0.0

Kubernetes – Deploy the Task Runner API

This deploys the Spring Boot app and MongoDB on a local Kubernetes cluster (Docker Desktop).
The app exposes a NodePort so I can call the REST API from my laptop.

What I used

Docker Desktop with Kubernetes enabled (docker-desktop context)

kubectl

Container image:

first I used a local image taskrun:1.0.0

then I switched to GHCR image ghcr.io/anvithsg2004/kaiburr:latest (built by GitHub Actions)

Kubernetes manifests (in k8s/)

00-namespace.yaml → creates namespace taskrun

10-rbac.yaml → ServiceAccount taskrun-sa, a Role (can create pods, read logs), and a RoleBinding

20-mongo.yaml → PersistentVolumeClaim + Deployment + Service for MongoDB

30-app.yaml → Deployment + Service (NodePort 30080) for the Spring Boot app

Important env for the app:

env:
  - name: SPRING_DATA_MONGODB_URI
    value: mongodb://mongodb:27017/taskrun
  - name: POD_NAMESPACE
    valueFrom:
      fieldRef:
        fieldPath: metadata.namespace


The app uses POD_NAMESPACE to create short-lived busybox runner pods when executing a task command.

How I deployed

Make sure your context is Docker Desktop:

kubectl config use-context docker-desktop
kubectl get nodes


Apply everything:

kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/10-rbac.yaml
kubectl apply -f k8s/20-mongo.yaml
kubectl -n taskrun wait --for=condition=available deploy/mongodb --timeout=120s

# If using the GHCR image (what I used finally):
kubectl apply -f k8s/30-app.yaml

# Watch rollout
kubectl -n taskrun rollout status deploy/taskrun
kubectl -n taskrun get pods -o wide
kubectl -n taskrun get svc


✅ Expected state:

NAMESPACE   NAME                       READY   STATUS    RESTARTS   AGE   IP           NODE
taskrun     mongodb-xxxxx              1/1     Running   0          ...   10.1.0.140   docker-desktop
taskrun     taskrun-xxxxx              1/1     Running   0          ...   10.1.0.145   docker-desktop


Service:

NAME          TYPE      CLUSTER-IP      PORT(S)          NODE-PORT
taskrun-svc   NodePort  10.99.198.86    8080:30080/TCP   30080

How I tested the API (from my laptop)

Base URL:

$BASE = 'http://localhost:30080'

1) Create a task
$task = [pscustomobject]@{
  id = "123"
  name = "Print Hello"
  owner = "John Smith"
  command = "echo Hello from K8s!"
  taskExecutions = @()
}
$taskJson = $task | ConvertTo-Json -Depth 5

Invoke-RestMethod -Method Put -Uri "$BASE/tasks" -ContentType "application/json" -Body $taskJson


Output (200 OK):

{
  "id": "123",
  "name": "Print Hello",
  "owner": "John Smith",
  "command": "echo Hello from K8s!",
  "taskExecutions": {}
}

2) Execute the task (this spawns a short-lived busybox pod)
Invoke-RestMethod -Method Put -Uri "$BASE/tasks/123/execute"


Output example:

{
  "startTime": "2025-10-19T06:38:34.354Z",
  "endTime": "2025-10-19T06:38:44.564Z",
  "output": "Hello from K8s!..."
}


Kubernetes events showed runner pods like taskrun-exec-xxxxx being Scheduled / Started and then disappearing.

3) Get by id
Invoke-RestMethod -Method Get -Uri "$BASE/tasks?id=123"


Output shows the execution recorded:

{
  "id": "123",
  "name": "Print Hello",
  "owner": "John Smith",
  "command": "echo Hello from K8s!",
  "taskExecutions": [
    {
      "startTime": "...",
      "endTime": "...",
      "output": "Hello from K8s! [executedIn=k8sPod]"
    }
  ]
}

4) Filter by name
Invoke-RestMethod -Method Get -Uri "$BASE/tasks?name=Print"

5) Search endpoint
Invoke-RestMethod -Method Get -Uri "$BASE/tasks/search?q=Print"


Note: q is required for /tasks/search. Without it, the app returns 400 Bad Request.

6) Delete a task
Invoke-RestMethod -Method Delete -Uri "$BASE/tasks/123"

Data persistence check (PVC)

I deleted the MongoDB pod and waited for it to restart:

kubectl -n taskrun delete pod -l app=mongodb
kubectl -n taskrun wait --for=condition=ready pod -l app=mongodb --timeout=120s


Because the pod uses a PersistentVolumeClaim, data stayed intact.

What broke & how I fixed it

CrashLoopBackOff at first run: Spring failed with
“No qualifying bean of type io.fabric8.kubernetes.client.KubernetesClient”.

Fix: I added K8sConfig.java (Spring @Configuration) to create the Fabric8 client (in-cluster or from kubeconfig). Rebuilt the image and redeployed.

Env var merge conflict when switching from a manual POD_NAMESPACE=value to the Downward API:

Error: valueFrom may not be specified when value is not empty

Fix: removed the old env on the live Deployment:

kubectl -n taskrun set env deploy/taskrun POD_NAMESPACE-
kubectl -n taskrun apply -f k8s/30-app.yaml


RBAC: Verified the service account can create pods and read logs:

kubectl -n taskrun auth can-i create pods --as system:serviceaccount:taskrun:taskrun-sa
kubectl -n taskrun auth can-i get pods/log --as system:serviceaccount:taskrun:taskrun-sa

Switching the image

Local dev image: set image: taskrun:1.0.0 (works on Docker Desktop because the node can see local images).

CI image from GHCR (what I used finally):
image: ghcr.io/anvithsg2004/kaiburr:latest
Make sure the package is public (or create an imagePullSecret).

Cleanup
kubectl delete ns taskrun
