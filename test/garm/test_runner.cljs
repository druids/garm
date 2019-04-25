(ns garm.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [garm.core-test]
            [garm.spec-tools-test]))

(enable-console-print!)

(doo-tests 'garm.core-test
           'garm.spec-tools-test)
