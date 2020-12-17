(ns advent-of-clojure.2018.02-inventory-management
  "https://adventofcode.com/2018/day/2"
  (:require [clojure.test :refer [deftest testing is]]
            [advent-of-clojure.2018.input :as io]))

;;;; Inventory management system
;;;; Year 1518: Warehouse boxes


;;; Puzzle 1: Count checksum of candidate boxes
;;; Simply scan find number of boxes with an ID containing any of letters repeating twice;
;;; then find number of boxes with an ID containing any of letters repeating three times;
;;; then multiply these two numbers together


(defn checksum [ids]
  (let [ids-freqs (map #(-> % frequencies vals set) ids) ;; e.g. '(#{1} #{1 3 2} #{1 2} #{1 3} #{1 2} #{1 2} #{3})
        count-freqs (fn [desired-freq] (count (filter #(contains? % desired-freq) ids-freqs)))
        twos-count (count-freqs 2)
        threes-count (count-freqs 3)]
    (* twos-count threes-count)))

(defn puzzle1 []
  (io/with-input "02_input.txt" checksum))

(deftest puzzle1-test
  (testing "Simple checksum"
    (is (= 12 (checksum ["abcdef" "bababc" "abbcde" "abcccd" "aabcdd" "abcdee" "ababab"]))))
  (testing "Real input"
    (is (= 4920 (puzzle1)))))


;;; Puzzle 2: 
;;; --------
(defn common-chars
  "Returns a string representing all characters that are the same and at the same position.
  Same characters at different positions don't count!"
  [w1 w2]
  (let [char-or-nil (map (fn [ch1 ch2]
                           (when (= ch1 ch2)
                             ch1))
                         w1
                         w2)]
    (->> char-or-nil
        (remove nil?)
        (apply str))))

(defn matching-ids
  "For given ID find another id that 'match it' - that means they differ in exactly one character."
  [id other-ids]
  (first (filter (fn [id2] (and (= (count id) (count id2))
                                (= (dec (count id)) (count (common-chars id id2)))))
                 other-ids
                 )))

(defn off-by-one-ids
  "In given set of ides find all ids that differ in exactly one character.
  See `common-chars`."
  [ids]
  (->> ids
       (map (fn [id]
              (let [other-ids (disj (set ids) id)]
                (matching-ids id other-ids))))
       (remove empty?)))
       

(defn common-chars-for-correct-ids
  "Finds common characters for 'matching ids in given collection.
  It's assumed that there are only two such IDs - otherwise `common=chars` call throws an exception."
  [ids]
  (->> ids
       off-by-one-ids
       (apply common-chars)))

(defn puzzle2 []
  (io/with-input "02_input.txt" common-chars-for-correct-ids))

(deftest puzzle2-test
  (testing "Simple test"
    (is (= #{"fghij" "fguij"}
           (set (off-by-one-ids ["abcde" "fghij" "klmno" "pqrst" "fguij" "axcye" "wvxyz"])))))
  (testing "Simple test - check common letters"
    (is (= "fgij"
           (apply common-chars (off-by-one-ids ["abcde" "fghij" "klmno" "pqrst" "fguij" "axcye" "wvxyz"])))
        (testing "Real input"
          (is (= "fonbwmjquwtapeyzikghtvdxl" (time (puzzle2))))))))



;;; Journal:
;;; ===============================================================================
(comment

  ;; let's start with frequencies
  (map frequencies ["abcdef" "bababc" "abbcde" "abcccd" "aabcdd" "abcdee" "ababab"])
  ;; ({\a 1, \b 1, \c 1, \d 1, \e 1, \f 1}
  ;;  {\b 3, \a 2, \c 1}
  ;;  {\a 1, \b 2, \c 1, \d 1, \e 1}
  ;;  {\a 1, \b 1, \c 3, \d 1}
  ;;  {\a 2, \b 1, \c 1, \d 2}
  ;;  {\a 1, \b 1, \c 1, \d 1, \e 2}
  ;;  {\a 3, \b 3})


  ;; filter only those with 2 or 3 frequency

  (->> ["abcdef" "bababc" "abbcde" "abcccd" "aabcdd" "abcdee" "ababab"]
       (map frequencies)
       (filter (fn [id-freqs]
                 (some (fn [[char freq]] (<= 2 freq 3))
                       id-freqs))))
  ({\b 3, \a 2, \c 1}
   {\a 1, \b 2, \c 1, \d 1, \e 1}
   {\a 1, \b 1, \c 3, \d 1}
   {\a 2, \b 1, \c 1, \d 2}
   {\a 1, \b 1, \c 1, \d 1, \e 2}
   {\a 3, \b 3})

  ;; what if I use only vals and make them sets?
  (->> ["abcdef" "bababc" "abbcde" "abcccd" "aabcdd" "abcdee" "ababab"]
       #_(map (comp frequencies vals distinct))
       (map #(-> % frequencies vals set)))
  ;; (#{1} #{1 3 2} #{1 2} #{1 3} #{1 2} #{1 2} #{3})

  ;; sum twos
  (->> ["abcdef" "bababc" "abbcde" "abcccd" "aabcdd" "abcdee" "ababab"]
       (map #(-> % frequencies vals set))
       (filter #(contains? % 2)))
  ;; and sum threes
  (->> ["abcdef" "bababc" "abbcde" "abcccd" "aabcdd" "abcdee" "ababab"]
       (map #(-> % frequencies vals set))
       (filter #(contains? % 3)))

  ;; let's put it together
  (let [ids ["abcdef" "bababc" "abbcde" "abcccd" "aabcdd" "abcdee" "ababab"]
        ids-freqs (map #(-> % frequencies vals set) ids)
        count-freqs (fn [desired-freq] (count (filter #(contains? % desired-freq) ids-freqs)))
        twos-count (count-freqs 2)
        threes-count (count-freqs 3)]
    (* twos-count

       threes-count))

  ;;; Puzzle 2
  ;; first create a skeleton + simple implementation
  (defn off-by-one-ids [ids]
    )
  (defn common-chars [w1 w2]
    (apply str (filter (set w1) w2)))
  (common-chars "fghij" "fguij")

  ;; continue to find candidates.
  (let [ids ["abcde" "fghij" "klmno" "pqrst" "fguij" "axcye" "wvxyz"]]
    (->> ids
         (map (fn [id]
                (first (filter (fn [id2] (and (= (count id) (count id2))
                                              (= (dec (count id)) (count (common-chars id id2)))))
                               (disj (set ids) id)))))
         (remove empty?)))


  )


