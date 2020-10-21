(ns clojure-experiments.purely-functional.puzzles.0399-digit-search
  "https://purelyfunctional.tv/issues/purelyfunctional-tv-newsletter-399-is-more-layers-better/")


;;; Write a function that takes a sequence of integers.
;;; You’re trying to get all 10 digits by looking through the numbers sequentially.
;;; When you have found one instance of every decimal digit,
;;; return whatever number you were on when you found the last one.
;;; If you get to the end of the sequence without finding all the digits
;;; (for instance, maybe there was no 9), then just return nil.


(defn digit-search [numbers]
  (let [number (reduce
                (fn [digits number]
                  (let [new-digits (into digits (str number))]
                    (if (= 10 (count new-digits))
                      (reduced number)
                      new-digits)))
                #{}
                numbers)]
    (when (number? number) number)))

(digit-search   [5175 4538 2926 5057 6401 4376 2280 6137]) ;=> 5057
;; digits found: 517- 4-38 29-6 -0

(digit-search   [5719 7218 3989 8161 2676 3847 6896 3370]) ;=> 3370
;; digits found: 5719 -2-8 3--- --6- ---- --4- ---- ---0

(digit-search   [4883 3876 7769 9846 9546 9634 9696 2832]) ;=> nil
;; digits found: 48-3 --76 ---9 ---- -5-- ---- ---- 2---
;; 0 and 1 are missing
