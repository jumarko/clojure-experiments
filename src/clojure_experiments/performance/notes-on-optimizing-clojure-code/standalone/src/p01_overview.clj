(ns p01-overview
  "Starts the blog post series - see https://cuddly-octo-palm-tree.com/posts/2022-01-16-opt-clj-1/
  See also https://cuddly-octo-palm-tree.com/archives/.

  Advent of code - day 12 is used as an example: https://adventofcode.com/2021/day/12
  "
  (:require
   [clojure.set :as set]
   [clojure.string :as string]
   (:gen-class)))

;;; His initial submission: https://cuddly-octo-palm-tree.com/posts/2022-01-16-opt-clj-1/#:~:text=slow%2C%20keep%20going.-,A%20concrete%20example,-To%20make%20things
(defn parse
  [lines]
  (->> lines
       (map #(string/split % #"-"))
       (mapcat (fn [[a b]] [{a #{b}} {b #{a}}]))
       (apply merge-with set/union {})))

(defn small?
  [^String s]
  (= s (.toLowerCase s)))

(defn ends?
  [[path _]]
  (= (last path) "end"))

(defn part2
  [input]
  (loop [num-paths 0
         paths [[["start"] #{"start"} false]]]
    (if (empty? paths)
      num-paths
      (let [path (for [[path visited twice?] paths
                       next-step (get input (last path))
                       :when (or (not (visited next-step))
                                 (and (not= "start" next-step)
                                      (not twice?)))]
                   [(conj path next-step)
                    (if (small? next-step)
                      (conj visited next-step)
                      visited)
                    (or twice?
                        (boolean (visited next-step)))])]
        (recur (->> path (filter ends?) count (+ num-paths))
               (->> path (remove ends?)))))))

(defn -main
  [& args]
  (let [input (-> (slurp "src/clojure_experiments/performance/notes-on-optimizing-clojure-code/day12.txt")
                  (string/split-lines)
                  parse)]
    (part2 input)))

(comment

  (time (-main))
  ;; "Elapsed time: 1266.318248 msecs"

  .)
