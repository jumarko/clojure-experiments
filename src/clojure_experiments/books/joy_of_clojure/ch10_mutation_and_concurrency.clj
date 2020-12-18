(ns clojure-experiments.books.joy-of-clojure.ch10-mutation-and-concurrency
  "Structure of the chapter:
  - refs (10.1 and 10.2)
  - agents (10.3)
  - atoms (10.4)
  - locks (10.5)
  - vars and dynamic binding (10.6)
  "
  (:require [clojure-experiments.books.joy-of-clojure.ch05-collections :as ch05])
  (:import java.util.concurrent.Executors))


;;; Refs (p. 226)

;; p. 227 - dothreads!
(defonce thread-pool (delay (Executors/newFixedThreadPool (+ 2 (-> (Runtime/getRuntime) .availableProcessors)))))

(defn dothreads!
  [ f & {thread-count :threads
         exec-count :times
         :or {thread-count 1 exec-count 1}}]
  (dotimes [t thread-count]
    (.submit @thread-pool #(dotimes [_ exec-count] (f)))))

(dothreads! #(.print System/out "Hi ") :threads 3 :times 4)
;; prints: Hi Hi Hi Hi Hi Hi Hi Hi Hi Hi Hi Hi 


;; chessboard representation using refs (p. 228)
(def initial-board
  [[:- :k :-]
   [:- :- :-]
   [:- :K :-]])
(defn board-map [f board]
  (mapv #(mapv f %) board) ; here's my approach that is shorter than in the book:
  #_(mapv #(vec (for [s %] (f s))) board))
(board-map str initial-board)
;; => [[":-" ":k" ":-"] [":-" ":-" ":-"] [":-" ":K" ":-"]]

(defn reset-board!
  "Resets the board state.
  Generally, these types of functions are a bad idea, but matters of page count force our hand."
  []
  (def board (board-map ref initial-board))
  (def to-move (ref [[:K [2 1]] [:k [0 1]]]))
  (def num-moves (ref 0)))

(def king-moves (partial ch05/neighbors
                         [[-1 -1] [-1 0] [-1 1]
                          [0 -1] [0 1]
                          [1 -1] [1 0] [1 1]]
                         3))
(defn good-move? [to enemy-sq]
  (when (not= to enemy-sq)
    to))

(defn choose-move
  "Randomly choose a legal move"
  [[[mover mpos] [_ enemy-pos]]]
  [mover (some #(good-move? % enemy-pos)
               (shuffle (king-moves mpos)))])

(reset-board!)
(take 5 (repeatedly #(choose-move @to-move)))
;; => ([:K [2 2]] [:K [1 2]] [:K [1 1]] [:K [2 0]] [:K [1 0]])


;;; Note: I'm not following the "refs" chapter closely with all the examples
;;; It was useful to read but refs are rarely used so I only read it quickly


;;; Agents (p. 240 - 249)
;;; Agents have a queue of actions that need to be performed on the value
;;; Only one action can be operating on agents at the same time

(def joy (agent []))
(send joy conj "First edition")
@joy
;; => ["First edition"]

(defn slow-conj [coll item]
  (Thread/sleep 1000)
  (conj coll item))

(send joy slow-conj "Second edition")
;; => #agent[{:status :ready, :val ["First edition"]} 0x207edf7e]
@joy
;; => ["First edition" "Second edition"]

;; Controlling IO with an agent (p. 243)
;; - serializing access to a resources such as file, etc.
;; Here we add a way for threads to report progress giving each thread
;; their unique number

(def log-agent (agent 0))

;; now comes the update function - `msg-id` is the agent's state
(defn do-log [msg-id message]
  (println msg-id ":" message)
  (inc msg-id))

;; similuate work
(defn do-step [channel message]
  (Thread/sleep 1)
  ;; using `send-off` to do a potentially blocking action (println)
  (send-off log-agent do-log (str channel message)))

(defn three-step [channel]
  (do-step channel " ready to begin (step 0)")
  (do-step channel " warming up (step 1)")
  (do-step channel " really getting going now (step 2)")
  (do-step channel " done! (step 3)"))

(defn all-together-now []
  (dothreads! #(three-step "alpha"))
  (dothreads! #(three-step "beta"))
  (dothreads! #(three-step "gamma")))

(all-together-now)
;;=> prints:
;; 0 : beta ready to begin (step 0)
;; 1 : alpha ready to begin (step 0)
;; 2 : gamma ready to begin (step 0)
;; 3 : alpha warming up (step 1)
;; 4 : beta warming up (step 1)
;; 5 : gamma warming up (step 1)
;; 6 : beta really getting going now (step 2)
;; 7 : alpha really getting going now (step 2)
;; 8 : gamma really getting going now (step 2)
;; 9 : beta done! (step 3)
;; 10 : alpha done! (step 3)
;; 11 : gamma done! (step 3)


;; Agent errors
(comment
  (send log-agent (fn [] 2000)) ; arity error


;; notice seemingly incorrect value
  @log-agent
;; => 12

  (agent-error log-agent)
;; => #error {
;; :cause "Wrong number of args (1) passed to: clojure-experiments.books.joy-of-clojure.ch10-mutation-and-concurrency/eval17576/fn--17577"
;; ...

;; trying another send will throw the old error
;; BUT! it should throw RuntimeException "Agent is failed, needs restart"
  (send log-agent (fn [_] 3000))
;; => Wrong number of args (1) passed to: clojure-experiments.books.joy-of-clojure.ch10-mutation-and-concurrency/eval17576/fn--17577

;; restart the agent
  (restart-agent log-agent 2400 :clear-actions true)


;; :continue error handling mode is used if you specify `:error-handler`


  (defn handle-log-error [the-agent the-err]
    (println "An action sent to the log agent threw " the-err))
;; print

  (set-error-handler! log-agent handle-log-error)
  (set-error-mode! log-agent :continue)

  (send log-agent (fn [x] (/ x 0)))
;; => the `handle-log-error` fn prints:
;; An action sent to the log agent threw  #error {
;;                                                :cause Divide by zero
;; ...

;; another incorrect action
  (send log-agent (fn [] 0))

;; and you can still do something correct
  (send-off log-agent do-log "Stayin' alive, stayin' alive...")
;; 2400 : Stayin' alive, stayin' alive...
  )



;;; Atoms (p. 249 - 251)

(def ^:dynamic *time* (atom 0))
(defn tick [] (swap! *time* inc))
(dothreads! tick :threads 1000 :times 100)
@*time*
;;=> 100000

;; atom used in a transaction as a memoization cache
;; Check `memoize` that doesn't allow you to manipulate the cache
;; (let [mem (atom {})]
;;  (fn [& args]
;;   (if-let [e (find @mem args)]
;;     (val e)
;;     (let [ret (apply f args)]
;;       (swap! mem assoc args ret)
;;       ret)))))
(defn manipulable-memoize [function]
  (let [cache (atom {})]
    (with-meta
      (fn [& args]
        (or (second (find @cache args))
            (let [ret (apply function args)]
              (swap! cache assoc args ret)
              ret)))
      {:cache cache})))

(def slowly (fn [x] (Thread/sleep 1000) x))
#_(time [(slowly 9) (slowly 9)])
"Elapsed time: 2003.566727 msecs"

(def sometimes-slowly (manipulable-memoize slowly))
#_(time [(sometimes-slowly 108) (sometimes-slowly 108)])
"Elapsed time: 1004.703774 msecs"

;; ... one more time ...
#_(time [(sometimes-slowly 108) (sometimes-slowly 108)])
"Elapsed time: 0.251938 msecs"

;; now we can explore the cache
(meta sometimes-slowly)
;; => {:cache #atom[{(9) 9, (108) 108} 0x1abdbe4]}

;; ... manipulate it ....
(swap! (-> sometimes-slowly meta :cache)
       dissoc [108])
;; => {(9) 9}

;; ... and try again
#_(time [(sometimes-slowly 108) (sometimes-slowly 108)])
"Elapsed time: 1001.367097 msecs"




