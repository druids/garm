(ns garm.specs
  (:refer-clojure :exclude [int bigdec uuid keyword])
  (:require
   [clojure.spec.alpha :as s]
   [spec-tools.core :as st]
   [spec-tools.spec :as ss]))

(def pos (assoc ss/pos? :reason {:id ::must-be-positive
                                 :message "Must be positive"
                                 :args []}))
(s/def ::pos pos)

(def int (assoc ss/int? :reason {:id ::must-be-integer
                                 :message "Must be an integer"
                                 :args []}))
(s/def ::int int)

(def number (assoc ss/number? :reason {:id ::must-be-number
                                       :message "Must be a number"
                                       :args []}))
(s/def ::number number)

(def string (assoc ss/string? :reason {:id ::must-be-string
                                       :message "Must be a string"
                                       :args []}))
(s/def ::string string)

(def keyword (assoc ss/keyword? :reason {:id ::must-be-keyword
                                         :message "Must be a keyword"
                                         :args []}))
(s/def ::keyword keyword)

(def uuid (assoc ss/uuid? :reason {:id ::must-be-uuid
                                   :message "Must be a UUID"
                                   :args []}))
(s/def ::uuid uuid)

#?(:clj
   (def decimal (assoc ss/decimal? :reason {:id ::must-be-decimal
                                            :message "Must be a decimal"
                                            :args []})))
#?(:clj
   (s/def ::decimal decimal))

#?(:clj
   (def inst (assoc ss/inst? :reason {:id ::must-be-java-time-instant
                                      :message "Must be valid java.time.Instant"
                                      :args []})))
#?(:clj
   (s/def ::inst inst))

(defn in-range
  [min max]
  (st/create-spec {:spec #(<= min % max)
                   :reason {:id ::must-be-in-range
                            :message "Must be between %s and %s"
                            :args [min max]}}))

(defn enum
  [values]
  (st/create-spec {:spec (partial contains? values)
                   :reason {:id ::must-be-one-of
                            :message "Must be one of %s"
                            :args [values]}}))
