(ns clojure-experiments.datomic.datascript
  "DataScript demo.
  See also https://github.com/vvvvalvalval/datascript-declarative-model-example/blob/master/src/datascript_demo.clj#L1
 http://vvvvalvalval.github.io/posts/datascript-as-a-lingua-franca-for-domain-modeling.html"
  (:require [datascript.core :as dt]))

;;;; http://vvvvalvalval.github.io/posts/datascript-as-a-lingua-franca-for-domain-modeling.html
;;;; https://github.com/vvvvalvalval/datascript-declarative-model-example/blob/master/src/datascript_demo.clj#L1


(def db-schema
  {:person/name {:db/unique :db.unique/identity}
   :movie/title {:db/unique :db.unique/identity}
   :movie/director {:db/valueType :db.type/ref
                    :db/cardinality :db.cardinality/one}
   :movie/actors {:db/valueType :db.type/ref
                  :db/cardinality :db.cardinality/many}})

(def empty-db (dt/db (dt/create-conn db-schema)))

;;; write some data
(def db
  (dt/db-with
   empty-db
   ;; This data structure below is a DataScript 'write', or 'transaction request'.
   ;; It is composed of maps, each map grouping several facts about an entity.
   ;; ***Notice how several facts about an entity can be spread over several maps and
   ;; will be automatically reconciled***: for example, facts about the movie "The Good, the Bad and The Ugly"
   ;; are specified in 3 different maps.
   [{:movie/title "Star Wars: a New Hope"
     ;; notice that `release-year` is not defined in schema (probably because it's neither identifier nor reference)
     :movie/release-year 1977
     :movie/director {:person/name "George Lucas"}
     :movie/actors [{:person/name "Carrie Fisher"}
                    {:person/name "Harrison Ford"
                     :person/gender :gender/male}]}
    {:person/name "George Lucas"
     :person/gender :gender/male}
    {:person/name "Carrie Fisher"
     :person/gender :gender/female}
    {:movie/title "The Good, the Bad and The Ugly"
     :movie/release-year 1966}
    {:movie/title "The Good, the Bad and The Ugly"
     :movie/director {:person/name "Sergio Leone"}}
    {:movie/title "Gran Torino"
     :movie/release-year 2008}
    {:person/name "Clint Eastwood"
     ;; If you precede an attribute's local name with an underscore (_), get will navigate backwards,
     ;; returning the Set of entities that point to the current entity
     ;; https://docs.datomic.com/on-prem/entities.html
     :movie/_director [{:movie/title "Gran Torino"}]
     :movie/_actors [{:movie/title "The Good, the Bad and The Ugly"}]}]))

;;; querying
(comment

  ;; the whole db content
  db

  ;; Find the names of all persons in db
  (dt/q
   '[:find [?name ...]
     :where [?p :person/name ?name]]
   db)

  ;; Find the names of all actors in DB
  ;; three dots mins "collection": https://docs.datomic.com/on-prem/query.html#find-specifications
  ;; see also binding: https://docs.datomic.com/on-prem/query.html#collection-binding
  (dt/q
   '[:find [?name ...]
     :where
     [?m :movie/actors ?p]
     [?p :person/name ?name]]
   db)

  ;; what do we know about the Star Wars movie?
  (def star-wars (dt/entity db [:movie/title "Star Wars: a New Hope"])) ; Entity API

  ;; notice that entity is lazy
  star-wars (:movie/title star-wars)

  (:movie/release-year star-wars)

  (dt/touch star-wars)

  ;; who's the directory
  (-> star-wars :movie/director dt/touch)

  ;; final all movies released after 1970
  (->>
   (dt/q
    '[:find [?m ...]
      :in $ ?time
      :where
      [?m :movie/release-year ?t]
      [(> ?t ?time)]]
    db
    1970)
   (map #(dt/entity db %))
   (sort-by :movie/release-year)
   (mapv dt/touch))

  )



