(ns clojure-experiments.purely-functional.puzzles.0327-lynch-bell
  "Puzzle: https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-327-tip-always-be-decomplecting/
  Solution: https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-328-tip-don-t-use-def-inside-a-defn/
  Find the largest Lynch Bell number, that is number consisting of unique digits
  and divisible by all such digits/numbers.
  E.g. 135 is divisible by all 1, 3, 5.
  Note: The number can't contain 0 since division by zero leads to the arithmetic exception.")

;;; The largest one can be 123456789

(defn- digits [n]
  "Returns all digits of given number"
  (assert (pos? n) "Can only work with positive numbers.")
  (loop [n n
         digits '()]
    (if (> n 9)
      (recur (quot n 10) (conj digits (rem n 10)))
      (conj digits n))))

#_(digits 135)
;; => (1 3 5)

(defn lynch-bell? [n]
  (let [digs (digits n)
        rems (map #(rem n %) digs)]
    (every? zero? rems)))

(lynch-bell? 135)
(lynch-bell? 137)

