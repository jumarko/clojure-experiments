(ns clojure-experiments.books.joy-of-clojure.ch03-dipping-toes
  "Chapter 3: Dipping your toes in the pool.
  Especially the 'Using the REPL to experiment' section.")

;;; Using the REPL to experiment: 59 - 65

;; first prepare a function that xors x and y coordinates
(defn xors [max-x max-y]
  (for [x (range max-x)
        y (range max-y)]
    [x y (bit-xor x y)]))

(xors 2 2)
;; => ([0 0 0] [0 1 1] [1 0 1] [1 1 0])


;; now create a Frame
(def frame (java.awt.Frame.))

;; but it's not visible yet => notice "hidden"
frame
;; => #object[java.awt.Frame 0x57bbba33 "java.awt.Frame[frame0,0,23,0x0,invalid,hidden,layout=java.awt.BorderLayout,title=,resizable,normal]"]

;; we want to examine Frame's methods
(for [meth (.getMethods java.awt.Frame)
      :let [name (.getName meth)]
      :when (re-find #"Vis" name)]
  meth
  )
;; => (#object[java.lang.reflect.Method 0x489b3dc9 "public void java.awt.Window.setVisible(boolean)"]
;;     #object[java.lang.reflect.Method 0x71b875df "public boolean java.awt.Component.isVisible()"])

(defn matching-methods [clazz pattern]
  (for [meth (.getMethods clazz)
        :let [name (.getName meth)]
        :when (re-find pattern name)]
    (str meth)))

(matching-methods java.awt.Frame #"Vis")
;; => ("public void java.awt.Window.setVisible(boolean)" "public boolean java.awt.Component.isVisible()")
(matching-methods java.awt.Frame #"vis")
;; => ()
(matching-methods java.awt.Frame #"(?i)vis")
;; => ("public void java.awt.Window.setVisible(boolean)" "public boolean java.awt.Component.isVisible()")

;; now make our frame visible
(.setVisible frame true)
;; => nil

;; it's visible but tiny:
(.setSize frame (java.awt.Dimension. 200 200))

;; draw on the frame (p. 62)
(def gfx (.getGraphics frame))
(.fillRect gfx 100 100 50 75)

;; add some color
(.setColor gfx (java.awt.Color. 255 128 0))
(.fillRect gfx 100 100 50 75)

;; Put it together - color all the xors (p. 63)
(doseq [[x y xor] (xors 400 200)]
  (.setColor gfx (java.awt.Color. xor xor xor))
  (.fillRect gfx x y 1 1))


(defn f-values [f max-x max-y]
  (for [x (range max-x)
        y (range max-y)]
    [x y (rem (f x y) 256)]))

(defn draw-values [values]
  (doseq [[x y val] values]
    (.setColor gfx (java.awt.Color. val val val))
    (.fillRect gfx x y 1 1)))

(comment
  
  (draw-values (f-values bit-xor 200 200))
  (draw-values (f-values * 200 200))
  (draw-values (f-values + 200 200))

  (.dispose frame))



