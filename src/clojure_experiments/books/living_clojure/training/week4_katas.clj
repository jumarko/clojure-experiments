(ns clojure-experiments.books.living-clojure.training.week4-katas
  "See https://github.com/gigasquid/wonderland-clojure-katas
  - https://github.com/gigasquid/wonderland-clojure-katas/tree/master/alphabet-cipher
  - https://github.com/gigasquid/wonderland-clojure-katas/tree/master/fox-goose-bag-of-corn"
  (:require
   [clojure.set]
   [clojure.test :refer [deftest is testing]]))

;;; Day 1: Alphabet cipher: https://github.com/gigasquid/wonderland-clojure-katas/tree/master/alphabet-cipher
(def substitution-chart )
(defn encode-char [k m]
  (let [[k0 m0] (mapv #(- (int %) (int \a))
                      [k m])]
    (char (+ (mod (+ (int k0) (int m0))
                  26)
             (int \a)))))
(encode-char \s \m)
;; => \e
(defn encode [keyword plain-message]
  (apply str
         (mapv encode-char (apply concat (repeat keyword)) plain-message)))
(encode "scones" "meetmebythetree")
;; => "egsgqwtahuiljgs"

(defn decode-char [k m]
  (let [[k0 m0] (mapv #(- (int %) (int \a))
                      [k m])]
    ;; this is the only difference from `encode-char` -> we do `(- (int m0) (int k0))` instead of `(+ (int k0) (int m0))` 
    (char (+ (mod (- (int m0) (int k0))
                  26)
             (int \a)))))

(defn decode [keyword encoded-message]
  (apply str
         (mapv decode-char (apply concat (repeat keyword)) encoded-message))
  )

(decode "scones" "egsgqwtahuiljgs")
;; => "meetmebythetree"


;; REFACTOR: get rid of the duplication between `decode-char` and `encode-char` 

(defn coding-char [k m op]
  (let [[k0 m0] (mapv #(- (int %) (int \a))
                      [k m])]
    (char (+ (mod (op (int m0) (int k0))
                  26)
             (int \a)))))

(defn encode-char [k m]
  (coding-char k m +))
(encode "scones" "meetmebythetree")
;; => "egsgqwtahuiljgs"

(defn decode-char [k m]
  (coding-char k m -))
(decode "scones" "egsgqwtahuiljgs")
;; => "meetmebythetree"



;;; Day 5: Fox, Goose, and Bag of Corn
;;; https://github.com/gigasquid/wonderland-clojure-katas/tree/master/fox-goose-bag-of-corn

(defn river-crossing-plan []
  [[[:fox :goose :corn :you] ; everybody on one side of the river
    [:boat] ; boat is empty
    [] ; the other side of the river is empty
    ]
   [[:fox :corn]
    [:boat :you :goose]
    []]

   [[:fox :corn]
    [:boat]
    [:goose :you]]

   [[:fox :corn]
    [:boat :you]
    [:goose]]

   [[:fox :corn :you]
    [:boat]
    [:goose]]

   [[:fox]
    [:boat :corn :you]
    [:goose]]

   [[:fox]
    [:boat]
    [:corn :you :goose]]

   [[:fox]
    ;; take the goose back
    [:boat :you :goose]
    [:corn]]

   [[:fox :you :goose]
    [:boat]
    [:corn]]

   [[:goose]
    [:boat :you :fox]
    [:corn]]

   [[:goose]
    [:boat]
    [:corn :you :fox]]

   [[:goose]
    [:boat :you]
    [:corn :fox]]

   [[:goose :you]
    [:boat]
    [:corn :fox]]

   [[]
    [:boat :you :goose]
    [:corn :fox]]

   ;; Finally!
   [[]
    [:boat]
    [:corn :fox :you :goose]]
   ])

;;; Tests: https://github.com/gigasquid/wonderland-clojure-katas/blob/master/fox-goose-bag-of-corn/test/fox_goose_bag_of_corn/puzzle_test.clj
(defn validate-move [step1 step2]
  (testing "only you and another thing can move"
    (let [diff1 (clojure.set/difference step1 step2)
          diff2 (clojure.set/difference step2 step1)
          diffs (concat diff1 diff2)
          diff-num (count diffs)]
      (is (> 3 diff-num))
      (when (pos? diff-num)
        (is (contains? (set diffs) :you)))
      step2)))

(deftest test-river-crossing-plan
  (let [crossing-plan (map (partial map set) (river-crossing-plan))]
    (testing "you begin with the fox, goose and corn on one side of the river"
      (is (= [#{:you :fox :goose :corn} #{:boat} #{}]
             (first crossing-plan))))
    (testing "you end with the fox, goose and corn on one side of the river"
      (is (= [#{} #{:boat} #{:you :fox :goose :corn}]
             (last crossing-plan))))
    (testing "things are safe"
      (let [left-bank (map first crossing-plan)
            right-bank (map last crossing-plan)]
        (testing "the fox and the goose should never be left alone together"
          (is (empty?
               (filter #(= % #{:fox :goose}) (concat left-bank right-bank)))))
        (testing "the goose and the corn should never be left alone together"
          (is (empty?
               (filter #(= % #{:goose :corn}) (concat left-bank right-bank)))))))
    (testing "The boat can carry only you plus one other"
      (let [boat-positions (map second crossing-plan)]
        (is (empty?
             (filter #(> (count %) 3) boat-positions)))))
    (testing "moves are valid"
      (let [left-moves (map first crossing-plan)
            middle-moves (map second crossing-plan)
            right-moves (map last crossing-plan)]
        (reduce validate-move left-moves)
        (reduce validate-move middle-moves)
        (reduce validate-move right-moves )))))



(map first (river-crossing-plan))

(map second (river-crossing-plan))

(map last (river-crossing-plan))
