(ns noteboks.recursive)

(declare nodd?)

(defn neven? [n]
  (if (zero? n) true (nodd? (dec n))))

(defn nodd? [n]
  (if (zero? n) false (neven? (dec n))))

(declare is-prime)

(def odds (iterate #(+ % 2) 3))

(def primes (cons 2 (filter is-prime odds)))

(defn prime-divisors [n]
  (filter #(zero? (mod n %))
          (take-while #(<= % (Math/ceil (Math/sqrt n)))
                      primes)))

(defn is-prime [n] (empty? (prime-divisors n)))

(take 10 primes)