;;; Model meta-data
;; These 2 values are DataScript Transaction Requests, i.e data structures defining writes to a DataScript database
;; NOTE in a real-world codebase, these 2 would typically live in different files.
(def user-model
  [{:twitteur.entity-type/name :twitteur/User
    :twitteur.schema/doc "a User is a person who has signed up to Twitteur."
    :twitteur.entity-type/attributes
    [{:twitteur.attribute/name :user/id
      :twitteur.schema/doc "The unique ID of this user."
      :twitteur.attribute/ref-typed? false
      :twitteur.attribute.scalar/type :uuid
      :twitteur.attribute/unique-identity true}
     {:twitteur.attribute/name :user/email
      :twitteur.schema/doc "The email address of this user (not visible to other users)."
      :twitteur.attribute/ref-typed? false
      :twitteur.attribute.scalar/type :string
      :twitteur.attribute.security/private? true}                    ;; here's a domain-specific security rule
     {:twitteur.attribute/name :user/name
      :twitteur.schema/doc "The public name of this user on Twitteur."
      :twitteur.attribute/ref-typed? false
      :twitteur.attribute.scalar/type :string}
     {:twitteur.attribute/name :user/follows
      :twitteur.schema/doc "The Twitteur users whom this user follows."
      :twitteur.attribute/ref-typed? true                            ;; this attribute is a reference-typed
      :twitteur.attribute.ref-typed/many? true
      :twitteur.attribute.ref-typed/type {:twitteur.entity-type/name :twitteur/User}}
     {:twitteur.attribute/name :user/n_followers
      :twitteur.schema/doc "How many users follow this user."
      :twitteur.attribute/ref-typed? false
      :twitteur.attribute.ref-typed/many? true
      :twitteur.attribute.scalar/type :long
      :twitteur.attribute/derived? true}                             ;; this attribute is not stored in DB
     {:twitteur.attribute/name :user/tweets
      :twitteur.schema/doc "The tweets posted by this user."
      :twitteur.attribute/ref-typed? true
      :twitteur.attribute.ref-typed/many? true
      :twitteur.attribute.ref-typed/type {:twitteur.entity-type/name :twitteur/Tweet}
      :twitteur.attribute/derived? true}
     ]}])

(def tweet-model
  ;; NOTE: to demonstrate the flexibility of DataScript, we choose a different but equivalent data layout
  ;; in this one, we define the Entity Type and the Attributes separately
  [;; Entity Type
   {:twitteur.entity-type/name :twitteur/Tweet
    :twitteur.schema/doc "a Tweet is a short message posted by a User on Twitteur, published to all her Followers."
    :twitteur.entity-type/attributes
    [{:twitteur.attribute/name :tweet/id}
     {:twitteur.attribute/name :tweet/content}
     {:twitteur.attribute/name :tweet/author}
     {:twitteur.attribute/name :tweet/time}]}
   ;; Attributes
   {:twitteur.attribute/name :tweet/id
    :twitteur.schema/doc "The unique ID of this Tweet"
    :twitteur.attribute/ref-typed? false
    :twitteur.attribute.scalar/type :uuid
    :twitteur.attribute/unique-identity true}
   {:twitteur.attribute/name :tweet/content
    :twitteur.schema/doc "The textual message of this Tweet"
    :twitteur.attribute/ref-typed? false
    :twitteur.attribute.scalar/type :string}
   {:twitteur.attribute/name :tweet/author
    :twitteur.schema/doc "The Twitteur user who wrote this Tweet."
    :twitteur.attribute/ref-typed? true
    :twitteur.attribute.ref-typed/many? false
    :twitteur.attribute.ref-typed/type {:twitteur.entity-type/name :twitteur/User}}
   {:twitteur.attribute/name :tweet/time
    :twitteur.schema/doc "The time at which this Tweet was published, as a timestamp."
    :twitteur.attribute/ref-typed? false
    :twitteur.attribute.scalar/type :long}])

