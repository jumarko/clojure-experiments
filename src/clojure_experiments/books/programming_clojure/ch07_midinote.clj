(ns clojure-experiments.books.programming-clojure.ch07-midinote
  (:import (javax.sound.midi MidiSystem)))

(defprotocol MidiNote
  (to-msec [this tempo])
  (key-number [this])
  (play [this tempo midi-channel]))

(defn perform [notes & {:keys [tempo] :or {tempo 120}}]
  (with-open [synth (doto (MidiSystem/getSynthesizer) .open)]
    (let [channel (aget (.getChannels synth) 0)]
      (doseq [note notes]
        (play note tempo channel)))))

(defrecord Note [pitch octave duration]
  MidiNote
  (to-msec [{:keys [duration]} tempo]
    (let [duration-to-bpm {1 240 1/2 120 1/4 60 1/8 30 1/16 15}]
      (* 1000 (/ (duration-to-bpm duration)
                 tempo))))
  (key-number [{:keys [octave pitch]}]
    (let [scale {:C 0 :C# 1 :Db 1 :D 2
                 :D# 3 :Eb 3 :E 4 :F 5
                 :F# 6 :Gb 6 :G 7 :G# 8
                 :Ab 8 :A 9 :A# 10 :Bb 10
                 :B 11}]
      (+ (* 12 (inc octave))
         (scale pitch))))
  (play [{:keys [velocity] :as this} tempo midi-channel]
    (let [velocity (or velocity 64)]
      (.noteOn midi-channel (key-number this) velocity)
      (Thread/sleep (to-msec this tempo)))))

(comment 
  (to-msec (->Note :D# 4 1/2)  120)
;; => 1000

  (def close-encounters [(->Note :D 3 1/2)
                         (->Note :E 3 1/2)
                         (->Note :C 3 1/2)
                         (->Note :C 2 1/2)
                         (->Note :G 2 1/2)
                         ])
  ;; TODO: try if it plays!!!
  (perform close-encounters)

  ;; another sample
  (def jaws (for [duration [1/2 1/2 1/4 1/4 1/8 1/8 1/8]
                  pitch [:E :F]]
              (Note. pitch 2 duration)))
  (perform jaws)

  ;; can use any clojure function that works on maps
  (perform (map #(update % :octave inc) close-encounters))

  (perform (map #(update % :octave dec) close-encounters))
  
  ;; end
  )
