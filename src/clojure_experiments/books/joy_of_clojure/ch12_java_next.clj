(ns clojure-experiments.books.joy-of-clojure.ch12-java-next
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [com.sun.net.httpserver HttpHandler HttpExchange HttpServer]
           [java.net InetSocketAddress URLDecoder URI]
           [java.io File FilterOutputStream]
           (javax.swing Box BoxLayout JTextField JPanel
                        JSplitPane JLabel JButton JOptionPane)
           (java.awt BorderLayout Component Container FlowLayout GridLayout)
           (java.awt.event ActionListener)))

(def OK java.net.HttpURLConnection/HTTP_OK)

;;; proxy (p. 285)

;; server
(defn new-server [port path handler]
  (doto (HttpServer/create (InetSocketAddress. port) 0)
    (.createContext path handler)
    (.setExecutor nil)
    (.start)))

;; request handler
(defn respond
  ([exchange body]
   (respond identity exchange body))
  ([around exchange body]
   (.sendResponseHeaders exchange OK 0)
   (with-open [resp (around (.getResponseBody exchange))]
     (.write resp (.getBytes body)))))

(defn default-handler [txt]
  (proxy [HttpHandler] []
    (handle [exchange] (respond exchange txt))))


;; create the server
(comment

  ;; => go to http://localhost:8123/joy/hello
  (def server (new-server 8123, "/joy/hello" (default-handler "Hello Cleveland")))
  (.stop server 0)

  ;;
  )

;; now add more dynamism to be able to change the handler independently
(def p (default-handler "There's no problem that can't be solved with another level of indirection"))
(comment
  ;; http://localhost:8123/
  (def server (new-server 8123 "/" p))

  ;;
  )

;; now use `update-proxy` to avoid restarting the server
(update-proxy p {"handle"
                 (fn [this exchange] (respond exchange (str "this is " this)))})

(defn echo-handler [_ exchange]
  (let [headers (.getRequestHeaders exchange)]
    (respond exchange (prn-str headers))))
(update-proxy p {"handle" echo-handler})

;; `proxy-mappings` examine the existing mappings
(proxy-mappings p)
;; => {"handle" #function[clojure-experiments.books.joy-of-clojure.ch12-java-next/echo-handler]}

;;; This is cool! Simple browser of file system
;;; (p. 282 - 285)

;; `around` filter works with OutputStream (.getResponseBody returns that)
;; so if we want to implement a custom filter it must return a compatible object
(defn html-around [o]
  (proxy [FilterOutputStream] [o]
    (write [raw-bytes]
      ;; Note: proxy-super is NOT thread safe!
      ;;   If some other thread were to call this proxy instance while proxy-super was still running,
      ;;   the base class's method would be called directly, incorrectly skipping the proxy implementation!
      (proxy-super write (.getBytes (str "<html><body>" (String. raw-bytes) "</body></html>"))))))

(defn listing [file]
  (-> file .list sort))
(filter #(string/ends-with? % ".clj")
        (listing (io/file ".")))
;; => ("foo2.clj" "lein-profiles.clj" "project.clj" "read-line.clj")

(defn html-links [root filenames]
  (string/join (for [file filenames]
                 (str "<a href='"
                      (str root
                           (if (= "/" root) "" File/separator)
                           file)
                      "'>"
                      file "<a/><br>"))))

(->> (listing (io/file "."))
     (filter #(string/ends-with? % ".clj"))
     (html-links "."))
;; => "<a href='./foo2.clj'>foo2.clj<a/><br><a href='./lein-profiles.clj'>lein-profiles.clj<a/><br><a href='./project.clj'>project.clj<a/><br><a href='./read-line.clj'>read-line.clj<a/><br>"

;; ... we have a function to list a directory
;; but we also want to deal with files ...
(defn details [file]
  (format "%s is %d bytes" (.getName file) (.length file)))
(details (io/file "README.md"))
;; => "README.md is 215 bytes"

(defn uri->file [root uri]
  (->> uri str URLDecoder/decode (str root) io/file))

(uri->file "." (URI. "/project.clj"))
;; => #object[java.io.File 0x3cbb2f9f "./project.clj"]
(details (uri->file "." (URI. "/project.clj")))
;; => "project.clj is 13123 bytes"

(defn fs-handler [_ exchange]
  (let [uri (.getRequestURI exchange)
        file (uri->file "." uri)]
    (if (.isDirectory file)
      (do (.add (.getResponseHeaders exchange)
                "Content-Type" "text/html")
          (respond html-around exchange (html-links (str uri) (listing file))))
      (respond exchange (details file)))))
#_(update-proxy p {"handle" fs-handler})


;;; gen-class and gui programming (p. 286 - 292)

;; must be standalone namespace?
#_(ns joy.gui.DynaFrame
  (:gen-class
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
             ^{:static true} [version [] String]])
  (:import (javax.swing JFrame JPanel JComponent)
           (java.awt BorderLayout Container)))

