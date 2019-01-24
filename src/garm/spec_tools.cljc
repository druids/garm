(ns garm.spec-tools
  (:require
   [spec-coerce.core :as sc]
   [spec-tools.core :as st]))

(defmulti type->coercer type)

(defmethod type->coercer :default
  [_]
  identity)

(defmethod type->coercer #?(:clj clojure.lang.Cons) #?(:cljs cljs.core.Cons)
  [x]
  ;; Spec Record could be in a form e.g.: (spec-tools.core/spec
  ;; {:reason :must-be-number :spec clojure.core/number? :type :double})
  (-> x rest first :spec sc/sym->coercer))

(defmethod type->coercer spec_tools.core.Spec
  [x]
  ;; Or just as a record
  ;; {:reason :must-be-number :form clojure.core/number? :type :double}
  (-> x :form sc/sym->coercer))

(defmethod sc/sym->coercer :default
  [x]
  (type->coercer x))
