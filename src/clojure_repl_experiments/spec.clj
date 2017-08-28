(ns clojure-repl-experiments.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest]))

;; copied from clojure.spec
(alias 'stc 'clojure.spec.test.check)


;;; Examples from Alex Miller's talk:
;;; Clojure Spec: Expressing Data Constraints Without Types: https://www.infoq.com/presentations/clojure-spec

(def user-data
  {:user/due-date #inst "2017-04-19T15:47:12.000-00:00"
   :user/purcharser "Acme Moving"
   :user/line-items
   [{:user/item-id 30790 :user/quantity 100}
    {:user/item-id 1375 :user/quantity 2}]})

;; let's create spec for our domain data...
(s/def ::item-id pos-int?)
(s/def ::quantity nat-int?)
(s/def ::line-item (s/keys :req [::item-id ::quantity]))

(s/def ::purcharser string?)
(s/def ::line-items (s/coll-of ::line-item :min-count 1 :gen-max 2))
(s/def ::due-date (s/inst-in #inst "2000" #inst "2020"))
(s/def ::order (s/keys :req [::purcharser ::line-items] :opt [::due-date]))

(gen/generate (s/gen ::order))


;; polygon example
(s/def ::point (s/cat :x int? :y int?))
(s/valid? ::point [1 2])

;; ranged-rand
(defn ranged-rand
  "Returns a random int such that start <= rand < end."
  [start end]
  (+ start (long (rand (- end start)))))

;; and write spec for it
(s/fdef ranged-rand
        :args (s/and (s/cat :start int? :end int?)
                     #(< (:start %) (:end %)))
        :ret int?
        :fn (s/and #(<= (-> % :args :start) (:ret %))
                   #(< (:ret %) (-> % :args :end))))
;; now we can exercise the function to see if it works as expected
(s/exercise-fn `ranged-rand)

;; we can also check function by running generative tests
;; notice that we changed the num-tests from default 1000 to 10,000
;; to increase the probability that we find an error
#_(stest/check `ranged-rand {::stc/opts {:num-tests 10000}})

;;=> this will uncover following problematic "integer overflow"
#_(ranged-rand -3800760469044029490 5422611567810746318)

;; following is improved definition which doesn't suffer from overflow
(defn ranged-rand
  "Returns a random int such that start <= rand < end."
  [start end]
  (long (+ start
           (bigint (rand (-' end start))))))
(ranged-rand -3800760469044029490 5422611567810746318)
(rand (-'  5422611567810746318 -3800760469044029490))

;; run check with increases number of tests - default is 1000
(stest/check `ranged-rand {::stc/opts {:num-tests 10000}})

(stest/abbrev-result (first (stest/check `ranged-rand)))


;;; Some examples from Spec guide: https://clojure.org/guides/spec
(s/def ::big-even (s/and int? even? #(> % 1000)))
(s/valid? ::big-even 1001)
(s/valid? ::big-even 1002)

(s/def ::name-or-id (s/or :name string?
                          :id int?))
(s/conform ::name-or-id "abc")
(s/conform ::name-or-id 1000)
(s/conform ::name-or-id :foo)

;; nilable
(s/valid? (s/nilable string?) nil)

;; `explain` - check also `explain-str`, and `explain-data`
(s/explain ::name-or-id :foo)

;; Entity maps
(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def ::email (s/and string? #(re-matches email-regex %)))
(s/def ::acctid int?)
(s/def ::first-name string?)
(s/def ::last-name string?)
(s/def ::person (s/keys :req [::first-name ::last-name ::email]
                        :opt [::phone]))
(s/explain ::person
           {::first-name "Elon"
            ::last-name "Musk"
            ::email "n/a"})

;; unqualified keys
(s/def :unq/person
  (s/keys :req-un [::first-name ::last-name ::email]
          :opt-un [::phone]))

(s/conform :unq/person
           {:first-name "Elon"
            :last-name "Musk"
            :email "elon@example.com"})
;; can also be used to check records
(defrecord Person [first-name last-name email phone])
(s/conform :unq/person
           (->Person "Elon" "Musk" "elon@example.com" nil))

;; merging multiple keys specs into one
(s/def :animal/kind string?)
(s/def :animal/says string?)
(s/def :animal/common (s/keys :req [:animal/kind :animal/says]))
(s/def :dog/tail? boolean?)
(s/def :dog/breed string?)
(s/def :animal/dog (s/merge :animal/common
                            (s/keys :req [:dog/tail? :dog/breed])))
(s/valid? :animal/dog
          {:animal/kind "dog"
           :animal/says "woof"
           :dog/tail? true
           :dog/breed "retriever"})


;; coll-of, tuple, map-of
(s/conform (s/coll-of keyword?) [:a :b :c])

;; `cat`
(s/def ::config (s/*
                 (s/cat :prop string?
                        :val  (s/alt :s string? :b boolean?))))
(s/conform ::config ["-server" "foo" "-verbose" true "-user" "joe"])
(gen/generate (s/gen ::config))


;; higher-order functions
(defn adder [x] (partial + x))
(s/fdef adder
        :args (s/cat :x number?)
        :ret (s/fspec :args (s/cat :y number?)
                      :ret number?)
        :fn #(= (-> % :args :x) ((:ret %) 0)))


;;; fizzbuzz with Clojure.spec by Stuart Halloway
;;; https://gist.github.com/stuarthalloway/01a2b7233b1285a8b43dfc206ba0036e
;; try this form-by-form at a REPL
(require '[clojure.spec.alpha :as s])

;; create an inline DSL to describe the FizzBuzz world
(defmacro divides-by
  [nm n]
  `(s/def ~nm (s/and pos-int? #(zero? (mod % ~n)))))

;; specify FizzBuzz 
(divides-by ::fizz 3)
(divides-by ::buzz 5)
(divides-by ::fizzbuzz 15)

;; try it out
(s/conform ::fizz 4)
(s/exercise ::fizz)

;; specify all of FizzBuzz so it can generate
(s/def ::fizzbuzznum
  (s/and (s/or :name (s/and (s/or :FizzBuzz ::fizzbuzz :Buzz ::buzz :Fizz ::fizz)
                            (s/conformer (comp name first)))
               :num pos-int?)
         (s/conformer second)))

;; take it for a stroll...
(s/exercise ::fizzbuzznum 25)

;; oops, almost forgot to solve the problem...
(def fizzbuzz-transducer (map (partial s/conform ::fizzbuzznum)))

;; ...lazily generate FizzBuzz for all the integers
(set! *print-length* 100)
(eduction fizzbuzz-transducer (range 1 101))

;; load a unit test library
(require '[clojure.edn :as edn])

;; comprehensive test suite
(assert (= (eduction fizzbuzz-transducer (range 1 101))
           (-> "https://gist.githubusercontent.com/stuarthalloway/ad1041d65b84626e5b8009e51ad69260/raw" slurp edn/read-string)))

;; Since FizzBuzz is used for interviews, here is an interview question:
;; Given the implementation above, why is it desirable that this number be small?
(Math/pow (/ 14.0 15) 100)
