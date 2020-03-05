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
