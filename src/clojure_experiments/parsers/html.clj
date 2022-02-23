(ns clojure-experiments.parsers.html
  (:require [clj-http.client :as http]
            [hickory.core :as h]
            [hickory.select :as hs]
            [clojure.spec.alpha :as s]
            )
  )


(defn download-as-hickory [url]
  (-> (http/get url)
      :body
      ;; https://github.com/davidsantiago/hickory
      (h/parse)
      (h/as-hickory)))


(comment
  (def vat-response (-> (http/post "https://ec.europa.eu/taxation_customs/vies/vatResponse.html"
                                   {:form-params {"memberStateCode" "CZ"
                                                  "number" "8507038320"}})
                        :body
                        ;; https://github.com/davidsantiago/hickory
                        (h/parse)
                        (h/as-hickory)
                        ))

  ;;; Demo selectors: https://github.com/davidsantiago/hickory#selectors
  (def five-km-around-sulov (download-as-hickory "https://reality.bazos.sk/?hledat=sulov&rubriky=reality&hlokalita=01352&humkreis=5&cenaod=&cenado=&Submit=H%C4%BEada%C5%A5&kitx=ano"))

  (->> (hs/select (hs/child
                 ;; TODO get both "nadpis" and "popis"
                   (hs/or (hs/class "nadpis")
                          (hs/class "popis"))) five-km-around-sulov)
       (partition 2)
       first
       (mapv (comp first :content))
       )
;; => [{:type :element,
;;      :attrs {:href "/inzerat/134684646/prenajom-pozemku.php"},
;;      :tag :a,
;;      :content ["Prenájom pozemku"]}
;;     "Ponúkam na prenájom slnečný pozemok v rekreačnej oblasti obce Súľov vhodný pre karavan, mobilný dom a pod. El. prípojka je k dispozícií. Viac informácií prostredníctvom e-mailu."]
  ,)


;; parse codescene onprem version from docs
(comment
  (def codescene-docs (download-as-hickory "https://docs.enterprise.codescene.io/"))
  (->> (hs/select (hs/child (hs/tag :td)) codescene-docs)
       first
       :content
       first)
  ;; => "5.1.6"
  ,)

