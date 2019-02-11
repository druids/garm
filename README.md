Garm
====

Garm is a library that decorates Clojure Specs for better understanding.

[![CircleCI](https://circleci.com/gh/druids/garm.svg?style=svg)](https://circleci.com/gh/druids/garm)
[![Dependencies Status](https://jarkeeper.com/druids/garm/status.png)](https://jarkeeper.com/druids/garm)
[![License](https://img.shields.io/badge/MIT-Clause-blue.svg)](https://opensource.org/licenses/MIT)


Leiningen/Boot
--------------

```clojure
[garm "0.6.1"]
```


Documentation
-------------

### validate

```clojure
(require '[clojure.spec.alpha :as s]
         '[garm.core :as garm]
         '[garm.specs :as specs]))

;; let's define specs
(s/def ::id specs/uuid)
(s/def ::price (s/and specs/decimal specs/pos))
(s/def ::status (specs/enum #{:dispatched :received}))

(s/def ::order (s/keys :req-un [::id ::price ::status]))

;; `validate` function iterates over explained-data and returns a result as
;; a tuple of `[data errors]`
;; if data are valid, the `errors` will be `nil` and vice versa
(garm/validate ::order {:price -1, :status :foo})

;; the result
[nil {:id [{:args []
            :id ::specs/must-be-uuid
            :message "Must be a UUID"}]
      :price [{:args []
               :id ::specs/must-be-decimal
               :message "Must be a decimal"}]
      :status [{:args []
                :id :garm.core/missing-key
                :message "This field is required"}]}]

;; with valid data
(garm/validate ::order {:status :dispatched
                        :price 1000
                        :id #uuid "0428a077-96fd-4050-870e-ed1449266142"})
[{:status :dispatched
  :price 1000
  :id #uuid "0428a077-96fd-4050-870e-ed1449266142"}
 nil]
```

Note:

When an error is in a nested object a vector is used for path instead of keyword.
E.g.:
```clojure
{[:object-wo-reason :missing-reason] ;; <-- a key path
 [{:args []
   :id :garm.core/missing-key
   :message "This field is required"}]}]

```

### ->json-api-response

It converts a result tuple of `validate` function to JSON API errors format.
It returns `nil` when the result of `validate` function is successful.

```clojure
(-> ::order
    (garm/validate {:status :foo
                    :price 1000
                    :id #uuid "0428a077-96fd-4050-870e-ed1449266142"}
    (garm/->json-api-response))

{:errors [{:id :status
           :meta {:data [{:id :garm.specs/must-be-one-of
                          :message "Must be one of %s"
                          :args [#{:dispatched :received}]
                          :title "Must be one of received, dispatched"}]}}]}
;; see formatted message in `:title`
```

Contribution
------------

### Conventions

* Please follow coding style defined by [`.editorconfig`](http://editorconfig.org)
 and [The Clojure Style Guide](https://github.com/bbatsov/clojure-style-guide)
* Write [good commit messages](https://chris.beams.io/posts/git-commit/)
 and provide an issue ID in a commit message prefixed by `#`
