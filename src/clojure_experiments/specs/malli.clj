(ns clojure-experiments.specs.malli
  "https://github.com/metosin/malli"
  (:require
   [malli.core :as m]
   [malli.generator :as mg]))


;;; Malli getting started - best library to work with schemas in Clojure:
;;; https://www.youtube.com/watch?v=n6OeKHm4BSQ
;;; The video uses the example from Malli readme - getting started: https://github.com/metosin/malli?tab=readme-ov-file#quickstart
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def UserId :string)

(def Address
  [:map
   [:street :string]
   [:country [:enum "FI" "UA"]]])

(def User
  [:map
   [:id #'UserId]
   [:address #'Address]
   [:friends [:set {:gen/max 2} [:ref #'User]]]])

(mg/generate User)
;; => {:id "9N99F0fiFktexF1131cWMt",
;;     :address {:street "7WicZr4wvG1VBm20fCs0Axds", :country "UA"},
;;     :friends
;;     #{{:id "U8",
;;        :address {:street "71", :country "FI"},
;;        :friends
;;        #{{:id "",
;;           :address {:street "", :country "UA"},
;;           :friends
;;           #{{:id "RBn", :address {:street "C", :country "FI"}, :friends #{}}
;;             {:id "PJ",
;;              :address {:street "Y", :country "UA"},
;;              :friends
;;              #{{:id "9239qdDG8",
;;                 :address {:street "Ky1iYpX346WbYZ7t", :country "UA"},
;;                 :friends #{}}}}}}
;;          {:id "0",
;;           :address {:street "53", :country "UA"},
;;           :friends
;;           #{{:id "", :address {:street "69", :country "UA"}, :friends #{}}
;;             {:id "k",
;;              :address {:street "x4", :country "UA"},
;;              :friends
;;              #{{:id "245868e1J0e4mXO5XMEiGPQVX",
;;                 :address {:street "77k3iZX8E32YlnHB86Gbc9r", :country "FI"},
;;                 :friends #{}}
;;                {:id "H7J71A1Z97M3",
;;                 :address {:street "8HoD8vgioMfpGr2gOmM4", :country "UA"},
;;                 :friends #{}}}}}}}}}}
(m/validate User *1)
