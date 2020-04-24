(ns clojure-experiments.books.sicp.ch2-abstractions-data.s2-3-symbolic-data
  "Starting on page. 142.")

;;; p. 144 - memq function which returns false or sublist beginning with the first occurence of `item`
(defn memq
  [item x]
  (cond
    ;; note: this might not be true if the list x does contain a nil value
    (nil? x) false
    (= item (first x)) x
    :else (memq item (next x))))

(memq 'apple '(pear banana prune))
;; => false

(memq 'apple '(x (apple sauce) y apple pear))
;; => (apple pear)


;;; Ex. 2.53 (p. 144)
;;; What would the interpreter print in response to evaluating each of the following expressions?

(list 'a 'b 'c)
;; => (a b c)

(list (list 'george))
;; => ((george))

;; using `next` instead of `cdr`
(next '((x1 x2) (y1 y2)))
;; => ((y1 y2))

;; using `fnext` instead of `cadr`
(fnext '((x1 x2) (y1 y2)))
;; => (y1 y2)

;; Clojure doesn't have `pair?` but this is false in Scheme because it's essential (pair? a)
#_(pair? (car '(a short list)))

(memq 'red '((red shoes) (blue socks)))
;; => false

(memq 'red '(red shoes blue socks))
;; => (red shoes blue socks)


;;; Ex. 2.54 (p. 145)
;;; implement equal? procedure for lists using eq? which works on symbols
;;; Note: Clojure's `=` already works properly for lists
;;; My simplified implemetation probably works only because `=` already works;
;;; see http://community.schemewiki.org/?sicp-ex-2.54
(defn equal? [x y]
  (cond
    (and (nil? x) (nil? y)) true
    (= (first x) (first y)) (equal? (next x) (next y))
    :else false))

(equal? '(this is a list) '(this is a list))
;; => true

(equal? '(this is a list) '(this (is a) list))
;; => false

(equal? '(this (is a) list) '(this (is a) list))
;; => true

(equal? () ())
;; => true


;;; Ex. 2.55 (p. 145)
;;; Explain why this expression prints quote
(first ''abracadabra)
;; => quote

'abracadabra
;; => abracadabra

''abracadabra
;; => (quote abracadabra)

;; Note:
''''abracadabra
;; => (quote (quote (quote abracadabra)))


;;; 2.3.2 Symbolic differentation

;; Wishful thinking - let's start by pretending we already have following procedures
;; for checking whether something is sum, product, constant, or a variable;
;; for extracting parts of an expression;
;; for constructing epxressions from parts:
(defn variable? [e])
(defn same-variable? [v1 v2])

(defn sum? [e])
(defn addend [e])
(defn augend [e])
(defn make-sum [a1 a2])

(defn product? [e])
(defn multiplier [e])
(defn multiplicand [e])
(defn make-product [m1 m2])

;; just by using those and the primitive predicate `number?` we can express differentation rules
;; using the following procedure
(defn deriv [expr var]
  (cond
    (number? expr)
    0

    (variable? expr)
    (if (same-variable? expr var) 1 0)

    (sum? expr)
    (make-sum (deriv (addend expr) var)
              (deriv (augend expr) var))

    (product? expr)
    (make-sum (make-product (multiplier expr)
                            (deriv (multiplicand expr) var))
              (make-product (multiplicand expr)
                            (deriv (multiplier expr) var)))

    :else
    (throw (ex-info "Unknown expression"
                    {:expr expr
                     :var var}))))


;; Now we shall define our data representation with prefix notation and lists
(defn variable? [e]
  (symbol? e))
(defn same-variable? [v1 v2]
  ;; providing that variables are symbols (which is required by `variable?`) the `=` does the proper job
  (and (variable? v1) (variable? v2) (= v1 v2)))

(defn sum? [e]
  (and (list? e) (= '+ (first e))))
(defn addend [e]
  (second e))
(defn augend [e]
  (nth e 2))
(defn make-sum [a1 a2]
  (list '+ a1 a2))

(defn product? [e]
  (and (list? e) (= '* (first e))))
(defn multiplier [e]
  (second e))
(defn multiplicand [e]
  (nth e 2))
(defn make-product [m1 m2]
  (list '* m1 m2))


;; Now try it!
(deriv '(+ x 3) 'x)
;; => (+ 1 0)

(deriv '(* x y) 'x)
;; => (+ (* x 0) (* y 1))

(deriv '(* (* x y) (+ x 3)) 'x)
;; => (+ (* (* x y) (+ 1 0)) (* (+ x 3) (+ (* x 0) (* y 1))))


;; So far answers are correct but "unsimplified"
;; E.g. (+ (* x 0) (* y 1)) is just y

;; To simplify, we only need to change `make-sum` and `make-product` to detect when numbers are used
;; and simplify if possible
(defn make-sum [a1 a2]
  (cond
    (and (number? a1) (zero? a1))
    a2

    (and (number? a2) (zero? a2))
    a1

    (and (number? a1) (number? a2))
    (+ a1 a2)

    :else
    (list '+ a1 a2)))

(defn make-product [m1 m2]
  (cond
    (or (and (number? m1) (zero? m1))
        (and (number? m2) (zero? m2)))
    0

    (= m1 1)
    m2

    (= m2 1)
    m1

    (and (number? m1) (number? m2))
    (* m1 m2)

    :else
    (list '* m1 m2)))

;; Now check if simplification works
(deriv '(+ x 3) 'x)
;; => 1

(deriv '(* x y) 'x)
;; => y

(deriv '(* (* x y) (+ x 3)) 'x)
;; => (+ (* x y) (* (+ x 3) y))


;; Ex. 2.56 (p. 150)
;; Solution: http://community.schemewiki.org/?sicp-ex-2.56
;; 
;; Extend the basic differentiator by implementing this differentiation rule:
;; - d(u^n) / dx = n*u^(n-1) * (du/dx)
;; define procedures:
;; - exponentiation?, exponent, base, make-exponentiation
;; update `deriv` to use the new rule
;; use `**` as a symbol for exponentiation

(defn exponentiation? [e]
  (and (list? e) (= '** (first e))))

(def base second)
(defn exponent [expr] (nth expr 2))
(defn make-exponentiation [b e]
  (cond
    (and (number? e) (zero? e))
    1

    (and (number? e) (= 1 e))
    b
    ;; (or (and (number? m1) (zero? m1))
    ;;     (and (number? m2) (zero? m2)))
    ;; 0

    ;; (= m1 1)
    ;; m2

    ;; (= m2 1)
    ;; m1


    :else
    (list '** b e)))

(defn deriv [expr var]
  (cond
    (number? expr)
    0

    (variable? expr)
    (if (same-variable? expr var) 1 0)

    (sum? expr)
    (make-sum (deriv (addend expr) var)
              (deriv (augend expr) var))

    (product? expr)
    (make-sum (make-product (multiplier expr)
                            (deriv (multiplicand expr) var))
              (make-product (multiplicand expr)
                            (deriv (multiplier expr) var)))

    (exponentiation? expr)
    (make-product (make-product (exponent expr)
                   (make-exponentiation (base expr)
                                        (make-sum (exponent expr)
                                                  -1)))
                  (deriv (base expr)
                         var))

    :else
    (throw (ex-info "Unknown expression"
                    {:expr expr
                     :var var}))))

(assert (= (deriv '(** x 3) 'x)
           '(* 3 (** x 2))))


;; Ex. 2.57 (p. 151)
;; Expand the differentiation program to handle sums and products of arbitrary numbers of two or more terms.
;; Try to do this by changing only the representation for sums and products.
;; e.g. the last example in the book (p. 150) could be expressed as:
(deriv '(* x y (+ x 3)) 'x)
;; instead of
(deriv '(* (* x y) (+ x 3)) 'x)
;; => (+ (* x y) (* (+ x 3) y))

(deriv '(* x y (+ x 3)) 'x)
;; Currently returns: y
;; updated version returns:
;; => (+ (* x y) (* y (+ x 3)))


(defn- second-operand [make-fn e]
  (let [snd (nth e 2)]
    (if (< 3 (count e))
      (make-fn snd (second-operand make-fn (rest e)))
      snd)))

(defn augend [e]
  (second-operand make-sum e))

(defn multiplicand [e]
  (second-operand make-product e))

;; try again
(assert (= (deriv '(* x y (+ x 3)) 'x)
           '(+ (* x y) (* y (+ x 3)))))



;; Ex. 2.58 (P. 151)
;; Infix notation
;; => SKIPPED.


;;; 2.3.3 Representing sets (p. 151 - 161)
;;; We'll represent sets as unordered lists, ordered lists, and binary trees.
;;; Informally, a set is simply a collection of distinct objects.
;;; To employ "data abstraction", we define "set" by *specifying the operations that are to be used on sets".
- `union-set`, `intersection-set`, `element-of-set?`, `adjoin-set`.

;; Note: If it was me, then I would call `x` `e` and make it second argument.
(defn union-set [s1 s2])

(defn intersection-set [s1 s2])

(defn element-of-set? [x s])

(defn adjoin-set [x s])


;; Sets as unordered lists (p. 152 - 153) 

(defn element-of-set? [x s]
  (cond
    (empty? s) false
    (= x (first s)) true
    :else (element-of-set? x (rest s))))

(element-of-set? 3 '(1 3 5))
;; => true
(element-of-set? 4 '(1 3 5))
;; => false

(defn adjoin-set [x s]
  (if (element-of-set? x s)
    s
    (cons x s)))
(adjoin-set 3 '(1 3 5))
;; => (1 3 5)
(adjoin-set 4 '(1 3 5))
;; => (4 1 3 5)

(defn intersection-set
  [s1 s2]
  (cond
    (or (empty? s1) (empty? s2))
    ()

    (element-of-set? (first s1) s2)
    (cons (first s1) (intersection-set (rest s1) s2))

    :else
    (intersection-set (rest s1) s2)))
(intersection-set '(1 3 5) '(2 4 6))
;; => ()
(intersection-set '(1 3 5) '(1 4 5 6))
;; => (1 5)

;; Ex. 2.59 implement `union-set` using the "unordered list" representation
(defn union-set [s1 s2]
  (cond
    (empty? s1)
    s2

    (element-of-set? (first s1) s2)
    (union-set (rest s1) s2)

    :else (cons (first s1) (union-set (rest s1) s2))))

(union-set '(1 3 5) '(2 4 6))
;; => (1 3 5 2 4 6)
(union-set '(1 3 5) '(1 4 5 6))
;; => (3 1 4 5 6)
(union-set '(1 3 5) '())
;; => (1 3 5)



;; Ex. 2.60 (p. 153)
;; Design set procedures for list-based representation where elements can be repeated;
;; e.g. set #{1 2 3} could be represented as (2 3 2 1 3 2 2)
;; What's the efficiency of such representation and possible applications?
;;=> could be useful when we take care great deal about performance of `adjoin-set`,
;;   and perhaps `union-set`?
;; See https://wizardbook.wordpress.com/2010/12/07/exercise-2-60/

;; a) Unchanged:
(def element-of-set?2 element-of-set?)

;; b) adjoin is simpler because we can always just add a new element without worry
;; => it's much more efficient (constant instead of linear time)
(defn adjoin-set2 [x s]
  (cons x s))
(adjoin-set2 3 '(1 3 5))
;; => (3 1 3 5)
(adjoin-set2 4 '(1 3 5))
;; => (4 1 3 5)

;; c) intersection is likely remain the same
(def intersection-set2 intersection-set)
(intersection-set '(1 3 1 5 3) '(2 4 2 6))
;; => ()
(intersection-set '(1 3 1 5 3) '(1 4 1 5 4 6))
;; => (1 1 5)

;; union is simpler since I can just concatenate them
;; => but not asymptotic complexity change but may be a real difference in performance
(defn union-set [s1 s2]
  (concat s1 s2))

(union-set '(1 3 1 5 3) '(2 4 2 6))
;; => (1 3 1 5 3 2 4 2 6)
(union-set '(1 3 1 5 3) '(1 4 5 6))
;; => (1 3 1 5 3 1 4 5 6)
(union-set '(1 3 5) '())



;;; Sets - ordered list representation (p. 153)
;;; To potentially speed things up, we'll always represent set of numbers in defined order (lower to greater)
;;; e.g. (1 3 5 6 10)

;; How to change this?
;; This is in fact Ex. 2.61
;; Show that it takes on average half the steps as before (=> we don't need to check if it's element or not)
;; https://wizardbook.wordpress.com/2010/12/07/exercise-2-61/
(defn adjoin-set [x s]
  (cond
    (empty? s) '(x)
    (= x (first s)) s
    (< x (first s)) (cons x s)
    :else (cons (first s) (adjoin-set x (rest s)))))
(adjoin-set 3 '(1 3 5))
;; => (1 3 5)
(adjoin-set 4 '(1 3 5))
;; => (1 3 4 5)
;; Note that it was unordered before:
;;  (4 1 3 5)


;; This can save some cycles when doing `element-of-set?`
;; we can save a factor of 2 on average (only traversing half of the elements)
(defn element-of-set? [x s]
  (cond
    (empty? s) false
    (= x (first s)) true
    (< x (first s)) false
    :else (element-of-set? x (rest s))))

(element-of-set? 3 '(1 3 5))
;; => true
(element-of-set? 4 '(1 3 5))
;; => false


;; intersection is more interesting - we can quickly dismiss elements that are obviously not in the result
(defn intersection-set
  [s1 s2]
  (if (or (empty? s1) (empty? s2))
    ()
    (let [x1 (first s1)
          x2 (first s2)]
      (cond
        (= x1 x2) (cons x1
                        (intersection-set (rest s1) (rest s2)))
        (< x1 x2) (intersection-set (rest s1) s2)
        (< x2 x1) (intersection-set s1 (rest s2))))))
(intersection-set '(1 3 5) '(2 4 6))
;; => ()
(intersection-set '(1 3 5) '(1 4 5 6))
;; => (1 5)

;; Ex. 2.62 implement O(n) `union-set` using the "ordered list" representation
;; https://wizardbook.wordpress.com/2010/12/07/exercise-2-62/
(defn union-set [s1 s2]
  (cond
    (empty? s1) s2
    (empty? s2) s1
    :else
    (let [x1 (first s1)
          x2 (first s2)]
      (cond
        (= x1 x2) (cons x1
                        (union-set (rest s1) (rest s2)))
        (< x1 x2) (cons x1 (union-set (rest s1) s2))
        (< x2 x1) (cons x2 (union-set s1 (rest s2)))))))

(union-set '(1 3 5) '(2 4 6))
;; => (1 2 3 4 5 6)
(union-set '(1 3 5) '(1 4 5 6))
;; => (1 3 4 5 6)
(union-set '(1 3 5) '())
;; => (1 3 5)
(union-set '() '(1 3 5))
;; => (1 3 5)
(union-set '() '())
;; => ()


;;; Sets as binary trees (p. 155 - 161)
;;; We can still make our representation more efficient by representing a set as a tree;
;;; by making so we can make element-of-set? and adjoin-set to be O(log N)
;;; while keeping union-set and intersection-set O(N).
;;; We assume a _balanced_ binary tree in which all the elements to the left
;;; are smaller than all the  elements to the right.

;; We represent trees as lists by using following 4 functions.
;; Note: tree contains "nodes" and each node contains "entry", left branch and right branch.

(defn entry [[e _l _r]] e)
(defn left-branch [[_e l _r]] l)
(defn right-branch [[_e _l r]] r)
(defn empty-set [] [])
(defn make-tree [e left right]
  ;; basic invariant check
  (assert (< (or (entry left) (dec e))
             e
             (or (entry right) (inc e)))
          (format "Elements in the left branch must be < entry and entry < elements in the right branch: entry=%s, left=%s, right=%s" e left right))
  (list e left right))

;; Now we implement element-of-set? using those primitives:
(defn element-of-set? [x s]
  (let [e (entry s)]
    (cond
      (empty? s) false
      (= x e) true
      (< x e) (element-of-set? x (left-branch s))
      (> x e) (element-of-set? x (right-branch s))))) 
(def my-set (make-tree  3
                        (make-tree 1 nil nil)
                        (make-tree 5 nil nil)))
(element-of-set? 3 my-set)
;; => true
(element-of-set? 4 my-set)
;; => false
(element-of-set? 3 (empty-set))
;; => false

;; adjoin an element
(defn adjoin-set [x s]
  (cond
    (empty? s) (make-tree x () ())

    (= x (entry s)) s

    (< x (entry s)) (make-tree (entry s)
                               (adjoin-set x (left-branch s))
                               (right-branch s))
    (> x (entry s)) (make-tree (entry s)
                               (left-branch s)
                               (adjoin-set x (right-branch s)))))
;; adjoin existing element => no effect
(adjoin-set 1 my-set)
;; => (3 (1 nil nil) (5 nil nil))
;; adjoin new element

(adjoin-set 0 my-set)
;; => (3 (1 (0 () ()) nil) (5 nil nil))

;; adjoin new element - greater than root entry
(adjoin-set 4 my-set)
;; => (3 (1 nil nil) (5 (4 () ()) nil))


;; Ex. 2.63 (p. 158)
;; Compare two given procedures for converting a binary tree to a list:
;; - do they produce the same lists all the time?
;; - is their O(n) complexity same for converting a _balanced_ tree?
;; What lists do they produce for trees in Figure 2.16?

;; my example
(def my-set-2 (->> my-set
                   (adjoin-set 0)
                   (adjoin-set 10)
                   (adjoin-set 4)
                   (adjoin-set -2)))
;; Figure 2.16 trees
(def tree216-1 (->> ()
                    (adjoin-set 7)
                    (adjoin-set 3)
                    (adjoin-set 9)
                    (adjoin-set 1)
                    (adjoin-set 5)
                    (adjoin-set 11)))
(def tree216-2 (->> ()
                    (adjoin-set 3)
                    (adjoin-set 1)
                    (adjoin-set 7)
                    (adjoin-set 5)
                    (adjoin-set 9)
                    (adjoin-set 11)))
(def tree216-3 (->> ()
                    (adjoin-set 5)
                    (adjoin-set 3)
                    (adjoin-set 1)
                    (adjoin-set 9)
                    (adjoin-set 7)
                    (adjoin-set 11)))

;; Notice this procedure calls itself on both left and right branches
;; => O(N logN) complexity where N is number of nodes in the tree
;;    (concat has O(N) complexity)
(defn tree->list-1 [tree]
  (if (empty? tree)
    ()
    (concat (tree->list-1 (left-branch tree))
            (cons (entry tree)
                  (tree->list-1 (right-branch tree))))))
(tree->list-1 my-set)
;; => (1 3 5)
(tree->list-1 my-set-2)
;; => (-2 0 1 3 4 5 10)

(tree->list-1 tree216-1)
;; => (1 3 5 7 9 11)
(tree->list-1 tree216-2)
;; => (1 3 5 7 9 11)
(tree->list-1 tree216-3)
;; => (1 3 5 7 9 11)

;; Notice this procedure calls itself only on the right branch
;; => O(N) complexity where N is number of nodes in the tree!!
(defn tree->list-2 [tree]
  (letfn [(copy-to-list [tree result-list]
            (if (empty? tree)
              result-list
              (copy-to-list (left-branch tree)
                            (cons (entry tree)
                                  (copy-to-list (right-branch tree)
                                                result-list)))))]
    (copy-to-list tree ())))

(tree->list-2 my-set)
;; => (1 3 5)
(tree->list-2 my-set-2)
;; => (-2 0 1 3 4 5 10)

(tree->list-2 tree216-1)
;; => (1 3 5 7 9 11)
(tree->list-2 tree216-2)
;; => (1 3 5 7 9 11)
(tree->list-2 tree216-3)
;; => (1 3 5 7 9 11)


;; Ex. 2.64 (p. 159)
;; 
;; http://community.schemewiki.org/?sicp-ex-2.64
;; https://wizardbook.wordpress.com/2010/12/07/exercise-2-64/
;; 
;; list->tree function converts an ordered list to a balanced binary tree
;; It uses the helper function `partial-tree`.
;; Explain how it works and draw the tree produced for the list '(1 3 5 7 9 11)

;; a) My explanation of how it works
;; Given desired number n indicating the number of elements in the tree,
;; it splits it into two "halves" - left and right;
;; for each half it calls itself recursively to get the left and right branch of the final tree.
;; The root node is obtained as the first element that's not included in the left branch's tree.
;; b) Order of growth: O(n)
;; - at every recursion step it cuts down the number of elements processed by the subsequent recursive
;; calls into half, but there are also two recursion calls => O(n)
(defn- partial-tree
  "Accepts a list of at least `n` elems and integer `n` and constructs
  a balanced binary tree from the list.
  Returns a pair [constructured-tree elements-not-in-tree]."
  [elems n]
  (if (zero? n)
    [() elems]
    ;; notice how much simpler this is compared to the Scheme-based implementation
    ;; (thanks for better let form and destructuring)
    (let [left-size (quot (dec n) 2)
          [left-tree [this-entry & non-left-elems-rst]] (partial-tree elems left-size)
          right-size (- n (+ left-size 1))
          [right-tree remaining-elems] (partial-tree non-left-elems-rst right-size)]
      [(make-tree this-entry left-tree right-tree)
       remaining-elems])))

(defn list->tree [elements]
  (first (partial-tree elements (count elements))))

(list->tree '(1 3 5 7 9 11))
;;=>
;; (5 (1
;;      ()
;;      (3 () ()))
;;    (9
;;       (7 () ())
;;       (11 () ())))


;; Ex. 2.65 (p. 160)
;; Use the results of exercises 2.63 and 2.64 to give O(n) implementations
;; of `union-set` and `intersection-set`.

;; https://wizardbook.wordpress.com/2010/12/07/exercise-2-65/?unapproved=1657&moderation-hash=994fdf7556d04e34484c5928a38eac3e#comment-1657
;; http://community.schemewiki.org/?sicp-ex-2.65

;; test data:
(def my-set (make-tree  3
                        (make-tree 1 nil nil)
                        (make-tree 5 nil nil)))
(def my-set-2 (->> (make-tree  1 nil nil)
                   (adjoin-set 0)
                   (adjoin-set 10)
                   (adjoin-set 4)
                   (adjoin-set -2)))

;; O(n) union-set
;; reusing the implementation for ordered-list which is O(n)
;;  and tree->list-2 which is O(n) too
;; as a result we have 3*O(n) which is still O(n)
(defn- union-set-ordered-lists [s1 s2]
  (cond
    (empty? s1) s2
    (empty? s2) s1
    :else
    (let [x1 (first s1)
          x2 (first s2)]
      (cond
        (= x1 x2) (cons x1
                        (union-set-ordered-lists (rest s1) (rest s2)))
        (< x1 x2) (cons x1 (union-set-ordered-lists (rest s1) s2))
        (< x2 x1) (cons x2 (union-set-ordered-lists s1 (rest s2)))))))

(defn union-set [s1 s2]
  (let [s1-list (tree->list-2 s1)
        s2-list (tree->list-2 s2)]
    (list->tree (union-set-ordered-lists s1-list s2-list))))

(union-set my-set my-set-2)
;; =>
;; (3
;;   (0
;;     (-2 () ())
;;     (1 () ()))
;;   (5
;;     (4 () ())
;;     (10 () ())))
(assert (= [-2 0 1 3 4 5 10]
           (tree->list-1 (union-set my-set my-set-2))))

;; Similarly to union-set we reuse tree->list-2 which is O(n)
;; and the implementation of intersection-set for ordered lists which is also O(n)
;; we do 3 * O(n) operations => still O(n)
(defn intersection-set-ordered-lists
  [s1 s2]
  (if (or (empty? s1) (empty? s2))
    ()
    (let [x1 (first s1)
          x2 (first s2)]
      (cond
        (= x1 x2) (cons x1
                        (intersection-set-ordered-lists (rest s1) (rest s2)))
        (< x1 x2) (intersection-set-ordered-lists (rest s1) s2)
        (< x2 x1) (intersection-set-ordered-lists s1 (rest s2))))))

(defn intersection-set [s1 s2]
  (let [s1-list (tree->list-2 s1)
        s2-list (tree->list-2 s2)]
    (list->tree (intersection-set-ordered-lists s1-list s2-list))))

(intersection-set my-set my-set-2)
;; => (1 () ())
(assert (= [1]
           (tree->list-1 (intersection-set my-set my-set-2))))

;; you may have noticed that intersection-set and union-set are VERY similar
;; => extract common pattern?
(defn combine-sets-with-list-representation [ordered-lists-set-fn s1 s2]
  (let [s1-list (tree->list-2 s1)
        s2-list (tree->list-2 s2)]
    (list->tree (ordered-lists-set-fn s1-list s2-list))))
(def union-set (partial combine-sets-with-list-representation union-set-ordered-lists))
(assert (= [-2 0 1 3 4 5 10]
           (tree->list-1 (union-set my-set my-set-2))))
(def intersection-set (partial combine-sets-with-list-representation intersection-set-ordered-lists))
(assert (= [1]
           (tree->list-1 (intersection-set my-set my-set-2))))

;; Ex. 266 (p. 161) SKIPPED.
;; Implement the `lookup` procedure (p. 160) for the case weher the set of records is structures as a binary tree, ordered by the numerical values of the keys


;;; 2.3.4 Huffman encoding (p. 161 - 169)

;; Representing Huffman trees (p. 164/5)
;; Leaf 
(defn make-leaf [symbol weight]
  ;; I'd probably use `:leaf` in Clojure instead of the symbol 'leaf
  (list 'leaf symbol weight))

(defn leaf? [obj]
  (= 'leaf (first obj)))

;; Note destructing throws an exception if it's not a list/vector 
(defn symbol-leaf [[_ s _]]
  s)

(defn weight-leaf [[_ _ w]]
  w)

;; Note: here I could use real clojure sets instead of plain lists
;; but I'm following with the book...
(defn left-branch [tree]
  (first tree))

(defn right-branch [tree]
  (second tree))

;; Notice that `symbols` and `weight` are "generic"
;; -> they must do something slightly different when dealing with leaves vs trees.

(defn symbols [tree]
  (if (leaf? tree)
      (list (symbol-leaf tree))
      (nth tree 2)))

(defn weight [tree]
  (if (leaf? tree)
    (weight-leaf tree)
    (nth tree 3)))

;; tree is (left branch, right branch, symbols, total weight)
(defn make-code-tree [left right]
  (list left
        right
        (concat (symbols left) (symbols right))
        (+ (weight left) (weight right))))

;; Now we have basic representation we can implement decoding
(defn- choose-branch [bit tree]
  (cond
    (= \0 bit) (left-branch tree)
    (= \1 bit) (right-branch tree)
    :else (throw (ex-info "Unexpected bit" {:bit bit :tree tree}))))

(defn decode [bits tree]
  ;; this helper fn serves the purpose to capture the original complete tree
  ;; to use it during decoding
  (letfn [(decode-1 [bits current-branch]
            (if (empty? bits)
              ()
              (let [next-branch (choose-branch (first bits) current-branch)]
                (if (leaf? next-branch)
                  (cons (symbol-leaf next-branch)
                        (decode-1 (rest bits) tree))
                  (decode-1 (rest bits) next-branch)))))]

    (decode-1 bits tree)))

;; taken from p. 162 (at the top) 
(def message "100010100101101100011010100100000111001111")
;; for tree, see figure 2.18 on p. 163
(def hf-tree (make-code-tree
              (make-leaf 'A 8)

              ;; BCDEFGH branch
              (make-code-tree

               ;; BCD left branch
               (make-code-tree
                (make-leaf 'B 3)
                (make-code-tree
                 (make-leaf 'C 1)
                 (make-leaf 'D 1)))

               ;; EFGH right branch
               (make-code-tree
                (make-code-tree
                 (make-leaf 'E 1)
                 (make-leaf 'F 1))
                (make-code-tree
                 (make-leaf 'G 1)
                 (make-leaf 'H 1))))))
(clojure.pprint/pprint hf-tree)

(decode message hf-tree)
;; => (B A C A D A E A F A B B A A A G A H)


;; adjoin set (p. 167)
;; We're using ordered list representation - elements ordered by increasing weight
;; - this is useful to make finding the smallest item in a set and merging of sets (leaves & subtrees)
;;   more efficient
;; - we assume elements are not duplicated!
(defn adjoin-set [x s]
  (cond
    (empty? s) (list x)
    (< (weight x) (weight (first s))) (cons x s)
    :else (cons (first s) (adjoin-set x (rest s)))))
(adjoin-set (make-leaf 'A 4) [(make-leaf 'C 5)
                              (make-leaf 'B 7)
                              (make-leaf 'F 10)])
;; => ((leaf A 4) (leaf C 5) (leaf B 7) (leaf F 10))
(adjoin-set (make-leaf 'A 4) [(make-leaf 'C 3)
                              (make-leaf 'B 7)
                              (make-leaf 'F 10)])
;; => ((leaf C 3) (leaf A 4) (leaf B 7) (leaf F 10))
(adjoin-set (make-leaf 'A 4) [(make-leaf 'C 1)
                              (make-leaf 'B 2)
                              (make-leaf 'F 3)])
;; => ((leaf C 1) (leaf B 2) (leaf F 3) (leaf A 4))

;; construct initial ordered set of eaves from a list of symbol-frequency pairs
(defn make-leaf-set [[fst & rst :as pairs]]
  (if (empty? pairs)
    ()
    (let [[symbol frequency] fst]
      (adjoin-set (make-leaf symbol frequency)
                  (make-leaf-set rst)))))
(make-leaf-set [['A 4] ['B 2] ['C 1] ['D 1]])
;; => ((leaf D 1) (leaf C 1) (leaf B 2) (leaf A 4))


;; Ex. 2.67 - define encoding tree and a sample message (p. 161)
;; Note I've already done it in my sample with `(decode message hf-tree)

(def my-message "0110010101110")
;; for tree, see figure 2.18 on p. 163
(def my-hf-tree (make-code-tree
                 (make-leaf 'A 4)
                 (make-code-tree
                  (make-leaf 'B 2)
                  (make-code-tree
                   (make-leaf 'D 1)
                   (make-leaf 'C 1)))))

(decode my-message my-hf-tree)
;; => (A D A B B C A)

;; Ex. 2.68
;; Given the encode procedure implement `encode-symbol`; it should raise error if the symbol
;; isn't in the tree at all.
;; * https://wizardbook.wordpress.com/2010/12/07/exercise-2-68/
;; * http://community.schemewiki.org/?SICP-Solutions

;; this naive implemtation is enough to start...
(defn- symbol-in-tree? [symbol tree]
  (loop [[fst & rst] (symbols tree)]
    (cond
      (nil? fst) false
      (= symbol fst) true
      :else (recur rst))))

;; using interop it's much simpler (although still inefficient)
(defn- symbol-in-tree? [symbol tree]
  (.contains (symbols tree) symbol))

(defn- encode-symbol [symbol tree]
  (loop [encoded-symbol []
         current-branch tree]
    (cond
      (leaf? current-branch)
      encoded-symbol

      (symbol-in-tree? symbol (left-branch current-branch))
      (recur (conj encoded-symbol 0) (left-branch current-branch))

      (symbol-in-tree? symbol (right-branch current-branch))
      (recur (conj encoded-symbol 1) (right-branch current-branch))

      :else
      (throw (ex-info "Symbol isn't in the tree" {:symbol symbol
                                                  :tree tree})))))

;; simpler recursive solution: https://wizardbook.wordpress.com/2010/12/07/exercise-2-68/
(defn- encode-symbol [sym tree]
  (cond
    (leaf? tree)
    nil

    (symbol-in-tree? sym (left-branch tree))
    (cons 0 (encode-symbol sym (left-branch tree)))

    (symbol-in-tree? sym (right-branch tree))
    (cons 1 (encode-symbol sym (right-branch tree)))

    :else
    (throw (ex-info "Symbol isn't in the tree" {:symbol sym
                                                :tree tree}))))


(defn encode [message tree]
  (if (empty? message)
    ()
    (concat (encode-symbol (first message) tree)
            (encode (rest message) tree))))
(assert (= my-message
           (apply str (encode '(A D A B B C A) my-hf-tree))))


;; Ex. 2.69 (p. 168)
;; Finish given `generate-huffman-tree` procedure
;; by providing implementation of `successive-merge` (using `make-code-tree` under the hood)
;; http://community.schemewiki.org/?sicp-ex-2.69
; https://wizardbook.wordpress.com/2010/12/07/exercise-2-69/
(defn- successive-merge [ordered-set]
  (let [[left right & rst] ordered-set]
    (if right
      (successive-merge (adjoin-set (make-code-tree left right) rst))
      left)))

(defn generate-huffman-tree [pairs]
  (successive-merge (make-leaf-set pairs)))

;; testing data
(def my-pairs [['A 4] ['B 2] ['C 1] ['D 1]])
(def my-hf-tree (make-code-tree
                 (make-leaf 'A 4)
                 (make-code-tree
                  (make-leaf 'B 2)
                  (make-code-tree
                   (make-leaf 'D 1)
                   (make-leaf 'C 1)))))
(make-leaf-set my-pairs)
;; => ((leaf D 1) (leaf C 1) (leaf B 2) (leaf A 4))

my-hf-tree
;; => ((leaf A 4) ((leaf B 2) ((leaf D 1) (leaf C 1) (D C) 2) (B D C) 4) (A B D C) 8)

(generate-huffman-tree my-pairs)
;; => (((leaf A 4) ((leaf B 2) ((leaf D 1) (leaf C 1) (D C) 2) (B D C) 4) (A B D C) 8))

(assert (= my-hf-tree
           (generate-huffman-tree my-pairs)))

(generate-huffman-tree ())
;; => nil
