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


![Upsert Task](docs/img/backend_put.png)
![Get by ID](docs/img/backend_get_id.png)
![Search](docs/img/backend_search.png)
![Execute Output](docs/img/backend_execute.png)

