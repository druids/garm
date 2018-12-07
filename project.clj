(defproject garm "0.0.0"
  :description "Garm is a library that decorates Clojure Specs for better understanding"
  :url "https://github.com/druids/garm"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}

  :dependencies [[metosin/spec-tools "0.8.2"]]

  :cloverage
  {:fail-threshold 90
   :ns-exclude-regex [#"garm.specs"]}

  :profiles {:dev {:plugins [[lein-ancient "0.6.15"]
                             [lein-cloverage "1.0.11"]
                             [lein-kibit "0.1.6"]
                             [jonase/eastwood "0.2.5"]]

                   :dependencies [[org.clojure/clojure "1.9.0"]]}})
