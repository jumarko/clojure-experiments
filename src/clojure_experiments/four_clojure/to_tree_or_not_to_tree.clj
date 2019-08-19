(ns four-clojure.to-tree-or-not-to-tree)

;;; Write a predicate which checks whether or not a given sequence represents a binary tree.
;;; Each node in the tree must have a value, a left child, and a right child.

(defn binary-tree? [binary-tree-candidate]
  (if (sequential? binary-tree-candidate)
    (let [[value left-child right-child] binary-tree-candidate]
      (and
       ;; need to check count because destructuring returns nil elements in case the input sequence count is different from 3
       (= 3 (count binary-tree-candidate))
       (binary-tree? left-child) (binary-tree? right-child)))
    ;; if input is not a sequence the only viable option is the nil - any other values without children means that it's not a valid binary tree
    (nil? binary-tree-candidate)
    ))

(= (binary-tree? '(:a (:b nil nil) nil))
   true)

(= (binary-tree? '(:a (:b nil nil)))
   false)

(= (binary-tree? [1 nil [2 [3 nil nil] [4 nil nil]]])
   true)

(= (binary-tree? [1 [2 nil nil] [3 nil nil] [4 nil nil]])
   false)

(= (binary-tree? [1 [2 [3 [4 nil nil] nil] nil] nil])
   true)

(= (binary-tree? [1 [2 [3 [4 false nil] nil]  nil] nil])
   false)

(= (binary-tree? '(:a nil ()))
   false)
