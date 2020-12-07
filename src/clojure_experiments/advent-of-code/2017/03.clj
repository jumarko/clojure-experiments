(ns advent-of-clojure.2017.03
  "Day 3 of Advent of Clojure 2017 - Spiral Memory: http://adventofcode.com/2017/day/3

  Part I:
  ------------------------------------------------------------------------------
  You come across an experimental new kind of memory stored on an infinite two-dimensional grid.

  Each square on the grid is allocated in a spiral pattern starting at a location marked 1
  and then counting up while spiraling outward. For example, the first few squares are allocated like this:

  17  16  15  14  13
  18   5   4   3  12
  19   6   1   2  11
  20   7   8   9  10
  21  22  23---> ...

  While this is very space-efficient (no squares are skipped), requested data must be carried back
  to square 1 (the location of the only access port for this memory system)
  by programs that can only move up, down, left, or right.
  They always take the shortest path: the Manhattan Distance between the location of the data and square 1.
  
  For example:
  
    Data from square 1 is carried 0 steps, since it's at the access port.
    Data from square 12 is carried 3 steps, such as: down, left, left.
    Data from square 23 is carried only 2 steps: up twice.
    Data from square 1024 must be carried 31 steps.

  How many steps are required to carry the data from the square identified in your puzzle input
  all the way to the access port?

  Your puzzle input is 325489.

  Part II:
  ------------------------------------------------------------------------------
  ")

;; PART I:
;; The key idea here is to understand that the count of numbers is equal to the squares:
;; 1^2, 3^2, 5^2, 7^2, 9^2.
;; So we can easily determine to which "layer" the given input number belongs.
;; The number of layer gives us the distance automatically.
;; To find the number of layer we just need to use square root.

(defn- layer-number
  "Returns the number of 'layer' to which given number belongs.
  If `x` is the layer number, then the final distance is in interval <x; 2x>."
  [input-square-number]
  (-> input-square-number
      Math/sqrt
      Math/ceil
      int
      ;; finally we have a square root and we need to get number of layer
      ;; this is done by dividing by 2 because all numbers up to 3^2 belong to the first layer
      ;; all numbers up to 5^2 belong to the second layer, all numbers up to 7^2 belong to the third layer, etc.
      (quot 2)))

(defn spiral-memory-steps-count
  [input-square-number]
  (let [layer-num (layer-number input-square-number)
        layer-width (* 2 layer-num)
        max-num-in-layer (* (inc layer-width) (inc layer-width))
        max-num-diff (- max-num-in-layer input-square-number)
        max-num-diff-mod (mod max-num-diff layer-width)]
    (if (zero? max-num-diff-mod)
      layer-width
      (max layer-num max-num-diff-mod))))

(spiral-memory-steps-count 325489)
