(ns vdom.diff-test
  (:require [vdom.diff :refer :all]
            [vdom.paths :refer [collapse-tree paths->radix-tree]]
            [vdom.test-utils :refer [refer-private]]
            [clojure.test :refer :all]))

(refer-private 'vdom.diff)

(defn- compile-and-execute [context context-sym paths old new]
  (let [paths (-> paths paths->radix-tree collapse-tree)
        old-sym (gensym "old")
        new-sym (gensym "new")
        cascade (generate-cascade paths old-sym new-sym)
        execute! (eval `(fn [~context-sym ~old-sym ~new-sym]
                          ~cascade))]
    (execute! context old new)))

(deftest test-compile-diff
  (let [state (atom [])
        state-sym (gensym "state")
        on-change (fn [context]
                    (fn [old new]
                      `(swap! ~state-sym conj [~context ~old ~new])))
        paths [{:path [:foo :bar] :data (on-change "foo bar 1")}
               {:path [:foo :bar] :data (on-change "foo bar 2")}
               {:path [:foo :baz] :data (on-change "foo baz")}]]
    (compile-and-execute state
                         state-sym
                         paths
                         {:foo {:bar 1}}
                         {:foo {:bar 2}})
    (is (= @state
           [["foo bar 1" 1 2]
            ["foo bar 2" 1 2]]))))












































