(ns clojure-experiments.parsers.antlr
  "Using clj-antlr https://github.com/aphyr/clj-antlr"
  (:require
   [clj-antlr.core :as antlr]))


;;; https://github.com/aphyr/clj-antlr?tab=readme-ov-file#usage
(def json (antlr/parser "../clj-antlr/grammars/Json.g4"))
(json "[1,2,3]")
;; => (:jsonText
;;     (:jsonArray
;;      "["
;;      (:jsonValue (:jsonNumber "1"))
;;      ","
;;      (:jsonValue (:jsonNumber "2"))
;;      ","
;;      (:jsonValue (:jsonNumber "3"))
;;      "]"))

;; error details
(try (json "[1,2,,3,]") (catch clj_antlr.ParseError e (clojure.pprint/pprint @e)));; => 
;; ({:symbol
;;   #object[org.antlr.v4.runtime.CommonToken 0x5b62dd2a "[@5,5:5=',',<5>,1:5]"],
;;   :line 1,
;;   :char 5,
;;   :message
;;   "extraneous input ',' expecting {'false', 'null', 'true', '{', '[', NUMBER, STRING}"}
;;  {:symbol
;;   #object[org.antlr.v4.runtime.CommonToken 0x68a326a2 "[@8,8:8=']',<9>,1:8]"],
;;   :line 1,
;;   :char 8,
;;   :message
;;   "mismatched input ']' expecting {'false', 'null', 'true', '{', '[', NUMBER, STRING}",
;;   :rule
;;   #object[org.antlr.v4.tool.GrammarInterpreterRuleContext 0xfc22286 "[51 15]"],
;;   :state 25,
;;   :expected
;;   #object[org.antlr.v4.runtime.misc.IntervalSet 0x3a44ff89 "{1..4, 8, 10, 12}"],
;;   :token
;;   #object[org.antlr.v4.runtime.CommonToken 0x68a326a2 "[@8,8:8=']',<9>,1:8]"]})


;;; CodeScene grammars

(def java-micro (antlr/parser "/Users/jumar/workspace/CODESCENE/CODE/codescene/analysis/src/antlr/hotspots_x_ray/languages/generated/JavaMicro.g4"))
(java-micro "public static void main() {}")
