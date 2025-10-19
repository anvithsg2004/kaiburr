# Task Manager – Java REST API (Spring Boot + MongoDB)

A minimal REST API to store and execute “tasks”.
- **Stack:** Java 17 • Spring Boot 3 • MongoDB 7
- **Ports:** API on `8080`

---

## ✅ What I built

**Entity**
/PUT Task
{
  "id": "task1",
  "name": "List files in directory",
  "owner": "abhinay",
  "command": "ls -l",
  "taskExecutions": []
}

/200 echoes
{
  "id": "task1",
  "name": "List files in directory",
  "owner": "abhinay",
  "command": "ls -l",
  "taskExecutions": []
}

/GET TASK
  {
    "id": "task1",
    "name": "List files in directory",
    "owner": "abhinay",
    "command": "ls -l",
    "taskExecutions": []
  }

Get task by id

GET /tasks?id=task1

200 OK:

{
  "id": "task1",
  "name": "List files in directory",
  "owner": "abhinay",
  "command": "ls -l",
  "taskExecutions": []
}

Search by name (contains)

GET /tasks?name=list

200 OK:

[
  {
    "id": "task1",
    "name": "List files in directory",
    "owner": "abhinay",
    "command": "ls -l",
    "taskExecutions": []
  }
]

Free-text search

GET /tasks/search?q=list

200 OK:

[
  {
    "id": "task1",
    "name": "List files in directory",
    "owner": "anvith",
    "command": "ls -l",
    "taskExecutions": []
  }
]

6) Execute the task

PUT /tasks/task1/execute

200 OK (example output I got):

{
  "startTime": "2025-10-18T06:42:03.081526100Z",
  "endTime": "2025-10-18T06:42:03.726046300Z",
  "output": "total 25\n-rw-r--r-- 1 91861 Administrators  828 Oct 18 11:12 HELP.md\n..."
}


This shows the directory listing (because the command was ls -l).
The execution is also appended to taskExecutions for that task.

7) Delete a task

DELETE /tasks/task1

![Upsert Task](docs/img/backend_put.png)
![Get by ID](docs/img/backend_get_id.png)
![Search](docs/img/backend_search.png)
![Execute Output](docs/img/backend_execute.png)


