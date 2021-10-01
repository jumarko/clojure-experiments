(ns clojure-experiments.collections
  (:require [clj-http.client :as http]
            [taoensso.timbre :as log]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [medley.core :as m]))

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


;;; vectors destructuring, subvec, etc.

;; when you destructure a vector you get a ...
... sequence, not vector;
;; so `conj` will add the element to the beginning!
(let [[f & r] [1 2 3 4]]
  (conj r 10))
;; => (10 2 3 4)


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


;;; sort-by

;; sort-by (Functional Design in Clojure)
;; 3 levels of higher-order functions
(sort-by (juxt (comp - :year) :title)
         [{:year 2019 :title "Pragmatic Programmer"}
          {:year 2020 :title "Clojure: The Essential Reference"}
          {:year 2019 :title "BPF Performance Tools"}])
;; => ({:year 2020, :title "Clojure: The Essential Reference"}
;;     {:year 2019, :title "BPF Performance Tools"}
;;     {:year 2019, :title "Pragmatic Programmer"})



;; order of keys/vals in a hashmap is consistent with `seq`
;; but only within the same hashmap: https://clojurians.slack.com/archives/C053AK3F9/p1609787876129600?thread_ts=1609773432.105500&cid=C053AK3F9
(def m1 (-> {} (assoc 0 :added-first) (assoc 92612215 :added-second)))
(keys m1)
;; => (0 92612215)
(def m2 (-> {} (assoc 92612215 :added-first) (assoc 0 :added-second)))
(keys m2)
;; => (92612215 0)

(def m1h (-> (hash-map :very-first -1) (assoc 0 :added-first) (assoc 92612215 :added-second)))
(keys m1h)
;; => (0 92612215 :very-first)
(def m2h (-> (hash-map :very-first -1) (assoc 92612215 :added-first) (assoc 0 :added-second)))
(keys m2h)
;; => (92612215 0 :very-first)


(nthrest (range 10) 2)
;; => (2 3 4 5 6 7 8 9)
(drop 2 (range 10))
;; => (2 3 4 5 6 7 8 9)


(take 20 (nthrest (map pr (range)) 2))

(take 20 (drop 2 (map pr (range))))


;; unfold from hiredman for API response pagination for example: https://gist.github.com/hiredman/022e617e37f9c5622e1a943a5c34afe5#file-scratch-clj-L1-L18
;; <<
;; Ramon Rios: Did someone recomends a lib to deal with responses pagination?
;; hiredman: if you mean unpaginating api responses, this is the basic pattern I use https://gist.github.com/hiredman/022e617e37f9c5622e1a943a5c34afe5#file-scratch-clj-L1-L18 I've written a lot of different versions of that. that particular unfold doesn't create a lazy seq
;; there is also a ticket to add similar functionality to core https://clojure.atlassian.net/browse/CLJ-2555 which supersedes an older ticket with more of a write up and commentary on it https://clojure.atlassian.net/browse/CLJ-1906
;; >>
;; See also clojure-experiments.flatland.useful
;; and https://clojure.atlassian.net/browse/CLJ-2555
(defn unfold
  "Returns a reducible whose values are created by iterative
  application of `producer` to the previous value, start with
  `seed`. Stops generating values when `continue?` returns false.
  See https://clojure.atlassian.net/browse/CLJ-2555 (`iteration` proposal which looks more useful for API pagination)"
  {:added "1.9"}
  [continue? producer seed]
  (reify
    clojure.lang.IReduceInit
    (reduce [_ fun init]
      (loop [init (fun init seed)
             seed seed]
        (if (reduced? init)
          @init
          (if (continue? seed)
            (let [next-seed (producer seed)]
              (recur (fun init next-seed)
                     next-seed))
            init))))))
