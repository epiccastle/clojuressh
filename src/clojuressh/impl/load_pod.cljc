(ns clojuressh.impl.load-pod
  #?(:bb (:require [babashka.pods :as pods])))

#?(:bb (pods/load-pod 'epiccastle/bbssh "0.7.0"))
