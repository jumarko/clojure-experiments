(ns clojure-experiments.transducers.eduction)

;;;; TODO: record a Clojure Quip episode about `eduction`?
;;;; 



;;; Long example from https://livebook.manning.com/book/clojure-the-essential-reference/chapter-7/v-25/291
;;; ------------------------------------------------------------------------------------------------
(def data
  [{:fee-attributes [49 8 13 38 62]
    :product {:visible false
              :online true
              :name "Switcher AA126"
              :company-id 183
              :part-repayment true
              :min-loan-amount 5000
              :max-loan-amount 1175000}
    :created-at 1504556932728}
   {:fee-attributes [11 90 79 7992]
    :product {:visible true
              :online true
              :name "Green Professional"
              :company-id 44
              :part-repayment true
              :min-loan-amount 25000
              :max-loan-amount 3000000}
    :created-at 15045569334789}
   {:fee-attributes [21 12 20 15 92]
    :product {:visible true
              :online true
              :name "Fixed intrinsic"
              :company-id 44
              :part-repayment true
              :min-loan-amount 50000
              :max-loan-amount 1000000}
    :created-at 15045569369839}])

(import 'java.util.Date)

(defn- merge-into [k ks]
  (map (fn [m]
         (merge (m k) (select-keys m ks)))))

(defn- update-at [k f]
  (map (fn [m]
         (update m k f))))

(defn- if-key [k]
  (filter (fn [m]
            (if k (m k) true))))

(defn if-equal [k v]
  (filter (fn [m]
            (if v (= (m k) v) true))))

(defn if-range [k-min k-max v]
  (filter (fn [m]
            (if v (<= (m k-min) v (m k-max)) true))))

(def prepare-data
  (comp
   (merge-into :product [:fee-attribute :created-at])
   (update-at :created-at #(Date. %))))

(defn filter-data [params]
  (comp
   (if-key :visible)
   (if-key :online)
   (if-equal :company-id (params :company-id))
   (if-key (params :repayment-method))
   (if-range :min-loan-amount
             :max-loan-amount
             (params :loan-amount))))

(defn xform [params]
  (comp
   prepare-data
   (filter-data params)))

(defn products [params data]
  (eduction (xform params) data))

;; returned products are likely to be different for each request => better to use eduction
;; if you have no plan to perform multiple scans of the output collection
(map :name
     (products
      {:repayment-method :part-repayment
       :loan-amount 500000}
      data))
;; => ("Green Professional" "Fixed intrinsic")

;;; ------------------------------------------------------------------------------------------------



;;; eduction - laziness vs eagerness
;;;

;; eduction starts a new loop for every sequence operation
;; notcie that count will be raised up to 66 (2x 32 elements realized from range)
(def cnt1 (atom 0))
(let [res (eduction (map #(do (swap! cnt1 inc) %)) (range 100))]
  (conj (rest res) (first res))
  @cnt1)
;; 66 (2 x 33 - chunked seqs?)

(def cnt2 (atom 0))
(let [res (sequence (map #(do (swap! cnt2 inc) %)) (range 100))]
  (conj (rest res) (first res)) ; (2)
  @cnt2)
;; => 33


;; Per Alex Miller it should consume entire thing eagerly when you ask for the first value?
;; => that's not what I observe....
;; UPDATE: alexmiller  I shouldn't have said "eager" there. 
;;   it's a delayed reduction, how eager it is depends on what you do with it
(time (let [res (eduction (map inc) (filter odd?)
                     (range 100000000))]
   (println "Hello")
   (println (clojure.core/take 10 res))
   (println "Done")))
;; Hello
;; (1 3 5 7 9 11 13 15 17 19)
;; Done
;; "Elapsed time: 1.734041 msecs"

;; also an alternative experiment:
;; Do they say that eduction/sequence realize everything once asked for the first element?
(def my-seq (range 40))
(let [xs (eduction (map #(doto % print inc)) my-seq)]
  (first xs))
;; prints (not all elements!)
;; 01234567891011121314151617181920212223242526272829303132

;; ... and the same when trying with line-seq
(comment
  (def my-lines (line-seq (clojure.java.io/reader "random.csv")))
  (let [xs (eduction (map #(doto % println identity)) my-lines)]
    (first xs)))

;; Do they say that eduction/sequence realize everything once asked for the first element?
(def my-seq (range 40))
(let [xs (eduction (map #(doto % print inc)) my-seq)]
  (first xs)
  ;; you can try call the first second time to see that eduction computes the elements again
  (first xs))
;; prints (not all elements!)
;; 0123456789101112131415161718192021222324252627282930313201234567891011121314151617181920212223242526272829303132

;; compare to `sequence`:
(let [xs (sequence (map #(doto % print inc)) my-seq)]
  (first xs)
  (first xs))
;; prints:
;; 01234567891011121314151617181920212223242526272829303132


;; compare to eduction wrap by another seq => it makes it lazy and elements are cached again!
(let [xs (seq (eduction (map #(doto % print inc)) my-seq))]
  (first xs)
  ;; you can try call the first second time to see that eduction computes the elements again
  (first xs))
;; just like `sequence` realizes only once and then the elements are cached!
;; 01234567891011121314151617181920212223242526272829303132


;; it's interesting to compare this to filter transducer which may produce "no element"
;; and thus more than 32 elements being realized even when we only ask for the first one
(def my-seq (range 70))
(let [xs (eduction (comp
                    (map #(doto % print))
                    (filter #(> % 32))
                    (map inc)) my-seq)]
  (first xs))
;; prints:
;; 01234567891011121314151617181920212223242526272829303132333435363738394041424344454647484950515253545556575859606162636465

;; OR to `mapcat` transducer which may produce many elements
;; THIS IS REALLY INTERESTING!
(def my-seq (range 40))
(let [xs (eduction (comp
                    (map #(doto % print))
                    (mapcat #(repeat 10 %))) my-seq)]
  (first xs))
;; prints !!!
;; 0123


;; Now try to use `eduction` ala IReduceInit
;; Shouldn't this realize all the elements??
;; (trying with very large range to actually check if it doesn't take too long...)
(def my-seq (range 40000000000))
(let [xs (eduction (map #(doto % print inc)) my-seq)]
  (reduce
   (fn [acc x]
     (if (< x 10)
       (conj acc x)
       (reduced acc)))
   []
   xs))
;; prints:
;; 012345678910

;; and compare to `sequence`
(def my-seq (range 40))
(let [xs (sequence (map #(doto % print inc)) my-seq)]
  (reduce
   (fn [acc x]
     (if (< x 10)
       (conj acc x)
       (reduced acc)))
   []
   xs))
;; prints:
;; 01234567891011121314151617181920212223242526272829303132