(compile 'clojure-experiments.books.joy-of-clojure.DynaFrame)
;; => clojure-experiments.books.joy-of-clojure.DynaFrame
;; notice that the actual name of the class is `joy.gui.DynaFrame` as stated in `:name` 
#_(joy.gui.DynaFrame. "First try")
;; Execution error (UnsupportedOperationException) at joy.gui.DynaFrame/<init> (REPL:-1).
;; clojure-experiments.books.joy-of-clojure.DynaFrame/df-init not defined

#_(joy.gui.DynaFrame. "Second try")
;; meta (clojure-experiments.books.joy-of-clojure.DynaFrame/df-meta not defined?)

(meta (joy.gui.DynaFrame. "Third try"))
;; => #:clojure-experiments.books.joy-of-clojure.DynaFrame{:title "Third try"}

;; note that this doesn't work - it's because my ns is named differently from the generated class
;; (joy.gui.DynaFrame/version)
;; No such var: joy.gui.DynaFrame/version

;; but using dot-special operator works
;; and notice that the function name must be with prefix too, that is `df-version`
(. joy.gui.DynaFrame version)
;; => "1.0"

(def gui (joy.gui.DynaFrame. "4th"))

#_(.display gui (doto (JPanel.)
                (.add (JLabel. "Charlemagne and Pippin"))))


;;; Let's build 3 GUI containers - shelves, stacks, splitters
(defn shelf [& components]
  (let [shelf (JPanel.)]
    (.setLayout shelf (FlowLayout.))
    (doseq [c components]
      (.add shelf c))
    shelf))

(defn stack [& components]
  (let [stack (Box. BoxLayout/PAGE_AXIS)]
    (doseq [c components]
      (.setAlignmentX c Component/CENTER_ALIGNMENT)
      (.add stack c))
    stack))

(defn splitter [top bottom]
  (doto (JSplitPane.)
    (.setOrientation JSplitPane/VERTICAL_SPLIT)
    (.setLeftComponent top)
    (.setRightComponent bottom)))

;; let's now provide basic widgets: buttons, labels, text boxes
(defn button [text f]
  (doto (JButton. text)
    ;; use reify instead of proxy: https://stackoverflow.com/questions/5821892/why-should-i-use-reify-instead-of-proxy-in-clojure
    #_(.addActionListener (proxy [ActionListener] [] (actionPerformed [_] (f))))
    (.addActionListener
     (reify ActionListener
       (actionPerformed [this _] (f))))))

(defn txt [cols t]
  (doto (JTextField.)
    (.setColumns cols)
    (.setText t)))

(defn label [txt] (JLabel. txt))

(defn alert
  ([msg] (alert nil msg))
  ([frame msg]
   (JOptionPane/showMessageDialog frame msg)))

