(ns clojuressh-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojuressh :as clojuressh]))

(deftest placeholder-test
  (testing "Library namespace loads"
    (is (find-ns 'clojuressh))))
