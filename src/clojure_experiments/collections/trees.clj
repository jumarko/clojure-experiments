(ns clojure-experiments.collections.trees
  (:import
   (clojure.lang PersistentQueue)))

;;; "Experiments with `tree-seq` core function
;;; RESOURCES:
;;; - How to implement a recursive DFS in Clojure (without using a vector / stack):
;;;   https://stackoverflow.com/a/47790682/1184752
;;; - `clojure-experiments.books.living-clojure.training.week5-katas` suggests using tree-seq as one way to implement it
;;; - Clojure For Data Science: Breadth-first and depth-first search -> https://learning.oreilly.com/library/view/clojure-for-data/9781784397180/ch08s03.html



;;;; Breadth-first search (BFS) & Depth-first search (DFS)
;;;; NOTE: `tree-seq` uses DFS.

(def tree
  {:value 1
   :children [{:value 2
               :children [{:value 5 :children []}
                          {:value 6 :children []}]}
              {:value 3 :children []}
              {:value 4 :children [{:value 7 :children []}]}]})

;;; BFS: https://codemia.io/knowledge-hub/path/stumped_with_functional_breadth-first_tree_traversal_in_clojure
(defn bfs-values [root]
  (loop [queue (conj PersistentQueue/EMPTY root)
         result []]
    (if (empty? queue)
      result
      (let [node (peek queue)
            rest-queue (pop queue)
            next-queue (apply conj rest-queue (:children node))]
        (recur next-queue (conj result (:value node)))))))

(bfs-values tree)
;; => [1 2 3 4 5 6 7]


;;; DFS

;;use `tree-seq`
(tree-seq coll? :children tree)
;; => ({:value 1,
;;      :children
;;      [{:value 2, :children [{:value 5, :children []} {:value 6, :children []}]}
;;       {:value 3, :children []}
;;       {:value 4, :children [{:value 7, :children []}]}]}
;;     {:value 2, :children [{:value 5, :children []} {:value 6, :children []}]}
;;     {:value 5, :children []}
;;     {:value 6, :children []}
;;     {:value 3, :children []}
;;     {:value 4, :children [{:value 7, :children []}]}
;;     {:value 7, :children []})

;; to get only values
(map :value (tree-seq coll? :children tree))
;; => (1 2 5 6 3 4 7)

;;; DFS - custom
;;; - NOTE: it could have 2 variants - left-most and right-most 

;; this is the right-most traversal
(defn dfs-values-right [root]
  (loop [to-process (list root)
         result []]
    (if (empty? to-process)
      result
      (let [node (peek to-process)
            restik (pop to-process)
            newik (apply conj restik (:children node))]
        (recur newik (conj result (:value node)))))))

(dfs-values-right tree)
;; => [1 4 7 3 2 6 5]

;; left-most traversal is almost the same - we just need to reverse the children when adding them
(defn dfs-values-left [root]
  (loop [to-process (list root)
         result []]
    (if (empty? to-process)
      result
      (let [node (peek to-process)
            restik (pop to-process)
            newik (apply conj restik (reverse (:children node)))]
        (recur newik (conj result (:value node)))))))
(dfs-values-left tree)
;; => [1 2 5 6 3 4 7]
