(ns clojure-repl-experiments.experiments
  "Single namespace for all my REPL experiments.
  This might be split up later if I find it useful."
  (:require [clojure.set :as set]
            [criterium.core :as c]
            [seesaw.core :as see]))

;;; Seesaw tutorial: https://gist.github.com/1441520
;;; check also https://github.com/daveray/seesaw
(comment
  (def f (see/frame :title "Get to know Seesaw"))
  (-> f see/pack! see/show!)
  )



;;; Simple By Design blog post: https://drewverlee.github.io/posts-output/2017-4-26-simple-by-design
;;;

;; everything is data
{
 :string "hello"
 :character \f
 :integer 42
 :floating-point 3.14
 :boolean true
 :symbol +
 :keyword [:foo ::foo]
 }

{:list '(1 2 3)
 :vector [1 2 3]
 :map {:a 1 :b 2 :c 3}
 :set #{:a :b :c}
 }

;; Macros
(defmacro backwards [form]
  (reverse form))

;; References
(defprotocol Nachos
  (yum [_] "eat some nachos"))
(defrecord Person [name lbs]
  Nachos
  (yum [person]
    (update-in person [:lbs] + 2)))

(def me (atom (->Person "Drew" 182)))
(def me-before @me)
(swap! me yum)
(def me-after @me)
me-after
me-before

;; Generality through reuse
(def xf (comp (remove odd?) (map inc)))
(transduce xf conj [] (range 10))
;; simplified
(into [] xf (range 10))

