(ns clojure-experiments.security.keystore
  (:import (javax.crypto KeyGenerator)
           (java.security KeyStore
                          KeyStore$SecretKeyEntry
                          KeyStore$PasswordProtection)))

;; https://stackoverflow.com/questions/71754646/java-saving-a-des-key-using-keystore

(comment
  ;; https://docs.oracle.com/javase/7/docs/api/javax/crypto/KeyGenerator.html
  (def my-kg (KeyGenerator/getInstance "DES"))
  (def my-key (.generateKey my-kg))

  ;; https://docs.oracle.com/javase/7/docs/api/java/security/KeyStore.html
  (def my-keystore (KeyStore/getInstance (KeyStore/getDefaultType)))
  (.load my-keystore nil nil) ; passing nil input stream to create a new KeyStore

  (def my-password (char-array "changeit"))

  ;; save the key in the keystore
  (def my-key-entry (KeyStore$SecretKeyEntry. my-key))
  (def my-key-alias "myKeyAlias")
  (def my-key-password (KeyStore$PasswordProtection. my-password))
  (.setEntry my-keystore my-key-alias my-key-entry my-key-password)

  ;; save the keystore
  (def keystore-path "/Users/jumar/my-key-store.jks")
  (.store my-keystore (java.io.FileOutputStream. keystore-path) my-password)

  ;; now read the key from the saved keystore
  (def read-keystore (KeyStore/getInstance (KeyStore/getDefaultType)))
  (.load read-keystore (java.io.FileInputStream. keystore-path) my-password)
  (.getEntry read-keystore my-key-alias my-key-password)
  ;; => #object[java.security.KeyStore$SecretKeyEntry 0x58b80b62 "Secret key entry with algorithm DES/CBC"]

,)
