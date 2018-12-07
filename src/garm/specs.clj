(ns garm.specs
  (:refer-clojure :exclude [int bigdec])
  (:require
   [clojure.spec.alpha :as s]
   [spec-tools.core :as st]))

(def pos (st/spec (s/def ::pos pos?) {:reason {:id ::must-be-positive
                                               :message "Must be positive"
                                               :args []}}))

(def int (st/spec (s/def ::int int?) {:reason {:id ::must-be-integer
                                               :message "Must be an integer"
                                               :args []}}))

(def string (st/spec (s/def ::string string?) {:reason {:id ::must-be-string
                                                        :message "Must be a string"
                                                        :args []}}))

(def uuid (st/spec (s/def ::uuid uuid?) {:reason {:id ::must-be-uuid
                                                  :message "Must be a UUID"
                                                  :args []}}))

(def decimal (st/spec (s/def ::decimal decimal?) {:reason {:id ::must-be-decimal
                                                           :message "Must be a decimal"
                                                           :args []}}))

(def inst (st/spec (s/def ::inst inst?)
                   {:reason {:id ::must-be-java-time-instant
                             :message "Must be valid java.time.Instant"
                             :args []}}))

(defn in-range
  [min max]
  (st/spec (s/def ::in-range #(<= min % max))
           {:reason {:id ::must-be-in-range
                     :message "Must be between %s and %s"
                     :args [min max]}}))

(defn enum
  [values]
  (st/spec (s/def ::enum (partial contains? values))
           {:reason {:id ::must-be-one-of
                     :message "Must be one of %s"
                     :args [values]}}))
