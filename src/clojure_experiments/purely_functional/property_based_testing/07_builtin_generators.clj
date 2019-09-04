(ns clojure-experiments.purely-functional.property-based-testing.07-builtin-generators
  "7th episode of Property-Based testing with test.check course
  by PurelyFunctional.tv: https://purelyfunctional.tv/lesson/a-tour-of-the-built-in-generators/

  See also Cheatsheet: https://github.com/clojure/test.check/blob/master/doc/cheatsheet.md
  "
  (:require
   [clojure.test.check.generators :as gen]))


;;; To start with...
(gen/sample gen/string-ascii)
;; => ("" "2" "S" "r:" "eWij" "qBr<" "#x@5" ":{ %dR=" "r" "+&Om1Z=")

;; notice the "size" growing ("randomness" is growing as the generator progresses)
;; always(?) the first two elements are 0 and 1 but as you progress the next elements
;; the possible "range" will increase
(gen/sample gen/nat)
;; => (0 1 1 2 4 2 3 5 6 1)

(gen/sample gen/nat 30)
;; => (0 1 2 1 3 1 3 2 5 4 3 5 2 12 7 9 4 1 4 14 2 16 17 11 1 6 18 20 19 28)

(drop 190 (gen/sample gen/nat 201))
;; => (54 145 94 95 191 187 135 71 63 156 0)
;; NOTICE the last is zero again (it cycled back to 0 after 200 elements)
;; => default size is 200?


;;;; -------
;;;; Numbers
;;;; -------

;;; Integers

;; positive numbers:
(gen/sample gen/nat)
;; => (0 0 2 2 1 1 0 6 6 9)

