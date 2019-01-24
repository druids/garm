(ns garm.spec-tools-test
  (:require [clojure.test :as t]
            [garm.specs :as specs]
            [spec-coerce.core :as sc]))

(t/deftest coerce-test
  (t/testing "should coerce data"
    (t/is (= 1 (sc/coerce ::specs/int "1")))))
