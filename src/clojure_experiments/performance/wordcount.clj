(ns clojure-experiments.performance.wordcount
  "https://github.com/benhoyt/countwords
  https://benhoyt.com/writings/count-words/
  https://github.com/benhoyt/countwords/blob/master/test.sh

  See clojurians slack discussion: https://clojurians.slack.com/archives/C053AK3F9/p1659109108134319?thread_ts=1659109081.172069&cid=C053AK3F9"
  (:require
   [clojure.string :as str]
   [clj-async-profiler.core :as prof]
   [clojure.java.io :as io]))


;;; this is OP solution in Clojure: https://clojurians.slack.com/archives/C053AK3F9/p1659109108134319?thread_ts=1659109081.172069&cid=C053AK3F9
(defn word-freqs [text]
  (let [data (slurp text)]
    (->> (str/split data #"\n|\W+")
         (map #(str/lower-case %))
         (frequencies)
         (sort-by val >))))
(comment
  ;; prepare data: https://github.com/benhoyt/countwords/blob/master/test.sh
  ;; cat kjvbible.txt kjvbible.txt kjvbible.txt kjvbible.txt kjvbible.txt kjvbible.txt kjvbible.txt kjvbible.txt kjvbible.txt kjvbible.txt >kjvbible_x10.txt

  (time (first (word-freqs "src/clojure_experiments/performance/kjvbible_x10.txt")))

  )

;; try to optimize a bit:
;; - but how?
;; - it's clear that frequencies and also string splitting is taking a lot of CPU time
(comment
  (prof/profile  (first (word-freqs "src/clojure_experiments/performance/kjvbible_x10.txt")))
  (prof/list-event-types)
  )

(defn my-word-freqs [text]
  (let [lines (line-seq (io/reader text))
        word-freqs (reduce (fn [all-freqs line]
                             (let [line-freqs (->> (str/split line #"\W+")
                                                   (map str/lower-case)
                                                   frequencies)]

                               (merge-with + all-freqs line-freqs)))
                           {}
                           lines)]
    (sort-by val word-freqs)))

(comment
  ;; Unfortunately, even this modest attempt is slower than the original!
  ;; - perhaps merge is slow?
  (time (first (my-word-freqs "src/clojure_experiments/performance/kjvbible.txt")))
  ;; "Elapsed time: 1320.943469 msecs"

  (time (first (word-freqs "src/clojure_experiments/performance/kjvbible.txt")))
  ;; "Elapsed time: 578.499542 msecs"

  )

(defn my-word-freqs2 [text]
  (let [rdr (io/reader text)
        word-freqs (loop [all-freqs {}
                          line (.readLine rdr)]
                     (if line
                       (let [word-freqs (->> (str/split line #"\W+")
                                             (map str/lower-case)
                                             frequencies)]
                         (recur (merge-with + all-freqs word-freqs)
                                (.readLine rdr)))
                       all-freqs))]
    (sort-by val word-freqs)))

  (comment
  ;; with loop, it's no better either...
  (time (first (my-word-freqs2 "src/clojure_experiments/performance/kjvbible.txt")))
  ;; "Elapsed time: 1418.682931 msecs"
  .)


;; another attempt - first get only words line-by-line, then frequencies for the whole thing
(defn my-word-freqs3 [text]
  (let [rdr (io/reader text)
        words (loop [all-words (transient [])
                     line (.readLine rdr)]
                (if line
                  (let [words (->> (str/split line #"\W+")
                                   (map str/lower-case))
                        all-new-words (reduce (fn [all w] (conj! all w))
                                              all-words
                                              words)]
                    (recur all-new-words
                           (.readLine rdr)))
                  (persistent! all-words)))]
    (->> words
         frequencies
         (sort-by val))))
(comment
  ;; Still not better :(
  (time (first (my-word-freqs3 "src/clojure_experiments/performance/kjvbible.txt")))
  ;; "Elapsed time: 702.156938 msecs"

  .)