;; my example:
(reduce conj []
        (unfold #(< % 10) inc 0))
;; => [0 1 2 3 4 5 6 7 8 9 10]

;; hiredman's eample:
(into []
      (unfold
       (partial some some?)
       (fn [seqs] (map next seqs))
       [(range 3) (range 5) (range 6)]))
;; => [[(0 1 2) (0 1 2 3 4) (0 1 2 3 4 5)] ((1 2) (1 2 3 4) (1 2 3 4 5)) ((2) (2 3 4) (2 3 4 5)) (nil (3 4) (3 4 5)) (nil (4) (4 5)) (nil nil (5)) (nil nil nil)]

;; https://clojure.atlassian.net/browse/CLJ-2555.patch
(defn iteration
  "creates a seqable/reducible given step!,
   a function of some (opaque continuation data) k

   step! - fn of k/nil to (opaque) 'ret'

   :some? - fn of ret -> truthy, indicating there is a value
           will not call vf/kf nor continue when false
   :vf - fn of ret -> v, the values produced by the iteration
   :kf - fn of ret -> next-k or nil (will not continue)
   :initk - the first value passed to step!

   vf, kf default to identity, some? defaults to some?, initk defaults to nil

   it is presumed that step! with non-initk is unreproducible/non-idempotent
   if step! with initk is unreproducible, it is on the consumer to not consume twice"
  {:added "1.11"}
  [step! & {:keys [vf kf some? initk]
            :or {vf identity
                 kf identity
                 some? some?
                 initk nil}}]
  (reify
    clojure.lang.Seqable
    (seq [_]
      ((fn next [ret]
         (when (some? ret)
           (cons (vf ret)
                 (when-some [k (kf ret)]
                   (lazy-seq (next (step! k)))))))
       (step! initk)))
    clojure.lang.IReduceInit
    (reduce [_ rf init]
      (loop [acc init
             ret (step! initk)]
        (if (some? ret)
          (let [acc (rf acc (vf ret))]
            (if (reduced? acc)
              @acc
              (if-some [k (kf ret)]
                (recur acc (step! k))
                acc)))
          acc)))))

;; "paginated API" test case
(let [items 12 pgsize 5
        src (vec (repeatedly items #(java.util.UUID/randomUUID)))
        api (fn [tok]
              (let [tok (or tok 0)]
                (when (< tok items)
                  {:tok (+ tok pgsize)
                   :ret (subvec src tok (min (+ tok pgsize) items))})))
      api-reducible (iteration api :kf :tok :vf :ret)]
  (reduce conj [] api-reducible)
  ;; or flatten the pages
  #_(into [] cat api-reducible)
  )
;; => [[#uuid "97586849-099e-4c6c-a3de-d6c95ffad7a1"
;;      #uuid "30ec19ea-df7f-43ce-8b25-66b348350cee"
;;      #uuid "9b537003-dced-4572-906a-c27b250e2064"
;;      #uuid "983b43f5-2426-47ae-a684-bf40e8b05d65"
;;      #uuid "a31e1098-9892-4748-b0f3-ddeb523637d4"]
;;     [#uuid "e97df0d4-d3b1-47e5-9351-c5b7fa1431b0"
;;      #uuid "93c00e46-6234-40b3-8d2d-0fa8491e8d02"
;;      #uuid "0a002374-4f72-4260-ad6f-921e340bd6af"
;;      #uuid "a0f0c989-49b9-472b-8f60-a5d5549298cb"
;;      #uuid "b5033dc5-f0fc-4f5e-91b5-8518945f1c44"]
;;     [#uuid "4d50d50d-7929-469f-b021-cbff0a9e7882"
;;      #uuid "feb9d57d-b3ed-4923-b394-ab60c9e77de0"]]

;; my paginated API example (github app installation repositories)
(comment
  (def my-repos
    (let [my-installation-token "generate-jwt-and-get-installation-access-token-from-that"
          api (fn fetch-page [page-url]
                (log/debug "Fetch all installation repositories - page: " page-url)
                (let [response (http/get page-url {:accept "application/vnd.github.v3+json"
                                                   :headers {"Authorization" (str "Bearer " my-installation-token)}
                                                   :as :json
                                                   :query-params {:per_page "100"}})]
                  (log/debug "Fetch all installation repositories - page FINISHED: " page-url)
                  response))
          api-reducible (iteration api
                                   :kf #(get-in % [:links :next :href])
                                   :vf #(-> % :body :repositories)
                                   :initk "https://api.github.com/installation/repositories")]
      ;; flatten the pages
      (into [] cat api-reducible)))

  (last my-repos)
  ;; => {:html_url "https://github.com/empear-analytics/netbeans",
  ;;     :description "Apache NetBeans",
  ;;     ,,,}

  ,)

;; it would be nicer to have a basic clj-http specific wrapper for `iteration`:
(defn http-api-iteration
  "Simple wrapper on top of `iteration` for fetching from HTTP APIs via clj-http.core/get"
  [main-url http-get-params next-page-fn value-fn]
  (let [api (fn fetch-page [page-url]
              (log/debug "Fetch page: " page-url)
              (let [response (http/get page-url http-get-params)]
                (log/debug "Fetch page FINISHED: " page-url)
                response))
        api-reducible (iteration api :kf next-page-fn :vf value-fn :initk main-url)]
    api-reducible))
(comment
  (let [my-installation-token "generate-jwt-and-get-installation-access-token-from-that"
        api-reducible (http-api-iteration "https://api.github.com/installation/repositories"
                                          {:accept "application/vnd.github.v3+json"
                                           :headers {"Authorization" (str "Bearer " my-installation-token)}
                                           :as :json
                                           :query-params {:per_page "100"}}
                                          (fn [response] (get-in response [:links :next :href]))
                                          (fn [response] (get-in response [:body :repositories])))]
    (into [] cat api-reducible))
  ,)


;; just my experiment with IReduce and IReduceInit
(def myri (reify
            clojure.lang.IReduce
            (reduce [_ rf]
              (loop [acc 0]
                (if (< acc 10)
                  (recur (inc acc))
                  acc)))
            clojure.lang.IReduceInit
            (reduce [_ rf init]
              (loop [acc init]
                (if (< acc 10)
                  (recur (inc acc))
                  acc)))))
(reduce identity myri)
;; => 10
(reduce identity 13 myri)
;; => 13


;; try medley.core/find-first and how it performs
(m/find-first pos? (range -10000 2))
;; => 1
(comment
  (require '[criterium.core :as crit])
  (crit/quick-bench (m/find-first pos? (range -10000 2)))
  ;; Execution time mean : 54.729040 µs

  (crit/quick-bench (first (filter pos? (range -10000 2))))
  ;; Execution time mean : 166.094125 µs


  ,)