#_(.display gui (splitter
               (button "Procrastinate" #(alert "Eat Cheetos"))
               (button "Move It" #(alert "Couch to 5k"))))

;; let's add one more widget builder
(defn grid [x y f]
  (let [g (doto (JPanel.)
            (.setLayout (GridLayout. x y)))]
    (dotimes [_ (* x y)]
      (.add g (f)))
    g))

(defn rich-ui [dyna-frame]
  (.display
   dyna-frame
   (let [g1 (txt 10 "Charlemagne")
         g2 (txt 10 "Pippin")
         r (txt 3 "10")
         d (txt 3 "5")]
     (splitter
      (stack
       (shelf (label "Player 1") g1)
       (shelf (label "Player 2") g2)
       (shelf (label "Rounds ") r (label "Delay  ") d))
      (stack
       (grid 21 11 #(label "-"))
       (button "Go1" #(alert (format "%s vs. %s for %s rounds, every %s seconds."
                                     (.getText g1) (.getText g2) (.getText r) (.getText d)))))))))
#_(rich-ui gui)


;;; Java arrays (p. 292 - 297)
;;;

;; Clojure compiler can never resolve the correct call
;; if we use reference array instead of a primitive array
;; here `.append` accepts a primitive array
(str (doto (StringBuilder. "abc")
   (.append (into-array [\x \y \z]))))
;; => "abc[Ljava.lang.Character;@1426536"
;; So it actually called StringBuilder.append(Object) method

(type (into-array [\x \y \z]))
;; => [Ljava.lang.Character;

;;=> we need primitive array
(str (doto (StringBuilder. "abc")
       (.append (char-array [\x \y \z]))))
;; => "abcxyz"


;; make-array and into-array can be used to create primitive arrays
(let [ary (make-array Long/TYPE 3 3)] ; this creates 2-D array
  (dotimes [i 3]
    (dotimes [j 3]
      (aset ary i j (+ i j))))
  (map seq ary))
;; => ((0 1 2) (1 2 3) (2 3 4))

(into-array Integer/TYPE [1 2 3])
;; => #object["[I" 0x15ef7772 "[I@15ef7772"]

;; creating reference type arrays
(into-array ["a" "b" "c"])
;; => #object["[Ljava.lang.String;" 0xeefbf9 "[Ljava.lang.String;@eefbf9"]

(into-array [(java.util.Date.) (java.sql.Time. 0)]) ; notice that `Time` extends `Date`
;; => #object["[Ljava.util.Date;" 0xacf7780 "[Ljava.util.Date;@acf7780"]

#_(into-array ["a" "b" 1M])
;; => array element type mismatch

;; use to-array to make heterogenous array of Object-s
(to-array-2d [[1 2 3]
              [4 5 6]])
;; => #object["[[Ljava.lang.Object;" 0x4f6de1e4 "[[Ljava.lang.Object;@4f6de1e4"]
(to-array ["a" 1M #(%) (proxy [Object] [])])
;; => #object["[Ljava.lang.Object;" 0x704189b3 "[Ljava.lang.Object;@704189b3"]

;; - it autoboxes primitives!
(to-array [1 2 3])
;; => #object["[Ljava.lang.Object;" 0x49994161 "[Ljava.lang.Object;@49994161"]
(into-array Integer/TYPE [1 2 3])
;; => #object["[I" 0x39b658f6 "[I@39b658f6"]

(type (to-array [1 2 3]))
;; => [Ljava.lang.Object;
(type (into-array [1 2 3]))
;; => [Ljava.lang.Long;
(type (into-array Integer/TYPE [1 2 3]))
;; => [I
(type (int-array [1 2 3]))
;; => [I
(type (aget (int-array [1 2 3])
            0))
;; => java.lang.Integer

(into-array Integer [(int 1)])
;; => #object["[Ljava.lang.Integer;" 0x20b9dc33 "[Ljava.lang.Integer;@20b9dc33"]

;; Array mutability (p. 294)
(def ary (into-array [1 2 3]))
(def sary (seq ary))
sary
;; => (1 2 3)
(aset ary 0 42)
sary
;; => (42 2 3)


;; multimethods
(defmulti what-is class)
(defmethod what-is
  (Class/forName "[Ljava.lang.String;")
  [_]
  "1d String")
(defmethod what-is
  (Class/forName "[[Ljava.lang.Object;")
  [_]
  "2d Object")
(defmethod what-is
  (Class/forName "[[[[I")
  [_]
  "Primitive 4d int")
(defmethod what-is
  (Class/forName "[[D")
  [_]
  "Primitive 2d double")
(what-is (into-array ["a" "b"]))
;; => "1d String"
(what-is (to-array-2d [[1 2] [3 4]]))
;; => "2d Object"
(what-is (make-array Integer/TYPE 2 2 2 2))
;; => "Primitive 4d int"
(what-is (into-array (map double-array [[1.0] [2.0]])))
;; => "Primitive 2d double"



;;; Varargs
#_(String/format "An int %d and a String %s" 99 "luftballons")
;; => class java.lang.String cannot be cast to class java.util.Locale (java.lang.String and java.util.Locale are in module java.base of loader 'bootstrap')
(String/format "An int %d and a String %s"
               (to-array [99 "luftballons"]))
;; => "An int 99 and a String luftballons"


;;; All Clojure functions implement ... (p. 297 - 299)
(ancestors (class #()))
;; => #{clojure.lang.IMeta java.lang.Runnable clojure.lang.AFunction java.io.Serializable
;;      clojure.lang.IFn clojure.lang.Fn clojure.lang.IObj java.util.Comparator java.lang.Object
;;      java.util.concurrent.Callable clojure.lang.AFn}

(parents (class #()))
;; => #{clojure.lang.AFunction}

;; java.util.Comparator
(import '[java.util Comparator Collections ArrayList])

(defn gimme [] (ArrayList. [1 3 4 8 2]))

(doto (gimme)
  (Collections/sort (Collections/reverseOrder)))
;; => [8 4 3 2 1]

;; now our own comparator
(doto (gimme)
  (Collections/sort
   (reify Comparator
     (compare [this l r]
       (cond
         (> l r) -1
         (< l r) 1
         :else 0)))))
;; => [8 4 3 2 1]

;; ... but we have better options with Clojure
(doto (gimme) (Collections/sort #(compare %2 %1)))
;; => [8 4 3 2 1]
(doto (gimme) (Collections/sort >))
;; => [8 4 3 2 1]
(doto (gimme) (Collections/sort <))
;; => [1 2 3 4 8]
(doto (gimme) (Collections/sort (complement <)))
;; => [8 4 3 2 1]

(doto (gimme) (Collections/sort #(if (< %1 %2) true false)))

;; => See clojure.lang.AFunction
;; public int compare(Object o1, Object o2){
;;                                          Object o = invoke(o1, o2);

;;                                          if(o instanceof Boolean)
;;                                          {
;;                                           if(RT.booleanCast(o))
;;                                           return -1;
;;                                           return RT.booleanCast(invoke(o2,o1))? 1 : 0;
;;                                           }

;;                                          Number n = (Number) o;
;;                                          return n.intValue();
;;                                          }


;; java.lang.Runnable
#_(doto (Thread. #(do (Thread/sleep 5000)
                    (println "haikeeba")))
  .start)


;; java.util.concurrent.Callable
(import '[java.util.concurrent FutureTask])
#_(let [f (FutureTask. #(do (Thread/sleep 5000) 42))]
  (.start (Thread. #(.run f)))
  (.get f))
;; => 42


;;; Using Clojure data structures in Java APIs (p. 299 - 302)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; java.util.List
;; Clojure collections conform to the immutable parts of java.util.List (which extends Collection and Iterable)

(.get '[a b c] 1)
;; => b

(.get (repeat :a) 138)
;; => :a

;; vectors are Collection-s
(.containsAll  '[a b c] '[b c])
;; => true

;; seqs are immutable
#_(.add '[a b c] 'd)
;; => Execution error (UnsupportedOperationException) at clojure-experiments.books.joy-of-clojure.ch12-java-next/eval20812 (form-init1354641799619265954.clj:452).

;; java.lang.Comparable - only vectors implement it!
(.compareTo [:a :b] [:a])
;; => 1
#_(.compareTo [1 2 3] '(1 2 3))
;; => class clojure.lang.PersistentList cannot be cast to class clojure.lang.IPersistentVector (clojure.lang.PersistentList and clojure.lang.IPersistentVector are in unnamed module of loader 'app')


;; java.util.RandomAccess (optimized constant-time access using `.get`)
;; -> only vectors
(.get '[a b c] 2)
;; => c

;; java.util.Collection
;; - idiom to use immutable clojure collection as a model for creating mutable java collection
;;   and then call java collections API
;; - note: this is the `clojure.core/shuffle` function
(defn shuffle [^java.util.Collection coll]
  (let [al (java.util.ArrayList. coll)] ; here we build mutable collection
    (java.util.Collections/shuffle al) ; ... and call the Java API
    (clojure.lang.RT/vector (.toArray al))))


;; java.util.Set
;; => don't use mutable objects in a set!
(def x (java.awt.Point. 0 0))
(def y (java.awt.Point. 0 42))
(def points #{x y})
points
;; => #{#object[java.awt.Point 0x1561daa1 "java.awt.Point[x=0,y=0]"]
;;      #object[java.awt.Point 0x40f429c0 "java.awt.Point[x=0,y=42]"]}

(.setLocation y 0 0)
;; We now have two equal objects in the set!!!
points
;; => #{#object[java.awt.Point 0x1561daa1 "java.awt.Point[x=0,y=0]"]
;;      #object[java.awt.Point 0x40f429c0 "java.awt.Point[x=0,y=0]"]}

