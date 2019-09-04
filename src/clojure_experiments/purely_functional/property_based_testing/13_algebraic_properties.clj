(ns clojure-experiments.purely-functional.property-based-testing.13-algebraic-properties
  "https://purelyfunctional.tv/lesson/strategies-for-properties-algebraic-properties/."
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.generators :as gen]
   [clojure.spec.alpha :as s]
   [clojure-experiments.purely-functional.property-based-testing.mergesort :refer [mergesort]]
   [clojure.set :as set])
  )

;;;; Basic categories of Algebraic properties
;;;; I. Simple formulas
;;;; II. Conditional/flexible
;;;; III. Functionality (they can sometimes be part of the implementation?)

;;; I. Simple formulas - quick to check every time you write a PB test
;;; 1. inverse
;;; 2. idempotence
;;; 3. commutativity
;;; 4. associativity
;;; 5. identity
;;; 6. zero


;;; 1. inverse
;;; E.g. calling `dec` on `inc` result and checking we get the same number
;;; Classic use case are parsers.

(defspec inc-dec-inverse 100
  (prop/for-all [number gen/large-integer]
                (= number (inc (dec number)))))

;; parsers
;; following fails!
(defspec reader-inverse 100
  (prop/for-all 
   [value gen/any-printable]
   ;; if we can get pr-str working right then we can be pretty sure that read-string works correctly
   (= value (read-string (pr-str value)))))

;; => try agian
(defspec reader-inverse 100
  (prop/for-all 
   [value (gen/recursive-gen gen/vector gen/string-ascii)]
   ;; if we can get pr-str working right then we can be pretty sure that read-string works correctly
   (= value (read-string (pr-str value)))))

;;; 2. idempotence
;;; "Doing a thing once is the same as doing it twice"
;;; E.g. adding something to a set

;; adding to a set
(defspec set-conj-idempotent 100
  (prop/for-all 
   [s (gen/set gen/large-integer)
    number gen/large-integer]
   (= (conj s number)
      (-> s
          ;; adding twice
          (conj number)
          (conj number)))))



;;; 3. commutativity
(defspec +-commutative 100
  (prop/for-all 
   [n1 gen/large-integer
    n2 gen/large-integer]
   (= (+ n1 n2)
      (+ n2 n1))))



;;; 4. associativity
(defspec *-associative 100
  (prop/for-all 
   [x gen/small-integer
    y gen/small-integer
    z gen/small-integer]
   (= (* x y z)
      (* x (* y z))
      (* (* x y) z))))

(defspec concat-associative 100
  (prop/for-all 
   [l (gen/vector gen/small-integer)
    m (gen/vector gen/small-integer)
    n (gen/vector gen/small-integer)
    ]
   (= (concat l (concat m n))
      (concat (concat l m) n))))

(defspec merge-associative 100
  (let [map-gen (gen/map gen/keyword (gen/choose 0 9))]
    (prop/for-all 
     [m1 map-gen
      m2 map-gen
      m3 map-gen]
     (= (merge (merge m1 m2) m3)
        (merge m1 (merge m2 m3))))))

(comment
  (gen/sample (gen/map gen/keyword (gen/choose 0 9)))
;; => ({}
;;     {}
;;     {:_9 6, :r 9}
;;     {:+ 7, :D- 0, :n? 2}
;;     {}
;;     {:DvE 1, :+i6 7, :Aat 7, :I1 7}
;;     {}
;;     {}
;;     {:.!1_ 2, :f 5, :!2le 3}
;;     {:J?_z 4, :C! 5})  
  )


;;; 5. identity
;;; For addition it's 0, for multiplication it's 1.
;;; Basically it's a value z then when passed to f(x z) it always returns x.

(defspec concat-identity 100
  (prop/for-all
   [l (gen/vector gen/large-integer)]
   (= l
      ;; notice both left and right identities
      (concat l [])
      (concat [] l))))




;;; 6. zero

(defspec *-zero 100
  (prop/for-all 
   [x gen/large-integer]
   (= 0
      (* 0 x)
      (* x 0))))

(defn cross-product [l1 l2]
  (for [x l1 y l2]
    [x y]))

(defspec cross-product-zero 100
  (prop/for-all 
   [l (gen/vector gen/small-integer)]
   (= []
      (cross-product [] l)
      (cross-product l []))))



;;; II. Conditional/flexible
;;; You can use them conditionally/flexible

;; Let's take this as an example:
(defspec +-commutative 100
  (prop/for-all 
   [n1 gen/large-integer
    n2 gen/large-integer]
   (= (+ n1 n2)
      (+ n2 n1))))
;; When you have log format which contains timestampes then
;; you cannot direclty use the test above (which uses `=` for equality cheking)
;; But you can define your own `equiv` fn which ignores timestamps and compares the rest of the data

;; another example: merge isn't generally commutative:
;; COOL! We can use this generative test to find examples where merge isn't commutative!
(defspec merge-commutative 100
  (let [map-gen (gen/map gen/keyword (gen/choose 0 9))]
    (prop/for-all 
     [m1 map-gen
      m2 map-gen]
     (= (merge m1 m2)
        (merge m2 m1)))))
(merge {:+ 0} {:+ 1})
;; => {:+ 1}
(merge {:+ 1} {:+ 0});; => {:+ 0}

;; I can fix my merge-commutative by using just `if`
;; => IF you're sure maps have distinct keys `merge` order doesn't matter!
(defspec merge-commutative 100
  (let [map-gen (gen/map gen/keyword (gen/choose 0 9))]
    (prop/for-all
     [m1 map-gen
      m2 map-gen]
     (if (empty? (set/intersection (set (keys m1))
                                   (set (keys m2))))
       (= (merge m1 m2)
          (merge m2 m1))
       ;; for unlucky case we can use weaker check:
       (= (set (keys (merge m1 m2)))
          (set (keys (merge m2 m1))))))))

;;; Sort - examples
;;; - doesn't have an inverse operation ('reversing' sorted list can be any random permutation of list's elements)
;;; - idempotence can be done

;; sort idempotence
;; sorting already sorted list doesn't change the order
(defspec sort-idempotent 100
  (prop/for-all 
   [numbers (gen/vector gen/large-integer)]
   (= (mergesort numbers)
      (-> numbers mergesort mergesort))))

;; associativity requires two args => not applicable
;; commutativity also requires two args
;; identity and zero also requires two args
;; (BUT note that you could test internal `merge` routine instead of `mergesort` in these cases!)

;; but we can talk about 'commutativity of elements' instead of `sort` fn argument(s)
(defspec sort-commutative 100
  (prop/for-all 
   [n1 (gen/shuffle (range 100))
    n2 (gen/shuffle (range 100))]
   (= (mergesort n1) (mergesort n2))))

;; is this really an identity? or zero?
(defspec sort-identity 100
  (prop/for-all
   [numbers (gen/vector gen/large-integer)]
   (= Long/MIN_VALUE
      (-> numbers
          (conj Long/MIN_VALUE)
          mergesort
          first))))
