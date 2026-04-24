(ns cljssh-test
  (:require [clojure.test :refer [deftest is testing]]
            [cljssh :as cljssh]))

(deftest placeholder-test
  (testing "Library namespace loads"
    (is (find-ns 'cljssh))))
