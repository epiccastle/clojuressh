(ns io.epiccastle.cljssh-test
  (:require [clojure.test :refer [deftest is testing]]
            [io.epiccastle.cljssh :as cljssh]))

(deftest placeholder-test
  (testing "Library namespace loads"
    (is (some? (find-ns 'io.epiccastle.cljssh)))))
