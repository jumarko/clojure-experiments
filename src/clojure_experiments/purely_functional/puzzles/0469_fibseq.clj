(ns clojure-experiments.purely-functional.puzzles.0469-fibseq
  "https://ericnormand.me/issues/469
  Solutions: https://gist.github.com/ericnormand/377523537b6f13e34b19cdd56ab48de8")

;;; Presents ubiquitous fibonacci sequence,
;;; with flexible operation and seeding elements (not just + 1 1)


;; First, implement `fib-seq` so it returns a lazy sequence of fibonacci numbers:
(defn fib [a b]
  (let [n (+ a b)]
    (lazy-seq (cons a (fib b n)))))
(defn fib-seq [] (fib 1 1))
(take 10 (fib-seq))
;; => (1 1 2 3 5 8 13 21 34 55)


;; Second, parameterize +, 1, 1 in the definition
(defn fib-seq [op fst snd]
  ;; Note: computing `(op fst snd)` in the let block makes it fail way too early with long overflow
  (let [nxt (op fst snd)]
    (lazy-seq (cons fst (fib-seq op snd nxt)))))
(take 10 (fib-seq + 1 1))
;; => (1 1 2 3 5 8 13 21 34 55)

;; Third, explore more varied examples:
(take 10 (fib-seq str "X" "O"))
;; => ("X" "O" "XO" "OXO" "XOOXO" "OXOXOOXO" "XOOXOOXOXOOXO" "OXOXOOXOXOOXOOXOXOOXO" "XOOXOOXOXOOXOOXOXOOXOXOOXOOXOXOOXO" "OXOXOOXOXOOXOOXOXOOXOXOOXOOXOXOOXOOXOXOOXOXOOXOOXOXOOXO")

(take 10 (fib-seq * 1 1))
;; => (1 1 1 1 1 1 1 1 1 1)

(take 10 (fib-seq * 1 -1))
;; => (1 -1 -1 1 -1 -1 1 -1 -1 1)

(take 10 (fib-seq vector [] []))
;; => ([] [] [[] []] [[] [[] []]] [[[] []] [[] [[] []]]] [[[] [[] []]] [[[] []] [[] [[] []]]]] [[[[] []] [[] [[] []]]] [[[] [[] []]] [[[] []] [[] [[] []]]]]] [[[[] [[] []]] [[[] []] [[] [[] []]]]] [[[[] []] [[] [[] []]]] [[[] [[] []]] [[[] []] [[] [[] []]]]]]] [[[[[] []] [[] [[] []]]] [[[] [[] []]] [[[] []] [[] [[] []]]]]] [[[[] [[] []]] [[[] []] [[] [[] []]]]] [[[[] []] [[] [[] []]]] [[[] [[] []]] [[[] []] [[] [[] []]]]]]]] [[[[[] [[] []]] [[[] []] [[] [[] []]]]] [[[[] []] [[] [[] []]]] [[[] [[] []]] [[[] []] [[] [[] []]]]]]] [[[[[] []] [[] [[] []]]] [[[] [[] []]] [[[] []] [[] [[] []]]]]] [[[[] [[] []]] [[[] []] [[] [[] []]]]] [[[[] []] [[] [[] []]]] [[[] [[] []]] [[[] []] [[] [[] []]]]]]]]])


(comment
  (take 9 (fib-seq * 1 2))
  ;; 1. Caused by java.lang.ArithmeticException
  ;;    long overflow
  ;; it works up to 8:
  (take 8 (fib-seq * 1 2))
;; => (1 2 2 4 8 32 256 8192)
.)

;; with arbitrary precision we can get the result
(take 10 (fib-seq *' 1 2))
;; => (1 2 2 4 8 32 256 8192 2097152 17179869184)

;; it's still very strange that it fails on long overflow
;; because the first eleven numbers are smaller than Long/MAX_VALUE
(> (Long/MAX_VALUE) (nth (fib-seq *' 1 2) 10))
;; => true
(> (Long/MAX_VALUE) (nth (fib-seq *' 1 2) 11))
;; => false

;; so `fib-seq` is really realizing far too many elements - but why?
;; Because I was computing `(op fst snd)` too early, in the let block
;; inlining it makes it possible to get two more elements:
(defn fib-seq [op fst snd]
  (cons fst (lazy-seq (fib-seq op snd (op fst snd)))))
(take 10 (fib-seq * 1 2))
;; => (1 2 2 4 8 32 256 8192 2097152 17179869184)
