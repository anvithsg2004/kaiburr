# Task Manager – Java REST API (Spring Boot + MongoDB)

A minimal REST API to store and execute “tasks”.
- **Stack:** Java 17 • Spring Boot 3 • MongoDB 7
- **Repo:** `anvithsg2004/kaiburr`
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
