(ns four-clojure.147-pascal-trapezoid
  "http://www.4clojure.com/problem/147.
  Write a function that for any given vector of numbers returns an infinite lazy sequence of vectors
  where each next one is constructured from the previous one followint Pascal's triangle rules.
  Beware of overflows.")

(defn pascal-seq [row-numbers]
  (let [next-row (mapv +'
                       ;; two same collections shifted by one and summed
                       (conj (vec row-numbers) 0)
                       (cons 0 row-numbers))]
    (lazy-seq (cons row-numbers (pascal-seq next-row)))))

;; alternative
#_(defn pascal-seq [row-numbers]
  (iterate #(map +' (cons 0 %) (concat % [0]))
           row-numbers))


(= (second (pascal-seq [2 3 2])) [2 5 5 2])

(= (take 5 (pascal-seq [1])) [[1] [1 1] [1 2 1] [1 3 3 1] [1 4 6 4 1]])

(= (take 2 (pascal-seq [3 1 2])) [[3 1 2] [3 4 3 2]])

(= (take 100 (pascal-seq [2 4 2])) (rest (take 101 (pascal-seq [2 2]))))
