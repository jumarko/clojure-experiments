(ns clojure-experiments.books.joy-of-clojure.ch09-data-and-code
  "Chapter 9: Combining data and code.
  Starting at the page 194.")

;;; Namespaces (p. 195)
;;;;;;;;;;;;;;;;;;;;;;;;
(in-ns 'joy.ns)
(def authors ["Chouser"])

(in-ns 'your.ns)
(clojure.core/refer 'joy.ns)
joy.ns/authors
;; => ["Chouser"]

(in-ns 'joy.ns)
(def authors ["Chouser" "Fogus"])

(in-ns 'your.ns)
joy.ns/authors
;; => ["Chouser" "Fogus"]

(in-ns 'your.ns)
;; this works
(Integer. 3)
;; but `reduce` isn't imported
#_(reduce + [1 2 (Integer. 3)])
;; Syntax error compiling at (src/clojure_experiments/books/joy_of_clojure/ch09_data_and_code.clj:22:1).
;; Unable to resolve symbol: reduce in this context

;; it works with `ns`
(ns chimp)
(reduce + [1 2 (Integer. 3)])
;; => 6

(def b (create-ns 'bonobo))
((ns-map b) 'String)
;; => java.lang.String

(ns-map 'bonobo)


;; intern a symbol:
(intern b 'x 9)
;; => #'bonobo/x
bonobo/x
;; => 9

(ns-unmap b 'x)
#_bonobo/x
; No such var: bonobo/x

(remove-ns 'bonobo)

(in-ns 'clojure-experiments.books.joy-of-clojure.ch09-data-and-code)

;; alternative way to make function private
(defn ^{:private true} answer [] 42)
(answer)
;; => 42

(remove-ns 'chimp)
(ns chimp)
#_(answer)
;; Unable to resolve symbol: answer in this context 

(ns clojure-experiments.books.joy-of-clojure.ch09-data-and-code)


;;; Multimethods and Universal Design Pattern (p. 200-206)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; UDP requires 5 basic functions: `beget`, `get`, `put`, `has?`, `forget`
;; => exclude clojure.core/get
(ns joy.udp
  (:refer-clojure :exclude [cat get]))

(defn beget
  "Makes `proto` a prototype of `this`."
  [this proto]
  (assoc this ::prototype proto))

(beget {:sub 0} {:super 1})
;; => {:sub 0, :joy.udp/prototype {:super 1}}

;; we can now implement `get` to perform potentially nested lookups
(defn get
  "Look up given key in the map and, if not found,
  recursively in all its prototypes."
  [m k]
  (when m
    (if-let [[_ v] (find m k)]
      v
      (recur (::prototype m) k))))

(get nil :sub)
;; => nil
(get {:sub 0} :sub)
;; => 0

(get
 (beget {:sub 0} {:super 1})
 :super)
;; => 1

(get
 (beget {:sub 0} {:super nil})
 :super)
;; => nil

(get
 (beget {:sub 0} (beget {:super 1} {:sup-super 10}))
 :sup-super)
;; => 10

;; put function only inserts element into the specific "level"
;; => it's assymetric with respect to `get`
(def put assoc)
(put (beget {:sub 0} {:super nil})
     :sub2 2)
;; => {:sub 0, :joy.udp/prototype {:super nil}, :sub2 2}


;; Just using `beget`, `get`, and `put` gives us opportunity to use UDP in simple
;; yet powerful ways

;; Assume that cats at birth like dogs and only learn to despise them when goaded ("vyprovokovane")

(def cat {:likes-dogs true :ocd-bathing true})
(def morris (beget {:likes-9lives true} cat))
(def post-traumatic-morris (beget {:likes-dogs nil} morris))

(get cat :likes-dogs)
;; => true
(get morris :likes-dogs)
;; => true
(get post-traumatic-morris :likes-dogs)
;; => nil
(get post-traumatic-morris :likes-9lives)
;; => true

;;; let's try multimethods with the UDP (p. 203)
;;;

(defmulti compiler :os)
(defmethod compiler ::unix [m] (get m :c-compiler))
(defmethod compiler ::osx [m] (get m :llvm-compiler))

(def clone (partial beget {}))
(def unix {:os ::unix :c-compiler "cc" :home "/home" :dev "/dev"})
(def osx (-> (clone unix)
             (put :os ::osx)
             (put :llvm-compiler "clang")
             (put :home "/Users")))

(compiler unix)
;; => "cc"
(compiler osx)
;; => "clang"

;; duplicating methods for every "subtype" is problematic
;; Let's imagine we can reuse `home` multimethod for both
;; but only define it once
(defmulti home :os)
(defmethod home ::unix [m] (get m :home))

(home unix)
;; => "/home"

;; Doesn't work with osx (yet)
#_(home osx)
;;=> No method in multimethod 'home' for dispatch value: :joy.udp/osx

;; now define that "::osx is ::unix"
(derive ::osx ::unix)

(home osx)
;; => "/Users"

;; you can query derivation hierarchy:
(parents ::osx) ; only immediate parents
;; => #{:joy.udp/unix}
(ancestors ::osx)
;; => #{:joy.udp/unix}
(descendants ::unix)
;; => #{:joy.udp/osx}
(isa? ::osx ::unix)
;; => true
(isa? ::unix ::osx)
;; => false

;; let's now try another derivation to introduce conflict
(derive ::osx ::bsd)
(defmethod home ::bsd [m] "/home")

#_(home osx)
;; Multiple methods in multimethod 'home' match dispatch value: :joy.udp/osx -> :joy.udp/bsd and :joy.udp/unix, and neither is preferred

(prefer-method home ::unix ::bsd)
(home osx)
;; => "/Users"

;; you can define custom hierarchy to avoid global state
(def my-hierarchy (derive (make-hierarchy) ::osx ::unix))
;; => {:parents #:joy.udp{:osx #{:joy.udp/unix}},
;;     :ancestors #:joy.udp{:osx #{:joy.udp/unix}},
;;     :descendants #:joy.udp{:unix #{:joy.udp/osx}}}()
;; TODO: doesn't work:
;; class clojure.lang.PersistentArrayMap cannot be cast to class clojure.lang.IRef (clojure.lang.PersistentArrayMap and clojure.lang.IRef are in unnamed module of loader 'app')
#_(defmulti my-home :os :hierarchy my-hierarchy)


;; we build more complicated dispatch function using `juxt`
(defmulti compile-cmd (juxt :os compiler)) ; notice using multimethod `compiler` in the dispatch function

(defmethod compile-cmd [::osx "gcc"] [m]
  (str "/usr/bin" (get m :c-compiler)))

(defmethod compile-cmd :default [m]
  (str "Unsure where to locate" (get m :c-compiler)))

;; TODO: doesn't work, why?
(compile-cmd osx)
;; => "Unsure where to locatecc"
((juxt :os compiler) osx)
;; => [:joy.udp/osx "clang"]

#_(ns clojure-experiments.books.joy-of-clojure.ch09-data-and-code)
