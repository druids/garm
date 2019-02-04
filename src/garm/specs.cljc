(ns garm.specs
  (:refer-clojure :exclude [boolean int bigdec uuid keyword])
  (:require
   [clojure.spec.alpha :as s]
   [spec-tools.core :as st]
   [spec-tools.spec :as ss]))

(def boolean (assoc ss/boolean? :reason {:id ::must-be-boolean
                                         :message "Must be a boolean"
                                         :args []}))
(s/def ::boolean boolean)

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

(def non-blank (st/create-spec {:spec (complement clojure.string/blank?)
                                :reason {:id ::must-not-be-blank
                                         :message "Must not be blank"
                                         :args []}}))
(s/def ::non-blank non-blank)

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

(def ^{:doc "Taken from https://www.regular-expressions.info/email.html"
       :const true}
  email-regex
  (re-pattern
   (str "\\A[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
        "@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\z")))

(def email (st/create-spec
            {:spec #(and (string? %)
                         (re-seq email-regex %))
             :type :string
             :reason {:id ::must-be-email-address
                      :message "Must be a valid an e-mail address"
                      :args []}}))

(s/def ::email email)

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

(defn- length-validation
  [op id message length]
  (st/create-spec {:spec #(try
                            (op (count %) length)
                            #?(:clj
                               (catch java.lang.UnsupportedOperationException _ false))
                            #?(:cljs
                               (catch :default _ false)))
                   :reason {:id id
                            :message message
                            :args [length]}}))

(def length (partial length-validation = ::length "Must have length equal to %s"))

(def max-length (partial length-validation <= ::max-length "Must not be longer then %s"))

(def min-length (partial length-validation >= ::min-length "Must not be shorter then %s"))
