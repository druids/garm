(ns garm.spec-tools-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :as t]
            [garm.specs :as specs]
            [spec-coerce.core :as sc]))

(s/def ::a ::specs/int)
(s/def ::obj
  (s/keys :req-un [::a]))

(t/deftest coerce-test
  (t/testing "should coerce data"
    (t/is (= 1 (sc/coerce ::a "1"))))

  (t/testing "should coerce an object"
    (t/is (= {:a 1} (sc/coerce ::obj {:a "1"})))))