;; Safety where you need it - clojure.spec
(require '[clojure.spec.alpha :as s])
(require '[clojure.spec.gen.alpha :as gen])
(s/def ::big-even (s/and integer? even? #(> % 1000)))
(s/valid? ::big-even 10)
(s/valid? ::big-even 1002)
;; now we can attach it to API and catch any nasty non-big numbers
(s/explain-data ::big-even 5)
;; and we can generate examples automatically!
(gen/sample (s/gen ::big-even))



;;; multimethod experiments
;;;
(defmulti login (fn [user-role credentials] user-role))
(defmethod login :admin [_ credentials]
  (println "Admin credentials: " credentials))
(defmethod login :user [_ credentials]
  (println "User credentials: " credentials))

(login :admin {:licensee "jumarko" :license-key "xyz"})

(login :user {:licensee "jumarko" :license-key "xyz"})


;; Petr Mensik's problem:
;; How to access https://pdfbox.apache.org/docs/2.0.4/javadocs/org/apache/pdfbox/pdmodel/PDPageContentStream.AppendMode.html
;; You have to import inner enum class
(import org.apache.pdfbox.pdmodel.PDPageContentStream$AppendMode)
org.apache.pdfbox.pdmodel.PDPageContentStream$AppendMode



;;; Asked on clojurians slack - brave-and-true channel 3.8.2017
(def filename "/Users/jumar/workspace/clojure/clojure-repl-experiments/resources/suspects.csv")
(slurp filename)
(def vamp-keys [:name :glitter-index])
(defn str->int
  [str]
  (Integer. str))
(def conversions {:name identity
                  :glitter-index str->int})
(defn convert
  [vamp-key value]
  ((get conversions vamp-key) value))
(defn parse
  "Convert a CSV into rows of columns"
  [string]
  (map #(clojure.string/split % #",")
       (clojure.string/split string #"\r?\n")))
(parse (slurp filename))
(defn mapify
  "Return a seq of maps like {:name \"Edward Cullen\" :glitter-index 10}"
  [rows]
  (map (fn [unmapped-row]
         (reduce (fn [row-map [vamp-key value]]
                   (assoc row-map vamp-key (convert vamp-key value)))
                 {}
                 (map vector vamp-keys unmapped-row)))
       rows))
(first (parse (slurp filename)))
(first (mapify (parse (slurp filename)))) ;; NullPointer here
(mapify (parse (slurp filename))) ;; and here


;;;
;;; defrecord and protocol experiments from Joy of Clojure
;;;
(defrecord TreeNode [v l r])
(def my-tree (TreeNode. 0 nil nil))
;; you can assoc new fields but the performance is as with regular map
(assoc my-tree :color :red)
;; you can dissoc fields but this will return normal map, not defrecord
(type (dissoc my-tree :v))
;; defrecord never equals to the map with same keys
(= my-tree {:v 0 :l nil :r nil})


;;; Clojure style mixins from Joy of Clojure - p. 211
;;; notice that you have to always provide full implementation of protocol
(defprotocol StringOps
  (rev [s])
  (upp [s]))

(extend-type String
  StringOps
  (rev [s] (clojure.string/reverse s)))

(rev "Works")

;; now try to extend `upp` separately
(extend-type String
  StringOps
  (upp [s] (clojure.string/upper-case s)))
(upp "Works")
;; but `rev` is lost now
#_(rev "Works")
;; but we still can define mixinx separately
(def rev-mixin {:rev clojure.string/reverse})
(def upp-mixin {:upp clojure.string/upper-case})
(def fully-mixed (merge rev-mixin upp-mixin))
(extend String StringOps fully-mixed)
(-> "Works" upp rev)


;;; how to compose multiple predicate functions into one?
;;; https://stackoverflow.com/questions/45643579/compose-multiple-predicate-functions-into-one
;;; => some-fn
(defn- multiple-of-three? [n] (zero? (mod n 3)))
(defn- multiple-of-five? [n] (zero? (mod n 5)))
(def multiple-of-three-or-five?
  (some-fn multiple-of-three? multiple-of-five?))
(multiple-of-three-or-five? 3)
(multiple-of-three-or-five? 4)
(multiple-of-three-or-five? 5)



;;; Dump system info from system properties
;;; See https://www.roseindia.net/java/beginners/OSInformation.shtml
(def env-system-properties
  ["os.name"
   "os.version"
   "os.arch"
   "java.version"
   "java.class.version"
   "java.vendor"
   "java.vm.name"
   "java.vm.vendor"
   "java.vm.info"
   "java.home"
   "java.io.tmpdir"
   "user.name"
   "user.dir"
   "user.timezone"
   "user.language"
   "file.encoding"
   "file.separator"
   "path.separator"
   "line.separator"
   ])

(defn- environment-info []
  (let [runtime (Runtime/getRuntime)]
    (into (->> env-system-properties
               (mapv (fn [prop-name] [prop-name (System/getProperty prop-name)])))
          [["available processors" (.availableProcessors runtime)]
           ["free memory" (.freeMemory runtime)]
           ["max memory" (.maxMemory runtime)]
           ["total memory" (.totalMemory runtime)]])))


;;; Great explanation of lazy sequences: https://groups.google.com/forum/#!topic/clojure/R288cQPgazw
;;; fibonacci numbers example

(def fibs (cons 0 (cons 1 (lazy-seq (map +' fibs (rest fibs))))))
(take 10 fibs)

;; Exercise:
;; You put the lazy seq at the beginning and it hit an infinite loop.
;; You put the lazy seq after the first two hard-coded values, and it worked.
;; What happens when you put it after just the first hard-coded value? Can you explain why?
;; (def fibs (cons 0 (lazy-seq (cons 1 (map +' fibs (rest fibs))))))
(take 10 fibs)

;; alternative fibo implementation using loop-recur
(defn fibo2 [n]
  (loop [a 1 b 0 acc n]
    (if (zero? acc)
      b
      (recur
       (+' a b)
       a
       (dec acc)))))

(fibo2 11)

#_(time (fibo2 100000))

;; Asked on Clojurians slack 19.9.2017 - #beginners
(defmulti query-dispatcher first)
(defmethod query-dispatcher "Select" [[_select [lhs op rhs]]]
  (str lhs " " op " " rhs))

(def q  ["Select" [
                   "first_name" "like" "Sitges"]])
(query-dispatcher q)


;;; Apache POI
;;; https://groups.google.com/forum/#!topic/clojure/XXVTXNHWejQ
(import 'org.apache.poi.xssf.eventusermodel.XLSX2CSV)
(def my-excel-file "test.xlsx")
#_(XLSX2CSV/main (into-array String [my-excel-file]))



;; pomegranate dependencies to classpath with sources and javadocs
;; see https://github.com/vise890/pocketbook/blob/master/src/pocketbook/core.clj
(require '[cemerick.pomegranate :as pomegrante])
;; following doesn't work, why?
(comment 
  (pomegrante/add-dependencies
   '[org.apache.poi/poi-ooxml "3.17" :classifier "sources" ])
  (pomegrante/add-dependencies
   '[org.apache.poi/poi-ooxml "3.17" :classifier "javadoc" ]))

;; with alembic it's easier?
(comment 
  (alembic.still/distill
   '[org.apache.poi/poi-ooxml "3.17" :classifier "sources" ])
  (alembic.still/distill
   '[org.apache.poi/poi-ooxml "3.17" :classifier "javadoc" ]))



;; simple example of using `defprotocol`
(defprotocol MyAnkiProtocol
  "Learn, understand and review."
  (-learn [this x] "Learn x throughly.")
  (-understand [this x] "Try to understand everything about x")
  (-review [this x] "Review x regularly - spaced repetition"))

(defrecord ClojureAnkiCard []
  MyAnkiProtocol
  (-learn [this x] (str "Clojure is a really nice language because of " x))
  (-understand [this x] (str "Now I know how to use " x " in Clojure"))
  (-review [this x] (str "I keep programming in Clojure and use " x " all the time!")))
(let [clojure-card (->ClojureAnkiCard)
      feature "transducers"]
  (println
   (-learn clojure-card feature))
  (println
   (-understand clojure-card feature))
  (println
   (-review clojure-card feature)))


;;; Unix epoch
;; get current unix epoch time in seconds
(quot (System/currentTimeMillis) 1000)
;; convert unix epoch time to date
(def my-unix-time 1234567890)
(java.util.Date. (* 1000 my-unix-time))


;; how do I replace the last element of vector in Clojure?
;; https://stackoverflow.com/questions/46352166/how-to-replace-the-last-element-in-a-vector-in-clojure
(def my-vec [1 2 3 4 5])
;; my solution:
(assoc my-vec (dec (count my-vec)) 10)
;; nicer!!!:
(conj (pop my-vec) 10)


;; how to take every other element in a vector?
(take-nth 2 [1 2 3 4 5])


;; defrecord can implement java interface!
(definterface MyI
  (printx [x]))
(defrecord MyR []
  MyI
  (printx [this a] (println a)))
(.printx (->MyR) "ahoj")


(defn my-fn [x y]
  #break (println "x and y are: " x y)
  #dbg (let [z (+ x y)]
    (dotimes [i 10]
      (println "some side effects: " i))
    (println "z is: " z)))

(my-fn 10 20)


;; reading octal numbers
(clojure.edn/read-string "050") ;=> 40


;; transient data structures
#_(c/quick-bench
 (reduce conj [] (range 1e6)))
;;=> Execution time means: 103 ms

#_(c/quick-bench
 (let [v (reduce conj! (transient []) (range 1e6))]
    (persistent! v)))
;;=> Execution time means: 73 ms


;;; Set operations
(set/map-invert {:a 1 :b 2 :c 3})
;;=> {1 :a, 2 :b, 3 :c}

(set/rename-keys {:a 1 :b 2 :c 3} {:a :aaa :b :bbb :c :ccc})
;;=> {:aaa 1, :bbb 2, :ccc 3}

;; You can work around key collisions by using an array-map to control
;; the order of the renamings.
(set/rename-keys  {:a 1 :b 2 :c 3}  (array-map :a :tmp :b :a :tmp :b))
;;=> {:b 1, :a 2, :c 3}

;; join - without common keys it's just a cartesian product
(set/join #{{:a 1} {:a 2}} #{{:b 1} {:b 2}})
;;=> #{{:a 1, :b 2} {:a 2, :b 1} {:a 1, :b 1} {:a 2, :b 2}}

;; with common keys it's an inner join
(def animals #{{:name "betsy" :owner "brian" :kind "cow"}
               {:name "jake"  :owner "brian" :kind "horse"}
               {:name "josie" :owner "dawn"  :kind "cow"}
               {:name "test"}})
(def personalities #{{:kind "cow" :personality "stoic"}
                            {:kind "horse" :personality "skittish"}})
(set/join animals personalities)
;;=> #{{:kind "horse", :personality "skittish", :name "jake", :owner "brian"}
;;     {:kind "cow", :personality "stoic", :name "betsy", :owner "brian"}
;;     {:kind "cow", :personality "stoic", :name "josie", :owner "dawn"}}

;; select elements in set for which predicate is true
;; like `filter` but returns set
(set/select odd? #{1 2 3})

;; `set/project` can be used to strip unwanted elements
(set/project #{{:name "betsy" :id 33}
               {:name "panda" :id 34}}
             [:name])
;; it's kinda similar to `(map select-keys ...)` but `project` always returns set
;; and it's shorter
(map #(select-keys % [:name])
     #{{:name "betsy" :id 33}
       {:name "panda" :id 34}})


;;; reader literals
(read-string "#inst \"2017-01-01\"")


;;; How to forward protocol methods to existing type?
;; https://stackoverflow.com/questions/46780207/how-to-forward-protocol-methods-to-existing-type
(defprotocol Cost
  (cost [this price-list]))

(defrecord Car [name speed]
  Cost
  (cost [this price-list] (price-list (:name this))))

(def my-car (->Car "bmw" 200))
(def price-list {"bmw" 100000})

(cost my-car price-list)
;;=> 100000

(-> my-car
    (assoc :color "blue")
    (cost price-list))
;;=> 100000


;; reading custom 
(defrecord Car [year model price])

(defn read-car [car-map]
  (map->Car car-map))

(clojure.edn/read-string
 {:readers {'car read-car}}
 "#car{:year 2017, :model \"Aston Martin V8 Vantage\", :price 3000000}")

(map nil [1 2 3])



;;; inspector-jay: https://github.com/timmolderez/inspector-jay

#_(inspector-jay.core/inspect java.util.AbstractQueue)


;;; case

;; petr.mensik
(def all-statuses {:won {:a 1}
                   :lost-project {:a 2}
                   :lost-invoice {:a 3}})
(def my-status {:a 1})
#_(let [contact-status (case my-status
                       (:won all-statuses) (println "won")
                       (:lost-project all-statuses) (println "lost project")
                       (:lost-invoice all-statuses) (println "lost invoice")
                       nil)]
  (println contact-status))

;; clojure pills screencast
(case 1 (inc 0) "1" (dec 1) "0" :default)


;;; clojure test output diffing
(def expected {:url "http://example.com",
                :accept "application/json",
                :conn-timeout 1000,
                :proxy-host "proxy.example.com",
                :proxy-port 3128,
                :proxy-user "user",
                :proxy-pass "password"})
(def actual {:url "http://example.com",
               :accept "application/json",
               :conn-timeout 1000,
               :proxy-host "proxy.example.com",
               :proxy-port 3128,
               :proxy-pass "password"})
(clojure.data/diff expected actual)



;;; https://codereview.stackexchange.com/questions/179678/binary-search-algorithm-in-clojure
(defn log-search
  [elements, elem-to-find]
  (loop [left 0
         right (- (count elements) 1)]
    (when (<= left right)
      (def m (int (Math/floor (/ (+ left right) 2))))
      (def actual (nth elements m))
      (cond
        (= actual elem-to-find) m
        (< actual elem-to-find) (recur (+ m 1) right)
        (> actual elem-to-find) (recur left (- m 1))))))

(defn log-search
  [elements elem-to-find]
  (loop [left 0
         right (dec (count elements))]
    (when (<= left right)
      (let [m (int (Math/floor (/ (+ left right) 2)))
            actual (nth elements m)]
        (cond
          (= actual elem-to-find) m
          (< actual elem-to-find) (recur (+ m 1) right)
          (> actual elem-to-find) (recur left (- m 1)))))))

(log-search (range 10) 10)



;;; performance of `re-pattern` vs. literal pattern syntax #
;;; see https://stackoverflow.com/questions/47126035/clojure-hash-vs-re-pattern

#_(c/quick-bench (type #"12(ab)*34"))

#_(c/quick-bench (type (re-pattern "12(ab)*34")))

#_(let [re #"12(ab)*34"
      s "aanbciasdfsidufuo12ab34xcnm,xcvnm,xncv,m"]
  (c/quick-bench 
   (re-find re s)))

#_(let [re (re-pattern "12(ab)*34")
      s "aanbciasdfsidufuo12ab34xcnm,xcvnm,xncv,m"]
  (c/quick-bench 
   (re-find re s)))


(defn break [x]
  (let [y (* x x)]
    (doseq [n (range y)]
      (let [msg (str "Hello " n)]
        #break (println msg)))))

;; just call the function and press "n"
#_(break 3)


;;; How to print all Clojure special forms
(prn (keys (clojure.lang.Compiler/specials)))



;;; pmap question from slack
;;; For my understanding pmap goal is to run a function in parallel over a collection, limiting parallelism.
;;; From pmap code I underatand that the desired parallelism level is 2+num-of-cpus
;;; In practice i see a different behaviour:
;;; This shows all 20 delays running in parallel
;;; (I have 4 CPUs and running with clojure 1.8)
;; alternative fibo implementation using loop-recur

(defn- f1 [delay-num]
  (Thread/sleep (+ 100 (rand-int 1000)))
  (println (java.util.Date.) "delay #" delay-num "started")
  (Thread/sleep (* 1000 5))
  (println (java.util.Date.) "delay #" delay-num "finished")
  delay-num)

(defn run []
  (let [delays (range 41)
        results (pmap f1 delays)]
    (println "num of cpus = " (.. Runtime getRuntime availableProcessors))
    (println "total is" (reduce + results))))
#_(run)
