(ns four-clojure.hello-world)

(= ((fn hello [name] (str "Hello, " name "!")) "Dave") "Hello, Dave!")

(= ((fn [name] (str "Hello, " name "!")) "Jenn") "Hello, Jenn!")

(= (#(str "Hello, " % "!") "Rhea") "Hello, Rhea!")