;;; merge it
;;; Writing this metadata to a DataScript db
(def meta-schema
  {:twitteur.entity-type/name {:db/unique :db.unique/identity}
   :twitteur.entity-type/attributes {:db/valueType :db.type/ref
                                     :db/cardinality :db.cardinality/many}
   :twitteur.attribute/name {:db/unique :db.unique/identity}
   :twitteur.attribute.ref-typed/type {:db/valueType :db.type/ref
                                       :db/cardinality :db.cardinality/one}})

(defn empty-model-db
  []
  (let [conn (dt/create-conn meta-schema)]
    (dt/db conn)))

(def model-db
  "A DataScript database value, holding a representation of our Domain Model."
  (dt/db-with
   (empty-model-db)
   ;; Composing DataScript transactions is as simple as that: concat
   (concat
    user-model
    tweet-model)))


;;;; Let's query this a bit
(comment
  ;; What are all the attributes names in our Domain Model ?
  (sort
    (dt/q
      '[:find [?attrName ...] :where
        [?attr :twitteur.attribute/name ?attrName]]
      model-db))
  => (:tweet/author :tweet/content :tweet/id :tweet/time :user/email :user/follows :user/id :user/n_followers :user/name)

  ;; What do we know about :tweet/author?
  (def tweet-author-attr
    (dt/entity model-db [:twitteur.attribute/name :tweet/author]))

  tweet-author-attr
  => {:db/id 10}

  (dt/touch tweet-author-attr)
  =>
  {:twitteur.schema/doc "The Twitteur user who wrote this Tweet.",
   :twitteur.attribute/name :tweet/author,
   :twitteur.attribute/ref-typed? true,
   :twitteur.attribute.ref-typed/many? false,
   :twitteur.attribute.ref-typed/type {:db/id 1},
   :db/id 10}

  (-> tweet-author-attr :twitteur.attribute.ref-typed/type dt/touch)
  =>
  {:twitteur.schema/doc "a User is a person who has signed up to Twitteur.",
   :twitteur.entity-type/attributes #{{:db/id 4} {:db/id 6} {:db/id 3} {:db/id 2} {:db/id 5}},
   :twitteur.entity-type/name :twitteur/User,
   :db/id 1}

  ;; What attributes have type :twitteur/User?
  (dt/q '[:find ?attrName ?to-many? :in $ ?type :where
          [?attr :twitteur.attribute.ref-typed/type ?type]
          [?attr :twitteur.attribute/name ?attrName]
          [?attr :twitteur.attribute.ref-typed/many? ?to-many?]]
    model-db [:twitteur.entity-type/name :twitteur/User])
  => #{[:tweet/author false] [:user/follows true]}

  ;; What attributes are derived, and therefore should not be stored in the database?
  (->>
    (dt/q '[:find [?attr ...] :where
            [?attr :twitteur.attribute/derived? true]]
      model-db)
    (map #(dt/entity model-db %))
    (sort-by :twitteur.attribute/name)
    (mapv dt/touch))
  =>
  [{:twitteur.schema/doc "The tweets posted by this user.",
    :twitteur.attribute/derived? true,
    :twitteur.attribute/name :user/follows,
    :twitteur.attribute/ref-typed? true,
    :twitteur.attribute.ref-typed/many? true,
    :twitteur.attribute.ref-typed/type {:db/id 7},
    :db/id 5}
   {:twitteur.schema/doc "How many users follow this user.",
    :twitteur.attribute/derived? true,
    :twitteur.attribute/name :user/n_followers,
    :twitteur.attribute/ref-typed? false,
    :twitteur.attribute.ref-typed/many? true,
    :twitteur.attribute.scalar/type :long,
    :db/id 6}]

  ;; What attributes are private, and therefore should not be exposed publicly?
  (set
    (dt/q '[:find [?attrName ...] :where
            [?attr :twitteur.attribute.security/private? true]
            [?attr :twitteur.attribute/name ?attrName]]
      model-db))
  => #{:user/email}
  )
