(ns clojure-experiments.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest]
            [clojure.string :as string]
            [clojure.test.check.generators :as gens]
            [expound.alpha :as exp]
            [net.cgrand.xforms :as x]))

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


;;; Building test.check generators - great talk from Gary Frederics:
;;; https://www.youtube.com/watch?v=F4VZPxLZUdA&t=174s

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


;;;is there a way in spec to say
;;; ::x is a vector of numbers
;;; ::y is a vector of numbers
;;; ::s is a string
;;; and the three have the same length (thisxis regarding to an s/keys) 
(s/def ::text-x (s/coll-of number? :kind vector?))
(s/def ::text-y (s/coll-of number? :kind vector?))
(s/def ::text-str string?)
(s/def ::svg-text (s/keys :req [::text-x ::text-y ::text-str ]))
;; my response:
(defn- vals-same-length?
  [m]
  (->> (vals m)
       (map count)
       (apply =)))
(s/def ::svg-text-same-length (s/and (s/keys :req [::text-x ::text-y ::text-str ])
                                     vals-same-length?))
(s/valid? ::svg-text-same-length {::text-x   [1 2 3]
                                  ::text-y   [10 20 30]
                                  ::text-str "abc"})

;; generate non-empty string
(s/exercise (s/and string? (complement string/blank?)))
;; suggested by gfredericks
(gen/sample (gen/not-empty (gen/string)))

(defn post 
  [{{:strs [email password]} :form-params session :session :as req}]
  (println req)
  (println email)
  (println password))



;;; Rich Hickey on Clojure Spec - LispNYC: https://vimeo.com/195711510

;; 46:40 spec validates all the keys if they have registered spec, not just those
;; explicitly stated in s/keys!!!
(s/def ::a int?)
(s/def ::b float?)
(s/def ::c int?)
(s/def ::m (s/keys :req [::a] :opt [::b])) ; notice that `::c` is not here at all
(s/valid? ::m {::a 1 ::b 2.0}) ;=> true
(s/valid? ::m {::a 1 ::b 2.0 ::c "ahoj"}) ;=> false!!
(s/explain ::m {::a 1 ::b 2.0 ::c "ahoj"})

;; just an "alias" to an existing spec
(s/def ::z ::b)
(s/def ::person (s/cat :name string? :age pos-int?))
(def conformed-person (s/conform ::person ["Juraj Martinka" 32]))
(s/unform ::person conformed-person)


;;; Clojure apropos screencast #8 about spec: https://youtu.be/iMOHTrsxzqg?t=1853
;;; The problem is to write a spec for contact list: 
;;;   contact list has: name, mobile, email
;;;   name = required; string, no numbers
;;;   mobile = optional; string, numbers and spaces or hyphens
;;;   email = optional; string, foo@bar.co - so >= 2 letters before @, an @, >= 2 letters, dot, >= 2 letters
;;;   contact-list is an atom holding an array of contacts (maps)write and spec the add-contact function
;;; 
(def contacts (atom {}))
(defn add-contact [contact]
  (swap! contacts conj contact))
