(ns clojure-experiments.security.hashes
  "Things like SHA256 et al."
  (:require [buddy.core.codecs :as codecs])
  (:import (java.security MessageDigest)))


;;; SHA256
;; https://marco.dev/blockchain-256
(defn sha-256
  "Computes SHA256 of given string.
  Returns the hash as a BASE64-encoded string using UTF-8 encoding.

  Tip: try to compare with `sha256sum` command line tool."
  [s]
  (let [md (MessageDigest/getInstance "SHA-256")
        charset (java.nio.charset.Charset/defaultCharset)
        _ (.update md (.getBytes s charset))
        sha-bytes (.digest md)]
    (codecs/bytes->str (codecs/bytes->b64u sha-bytes)
                       (str charset))))

(sha-256 "zookeeper-server/src/main/java/org/apache/zookeeper/server/quorum/QuorumCnxManager.java")
;; => "_oSbvf9IhXu_1xpgOBk-_VxIkRAjIq3c2cR-frmteTk"
