(ns clojure-experiments.mcp.test-client
  "Simple test client for the MCP server."
  (:require [clojure-experiments.mcp.server :as server]))

(defn test-initialize []
  (println "\n=== Testing initialize ===")
  (let [request {:jsonrpc "2.0"
                 :id 1
                 :method "initialize"
                 :params {:protocolVersion "2024-11-05"
                          :capabilities {}
                          :clientInfo {:name "test-client" :version "1.0.0"}}}
        response (server/process-request request)]
    (clojure.pprint/pprint response)))

(defn test-tools-list []
  (println "\n=== Testing tools/list ===")
  (let [request {:jsonrpc "2.0"
                 :id 2
                 :method "tools/list"
                 :params {}}
        response (server/process-request request)]
    (clojure.pprint/pprint response)))

(defn test-read-file [path]
  (println "\n=== Testing read_file ===" path)
  (let [request {:jsonrpc "2.0"
                 :id 3
                 :method "tools/call"
                 :params {:name "read_file"
                          :arguments {:path path}}}
        response (server/process-request request)]
    (clojure.pprint/pprint response)))

(defn test-unknown-method []
  (println "\n=== Testing unknown method ===")
  (let [request {:jsonrpc "2.0"
                 :id 4
                 :method "unknown/method"
                 :params {}}
        response (server/process-request request)]
    (clojure.pprint/pprint response)))

(defn test-list-directory [path]
  (println "\n=== Testing list_directory ===" path)
  (let [request {:jsonrpc "2.0"
                 :id 5
                 :method "tools/call"
                 :params {:name "list_directory"
                          :arguments {:path path}}}
        response (server/process-request request)]
    (clojure.pprint/pprint response)))

(defn run-tests []
  (println "Starting MCP Server Tests\n")
  (test-initialize)
  (test-tools-list)
  (test-read-file (str (System/getProperty "user.dir") "/README.md"))
  (test-read-file "/does/not/exist.txt")
  (test-list-directory (str (System/getProperty "user.dir") "/src"))
  (test-list-directory "/does/not/exist")
  (test-unknown-method)
  (println "\n=== All tests completed ==="))

(defn -main [& args]
  (run-tests))

(comment
  (run-tests)
  )
