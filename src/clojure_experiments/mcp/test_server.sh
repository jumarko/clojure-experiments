#!/bin/bash

# Simple script to test the MCP server interactively
# Usage: ./test_server.sh

echo "Starting MCP Server test..."
echo ""

cd "$(dirname "$0")/../../.."

# Test 1: Initialize
echo "Test 1: Initialize"
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}' | clj -M -m clojure-experiments.mcp.server 2>/dev/null &
SERVER_PID=$!
sleep 1

# Since the server blocks on reading, let's just use the test client instead
echo "Running test client..."
clj -M -m clojure-experiments.mcp.test-client

echo ""
echo "Tests completed!"
