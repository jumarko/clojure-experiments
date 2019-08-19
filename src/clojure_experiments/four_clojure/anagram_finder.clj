(ns four-clojure.anagram-finder)

;;; http://www.4clojure.com/problem/77
;;; Write a function which finds all the anagrams in a vector of words.
;;; Word x is an anagram of word y if all the letters in x can be rearranged in a different order to form y.
;;; Your function should return a set of sets, where each sub-set is a group of words which are anagrams of each other.
;;; Each sub-set should have at least two words. Words without anagrams should not be included in the result.

(defn find-anagrams [words]
  (set (filter #(> (count %) 1)
               (map #(set %)
                    ;; words which have the same frequencies of characters are anagrams
                    (vals (group-by frequencies words))))))

;; using thread macro - more readable?
(defn find-anagrams [words]
  (->> (group-by frequencies words)
       (vals)
       (map #(set %))
       (filter #(> (count %) 1))
       (set)))


(= (find-anagrams ["meat" "mat" "team" "mate" "eat"])
   #{#{"meat" "team" "mate"}})

(= (find-anagrams ["veer" "lake" "item" "kale" "mite" "ever"])
   #{#{"veer" "ever"} #{"lake" "kale"} #{"mite" "item"}})
