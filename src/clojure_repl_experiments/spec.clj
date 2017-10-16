(ns clojure-repl-experiments.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            ;; note that almost everything should be in spec, but there are some generators that are not
            [clojure.test.check.generators :as gens]
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

#_(gen/generate (s/gen ::order))


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

(s/def ::name keyword?)
(s/valid? (s/keys :req-un [::name]) {:name "john"})

(defn f [m]
  (:name m))

(s/fdef f
        :args (s/cat :m (s/keys :req-un [::name])))

#_(stest/instrument)


;;; spec on multi-arity functions
(defn zero-or-one
  ([] (zero-or-one 1))
  ([x] (inc x)))
(s/fdef zero-or-one :args (s/? pos-int?))
#_(zero-or-one -10)
;; if you want better doc’ed conformed args, could also do:
(s/fdef zero-or-one :args (s/cat :x (s/? pos-int?)))
#_(zero-or-one -10)


;;; test.check generators
;;; great talk from Gary Frederics: https://www.youtube.com/watch?v=F4VZPxLZUdA&t=174s

;; this doesn't work without parentheses
(def great-data
  (gen/hash-map
   :boolean (gen/boolean)
   ;; note: `nat` generator is not in spec 
   :small-integers (gen/vector gens/nat)
   :large-integer  (gen/large-integer)
   :double         (gen/double)
   :color          (gen/elements [:red :green :blue])
   :uuid           (gen/uuid)
   ;; args to generators 
   :string-or-keyword (gen/tuple (gen/string) (gen/keyword))))

;; this works
(def great-data
  (gens/hash-map
   :boolean gens/boolean
   ;; note: `nat` generator is not in spec 
   :small-integers (gens/vector gens/nat)
   :large-integer  gens/large-integer
   :double         gens/double
   :color          (gens/elements [:red :green :blue])
   :uuid           gens/uuid
   ;; args to generators 
   :string-or-keyword (gens/tuple gens/string gens/keyword)))

;; generate one example
#_(gen/generate great-data)
;; generate 10 samples
#_(gen/sample great-data)

;; fmap is pretty powerful and similar to bind,
;; but bind has argument in different order:
(def gen-collection-and-element (gen/fmap (fn [coll]
              (gen/fmap (fn [x] [coll x])
                        (gen/elements coll)))
            (gen/not-empty
             (gen/vector (gen/large-integer)))))
;; this produces the generator, not the desired output
(gen/generate gen-collection-and-element)

;; => let's try with bind
(def gen-collection-and-element
  (gen/bind (gen/not-empty
             (gen/vector (gen/large-integer)))
            (fn [coll]
              (gen/fmap (fn [x] [coll x])
                        (gen/elements coll)))))
(gen/generate gen-collection-and-element)
;;=> [[-14246 -432106537 0 0 0 110689 11208554] 11208554]

;; and we can simplify this with macro `gen/let`
;; notice that locals refer to values generated by generators,
;; not generators themselves
(def gen-collection-and-element
  ;; notice that let is not in spec.alpha namespace
  (gens/let [coll (gens/not-empty
                   (gens/vector gens/large-integer))
             x (gens/elements coll)]
    [coll x]))
(gen/generate gen-collection-and-element)


(def gen-file-name (gen/such-that
                    #(not (re-find #"/" %))
                    gens/string-ascii))
(gen/generate gen-file-name)
;; try a bit harder and you'll get an error
#_(count (gen/sample gen-file-name 1000))
;;=> ExceptionInfo Couldn't satisfy such-that predicate after 10 tries.  clojure.core/ex-info (core.clj:4744)

;; fortunately most usages of such-that can be replaced with fmap
(def gen-file-name (gens/fmap
                    #(clojure.string/replace % "/" "")
                    gens/string-ascii))
(count (gens/sample gen-file-name 1000))

;; byte arrays are useful for file contents
(def gen-file-contents gens/bytes)
(gens/sample gen-file-contents)

;; Generating octal numbers for file permissions metadata
;; notice that you need to use `large-integer*` instead of `large-integer`
;; because the former one doesn't support min/max
;; Beware that `clojure.spec.gen.alpha/large-integer` lets you pass options map but ignores it
(def gen-permissions-octal
  (gens/fmap 
   #(format "%03o" %)
   (gens/large-integer* {:min 0 :max 0777})))
(gens/sample gen-permissions-octal)

;; generate date times - notice that this generates timestamps very close to 01-01-1970
;; and then dates very far to the past or future
(def gen-datetime
  (gen/fmap #(java.time.Instant/ofEpochMilli %)
            gens/large-integer))
(gens/sample gen-datetime)

;; now we can generate file metadata
(def gen-metadata
  (gens/hash-map :permissions gen-permissions-octal
                 :user-id gens/nat
                 :group-id gens/nat
                 :created-at gen-datetime
                 :modified-at gen-datetime))
(gens/sample gen-metadata)

;; generating directory structure
(defn gen-directory-of [gen-content]
  (gens/map gen-file-name
           (gens/hash-map :metadata gen-metadata
                         :content gen-content)))
(def gen-directory
  (gens/such-that map?
                 ;; recursive-gen is also missing in clojure.spec.gen.alpha
                 (gens/recursive-gen
                  gen-directory-of
                  gen-file-contents)))
(gens/generate gen-directory 10)

;; generating changes
(defn all-filepaths [directory]
  (apply concat
         (for [[name {:keys [content]}] directory]
           (if (map? content)
             (map #(str name "/" %)
                  (all-filepaths content))
             [name]))))

(all-filepaths (gens/generate gen-directory))

(defn gen-file-appension [directory]
  (let [filepaths (all-filepaths directory)]
    (assert (not-empty filepaths))
    (gens/fmap (fn [[filepath more-bytes]]
                {:filepath filepath
                 :append more-bytes})
              (gens/tuple (gens/elements filepaths)
                         gens/bytes))))

(defn gen-changes [directory]
  (gens/vector (gen-file-appension directory)))

(def gen-directory-with-changes
  (gens/let [directory gen-directory
            changes (gen-changes directory)]
    {:directory directory
     :changes changes}))

#_(gens/generate gen-directory-with-changes 3)
