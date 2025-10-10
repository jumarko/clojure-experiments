(ns clojure-experiments.books.clojure-brain-teasers.10-off-record
  "While records may have additional fields, they are guaranteed to have the specified
  fields, and their lookup is optimized for performance.")


(defrecord Album [name artist])

(def news-of-the-world
  (->Album "News of the World" "Queen"))

;; disoccing a field from a record makes it a map
(instance? Album (dissoc news-of-the-world :artist))
;; => false
(type (dissoc news-of-the-world :artist))
;; => clojure.lang.PersistentArrayMap
(type news-of-the-world)
;; => clojure_experiments.books.clojure_brain_teasers.10_off_record.Album

;; compare to assoc
(assoc news-of-the-world :year 2025)
;; => #clojure_experiments.books.clojure_brain_teasers.10_off_record.Album{:name "News of the World", :artist "Queen", :year 2025}

;; but dissocing the extra field is OK -> preserves the record type
(dissoc (assoc news-of-the-world :year 2025) :year)
;; => #clojure_experiments.books.clojure_brain_teasers.10_off_record.Album{:name "News of the World", :artist "Queen"}

