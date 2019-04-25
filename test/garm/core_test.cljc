(ns garm.core-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :as t]
            [garm.core :as garm]
            [garm.specs :as specs]))

(s/def ::id specs/uuid)
(s/def ::price (s/and #?(:clj specs/decimal) specs/pos))
(s/def ::note (s/nilable specs/string))
(s/def ::qty (s/and specs/int (specs/in-range 1 10)))
(s/def ::created-at #?(:clj specs/inst
                       :cljs specs/string))
(s/def ::status (specs/enum #{:dispatched :received}))

(s/def ::order (s/keys :req-un [::id ::price ::qty ::status ::created-at]
                       :opt-un [::note]))

(s/def ::missing-reason int?)
(s/def ::object-wo-reason (s/keys :req-un [::missing-reason]))

(s/def ::address specs/non-blank)
(s/def ::contact (s/keys :req-un [::address]))

(s/def ::email ::specs/email)

#?(:clj (def now (java.time.Instant/now)))

(s/def ::nested-object
  (s/keys :req-un [::object-wo-reason]))

(s/def ::a (s/keys :req-un [::id]))
(s/def ::b (s/keys :req-un [::id ::price]))
(s/def ::c (s/or :a ::a :b ::b))

(s/def ::length (specs/length 1))
(s/def ::max-length (specs/max-length 4))
(s/def ::min-length (specs/min-length 4))
(s/def ::length-obj
  (s/keys :req-un [::length
                   ::max-length
                   ::min-length]))

(s/def ::bool ::specs/boolean)
(s/def ::bool-obj
  (s/keys :req-un [::bool]))

(s/def ::map ::specs/map)
(s/def ::map-obj
  (s/keys :req-un [::map]))

(t/deftest validate-test
  #?(:clj
     (t/testing "should validate given data"
       (t/are [expected spec-model data] (= expected
                                            (garm/validate spec-model data))
         [nil {:id [{:args []
                     :id ::specs/must-be-uuid
                     :message "Must be a UUID"}]
               :price [{:args []
                        :id ::specs/must-be-decimal
                        :message "Must be a decimal"}]
               :created-at [{:args []
                             :id :garm.specs/must-be-java-time-instant
                             :message "Must be valid java.time.Instant"}]
               :status [{:args []
                         :id :garm.core/missing-key
                         :message "This field is required"}]
               :qty [{:args [1 10]
                      :id :garm.specs/must-be-in-range
                      :message "Must be between %s and %s"}]}]
         ::order
         {:id "1234" :price -1 :qty 0 :created-at ""}

         [nil {:id [{:args []
                     :id :garm.core/missing-key
                     :message "This field is required"}]}]
         ::order
         {:price 100M :qty 1, :created-at now, :status :dispatched}

         [{:id #uuid "0428a077-96fd-4050-870e-ed1449266142"
           :price 100M
           :qty 1
           :foo 1
           :created-at now
           :status :dispatched}
          nil]
         ::order
         {:id #uuid "0428a077-96fd-4050-870e-ed1449266142"
          :price 100M
          :qty 1
          :created-at now
          :status :dispatched
          :foo 1}

         [{:id #uuid "0428a077-96fd-4050-870e-ed1449266142"
           :price 100M
           :qty 1
           :note "note"
           :created-at now
           :status :dispatched}
          nil]
         ::order
         {:id #uuid "0428a077-96fd-4050-870e-ed1449266142"
          :price 100M
          :qty 1
          :created-at now
          :status :dispatched
          :note "note"}

         [{:id #uuid "0428a077-96fd-4050-870e-ed1449266142"
           :price 100M
           :qty 1
           :note nil
           :created-at now
           :status :dispatched}
          nil]
         ::order
         {:id #uuid "0428a077-96fd-4050-870e-ed1449266142"
          :price 100M
          :qty 1
          :created-at now
          :status :dispatched
          :note nil}

         [{:id #uuid "0428a077-96fd-4050-870e-ed1449266142"
           :price 100M
           :qty 1
           :created-at now
           :status :dispatched}
          nil]
         ::order
         {:id #uuid "0428a077-96fd-4050-870e-ed1449266142"
          :price 100M
          :created-at now
          :qty 1
          :status :dispatched})))

  (t/testing "should return unknown-error caused by missing :reason"
    (t/is (= [nil
              {:missing-reason [{:args []
                                 :id :garm.core/unknown-error
                                 :message #?(:clj :clojure.core/int?
                                             :cljs :cljs.core/int?)}]}]
             (garm/validate ::object-wo-reason {:missing-reason ""}))))

  (t/testing "should validate e-mail addresses"
    (t/are [expected input] (= expected (garm/validate ::email input))
           [nil
            {nil [{:args []
                   :id :garm.specs/must-be-email-address
                   :message "Must be a valid e-mail address"}]}]
           nil

           [nil
            {nil [{:args []
                   :id :garm.specs/must-be-email-address
                   :message "Must be a valid e-mail address"}]}]
           "sasa@"

           ["foo@bar.com" nil]
           "foo@bar.com"

           ["FOO@BAR.COM" nil]
           "FOO@BAR.COM")))

(t/deftest a-test
  (t/is (= ["FOO" "foo"] (re-seq (re-pattern "(?i)foo") "FOO BAR foo bar")))

  (t/testing "should check non-blank spec"
    (t/are [expected input] (t/is (= expected (garm/validate ::contact input)))
           [{:address "asdf"} nil]
           {:address "asdf"}

           [nil {:address [{:args []
                            :id :garm.specs/must-not-be-blank
                            :message "Must not be blank"}]}]
           {:address " "}

           [nil {:address [{:args []
                            :id :garm.specs/must-not-be-blank
                            :message "Must not be blank"}]}]
           {:address nil}

           [nil {:address [{:args []
                            :id :garm.specs/must-not-be-blank
                            :message "Must not be blank"}]}]
           {:address ""}))

  (t/testing "should validate a nested object"
    (t/is (= [nil
              {[:object-wo-reason :missing-reason]
               [{:args []
                 :id :garm.core/missing-key
                 :message "This field is required"}]}]
             (garm/validate ::nested-object {:object-wo-reason {}}))))

  (t/testing "should not return duplicated errors for union types"
    (t/is (= [nil {:id [{:args []
                         :id :garm.core/missing-key
                         :message "This field is required"}]
                   :price [{:args []
                            :id :garm.core/missing-key
                            :message "This field is required"}]}]
             (garm/validate ::c {}))))

  (t/testing "should validate a boolean"
    (t/are [expected input] (= expected (garm/validate ::bool-obj input))
      [{:bool true} nil] {:bool true}
      [{:bool false} nil] {:bool false}
      [nil {:bool [{:args []
                    :id :garm.specs/must-be-boolean
                    :message "Must be a boolean"}]}]
      {:bool ""}))

  (t/testing "should validate a map"
    (t/are [expected input] (= expected (garm/validate ::map-obj input))
      [{:map {}} nil] {:map {}}
      [nil {:map [{:args []
                   :id :garm.specs/must-be-map
                   :message "Must be a map"}]}]
      {:map ""}
      [nil {:map [{:args []
                   :id :garm.specs/must-be-map
                   :message "Must be a map"}]}]
      {:map ""}))

  (t/testing "should validate length as valid"
    (t/is (= [{:length "a"
               :max-length "abcd"
               :min-length "abcd"}
              nil]
             (garm/validate ::length-obj {:length "a"
                                          :max-length "abcd"
                                          :min-length "abcd"}))))

  (t/testing "should return errors for lengths"
    (t/is (= [nil {:length [{:args [1]
                             :id :garm.specs/length
                             :message "Must have length equal to %s"}]
                   :max-length [{:args [4]
                                 :id :garm.specs/max-length
                                 :message "Must not be longer then %s"}]
                   :min-length [{:args [4]
                                 :id :garm.specs/min-length
                                 :message "Must not be shorter then %s"}]}]
             (garm/validate ::length-obj {:length ""
                                          :max-length "abcde"
                                          :min-length "abc"})))))


(t/deftest ->error-test
  (t/testing "should return an error message placeholder if missing"
    (t/is (= {:id :garm.core-test/error
              :title "MISSING ERROR MESSAGE"
              :message "MISSING ERROR MESSAGE"}
             (garm/->error {:id ::error}))))
  (t/testing "should format an error title"
    (t/is (= {:args [[1 2 3 4]]
              :id :garm.core-test/error
              :message "Values: %s"
              :title "Values: 1, 2, 3, 4"}
             (garm/->error {:id ::error
                            :message "Values: %s"
                            :args [[1 2 3 4]]})))))

#?(:clj
   (t/deftest ->json-api-response-test
     (t/testing "should return JSON API response"
       (t/are [expected spec-model data] (= expected
                                           (garm/->json-api-response
                                             (garm/validate spec-model data)))
         nil
         ::order
         {:id #uuid "0428a077-96fd-4050-870e-ed1449266142"
          :price 100M
          :qty 1
          :created-at now
          :status :dispatched}

         {:errors [{:id :id
                    :meta {:data [{:args []
                                   :id :garm.specs/must-be-uuid
                                   :message "Must be a UUID"
                                   :title "Must be a UUID"}]}}
                   {:id :price
                    :meta {:data [{:args []
                                   :id :garm.specs/must-be-decimal
                                   :message "Must be a decimal"
                                   :title "Must be a decimal"}]}}
                   {:id :qty
                    :meta {:data [{:args [1 10]
                                   :id :garm.specs/must-be-in-range
                                   :message "Must be between %s and %s"
                                   :title "Must be between 1 and 10"}]}}
                   {:id :created-at
                    :meta {:data [{:id :garm.specs/must-be-java-time-instant
                                   :message "Must be valid java.time.Instant"
                                   :title "Must be valid java.time.Instant"
                                   :args []}]}}
                   {:id :status
                    :meta {:data [{:id :garm.specs/must-be-one-of
                                   :message "Must be one of %s"
                                   :args [#{:dispatched :received}]
                                   :title "Must be one of received, dispatched"}]}}]}

         ::order
         {:id "1234" :price -1 :qty 0, :created-at "", :status :foo}))))
