(ns clojure-experiments.java.jfr
  "See https://cr.openjdk.java.net/~egahlin/jep-349/javadocs/api/jdk.jfr/jdk/jfr/consumer/"
  (:import
   (java.nio.file Path)
   (jdk.jfr Configuration)
   (jdk.jfr.consumer RecordingFile
                     RecordingStream)))

;;; https://cr.openjdk.java.net/~egahlin/jep-349/javadocs/api/jdk.jfr/jdk/jfr/consumer/RecordingFile.html
;;; https://cr.openjdk.java.net/~egahlin/jep-349/javadocs/api/jdk.jfr/jdk/jfr/consumer/RecordedEvent.html

(comment
  (def my-recording-file "my-jfr-profiling.jfr")

  (def my-events
    (->> (Path/of my-recording-file (make-array String 0))
         (RecordingFile/readAllEvents)
         (map bean)))


(first my-events)
;; => {:class jdk.jfr.consumer.RecordedEvent,
;;     :duration #object[java.time.Duration 0x6d1089ef "PT0S"],
;;     :endTime #object[java.time.Instant 0x14dbfa5a "2019-10-15T07:38:12.240645921Z"],
;;     :eventType #object[jdk.jfr.EventType 0x54244215 "jdk.jfr.EventType@54244215"],
;;     :fields
;;     [#object[jdk.jfr.ValueDescriptor 0x3d92e68f "jdk.jfr.ValueDescriptor@3d92e68f"] #object[jdk.jfr.ValueDescriptor 0x1b89417b "jdk.jfr.ValueDescriptor@1b89417b"] #object[jdk.jfr.ValueDescriptor 0x635d471e "jdk.jfr.ValueDescriptor@635d471e"] #object[jdk.jfr.ValueDescriptor 0x151f858a "jdk.jfr.ValueDescriptor@151f858a"] #object[jdk.jfr.ValueDescriptor 0x3f93af38 "jdk.jfr.ValueDescriptor@3f93af38"] #object[jdk.jfr.ValueDescriptor 0x6ec0a328 "jdk.jfr.ValueDescriptor@6ec0a328"] #object[jdk.jfr.ValueDescriptor 0x67ac7c59 "jdk.jfr.ValueDescriptor@67ac7c59"]],
;;     :stackTrace nil,
;;     :startTime #object[java.time.Instant 0x4e5638b9 "2019-10-15T07:38:12.240645921Z"],
;;     :thread
;;     #object[jdk.jfr.consumer.RecordedThread 0x63283271 "{\n  osName = \"Attach Listener\"\n  osThreadId = 3953\n  javaName = \"Attach Listener\"\n  javaThreadId = 3617\n  group = {\n    parent = N/A\n    name = \"system\"\n  }\n}\n"]}

  )


;;; Start recording and listen to events
;;; RecordingStream: https://docs.oracle.com/en/java/javase/15/docs/api/jdk.jfr/jdk/jfr/consumer/RecordingStream.html
(defn consumer [accept-fn]
  (reify
    java.util.function.Consumer
    (accept [this obj] (accept-fn obj))))

(defn recording
  "Starts blocking recording (via start method) using `RecordingStream`.
  The events will be processes using provided `accept-fn`."
  ([jfr-configuration accept-fn]
   (recording jfr-configuration accept-fn ["jdk.CPULoad" "jdk.JVMInformation"]))
  ([jfr-configuration accept-fn events]
   (let [consume (consumer accept-fn)]
     (with-open [rs (RecordingStream. jfr-configuration)]
       (doseq [e events]
         (.onEvent rs e consume))
       ;; https://docs.oracle.com/en/java/javase/15/docs/api/jdk.jfr/jdk/jfr/consumer/EventStream.html#startAsync()
       (.start rs)))))

(comment
  ;; blocks and keeps printing the CPULoad events to stdout
  (recording (Configuration/getConfiguration "default")
             println)

  ;; collects events in the atom on a separate thread
  (def my-events (atom []))
  (def my-recording (future (recording (Configuration/getConfiguration "default")
                      #(swap! my-events conj %))))
  .)


