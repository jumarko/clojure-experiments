(ns clojure-experiments.walk
  (:require [clojure.string :as string]
            [clojure.walk :as w]))

(w/walk prn #(println "RESULT: " %) [[1 2] {:a 1 :b 20} [{:aa 1000}]])

(defn transform-keys
  "Recursively transforms all map keys in coll with the transform-key fn.
  See https://stackoverflow.com/questions/25787001/hyphenating-underscored-clojure-map-keys-for-database-results
  and `walk/keywordize-keys`"
  [transform-key coll]
  (letfn [(transform [x] (if (map? x)
                           (into {} (map (fn [[k v]] [(transform-key k) v]) x))
                           x))]
    (w/postwalk transform coll)))

(defn dash->underscore [akey]
  (cond
    (keyword? akey)
    (keyword (string/replace (name akey) \- \_))

    (string? akey)
    (string/replace (name akey) \- \_)

    :dont-know-how-to-transform
    akey))

(defn to-db-keys-with-underscores
  "Replaces all string or keyword keys in the map containing dashes to corresponding keys with underscores
  to conform to the SQL syntax requirements.
  Works recursively for (i.e. nested maps supported)."
  [m]
  (transform-keys dash->underscore m))

(comment
  (to-db-keys-with-underscores {:job-id 123 :project-name "abc" :results-dir {:absolute-path "/x/y/z"}})
  ;; => {:job_id 123, :project_name "abc", :results_dir {:absolute_path "/x/y/z"}}

  ;; works on strings too
  (to-db-keys-with-underscores {"job-id" 123 "project-name" "abc" "results-dir" {"absolute-path" "/x/y/z"}})
  ;; => {"job_id" 123, "project_name" "abc", "results_dir" {"absolute_path" "/x/y/z"}}

  ;; should keep non string/keyword keys untouched
  (to-db-keys-with-underscores {"job-id" 123 "project-name" "abc" "results-dir" {1 "/x/y/z"}})
  ;; => {"job_id" 123, "project_name" "abc", "results_dir" {1 "/x/y/z"}}

  ;;
  )
;;; Learning to walk with Clojure https://www.abhinavomprakash.com/posts/clojure-walk/
(w/prewalk-demo [1 [2 [3 "A" "B"] 4]])
;; prints:
;; Walked: [1 [2 [3 "A" "B"] 4]]
;; Walked: 1
;; Walked: [2 [3 "A" "B"] 4]
;; Walked: 2
;; Walked: [3 "A" "B"]
;; Walked: 3
;; Walked: "A"
;; Walked: "B"
;; Walked: 4

(defn append-or-inc
  [x]
  (if (vector? x)
    (conj x 777)
    (inc x)))

(def tree [1 [2] [3 [4]]])

;; prewalk first appends the element to the parent node,
;; then walks all the children, thus incrementing the appended element too
(w/prewalk append-or-inc tree)
;; => [2 [3 778] [4 [5 778] 778] 778]

;; postwalk first walks the children, so it does not increment 
(w/postwalk append-or-inc tree)
;; => [2 [3 777] [4 [5 777] 777] 777]

(w/prewalk str tree)
;; => "[1 [2] [3 [4]]]"
(w/postwalk str tree)
;; => "[\"1\" \"[\\\"2\\\"]\" \"[\\\"3\\\" \\\"[\\\\\\\"4\\\\\\\"]\\\"]\"]"

;; walking maps
(def map-tree {:a 1 :b {:c 2}})

(w/prewalk-demo map-tree)
;; prints:
;; Walked: {:a 1, :b {:c 2}}
;; Walked: [:a 1] ; this is a MapEntry
;; Walked: :a
;; Walked: 1
;; Walked: [:b {:c 2}]
;; Walked: :b
;; Walked: {:c 2}
;; Walked: [:c 2]
;; Walked: :c
;; Walked: 2

(w/postwalk-demo map-tree)
;; prints:
;; Walked: :a
;; Walked: 1
;; Walked: [:a 1]
;; Walked: :b
;; Walked: :c
;; Walked: 2
;; Walked: [:c 2]
;; Walked: {:c 2}
;; Walked: [:b {:c 2}]
;; Walked: {:a 1, :b {:c 2}}

;; Expanding trees with prewalk
;; - compare to postwalk which only expands 'c
(def a '[1 2 3 b])
(def b '["a" "b" c])
(def c '[:a :b :c])
(def tree [1 'a ['b  [2 'c]]])

(w/prewalk
 (fn [x]
   (if (symbol? x)
     (eval x) ; It's for a good cause, don't judge!
     x))
 tree)
;; => [1 [1 2 3 ["a" "b" [:a :b :c]]] [["a" "b" [:a :b :c]] [2 [:a :b :c]]]]

(w/postwalk
 (fn [x]
   (if (symbol? x)
     (eval x) ; I can see your judgemental eyes.
     x))
 tree)
;; => [1 [1 2 3 b] [["a" "b" c] [2 [:a :b :c]]]]

;; Trimming trees with postwalk
(def a '[1 2 3 b])
(def b '["a" "b" c])
(def c '[:a :b :c])
(def tree '[1 a [b [2 c]]])
(def expanded-tree [1 [1 2 3 ["a" "b" [:a :b :c]]] [["a" "b" [:a :b :c]] [2 [:a :b :c]]]])

(w/prewalk (fn [x]
             (if (vector? x)
               (condp = x
                 a 'a
                 b 'b
                 c 'c
                 x)
               x))
           expanded-tree)
;; => [1 [1 2 3 ["a" "b" c]] [["a" "b" c] [2 c]]]

(w/postwalk (fn [x]
              (if (vector? x)
                (condp = x
                  a 'a
                  b 'b
                  c 'c
                  x)
                x))
            expanded-tree)
;; => [1 a [b [2 c]]]

;; Exercise: remove keys from a map whose value is nil or an empty collection
(def my-map {:a 1 :b [false true false] :c 2 :d nil :e {:ea 1 :eb nil :ec 2 :ed nil :ee {:eea 1 :eeb nil :eee [nil nil nil]}}})
(defn none? [x]
  (or (nil? x)
      ;; Note: in the post they reversed the order of the predicates which is incorrect and leads to ClassCastException
      ((every-pred coll? empty?) x)))
(defn remove-none [x]
  (cond
    (map-entry? x)
    (let [v (val x)]
      (when-not (none? v)
        x))

    (coll? x)
    (into (empty x) ; to preserve the type
          (remove none? x))

    :else x))

(w/prewalk remove-none my-map)
;; => {:a 1, :b [false true false], :c 2, :e {:ea 1, :ec 2, :ee {:eea 1, :eee []}}}

;; postwalk is the correct function to use, because we want to "trim" the tree
;; notice that prewalk doesn't remove the :eee key because it was originally non-empty value (a vector of three nils)
(w/postwalk remove-none my-map)
;; => {:a 1, :b [false true false], :c 2, :e {:ea 1, :ec 2, :ee {:eea 1}}}

;; the above can be somewhat simplified because now we now we need to use `postwalk`
;; => we don't need the redundant none? check in the map-entry case
(defn remove-none2 [x]
  (cond
    (map-entry? x) (when (some? (val x)) x)
    (coll? x) (not-empty (into (empty x) (remove nil? x)))
    :else x))
(w/postwalk remove-none2 my-map)
;; => {:a 1, :b [false true false], :c 2, :e {:ea 1, :ec 2, :ee {:eea 1}}}

(def their-map {:a 1
               :b [1 2 [nil] []]
               :c {:d :a
                   :e {:f nil}
                   :g #{1 2}
                   :h #{nil}
                   :i ()}
               :z {}})
(w/postwalk remove-none2 their-map)
;; => {:a 1, :b [1 2], :c {:d :a, :g #{1 2}}}

