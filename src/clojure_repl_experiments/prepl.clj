(ns clojure-repl-experiments.prepl
  (:require [clojure.core.server :as clj-server]
            [clojure.java.io :as io])
  (:import (clojure.lang DynamicClassLoader LineNumberingPushbackReader)
           (java.net ServerSocket Socket)
           (java.io OutputStream OutputStreamWriter)))

;;;; Experiments with new PREPL: https://github.com/clojure/clojure/blob/86a158d0e0718f5c93f9f2bb71e26bc794e7d58e/src/clj/clojure/core/server.clj#L187
;;;; Inspired by raymcdermott on slack (#clojure-spec)


(defn configured-prepl
  []
  (clj-server/io-prepl :valf identity))

(defn shared-prepl
  [opts]
  (let [socket-opts {:port          5555
                     :server-daemon false                   ; Keep the app running
                     :accept        `configured-prepl}]
    (let [server (clj-server/start-server (merge socket-opts opts))]
      (println "listening on port" (.getLocalPort ^ServerSocket server)))

    ;; A clojure.lang.DynamicClassLoader is needed to enable interactive library addition
    #_(try (let [cl (.getContextClassLoader (Thread/currentThread))]
           (.setContextClassLoader (Thread/currentThread) (DynamicClassLoader. cl))

           (let [server (clj-server/start-server (merge socket-opts opts))]
             (println "listening on port" (.getLocalPort ^ServerSocket server))))

         (catch Exception e (str "ClassLoader issue - caught exception: " (.getMessage e))))))


;;; I then have a web server to intermediate
(defn prepl-client
  "Attaching the PREPL to a given `host` and `port`"
  [host port]
  (let [client        (Socket. ^String host ^Integer port)
        server-reader (LineNumberingPushbackReader. (io/reader client))
        server-writer (OutputStreamWriter. (io/output-stream client))]
    [server-reader server-writer]))


;;; and this takes request from a REPL client in the browser
;;; which send over strings that are then read and sent to the remote `prepl`
(defn shared-eval
  [repl form]
  #_(send-code (:writer repl) form)

  (when-let [result (try (read (:reader repl))
                         (catch Exception e (println (str "Read fail" e))))]
    (loop [results [result]]
      ; TODO ... see if we can catch / parse spec errors on this side
      (if (= :ret (:tag (last results)))
        results
        (recur (conj results (try (read (:reader repl))
                                  (catch Exception e (println (str "Read fail" e))))))))))


(comment

  ;; response from server when `(defn foo 1)` is sent
  (require '[zprint.core :as zp :refer [zprint]])

  (read-string (pr-str (Object.)))

  (zprint {:tag :ret, :val {:cause "Call to clojure.core/defn did not conform to spec:\nIn: [1] val: 1 fails spec: :clojure.core.specs.alpha/arg-list at: [:args :bs :arity-1 :args] predicate: vector?\nIn: [1] val: 1 fails spec: :clojure.core.specs.alpha/args+body at: [:args :bs :arity-n :bodies] predicate: (cat :args :clojure.core.specs.alpha/arg-list :body (alt :prepost+body (cat :prepost map? :body (+ any?)) :body (* any?)))\n", :via [{:type clojure.lang.Compiler$CompilerException, :message "clojure.lang.ExceptionInfo: Call to clojure.core/defn did not conform to spec:\nIn: [1] val: 1 fails spec: :clojure.core.specs.alpha/arg-list at: [:args :bs :arity-1 :args] predicate: vector?\nIn: [1] val: 1 fails spec: :clojure.core.specs.alpha/args+body at: [:args :bs :arity-n :bodies] predicate: (cat :args :clojure.core.specs.alpha/arg-list :body (alt :prepost+body (cat :prepost map? :body (+ any?)) :body (* any?)))\n #:clojure.spec.alpha{:problems ({:path [:args :bs :arity-1 :args], :pred clojure.core/vector?, :val 1, :via [:clojure.core.specs.alpha/defn-args :clojure.core.specs.alpha/args+body :clojure.core.specs.alpha/arg-list :clojure.core.specs.alpha/arg-list], :in [1]} {:path [:args :bs :arity-n :bodies], :pred (clojure.spec.alpha/cat :args :clojure.core.specs.alpha/arg-list :body (clojure.spec.alpha/alt :prepost+body (clojure.spec.alpha/cat :prepost clojure.core/map? :body (clojure.spec.alpha/+ clojure.core/any?)) :body (clojure.spec.alpha/* clojure.core/any?))), :val 1, :via [:clojure.core.specs.alpha/defn-args :clojure.core.specs.alpha/args+body :clojure.core.specs.alpha/args+body], :in [1]}), :spec #object[clojure.spec.alpha$regex_spec_impl$reify__2436 0x233319e7 \"clojure.spec.alpha$regex_spec_impl$reify__2436@233319e7\"], :value (foo 1), :args (foo 1)}, compiling:(NO_SOURCE_PATH:2:1)", :at [clojure.lang.Compiler checkSpecs "Compiler.java" 6891]} {:type clojure.lang.ExceptionInfo, :message "Call to clojure.core/defn did not conform to spec:\nIn: [1] val: 1 fails spec: :clojure.core.specs.alpha/arg-list at: [:args :bs :arity-1 :args] predicate: vector?\nIn: [1] val: 1 fails spec: :clojure.core.specs.alpha/args+body at: [:args :bs :arity-n :bodies] predicate: (cat :args :clojure.core.specs.alpha/arg-list :body (alt :prepost+body (cat :prepost map? :body (+ any?)) :body (* any?)))\n", :data #:clojure.spec.alpha{:problems ({:path [:args :bs :arity-1 :args], :pred clojure.core/vector?, :val 1, :via [:clojure.core.specs.alpha/defn-args :clojure.core.specs.alpha/args+body :clojure.core.specs.alpha/arg-list :clojure.core.specs.alpha/arg-list], :in [1]} {:path [:args :bs :arity-n :bodies], :pred (clojure.spec.alpha/cat :args :clojure.core.specs.alpha/arg-list :body (clojure.spec.alpha/alt :prepost+body (clojure.spec.alpha/cat :prepost clojure.core/map? :body (clojure.spec.alpha/+ clojure.core/any?)) :body (clojure.spec.alpha/* clojure.core/any?))), :val 1, :via [:clojure.core.specs.alpha/defn-args :clojure.core.specs.alpha/args+body :clojure.core.specs.alpha/args+body], :in [1]}), :spec #object[clojure.spec.alpha$regex_spec_impl$reify__2436 0x233319e7 "clojure.spec.alpha$regex_spec_impl$reify__2436@233319e7"], :value (foo 1), :args (foo 1)}, :at [clojure.core$ex_info invokeStatic "core.clj" 4754]}], :trace [[clojure.core$ex_info invokeStatic "core.clj" 4754] [clojure.core$ex_info invoke "core.clj" 4754] [clojure.spec.alpha$macroexpand_check invokeStatic "alpha.clj" 689] [clojure.spec.alpha$macroexpand_check invoke "alpha.clj" 681] [clojure.lang.AFn applyToHelper "AFn.java" 156] [clojure.lang.AFn applyTo "AFn.java" 144] [clojure.lang.Var applyTo "Var.java" 702] [clojure.lang.Compiler checkSpecs "Compiler.java" 6889] [clojure.lang.Compiler macroexpand1 "Compiler.java" 6907] [clojure.lang.Compiler macroexpand "Compiler.java" 6972] [clojure.lang.Compiler eval "Compiler.java" 7046] [clojure.lang.Compiler eval "Compiler.java" 7025] [clojure.core$eval invokeStatic "core.clj" 3206] [clojure.core.server$prepl$fn__8666 invoke "server.clj" 225] [clojure.core.server$prepl invokeStatic "server.clj" 221] [clojure.core.server$prepl doInvoke "server.clj" 187] [clojure.lang.RestFn invoke "RestFn.java" 425] [clojure.core.server$io_prepl invokeStatic "server.clj" 269] [clojure.core.server$io_prepl doInvoke "server.clj" 261] [clojure.lang.RestFn invoke "RestFn.java" 421] [clojure_repl_experiments.prepl$configured_prepl invokeStatic "form-init3426780150075104129.clj" 14] [clojure_repl_experiments.prepl$configured_prepl invoke "form-init3426780150075104129.clj" 12] [clojure.lang.AFn applyToHelper "AFn.java" 152] [clojure.lang.AFn applyTo "AFn.java" 144] [clojure.lang.Var applyTo "Var.java" 702] [clojure.core$apply invokeStatic "core.clj" 657] [clojure.core.server$accept_connection invokeStatic "server.clj" 73] [clojure.core.server$start_server$fn__8605$fn__8606$fn__8608 invoke "server.clj" 117] [clojure.lang.AFn run "AFn.java" 22] [java.lang.Thread run "Thread.java" 748]], :data #:clojure.spec.alpha{:problems ({:path [:args :bs :arity-1 :args], :pred clojure.core/vector?, :val 1, :via [:clojure.core.specs.alpha/defn-args :clojure.core.specs.alpha/args+body :clojure.core.specs.alpha/arg-list :clojure.core.specs.alpha/arg-list], :in [1]} {:path [:args :bs :arity-n :bodies], :pred (clojure.spec.alpha/cat :args :clojure.core.specs.alpha/arg-list :body (clojure.spec.alpha/alt :prepost+body (clojure.spec.alpha/cat :prepost clojure.core/map? :body (clojure.spec.alpha/+ clojure.core/any?)) :body (clojure.spec.alpha/* clojure.core/any?))), :val 1, :via [:clojure.core.specs.alpha/defn-args :clojure.core.specs.alpha/args+body :clojure.core.specs.alpha/args+body], :in [1]}), :spec #object[clojure.spec.alpha$regex_spec_impl$reify__2436 0x233319e7 "clojure.spec.alpha$regex_spec_impl$reify__2436@233319e7"], :value (foo 1), :args (foo 1)}}, :ns "user", :form "(defn foo 1)"})

  )
