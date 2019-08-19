(ns four-clojure.074-filter-perfect-squares
  "http://www.4clojure.com/problem/74.
  Given a string of comma separate integers,
  write a function whih returns a new comma separated string
  that only contains the numbers which are perfect squares.")

(defn perfect-squares
  [ints-str]
  (letfn [(perfect-square? [x]
            (let [square-root (int (Math/sqrt x))]
              (= (* square-root square-root) x)))]
    (->> (re-seq #"\d+" ints-str)
         (map #(Integer/parseInt %))
         (filter perfect-square?)
         (clojure.string/join ","))))

(= (perfect-squares "4,5,6,7,8,9") "4,9")

(= (perfect-squares "15,16,25,36,37") "16,25,36")
