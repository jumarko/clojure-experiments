# MCP Server in Clojure

A simple implementation of the [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) server in Clojure.

## What is MCP?

Model Context Protocol (MCP) is an open protocol that enables AI applications to connect to external data sources and tools. It standardizes how applications provide context to LLMs and interact with external tools through a client-server architecture.

## Features

This implementation provides:

- **stdio transport** for local communication (suitable for desktop AI apps)
- **JSON-RPC 2.0** message protocol
- **Tool support** with a simple `read_file` tool

## Current Tools

### read_file

Reads the contents of a file from the filesystem.

**Input:**

- `path` (string, required): The absolute path to the file to read

**Returns:**

- File contents as text, or an error if the file doesn't exist or cannot be read

### list_directory

Lists the contents of a directory.

**Input:**

- `path` (string, required): The absolute path to the directory to list

**Returns:**

- List of files and directories (directories are marked with a trailing `/`), or an error if the directory doesn't exist or cannot be read

## Usage

### Running the Server

```bash
# Start the server using Clojure CLI
clj -M -m clojure-experiments.mcp.server
```

The server communicates via stdin/stdout using JSON-RPC 2.0 messages.

### Testing with Manual JSON-RPC Messages

You can test the server by sending JSON-RPC messages to stdin:

**Initialize:**
```json
{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}
```

**List tools:**
```json
{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}
```

**Call read_file:**
```json
{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"read_file","arguments":{"path":"/path/to/file.txt"}}}
```

### Using with AI Applications

To integrate this MCP server with AI applications like Claude Desktop, you would add it to your MCP configuration file:

**For Claude Desktop** (`~/Library/Application Support/Claude/claude_desktop_config.json` on macOS):

```json
{
  "mcpServers": {
    "clojure-mcp": {
      "command": "clj",
      "args": ["-M", "-m", "clojure-experiments.mcp.server"],
      "cwd": "/Users/jumar/workspace/clojure/clojure-experiments"
    }
  }
}
```

## Development

### Testing in the REPL

The namespace includes a comment block with test examples:

```clojure
(require '[clojure-experiments.mcp.server :as mcp])

;; Test initialize
(mcp/process-request
 {:jsonrpc "2.0"
  :id 1
  :method "initialize"
  :params {:protocolVersion "2024-11-05"
           :capabilities {}
           :clientInfo {:name "test-client" :version "1.0.0"}}})

;; Test list tools
(mcp/process-request
 {:jsonrpc "2.0"
  :id 2
  :method "tools/list"
  :params {}})

;; Test read_file
(mcp/process-request
 {:jsonrpc "2.0"
  :id 3
  :method "tools/call"
  :params {:name "read_file"
           :arguments {:path "/path/to/some/file.txt"}}})
```

### Adding New Tools

To add a new tool:

1. Add the tool definition to the `tools` vector
2. Implement the tool handler function
3. Register the handler in the `tool-handlers` map

Example:

```clojure
;; 1. Add tool definition
(def tools
  [{:name "read_file" ...}
   {:name "my_new_tool"
    :description "Description of what the tool does"
    :inputSchema {:type "object"
                  :properties {:param1 {:type "string"
                                        :description "A parameter"}}
                  :required ["param1"]}}])

;; 2. Implement handler
(defn my-new-tool
  [{:keys [param1]}]
  {:content [{:type "text"
              :text (str "Result: " param1)}]})

;; 3. Register handler
(def tool-handlers
  {"read_file" read-file-tool
   "my_new_tool" my-new-tool})
```

## Protocol Reference

- [MCP Specification](https://spec.modelcontextprotocol.io/)
- [MCP Documentation](https://modelcontextprotocol.io/docs)
- [JSON-RPC 2.0 Specification](https://www.jsonrpc.org/specification)

## Next Steps

Potential improvements:
- Add more file system tools (list directory, write file, etc.)
- Implement resource support for serving content
- Add prompt templates support
- Implement SSE transport for web-based clients
- Add configuration file support
- Add logging and error handling improvements
