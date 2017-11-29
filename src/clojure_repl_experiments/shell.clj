(ns clojure-repl-experiments.shell
  (:require [me.raynes.conch :refer [programs with-programs let-programs] :as sh]
            [me.raynes.conch.low-level :as low-sh]))

;;; Running shell commands from Clojure
;;; `conch` library is useful and more flexible than `clojure.java.shell`
;;; Especially low-level API: https://github.com/Raynes/conch#low-level-usage

(programs echo tail)
;; Once you define program, you can execute it!
(echo "Hi")

;; let-programs is useful when you cant to specify programs that are not on the PATH
(let-programs [full-echo "/bin/echo"]
  (full-echo "HIIIII!"))

;; we can redirect output somewhere else: https://github.com/Raynes/conch#output

(defn read-until-done [reader]
  (doseq [line (line-seq reader)]
    (println "FROM READER: " line)))

(defn read-command-out []
  (let [process (low-sh/proc "tail" "-f" "/var/log/system.log")]
    (with-open [process-output-reader (java.io.BufferedReader. (java.io.InputStreamReader. (:out process)))]
      (read-until-done process-output-reader))
    (println "Process exit code: " (.exitValue (:process process)))))
#
#_(read-command-out)
