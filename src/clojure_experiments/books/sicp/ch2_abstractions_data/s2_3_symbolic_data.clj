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
    (cons '() elems)
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
