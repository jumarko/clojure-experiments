^{:kindly/hide-code true
  :clay             {:title  "Macroexpand 2025 Noj: Clay Workshop"
                     :quarto {:author      :timothypratley
                              :description "What is Clay, why use it, and how to use it."
                              :type        :post
                              :date        "2025-10-16"
                              :category    :clay
                              :tags        [:clay :workflow]}}}
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
(+ 4 5)



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
