(ns clojure-experiments.purely-functional.property-based-testing.14-generate-output
  "https://purelyfunctional.tv/lesson/strategies-for-properties-generate-the-output/.
  Counter-intuitive strategy for properties - generating output may be sometimes easy
  and just convert it to the fn input."
  (:require [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [clojure.string :as string]
            ))


;;; Typical way to test a function
;;; 1. Generate the input
;;; 2. Run the function
;;; 3. Check the output


;;; But how would you test `read-string` fn (basically, it's just a parser)
;;; How we could create a generator that generates the stuff that this can parse
;;; (you'd basically need to re-implement the parser)
;;; But it's quite easy to generate random Clojure data (which is also the expected output)
;;; 1. Generate the output
;;; 2. Convert the output to an input (use `pr-str`)
;;; 3. Run the function
;;; 4. Compare the output to generated output (step 1)

;; we're a bit surprised that this works for the first time:
(defspec test-read-string 100
  (prop/for-all 
   ;; TODO: will `gen/any-printable-equatable` work (may generate things that aren't parseable?)
   [output gen/any-printable-equatable]
   (let [input (pr-str output)]
     (= (clojure.edn/read-string input) output))))

;; just to be sure sample the gen
(comment
  (gen/sample gen/any-printable-equatable)
;; => ({}
;;     (0)
;;     {}
;;     #{#uuid "1a24704b-ca93-4545-b9f8-a0c98aaba001"}
;;     #{}
;;     #{}
;;     {}
;;     ({})
;;     -219N
;;     [Ts4/J** :*/C :*c3])  ;;
  )


;;; Another example:
(def gen-char-no-newlines (gen/such-that #(not= \newline %) gen/char-ascii))
(def gen-str-no-newlines (gen/fmap string/join (gen/vector gen-char-no-newlines)))

;; sample our generators
(comment
  (gen/sample gen-char-no-newlines)
  ;; => (\V \C \3 \A \x \^ \v \] \v \7)

  (gen/sample gen-str-no-newlines) 
  ;; => ("" "<" "8" "" "8tE" "U\"" "^+A4" "1F:k" "wYv" "3*vLl=C|");; => 
  
  )

(defn lines [s]
  )

;; let's make a test now
(defspec test-lines 100
  (prop/for-all 
   [output (gen/vector gen-str-no-newlines)]
   ;; here the usage of `string/join` is interesting
   (let [input (string/join "\n" output)]
     (= output (lines input)))))

;; now let's our first try on lines implementation
(defn lines [s]
  (string/split s #"\n")
  )
;;=> we got a failure where output and input are empty vectors
(string/join "\n" [])
;; => ""
(lines "")
;; => [""]

;; Let's update our generator to generate non-empty strings:
(defspec test-lines 100
  (prop/for-all 
   [output (gen/not-empty (gen/vector gen-str-no-newlines))]
   ;; here the usage of `string/join` is interesting
   (let [input (string/join "\n" output)]
     (= output (lines input)))))
;;=> still failing on ["" ""]
(string/join "\n" ["" ""])
;; => "\n"
;; the same 'problem' exists when we use built-in `string/split-lines` too!
(defn lines [s]
  (string/split-lines s)
  )

;; Now we need to start thinking how do we want to deal with empty strings ... :)

;; let's try generator without whitespace
(def gen-char-no-whitespace (gen/such-that #(not (Character/isWhitespace  %)) gen/char-ascii))
(def gen-str-no-whitespace (gen/fmap string/join (gen/vector gen-char-no-whitespace)))

(defn words [s]
  (string/split s #"\s+"))

(defspec test-words 100
  (prop/for-all 
   [output (gen/not-empty (gen/vector gen-str-no-newlines))]
   ;; here the usage of `string/join` is interesting
   (let [input (string/join " " output)]
     (= output (lines input)))))
;;=> again a failure on the same output:
;;                :smallest [["" ""]]},

;; As an exercise, try to implement `words` and `lines`
