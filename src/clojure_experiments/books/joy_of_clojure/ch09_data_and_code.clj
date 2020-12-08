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

;; define shorter ns name to make examples with TreeNode shorter
(ns joc.records)


;;; types, protocols, records (p. 207 - 219)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Record, unlike a map, has a type
(defrecord TreeNode [val l r])
;; => joc.records.TreeNode

(TreeNode. 5 nil nil)
;; => #joc.records.TreeNode{:val 5, :l nil, :r nil}

;; Let's try to implement persistent binary tree with records
(defrecord TreeNode [val l r])

(defn xconj [t v]
  (cond
    (nil? t) (TreeNode. v nil nil)
    (< v (:val t)) (TreeNode. (:val t) (xconj (:l t) v) (:r t))
    :else (TreeNode. (:val t) (:l t) (xconj (:r t) v))))

(defn xseq [t]
  (when t
    (concat (xseq (:l t))
            [(:val t)]
            (xseq (:r t)))))

(def sample-tree (reduce xconj nil [3 5 2 4 5]))


;; Imagine a FIXO protocol to describe operations that Queues and Stacks have in common
(defprotocol FIXO
  (fixo-push [fixo value])
  (fixo-pop [fixo])
  (fixo-peek [fixo]))
;; => FIXO

;; now implement the protocol
(extend-type TreeNode
  FIXO
  (fixo-push [node value]
    (xconj node value)))

(xseq (fixo-push sample-tree 5/2))
;; => (2 5/2 3 4 5 5)

;; extend vectors
(extend-type clojure.lang.IPersistentVector
  FIXO
  (fixo-push [vect value]
    (conj vect value)))

(fixo-push [2 3 4 5 6] 5/2)
;; => [2 3 4 5 6 5/2]

;; Gotcha - you have to define all the protocol methods in the extend-type to have them defined
(defprotocol StringOps
  (rev [s])
  (upp [s]))

(extend-type String
  StringOps
  (rev [s] (clojure.string/reverse s)))
(rev "ahoj")
;; => "joha"

(extend-type String
  StringOps
  (upp [s] (clojure.string/upper-case s)))
(upp "ahoj")
;; => "AHOJ"
#_(rev "ahoj")
;; No implementation of method: :rev of protocol: #'joc.records/StringOps found for class: java.lang.String


;; extending protocol to `nil`
;; - we need to extend FIXO to nil explicitly, because nil is a special type:
#_(reduce  fixo-push nil [3 5 2 4 6 0])
;; No implementation of method: :fixo-push of protocol: #'joc.records/FIXO found for class: nil

(extend-type nil
  FIXO
  (fixo-push [t v]
    (TreeNode. v nil nil)))
(xseq (reduce  fixo-push nil [3 5 2 4 6 0]))
;; => (0 2 3 4 5 6)


;; If we need to share protocol methods/code between multiple types
;; we can use more advanced `extend` construct
;; You first define a map of functions ...
(def tree-node-fixo
  {:fixo-push (fn [node value]
                (xconj node value))
   :fixo-peek (fn [node] (if (:l node)
                           (recur (:l node))
                           (:val node)))
   :fixo-pop (fn [node] (if (:l node)
                          (TreeNode. (:val node) (fixo-pop (:l node)) (:r node))
                          (:r node)))})

;; ... and then you can use it (and merge it with anything else):
(extend TreeNode FIXO tree-node-fixo)
(fixo-peek sample-tree)
;; => 2
(fixo-pop sample-tree)
;; => #joc.records.TreeNode{:val 3, :l nil, :r #joc.records.TreeNode{:val 5, :l #joc.records.TreeNode{:val 4, :l nil, :r nil}, :r #joc.records.TreeNode{:val 5, :l nil, :r nil}}}

;; You could then use tree-node-fixo for another type and just override one of the methods, e.g.
(defrecord TreeNodeUnmodifiable [])
(extend TreeNodeUnmodifiable
  FIXO
  (merge tree-node-fixo {:fixo-push (fn [_ _] (throw (IllegalStateException. "Cannot modify")))}))
