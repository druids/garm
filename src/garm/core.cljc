(ns garm.core
  (:require
   #?(:clj
      [clojure.spec.alpha :as s])
   #?(:cljs
      [cljs.spec.alpha :as s])
   [spec-tools.core :as st]
   [garm.spec-tools]
   #?(:cljs
      [goog.string.format])
   #?(:cljs
      [goog.string :refer [format]])))

(defn missing-key?
  "Returns `true` is a given `problem` is missing key"
  [problem]
  (and (-> problem :pred sequential?)
       (<= 3 (-> problem :pred count))
       (<= 1 (-> problem :pred (nth 2) count))
       (= "contains?" (-> problem :pred (nth 2) (nth 0) name))))

(defn problem->id
  "Returns an `:id` of a given `problem`, it could return `nil`.
  It returns a `vector` for nested keys, for flat structure return `keyword`,
  otherwise `nil`."
  [problem]
  (let [id
        (if (missing-key? problem)
          (conj (:in problem) (-> problem :pred (nth 2) (nth 2)))
          (:in problem))]
    (case (count id)
      1 (first id)
      0 nil
      id)))

(defn problem->reason
  "Returns a `reason` map of a given `problem`"
  [problem]
  (cond
    (-> problem :reason some?)
    (:reason problem)

    (missing-key? problem)
    {:id ::missing-key, :message "This field is required", :args []}

    :else
    {:id ::unknown-error
     :message (-> problem :pred str keyword)
     :args []}))

(defn nilable-spec?
  [problem]
  (= ::s/nil (-> problem :path last)))

(defn validate
  "Validates given `data` with a given `spec-model`. It return a tuple like
  `[valid-data nil]` if the `data` are valid, otherwise it returns
  `[nil errors-map]`
  `errors-map` looks like:
  {:id [{:args [], :id ::specs/must-be-uuid, :message \"Must be a UUID\"}]
   :price [{:args [], :id ::specs/must-be-decimal, :message \"Must be a decimal\"}]
   :qty [{:args [1 10]
          :id :garm.specs/must-be-in-range
          :message \"Must be between %s and %s\"}]}]
  For nested structures it returns a `vector` as an ID instead of `keyword`."
  [spec-model data]
  (if (s/valid? spec-model data)
    [data nil]
    (let [explained-data (s/explain-data spec-model data)]
      [nil (->> explained-data
                ::s/problems
                (reduce (fn [acc problem]
                          (if (nilable-spec? problem)
                            ;; skip error message for nilables
                            acc
                            (update acc
                                    (problem->id problem)
                                    conj
                                    (problem->reason problem))))
                        {})
                (reduce-kv #(assoc %1 %2 (dedupe %3)) {}))])))

(defmulti ->str type)

#?(:clj
   (defmethod ->str clojure.lang.Keyword
     [value]
     (name value)))

#?(:cljs
   (defmethod ->str cljs.core.Keyword
     [value]
     (name value)))

(defmethod ->str :default
  [value]
  (str value))

(defn- format-placeholder
  [value]
  (if (coll? value)
    (->> value
         (map ->str)
         (clojure.string/join ", "))
    value))

(defn ->error
  [prob]
  (let [message (if (-> prob :message string?)
                  (:message prob)
                  "MISSING ERROR MESSAGE")]
    (merge prob
           {:title (apply format (cons message (map format-placeholder (:args prob))))
            :message message})))

(defn- conj-error
  [acc [id errors]]
  (conj acc {:id id
             :meta {:data (map ->error errors)}}))

(defn ->json-api-response
  "Converts a result tuple of `validate` function to JSON API errors format.
  It returns `nil` when the result of `validate` function is successful.
  https://jsonapi.org/format/#errors"
  [[data explained-data]]
  (when (some? explained-data)
    {:errors (reduce conj-error [] explained-data)}))
