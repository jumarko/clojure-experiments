(ns clojure-experiments.mcp.server
  "A simple Model Context Protocol (MCP) server implementation in Clojure.
  
  MCP is a protocol that enables AI applications to connect to external data sources
  and tools. This implementation uses stdio transport for local communication.
  
  References:
  - https://modelcontextprotocol.io/
  - https://spec.modelcontextprotocol.io/"
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.io BufferedReader InputStreamReader PrintWriter File]))

;; Configuration and security

(def ^:dynamic *allowed-paths*
  "Allow-list of paths that the MCP server can access.
  Can be set via environment variable MCP_ALLOWED_PATHS (colon-separated).
  Defaults to the current working directory."
  (atom nil))

(defn set-allowed-paths!
  "Set the allowed paths for file system access.
  Paths should be absolute paths to files or directories."
  [paths]
  (reset! *allowed-paths*
          (mapv (fn [path]
                  (let [file (io/file path)]
                    (.getCanonicalPath file)))
                paths)))

(defn get-allowed-paths
  "Get the currently configured allowed paths."
  []
  (or @*allowed-paths*
      ;; Default to current working directory if not configured
      (let [default-path (.getCanonicalPath (io/file "."))]
        (reset! *allowed-paths* [default-path])
        @*allowed-paths*)))

(defn path-allowed?
  "Check if a given path is within the allowed paths.
  Returns true if the path or any of its parent directories are in the allow-list.
  Uses canonical paths to prevent path traversal attacks."
  [path]
  (try
    (let [file (io/file path)
          canonical-path (.getCanonicalPath file)
          allowed-paths (get-allowed-paths)]
      (some (fn [allowed-path]
              ;; Check if the path is the allowed path or within it
              (or (= canonical-path allowed-path)
                  (str/starts-with? canonical-path (str allowed-path File/separator))))
            allowed-paths))
    (catch Exception e
      ;; If we can't resolve the canonical path, deny access
      false)))

(defn validate-path
  "Validate that a path is allowed. Returns [ok? error-message]."
  [path]
  (cond
    (str/blank? path)
    [false "Path cannot be empty"]
    
    (not (path-allowed? path))
    [false (str "Access denied: Path is outside allowed directories. Allowed paths: "
                (str/join ", " (get-allowed-paths)))]
    
    :else
    [true nil]))

;; JSON-RPC 2.0 message handling

(defn json-rpc-error
  "Create a JSON-RPC 2.0 error response."
  [id code message & [data]]
  (cond-> {:jsonrpc "2.0"
           :id id
           :error {:code code
                   :message message}}
    data (assoc-in [:error :data] data)))

(defn json-rpc-response
  "Create a JSON-RPC 2.0 success response."
  [id result]
  {:jsonrpc "2.0"
   :id id
   :result result})

(defn json-rpc-notification
  "Create a JSON-RPC 2.0 notification (no id)."
  [method params]
  {:jsonrpc "2.0"
   :method method
   :params params})

;; MCP Protocol Implementation

(def server-info
  {:name "clojure-mcp-server"
   :version "0.1.0"})

(def server-capabilities
  {:tools {}})

;; Tool definitions

(def tools
  [{:name "read_file"
    :description "Read the contents of a file from the filesystem"
    :inputSchema {:type "object"
                  :properties {:path {:type "string"
                                      :description "The absolute path to the file to read"}}
                  :required ["path"]}}
   
   {:name "list_directory"
    :description "List the contents of a directory"
    :inputSchema {:type "object"
                  :properties {:path {:type "string"
                                      :description "The absolute path to the directory to list"}}
                  :required ["path"]}}])

;; Tool implementations

(defn read-file-tool
  "Implementation of the read_file tool."
  [{:keys [path]}]
  (let [[valid? error-msg] (validate-path path)]
    (if-not valid?
      {:isError true
       :content [{:type "text"
                  :text error-msg}]}
      (try
        (let [file (io/file path)]
          (if (.exists file)
            (if (.isFile file)
              {:content [{:type "text"
                          :text (slurp file)}]}
              {:isError true
               :content [{:type "text"
                          :text (str "Path is not a file: " path)}]})
            {:isError true
             :content [{:type "text"
                        :text (str "File not found: " path)}]}))
        (catch Exception e
          {:isError true
           :content [{:type "text"
                      :text (str "Error reading file: " (.getMessage e))}]})))))

