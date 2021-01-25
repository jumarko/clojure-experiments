(ns clojure-experiments.books.joy-of-clojure.DynaFrame
  "See `ch12_java_next.clj`."
  (:gen-class
   ;; notice that the actual name of the class is `joy.gui.DynaFrame` as stated in `:name` 
   :name joy.gui.DynaFrame
   :extends javax.swing.JFrame
   :implements [clojure.lang.IMeta]
   :prefix "df-" ; must be string
   :state state
   :init init
   ;; mapping of the class constructors to the superclass constructors
   :constructors {[String] [String]
                  [] [String]}
   :methods [[display [java.awt.Container] void]
             ;; look here
             ^:static [version [] String]])
  (:import (javax.swing JFrame JPanel JComponent JLabel)
           (java.awt BorderLayout Container)))


(defn df-init [title]
  [[title] (atom {::title title})])

(defn df-meta [this] @(.state this))

;; static method
(defn df-version [] "1.0")

(defn df-display [this pane]
  (doto this
    (-> .getContentPane .removeAll)
    (.setContentPane (doto (JPanel.)
                       (.add pane BorderLayout/CENTER)))
    (.pack)
    (.setVisible true)))

(def gui (joy.gui.DynaFrame. "4th"))
(comment
  (.display gui (doto (JPanel.)
                  (.add (JLabel. "Charlemagne and Pippin"))))
  ;; change it on the fly
  (.display gui (doto (JPanel.)
                  (.add (JLabel. "Mater semper certa est."))))
  )
