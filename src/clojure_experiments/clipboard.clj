(ns clojure-experiments.clipboard
  "Utilities for accessing system clipboard.")

;;; See https://gist.github.com/brake/c944229350e91f295a1762d3274393ef

(defn get-clipboard
  []
  (-> (java.awt.Toolkit/getDefaultToolkit)
      (.getSystemClipboard)))

(defn slurp-clipboard
  []
  (when-let [^java.awt.datatransfer.Transferable clip-text (some-> (get-clipboard)
                                                                   (.getContents nil))]
    (when (.isDataFlavorSupported clip-text java.awt.datatransfer.DataFlavor/stringFlavor)
      (->> clip-text
           (#(.getTransferData % java.awt.datatransfer.DataFlavor/stringFlavor))
           (cast String)))))

(defn spit-clipboard [text]
  (.setContents (get-clipboard) (java.awt.datatransfer.StringSelection. text) nil))

(defn read-string-from-clipboard!
  "As `clojure.core/read-string` but reads string from system clipboard.
  Use only if you can trust the code/data you read.
  Alternatively, you can pass custom `read-string-fn` (presumably clojure.edn/read-string).

  It may be useful to `(set! *default-data-reader-fn* tagged-literal)` to handle unknown tags.
  See https://github.com/clojure-cookbook/clojure-cookbook/blob/master/04_local-io/4-17_unknown-reader-literals.asciidoc
  and http://insideclojure.org/2018/06/21/tagged-literal/?cn=ZmxleGlibGVfcmVjc18y&refsrc=email."
  ([] (read-string-from-clipboard! read-string))
  ([read-string-fn]
   (let [clipboard-string (slurp-clipboard)]
     (read-string-fn clipboard-string))))

;; copy this first:
;;   (1 2 3 {:a (1 2 3)})
#_(read-string-from-clipboard!)
;; => (1 2 3 {:a (1 2 3)})
