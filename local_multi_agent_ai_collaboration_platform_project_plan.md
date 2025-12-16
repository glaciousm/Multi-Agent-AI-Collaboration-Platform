# Local Multi‑Agent AI Collaboration Platform

## 1. Purpose and Vision

The goal of this project is to build a **local-first, experimental collaboration platform** where human users and multiple AI systems (ChatGPT, Claude, Codex, Claude Code) collaborate in a shared chat-like environment.

The distinguishing characteristic of the system is **role-based multi-agent collaboration**, where:
- Some AI agents act as **planners/reviewers**
- Other AI agents act as **implementors**
- Humans remain first-class participants and final decision-makers

All AI interactions are performed **via the providers’ web interfaces**, not via official APIs. This project is strictly intended for **local, personal, non-commercial use**.

This is an R&D platform, not a production service.

---

## 2. Target Outcomes

### Primary Target
- Enable structured, multi-turn collaboration between humans and multiple AI agents in a **single shared room**.

### Secondary Targets
- Validate whether multi-model collaboration produces higher-quality plans and implementations.
- Explore deterministic orchestration of AI agents with explicit roles and turn-taking.
- Produce a system architecture that can later swap web automation for official APIs.

### Non-Goals
- Public hosting or SaaS deployment
- High availability or horizontal scalability
- Zero-intervention autonomy
- Legal/compliance hardening for third-party users

---

## 3. Scope Definition

### In Scope
- Local execution only
- One user workspace
- One active room at a time (initially)
- Role-based AI participants
- Artifact-driven workflow (plans, tasks, patches)
- Browser automation using Playwright (or equivalent)

### Out of Scope
- User management / authentication beyond local use
- Billing, quotas, or usage metering
- Mobile clients
- Guaranteed provider uptime or access

---

## 4. High-Level Architecture

### 4.1 Logical Components

```
+---------------------+
|  Web UI (Client)    |
|  - Chat             |
|  - Artifacts        |
|  - Participants     |
+----------+----------+
           |
   WebSocket / REST
           |
+----------v----------+
|  Orchestrator       |
|  (Java / Spring)    |
|                     |
|  - Room State       |
|  - Workflow Engine  |
|  - Turn Management  |
|  - Artifact Control |
+----------+----------+
           |
       Job Queue
           |
+----------v----------+
|  AI Driver Workers  |
|  (Playwright)       |
|                     |
|  - ChatGPT Web      |
|  - Claude Web       |
|  - Codex Web        |
+---------------------+
```

### 4.2 Architectural Principles
- **Strict separation** between orchestration logic and provider automation
- **Replaceable adapters** for each AI provider
- **Artifact-first collaboration** (chat is transport, artifacts are truth)
- **Single active agent lease** per room

---

## 5. Core Concepts

### 5.1 Rooms
A room represents a collaborative session with:
- One or more humans
- Multiple AI participants
- A defined workflow state

### 5.2 Participants
Participants are either:
- HUMAN
- AI (with provider, role, and capabilities)

Each AI participant has:
- Provider (ChatGPT, Claude, Codex, Claude Code)
- Role (Planner, Reviewer, Implementor)
- Driver adapter

### 5.3 Artifacts
Artifacts are structured, versioned objects:
- Plan
- Task
- Patch
- Review

Artifacts are immutable per version and auditable.

---

## 6. Workflow Model

### 6.1 State Machine

```
INTAKE
  ↓
CLARIFICATION
  ↓
PLAN_DRAFT
  ↓
PLAN_REVIEW
  ↓
PLAN_APPROVED
  ↓
IMPLEMENTATION (per task)
  ↔ CODE_REVIEW
  ↓
DONE
```

### 6.2 Turn-Taking Rules
- Only one AI participant may speak at a time
- AI responses are time-boxed
- Human may interrupt or override at any point

---

## 7. Use Cases

### UC-1: Collaborative Planning
1. User submits a feature request
2. Planner AI asks clarifying questions
3. Planner AI produces a structured Plan artifact
4. Reviewer AI critiques and refines the plan
5. User approves the plan

### UC-2: Task-Based Implementation
1. Plan is decomposed into tasks
2. Each task is assigned to an Implementor AI
3. Implementor produces a patch artifact
4. Reviewer evaluates patch
5. Revisions occur until accepted

### UC-3: Human Intervention
1. AI driver encounters CAPTCHA or block
2. Workflow enters PAUSED state
3. Human resolves issue manually
4. Workflow resumes

---

## 8. Feature Breakdown

### Core Features
- Real-time chat
- Participant roles and visibility
- Artifact creation and versioning
- Deterministic workflow transitions
- AI turn orchestration

### Supporting Features
- Pause/resume workflows
- Retry and timeout handling
- Artifact diff viewing
- Local project workspace integration

---

## 9. Epics

### Epic 1: Collaboration Core
- Room lifecycle
- Chat infrastructure
- Participant management

### Epic 2: Orchestration Engine
- Workflow state machine
- Turn leasing and locks
- Artifact enforcement

### Epic 3: AI Web Drivers
- Playwright-based automation
- Provider-specific adapters
- Session persistence

### Epic 4: Code Integration
- Local git workspace
- Patch application
- Diff visualization

### Epic 5: UX & Control
- Artifact panel
- Workflow state visualization
- Manual override controls

---

## 10. Roadmap

### Phase 0 — Foundations
- Repository setup
- Core domain model
- Basic Web UI shell

### Phase 1 — Single-Agent MVP
- One room, one user
- One AI (Planner)
- Chat + Plan artifact

### Phase 2 — Multi-Agent Planning
- Add Reviewer AI
- Plan review loop
- Artifact versioning

### Phase 3 — Implementation Loop
- Implementor AI
- Patch artifacts
- Review/revision cycle

### Phase 4 — Stability & Tooling
- Retry logic
- Pause/resume
- Improved driver resilience

### Phase 5 — Expansion (Optional)
- Additional providers
- Parallel task execution (carefully)
- API-based adapters (future-proofing)

---

## 11. Success Criteria

The project is considered successful if:
- Multiple AI agents can collaborate coherently in one room
- Plans and patches are materially better than single-agent output
- The system remains understandable, debuggable, and controllable by a human

---

## 12. Final Notes

This project is intentionally constrained and experimental. Its value lies in:
- architectural clarity
- disciplined orchestration
- insight into multi-agent collaboration

Robustness and compliance are secondary to learning and controlled experimentation.

