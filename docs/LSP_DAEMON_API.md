# DexForge LSP Daemon API

DexForge CLI exposes a JSON-RPC daemon mode (`lsp`) that enables IDE integrations, automation scripts, and programmatic decompilation workflows.

## Overview

The daemon implements a subset of the Language Server Protocol (LSP) tailored for Android reverse engineering. It allows clients to:

- Load DEX/APK/AAB files into a persistent decompiler instance
- Navigate code symbols (go to definition, find references)
- Search workspace symbols
- Get hover information for Java/Kotlin symbols
- Decompile classes and methods on demand

## Starting the Daemon

```bash
# Start daemon on default port (8080)
dexforge lsp

# Start daemon on custom port
dexforge lsp --port 9090

# Start daemon with auto-shutdown after inactivity
dexforge lsp --idle-timeout 300
```

## Protocol

The daemon uses **JSON-RPC 2.0** over **stdio** or **TCP socket**.

### Request Format

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "textDocument/definition",
  "params": {
    "textDocument": { "uri": "file:///path/to/Class.dex" },
    "position": { "line": 10, "character": 5 }
  }
}
```

### Response Format

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "uri": "file:///path/to/OtherClass.dex",
    "range": {
      "start": { "line": 0, "character": 0 },
      "end": { "line": 50, "character": 1 }
    }
  }
}
```

### Error Response

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "error": {
    "code": -32600,
    "message": "Invalid Request: missing 'textDocument' parameter"
  }
}
```

## Methods

### `initialize`

Initialize the daemon and negotiate capabilities.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "initialize",
  "params": {
    "processId": 12345,
    "rootUri": "file:///workspace",
    "capabilities": {}
  }
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "capabilities": {
      "textDocumentSync": 1,
      "definitionProvider": true,
      "referencesProvider": true,
      "workspaceSymbolProvider": true,
      "hoverProvider": true
    }
  }
}
```

### `shutdown`

Gracefully shutdown the daemon.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "shutdown"
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": null
}
```

### `dexforge/load`

Load a DEX, APK, AAB, or other supported input file.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "dexforge/load",
  "params": {
    "file": "/path/to/app.apk",
    "options": {
      "showInconsistentCode": false,
      "deobfuscationEnabled": true
    }
  }
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "result": {
    "success": true,
    "classesCount": 1523,
    "packages": ["com.example.app", "org.json"],
    "loadTimeMs": 342
  }
}
```

### `textDocument/definition`

Go to the definition of a symbol at a given position.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "id": 3,
  "method": "textDocument/definition",
  "params": {
    "textDocument": { "uri": "file:///workspace/com/example/app/MainActivity.java" },
    "position": { "line": 42, "character": 15 }
  }
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 3,
  "result": {
    "uri": "file:///workspace/com/example/app/User.java",
    "range": {
      "start": { "line": 0, "character": 0 },
      "end": { "line": 120, "character": 1 }
    }
  }
}
```

### `textDocument/references`

Find all references to a symbol at a given position.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "id": 4,
  "method": "textDocument/references",
  "params": {
    "textDocument": { "uri": "file:///workspace/com/example/app/User.java" },
    "position": { "line": 15, "character": 8 },
    "context": { "includeDeclaration": true }
  }
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 4,
  "result": [
    {
      "uri": "file:///workspace/com/example/app/MainActivity.java",
      "range": { "start": { "line": 42, "character": 10 }, "end": { "line": 42, "character": 14 } }
    },
    {
      "uri": "file:///workspace/com/example/app/User.java",
      "range": { "start": { "line": 15, "character": 8 }, "end": { "line": 15, "character": 12 } }
    }
  ]
}
```

### `workspace/symbol`

Search for symbols across the loaded workspace.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "id": 5,
  "method": "workspace/symbol",
  "params": {
    "query": "MainActivity"
  }
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 5,
  "result": [
    {
      "name": "MainActivity",
      "kind": 5,
      "location": {
        "uri": "file:///workspace/com/example/app/MainActivity.java",
        "range": { "start": { "line": 0, "character": 0 }, "end": { "line": 200, "character": 1 } }
      },
      "containerName": "com.example.app"
    }
  ]
}
```

### `textDocument/hover`

Get hover information for a symbol at a given position.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "id": 6,
  "method": "textDocument/hover",
  "params": {
    "textDocument": { "uri": "file:///workspace/com/example/app/MainActivity.java" },
    "position": { "line": 42, "character": 15 }
  }
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 6,
  "result": {
    "contents": {
      "kind": "markdown",
      "value": "**User**\n\n```java\npublic class User implements Serializable {\n    private String name;\n    private int age;\n    // ...\n}\n```"
    },
    "range": { "start": { "line": 42, "character": 10 }, "end": { "line": 42, "character": 14 } }
  }
}
```

## Error Codes

| Code | Meaning |
|------|---------|
| -32700 | Parse error |
| -32600 | Invalid Request |
| -32601 | Method not found |
| -32602 | Invalid params |
| -32603 | Internal error |
| -32000 | Server error (e.g., decompiler not loaded) |

## Client Implementation Example (Python)

```python
import json
import subprocess
import threading
import queue

class DexForgeClient:
    def __init__(self, port=8080):
        self.process = subprocess.Popen(
            ["dexforge", "lsp", "--port", str(port)],
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            bufsize=1
        )
        self.request_id = 0
        self.responses = {}
        self._reader_thread = threading.Thread(target=self._read_responses, daemon=True)
        self._reader_thread.start()

    def _read_responses(self):
        for line in self.process.stdout:
            if line.strip():
                response = json.loads(line)
                if "id" in response:
                    self.responses[response["id"]] = response

    def send_request(self, method, params=None):
        self.request_id += 1
        request = {
            "jsonrpc": "2.0",
            "id": self.request_id,
            "method": method,
            "params": params or {}
        }
        self.process.stdin.write(json.dumps(request) + "\n")
        self.process.stdin.flush()
        # Wait for response
        while self.request_id not in self.responses:
            pass
        return self.responses.pop(self.request_id)

    def load_file(self, filepath):
        return self.send_request("dexforge/load", {"file": filepath})

    def get_definition(self, uri, line, character):
        return self.send_request("textDocument/definition", {
            "textDocument": {"uri": uri},
            "position": {"line": line, "character": character}
        })

    def shutdown(self):
        self.send_request("shutdown")
        self.process.terminate()
```

## Integration with VS Code

See [`dexforge-vscode`](dexforge-vscode/src/extension.js) for a reference implementation.

## Integration with IntelliJ IDEA

The daemon can be used as a backend for IntelliJ plugins via the same JSON-RPC protocol. See the roadmap for planned IntelliJ plugin support.
