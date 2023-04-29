(ns clojure-experiments.random
  "Random Numbers and generators.")


;;; Exploration of rand-nth, rand-int and Math/random
;;; It should be pretty clear that they are _not_ secure (to generate unguessable random numbers)
;;; See https://franklinta.com/2014/08/31/predicting-the-next-math-random-in-java/
;;; and the code here: https://github.com/fta2012/ReplicatedRandom
;;;
;;; passing a single random double to ReplicatedRandom is enough to reliable generate the next random number!
;;;
;;; This is a naive rewrite of the Java code to Clojure without
;;; understanding the logic and doing any optimizations

;; constants copied from java.util.Random
(def ^:const multiplier 0x5DEECE66D)
(def ^:const addend 0xB)
(def ^:const mask (dec (bit-shift-left 1 48)))


(defn- possible-seeds [upper-m-of-48-mask old-seed-upper-n n new-seed-upper-m]
  ;; Bruteforce the lower (48 - n) bits of the oldSeed that was truncated.
  ;; Calculate the next seed for each guess of oldSeed and check if it has the same top m bits as our newSeed.
  ;; If it does then the guess is right and we can add that to our candidate seeds.

  (loop [old-seed old-seed-upper-n
         seeds []]
    ;; notice `unchecked-multiply` - the numbers being multiplied are large
    ;; and will cause a long overflow. The Java code simply uses the overflowed result
    (let [new-seed (bit-and (+ (unchecked-multiply old-seed multiplier) addend)
                            mask)]
      (if (<= old-seed (bit-or old-seed-upper-n
                               (dec (bit-shift-left 1 (- 48 n)))))
        (recur (inc old-seed)
               (if (= new-seed-upper-m
                      (bit-and new-seed upper-m-of-48-mask))
                 (conj seeds new-seed)
                 seeds))
        seeds))))


(defn replicate [random-instance nextn, n, nextm, m]
  (let [upper-m-of-48-mask (bit-shift-left (dec (bit-shift-left 1 m))
                                           (- 48 m))
        ;; next(x) is generated by taking the upper x bits of 48 bits of (oldSeed * multiplier + addend) mod (mask + 1)
        ;; So now we have the upper n and m bits of two consecutive calls of next(n) and next(m)
        old-seed-upper-n (bit-and (bit-shift-left nextn (- 48 n))
                                  mask)
        new-seed-upper-m (bit-and (bit-shift-left nextm (- 48 m))
                                  mask)

        seeds (possible-seeds upper-m-of-48-mask old-seed-upper-n n new-seed-upper-m)]
    (cond
      (= 1 (count seeds))
      ;; setSeed(x) sets seed to `(x ^ multiplier) & mask`, so we need another `^ multiplier` to cancel it out
      (doto random-instance (.setSeed (bit-xor (first seeds) multiplier)))

      (< 1 (count seeds))
      (println "didn't find a unique seed. possible seeds were: " seeds)

      :else (println "failed to find seed"))))

(defn replicate-random
  ([random-double]
   (replicate-random (java.util.Random.) random-double))
  ([random-instance random-double]
   (let [numerator (long (* random-double (bit-shift-left 1 53)))
         first26 (int (unsigned-bit-shift-right numerator 27))
         last27 (int (bit-and numerator
                              (dec (bit-shift-left 1 27))))]
     (replicate random-instance first26, 26, last27, 27))))

(comment
  (def my-replicated-random (replicate-random (rand)))

  (.nextDouble my-replicated-random)
  ;; => 0.9583403954404643
  (rand)
  ;; It's same !!!
  ;; => 0.9583403954404643
  )

