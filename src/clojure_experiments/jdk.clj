(ns clojure-experiments.jdk
  (:import (org.openjdk.jol.info ClassLayout GraphLayout)))

;;; jol - Java Object Layout tool: http://openjdk.java.net/projects/code-tools/jol/
;;; http://central.maven.org/maven2/org/openjdk/jol/jol-cli
;;; org.openjdk.jol/jol-core
;;; http://www.mastertheboss.com/jboss-server/jboss-monitoring/monitoring-the-size-of-your-java-objects-with-java-object-layout



(println (.toPrintable (ClassLayout/parseInstance (make-array Integer/TYPE 128 2))))
;; [[I object internals:
;;   OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
;;   0     4        (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
;;   4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
;;   8     4        (object header)                           01 0e 22 00 (00000001 00001110 00100010 00000000) (2231809)
;;   12     4        (object header)                           80 00 00 00 (10000000 00000000 00000000 00000000) (128)
;;   16   512     [I [[I.<elements>                            N/A
;;                     Instance size: 528 bytes
;;                     Space losses: 0 bytes internal + 0 bytes external = 0 bytes total


;; (println (.toPrintable (GraphLayout/parseInstance (make-array Integer/TYPE 128 2))))
(println (.toFootprint (GraphLayout/parseInstance (make-array Integer/TYPE 128 2))))
;; COUNT       AVG       SUM   DESCRIPTION
;;   128        24      3072   [I
;; => 3600 bytes in total


;; compare to more efficient solution using int[256] array
(println (.toPrintable (ClassLayout/parseInstance (make-array Integer/TYPE 256))))
 ;; [I object internals:
 ;; OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
 ;; 0     4        (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
 ;; 4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
 ;; 8     4        (object header)                           38 97 07 00 (00111000 10010111 00000111 00000000) (497464)
 ;; 12     4        (object header)                           00 01 00 00 (00000000 00000001 00000000 00000000) (256)
 ;; 16  1024    int [I.<elements>                             N/A
 ;;                  Instance size: 1040 bytes
 ;;                  Space losses: 0 bytes internal + 0 bytes external = 0 bytes total


;; empty array
(println (.toPrintable (ClassLayout/parseInstance (int-array []))))
;; [I object internals:
;;  OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
;;  0     4        (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
;;  4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
;;  8     4        (object header)                           38 97 07 00 (00111000 10010111 00000111 00000000) (497464)
;;  12     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
;;  16     0    int [I.<elements>                             N/A
;;                   Instance size: 16 bytes
;;                   Space losses: 0 bytes internal + 0 bytes external = 0 bytes total
