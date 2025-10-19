# Task Manager – Java REST API (Spring Boot + MongoDB)

A minimal REST API to store and execute “tasks”.
- **Stack:** Java 17 • Spring Boot 3 • MongoDB 7
- **Ports:** API on `8080`

---

## ✅ What I built

**Entity**
```text
Task {
  id: string,
  name: string,
  owner: string,
  command: string,
  taskExecutions: [
    { startTime, endTime, output }
  ]
}

docker run -d --name mongo -p 27017:27017 mongo:7
./mvnw -B -ntp clean package -DskipTests
java -jar target/taskrun-0.0.1-SNAPSHOT.jar
$BASE = 'http://localhost:8080'

# upsert
$task = [pscustomobject]@{
  id="123"; name="Print Hello"; owner="John Smith"; command="echo Hello"; taskExecutions=@()
}
Invoke-RestMethod -Method Put -Uri "$BASE/tasks" -ContentType "application/json" -Body ($task | ConvertTo-Json -Depth 5)

# all
Invoke-RestMethod -Method Get -Uri "$BASE/tasks"

# by id
Invoke-RestMethod -Method Get -Uri "$BASE/tasks?id=123"

# by name prefix
Invoke-RestMethod -Method Get -Uri "$BASE/tasks?name=Print"

# text search (contains) – note the param key is q
Invoke-RestMethod -Method Get -Uri "$BASE/tasks/search?q=hello"

# execute
Invoke-RestMethod -Method Put -Uri "$BASE/tasks/123/execute"

# delete
Invoke-RestMethod -Method Delete -Uri "$BASE/tasks/123"

![Upsert Task](docs/img/backend_put.png)
![Get by ID](docs/img/backend_get_id.png)
![Search](docs/img/backend_search.png)
![Execute Output](docs/img/backend_execute.png)
