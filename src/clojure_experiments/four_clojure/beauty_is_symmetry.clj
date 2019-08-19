(ns four-clojure.beauty-is-symmetry)

;;; http://www.4clojure.com/problem/96
;;; Let us defined a binary tree as "symmetric" if the left half of the tree is the mirror image of the right half of the tree.
;;; Write a predicate to determine whether or not a given binary tree is symmetric
;;;
;;; Notice that other poeple often use coll? predicate to check whether supplied argument is collection


(defn mirror-tree [[root-val left-child right-child :as tree]]
  (if (nil? tree)
    nil
    (list root-val (mirror-tree right-child) (mirror-tree left-child))))

(defn symmetric-tree? [[root-val left-child right-child :as tree]]
  (= left-child (mirror-tree right-child)))

;; following is the version of former two functions using single anonymous function
(fn symmetric-tree? [[root-val left-child right-child :as tree]]
  (letfn [(mirror-tree [[val left right :as t]]
            (if (nil? t)
              nil
              (list val (mirror-tree right) (mirror-tree left))))]
    (= left-child (mirror-tree right-child))))



;;; my tests
(true? (symmetric-tree? nil))
(true? (symmetric-tree? '(:a nil nil)))

;;; 4clojure tests
(= (symmetric-tree? '(:a (:b nil nil) (:b nil nil)))
   true)

(= (symmetric-tree? '(:a (:b nil nil) nil))
   false)

(= (symmetric-tree? '(:a (:b nil nil) (:c nil nil)))
   false)

(= (symmetric-tree? [1 [2 nil [3 [4 [5 nil nil] [6 nil nil]] nil]]
                       [2 [3 nil [4 [6 nil nil] [5 nil nil]]] nil]])
   true)

(= (symmetric-tree? [1 [2 nil [3 [4 [5 nil nil] [6 nil nil]] nil]]
                       [2 [3 nil [4 [5 nil nil] [6 nil nil]]] nil]])
   false)

(= (symmetric-tree? [1 [2 nil [3 [4 [5 nil nil] [6 nil nil]] nil]]
                       [2 [3 nil [4 [6 nil nil] nil]] nil]])
   false)
