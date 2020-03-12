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
