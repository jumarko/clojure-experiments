(ns clojure-experiments.networking.spotify
  "Experiments with Spotify API: https://developer.spotify.com/documentation/general/guides/authorization/app-settings/
  Motivated by https://stackoverflow.com/questions/73113690/clojure-spotify-api-get-creds"
  (:require [clj-http.client :as http]))


;;; Client credentials flow: https://developer.spotify.com/documentation/general/guides/authorization/client-credentials/

(defn get-token [{:keys [id secret] :as creds}]
  (:body (http/post "https://accounts.spotify.com/api/token"
              {#_#_:content-type :x-www-form-urlencoded
               :as :json
               :basic-auth [id secret]
               :form-params {"grant_type" "client_credentials"}})))

(comment
  (get-token {:id "xxx"
              :secret "yyy"})
  ;; => {:access_token
  ;;     "BQB...",
  ;;     :token_type "Bearer",
  ;;     :expires_in 3600}

  ;; When I got 400 response, it was this: 
  ;;    "{\"error\":\"unsupported_grant_type\",\"error_description\":\"grant_type parameter is missing\"}",

  .)
