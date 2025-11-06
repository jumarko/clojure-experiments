^{:kindly/hide-code true
  :clay {:title  "Macroexpand 2025 Noj: Clay Workshop"}}
(ns clojure-experiments.visualizations.clay
  "https://scicloj.github.io/clay/.
  https://scicloj.github.io/clay/clay_book.examples.html
  https://scicloj.github.io/kindly-noted/kindly

  Clojure Civitas:
  https://github.com/ClojureCivitas/clojurecivitas.github.io/blob/main/src/scicloj/clay/workshop/macroexpand2025.clj#L9"
  (:require [scicloj.kindly.v4.kind :as kind]))

(comment
  ;; build and view
  (require 'scicloj.clay.v2.make)
  (scicloj.clay.v2.make/make! {:source-path "src/clojure_experiments/visualizations/clay.clj"})

  ;; watch 
  (require 'scicloj.clay.v2.snippets)
  (scicloj.clay.v2.snippets/watch! {})

  ;;
  )


;;; Clay examples: https://scicloj.github.io/clay/clay_book.examples.html
(+ 4 50)



;; hiccup
(kind/hiccup
 [:ul
  [:li [:p "hi"]]
  [:li [:big [:big [:p {:style ; https://www.htmlcsscolor.com/hex/7F5F3F
                        {:color "#7F5F3F"}}
                    "hello"]]]]])


;; Markdown
(kind/md
 (list
  "
* This is [markdown](https://www.markdownguide.org/).
  * *Isn't it??*"
  "
* Here is **some more** markdown."))


;; Mermaid
(kind/mermaid
 "
flowchart TD
    A[Christmas] -->|Get money| B(Go shopping)
    B --> C{Let me think}
    C -->|One| D[Laptop]
    C -->|Two| E[iPhone]
    C -->|Three| F[fa:fa-car Car]
  ")


;; Tables
(def people [{:name "Juraj" :age 40 :preferred-language "Clojure"}
             {:name "Zdenek" :age 39 :preferred-language "Scala"}])
(kind/table
 {:column-names [:preferred-language :age]
  :row-maps people})



;; watch! doesn't work for me for some reason - call `make!` manually
(comment
  (scicloj.clay.v2.make/make! {:source-path "src/clojure_experiments/visualizations/clay.clj"})
  ;;
  )