;; both positive and negative integers
;; note that `small-integer` is relative new and may have different name in an older version
;; bounded by generator's "size" parameter (that is how many numbers you generate also determines how big the numbers can be)
;; NOTE THAT NUMBERS STILL START SMALL (the reason is that it's often faster to debug program logic with smaller numbers)
(gen/sample gen/small-integer)
;; => (0 0 1 0 -2 -4 -1 6 8 7)
(gen/sample gen/small-integer 30)
;; => (0 0 0 2 2 0 -3 5 4 0 6 -5 -6 0 12 -1 -6 16 10 9 1 -7 -9 -2 15 21 23 20 13 6)

;; use `gen/large-integer` for generating large integers regardless of generator's size
(gen/sample gen/large-integer)
;; => (-1 -1 0 -1 -2 0 2 -1 -1 20)
(gen/sample gen/large-integer 20)
;; => (0 -1 -1 -1 -5 -8 1 0 -9 -1 0 -1 -471 -106 0 -122 -2 141 -3 51042)
;; you can define :min and :max too, but need to use `gen/large-integer*`
(gen/sample (gen/large-integer* {:min 10 :max 100000}) 20)
;; => (11 11 10 11 13 11 13 16 51 10 50 11 14 53 3348 24 1942 1785 10 12)

;; `gen/choose` for returning a uniform distribution of integers _within a range_.
;; notice that this time the numbers doesn't start small
(gen/sample (gen/choose 0 1000))
;; => (945 487 465 972 4 168 870 838 491 64)


;;; Ratios

;; basic ratios
(gen/sample gen/ratio)
;; => (0 1/2 1 1/3 1/3 -1/2 -5/4 5/2 -2/5 0)

;; if you need bigger ratios
(gen/sample gen/big-ratio)
;; => (0N 0N 40/1553 17/514 46421/665 277175366N 21/3498013 1191291/61506169 16761840995/169636154 2/247533224387)


;;; Doubles

;; includes positive/negative infinity and NANs
(gen/sample gen/double)
;; => (##-Inf -0.5 1.5 2.5 1.0 -1.5 0.5 -0.96875 2.125 0.75)

;; if you don't want pos/neg infinity and NANs => use `gen/double*`
(gen/sample (gen/double* {:min 1 :max 1000 :infinite? false :NaN? false}))
;; => (1.0 1.0 3.5 1.375 3.0 3.0 3.375 1.984375 1.3125 3.375)


;;; BigInits

;; size-bounded big integers
;; notice that this is again bounded by generator's size although it grows faster than simple ints generator
(gen/sample gen/size-bounded-bigint)
;; => (0N 13N 690N 151809N 1N 29124999N 43739763N -42248827190N 56N -52216857710712N)



;;;; ----------------------
;;;; Characters and Strings
;;;; ----------------------

;;; Characters

;; produces unprintable characters!!!
(gen/sample gen/char)

;; printable/ascii stuff
(gen/sample gen/char-ascii)
;; => (\} \9 \; \J \# \M \Z \F \I \})

(gen/sample gen/char-alphanumeric)
;; => (\1 \s \a \3 \s \O \9 \V \3 \R)
;; this older version is deprecated!
#_(gen/sample gen/char-alpha-numeric)

;; just letters (still limited to ascii)
(gen/sample gen/char-alpha) 
;; => (\i \Q \U \N \G \p \p \g \Q \w)


;;; Strings (super important)
;;; - we learn later how to generate specific strings (following a "pattern")
;;;   or non-ascii strings (but excluding unprintable chars) in later lessons

;; watch out! can return unprintable characters!
(gen/sample gen/string)

(gen/sample gen/string-ascii)
;; => ("" "" "B" "GaE" "OyX1" "J" "55" "N8erBqZ" "HZr )G+" "z#g; 6BY")

(gen/sample gen/string-alphanumeric)
;; => ("" "D" "6" "2n0" "HPZ" "GTS" "1y06ed" "D" "" "K5my")



;;;; ----------------------
;;;; Other basic types - keywords, symbols, uuids
;;;; ----------------------

;;; Keywords 
(gen/sample gen/keyword)
;; => (:- :GF :?S :O :w!v :C :SG :G :?r+_ :o+Rd)

(gen/sample gen/keyword-ns)
;; => (:L/. :Hs/OB :S/X :?/* :?/E?6 :w/*+ :m/q8 :./Y? :J-./?* :q/KX.)

;;; Symbols
(gen/sample gen/symbol)
;; => (. y *X _ Cg - x! .C S -mjT)
(gen/sample gen/symbol-ns)
;; => (n/i H/N E?/W */J ./_- */. e*/A8 *?l/M3 w!O/?Y! !1r!/e_I0)

;;; uuid
(gen/sample gen/uuid)
;; => (#uuid "87fac6f0-793d-4c28-8f95-99637f99a7ca" #uuid "e23239c5-3cd0-4f2a-9295-0750d63a6045" #uuid "7981eac2-c610-4804-8ad7-53c1d5eda9ff" #uuid "0e144ab6-05c4-4b9d-83da-7abf28ccf803" #uuid "17e2f343-2730-4b16-bd50-1ee7bf9fb95f" #uuid "5ed421e5-6dc2-4dcb-bd39-fd5eb4571808" #uuid "62383a82-6d87-4c98-a7f5-6a3896bb8f9b" #uuid "47023a1e-ae25-4228-90ad-e155124c1694" #uuid "4ba0b7bb-d3c7-492b-a3fb-9a9b1ffabdf5" #uuid "dc742d60-e1df-41a3-8dd4-f60fbdad85bb")



;;;; ----------------------
;;;; collections
;;;; ----------------------


;;; Vectors
;;; NOTICE that size get's passed to the nested generator (gen/nat)!
(gen/sample (gen/vector gen/nat))
;; => ([] [0] [] [0] [4 3 1] [3 5 1] [] [4 2 0] [4 4] [3 0 8 9 4 1])

;; fixed size vector
(gen/sample (gen/vector gen/nat 3))
;; => ([0 0 0] [0 0 0] [1 1 0] [2 1 3] [2 4 3] [4 1 1] [4 1 6] [4 0 6] [8 1 6] [1 2 6])

;; min-max sized vector
(gen/sample (gen/vector gen/nat 2 4))
;; => ([0 0] [0 0 1 0] [1 1 1 0] [3 2 1 3] [4 4 4] [5 1 5 5] [5 5] [6 7 2] [3 2] [6 8 9])

;; distinct elements
(gen/sample (gen/vector-distinct gen/nat))
;; => ([] [0] [1 0] [3 2 1] [3 4] [5 1] [] [4 2] [5 8 1 6 3 7 4] [5 9 7])


;;; Lists
;;; works pretty much the same way as vectors
(gen/sample (gen/list gen/boolean))
;; => (() () () (true false false) () (true) (true) (false false false false) (true true true) (true true true false false false false true))

;; distinct
(gen/sample (gen/list-distinct gen/small-integer))
;; => (() (-1) () (-1 2) (2 -2) (5) (-4 0 -3) (-3 -1 -4 0 2 -8) () (2 0 -7 8 9 -5 -4 6))


;;; Sets

(gen/sample (gen/set gen/nat))
;; => (#{} #{0} #{1 2} #{0 1 2} #{0 1 4} #{0 3} #{4 6 2} #{2 5} #{0 7 1 4 2 5} #{0 7 1 4 13 6 12 2 14})

;; sorted set
(gen/sample (gen/sorted-set gen/nat))
;; => (#{} #{} #{2} #{2} #{} #{2 3 4 5} #{} #{1 2 3 4 5 6 7} #{1 2 4} #{0 3 5 8 9 10})


;;; Maps
(gen/sample (gen/map gen/keyword gen/string-ascii))
;; => ({} {:_B ";"} {:C2 "1", :-. "^"} {:f "V9n", :TW "", :k+ "2"} {} {:v.p "|&", :-. "6H> P", :p.0 "g~Cpk"} {} {:Xx-d "beyX", :S:+ "M*-J", :Gr "!k", :*:Yu "fa?{fzX"} {:C "", :B "+EU#", :qzx "%B", :kR-1 "@fq", :!0Nx "\"r\\EF5"} {:aK2 "r", :T.q "", :p? "z<", :fQ_i "<4pDZnP("})

;; sorted map?
;; => DOESN'T EXIST (just sorted-set)


;;; Tuple
(gen/sample (gen/tuple gen/nat gen/string-alphanumeric gen/boolean))
;; => ([0 "" false] [1 "" false] [1 "Tq" false] [0 "I" false] [0 "Ift0" true] [4 "3JZ" true] [2 "hLM" false] [1 "xWr9h" false] [7 "p8725" false] [9 "B" true])

;; tuples can be used for generating e.g. 3 values at a time (not just for generating tuples)
;; => IT'S GREAT TO HAVE SINGLE GENERATOR THAT GENERATES ALL VALUES YOU NEED (to build more complex generators)


;;; Entity
;;; keywords are knowns, types are determined by keywords

;; Example entity
{:first-name "Eric"
 :last-name "Normand"
 :age 38}

(gen/sample (gen/hash-map :first-name gen/string-alphanumeric
                          :last-name gen/string-alphanumeric
                          :age gen/nat)
            5)
;; => ({:first-name "", :last-name "", :age 0}
;;     {:first-name "4", :last-name "", :age 0}
;;     {:first-name "", :last-name "", :age 1}
;;     {:first-name "Ln3", :last-name "7o", :age 1}
;;     {:first-name "07", :last-name "5kzD", :age 2})


;;; not empty collections

;; compare
(gen/sample (gen/vector gen/boolean))
;; => ([] [] [false true] [true] [] [true] [true false true false true true] [false] [true false true true] [true false false true])

;; TO
(gen/sample (gen/not-empty (gen/vector gen/boolean)))
;; => ([true] [false] [false] [false true false true] [false true] [true true false true] [false false false false true true] [false] [true true true false true true true false] [false false false false])

;; gen/not-empty works for strings too
(gen/sample (gen/not-empty gen/string-alphanumeric))
;; => ("j3" "e" "iV" "kD" "82" "e4m" "t2I4" "x8x" "6t5y" "qbBbi0X")


;;; Nested collections

(gen/sample (gen/vector (gen/vector gen/boolean))
            5)
;; => ([]
;;     [[]]
;;     [[true]]
;;     []
;;     [[] [true false false false] [true]])



;;; Random selection

;; Uniformly randomly selecting from given elements
(gen/sample (gen/elements [1 2 3]))
;; => (1 2 3 3 1 3 1 2 3 1)

;; if you only have one value
(gen/sample (gen/return 1))
;; => (1 1 1 1 1 1 1 1 1 1)

(gen/sample (gen/shuffle [1 2 3 4]))
;; => ([4 1 3 2] [2 1 3 4] [1 4 3 2] [1 3 4 2] [3 2 4 1] [1 2 4 3] [1 2 3 4] [4 3 2 1] [3 4 1 2] [3 2 4 1])

;; one-of is COOL: it let's you combine generators
(gen/sample (gen/one-of [gen/string-alphanumeric
                         gen/nat
                         ;; I wanna allow nil
                         (gen/return nil)
                         (gen/vector gen/nat)]))
;; => ([] nil [] "I" nil 1 nil "TPB" "" nil)

;; choose uniformly
(gen/sample (gen/choose 0 100))
;; => (29 71 97 55 65 29 94 12 39 62)

;; use gen/frequency if some values should be "more likely to generate"
;; (e.g. nils in our example shouldn't be as common as natural numbers)
(gen/sample (gen/frequency [[10 gen/nat]
                            [1 (gen/return nil)]])
            30)
;; => (0 0 1 3 1 1 0 2 1 7 10 10 5 9 8 9 3 11 3 6 nil 3 20 10 12 nil 5 4 17 18)


;;; Recursive data structures (like trees)
(drop 45
      (gen/sample (gen/recursive-gen
                   ;; 'container'
                   gen/vector
                   ;; base type
                   gen/boolean)
                  50))
;; => ([[true true] false []]
;;     [true [] [[false true] [false false false true]]]
;;     [[true]
;;      [true true false false false]
;;      [false false true true]
;;      [true false true false]
;;      [true]
;;      []
;;      [true]]
;;     []
;;     [true true false false])



