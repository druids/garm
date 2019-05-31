(defproject garm "0.7.2"
  :description "Garm is a library that decorates Clojure Specs for better understanding"
  :url "https://github.com/druids/garm"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}

  :dependencies [[metosin/spec-tools "0.9.2"]
                 [spec-coerce "1.0.0-alpha9"]]

  :cloverage
  {:fail-threshold 90
   :ns-exclude-regex [#"garm.specs"]}

  :aliases {"cljs-test"
            ["with-profile" "dev" "doo" "phantom" "once"]}

  :doo {:build "test"}

  :cljsbuild {:builds
              [{:id "test"
                :source-paths ["src" "test"]
                :compiler {:output-to "target/js/compiled/testable.js"
                           :main garm.test-runner
                           :optimizations :none}}]}

  :profiles {:dev {:plugins [[lein-ancient "0.6.15"]
                             [lein-cljsbuild "1.1.7"]
                             [lein-cloverage "1.1.1"]
                             [lein-doo "0.1.11"]
                             [lein-kibit "0.1.6"]
                             [jonase/eastwood "0.3.5"]]

                   :dependencies [[org.clojure/clojure "1.10.0"]
                                  [org.clojure/clojurescript "1.10.520"]]}})
