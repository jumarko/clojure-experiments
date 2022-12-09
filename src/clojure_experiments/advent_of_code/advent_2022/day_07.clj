(ns clojure-experiments.advent-of-code.advent-2022.day-06
  "https://adventofcode.com/2022/day/7
  Input: https://adventofcode.com/2022/day/7/input

  The disk of your device is full and you need to navigate around the file system
  and find directories that are good candidates for deletion.
  Thus, your task is to determine the total size of each directory.
  "
  (:require
   [clojure-experiments.advent-of-code.advent-2022.utils :as utils]
   [clojure-experiments.macros.macros  :refer [assert=]]
   [clojure.string :as str]))

(def full-input (utils/read-input "07"))

(def sample-input "$ cd /
$ ls
dir a
14848514 b.txt
8504156 c.dat
dir d
$ cd a
$ ls
dir e
29116 f
2557 g
62596 h.lst
$ cd e
$ ls
584 i
$ cd ..
$ cd ..
$ cd d
$ ls
4060174 j
8033020 d.log
5626152 d.ext
7214296 k")


;;; Part 1
;;; To being, final all the directories with a total size _at most_ 100,000,
;;; then calculate the sum of their total sizes
;;; - In the above sample, these are `a` and `e` with sum 95437 (94853 + 584)

(defn parse-ls-output [output-lines]
  (map (fn [line]
         (let [[f s] (str/split line #" ")]
           (if (= f "dir")
             {:type :dir :name s}
             {:type :file :name s :size (parse-long f)})))
       output-lines))

(parse-ls-output [ "dir a" "14848514 b.txt" "8504156 c.dat" "dir d" ])

(defn parse-commands [input]
  (let [commands (->> (str/split input #"\$ ")
                      (remove str/blank?)
                      (map str/split-lines))]
    ;; transform commands by to have structure like {:command :ls :dir "a" :result ["dir e" "2557 g" "62596 h.lst"]}
    (first (reduce
            (fn [[cmds current-dir] [cmd & args]]
              (if (= cmd "ls")
                [(conj cmds {:cmd :ls :dir current-dir :result (parse-ls-output args)})
                 current-dir]
                (let [dir (-> cmd (str/split #" ") second)]
                  [(conj cmds {:cmd :cd :dir dir})
                   dir])))
            [[] nil]
            commands))))


(parse-commands sample-input)
;; => [{:cmd :cd, :dir "/"} {:cmd :ls, :dir "/", :result ({:type :dir, :name "a"} {:type :file, :name "b.txt", :size 14848514} {:type :file, :name "c.dat", :size 8504156} {:type :dir, :name "d"})} {:cmd :cd, :dir "a"} {:cmd :ls, :dir "a", :result ({:type :dir, :name "e"} {:type :file, :name "f", :size 29116} {:type :file, :name "g", :size 2557} {:type :file, :name "h.lst", :size 62596})} {:cmd :cd, :dir "e"} {:cmd :ls, :dir "e", :result ({:type :file, :name "i", :size 584})} {:cmd :cd, :dir ".."} {:cmd :cd, :dir ".."} {:cmd :cd, :dir "d"} {:cmd :ls, :dir "d", :result ({:type :file, :name "j", :size 4060174} {:type :file, :name "d.log", :size 8033020} {:type :file, :name "d.ext", :size 5626152} {:type :file, :name "k", :size 7214296})}]

;; file system is a tree
(defn file-system [commands]
  (reduce
   (fn [tree cmd]
     (case cmd
       ;; when it's :cd then add children;
       :cd (conj tree (:dir cmd))
       ;; when it's :ls then add siblings
       :ls (into tree (:result cmd)))
     )
   []
   commands)

  #_[{:type :dir :name "/"}
     [{:type :dir, :name "a"}
      {:type :dir, :name "e"}
      {:type :file, :name "f", :size 29116}
      {:type :file, :name "g", :size 2557}
      {:type :file, :name "h.lst", :size 62596}]
     {:type :file, :name "b.txt", :size 14848514}
     {:type :file, :name "c.dat", :size 8504156}
     {:type :dir, :name "d"}])

(->> sample-input
     parse-commands
     file-system)


(defn calculate-sizes [file-system]
  ;; TODO: calculate and add `:size` to all the elements that do not have it (that is directories)
  ;; do this recursively, bottom up
  ;; try to use `clojure.walk`
  )



;;; => Giving up - copy the solution from here: https://gitlab.com/maximoburrito/advent2022/-/blob/main/src/day07/main.clj

;; read input
;; solutions
(defn run [input]
  ;; run the input to produce a fs tree
  (loop [cmds input
         dir []
         data {}]
    (if-let [cmd (first cmds)]
      (cond
        (= "$ ls" cmd) (recur (rest cmds) dir data)

        (str/starts-with? cmd "dir ") (recur (rest cmds) dir data)

        (str/starts-with? cmd "$ cd ")
        (let [cd (subs cmd 5)]
          (cond
            (= "/" cd) (recur (rest cmds) ["/"] data)
            (= ".." cd) (recur (rest cmds) (pop dir) data)
            :else (recur (rest cmds) (conj dir cd) data)))

        :else (let [[_ size name] (re-matches #"(\d+) (.+)" cmd)]
                (recur (rest cmds)
                       dir
                       (update-in data dir assoc name (parse-long size)))))
      data)))

(defn fs-size [fs]
  ;; return the total directory size of a fs
  (reduce +
          (for [[k v] fs]
            (if (number? v)
              v
              (fs-size v)))))

(defn all-dirs [fs path]
  ;; return a list of all non-leaf paths
  (apply concat
         (when (seq path) [path])
         (for [[k v] fs
               :when (map? v)]
           (all-dirs v (conj path k)))))

(defn sum [ns] (reduce + ns))

(defn part1 [input]
  ;; really inefficient, re-calculating the size of every sub-directory multiple times
  (let [fs (run input)
        dirs (all-dirs fs [])]
    (sum
     (for [dir dirs
           :let [size (fs-size (get-in fs dir))]
           :when (>= 100000 size)]
       size))))

(defn part2 [input]
  (let [fs (run input)
        dirs (all-dirs fs [])
        used (fs-size fs)
        need (- used 40000000)]
    ;; find the smallest directory bigger than the space needed
    (-> (for [dir dirs
              :let [size (fs-size (get-in fs dir))]
              :when (>= size need)]
          size)
        sort
        first)))
(comment
  (part1 full-input)
;; => 1581595
  (part2 full-input)
;; => 1544176
)
