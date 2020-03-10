(ns clojure-experiments.java.annotations)

;;; https://clojure.org/reference/datatypes#_java_annotation_support
;;;
(definterface Foo
  (^int foo []))

(deftype
 ^{Deprecated true
   Retention RetentionPolicy/RUNTIME
   javax.annotation.processing.SupportedOptions ["foo" "bar" "baz"]
   javax.xml.ws.soap.Addressing {:enabled false :required true}
   WebServiceRefs [(WebServiceRef {:name "fred" :type String})
                   (WebServiceRef {:name "ethel" :mappedName "lucy"})]}
 Bar
 [^int a
  ;; on field
  ^{:tag int
    Deprecated true
    Retention RetentionPolicy/RUNTIME
    javax.annotation.processing.SupportedOptions ["foo" "bar" "baz"]
    javax.xml.ws.soap.Addressing {:enabled false :required true}
    WebServiceRefs [(WebServiceRef {:name "fred" :type String})
                    (WebServiceRef {:name "ethel" :mappedName "lucy"})]}
  b]

  Foo
  ;; on method
  (^{Deprecated true
         Retention RetentionPolicy/RUNTIME
         javax.annotation.processing.SupportedOptions ["foo" "bar" "baz"]
         javax.xml.ws.soap.Addressing {:enabled false :required true}
         WebServiceRefs [(WebServiceRef {:name "fred" :type String})
                         (WebServiceRef {:name "ethel" :mappedName "lucy"})]}
    foo [this] 42))

(comment

  (seq (.getAnnotations Bar))
  (seq (.getAnnotations (.getField Bar "b")))
  (seq (.getAnnotations (.getMethod Bar "foo" nil)))
  
  )
