(ns clojure-experiments.collections
  "See also experiments.clj"
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))


;;; apply concat vs flatten, etc.
;;; http://chouser.n01se.net/apply-concat/

;; flatten uses `sequential?` under the hood
;; which means it doesn't work with sets, strings, arrays, ...
(sequential? "abc")
;; => false

(flatten [#{:a :b} #{:c :d}])
;; => (#{:b :a} #{:c :d})
;; VS.
(flatten [[:a :b] [:c :d]])
;; => (:a :b :c :d)

;; on the other hand `apply concat` or `mapcat seq` work fine
(apply concat [#{:a :b} #{:c :d}])
;; => (:b :a :c :d)
(mapcat seq [#{:a :b} #{:c :d}])
;; => (:b :a :c :d)

;; and this one is particularly tricky!
(flatten [[1 [2 3]] #{4 5 6}])	
;; => (1 2 3 #{4 6 5})

;; reduce into is eager and dependent on the type of the first element in the collection
(reduce into [#{:a :b} #{:c :d}])
;; => #{:c :b :d :a}
(reduce into [[:a :b] #{:c :d}])
;; => [:a :b :c :d]

;; sequence/edution cat:
(sequence cat [#{:a :b} #{:c :d}])
;; => (:b :a :c :d)


;; There's also cool tool for not-printing lazy seqs
(def ^:dynamic *hold-the-lazy* false)

(defn write-lazy-seq [space? coll w]
  (if (and *hold-the-lazy*
           (instance? clojure.lang.IPending coll)
           (not (realized? coll)))
    (.write w (str (when space? " ") "..."))
    (when (seq coll)
      (when space?
        (.write w " "))
      (.write w (pr-str (first coll)))
      (write-lazy-seq true (rest coll) w))))

(defmethod print-method clojure.lang.ISeq [coll w]
  (.write w "(")
  (write-lazy-seq false coll w)
  (.write w ")"))

(let [xs (map inc (range 50))]
  (binding [*hold-the-lazy* true]
    (prn xs)))
;;=> will print:
;; (...)

;; ... with chunked seqs
(binding [*hold-the-lazy* true]
  (let [xs (map inc (range 50))]
    (first xs)
    (prn xs)))
;;=> prints:
;; (1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 ...)

;; "unchunk" any chunked seqs
;; (Joy of Clojure 15.3.1)
(defn seq1 [s]
  (lazy-seq
   (when-let [[x] (seq s)]
     (cons x (seq1 (rest s))))))
(binding [*hold-the-lazy* true]
  (let [xs (seq1 (range 50))]
    (first xs)
    (prn xs)))
;;=> prints:
;; (0 ...)


;;; ghadi - index-by (on Slack)
(defn index-by
  "Builds a map whose entries are the result of applying
   keyf and valf to every item in the provided collection.
   Throws an exception if two items map to the same key,
   unless mergef is provided.

   valf defaults to identity"
  ([keyf coll]
   (index-by keyf identity coll))
  ([keyf valf coll]
   (reduce (fn [m v]
             (let [k (keyf v)]
               (if (find m k)
                 (throw (ex-info "Duplicate key" {:k k}))
                 (assoc m k (valf v)))))
           {}
           coll))
  ([keyf valf mergef coll]
   (reduce (fn [m v]
             (let [k (keyf v)]
               (if-let [entry (find m k)]
                 (assoc m k (mergef (val entry) (valf v)))
                 (assoc m k (valf v)))))
           {}
           coll)))
(index-by inc (partial + 3) [1 10 100])
;; => {2 4, 11 13, 101 103}

;;; hiredman using clojure.set/index https://gist.github.com/hiredman/7d17d8d2b58c41ce95bf2db305b0f427
;;; in response to:
;;;    hindol In Clojure, what is the idiomatic way to build multiple indices over the same data?
;;;    Suppose I have a set of records and I want to look them up using different keys.
;;;    If the values change, they should immediately/eventually reflect across all keys.
;;;    Kind of like a database.
;;;    Currently, I am just mapping/filtering over the set of things at runtime.
(require '[clojure.set :as set])

(def info
  [{:year 2017
    :month 4
    :data "x"}
   {:year 2017
    :month 4
    :data "y"}
   {:year 2017
    :month 7
    :data "z"}])

(def db (reduce
         (fn [db fact]
           ;; notice `merge-with-into` and `set/index`
           (merge-with into db
                       (set/index [fact] [:entity])
                       (set/index [fact] [:entity :attribute])
                       (set/index [fact] [:entity :attribute :value])
                       (set/index [fact] [:attribute])
                       (set/index [fact] [:attribute :value])
                       (set/index [fact] [:value])))
         {}
         (for [m info
               :let [id (java.util.UUID/randomUUID)]
               [k v] m]
           {:entity id
            :attribute k
            :value v})))
db
;; partial inspection =>:
;;    {{:attribute :month}
;;     #{{:entity #uuid "994a2626-85c8-440a-a85c-c11def76f2c1", :attribute :month, :value 4}
;;       {:entity #uuid "d8afcc50-87b3-49a4-ba80-678c23aeab53", :attribute :month, :value 7}
;;       {:entity #uuid "4320e80e-9b49-4ef6-85be-816d57e73531", :attribute :month, :value 4}},

;;     {:entity #uuid "d8afcc50-87b3-49a4-ba80-678c23aeab53", :attribute :month}
;;     #{{:entity #uuid "d8afcc50-87b3-49a4-ba80-678c23aeab53", :attribute :month, :value 7}},

;;     {:value 7} #{{:entity #uuid "d8afcc50-87b3-49a4-ba80-678c23aeab53", :attribute :month, :value 7}},

;;     {:entity #uuid "4320e80e-9b49-4ef6-85be-816d57e73531", :attribute :month}
;;     #{{:entity #uuid "4320e80e-9b49-4ef6-85be-816d57e73531", :attribute :month, :value 4}},

;;     {:entity #uuid "994a2626-85c8-440a-a85c-c11def76f2c1", :attribute :month, :value 4}
;;     #{{:entity #uuid "994a2626-85c8-440a-a85c-c11def76f2c1", :attribute :month, :value 4}},

;;     {:entity #uuid "d8afcc50-87b3-49a4-ba80-678c23aeab53"}
;;     #{{:entity #uuid "d8afcc50-87b3-49a4-ba80-678c23aeab53", :attribute :month, :value 7}
;;       {:entity #uuid "d8afcc50-87b3-49a4-ba80-678c23aeab53", :attribute :year, :value 2017}
;;       {:entity #uuid "d8afcc50-87b3-49a4-ba80-678c23aeab53", :attribute :data, :value "z"}},

;;     {:attribute :data, :value "z"}
;;     #{{:entity #uuid "d8afcc50-87b3-49a4-ba80-678c23aeab53", :attribute :data, :value "z"}},

;;     ...
;;     {:value 4}
;;     #{{:entity #uuid "994a2626-85c8-440a-a85c-c11def76f2c1", :attribute :month, :value 4}
;;       {:entity #uuid "4320e80e-9b49-4ef6-85be-816d57e73531", :attribute :month, :value 4}}}

(for [{:keys [entity attribute value]} (get db {:attribute :month :value 4})
      {data :value} (get db {:entity entity :attribute :data})]
  [entity data])
;;=> ([#uuid "8ce5aaea-e633-4972-b8ef-3202680b2f6f" "y"] [#uuid "08d3e0ec-c08b-492e-b638-86fb2e932a93" "x"])


;;; https://juxt.pro/blog/posts/ontheflycollections-with-reducible.html
;;; Implementing IReduceInit; e.g. for reading zip file entries
(defn zipfile-reducible
  "expects to be passed a .zip or .jar file
   crawls through the file, presenting entries to the reducing function
   ensures the file is closed afterwards"
  [zf]
  (reify clojure.lang.IReduceInit
    (reduce [this f init]
      (with-open [is (io/input-stream zf)
                  zis (java.util.zip.ZipInputStream. is)]
        (loop [acc init]
          (let [next-entry (.getNextEntry zis)]
            (if (and (some? next-entry)
                     (not (reduced? acc)))
              (recur (f acc (assoc (bean next-entry)
                                   :input-stream zis)))
              (unreduced acc)))
          )))))

;; use it like this
(comment 
  (into []
        (comp (drop 10) (take 1))
        (zipfile-reducible
         (io/file (System/getProperty "java.home") "lib/jrt-fs.jar")))
;; => [{:input-stream
;;      #object[java.util.zip.ZipInputStream 0x113dd8ce "java.util.zip.ZipInputStream@113dd8ce"],
;;      :creationTime nil,
;;      :compressedSize -1,
;;      :lastAccessTime nil,
;;      :directory false,
;;      :method 8,
;;      :name "jdk/internal/jimage/decompressor/ResourceDecompressorFactory.class",
;;      :time 1548657016000,
;;      :extra nil,
;;      :size -1,
;;      :crc -1,
;;      :lastModifiedTime #object[java.nio.file.attribute.FileTime 0x4d46fd6f "2019-01-28T06:30:16Z"],
;;      :class java.util.zip.ZipEntry,
;;      :comment nil,
;;      :timeLocal #object[java.time.LocalDateTime 0x34b5e4b2 "2019-01-28T07:30:16"]}];;
  )

;; or even more interesting - crawl all JDK jar files
(defn preserving-reduced
  "copy-and-pasted from clojure.core, which declares it as private"
  [rf]
  #(let [ret (rf %1 %2)]
     (if (reduced? ret)
       (reduced ret)
       ret)))

(defn chaining-reducible
  "like concat but for reducibles
  takes a coll of colls.
  Returns reducible that chains call to reduce over each coll"
  [coll-of-colls]
  (reify clojure.lang.IReduceInit
    (reduce [_ f init]
      (let [prf (preserving-reduced f)]
        (reduce (partial reduce prf)
          init
          coll-of-colls)))))

(defn iszipped-suffix?
  [^java.io.File f]
  (let [n (.getName f)]
    (some #(string/ends-with? n %)
      #{".jar" ".zip"})))

(comment
  (time (reduce
         (fn [acc _] (inc acc))
         0
         (chaining-reducible
          (map zipfile-reducible
               (filter iszipped-suffix?
                       (file-seq (.getParentFile (.getParentFile (io/file (System/getProperty "java.home"))))))))))
  ;;=> "Elapsed time: 1491.455866 msecs"
  ;;   19774


  ;;
  )

