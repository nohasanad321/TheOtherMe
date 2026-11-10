# The Other Me — reconstructed baseline

A small **local-only** Java web chat with two personalities: **Donna** and **Alter Ego**.

> This is a reconstruction of the earlier project design, not a copy of source code from the work computer.

## Requirements
- JDK 17 or newer
- No Maven, Spring Boot, Node, Docker, Ollama, or API keys required

## Run on Windows (PowerShell)
From the repository root:
```powershell
mkdir out
javac -encoding UTF-8 -d out src/theotherme/Main.java
java -cp out theotherme.Main
```

Open <http://127.0.0.1:8085> in a browser. Stop the server with Ctrl+C.

## Run on macOS/Linux
```bash
mkdir -p out
javac -encoding UTF-8 -d out src/theotherme/Main.java
java -cp out theotherme.Main
```

## Current features
- Browser chat UI
- Switchable Donna / Alter Ego personalities
- Local Java HTTP server bound to 127.0.0.1
- Distinct rule-based responses
- No external AI or data transmission

## Current limitations
- Chat messages are not persisted (refreshing the page clears them)
- Responses are simple rules, not language-model output

## Next feature: Decision Memory
Add a local record of decisions and their reasons, recall them in conversation, and persist them to disk. Keep it lightweight and testable.