(defn list-directory-tool
  "Implementation of the list_directory tool."
  [{:keys [path]}]
  (let [[valid? error-msg] (validate-path path)]
    (if-not valid?
      {:isError true
       :content [{:type "text"
                  :text error-msg}]}
      (try
        (let [file (io/file path)]
          (if (.exists file)
            (if (.isDirectory file)
              (let [files (.listFiles file)
                    file-list (map (fn [f]
                                     (str (.getName f)
                                          (when (.isDirectory f) "/")))
                                   (sort-by #(.getName %) files))]
                {:content [{:type "text"
                            :text (str/join "\n" file-list)}]})
              {:isError true
               :content [{:type "text"
                          :text (str "Path is not a directory: " path)}]})
            {:isError true
             :content [{:type "text"
                        :text (str "Directory not found: " path)}]}))
        (catch Exception e
          {:isError true
           :content [{:type "text"
                      :text (str "Error listing directory: " (.getMessage e))}]})))))

(def tool-handlers
  {"read_file" read-file-tool
   "list_directory" list-directory-tool})

;; Request handlers

(defn handle-initialize
  "Handle the initialize request from the client."
  [params]
  {:protocolVersion "2024-11-05"
   :capabilities server-capabilities
   :serverInfo server-info})

(defn handle-tools-list
  "Handle the tools/list request."
  [_params]
  {:tools tools})

(defn handle-tools-call
  "Handle the tools/call request."
  [{:keys [name arguments]}]
  (if-let [handler (get tool-handlers name)]
    (handler arguments)
    {:isError true
     :content [{:type "text"
                :text (str "Unknown tool: " name)}]}))

(def request-handlers
  {"initialize" handle-initialize
   "tools/list" handle-tools-list
   "tools/call" handle-tools-call})

;; Message processing

(defn process-request
  "Process a single JSON-RPC request."
  [{:keys [id method params] :as request}]
  (try
    (if-let [handler (get request-handlers method)]
      (let [result (handler params)]
        (json-rpc-response id result))
      (json-rpc-error id -32601 (str "Method not found: " method)))
    (catch Exception e
      (json-rpc-error id -32603 "Internal error" {:message (.getMessage e)}))))

(defn send-message
  "Send a JSON-RPC message to stdout."
  [writer message]
  (let [json-str (json/write-str message)]
    (binding [*out* writer]
      (println json-str)
      (flush))))

(defn read-message
  "Read a JSON-RPC message from stdin."
  [reader]
  (when-let [line (.readLine reader)]
    (try
      (json/read-str line :key-fn keyword)
      (catch Exception e
        (binding [*err* *err*]
          (println "Error parsing JSON:" (.getMessage e)))
        nil))))

;; Main server loop

(defn start-server
  "Start the MCP server using stdio transport."
  []
  (let [reader (BufferedReader. (InputStreamReader. System/in))
        writer (PrintWriter. System/out true)]
    (binding [*err* *err*] ; Keep stderr for logging
      (println "MCP Server starting..."))
    
    (loop []
      (when-let [request (read-message reader)]
        (let [response (process-request request)]
          (send-message writer response))
        (recur)))
    
    (binding [*err* *err*]
      (println "MCP Server stopped."))))

(defn -main
  "Main entry point for the MCP server.
  
  Supports environment variables:
  - MCP_ALLOWED_PATHS: Colon-separated list of allowed paths (defaults to current directory)"
  [& args]
  ;; Initialize allowed paths from environment variable if set
  (when-let [env-paths (System/getenv "MCP_ALLOWED_PATHS")]
    (let [paths (str/split env-paths (re-pattern (System/getProperty "path.separator")))]
      (set-allowed-paths! paths)
      (binding [*err* *err*]
        (println "Allowed paths configured:" (str/join ", " (get-allowed-paths))))))
  
  (start-server))

(comment
  ;; Test the server by simulating requests
  
  ;; Configure allowed paths for testing
  (set-allowed-paths! ["/Users/jumar/workspace/clojure/clojure-experiments"])
  (get-allowed-paths)
  ;; => ["/Users/jumar/workspace/clojure/clojure-experiments"]
  
  ;; Test path validation
  (path-allowed? "/Users/jumar/workspace/clojure/clojure-experiments/README.md")
  ;; => true
  
  (path-allowed? "/etc/passwd")
  ;; => false
  
  (path-allowed? "/Users/jumar/workspace/clojure/clojure-experiments/../../../etc/passwd")
  ;; => false (canonical path resolution prevents path traversal)
  
  ;; Initialize request
  (process-request
   {:jsonrpc "2.0"
    :id 1
    :method "initialize"
    :params {:protocolVersion "2024-11-05"
             :capabilities {}
             :clientInfo {:name "test-client" :version "1.0.0"}}})
  ;; => {:jsonrpc "2.0",
  ;;     :id 1,
  ;;     :result {:protocolVersion "2024-11-05",
  ;;              :capabilities {:tools {}},
  ;;              :serverInfo {:name "clojure-mcp-server", :version "0.1.0"}}}
  
  ;; List tools
  (process-request
   {:jsonrpc "2.0"
    :id 2
    :method "tools/list"
    :params {}})
  ;; => {:jsonrpc "2.0",
  ;;     :id 2,
  ;;     :result {:tools [{:name "read_file", ...}]}}
  
  ;; Call read_file tool - allowed path
  (process-request
   {:jsonrpc "2.0"
    :id 3
    :method "tools/call"
    :params {:name "read_file"
             :arguments {:path "/Users/jumar/workspace/clojure/clojure-experiments/README.md"}}})
  
  ;; Call read_file tool - disallowed path
  (process-request
   {:jsonrpc "2.0"
    :id 4
    :method "tools/call"
    :params {:name "read_file"
             :arguments {:path "/etc/passwd"}}})
  ;; => {:jsonrpc "2.0",
  ;;     :id 4,
  ;;     :result {:isError true,
  ;;              :content [{:type "text",
  ;;                         :text "Access denied: Path is outside allowed directories..."}]}}
  
  ;; Test path traversal attempt
  (process-request
   {:jsonrpc "2.0"
    :id 5
    :method "tools/call"
    :params {:name "read_file"
             :arguments {:path "/Users/jumar/workspace/clojure/clojure-experiments/../../../etc/passwd"}}})
  ;; => {:jsonrpc "2.0",
  ;;     :id 5,
  ;;     :result {:isError true,
  ;;              :content [{:type "text",
  ;;                         :text "Access denied..."}]}}
  
  ;; Test with multiple allowed paths
  (set-allowed-paths! ["/Users/jumar/workspace/clojure/clojure-experiments"
                       "/tmp"])
  (path-allowed? "/tmp/test.txt")
  ;; => true
  
  )
