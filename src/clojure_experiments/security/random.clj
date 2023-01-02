(ns clojure-experiments.security.random
  "Experiments with random number genreation, notably cryptographically secure.")

;;; java.util.UUID/randomUUID can be used as a source of securely generated random numbers
;;; however, you can also get shorter identifiers by using SecureRandom directly
;;; Further resources
;;; - https://stackoverflow.com/questions/39786902/uuid-randomuuid-vs-securerandom
;;; - https://stackoverflow.com/questions/11026061/is-uuid-randomuuid-suitable-for-use-as-a-one-time-password
;;; - https://www.coalfire.com/the-coalfire-blog/appsec-concerns-uuid-generation
;;; - https://news.ycombinator.com/item?id=10631806

(def ^:private encoder (.withoutPadding (java.util.Base64/getUrlEncoder)))
(defn base64-url [bytes]
  (.encodeToString encoder bytes))

;; inspired by java.util.UUID.Holder class
(def ^:private number-generator (delay (java.security.SecureRandom.)))
(defn random-id
  "Generates a new random (string) identifier using, by default, 20 bytes of randomness.
  Uses `java.util.SecureRandom` under the hood."
  ([]
   (random-id 20))
  ([n-bytes]
   (let [bytes (byte-array n-bytes)]
     (.nextBytes @number-generator bytes)
     (base64-url bytes))))

(random-id)
;; => "xKK9Lf5OYO7f6rWk2L4yz8bTDEw"
(random-id 16)
;; => "fGCu2Yn6vlJBXhpmzJX65g"

;; compare to random-uuid
(random-uuid)
;; => "2f04c35b-963a-4f3b-937d-e3aa57ebe1fb"

;;=> random-id produces significantly shorter yet more secure random identifiers/strings!


