(ns clojure-experiments.visualizations.cljfx
  "See https://github.com/cljfx/cljfx:
  I wanted to have an elegant, declarative and composable UI library for JVM and couldn't find one.
  Cljfx is inspired by react, reagent, re-frame and fn-fx.
  - Like react, it allows to specify only desired layout, and handles all actual changes underneath.
    Unlike react (and web in general) it does not impose xml-like structure of everything possibly having multiple children,
    thus it uses maps instead of hiccup for describing layout
  - Like reagent, it allows to specify component descriptions using simple constructs such as data and functions.
    Unlike reagent, it rejects using multiple stateful reactive atoms for state and instead prefers composing ui in more pure manner.
  - Like re-frame, it provides an approach to building large applications using subscriptions and events to separate view from logic.
    Unlike re-frame, it has no hard-coded global state, and subscriptions work on referentially transparent values instead of ever-changing atoms.
  - Like fn-fx, it wraps underlying JavaFX library so developer can describe everything with clojure data.
    Unlike fn-fx, it is more dynamic, allowing users to use maps and functions instead of macros and deftypes,
    and has more explicit and extensible lifecycle for components.
  "
  (:require [cljfx.api :as fx]))

;; simple window
(comment 
  (fx/on-fx-thread
   (fx/create-component
    {:fx/type :stage
     :showing true
     :title "Cljfx example"
     :width 300
     :height 100
     :scene {:fx/type :scene
             :root {:fx/type :v-box
                    :alignment :center
                    :children [{:fx/type :label
                                :text "Hello world"}]}}})))


;; to be useful we would need some rendered(def renderer
(comment 
  (def renderer (fx/create-renderer))

  (defn root [{:keys [showing]}]
    {:fx/type :stage
     :showing showing
     :scene {:fx/type :scene
             :root {:fx/type :v-box
                    :padding 50
                    :children [{:fx/type :button
                                :text "close"
                                :on-action (fn [_]
                                             (renderer {:fx/type root
                                                        :showing false}))}]}}})

  (renderer {:fx/type root
             :showing true})

  )


;;; But what we really want is to have a single global state as a value: https://github.com/cljfx/cljfx#atoms
;;; then derive our description of JavaFX state from this value and change the atom's contents instead

(comment

  ;; Define application state

  (def *state
    (atom {:title "App title"}))

  ;; Define render functions

  (defn title-input [{:keys [title]}]
    {:fx/type :text-field
     :on-text-changed #(swap! *state assoc :title %)
     :text title})

  (defn root [{:keys [title]}]
    {:fx/type :stage
     :showing true
     :title title
     :scene {:fx/type :scene
             :root {:fx/type :v-box
                    :children [{:fx/type :label
                                :text "Window title input"}
                               {:fx/type title-input
                                :title title}
                               ]}}})

  ;; Create renderer with middleware that maps incoming data - description -
  ;; to component description that can be used to render JavaFX state.
  ;; Here description is just passed as an argument to function component.

  (def renderer
    (fx/create-renderer
     :middleware (fx/wrap-map-desc assoc :fx/type root)))

  ;; Convenient way to add watch to an atom + immediately render app

  (fx/mount-renderer *state renderer)

;;
  )