#_(fixo-push (TreeNodeUnmodifiable.) 1)

;; you can also examine protocol's implementation details
;; - check `:impls` and `:method-builders`
;; - see also `satisfies?`
(:impls FIXO)
;; => {joc.records.TreeNode
;;     {:fixo-push #function[joc.records/fn--15839],
;;      :fixo-peek #function[joc.records/fn--15841],
;;      :fixo-pop #function[joc.records/fn--15843]},
;;     clojure.lang.IPersistentVector {:fixo-push #function[joc.records/eval20835/fn--20836]},
;;     nil {:fixo-push #function[joc.records/eval20892/fn--20893]},
;;     joc.records.TreeNodeUnmodifiable
;;     {:fixo-push #function[joc.records/eval20519/fn--20520],
;;      :fixo-peek #function[joc.records/fn--15841],
;;      :fixo-pop #function[joc.records/fn--15843]},
;;     joc.records.TreeNode
;;     {:fixo-push #function[joc.records/fn--20898],
;;      :fixo-peek #function[joc.records/fn--20900],
;;      :fixo-pop #function[joc.records/fn--20902]},
;;     joc.records.TreeNodeUnmodifiable
;;     {:fixo-push #function[joc.records/eval20928/fn--20929],
;;      :fixo-peek #function[joc.records/fn--20900],
;;      :fixo-pop #function[joc.records/fn--20902]}}(:var FIXO)


;; we can implement protocol directly when defining defrecord

(defrecord TreeNode [val l r]
  FIXO
  (fixo-push [t v]
    (if (< v val)
      (TreeNode. val (fixo-push l v) r)
      (TreeNode. val l (fixo-push r v))))
  (fixo-peek [t]
    (if l
      (fixo-peek l) ; notice calling fixo-peek instead of using `recur` => recur isn't polymorphic!
      val))
  (fixo-pop [t]
    (if l
      (TreeNode. val (fixo-pop l) r)
      r)))

(def sample-tree2 (reduce fixo-push (TreeNode. 3 nil nil) [5 2 4 6]))
(xseq sample-tree2);; => (2 3 4 5 6)


;; we have been using `xseq` because our defrecord cannot implement `clojure.lang.ISeq`
;; (it's implemented automatically by defrecord)
;; But we may use `deftype` (that doesn't implement anything automatically)

#_(defrecord InfiniteConstant [i]
  clojure.lang.ISeq
  (seq [this]
    (lazy-seq (cons i (seq this)))))
;;=> Duplicate method name "seq" with signature "()Lclojure.lang.ISeq;" in class file joc/records/InfiniteConstant

(deftype InfiniteConstant [i]
  clojure.lang.ISeq
  (seq [this]
    (lazy-seq (cons i (seq this)))))
(take 3 (InfiniteConstant. 5))
;; => (5 5 5)


;;; Putting it all together: a fluent builder for chess moves (p. 219 - 223)

;; p .221: Instead of having a special Move class in Java
;; we just use a plain map:
{:from "e7" :to "e8" :castle? false :promotion \Q}

;; now existing sequence functions just work:
(defn build-move [& pieces]
  (apply hash-map pieces))
(build-move :from "e7" :to "e8" :promotion \Q)
;; => {:from "e7", :promotion \Q, :to "e8"}

;; but using a plain map we cannot define our own toString representation
;; => try defrecord
(defrecord Move [from to castle? promotion]
  Object
  (toString [this]
    (format "Move %s to %s%s%s"
            from
            to
            (if castle? " castle" "")
            (if promotion (str " promote to " promotion) ""))))

(str (Move. "e2" "e4" nil nil))
;; => "Move e2 to e4"

(str (Move. "e7" "e8" nil \Q))
;; => "Move e7 to e8 promote to Q"

;; now we can improve our build-move function
(defn build-move [& {:keys [from to castle? promotion]}]
  {:pre [from to]}
  (Move. from to castle? promotion))
(str (build-move :from "e2" :to "e4"))
;; => "Move e2 to e4"
#_(build-move :to "e4")
;; Assert failed: from

