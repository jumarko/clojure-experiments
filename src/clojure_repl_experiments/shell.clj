(ns clojure-repl-experiments.shell
  (:require [me.raynes.conch :refer [programs with-programs let-programs] :as sh]
            [me.raynes.conch.low-level :as low-sh]
            [clojure.java.io :as io]))

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
    (with-open [process-output-reader (io/reader (:out process))]
      (read-until-done process-output-reader))
    (println "Process exit code: " (.exitValue (:process process)))))
#
#_(read-command-out)

;; run process in the context of given directory
(print (low-sh/stream-to-string (low-sh/proc "ls" "-l" :dir "/bin") :out))


;;; real-time interactive IO
;;; motivated by noisesmith's comment on slack:
;;; "* also ymmv but I found ProcessBuilder and Process to be more flexible
;;;  and easier to use for advanced purposes compared to conch that is,
;;;  the built in java classes that conch is built on
;;;  for example I never figured out how to get real-time interactive IO with conch
;;;  (send string to a process, read result, send another string in reply, etc.),
;;;  it is straightforward with ProcessBuilder and Process classes"
(defn write-and-read [command-vector]
  (let [process (apply low-sh/proc command-vector)]
    (with-open [process-input-writer (io/writer (:in process))
                process-output-reader (io/reader (:out process))]
      (println "WRITING...")
      (.write process-input-writer (str "Hello cat!" \newline))
      (println "READING...")
      (.readLine process-output-reader))
    (println "Process exit code: " (.exitValue (:process process)))))

;; doesn't work for some reason - it blocks on `.readLine`
#_(write-and-read ["cat"])
