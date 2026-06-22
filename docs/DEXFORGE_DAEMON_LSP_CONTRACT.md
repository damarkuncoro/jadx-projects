# DexForge Daemon and LSP Contract

## Status

This contract is a stable API for DexForge IDE integrations and automation clients.
It is intentionally shaped around DexForge public engine models, while the current
backend can still be implemented by the JADX adapter.

Schema version: `1.0.0`

## Transport

The daemon reads one JSON object per line from standard input and writes one JSON
response object per line to standard output.

Every request uses this envelope:

```json
{
  "id": 1,
  "method": "method-name",
  "params": {}
}
```

Every successful response uses this envelope:

```json
{
  "id": 1,
  "status": "SUCCESS",
  "result": {}
}
```

Every failed response uses this envelope:

```json
{
  "id": 1,
  "status": "ERROR",
  "error": "Human-readable error message"
}
```

## Core Daemon Methods

## `load`

Loads a project input and creates the active DexForge project session.

Request:

```json
{
  "id": 1,
  "method": "load",
  "params": {
    "path": "/absolute/path/app.apk",
    "deobfuscationOn": true,
    "commentsLevel": "INFO",
    "decompilationMode": "AUTO"
  }
}
```

Response result:

```json
{
  "classesCount": 42,
  "resourcesCount": 7
}
```

Only `path` is required.

## `list-classes`

Lists classes in the active project session.

Request:

```json
{
  "id": 2,
  "method": "list-classes"
}
```

Response result:

```json
[
  {
    "fullName": "defpackage.HelloWorld",
    "shortName": "HelloWorld",
    "alias": "defpackage.HelloWorld",
    "packageName": "defpackage"
  }
]
```

## `decompile`

Decompiles a single class by full class name.

Request:

```json
{
  "id": 3,
  "method": "decompile",
  "params": {
    "className": "defpackage.HelloWorld"
  }
}
```

Response result:

```json
{
  "code": "package defpackage;\n\npublic class HelloWorld {\n}\n",
  "lineMapping": {},
  "diagnostics": []
}
```

Diagnostics are objects with this shape:

```json
{
  "line": 12,
  "character": 4,
  "severity": "ERROR",
  "message": "bad bytecode",
  "source": "Example.method"
}
```

`line`, `character`, and `source` can be omitted when unavailable.

## `get-definition`

Legacy daemon definition lookup by class and source offset.

Request:

```json
{
  "id": 4,
  "method": "get-definition",
  "params": {
    "className": "defpackage.HelloWorld",
    "pos": 15
  }
}
```

Response result:

```json
{
  "name": "HelloWorld",
  "fullName": "defpackage.HelloWorld",
  "declaringClass": "defpackage.HelloWorld",
  "defPos": 8
}
```

## LSP-Compatible Methods

## `initialize`

Returns the supported LSP capabilities.

Request:

```json
{
  "id": 10,
  "method": "initialize"
}
```

Response result:

```json
{
  "tool": "dexforge",
  "schemaVersion": 1,
  "capabilities": {
    "textDocumentSync": 1,
    "definitionProvider": true,
    "referencesProvider": true,
    "workspaceSymbolProvider": true,
    "hoverProvider": true
  }
}
```

## Common LSP Location Shape

Definition, references, and workspace symbols use the LSP location shape:

```json
{
  "uri": "file:///sources/defpackage/HelloWorld.java",
  "range": {
    "start": {
      "line": 7,
      "character": 12
    },
    "end": {
      "line": 7,
      "character": 22
    }
  }
}
```

Line and character positions are zero-based.

## `textDocument/definition`

Request:

```json
{
  "id": 11,
  "method": "textDocument/definition",
  "params": {
    "textDocument": {
      "uri": "file:///sources/defpackage/HelloWorld.java"
    },
    "position": {
      "line": 7,
      "character": 12
    }
  }
}
```

Response result: a single location object.

## `textDocument/references`

Request:

```json
{
  "id": 12,
  "method": "textDocument/references",
  "params": {
    "textDocument": {
      "uri": "file:///sources/defpackage/HelloWorld.java"
    },
    "position": {
      "line": 7,
      "character": 12
    }
  }
}
```

Response result: an array of location objects.

## `workspace/symbol`

Request:

```json
{
  "id": 13,
  "method": "workspace/symbol",
  "params": {
    "query": "Hello"
  }
}
```

Response result:

```json
[
  {
    "name": "HelloWorld",
    "kind": 5,
    "location": {
      "uri": "file:///sources/defpackage/HelloWorld.java",
      "range": {
        "start": {
          "line": 7,
          "character": 12
        },
        "end": {
          "line": 7,
          "character": 22
        }
      }
    },
    "containerName": "defpackage"
  }
]
```

`kind` follows LSP `SymbolKind` numeric values.
`containerName` is omitted when unavailable.

## `textDocument/hover`

Request:

```json
{
  "id": 14,
  "method": "textDocument/hover",
  "params": {
    "textDocument": {
      "uri": "file:///sources/defpackage/HelloWorld.java"
    },
    "position": {
      "line": 7,
      "character": 12
    }
  }
}
```

Response result:

```json
{
  "contents": {
    "kind": "markdown",
    "value": "```java\nclass defpackage.HelloWorld\n```"
  }
}
```

## Compatibility Notes

- This contract is a stable schema version (v1.0.0) for production integrations.
- New IDE clients should prefer the LSP-compatible methods.
- `get-definition` remains available for older daemon clients.
- The daemon currently returns `SUCCESS` and `ERROR` strings rather than JSON-RPC
  `result` and `error` envelopes. Clients should not assume full JSON-RPC 2.0
  compliance yet.
