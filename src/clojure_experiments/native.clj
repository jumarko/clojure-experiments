(ns clojure-experiments.native
  (:import (com.sun.jna Native Pointer)))

;;; Accessing native shared libraries from Clojure via JNA
;;; https://nakkaya.com/2009/11/16/java-native-access-from-clojure/
;;; See https://github.com/java-native-access/jna
;;; notice that you need to add `net.java.dev.java/jna` library on classpath
(comment
  "Dangerous area - native code handling!!!"

  (gen-interface
   :name jna.CLibrary
   :extends [com.sun.jna.Library]
   :methods [[printf [String] void]])

  ;; create an instance
  (def glibc (com.sun.jna.Native/loadLibrary "c" jna.CLibrary))

  ;; and call!
  (.printf glibc "Hello, World.. \n")
  (System/getProperty "java.library.path")

  ;; using `getFunction` we don't need to specify every used native function beforehand
  ;; we can just look up them at runtime
  (.invoke (com.sun.jna.Function/getFunction "c" "printf") Integer (to-array ["Hello, World"]))

  ;; let's a define a handy macro for dynamic function lookup and call
  (defmacro jna-call [lib func ret & args]
    `(let [library#  (name ~lib)
           function# (com.sun.jna.Function/getFunction library# ~func)]
       (.invoke function# ~ret (to-array [~@args]))))

  (jna-call :c "printf" Integer "Hello, World!")
  ;;Some POSIX Calls
  (jna-call :c "mkdir" Integer "/tmp/jnatesttemp" 07777)
  (jna-call :c "rename" Integer "/tmp/jnatesttemp" "/tmp/jnatesttempas")
  (jna-call :c "rmdir" Integer "/tmp/jnatesttempas")

  ;; for more complex native calls we need a way how pass pointers into functions
  ;; which use them as output arguments (fill them with return data)
  ;; You give JNA a ByteBuffer it will give you a pointer, you can pass this Pointer around instead of a Structure.
  (defmacro jna-malloc [size]
    `(let [buffer# (java.nio.ByteBuffer/allocateDirect ~size)
           pointer# (Native/getDirectBufferPointer buffer#)]
       (.order buffer# java.nio.ByteOrder/LITTLE_ENDIAN)
       {:pointer pointer# :buffer buffer#}))

  ;; final program using jna-malloc
  (let [struct (jna-malloc 44)]
    (let [jna-call-result (jna-call :c "statvfs" Integer "/tmp" (:pointer struct))]
      (println "jna-call-result:" jna-call-result))
    (let [fbsize (.getInt (:buffer struct))
          frsize (.getInt (:buffer struct) 4)
          blocks (.getInt (:buffer struct) 8)
          bfree (.getInt (:buffer struct) 12)
          bavail (.getInt (:buffer struct) 16)]

      (println "f_fbsize" fbsize)
      (println "f_frsize" frsize)
      (println "blocks" blocks)
      (println "bfree" bfree)
      (println "bavail" bavail)))



  ;;; Kerberos library
  ;;; TODO: rethink the usage of PointerByReference - perhaps just Pointer?
  (gen-interface
   :name jna.KerberosLibrary
   :extends [com.sun.jna.Library]
   ;; notice the usage of FQN com.sun.jna.Pointer which is necessary
   ;; even if you import this class
   :methods [[krb5_init_context [com.sun.jna.Pointer] void]
             [cc_initialize [com.sun.jna.Pointer, com.sun.jna.Pointer, com.sun.jna.Pointer, com.sun.jna.Pointer] void]
             [krb5_cc_default [com.sun.jna.Pointer, com.sun.jna.Pointer] void]])

  (def kerberos-lib (com.sun.jna.Native/loadLibrary "Kerberos" jna.KerberosLibrary))

  (def k-context-buffer (java.nio.ByteBuffer/allocateDirect 10000))
  (def k-context-pointer (Native/getDirectBufferPointer k-context-buffer))
  #_(def k-context (com.sun.jna.Memory. 100))
  (.krb5_init_context kerberos-lib k-context-pointer)

  ;; and continue with calls using kcontext...
  ;; check klist implementation: https://opensource.apple.com/source/Kerberos/Kerberos-65.15/KerberosClients/klist/Sources/klist.c.auto.html

  (def cc-context-buffer (java.nio.ByteBuffer/allocateDirect 10000))
  (def cc-context-pointer (Native/getDirectBufferPointer cc-context-buffer))
  (.cc_initialize kerberos-lib cc-context-pointer nil nil nil)

  ;; check http://web.mit.edu/kerberos/krb5-current/doc/appldev/refs/api/krb5_cc_default.html
  (def main-ccache-buffer (java.nio.ByteBuffer/allocateDirect 10000))
  (def main-ccache-pointer (Native/getDirectBufferPointer main-ccache-buffer))
  ;; TODO: how to get proper result ??
  ;; This fails - WHY?
  #_(.krb5_cc_default kerberos-lib k-context-pointer main-ccache-pointer)


  )


