# Exam Assignment System

Generates exam invigilator assignments from staff and room data. A two-service system: a REST API backend computes fair multi-session assignments, and a web frontend provides a user interface.

## Architecture

| Service | Port | Description |
|---------|------|-------------|
| `assign-server` | 8081 | REST API — parses XLSX, computes assignments, stores in SQLite |
| `client-web` | 8080 | JSP web UI — upload forms, result views, file downloads |

## Requirements

- Java 17+
- Node.js 18+ (for E2E tests only)
- [just](https://github.com/casey/just) (optional, for task runner)

## Quick Start

```bash
# Start the backend (port 8081)
just backend

# In another terminal, start the frontend (port 8080)
just frontend
```

Or start both simultaneously:

```bash
just dev
```

Then open `http://localhost:8080`.

## Usage

1. Open the web UI at `http://localhost:8080`
2. Upload staff list (CANBOCOITHI.XLSX) and room list (PHONGTHI.XLSX)
3. Enter the number of exam sessions
4. Submit — the system assigns 2 invigilators per room per session
5. Download results:
   - Invigilator assignment (DANHSACH_PHANCONG.XLSX)
   - Hall monitor assignment (DANHSACH_GIAMSAT.XLSX)

## API

Base URL: `http://localhost:8081/api`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/assignments` | List all assignment runs |
| POST | `/assignments` | Create new assignment (multipart: `staffFile`, `roomFile`, `sessionCount`) |
| GET | `/assignments/{id}` | Get assignment detail |
| GET | `/assignments/{id}/sessions/{no}` | Get session detail |
| GET | `/assignments/{id}/downloads/invigilators` | Download invigilator XLSX |
| GET | `/assignments/{id}/downloads/monitors` | Download hall monitor XLSX |

## Testing

```bash
# Unit tests
just test

# E2E tests (requires both servers running)
npm test
```

## Input Format

Two XLSX files with specific columns:

**CANBOCOITHI.XLSX** — `STT | Họ và tên | Ngày sinh | Mã cán bộ | Đơn vị công tác`

**PHONGTHI.XLSX** — `STT | Phòng thi | Địa điểm`

## Project Structure

```
├── assign-server/        # REST API (Jakarta EE, Maven)
├── client-web/           # Web UI (JSP, Jakarta EE, Maven)
├── tests-e2e/            # Playwright E2E tests
├── pom.xml               # Parent Maven POM
├── justfile              # Task definitions
└── package.json          # E2E test dependencies
```
