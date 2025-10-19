Task Execution API

This document provides instructions on how to use the Task Execution API. This API allows you to create, manage, retrieve, execute, and delete tasks. Each task consists of a command that can be executed on the server.

API Endpoints

1. Create or Update a Task

Creates a new task or updates an existing one with the specified ID.

Method: PUT

Endpoint: /tasks/{id}

Request Body:

{
  "id": "task1",
  "name": "List files in directory",
  "owner": "abhinay",
  "command": "ls -l",
  "taskExecutions": []
}

!Output(image_filename.jpg)
Success Response (200 OK):

The API echoes the created or updated task object.

{
  "id": "task1",
  "name": "List files in directory",
  "owner": "abhinay",
  "command": "ls -l",
  "taskExecutions": []
}

![Alt text for the image](image_filename.jpg)
<p align="center"></p>

2. Get Task by ID

Retrieves a specific task using its unique ID.

Method: GET

Endpoint: /tasks?id={id}

Example: /tasks?id=task1

Success Response (200 OK):

Returns the task object matching the ID.

{
  "id": "task1",
  "name": "List files in directory",
  "owner": "abhinay",
  "command": "ls -l",
  "taskExecutions": []
}

![Alt text for the image](image_filename.jpg)
<p align="center"></p>

3. Search Tasks by Name

Searches for tasks where the name field contains the provided query string.

Method: GET

Endpoint: /tasks?name={query}

Example: /tasks?name=list

Success Response (200 OK):

Returns an array of task objects that match the search criteria.

[
  {
    "id": "task1",
    "name": "List files in directory",
    "owner": "abhinay",
    "command": "ls -l",
    "taskExecutions": []
  }
]

![Alt text for the image](image_filename.jpg)
<p align="center"></p>

4. Free-Text Search

Performs a free-text search across task fields.

Method: GET

Endpoint: /tasks/search?q={query}

Example: /tasks/search?q=list

Success Response (200 OK):

Returns an array of task objects that match the search query.

[
  {
    "id": "task1",
    "name": "List files in directory",
    "owner": "abhinay",
    "command": "ls -l",
    "taskExecutions": []
  }
]

![Alt text for the image](image_filename.jpg)
<p align="center"></p>

5. Execute a Task

Triggers the execution of the command associated with a specific task ID.

Method: PUT

Endpoint: /tasks/{id}/execute

Example: /tasks/task1/execute

Success Response (200 OK):

The immediate response contains the startTime, endTime, and the output of the command execution.

{
  "startTime": "2025-10-18T06:42:03.081526100Z",
  "endTime": "2025-10-18T06:42:03.726046300Z",
  "output": "total 25\n-rw-r--r-- 1 91861 Administrators  828 Oct 18 11:12 HELP.md\n..."
}

![Alt text for the image](image_filename.jpg)
<p align="center"></p>

6. Delete a Task

Deletes a task by its ID.

Method: DELETE

Endpoint: /tasks/{id}

Example: /tasks/task1

Success Response:

A successful deletion will typically return a 204 No Content or 200 OK with a confirmation message.

![Alt text for the image](image_filename.jpg)
<p align="center"></p>

Where to Find Execution Outputs

When you execute a task, the output is available in two places:

Immediate Response Body: The PUT /tasks/{id}/execute endpoint returns the output directly in the JSON response, as shown in the example above. This is useful for getting the result of a single, immediate execution.

Task Object History: The execution result is also appended to the taskExecutions array within the main task object. To view the history of all executions for a task, you can retrieve the task using the Get Task by ID endpoint (GET /tasks?id={id}).

Example: After execution, fetching task1 would look like this:

{
  "id": "task1",
  "name": "List files in directory",
  "owner": "abhinay",
  "command": "ls -l",
  "taskExecutions": [
    {
      "startTime": "2025-10-18T06:42:03.081526100Z",
      "endTime": "2025-10-18T06:42:03.726046300Z",
      "output": "total 25\n-rw-r--r-- 1 91861 Administrators  828 Oct 18 11:12 HELP.md\n..."
    }
  ]
}

![Alt text for the image](image_filename.jpg)
<p align="center"></p>

