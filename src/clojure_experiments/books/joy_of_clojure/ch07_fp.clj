 (ns clojure-experiments.books.joy-of-clojure.ch07-fp
  "Examples from the chapter 7: Functional Programming."
  (:require [clojure.test :as t]))

;;; First class functions

;; building arbitray nth functions (p. 138)
(defn fnth [n]
  (apply comp
         (cons first
               (take (dec n)
                     (repeat rest)))))


((fnth 5) [1 2 3 4 5 6])
;; => 5


;; clojure.test stores tests in function's metadata
(defn join
  {:test (fn []
           (assert (= "1,3,3"
                      (join "," [1 2 3]))))}
  [sep s]
  (apply str (interpose sep s)))

(join "," [1 2 3])

#_(clojure.test/run-tests *ns*)
;;=>
;; Testing clojure-experiments.books.joy-of-clojure.ch07-fp
;; ERROR in (join) (ch07_fp.clj:22)
;; Uncaught exception, not in assertion.
;; expected: nil
;; actual: java.lang.AssertionError: Assert failed: (= (join "," [1 2 3]) "1,3,3")


;;; 7.1.2 Higher-order functions (p. 140)
;;; -------------------------------------------

;; sort can be used to sort wide variety of elements
(import '(java.util Date))
(sort [(Date.) (Date. 100)])
;; => (#inst "1970-01-01T00:00:00.100-00:00" #inst "2020-10-06T04:58:11.381-00:00")

;; you can use sort with custom comparison function
;; e.g. to reverse sorting order
(sort > [7 1 4])
;; => (7 4 1)

(sort [7 1 4])
;; => (1 4 7)
(sort < [7 1 4])
;; => (1 4 7)

;; but sort cannot be used to sort different types of elements
;; or sort by only part of an aggregate;
;; e.g. here we want to sort by the second element
(sort [[:a 7] [:c 13] [:b 21]])
;; => ([:a 7] [:b 21] [:c 13])

;; trying `sort second` doesn't work
#_(sort second [[:a 7] [:c 13] [:b 21]])
;;=> Wrong number of args (2) passed to: clojure.core/second--5392

;; => use `sort-by`
(sort-by second [[:a 7] [:c 13] [:b 21]])
;; => ([:a 7] [:c 13] [:b 21])

;; sort-by example
(def plays [{:band "Burial" :plays 979 :loved 9}
            {:band "Eno" :plays 2333 :loved 15}
            {:band "Bill Evans" :plays 979 :loved 9}
            {:band "Magma" :plays 2665 :loved 31}])

(def sort-by-loved-ratio (partial sort-by #(/ (:plays %) (:loved %))))

(sort-by-loved-ratio plays)
;; => ({:band "Magma", :plays 2665, :loved 31}
;;     {:band "Burial", :plays 979, :loved 9}
;;     {:band "Bill Evans", :plays 979, :loved 9}
;;     {:band "Eno", :plays 2333, :loved 15})

;; p. 143 - we wish to implement a function that can sort by columns;
;; primary column and then any number of secondary columns

;; here's my implementation:
(defn columns [cols]
  (apply juxt cols))

;; and implementation from the book
(defn columns [cols]
  (fn [row]
    (vec (map row cols))))

(sort-by (columns [:plays :loved :band])
         plays)
;; => ({:band "Bill Evans", :plays 979, :loved 9}
;;     {:band "Burial", :plays 979, :loved 9}
;;     {:band "Eno", :plays 2333, :loved 15}
;;     {:band "Magma", :plays 2665, :loved 31})


;;; 7.1.3 Pure functions (p. 144)
;;; -------------------------------------------

(defn keys-apply [f ks m]
  (let [only (select-keys m ks)]
    (zipmap (keys only)
            (map f (vals only)))))
(keys-apply #(.toUpperCase %) #{:band} (plays 0))
;; => {:band "BURIAL"}

;; another interesting function
(defn manip-map [f ks m]
  (merge m (keys-apply f ks m)))
(manip-map #(int (/ % 2))
           #{:plays :loved}
           (plays 0))
;; => {:band "Burial", :plays 489, :loved 4}


;;; Named/optional arguments (p. 146)
;;; -------------------------------------------
(defn slope [& {:keys [p1 p2]
                :or {p1 [0 0] p2 [1 1]}}]
  (float (/ (- (p2 1) (p1 1))
            (- (p2 0) (p1 0)))))
(slope :p1 [4 15] :p2 [3 21])
;; => -6.0
(slope :p2 [2 1])
;; => 0.5
(slope)
;; => 1.0


;;; pre- and post-conditions (p. 146)
;;; -------------------------------------------


;; Decoupling assertions from functions can be a good idea!
;; to make them contextual
;; so instead of adding pre-/post- conditions to `put-things` directly:
(defn put-things [m]
  (into m {:meat "beef" :veggie "broccoli"}))

(put-things {})
;; => {:meat "beef", :veggie "broccoli"}

(defn vegan-constraints [f m]
  {:pre [(:veggie m)]
   :post [(:veggie %) (nil? (:meat %))]}
  (f m))


#_(vegan-constraints put-things {:veggie "carrot"})
;; Assert failed: (nil? (:meat %))

(defn balanced-diet [f m]
  {:post [(:meat %) (:veggie %)]}
  (f m))

(balanced-diet put-things {})
;; => {:meat "beef", :veggie "broccoli"}

(defn finicky "never change the meat"
  [f m]
  {:post [(= (:meat %) (:meat m))]}
  (f m))

#_(finicky put-things {:meat "chicken"})
;; Assert failed: (= (:meat %) (:meat m))


;; my little experiments
(defn pre-post [x]
  {:pre [(number? x)]
   :post [(int? %)]}
  (* x x))
(pre-post 10)
;; => 100
#_(pre-post "10")
;; Execution error (AssertionError) at clojure-experiments.core/pre-post (form-init5739487266911467216.clj:157).
;; Assert failed: (number? x)
#_(pre-post 10.0)
;; Execution error (AssertionError) at clojure-experiments.core/pre-post (form-init5739487266911467216.clj:157).
;; Assert failed: (int? %)


;; try truss library with richer error messages

(require '[taoensso.truss :as it :refer [have have! have?]])
(defn pre-post2 [x]
  {:pre [(have number? x)]
   :post [(have int? %)]}
  (* x x))
#_(pre-post2 "10")
;; Invariant violation in `clojure-experiments.core:159`. Test form: `(number? x)` with failing input: `10`
#_(pre-post2 10.0)
;; Invariant violation in `clojure-experiments.core:160`. Test form: `(int? %)` with failing input: `100.0`





(conj {:a 1 } {:b 2 :c 3})
;; => {:a 1, :b 2, :c 3}

(conj nil {:b 2 :c 3})
;; => ({:b 2, :c 3})

(merge nil {:b 2 :c 3})
;; => {:b 2, :c 3}



;;; Closures (p. 148)
;;; ----------------

;; Closure is a function that has access to locals from the context where it was created:
(def times-two
  (let [x 2]
    (fn [y] (* y x))))

;; closing-over mutable locals
(def add-and-get
  (let [ai (java.util.concurrent.atomic.AtomicInteger.)]
    (fn [y] (.addAndGet ai y))))
(add-and-get 2)
;; => 2
(add-and-get 2)
;; => 4
(add-and-get 2)
;; => 6


;; functions returning closures (p. 150)
(defn times [n]
  (let [x n]
    (fn [y] (* y x))))
;; or just this (closing over parameters)
(defn times [n]
  (fn [y] (* y n)))


(def times-three (times 3))
(times-three 4)
;; => 12

;; you can use closure whenever an anonymous function is needed
(defn filter-divisible [denom s]
  (filter #(zero? (rem % denom)) s))
(filter-divisible 5 (range 20))
;; => (0 5 10 15)


;;; Sharing closure context - multiple closures using same context (p. 151-153)
;;; - and polymorphism

(def bearings [{:x 0 :y 1} ; north
               {:x 1 :y 0} ; east
               {:x 0 :y -1} ; south
               {:x -1 :y 0} ; west
               ])

(defn bot [x y bearing-num]
  {:coords [x y]
   :bearing ([:north :east :south :west] bearing-num)
   :forward (fn [] (bot (+ x (:x (bearings bearing-num)))
                        (+ y (:y (bearings bearing-num)))
                        bearing-num))
   :turn-right (fn [] (bot x y (mod (+ 1 bearing-num) 4)))
   :turn-left (fn [] (bot x y (mod (- 1 bearing-num) 4)))})

(:bearing ((:forward (bot 5 5 0))))
;; => :north

;; you could now define a mirror-bot which would use different definitions for forward, turn-right and turn-left



;;;; 7.3 Thinking Recursively (p. 155)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; mundane recursion
(defn pow [base exp]
  {:pre [(nat-int? exp)]}
  (if (zero? exp)
    1
    (* base (pow base (dec exp)))))
(pow 3 4)
;; => 81

;; stackoverflow
#_(pow 2 10000)

;; let's use letfn to avoid stack overflow
(defn pow [base exp]
  (letfn [(kapow [base exp acc]
            (if (zero? exp)
              acc
              (recur base (dec exp) (* base acc))))]
    (kapow base exp 1)))
(pow 2 10)
;; => 1024
(time (pow 2N 10000))
"Elapsed time: 3.653808 msecs"
;; => 19950631168807583848837421626835850838234968318861924548520089498529438830221946631919961684036194597899331129423209124271556491349413781117593785932096323957855730046793794526765246551266059895520550086918193311542508608460618104685509074866089624888090489894838009253941633257850621568309473902556912388065225096643874441046759871626985453222868538161694315775629640762836880760732228535091641476183956381458969463899410840960536267821064621427333394036525565649530603142680234969400335934316651459297773279665775606172582031407994198179607378245683762280037302885487251900834464581454650557929601414833921615734588139257095379769119277800826957735674444123062018757836325502728323789270710373802866393031428133241401624195671690574061419654342324638801248856147305207431992259611796250130992860241708340807605932320161268492288496255841312844061536738951487114256315111089745514203313820202931640957596464756010405845841566072044962867016515061920631004186422275908670900574606417856951911456055068251250406007519842261898059237118054444788072906395242548339221982707404473162376760846613033778706039803413197133493654622700563169937455508241780972810983291314403571877524768509857276937926433221599399876886660808368837838027643282775172273657572744784112294389733810861607423253291974813120197604178281965697475898164531258434135959862784130128185406283476649088690521047580882615823961985770122407044330583075869039319604603404973156583208672105913300903752823415539745394397715257455290510212310947321610753474825740775273986348298498340756937955646638621874569499279016572103701364433135817214311791398222983845847334440270964182851005072927748364550578634501100852987812389473928699540834346158807043959118985815145779177143619698728131459483783202081474982171858011389071228250905826817436220577475921417653715687725614904582904992461028630081535583308130101987675856234343538955409175623400844887526162643568648833519463720377293240094456246923254350400678027273837755376406726898636241037491410966718557050759098100246789880178271925953381282421954028302759408448955014676668389697996886241636313376393903373455801407636741877711055384225739499110186468219696581651485130494222369947714763069155468217682876200362777257723781365331611196811280792669481887201298643660768551639860534602297871557517947385246369446923087894265948217008051120322365496288169035739121368338393591756418733850510970271613915439590991598154654417336311656936031122249937969999226781732358023111862644575299135758175008199839236284615249881088960232244362173771618086357015468484058622329792853875623486556440536962622018963571028812361567512543338303270029097668650568557157505516727518899194129711337690149916181315171544007728650573189557450920330185304847113818315407324053319038462084036421763703911550639789000742853672196280903477974533320468368795868580237952218629120080742819551317948157624448298518461509704888027274721574688131594750409732115080498190455803416826949787141316063210686391511681774304792596709376N


;; recursive unit calculator
(def simple-metric {:meter 1,
                    :km 1000,
                    :cm 1/100,
                    :mm [1/10 :cm]})

(defn convert [context descriptor]
  (reduce
   (fn [result [mag unit]]
     (if-let [val (get context unit)]
       (+ result
          (if (vector? val)
            ;; here's the recursive call
            (* mag (convert context val))
            (* val mag)))
       (throw (ex-info (format "Invalid unit '%s' not present in the context." unit)
                       {:unit unit
                        :context context}))))
   0
   (partition 2 descriptor)))

(convert simple-metric [50 :cm])
;; => 1/2
(convert simple-metric [100 :mm])
;; => 1/10
(float (convert simple-metric [3 :km 10 :meter 80 :cm 10 :mm]))
;; => 3010.81

;; you can now use `convert` for generic unit conversion
(convert {:bit 1 :byte 8 :nibble [1/2 :byte]}
         [32 :nibble])
;; => 128N


;;; Tail recursion (p. 159)

;; gcd implementation using mundane recursion
(defn gcd [x y]
  (cond
    (> x y) (gcd (- x y) y)
    (< x y) (gcd x (- y x))
    :else x))
(gcd 10 15)
;; => 5
#_(gcd 1013718973981723987 1537891723891)
;;=> StackOverflow

;; optimized gcd
(defn gcd2 [x y]
  (cond
    (> x y) (recur (- x y) y)
    (< x y) (recur x (- y x))
    :else x))
(gcd2 10 15)
;; => 5
(gcd2 1013718973981723987 1537891723891)
;; => 1

;; but just adding outer int cast make recur fail because it's no longer in tail position
#_(defn gcd2 [x y]
  (int (cond
     (> x y) (recur (- x y) y)
     (< x y) (recur x (- y x))
     :else x)))
;;=> Can only recur from tail position


;;; Trampoline - Elevator example (FSM - finite state machine)
;;; p. 161
(defn elevator
  "Given a sequence of commands  and the elevator in the `ff-open` state - first floor, open doors -
  return true if the sequence is valid, false otherwise."
  [commands]
  (letfn
      [(ff-open [[_ & r]]
         "When the elevator is open on the 1st floor it acn either close or be done"
         #(case _
            :close (ff-closed r)
            :done true
            false))
       (ff-closed [[_ & r]]
         "When the elevator"
         #(case _
            :open (ff-open r)
            :up (sf-closed r)
            false))
       (sf-closed [[_ & r]]
         "When the elevator is closed on the 2nd floor it can either go down or open."
         #(case _
            :open (sf-open r)
            :down (ff-closed r)
            false))
       (sf-open [[_ & r]]
         "When the elvator is open on the 2nd floor it can either close or be done."
         #(case _
            :close (sf-closed r)
            :done true
            false))]
    (trampoline ff-open commands)))

(elevator [:close :open :done])
;; => true
(elevator [:close :up :open :done])
;; => true
;; But can't open already opened door
(elevator [:open :open :done])
;; => false


;;; Continuation-passing style (p. 163)

;; factorial using CPS style
(defn fac-cps [n k]
  ;; `cont` is the "Continuation" function - notice it also calls the "Return" function `k`
  ;; notice that `k` is first just `identity` but then it becomes `cont` function as passed in recur
  (letfn [(cont [v]
            #_(println "Computing factorial" n "using value:" v)
            ;; when n reaches zero, the stack of `cont` functions will get called repeatedly here
            ;; but the very last `cont` implementation that would use `n` bound to zero won't get called at all
            ;; because it's the previous version of `cont` passed in as `k` that gets called
            (k (* v n)))]
    ;; `zero?` is the "Accept" function
    (if (zero? n)
      ;; when n reaches zero, this triggers calls of `cont` functions stack
      (k 1)
      (recur (dec n) cont))))

(defn fac [n]
  ;; identity is the "Return" function
  (fac-cps n identity))
(fac 5)
;; Computing factorial 1 using value: 1
;; Computing factorial 2 using value: 1
;; Computing factorial 3 using value: 2
;; Computing factorial 4 using value: 6
;; Computing factorial 5 using value: 24
;; => 120

;; it can compute huge numbers too
;; but don't try to print this in the buffer!
#_(fac 5000N)

;; generalized builer function
(defn make-cps [accept? kend kont]
  (fn [n]
    ((fn [n k]
       (let [cont (fn [v]
                    (k ((partial kont v) n)))]
         (if (accept? n)
           (k 1)
           (recur (dec n) cont))))
     n kend)))

(def fac (make-cps zero?
                   identity
                   #(* %1 %2)))
(fac 5);; => 120

(def tri (make-cps #(== 1 %)
                   identity
                   #(+ %1 %2)))
(tri 10) ;; => 55


;;;; A* implementation (p. 165)

;; borrow the neighboars function from the chapter 5
(defn neighbors
  ([size yx]
   (neighbors [[-1 0] [1 0] [0 -1] [0 1]]
              size
              yx))
  ([deltas size [y x :as yx]]
   ;; my own implementation
   (for [[dy dx :as d] deltas
         :let [newyx (mapv + d yx)]
         :when (every? #(< -1 % size) newyx)]
     newyx)))
(neighbors 5 [0 0])
;; => ([1 0] [0 1])

;; 1. estimate cost by assuming we can travel to the right edge and then down
;; - note that A* requires that the function never "over-estimates".
(defn estimate-cost [step-cost-est size y x]
  (* step-cost-est
     (- (* 2 size)
        x
        y
        2)))
;; having grid of size 5, that is upper-left coordinates are [0 0] and lower right [4 4]
;; we can get the estimate of 7200 for cost 900 (going 4 steps to the right and 4 steps down)
(estimate-cost 900 5 0 0)
;; => 7200

;; 2. compute cost of the path so far
(defn path-cost [node-cost
                 {:keys [cost] :as cheapest-neighbor}]
  (+ node-cost
     (or cost 0)))
(path-cost 900 {:cost 1})
;; => 901

;; 3. finally, `total-cost`
(defn total-cost [new-cost step-cost-est size y x]
  (+ new-cost
     (estimate-cost step-cost-est size y x)))
(total-cost 0 900 5 0 0)
;; => 7200
(total-cost 1000 900 5 3 4)
;; => 1900
(total-cost (path-cost 900 {:cost 1})
            900 5 3 4)
;; => 1801

;;; We also need `min-by` function
(defn min-by [f coll]
  (when (seq coll) ; this guards against empty seq because there's no clear "minium" of such a sequence
    (reduce (fn [min other]
              (if (> (f min) (f other))
                other
                min))
            coll)))

(min-by #(* % %) [-3 2 3 4])
;; => 2
(min-by #(* % %) [])
;; => nil

(min-by :cost [{:cost 100} {:cost 36} {:cost 9}])
;; => {:cost 9}


;;; A* implementation (p. 167)
;;; - tail-recursive implementation
(defn astar [start-yx step-est cell-costs]
  (let [size (count cell-costs)]
    (loop [steps 0
           routes (vec (repeat size (vec (repeat size nil))))
           work-todo (sorted-set [0 start-yx])]
      (if (empty? work-todo)
        [(peek (peek routes))
         :steps steps]
        (let [[_ yx :as work-item] (first work-todo)
              rest-work-todo (disj work-todo work-item)
              nbr-yxs (neighbors size yx)
              cheapest-nbr (min-by :cost
                                   (keep #(get-in routes %)
                                         nbr-yxs))
              ;; calculate path so far
              newcost (path-cost (get-in cell-costs yx)
                                 cheapest-nbr)
              oldcost (:cost (get-in routes yx))]
          (if (and oldcost (>= newcost oldcost))
            (recur (inc steps) routes rest-work-todo)
            (recur (inc steps)
                   ;; place a new path in the routes
                   (assoc-in routes yx {:cost newcost :yxs (conj (:yxs cheapest-nbr []) yx)})
                   ;; add the estimated path to the todo
                   (into rest-work-todo
                         (map (fn [[y x :as w]]
                                [(total-cost newcost step-est size y x) w])
                              nbr-yxs)))))))))

;; world defined on p 165
(def world [[1   1   1   1   1]
            [999 999 999 999 1]
            [1   1   1   1   1]
            [1 999 999 999 999]
            [1   1   1   1   1]])
(astar [0 0]
       900
       world)
;; => [{:cost 17, :yxs [[0 0] [0 1] [0 2] [0 3] [0 4] [1 4] [2 4] [2 3] [2 2] [2 1] [2 0] [3 0] [4 0] [4 1] [4 2] [4 3] [4 4]]} :steps 94]

(astar [0 0]
       900
       [[   1   1   1   2   1]
        [   1   1   1 999   1]
        [   1   1   1 999   1]
        [   1   1   1 999   1]
        [   1   1   1   1   1]])
;; => [{:cost 9, :yxs [[0 0] [0 1] [0 2] [1 2] [2 2] [3 2] [4 2] [4 3] [4 4]]} :steps 134]


(astar [0 0]
       900
       [[1   1   1   2   1]
        [1   1   1 999   1]
        [1   1   1 999   1]
        [1   1   1 999   1]
        [1   1   1 666   1]])
;; => [{:cost 10, :yxs [[0 0] [0 1] [0 2] [0 3] [0 4] [1 4] [2 4] [3 4] [4 4]]} :steps 132]


;;; Try Dijkstra algorithm using our generic `astar` function (p. 455)
(defn dijkstra-estimate-cost [step-cost-est sz y x]
  0)
(def estimate-cost dijkstra-estimate-cost)

(astar [0 0]
       900
       world)
;; astar:
;; => [{:cost 17, :yxs [[0 0] [0 1] [0 2] [0 3] [0 4] [1 4] [2 4] [2 3] [2 2] [2 1] [2 0] [3 0] [4 0] [4 1] [4 2] [4 3] [4 4]]} :steps 94]
;; Dijkstra:
;; => [{:cost 17, :yxs [[0 0] [0 1] [0 2] [0 3] [0 4] [1 4] [2 4] [2 3] [2 2] [2 1] [2 0] [3 0] [4 0] [4 1] [4 2] [4 3] [4 4]]} :steps 81]

(astar [0 0]
       900
       [[   1   1   1   2   1]
        [   1   1   1 999   1]
        [   1   1   1 999   1]
        [   1   1   1 999   1]
        [   1   1   1   1   1]])
;; astar
;; => [{:cost 9, :yxs [[0 0] [0 1] [0 2] [1 2] [2 2] [3 2] [4 2] [4 3] [4 4]]} :steps 134]
;; dijkstra
;; => [{:cost 9, :yxs [[0 0] [0 1] [0 2] [1 2] [2 2] [3 2] [4 2] [4 3] [4 4]]} :steps 65]

(astar [0 0]
       900
       [[1   1   1   2   1]
        [1   1   1 999   1]
        [1   1   1 999   1]
        [1   1   1 999   1]
        [1   1   1 666   1]])
;; astar
;; => [{:cost 10, :yxs [[0 0] [0 1] [0 2] [0 3] [0 4] [1 4] [2 4] [3 4] [4 4]]} :steps 132]
;; Dijkstra
;; => [{:cost 10, :yxs [[0 0] [0 1] [0 2] [0 3] [0 4] [1 4] [2 4] [3 4] [4 4]]} :steps 65]

