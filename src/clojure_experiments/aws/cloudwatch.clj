(ns clojure-experiments.aws.cloudwatch
  (:require [clj-http.client :as http]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

;;;; https://aws.amazon.com/cloudwatch/faqs/
;;;; https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/cloudwatch_architecture.html
;;;; https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/cloudwatch_concepts.html


;;; CloudWatch pricing
;;; https://a0.awsstatic.com/pricing/1/deprecated/cloudwatch/pricing-cloudwatch.json

(defn get-prices []
  (let [response-data
        (http/get "https://a0.awsstatic.com/pricing/1/deprecated/cloudwatch/pricing-cloudwatch.json"
                  {:as :json})
        raw-data (:body response-data)
        ]
    {:raw-data raw-data
     :data (-> raw-data :config :regions)}
    ))

(s/def ::name string?)
(s/def ::rate string?)
(s/def ::currency keyword?)
(s/def ::nominal-value string?)
(s/def ::price (s/map-of ::currency ::nominal-value))
(s/def ::prices (s/coll-of ::price))
(s/def ::value (s/keys :req-un [::rate ::prices]))
(s/def ::values (s/coll-of ::value))
(s/def ::region-prices-types (s/coll-of (s/keys :req-un [::name ::values])))
(s/def ::types ::region-prices-types)

(s/fdef region-prices
  :args (s/cat :prices (s/keys :req-un [::data]))
  :ret (s/keys :req-un [::region ::types]))
(defn region-prices
  [prices region-name]
  (first
   (filter #(= region-name (:region %))
           (:data prices))))


(s/fdef region-log-prices)
(defn region-log-prices [region-prices]
  (first )
  (-> region-prices :types ))

(comment

  (def cw-prices (get-prices))

  (def eu-west-1-prices (region-prices cw-prices "eu-west-1"))
  (-> eu-west-1-prices :types)
  ;; logs:
  { :rate "perGBIngested", :prices { :USD "0.57" } }
  { :rate "perGBArchivedMo", :prices { :USD "0.03" } }


  ;; 
  )
