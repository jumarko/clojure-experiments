(ns clojure-experiments.courses.lambdaisland.048-for
  "https://lambdaisland.com/episodes/list-comprehension-clojure-for"
  (:require [clojure.string :as str]))

;;; Intro
(for [x (range 2 7)]
  x)
;; => (2 3 4 5 6)


(map inc (range 5))
;; => (1 2 3 4 5)


;; run! is not very useful without side effects
(run! inc (range 5))
;; => nil

;; check the console!
(run! println (range 5))
;; => nil

;; you cannot use map println in the same way
(do
  (map println (range 5))
  nil)
;; you could use `mapv` but it would be surprising for readers



;;; Destructuring
(defn url-parts [url]
  (next (re-matches #"(\w+)://([^/]+)(.*)" url)))

(url-parts "https://lambdaisland.com/episodes")
;; => ("https" "lambdaisland.com" "/episodes")

(let [parts (url-parts "https://lambdaisland.com/episodes")]
  {:scheme (nth parts 0)
   :domain (nth parts 1)
   :path (nth parts 2)
   })
;; => {:scheme "https", :domain "lambdaisland.com", :path "/episodes"}

;; you can instead do this
(defn url-map [url]
  (let [[scheme domain path] (url-parts "https://lambdaisland.com/episodes")]
   {:scheme scheme
    :domain domain
    :path path}))
(def urls ["http://gaiwan.co" "http:///lambdaisland.com" "http://clojureverse.org/"])
(for [{:keys [scheme domain]} (map url-map urls)]
  (str scheme "://" domain "/favicon.ico"))
;; => ("https://lambdaisland.com/favicon.ico" "https://lambdaisland.com/favicon.ico" "https://lambdaisland.com/favicon.ico")


;;; Examples
(defn keywordize-map-keys [m]
  (into {}
        (for [[k v] m]
          [(keyword k) v])))
(keywordize-map-keys {"lamda" "island"})
;; => {:lamda "island"}

;; alternatively using point-free style
(defn keywordize-map-keys2 [m]
  (into {}
        (map (juxt (comp keyword key) val))
        m))
(keywordize-map-keys2 {"lamda" "island"})
;; => {:lamda "island"}


;;; Cartesian product
;; binding vector can contain multiple elements
(for [x (range 2)
      y [:a :b]]
  [x y])
;; => ([0 :a] [0 :b] [1 :a] [1 :b])

(def board
  [[:R :N :B :K :Q :B :N :R]
   [:P :P :P :P :P :P :P :P]
   [:_ :_ :_ :_ :_ :_ :_ :_]
   [:_ :_ :_ :_ :_ :_ :_ :_]
   [:_ :_ :_ :_ :_ :_ :_ :_]
   [:_ :_ :_ :_ :_ :_ :_ :_]
   [:p :p :p :p :p :p :p :p]
   [:r :r :b :k :q :b :n :r]
   ])
(for [x (range 8)
      y (range 8)]
  [(str (get "ABDEFGH" y) x)
   (get-in board [x y])])
;; => (["A0" :R]
;;     ["B0" :N]
;;     ["D0" :B]
;;     ["E0" :K]
;;     ["F0" :Q]
;;     ["G0" :B]
;;     ["H0" :N]
;;     ["0" :R]
;;     ["A1" :P]
;; ...


;;; cartesian products are not that common
;;; BUT processing nested collections producing "flat result" is fairly common
(into #{}
      (for [ns (all-ns)
            var (vals (ns-publics ns))
            k (keys (meta var))]
        k))
;; => #{:redef :no-doc :private :protocol :added :ns :name :special-form :file :potemkin/body :static
;;      :inline-arities :skip-wiki :nrepl.middleware/descriptor :column :const :dynamic :line :macro
;;      :deprecated :declared :url :style/indent :tag :arglists :see-also :doc :forms :inline}



;;; Using "switches" (:let, :when, :while)

(for [x (range 8)
      y (range 8)
      :let [pos (str (get "ABDCDEFG" y) (inc x))
            piece (get-in board [x y])]
      :when (#{:K :k :Q :q} piece)]
  [pos piece])
;; => (["C1" :K] ["D1" :Q] ["C8" :k] ["D8" :q])


;; find all metadata keys in clojure.core which has string as their value
(into #{}
      (for [ns (all-ns)
            :let [n (str (ns-name ns))]
            :when (str/starts-with? n "clojure")
            var (vals (ns-publics ns))
            [k v] (meta var)
            :when (string? v)]
        k))
;; => #{:added :file :deprecated :tag :doc}




