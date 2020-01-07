(ns clojure-experiments.books.sicp.ch2-abstractions-data.quiz-answer)

;; Ex. 2.6. Church numerals (p. 93)
;; How do you represent them and how you can use them
;; If stuck see http://community.schemewiki.org/?sicp-ex-2.6

(comment
  
  (defn zero ,,,)
  (defn one ,,,)
  (defn two ,,,)
  (defn three ,,,)

  (defn church-to-int [church-number] ,,,)

  (defn add-one ,,,)

  (defn plus ,,,))


;; Ex. 2.18 (p. 103)
;; Write the 'reverse' function
(defn my-reverse [l]
  (loop [[fst & rst] l
         rev '()]
    (if fst
      (recur rst (conj rev fst))
      rev)))

(assert (= '(25 16 9 4 1) (my-reverse '(1 4 9 16 25))))
(assert (= '() (my-reverse '())))
