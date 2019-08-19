(ns four-clojure.053-longest-increasing-subseq
  "http://www.4clojure.com/problem/53.
  Given a vector of integers, find the longest consecutive sub-sequence of increasing numbers.
  If two sub-sequences have the same length, use the one that occurs first.
  An increasing sub-sequence must have a length of 2 or greater to qualify."
  (:require [clojure.test :as t]))

;;; Check https://stackoverflow.com/questions/23207490/partition-a-seq-by-a-windowing-predicate-in-clojure
;;; to see for alternative solutions

(defn longest-increasing-subseq [numbers]
  (let [[first-seq second-seq] (reduce
                                (fn [[longest-seq current-seq] current-number]
                                  (let [previous-number (peek current-seq)]
                                    (if (or (nil? previous-number)
                                            (< previous-number current-number))
                                      ;; keep building current-seq

                                      [longest-seq (conj current-seq current-number)]
                                      (if (> (count current-seq) (count longest-seq))
                                        [current-seq [current-number]]
                                        ;; notice that if both seqs have equal size than the first one t/is returned
                                        [longest-seq [current-number]]))))
                                [[] []]
                                numbers)
        longest-seq (if (< (count first-seq) (count second-seq))
          second-seq
          first-seq)]
    (if (< 1 (count longest-seq))
      longest-seq
      [])))

;;; alternative implementation from leetwinski: http://www.4clojure.com/problem/solutions/53
(defn longest-increasing-subseq [numbers]
  (or (->> (range (count numbers) 1 -1)
           (mapcat #(partition % 1 numbers))
           (filter #(apply < %))
           first)
      []))

;;; tests
(t/deftest longest-subseq
  (t/testing "trivial single sequence"
    (t/is (= [1 2]
             (longest-increasing-subseq [1 2 0]))))
  (t/testing "two sequences of same length"
    (t/is (= [1 2]
             (longest-increasing-subseq [1 2 0 3]))))
  (t/testing "strictly increasing?"
    (t/is (= [1 2]
             (longest-increasing-subseq [1 2 2 0 3]))))
  (t/testing "shorter sequence, then longer sequence"
    (t/is (= [0 3 4 5]
             (longest-increasing-subseq [1 2 0 3 4 5 4]))))
  )

(t/deftest official-tests
  (t/testing "test1" (t/is (= [0 1 2 3]
                              (longest-increasing-subseq [1 0 1 2 3 0 4 5]))))
  (t/testing "test2" (t/is (= [5 6]
                              (longest-increasing-subseq [5 6 1 3 2 7]))))
  (t/testing "test3" (t/is (= [3 4 5]
                              (longest-increasing-subseq [2 3 3 4 5]))))
  (t/testing "test4" (t/is (= []
                              (longest-increasing-subseq [7 6 5 4])))))
