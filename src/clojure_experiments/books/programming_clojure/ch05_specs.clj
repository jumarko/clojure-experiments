(ns clojure-experiments.books.programming-clojure.ch05-specs
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]))

;;; Quick random experiment using index-filter function from chapter two

(s/fdef index-filter
  :args (s/cat :pred (s/fspec :args (s/cat :el any?)
                              :ret boolean?)
               :coll (s/coll-of any?))
  :ret (s/coll-of any?)
  :fn (fn [{:keys [args ret]}]
        ;; the size of filtered output should not exceed the size of the input
        (<= (count ret)
            (count (:coll args)))))
(defn index-filter
  [pred coll]
  (->> coll
       (map-indexed vector)
       (filter (fn [[idx el]]
                 (when (pred el) idx)))
       (map first)))

(index-filter #{\a \c \f}
              "abcdef")
;; => (0 2 5)

(stest/check `index-filter)
;; => ({:spec
;;      #object[clojure.spec.alpha$fspec_impl$reify__2509 0x6ffdd8d "clojure.spec.alpha$fspec_impl$reify__2509@6ffdd8d"],
;;      :clojure.spec.test.check/ret
;;      {:result true,
;;       :pass? true,
;;       :num-tests 1000,
;;       :time-elapsed-ms 1060,
;;       :seed 1789190363729},
;;      :sym clojure-experiments.books.programming-clojure.ch05-specs/index-filter})


