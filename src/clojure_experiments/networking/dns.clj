(ns clojure-experiments.networking.dns
  (:import (java.net InetAddress)))

;;; Hostname resolution in Java: https://maheshsenniappan.medium.com/host-name-resolution-in-java-80301fea465a
(InetAddress/getByName "codescene.io")
;; => #object[java.net.Inet4Address 0x3f90af53 "codescene.io/143.204.98.12"]

(seq (InetAddress/getAllByName "codescene.io"))
;; => (#object[java.net.Inet4Address 0x5b38cb3c "codescene.io/13.32.99.27"]
;;     #object[java.net.Inet4Address 0x4caec28 "codescene.io/13.32.99.7"]
;;     #object[java.net.Inet4Address 0x6809b216 "codescene.io/13.32.99.112"]
;;     #object[java.net.Inet4Address 0xf948708 "codescene.io/13.32.99.85"])

;; DNS caching defaults
;; (sun.net.InetAddressCachePolicy/getNegative) ; IllegalAccessError
;; On JDK 8 you could get this:
;; (sun.net.InetAddressCachePolicy/getNegative)
;; => 10
;; (sun.net.InetAddressCachePolicy/get)
;; => 30


;; I'm really puzzled by this negative cache 10 seconds!
;; the properties ar enull and I cannot see anywhere in JDK 8 or 17 code that 10 seconds would be used
(System/getProperty "networkaddress.cache.negative.ttl")
;; => nil
(System/getProperty "sun.net.inetaddr.negative.ttl")
;; => nil

(java.security.Security/getProperty "networkaddress.cache.negative.ttl")
;; => "10"
(java.security.Security/getProperty "networkaddress.cache.ttl")
;; => nil