(add-contact {:name "Josh"})
(require '[clojure.spec.alpha :as s])

;; name is supposed to be letters but no numbers
;; Mikes `contains-digit?` implementation
(defn contains-digit? [s]
  (some (into #{} "0123456789") s))
;; alternatively - my implementation
(defn contains-digit? [s]
  (re-matches #".*\d.*" s))

(s/def ::name (s/and string? (complement contains-digit?)))
(s/valid? ::name "abc")
(s/valid? ::name "ab33c")

(s/exercise ::name )

;; let's look at problems when satisfying predicates
(s/def ::foo (s/and string? #(= (count %) 15)))
#_(s/exercise ::foo)
;;=> Couldn't satisfy such-that predicate after 100 tries.
;; let's fix it with `with-gen`
#_(s/def ::foo (s/with-gen
               (s/and string? #(= (count %) 15))
                 ;; write your gen fn here - maybe use test.chuck: https://github.com/gfredericks/test.chuck
                 ...
               ))

(s/exercise inst?)
(s/exercise uri?)

(s/def ::foo (s/keys :req [::age]))
(s/def ::age int?)
(s/exercise ::foo)
(s/valid? ::foo {::age 12 ::name "bar"})
;; now notice that `::name` key is validated against `::name` spec event it's not mentioned in s/keys at all
(s/valid? ::foo {::age 12 ::name "b343ar"})

;; try test.chuck
(require '[com.gfredericks.test.chuck :as chuck])
(require '[com.gfredericks.test.chuck.generators :as genc])
(s/def ::foo (s/with-gen
               (s/and string? #(= (count %) 15))
               ;; Note that with-gen (and other places that take a custom generator) take a no-arg function that returns the generator, allowing it to be lazily realized.
               ;; (see spec guide: https://clojure.org/guides/spec)
               #(genc/string-from-regex #"\w{10,20}")
               ))
(s/exercise ::foo)

;; or perhaps pure test.check
(require '[clojure.spec.gen.alpha :as gen])
(s/def ::foo (s/with-gen
               (s/and string? #(= (count %) 15))
               ;; Note that with-gen (and other places that take a custom generator) take a no-arg function that returns the generator, allowing it to be lazily realized.
               ;; (see spec guide: https://clojure.org/guides/spec)
               #(gen/fmap (fn [chars] (apply str chars))
                          (gen/vector (gen/char-alpha) 10 20))))
(s/exercise ::foo)

;;; phrase: https://github.com/alexanderkiel/phrase
;;; weird and not very useful :(
(require '[phrase.alpha :refer [defphraser phrase-first]])

(defphraser :default [_ _] "Invalid value")
(phrase-first {} ::m {::a 1 ::b 2.0 ::c "ahoj"}) ;=> false!!


;;; expound: https://github.com/bhb/expound
(exp/expound ::m {::a 1 ::b 2.0 ::c "ahoj"})
(exp/expound ::m {::a 1 ::b 2.0 ::c "ahoj"})

(s/def ::x (s/coll-of int?))
(s/def ::m (s/keys :req-un [::x ::y] :opt-un [::z]))
(s/def ::t (s/keys :req-un [::m ::n]))
(exp/expound ::t {:m {:x [1 "a"]}})
(s/explain-data ::t {:m {:x [1 "a"]}})



;; use spec-provider to generate specs
(require '[spec-provider.provider :as sp])
(let [my-ns (name (ns-name *ns*))
      my-spec "my-spec"]
  (sp/pprint-specs (sp/infer-specs [{:status 201, :headers {"Content-Type" "application/json"}, :body "Some html body"}]
                                   (keyword my-ns my-spec))
                   my-ns
                   's))


;;; quick showcase how to use different specs for the same unqualified key `:type`
(s/def :a/type int?)
(s/def ::a-type (s/keys :req-un [:a/type]))

(s/def :b/type string?)
(s/def ::b-type (s/keys :req-un [:b/type]))

(def a {:type 10})
(def b {:type "number"})

(s/valid? ::a-type a)
;; => true
(s/valid? ::a-type b)
;; => false

(s/valid? ::b-type b)
;; => true
(s/valid? ::b-type a)
;; => false




;; pyr Hi, I'm trying to spec a map (over which I dont' have control) which uses an inconvenient idiom:
;;   for a number of members it accepts either a :membername or :memberid key, but at least one must be present. If there's a single member like this, `spec/or` is convenient.
;; * mpenet can't you use
(s/def ::member (s/keys :req-un [(or  ::membername ::memberid)]))
(s/explain-str ::member {:membername "juraj"})
(s/explain-str ::member {:memberfirstname "juraj"})
(s/explain-str ::member {:memberfirstname "juraj" :memberid "jm"})
;; * it also supports `and`, kind of a cool feature: from the docstring: `(s/keys :req [::x ::y (or ::secret (and ::user ::pwd))] :opt [::z])`
(s/def ::member (s/keys :req-un [(or  (and ::membername ::memberage) ::memberid)]))
(s/explain-str ::member {:membername "juraj"})
(s/explain-str ::member {:membername "juraj" :memberage 33})
(s/explain-str ::member {:membername "juraj" :memberid "jm"})


;;; s/and flows conformed results which is why s/or inside s/and doesn't work
#_(s/explain
 (s/and (s/or :int int? :double double?)
        pos?)
 3)
;;=>    clojure.lang.MapEntry cannot be cast to java.lang.Number
;;alex miller:
;;  s/and will flow the conformed result so the and will be receiving a value like `[:int pos?]`
;;  prob the best option here is to
(s/explain
 (s/or :int (s/and int? pos?)
       :double (s/and double? pos?))
 3)

;;; s/and and generators
;;; s/and will use only the first predicate for generator and then filter the remaining stuff

(s/exercise (s/and int? pos? odd?))
;; => ([9 9] [1 1] [3 3] [7 7] [161 161] [11 11] [7 7] [1 1] [101 101] [17 17])

;; but following would fail because odd? doesn't have automatically mapped generator
#_(s/exercise (s/and odd? int? pos?))
;; => ExceptionInfo Unable to construct gen at: [] for: odd? #:clojure.spec.alpha{:path [], :form clojure.core/odd?, :failure :no-gen}
;; clojure.spec.alpha/gensub (alpha.clj:282)
;; ...
;; DON'T BE CONFUSED BY CIDER SHOWING DUMMY ERROR!
;;  1. Unhandled clojure.lang.ExceptionInfo
;;  Spec assertion failed.
;;  
;;  Spec: nil
;;  Value: nil


;;; Sampling specs - every, every-kv
(s/valid? (s/every int? )
          (range 100))
;; => true

(s/valid? (s/every int? )
          (concat (range 100)
                  ["ahoj"]))
;; => false

(s/valid? (s/every int? )
          (concat (range 100)
                  ["ahoj"]))
;; => false

;; sampling specs check only `s/*coll-check-limit` (by default 101)
(s/valid? (s/every int?)
          (concat (range 101)
                  ["ahoj"]))
;; => true

;;; s/exercise more clear - generate pairs of examples and conformed values
;; (s/exercise (s/cat :ns (s/? string?) :name string?))
(s/def ::namespace (s/cat :ns (s/? string?) :name string?))
(s/exercise ::namespace)
'([("" "") {:ns "", :name ""}]
  [("5") {:name "5"}]
  [("x4") {:name "x4"}]
  [("Bt") {:name "Bt"}]
  [("uK") {:name "uK"}]
  [("LcJai") {:name "LcJai"}]
  [("") {:name ""}]
  [("" "") {:ns "", :name ""}]
  [("D4f459h") {:name "D4f459h"}]
  [("qxs") {:name "qxs"}])

(gen/sample (s/gen ::namespace))
;; => (("" "")
;;     ("2")
;;     ("Ao" "jV")
;;     ("3h" "AR3")
;;     ("")
;;     ("231m")
;;     ("2oy" "")
;;     ("4De" "0caUY25")
;;     ("F2A")
;;     ("jePAaU1" "ks6Ilinby"))
