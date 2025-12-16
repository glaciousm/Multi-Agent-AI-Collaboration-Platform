# User Guide

This guide walks you through running the local multi-agent collaboration platform, exploring the built-in web shell, and exercising the REST API for orchestration.

## 1) Setup and launch
1. **Install prerequisites:** Java 21 and a recent version of Maven (the Maven wrapper is included).
2. **Start the application:**
   ```bash
   ./mvnw spring-boot:run
   ```
3. **Open the web shell:** visit `http://localhost:8080` to inspect rooms and participants.

You can also build a standalone jar with `./mvnw clean package` and run it from the `target/` directory.

## 2) What is provisioned by default?
- **Provider catalog:** ChatGPT, Claude, Claude Code, Gemini, and a Local API adapter are pre-loaded with web-UI/API access modes and capability flags so rooms can reuse them immediately.【F:src/main/java/com/localcollab/platform/service/InMemoryRoomService.java†L106-L123】
- **Default room:** the first boot creates the “Multi-Agent Planning Room” and reuses the earliest room ID on subsequent create calls to maintain a single active room at a time.【F:src/main/java/com/localcollab/platform/service/InMemoryRoomService.java†L32-L58】
- **Seeded participants:** each room starts with a human observer plus planner, reviewer, and implementor AI entries, each tagged with default providers and capabilities.【F:src/main/java/com/localcollab/platform/service/InMemoryRoomService.java†L42-L50】
- **Starter artifacts:** a versioned plan and patch are added automatically, along with a primary task lane owned by the implementor to demonstrate scheduling state.【F:src/main/java/com/localcollab/platform/service/InMemoryRoomService.java†L52-L75】

## 3) Using the web shell
- The root page lists all rooms and shows counts for participants, artifacts, and chat messages; use the **Create Room** button to initialize the default setup if it does not exist yet.【F:src/main/resources/templates/index.html†L11-L55】
- Cards summarize each room with creation time, participant roster, artifact list, and chat totals so you can confirm orchestration state at a glance.【F:src/main/resources/templates/index.html†L32-L49】

## 4) REST API quick reference
All endpoints are JSON-based and live under `/api/rooms`.

### Rooms
- `GET /api/rooms` — list all rooms (typically just the single seeded room).【F:src/main/java/com/localcollab/platform/web/RoomController.java†L35-L38】
- `POST /api/rooms` — create the default room; if one exists the earliest room is returned to enforce a single workspace.【F:src/main/java/com/localcollab/platform/web/RoomController.java†L40-L48】【F:src/main/java/com/localcollab/platform/service/InMemoryRoomService.java†L33-L37】

### Participants and providers
- `POST /api/rooms/{roomId}/participants` — add a participant. If `type` or `role` are omitted, they default to `AI` and `OBSERVER`; providers are auto-registered when missing.【F:src/main/java/com/localcollab/platform/web/RoomController.java†L50-L78】【F:src/main/java/com/localcollab/platform/service/InMemoryRoomService.java†L77-L97】【F:src/main/java/com/localcollab/platform/service/InMemoryRoomService.java†L125-L134】
- `POST /api/rooms/{roomId}/providers` — register a provider adapter for the room with access mode, capabilities, and optional endpoint details.【F:src/main/java/com/localcollab/platform/web/RoomController.java†L80-L98】【F:src/main/java/com/localcollab/platform/service/InMemoryRoomService.java†L99-L124】

### Artifacts and task lanes
- `POST /api/rooms/{roomId}/artifacts` — create a plan, patch, task, review, or note. Validations enforce non-blank title/content and required parent relationships for reviews and patches.【F:src/main/java/com/localcollab/platform/web/RoomController.java†L100-L113】【F:src/main/java/com/localcollab/platform/service/InMemoryRoomService.java†L143-L207】
- `POST /api/rooms/{roomId}/task-lanes` — create a task lane tied to an implementor participant (required).【F:src/main/java/com/localcollab/platform/web/RoomController.java†L115-L135】【F:src/main/java/com/localcollab/platform/service/InMemoryRoomService.java†L209-L234】
- `POST /api/rooms/{roomId}/task-lanes/{laneId}/tasks` — schedule a task artifact into a lane; only task artifacts are accepted and lanes must be active.【F:src/main/java/com/localcollab/platform/web/RoomController.java†L137-L158】【F:src/main/java/com/localcollab/platform/service/InMemoryRoomService.java†L236-L262】
- `POST /api/rooms/{roomId}/task-lanes/{laneId}/state` — update a lane’s lifecycle state (active, blocked, completed).【F:src/main/java/com/localcollab/platform/web/RoomController.java†L160-L176】【F:src/main/java/com/localcollab/platform/service/InMemoryRoomService.java†L264-L282】

### Chat and workflow controls
- `GET /api/rooms/{roomId}/messages` — list chat history for a room.【F:src/main/java/com/localcollab/platform/web/RoomController.java†L178-L186】
- `POST /api/rooms/{roomId}/messages` — post a message; if `participantId` is omitted, the first human participant is used automatically.【F:src/main/java/com/localcollab/platform/web/RoomController.java†L188-L214】
- `POST /api/rooms/{roomId}/pause` and `POST /api/rooms/{roomId}/resume` — toggle the room’s paused state, which blocks new changes while paused.【F:src/main/java/com/localcollab/platform/web/RoomController.java†L216-L227】【F:src/main/java/com/localcollab/platform/service/InMemoryRoomService.java†L284-L293】
- `POST /api/rooms/{roomId}/driver/failures` and `POST /api/rooms/{roomId}/driver/recoveries` — record driver health events; failures can automatically pause the room until recovery is logged.【F:src/main/java/com/localcollab/platform/web/RoomController.java†L229-L247】【F:src/main/java/com/localcollab/platform/service/InMemoryRoomService.java†L295-L312】
- `GET /api/rooms/{roomId}/summary` — retrieve aggregated counts of participants, artifacts, task lanes, message totals, and driver status for monitoring.【F:src/main/java/com/localcollab/platform/web/RoomController.java†L249-L256】【F:src/main/java/com/localcollab/platform/service/InMemoryRoomService.java†L314-L335】

### Example cURL flow
```bash
# Fetch the single seeded room
ROOM_ID=$(curl -s http://localhost:8080/api/rooms | jq -r '.[0].id')

# Add an AI implementor and a task lane for them
curl -s -X POST http://localhost:8080/api/rooms/$ROOM_ID/participants \
  -H 'Content-Type: application/json' \
  -d '{"displayName":"Coder","type":"AI","role":"IMPLEMENTOR","provider":"Local API","capabilities":["implementation","patch"]}'

curl -s -X POST http://localhost:8080/api/rooms/$ROOM_ID/task-lanes \
  -H 'Content-Type: application/json' \
  -d '{"name":"New Lane","implementorId":"<IMPLEMENTOR_ID_FROM_PREVIOUS_RESPONSE>"}'
```

Use the task and artifact endpoints above to add tasks, patches, and reviews as you iterate.
