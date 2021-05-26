(ns clojure-experiments.java.classloading)

;;; Once upon a class: https://danielsz.github.io/blog/2021-05-12T13_24.html
(->> (.. java.lang.Thread currentThread getContextClassLoader)
     (iterate #(.getParent %))
     (take-while identity))


;; - When you create foo at the REPL, Clojure’s compiler emits bytecode for consumption by DynamicClassLoader. It will create a new class with the defineClass method before linking it.
;; - Once a class loader links a class, it is final. Attempting to link a new definition of the class does nothing.
;; - To work around this limitation, a new DynamicClassLoader is created for each evaluation.

(defn foo [x] (+ x x))
(.getClassLoader (class foo))
;;=> #object[clojure.lang.DynamicClassLoader 0x227119dc "clojure.lang.DynamicClassLoader@227119dc"]

;; when you redefine it a new classloader is created
(defn foo [x] (+ x x))
;;=> #object[clojure.lang.DynamicClassLoader 0x4760597d "clojure.lang.DynamicClassLoader@4760597d"]


;; The compiler tracks the current instance of DynamicClassLoader in clojure.lang.Compiler/LOADER,
;; while DynamicClassLoader tracks its classes via a cache.
;; The latter is backed by a reference queue, helping the garbage collector do its job.
;; We can peek into it via the Reflection API.
(defn inspect-cache
  "This will reveal a mapping between the names of the generated classes and soft references.
  If you redefine foo at the REPL, the soft reference associated with foo in the cache will be updated."
  []
  (let [cache (.getDeclaredField clojure.lang.DynamicClassLoader "classCache")]
    (.setAccessible cache true)
    (.get cache nil)))
;; Can be huge!
#_(inspect-cache)
