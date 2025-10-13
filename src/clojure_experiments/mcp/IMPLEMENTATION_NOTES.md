# MCP Server Implementation Notes

## Architecture Overview

This is a simple but complete implementation of the Model Context Protocol (MCP) server in Clojure.

### Key Components

1. **Transport Layer** - stdio-based communication
   - Reads JSON-RPC messages from stdin
     - Writes JSON-RPC responses to stdout
   - Uses stderr for server logs

2. **Protocol Layer** - JSON-RPC 2.0
   - Handles request/response messages
   - Implements proper error codes
   - Supports notifications (though not currently used)

3. **MCP Protocol Implementation**
   - Initialize handshake
   - Tool listing
   - Tool execution

### Protocol Flow

```
Client                          Server
  |                               |
  |--- initialize request ------->|
  |<-- initialize response -------|
  |                               |
  |--- tools/list request ------->|
  |<-- tools/list response -------|
  |                               |
  |--- tools/call request ------->|
  |<-- tools/call response -------|
```

## MCP Protocol Version

This implementation uses MCP protocol version `2024-11-05`.

## Implemented Methods

### Core Protocol Methods

1. **initialize**
   - Purpose: Establish connection and exchange capabilities
   - Required for all MCP sessions
   - Returns server info and capabilities

2. **tools/list**
   - Purpose: List available tools
   - Returns array of tool definitions with schemas

3. **tools/call**
   - Purpose: Execute a specific tool
   - Takes tool name and arguments
   - Returns tool-specific results

## Tool Implementation Pattern

Each tool follows this pattern:

```clojure
;; 1. Define the tool schema
{:name "tool_name"
 :description "What the tool does"
 :inputSchema {:type "object"
               :properties {:param {:type "string"
                                    :description "Parameter description"}}
               :required ["param"]}}

;; 2. Implement the handler function
(defn tool-name-handler
  [{:keys [param]}]
  {:content [{:type "text"
              :text "result"}]})

;; 3. Register in tool-handlers map
{"tool_name" tool-name-handler}
```

## Error Handling

The implementation uses standard JSON-RPC 2.0 error codes:

- `-32601`: Method not found
- `-32603`: Internal error

Tool-specific errors are returned in the result with `isError: true`.

## Design Decisions

### Why stdio transport?

- Simple and reliable for local processes
- Perfect for desktop applications like Claude Desktop
- No need for network setup or security concerns
- Standard transport for MCP local servers

### Why not SSE?

- SSE (Server-Sent Events) is better for web-based clients
- Requires HTTP server setup
- More complex for local use cases
- Can be added later if needed

### Tool Result Format

Tools return results in this format:

```clojure
{:content [{:type "text"
            :text "..."}]}
```

For errors:

```clojure
{:isError true
 :content [{:type "text"
            :text "error message"}]}
```

This follows the MCP specification for tool responses.

## Testing Strategy

The implementation includes:

1. **Unit-level testing** via REPL (comment blocks)
2. **Integration testing** via test-client
3. **Manual testing** via stdin/stdout

## Future Enhancements

Possible additions:

1. **More tools**
   - `write_file` - Write content to a file
   - `search_files` - Search for files by pattern
   - `execute_command` - Run shell commands (with safety checks)

2. **Resources**
   - Implement MCP resources for serving content
   - Examples: file://, git://, etc.

3. **Prompts**
   - Add prompt templates support
   - Examples: code review, documentation, etc.

4. **Sampling**
   - Allow server to request LLM completions
   - Advanced use case for autonomous agents

5. **SSE Transport**
   - Add HTTP/SSE transport option
   - Useful for web-based clients

6. **Configuration**
   - Add config file support
   - Allow configuring allowed directories, etc.

7. **Security**
   - Add path restrictions
   - Implement allowlist/denylist for file operations
   - Add audit logging

## References

- [MCP Specification](https://spec.modelcontextprotocol.io/)
- [MCP TypeScript SDK](https://github.com/modelcontextprotocol/typescript-sdk)
- [JSON-RPC 2.0 Spec](https://www.jsonrpc.org/specification)
- [Clojure MCP implementations](https://github.com/search?q=clojure+mcp+server&type=repositories)

## Known Limitations

1. **No persistent state** - Server doesn't maintain state between requests
2. **No authentication** - Assumes trusted local environment
3. **No rate limiting** - Tools can be called unlimited times
4. **No async operations** - All tools are synchronous
5. **Basic error handling** - Could be more sophisticated

## Performance Considerations

- Startup time: ~1-2 seconds (JVM startup)
- Per-request overhead: Minimal (JSON parsing + tool execution)
- Memory usage: Low (~50MB base JVM)

For production use with frequent calls, consider:
- Using GraalVM native-image for faster startup
- Implementing a persistent server mode
- Adding caching for frequently accessed files
