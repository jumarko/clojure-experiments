(ns clojure-experiments.parsers.tree-sitter
  "Playing with Tree-sitter: https://tree-sitter.github.io/tree-sitter/using-parsers/1-getting-started.html
  Java bindings: https://github.com/tree-sitter/java-tree-sitter.

  Unfortunately, there's little documentation about how to use Java bindings - this code should help.

  SETUP - for macOS (which is more troublesome due to unavailability of LD_PRELOAD_PATH-like variable)
  ------
  It's necessary to clone and compile tree-sitter core and all the grammars you intend to use.
  Run `make` inside all the repos which should produce the static and _dynamic_ libraries
  (on the JVM you can only use _dynamic_ libraries, afaik):
  - Core lib: https://github.com/tree-sitter/tree-sitter
  - Java grammar: https://github.com/tree-sitter/tree-sitter-java
  - Python grammar: https://github.com/tree-sitter/tree-sitter-python
  - C# grammar: https://github.com/tree-sitter/tree-sitter-c-sharp
  - ...
  The code below assumes you have cloned all the repositories under the same parent folder,
  which you can specify via `TREE_SITTER_HOME` env variable
  (you can also change the hardcoded value down below).

  Additionally, you need Java binding for Tree-sitter, that is https://github.com/tree-sitter/java-tree-sitter
  This is added as a dependency in deps.edn and you do not need to take any further action.
  The library requires JDK 22+.

  NOTE: It should also be possible to install all the tree-sitter's dynamic libs system-wide
  or in `java.library.path`, but this is not what this code is for
  (but you could still use `load-lib` in that scenario and, at least, get some inspiration from `load-treesitter-lib`).
  "
  (:import
   [io.github.treesitter.jtreesitter Language Parser]
   ;; https://docs.oracle.com/en/java/javase/22/docs//api/java.base/java/lang/foreign/SymbolLookup.html
   [java.lang.foreign Arena SymbolLookup]
   [java.nio.file Path])
  (:require [clojure.string :as str]
            [babashka.fs :as fs]))


(def tree-sitter-home (or (System/getenv "TREE_SITTER_HOME")
                          "/Users/jumar/workspace/parsing/tree-sitter"))

(def main-lib )

(defn grammar-lib-path
  [language]
  (let [grammar-lib (str "tree-sitter-" language)]
    (format "%s/%s/%s" tree-sitter-home grammar-lib (System/mapLibraryName grammar-lib))))
(grammar-lib-path "java")
;; => "/Users/jumar/workspace/parsing/tree-sitter/tree-sitter-java/libtree-sitter-java.dylib"

(defn load-lib
  "Loads a dynamic library from given path (string).
  Returns a `SymbolLookup`."
  [lib-path]
  (let [library (Path/of lib-path (make-array String 0))
        s (SymbolLookup/libraryLookup library (Arena/global))]
    s))
(defn ensure-main-lib!
  "Make sure the core tree-sitter library is copied to the working directory.
  This must be done before loading the grammars via `load-treesitter-lib`.
  NOTE: Check scripts/TreeSitter_java.path used by Jextract for more details about loading the core tree-sitter lib.

  If the core lib is not present yet, this function will exit (!) the current program.
  This is necessary because, after copying the lib to the working dir, the REPL must be restarted."
  []
  (try
    (load-lib (System/mapLibraryName "tree-sitter"))
    (catch IllegalArgumentException iae
      ;; cannot load Tree-sitter core lib from the current working directory (or system-wide path).
      ;; This is what java-tree-sitter's Language constructor will try to do so let's try to fix the problem
      ;; by copying the tree-sitter dynamic library to the current directory.
      (let [lib-name (System/mapLibraryName "tree-sitter")]
        (fs/copy (str tree-sitter-home "/tree-sitter/" lib-name)
                 lib-name))
      (binding [*out* *err*]
        (println "Tree-sitter dynamic library copied to the current directory. Please, restart the REPL!"))
      (System/exit 1))))

(defn load-treesitter-lib
  "Loads tree-sitter grammar (native lib) and uses it to initialize an instance of `Language`.
  This can be directly used to make a parser."
  [language]
  (ensure-main-lib!)
  (let [lib-symbol (str "tree_sitter_" (str/replace language "-" "_"))]
    ;; NOTE: this requires that the main libtree-sitter lib is in the current working directory
    ;; `Language` constructor checks this by accessing `ts_language_version`  symbol
    (some-> (grammar-lib-path language) load-lib (Language/load lib-symbol))))

(defn make-parser
  [language]
  (Parser. language))

(defn parse
  "Parses given code, producing an instance of `ParseTree`."
  [parser file-path]
  (.get (.parse parser (slurp file-path))))

(defn parse-root
  "A convenient function for loading a grammar lib for given language,
  making a new instance of a parser,
  parsing the file at `flie-path`,
  and returning the root node of the parse tree.."
  [language file-path]
  (-> (load-treesitter-lib language)
      (make-parser)
      (parse file-path)
      .getRootNode))

(comment
  (def java-lang (load-treesitter-lib "java"))
  (def java-parser (make-parser java-lang))

  (def java-tree (parse java-parser "/path/to/File.java"))
  ;; see https://github.com/tree-sitter/java-tree-sitter/blob/master/src/main/java/io/github/treesitter/jtreesitter/Tree.java for the API
  (def java-root (.getRootNode java-tree))


  ;; this parses within 100 msecs!
  (def c-sharp-root (time (parse-root "c-sharp" "/path/to/File.cs")))
  (.getChildren c-sharp-root)


  )
