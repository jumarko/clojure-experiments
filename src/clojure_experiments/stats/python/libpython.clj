(ns clojure-experiments.stats.python.libpython
  "NOTE: To use libpython-clj2 with jdk-17 you need to enable the foreign module - see https://github.com/clj-python/libpython-clj/blob/6e7368b44aaabddf565a5bbf3a240e60bf3dcbf8/deps.edn#L10
  Unfortunately, even with that, I'm getting the following error when loading the ns:
     Unable to find static field: ACC_OPEN in interface org.objectweb.asm.Opcodes.

  See also https://github.com/clj-python/libpython-clj/issues/212
  - JDK-18, 19 aren't at this time supported as they both have incompatible changes to the pathway from an integer to a direct buffer."
  (:require [libpython-clj2.require :refer [require-python]]
            [libpython-clj2.python :refer [py. py.. py.-] :as py]
            [tech.v3.datatype :as dtype]))

