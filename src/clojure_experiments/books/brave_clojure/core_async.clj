(ns clojure-experiments.books.brave-clojure.core-async
  "  E.g. Brave Clojure tutorial: https://www.braveclojure.com/core-async/"
  (:require [clojure.core.async :as a]
            [clojure.string :as string]))

;;; First version of hot dog machine
(defn hot-dog-machine []
  (let [in (a/chan)
        out (a/chan)]
    (a/go (a/<! in)
          (a/>! out "hot dog"))
    [in out]))

(comment 
  (let [[in out] (hot-dog-machine)]
    (a/>!! in "pocket lint")
    (a/<!! out)))

;;; let's make it only accept money (numbers)
(defn hot-dog-machine-v2 [hot-dog-count]
  (let [in (a/chan)
        out (a/chan)]
    (a/go-loop [hc hot-dog-count]
          (if (pos? hc)
            ;; using take and put in the same go block is one way to create "pipelines"
            ;; -> just make input channel of one process the out channel f another
            (let [input (a/<! in)]
              (if (= 3 input)
                (do (a/>! out "hot dog")
                    (recur (dec hc)))
                (do (a/>! out "wilted lettuce")
                    (recur hc))))
            (do (a/close! in)
                (a/close! out))))
    [in out]))

(comment 
  (let [[in out] (hot-dog-machine-v2 2)]
    (a/>!! in "pocket lint")
    (println (a/<!! out))

    (a/>!! in 3)
    (println (a/<!! out))

    (a/>!! in 3)
    (println (a/<!! out))

    ;; the last one will print nil (channel is already closed)
    (a/>!! in 3)
    (println (a/<!! out))))


;; Simple pipeline:
;; using take and put in the same go block is one way to create "pipelines"
;; -> just make input channel of one process the out channel f another
(comment

  (let [c1 (a/chan)
        c2 (a/chan)
        c3 (a/chan)]
    ;; passing string through a series of processes
    (a/go (a/>! c2 (clojure.string/upper-case (a/<! c1))))
    (a/go (a/>! c3 (clojure.string/reverse (a/<! c2))))
    (a/go (println (a/<! c3)))
    (a/>!! c1 "redrum"))

  )


;;; alts!!
;;; let's you use the result of the first successful channel operation among a collection of operations

;; upload set of headshots to a headshot-sharing site and notify headshot owner
;; when the first photo is uploaded
(defn upload [headshot c]
  ;; TODO: Thread/sleep inside `go` looks suspicious; should we use `a/thread`?
  (a/thread (Thread/sleep (rand 100))
        (a/>!! c headshot)))
(comment
  ;; this basically selects one value "randomly" (that is the first value from the channels
  ;; but the order in which the values are delivered is sort of random because of interleaving threads)
  (let [c1 (a/chan)
        c2 (a/chan)
        c3 (a/chan)]
    (upload "serious.jpg" c1)
    (upload "fun.jpg" c2)
    (upload "sassy.jpg" c3)
    (let [[headshot channel] (a/alts!! [c1 c2 c3])]
      (printf "Sending headshot notification for %s (%s)\n" headshot channel)
      (print (bean channel)))))

;; cool application of alts!! is a timeout channel
;; => putting a time limit on concurrent operations
(let [c1 (a/chan)]
  (upload "serious.jpg" c1)
  (let [[headshot channel] (a/alts!! [c1 (a/timeout 20)])]
    (if headshot
      (println "Sending headshot notification for" headshot)
      (println "Timed out"))))

;; atls!! can be used for 'put' operations too!
;; => just use vector inside a vector
(let [c1 (a/chan)
      c2 (a/chan)
      c2-result-chan (a/go (a/<! c2))]
  
  (let [[value channel] (a/alts!! [c1 [c2 "put!"]])]
    ;; this prints 'true' because put operation returns true
    (println value)
    ;; this prints "put!"
    (println (a/<!! c2-result-chan))
    (= channel c2)))


;;; Queues
;;; Download quotes form a website and write them to a file serially so they don't interleave
(defn append-to-file
  "Write a string to the end of a file"
  [filename s]
  (spit filename s :append true))

(defn format-quote
  "Delineate the beginning and end of a quote because it's convenient"
  [quote]
  (str "=== BEGIN QUOTE ===\n" quote "=== END QUOTE ===\n\n"))

(defn random-quote
  "Retrieve a random quote and format it"
  []
  (format-quote (slurp "https://www.braveclojure.com/random-quote")))

(defn snag-quotes
  [filename num-quotes]
  (let [c (a/chan)]
    (a/go-loop []
      (append-to-file filename (a/<! c))
      (recur))
    (dotimes [n num-quotes]
      (println "processing next quote")
      (a/go (a/>! c (random-quote))))))

(comment
  (snag-quotes "quotes.txt" 5)
  )



;;; Escape Callback Hell with Process Pipeliens
;;; https://www.braveclojure.com/core-async/#Escape_Callback_Hell_with_Process_Pipelines
;;;
;;; With callbacks, it's very easy to create dependencies among layers of callbacks
;;; that aren't immediately obvious - they end up sharing state, making it difficult
;;; to reason about the state of the overall system as the callbacks get triggered.
;;; => solution is to create a process pipeline where each unit of logic lives in its own isolated process
;;;    and all communication between these units happens through explicitly defined input/output channels

(defn upper-caser
  [in]
  (let [out (a/chan)]
    (a/go-loop []
      (a/>! out (string/upper-case (a/<! in)))
      (recur))
    out))

(defn reverser
  [in]
  (let [out (a/chan)]
    (a/go-loop []
      (a/>! out (string/reverse (a/<! in)))
      (recur))
    out))

(defn printer
  [in]
  (a/go-loop []
    (println (a/<! in))
    (recur)))

(comment
  (def in-chan (a/chan))
  (def upper-case-out (upper-caser in-chan))
  (def reverser-out (reverser upper-case-out))
  (printer reverser-out)

  (a/>!! in-chan "redrum")
  (a/>!! in-chan "repaid")

  ;; 
  )

;; Note: above solution CANNOT be refactored like this!
;; Although `go` implicitly returns a channel when the result of the go block body is put
;; with `loop` the body never returns!

(comment
  (defn upper-caser
    [in]
    ;;
    (a/go-loop []
      (string/upper-case (a/<! in))
      (recur)))

  (defn reverser
    [in]
    (a/go-loop []
      (string/reverse (a/<! in))
      (recur)))

  (defn printer
    [in]
    (a/go-loop []
      (println (a/<! in))
      (recur)))
  ;; 
  )
