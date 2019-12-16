(ns clojure-experiments.experiments
  "Single namespace for all my REPL experiments.
  This might be split up later if I find it useful."
  (:require [clj-java-decompiler.core :refer [decompile]]
            [clojure.datafy :as d]
            [clojure.set :as set]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [criterium.core :as c]
            [flatland.useful.debug :as ud]
            [seesaw.core :as see]
            [tupelo.core :as t]))

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
(def filename "/Users/jumar/workspace/clojure/clojure-experiments/resources/suspects.csv")
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

;;; Fibonacci implementation using `iterated` (Chtristopher Grand)
(defn fibo []
  (map first
       (iterate (fn [[a b]] [b (+ a b)]) [0 1])))

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
(comment
  (import 'org.apache.poi.xssf.eventusermodel.XLSX2CSV)
  (def my-excel-file "test.xlsx")
  (XLSX2CSV/main (into-array String [my-excel-file])))



;; THIS ISN'T ON THE CLASSPATH ANYMORE!
;; pomegranate dependencies to classpath with sources and javadocs
;; see https://github.com/vise890/pocketbook/blob/master/src/pocketbook/core.clj
#_(require '[cemerick.pomegranate :as pomegrante])
;; following doesn't work, why?
(comment 
  (pomegrante/add-dependencies
   '[org.apache.poi/poi-ooxml "3.17" :classifier "sources" ])
  (pomegrante/add-dependencies
   '[org.apache.poi/poi-ooxml "3.17" :classifier "javadoc" ]))

;; with alembic it's easier?
  ;; (alembic.still/distill
  ;;  '[org.apache.poi/poi-ooxml "3.17" :classifier "sources" ])
  ;; (alembic.still/distill
  ;;  '[org.apache.poi/poi-ooxml "3.17" :classifier "javadoc" ])



;; simple example of using `defprotocol`
(defprotocol MyAnkiProtocol
  "Learn, understand and review."
  (-learn [this x] "Learn x thoroughly.")
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


(comment
  
  ;; note: #break and #dbg doesn't work outside cider, in particular it fails when you try `lein uberjar`
  ;; (defn my-fn [x y]
  ;;   #break (println "x and y are: " x y)
  ;;   #dbg (let [z (+ x y)]
  ;;          (dotimes [i 10]
  ;;            (println "some side effects: " i))
  ;;          (println "z is: " z)))

  #_(my-fn 10 20))


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


;; (defn break [x]
;;   (let [y (* x x)]
;;     (doseq [n (range y)]
;;       (let [msg (str "Hello " n)]
;;         #break (println msg)))))

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



;;; https://stackoverflow.com/questions/47291914/finding-max-value-using-reduce
;;; need to transform the table to a hash-map that maps products to the city that has the highest sale
(def table [
            {:product "Pencil"
             :city "Toronto"
             :year "2010"
             :sales "2653.00"}
            {:product "Pencil"
             :city "Oshawa"
             :year "2010"
             :sales "525.00"}
            {:product "Bread"
             :city "Toronto"
             :year "2010"
             :sales "136,264.00"}
            {:product "Bread"
             :city "Oshawa"
             :year "nil"
             :sales "242,634.00"}
            {:product "Bread"
             :city "Ottawa"
             :year "2011"
             :sales "426,164.00"}])

(defn process [table]
  (let [parseDouble #(Double/parseDouble (clojure.string/replace % #"," ""))]
    (->> table
         (group-by :product)
         (map (comp (juxt :product :city)
                    (partial apply max-key (comp parseDouble :sales))
                    val))
         (into {}))))

(process table);=> {"Pencil" "Toronto", "Bread" "Ottawa"}



;;; Slack question about debugging;
;;; andras.liter hi guys. I'm using Cursive to develop & debug our Clojure apps,
;;; often experimenting things with the REPL. I wonder if there is an efficient way to create datastructures in the REPL
;;; sourced from a Debug breakpoint - e.g. if I have a complex datastructure,
;;; which I'd like to work with in the REPL as well (to test functions, debug etc...),
;;; how can I have a var referencing it the easiest way in the REPL?
;;; Of course I dont want to type the whole thing in the REPL, but would like to get the data
;;; from an app under- debugging (say I have a breakpoint and the object is there)?

;; let's say I want to get `m`
(defn debug-me [x y]
  (let [z (* x y)
        w (Math/pow z x)
        m {:x x
           :y y
           :wz {:z z :w w}}]
    (def d-m m)
    (keys m)))
(debug-me 4 5)
d-m


;;; How to filter a sequence and retain evaluated pred values?
;;; https://stackoverflow.com/questions/47325664/how-to-filter-a-sequence-and-retain-evaluated-pred-values
;;; (keep #(some->> % self-pred (vector %)) data)
(keep #(some->> % rseq (vector %)) [[1 2] [] [3 4]])
;;=> ([[1 2] (2 1)] [[3 4] (4 3)])



;;; quick debugging of threadinug macro
(def spy #(do (prn %) %))
(defn quick-spy [coll]
  (->> coll
       (map inc)
       spy
       (filter odd?)
       spy
       (map identity)))
(quick-spy (range 10))

;; with debux: https://github.com/philoskim/debux
;; don't forget to require `debux.core/dbg` first (use Alfred's snippet `debux`)
#_(defn quick-debux [coll]
  ;; try also `dbgn`
  (dbg (->> coll
            (map inc)
            (filter odd?)
            (map identity))))
#_(quick-debux (range 10))


;;; cmal Hi, how to get the min value's index of a vector? shall i use `(let [v [1 2 3 4]] (.indexOf v (apply min v)))`? (edited)
;;; bronsa you can:
(let [v [4 1 2 3]]
  (apply min-key v (range (count v))))


;;; stackoverflow: https://stackoverflow.com/questions/47424089/clojure-filter-sequence-of-maps-for-a-value-in-a-list
;;; Trying to filter a sequence of maps that have a value for a key in a list of values.
(remove #(#{"0000" "2222"} (:x %))
        [{ :y "y" :x "0000"} {:y "y" :z "z" :x "1111"} {:y "y" :z "z" :x "2222"}])


;; I want to return this
;; => [{:y "y" :z "z" :x "1111"}]


;;; https://dev.clojure.org/jira/browse/CLJ-2275
;;; case fails for vectors with negative numbers
(case -1 -1 true false)
;;=> true
(case [-1] [-1] true false)
;;=> true
(case (int -1) -1 true false)
;;=> true
(case [(int 1)] [1] true false)
;;=> true
(case [(int -1)] [-1] true false)
;;=> false



;;; bronsa
;; use `replace`
(def myv [:a :c :b])
(def mym {:a :anew})
(replace mym myv)
;; user `reduce-kv` and `update`
(def a {:a 1 :b 2})
(def b {:a inc})
(reduce-kv update a b)


;;; interesting examples from Stuart Sierra's talk
;;; "Learning Clojure: Next Steps": https://www.youtube.com/watch?v=pbodL96HM28

;; printer & reader - simple serialization protocol
(defn serialize [x]
  (binding [*print-dup* true]
    (pr-str x)))

(defn deserialize [string]
  (read-string string))

(deserialize (serialize {:a 1 :bv [1 2 #{:a :b :c}]}))

;; #=() trick in clojure reader to eval code
(read-string "[a b #=(* 3 4)]")
;;=> [a b 12]

;; set *read-eval* to false if you don't trust the source:
#_(binding [*read-eval* false]
  (read-string "#=(java.lang.System/exit 0)"))

;; read from resources with PushbackReader 
(with-open [r (java.io.PushbackReader.
               (clojure.java.io/reader
                (clojure.java.io/resource "clojure/core.clj")))]
  [(read r)
   (read r)
   (read r)
   (read r)
   (read r)])


;;; Clojure / Clojurescript: group-by a map on multiple values:
;;; https://stackoverflow.com/questions/48010316/clojure-clojurescript-group-by-a-map-on-multiple-values
(def tools 
  [{:name "A",
    :config
    {:accepts ["id"],
     :classes ["X"]}}
   {:name "B",
    :config
    {:accepts ["id"],
     :classes ["X", "Y"]
     }}])
(apply merge-with into {}
       (for [tool tools
             class (get-in tool [:config :classes])]
         {class [tool]}))
;;=> {"X" [{:name "A", :config {:accepts ["id"], :classes ["X"]}}
;;         {:name "B", :config {:accepts ["id"], :classes ["X" "Y"]}}],
;;    "Y" [{:name "B", :config {:accepts ["id"], :classes ["X" "Y"]}}]}


;;; keywordize map's keys
(clojure.walk/keywordize-keys {"a" 1 "b" 2})
;; "manually"
(into {}
      (map
       (fn [[k v]] [(keyword k) v]))
       {"a" 1 "b" 2})



;;; interesting behavior of when-let when using destructuring
;;; there's just no way to use `when-let`/`if-let` on destructured values
;;; 
(def foo {:a 1})

(when-let [a (:a foo)] (println a))
;; => prints "1", returns nil

(when-let [{:keys [a]} foo] (println a))
;; => prints "1", returns nil

(when-let [a (:a (assoc foo :a nil))] (println a))
;; => returns nil

(when-let [{:keys [a]} (assoc foo :a nil)] (println a))
;; => prints "nil", returns nil


;;; clj-java-decompiler: https://github.com/clojure-goes-fast/clj-java-decompiler
(decompile (fn [] (println "Hello, decompiler!")))
(decompile (let [x 1] (println x)))

(decompile (loop [i 100, sum 0]
             (if (< i 0)
               sum
               (recur (unchecked-dec i) (unchecked-add sum i)))))



;;; clj-ldap exploration
;; [org.clojars.pntblnk/clj-ldap "0.0.15"]
;; unboundid sdk: 
(comment
  
  (require '[clj-ldap.client :as ldap])
  (ldap/connect {:host {:address "codescene-sso.westeurope.cloudapp.azure.com"
                        :port 636}
                 :ssl? true
                 :timeout 3000
                 :connect-timeout 3000})
  (ldap/bind? {} "csuser@mycompany.local" "abc")

  (import '(com.unboundid.ldap.sdk LDAPConnectionPool ResultCode))
  (defn my-bind [connection bind-dn password]
    (try
      (let [r (if (instance? LDAPConnectionPool connection)
                (.bindAndRevertAuthentication connection bind-dn password nil)
                (.bind connection bind-dn password))]
        (= ResultCode/SUCCESS (.getResultCode r)))
      (catch Exception e
        (do
          (println (.getMessage e))
          false))))
  (my-bind {} "csuser@mycompany.local" "abc"))

;; now let's try to add "sources" artifact to the project.clj
;; :profiles {:uberjar {:aot :all}
;;            ;; notice sources for development!
;;            :dev {:dependencies [[com.unboundid/unboundid-ldapsdk "4.0.0" :classifier "sources"]]}})
;; and restart REPL...

;; if that's too anoying we can try to hotload the dependency...
;; `cljr-hotload-dependency`
;; => BOOM! this artificat is not in Clojars
;; => Try alembic: https://github.com/pallet/alembic
;; add alembic (, r a p)
;; make sure to clojure clojure-emacs/alembic: [clojure-emacs/alembic "0.3.3"]
;; and use it
;; (require '[alembic.still :as a])
#_(a/distill '[com.unboundid/unboundid-ldapsdk "4.0.0" :classifier "sources"])

;; Unfortunately, this still doesn't mean that it will work with cider navigation automatically.
;; But: you can use `cider-open-classpath-entry`  to locate unboundid sources jar and open
;; whatever source file you want



;;; http://blog.cognitect.com/blog/2016/9/15/works-on-my-machine-understanding-var-bindings-and-roots

(def ^:dynamic answer 42)

(binding [answer 0]
  (future
    (Thread/sleep 1000)
    (println answer)))
                                        ;=> prints "0"


(with-redefs [answer 0]
  (future
    (Thread/sleep 1000)
    (println answer)))

                                        ;=> prints "42"



;;; dadair Is it safe, in terms of sorting/order, to do ``? Or is there potential for the id to actually be incorrect for a given `x`?
(def xs [{:id 1 :name "John"}
         {:id 10 :name "Ann"}
         {:id 3 :name "Ben"}
         {:id 2 :name "Henry"}]) 
(zipmap (map :id xs) xs)
(reduce (fn [m x] (assoc m (:id x) x)) {} xs)
(into {} (map (juxt :id identity) xs))
(into {} (map (juxt :id identity)) xs)

(comment
  (require '[criterium.core :as c])
  (c/quick-bench (zipmap (map :id xs) xs))
  (c/quick-bench (reduce (fn [m x] (assoc m (:id x) x)) {} xs))
  (c/quick-bench (into {} (map (juxt :id identity) xs)))
  (c/quick-bench (into {} (map (juxt :id identity)) xs))
  )


;;; namespaced maps

;; duplicated key `foo` is overwritten
(let [{:x/keys [foo bar] :y/keys [foo baz]} {:x/foo 1 :x/bar 2 :y/foo 10 :y/baz 20}]
  [foo bar baz])
;;=> [10 2 20]

;; we can resolve the conflict
(let [{:x/keys [foo bar] :y/keys [baz] yfoo :y/foo} {:x/foo 1 :x/bar 2 :y/foo 10 :y/baz 20}]
  [foo bar yfoo baz])
;;=> [1 2 10 20]

;; aliases require double collons and namespace has to exist
::s/my-key
;; #_::super/my-key ;=> invalid token!
#_(alias 'superx 'not.exist) ;=> no namespace found


;;; Profile with tufte: https://github.com/ptaoussanis/tufte#how-does-tufte-compare-to-hugoduncancriterium

(require '[taoensso.tufte :as tufte :refer (defnp p profiled profile)])
(comment
  ;; We'll request to send `profile` stats to `println`:
  (tufte/add-basic-println-handler! {})

;;; Let's define a couple dummy fns to simulate doing some expensive work
  (defn get-x [] (Thread/sleep 500)             "x val")
  (defn get-y [] (Thread/sleep (rand-int 1000)) "y val")

  ;; How do these fns perform? Let's check:
  (profile ; Profile any `p` forms called during body execution
   {} ; Profiling options; we'll use the defaults for now
   (dotimes [_ 5]
     (p :get-x (get-x))
     (p :get-y (get-y)))))


(comment 

  (defmacro protocol [name & body]
    `(defn [command]
       (condp = command
         body)))

  (protocol cmds
            "LIST" (list-handler)
            "EXIT" (exit-handler))

  (defn cmds [command]
    (condp = command
      "LIST" (list-handler)
      "EXIT" (exit-handler))))



;;; https://stackoverflow.com/questions/50663848/converting-string-to-nested-map-in-clojure
(def my-file "1|apple|sweet
  2|coffee|bitter
  3|gitpush|relief")

(as-> my-file $
  (string/split $ #"\n")
  (map #(string/split % #"\|") $)
  (map (juxt first rest) $)
  (into {} $))


;;; https://stackoverflow.com/questions/50650009/filter-on-the-values-of-multiple-keys-in-a-map-entry-and-return-a-map-with-those
(def maps
  [{:id 2, :category "Big bang theory", :name "The Big Bang!"}
   {:id 3, :category "The big Lebowski", :name "Ethan Coen"}
   {:id 4, :category "Chitty Chitty Bang Bang", :name "Roald Dahl"}])


(filter
 #(some
   (fn [v]
     (when (string? v)
       (-> v
           (string/lower-case)
           (string/includes? "ban"))))
   (vals %))
 maps)

(->> maps
     (filter (fn [m]
               ())))


;;; noisesmith @sova here's a macro that's very useful when debugging
(defmacro locals
  []
  (into {}
        (map (juxt (comp keyword name)
                   identity))
        (keys &env)))

(defn foo [x]
  (let [y (+ x x)]
    (println (locals)) y))
(foo 2)
;; prints
;; {:x 2, :y 4}

;; see also: https://www.safaribooksonline.com/library/view/clojure-programming/9781449310387/ch05s08.html
(defmacro spy-env []
  (let [ks (keys &env)]
    `(prn (zipmap '~ks [~@ks]))))

(let [x 1 y 2]
  (spy-env)
  (+ x y))



;;; performance `doall` vs `mapv` (vector)

;; `doall`
(comment
  
  (c/quick-bench (doall (map inc (range 1000000))))
  ;; Evaluation count : 6744 in 6 samples of 1124 calls.
  ;; Execution time mean : 79.154994 µs
  ;; Execution time std-deviation : 5.479175 µs
  ;; Execution time lower quantile : 71.472044 µs ( 2.5%)
  ;; Execution time upper quantile : 85.333914 µs (97.5%)
  ;; Overhead used : 12.084946 ns

  )

;; `mapv`
(comment 
  (c/quick-bench (mapv inc (range 1000000)))
  ;; Evaluation count : 13680 in 6 samples of 2280 calls.
  ;; Execution time mean : 51.043335 µs
  ;; Execution time std-deviation : 10.854776 µs
  ;; Execution time lower quantile : 41.069582 µs ( 2.5%)
  ;; Execution time upper quantile : 68.105034 µs (97.5%)
  ;; Overhead used : 12.084946 ns

  (c/quick-bench (vec (map inc (range 1000))))
  
  )

;;; how to turn this:
(def my-data {:foo ["a" "b" "c"], :bar ["x" "y" "z"] :baz [1 2 3]})
;; to this:
[{:foo "a", :bar "x"} {:foo "b", :bar "y"}]

;; my solution
(apply
 mapv
 #(into {} %&)
 (for [[k vs] my-data]
   (for [v vs] [k v])))

       
;; alexyakushev [11:28 AM]
(let [ks (keys my-data)]
  (->> (vals my-data)
       (#(doto % prn))
       (apply map vector) ;; Transpose the matrix of values
       (#(doto % prn))
       (mapv (partial zipmap ks))))


;;; holyjak [9:34 AM]
;;; Hello, is there a better way to extract an item from a collection based on its attribute than
(def cars [{:name "AM" :producer "Ferrari"}
           {:name "BM" :producer "Mercedes"}
           {:name "CM" :producer "Ferrari"}])

(some 
 #(when 
      (= (:producer %) "Ferrari") 
    %) 
 cars)

;; VS seancorfield [10:29 AM]
;; @holyjak `(first (filter #(= "Ferrari" (:producer %)) cars))` would work if you don't mind the search going further than the first match (chunking).
(first (filter #(= "Ferrari" (:producer %)) cars))

;; VS valtteri [10:37 AM]
;; If the collection is not enormous I often find myself transforming it into a map and then doing a lookup. My favourite function atm is:
;; NOTE: this doesn't work well
(defn index-by
  ([idx-fn coll]
   (index-by idx-fn identity coll))
  ([idx-fn value-fn coll]
   (into {} (map (juxt idx-fn value-fn)) coll)))
(index-by :producer cars)

;; hiredman: clojure.set/index
;; https://gist.github.com/hiredman/7d17d8d2b58c41ce95bf2db305b0f427
(-> cars
    (clojure.set/index [:producer])
    (get {:producer "Ferrari"})
    first)


;; clojuredocs: http://clojuredocs.org/clojure.set/index
(def weights #{ {:name 'betsy :weight 1000}
               {:name 'jake :weight 756}
               {:name 'shyq :weight 1000} })

(clojure.set/index weights [:weight])


;;; https://groups.google.com/forum/#!topic/clojure/tfHYGjyIWWM
(defmacro foo [x]
  (+ x 10))
(def ^:const bar 100)
;; following doesn't work:
#_(foo bar)


#_(require '[debux.core :refer [dbg dbgn]])


;;; 4clojure 53 - leetwinski elegant solution: http://www.4clojure.com/problem/solutions/53
;; also interesting example of using debux
;; I dont' use debux anymore
;; (require '[debux.core :refer [dbg dbgn]])
(defn longest-increasing-subseq [numbers]
  (or (->> (range (count numbers) 1 -1)
           (mapcat #(partition % 1 numbers))
           (filter #(apply < %))
           first)
      []))

(longest-increasing-subseq [1 0 1 2 3 0 4 5])


;;; Question on slack about lazy seq
;;; *The problem*
;;;   Works fine for when `first-duplicate` only has to look at small amounts of accumulated list,
;;;   get StackOverflow error for longer input:
;;; Notes:
;;; - this is from Advent of Code - Day 1
;;; - accumulcate-lazy can be replaced with `(reductions + ...)
(defn accumulate-lazy
  "Takes in a lazy sequence and lazily produces the accumulated output."
  ([coll] (accumulate-lazy 0 coll))
  ([sum coll]
   (when-let [[x & xs] (seq coll)]
     (let [new-sum (+ sum x)]
       (cons new-sum (lazy-seq (accumulate-lazy new-sum xs)))))))

(defn first-duplicate
  "Returns the first duplicate in a sequence"
  ([coll] (first-duplicate coll #{}))
  ([coll accumulator]
   (loop [[x & xs] (seq coll)
          acc accumulator]
     (when x
       (if (contains? acc x)
         x
         (recur xs (conj acc x)))))))

#_(first-duplicate (accumulate-lazy (cycle [-14 1 10 4])))
;; => -13
#_(first-duplicate (accumulate-lazy (cycle (range 10000))))
;; => 49995000
#_(first-duplicate (accumulate-lazy (range 10000)))
;; => nil


;;; Threading macros gotchas/experiments
((-> x10 (fn [x] (inc x)))
 1)

;; OR
((-> x10 #(inc %))
 1)
;; => 2

;; BUT
#_((-> 10 (fn [x] (inc x)))
 1)
;; => macroexpanding error: 10 - failed: vector? 
#_((-> x10 (fn x10 [x] (inc x)))
 1)
;; => macroexpanding error: x10 - failed: vector? 


(first (filter (fn [i] (println "resolved" i) (= i 14)) (range 100)))
;; => 14
;; prints numbers from 0 to 31 (chunked seq)

(some (fn [i] (println "resolved" i) (when (= i 14) i)) (range 100))
;; => 14
;; printlns only numbers from 0 to 14


;;; useful by Alan Malloy
;;; https://github.com/clj-commons/useful

;; macro ? for debugging -> very primitive
#_(ud/? (map (fn [x] (inc x))
           (range 10)))


;;; Question on Slack:
;; Christian Barraza  Hey everyone! Working through Brave and True so I can help with a particular project-
;; came across a suggestion to try and re-implement the 'comp' function
;; Anyways, here is my code, which works with two arguments, but no more.
;; And I'm really struggling to understand why:
(defn my-comp
  [f & g]
  (fn [& args]
    (f (if (= (count g) 1)
         (apply (first g) args)
         (apply (apply my-comp g) args)))))
((my-comp inc inc *) 2 3)
;; => 8

(comp)
