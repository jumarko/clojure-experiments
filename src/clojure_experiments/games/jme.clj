(ns clojure-experiments.games.jme
  "Experiments with jMonkeyEngine via jme-clj: https://github.com/ertugrulcetin/jme-clj
  Check quick intro video: https://www.youtube.com/watch?v=IOPz9I49snM"
  (:require [jme-clj.core :as j])
  (:import (com.jme3.math ColorRGBA)))


;;; From https://github.com/ertugrulcetin/jme-clj#usage
;;;
(defn init []
  (let [box  (j/box 1 1 1)
        geom (j/geo "Box" box)
        mat  (j/material "Common/MatDefs/Misc/Unshaded.j3md")]
    (j/set* mat :color "Color" ColorRGBA/Blue)
    (j/set* geom :material mat)
    (j/add-to-root geom)))

(comment
  (j/defsimpleapp app :init init)

  (j/start app)

  ;;
  )


;;; REPL-based tutorial: https://github.com/ertugrulcetin/jme-clj/blob/master/test/examples/repl_based_tutorial.clj
;;;
;; Our simple init fn. It creates a cube with jMonkeyEngine's logo on the surface.
 (defn init []
   (let [cube (j/geo "jMonkey cube" (j/box 1 1 1))
         mat  (j/unshaded-mat)]
     (j/set* mat :texture "ColorMap" (j/load-texture "Interface/Logo/Monkey.jpg"))
     (j/set* cube :material mat)
     (j/add-to-root cube)))

 ;; Let's create simple-update fn with no body for now.
 (defn simple-update [tpf])

 ;; We define the `app` var.
(comment
  
  (j/defsimpleapp app
    :init init
    :update simple-update)

  ;; Here we go. You'll see jMonkeyEngine's dialog box. After hitting the "Continue" button,
  ;; our app will start.
  (j/start app)

  ;;
  )

 ;; It's time to add some magic. Let's rotate the cube. First of all, we're going to re-define the init fn.
 ;; Can you spot the change? Yes, we're returning a hash map that holds cube data.
 ;; When init fn returns a hash map, its value is registered into the app's global mutable state,
 ;; so other fns can access it for later use. This applies to the update fn as well.
 (defn init []
   (let [cube (j/geo "jMonkey cube" (j/box 1 1 1))
         mat  (j/unshaded-mat)]
     (j/set* mat :texture "ColorMap" (j/load-texture "Interface/Logo/Monkey.jpg"))
     (j/set* cube :material mat)
     (j/add-to-root cube)
     {:cube cube}))

 ;; Let's call re-init (Re-initializes the app), so we can let the app know about the changes.
 ;; re-init is like restart but does not call stop and start fns.
(comment
  
  (j/run app
    (j/re-init init))
  ;;
  )

 ;; Now, we're filling simple-update's body.
 ;; When you focus your cursor on the app, you'll see that the cube is rotating. Update fn (simple-update) does not
 ;; need something like `re-update` fn. init fn is only called once. That's why we re-initialize with re-init.
 ;; On the other hand, the app is constantly calling the simple-update fn.
 (defn simple-update [tpf]
   (let [{:keys [cube]} (j/get-state)]
     (j/rotate cube 0 (* 2 tpf) 0)))

 ;; `unbind-app` destroys the app, unbinds the `app` var. Which means it's gone for good. It can be used for
 ;; development purposes, e.g. when you want to create an app with different attributes.
 (j/unbind-app #'app)

 ;; Let's create an app with different options.
 (j/defsimpleapp app
               :opts {:show-settings?       false
                      :pause-on-lost-focus? false
                      :settings             {:title          "My JME Game"
                                             :load-defaults? true
                                             :frame-rate     60
                                             :width          800
                                             :height         600}}
               :init init
               :update simple-update)

(comment
 ;; We can start the new app. Now, you won't see jMonkeyEngine's dialog because we set :show-settings? to false.
 ;; Since we set :pause-on-lost-focus? to false, we don't have to focus the cursor, the cube is rotating.
 ;; FPS limit set to 60, and we have a new title `My JME Game`. Finally, resolution is 800x600.
 (j/start app)

 ;; We can change the cube's position by setting its local translation.
 ;; Please note that every state change related code should we wrapped with run macro!
 (j/run app
      (let [{:keys [cube]} (j/get-state)]
        (j/set* cube :local-translation (j/add (j/get* cube :local-translation) 1 1 1))))

 ;; By default, there is a Fly Camera attached to the app that you can control with W, A, S and D keys.
 ;; Let's increase its movement speed. Now, you fly faster :)
 (j/run app
      (j/set* (j/fly-cam) :move-speed 15))

 ;; It would be cool to add a new cube. Let's go for it.
 (j/run app
      (let [cube (j/geo "jMonkey cube" (j/box 1 1 1))
            mat  (j/unshaded-mat)]
        (j/set* mat :texture "ColorMap" (j/load-texture "Interface/Logo/Monkey.jpg"))
        (j/setc cube
              :material mat
              :local-translation [-3 0 0])
        (j/add-to-root cube)
        (j/set-state :cube2 cube)))

 ;;

 )

 ;; We added the new cube, but it's not rotating. We need to update the simple-update fn.
 (defn simple-update [tpf]
   (let [{:keys [cube cube2]} (j/get-state)]
     (j/rotate cube 0 (* 2 tpf) 0)
     (j/rotate cube2 0 (* 2 tpf) 0)))

 ;; I hope you liked the tutorial! One of the best things about writing a game in Clojure is that
 ;; you can modify it in a REPL while it's running. It's cool to have hot reload for 3D game development.

 ;; If you have any questions, please reach me out through https://github.com/ertugrulcetin/jme-clj/issues
 
