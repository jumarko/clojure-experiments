(ns four-clojure.62-iterate
  "http://www.4clojure.com/problem/62
  Given a side-effect free function f and an initial value x
  write a function which returns an infinite lazy sequence of
  f x, (f x), (f (f x)), (f (f (f x))), etc.
  Special restrictions: `iterate`")

(defn my-iterate [f x]
  (cons x (lazy-seq (my-iterate f (f x)))))

(= (take 5 (my-iterate #(* 2 %) 1)) [1 2 4 8 16])

(= (take 100 (my-iterate inc 0)) (take 100 (range)))

(= (take 9 (my-iterate #(inc (mod % 3)) 1)) (take 9 (cycle [1 2 3])))
