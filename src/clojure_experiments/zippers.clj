(ns clojure-experiments.zippers
  (:require [clojure.zip :as z]))


;;; Den of Clojure: Functional Zippers: https://www.youtube.com/watch?v=HJJG-xbXRdg

;; nested vectors can be treated as kind of tree structure
(def loc (z/vector-zip [[1 2] [3 4]]))

;; Caveats: not a cursor
#_(->> [[1 2]]
     z/vector-zip
     z/down
     z/down
     z/remove
     z/remove
     (z/insert-left 3)
     )




;;; Clojure Zippers - Luke Vanderhart
;;; https://www.youtube.com/watch?v=6c4DJX2Xr3k

(def data [[1 2] 3 [4 5]])
(def z1 (z/vector-zip data))

(def z2 (z/down z1))
;;=> [[1 2] {:l [], :pnodes [[[1 2] 3 [4 5]]], :ppath nil, :r (3 [4 5])}]

(z2 1)
;;=> {:l [], :pnodes [[[1 2] 3 [4 5]]], :ppath nil, :r (3 [4 5])}



;;; Joseph Fahey: Getting Acquainted With Clojure Zippers: http://josf.info/blog/2014/03/21/getting-acquainted-with-clojure-zippers/

(z/vector-zip [1 [:a :b] 2 3 [40 50 60]])
;;=> [[1 [:a :b] 2 3 [40 50 60]] nil]

(->  [1 [:a :b] 2 3 [40 50 60]]
     z/vector-zip
     z/down
     z/right)
;;=> [[:a :b] {:l [1], :pnodes [[1 [:a :b] 2 3 [40 50 60]]], :ppath nil, :r (2 3 [40 50 60])}]

;; depth-first traversal
(def zzz (z/vector-zip [1 [:a :b] 2 3 [40 50 60]]))
(-> zzz z/next z/node)
;;=> 1
(-> zzz z/next z/next z/node)
;;=> [:a :b]
(-> zzz z/next z/next z/next z/node)
;;=> :a
(-> zzz z/next z/next z/next z/next z/node)
;;=> :b
(-> zzz z/next z/next z/next z/next z/next z/node)




;;; Brian Marick's tutorial - “Editing” trees in Clojure with clojure.zip:
;;; http://www.exampler.com/blog/2010/09/01/editing-trees-in-clojure-with-clojurezip/trackback/index.html

;; We represent tree as a sequence
(def original [1 '(a b c) 2])

;; Transform sequence into the data structure that allows free movement
(def root-loc (z/seq-zip (seq original)))

;; moving around
(-> root-loc z/down z/right z/down z/right z/node)

(def last-one (-> root-loc z/down z/right z/right))
(z/node last-one)
(-> last-one z/rightmost z/node)

;; Parts of a tree
(def b (-> root-loc z/down z/right z/down z/right))
(z/node b)
;;=> b
(z/lefts b)
;;=> (a)
(z/rights b)
;;=> (c)

;; all subtrees from the root of the tree down to just above the current log
(z/path b)
;;=> [(1 (a b c) 2) (a b c)]

;; Changing the tree
(def loc-in-new-tree (z/remove (z/up b)))
(z/root loc-in-new-tree)
;;=> (1 2)
;; and the new location is at 1:
(z/node loc-in-new-tree)
;;=> 1

;; other functions for editing the tree
(z/root (z/insert-left b :left))
;;=> (1 (a :left b c) 2)
(z/root (z/insert-right b :right))
;;=> (1 (a b :right c) 2)
(z/root (z/replace b :b-new))
;;=> (1 (a :b-new c) 2)
(z/root (z/edit b (fn [node & args] (cons node (seq args))) :b1 :b2 :b3))
;;=> (1 (a (b :b1 :b2 :b3) c) 2)
;; notice you cannot insert child for leaf node - therefore we use `(z/up b)`
(z/root (z/insert-child (z/up b) :b's-parent-child))
;;=> (1 (:b's-parent-child a b c) 2)
(z/root (z/append-child (z/up b) :b's-parent-child))
;;=> (1 (a b c :b's-parent-child) 2)

;; Whole tree editing
;; notice that z/end? and z/next works in a depth-first manner
(defn print-tree [original]
  (loop [loc (z/seq-zip (seq original))]
    (if (z/end? loc)
      (z/root loc)
      (recur (z/next
              (do (println (z/node loc))
                  loc))))))

(print-tree [1 '(a (i ii iii) c) 2])
;; prints:
;; (1 (a (i ii iii) c) 2)
;; 1
;; (a (i ii iii) c)
;; a
;; (i ii iii)
;; i
;; ii
;; iii
;; c
;; 2

(comment 
  ;; general pattern
  (loop [loc (z/seq-zip original-tree)]
    (if (z/end?> loc)
      (z/root loc)
      (recur (z/next
              (cond (subtree-to-change? loc)
                    (modify-subtree loc)
                    …
                    :else loc))))))


;;; Most embedded list in a Clojure form: https://stackoverflow.com/questions/47996342/most-embedded-list-in-a-clojure-form
(defn replace-deepest [data replacer]
  (->> data
       z/seq-zip
       (iterate z/next)
       (take-while (complement z/end?))
       (apply max-key #(if (seq? (z/node %))
                         (count (z/path %))
                         -1))
       (#(z/replace % replacer))
       z/root))

(replace-deepest '(+ [* a b] (* c d) (* e (/ f g))) 'foo)
;;=> (+ [* a b] (* c d) (* e foo))
