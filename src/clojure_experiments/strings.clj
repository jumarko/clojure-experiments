(ns clojure-experiments.strings
  "Experiments with strings, encoding, surrogate pairs, etc.")


;;; Motivated by message Clojurians slack - message by Sky Higgins: https://clojurians.slack.com/archives/C053AK3F9/p1658699721494909?thread_ts=1658652699.188059&cid=C053AK3F9
;; Java stores strings in UTF-16  (ignoring under-the hood optimizations). 
;; When count delegates to  .length, you get the number of UTF-16 code points in your string. 
;; * if all your code points will stay within U+0000-U+D7FF and U+E000-U+FFFD (think Latin-1 charset), then you can consider that the same as counting "chararcters".
;; * More likely, you will need to deal with surrogate pairs, where the the first of two points names a lookup table from which to interpret the second. 
;; * So the count of code points might be wildly different from your intuitive expectation
;;     * his Applies to all the emojis, to full support of most non-English languages, and to any support at all for most non-European languages. 
;;     * The complication goes even deeper, as many interpreted surrogate pairs still function merely as modifiers for other characters, e.g. diacritics or emoji skin tone colors... plus numerous other complications arising from different writing systems around the world.

(count "ahoj")
;; => 4
(count "áhôj")
;; => 4
;; ... all is good so far - but now it gets weird - 5 instead of 4!
(count "aho🚩")
;; => 5


;;; https://lambdaisland.com/blog/2017-06-12-clojure-gotchas-surrogate-pairs
;;; This is an article and offers `char-seq` function which makes the count correct again
(defn char-code-at [str pos]
  #?(:clj (.charAt str pos)
     :cljs (.charCodeAt str pos)))

(defn char-seq
  "Return a seq of the characters in a string, making sure not to split up
  UCS-2 (or is it UTF-16?) surrogate pairs. Because JavaScript. And Java."
  ([str]
   (char-seq str 0))
  ([str offset]
   (if (>= offset (count str))
     ()
     (let [code (char-code-at str offset)
           width (if (<= 0xD800 (int code) 0xDBFF) 2 1)] ; detect "high surrogate"
       (cons (subs str offset (+ offset width))
             (char-seq str (+ offset width)))))))
(count (char-seq "aho🚩"))
;; => 4


;;; Odd problem with Large strings with Unicode characters
;;; .getBytes() will throw NegativeArraySizeException
;;; .getBytes returning UTF-8 encoded string as byte array.
;;;  The UTF-16 to UTF-8 encoder will allocate an array of 3*n where n is number of characters
;;;  in a string (so half of size of existing UTF-16 bytes).
;;; It is not possible to get UTF-8 bytes of a string larger than 715827882 characters
;;; if there are any non-ascii characters in it.
;;; This affects all such operations in JVM
;;; Example where it can manifest: Storing Linux's git log in memory.
(comment

  ;; if you have enough memory, this will throw NegativeArraySizeException
  (let [x (String. (char-array (repeat 1000000 \u010d)))
        s (StringBuilder. (* 800 (.length x)))]
    ;; if you change 716 to 715 it will pass
    (dotimes [n 716] (.append s x))
    (.getBytes (str s)))
  ;;=>
  ;; 1. Unhandled java.lang.NegativeArraySizeException
  ;; -1894967296
  ;; String.java: 1298  java.lang.String/encodeUTF8_UTF16
  ;; String.java: 1274  java.lang.String/encodeUTF8
  ;; String.java:  847  java.lang.String/encode
  ;; String.java: 1811  java.lang.String/getBytes
  ;; REPL:   57  clojure-experiments.strings/eval25912



  .)


