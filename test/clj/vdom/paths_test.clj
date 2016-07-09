(ns vdom.paths-test
  (:require [vdom.paths :refer :all]
            [vdom.test-utils :refer [refer-private]]
            [clojure.test :refer :all]))

(refer-private 'vdom.paths)

(deftest test-group-paths
  (is (= (group-paths [{:path [:foo :bar]}
                         {:path [:foo :baz]}])
         {:foo [{:path [:bar]}
                {:path [:baz]}]})))

(deftest test-paths->radix-tree
  (are [input output]
    (= (paths->radix-tree input) output)

    ;; empty
    []
    (node [])

    ;; one empty element
    [{:path [] :data "a"}]
    (node [{:path [] :data "a"}])
        
    ;; one real element
    [{:path [:a] :data "a"}]
    (node nil {:a (node [{:path [] :data "a"}])})

    ;; duplicate element
    [{:path [:a] :data "a1"}
     {:path [:a] :data "a2"}]
    (node nil {:a (node [{:path [] :data "a1"} {:path [] :data "a2"}])})
    
    ;; complex situation
    [{:path [:a :b :c] :data "abc"}
     {:path [:a :b :d] :data "abd"}
     {:path [:a :b :d :e] :data "abde"}
     {:path [:x :y :z] :data "xyz"}
     {:path [:1 :2 :3] :data "123"}
     {:path [:1 :2 :4] :data "124"}]
    (node nil
          {:a (node nil
                    {:b (node nil
                              {:c (node [{:path [] :data "abc"}])
                               :d (node [{:path [] :data "abd"}]
                                        {:e (node [{:path [] :data "abde"}])})})})
           :x (node nil
                    {:y (node nil
                              {:z (node [{:path [] :data "xyz"}])})})
           :1 (node nil
                    {:2 (node nil
                              {:3 (node [{:path [] :data "123"}])
                               :4 (node [{:path [] :data "124"}])})})})))

(deftest test-collapse-groups
  (are [input output]
    (= (-> input paths->radix-tree collapse-tree)
       output)

    [{:path [:foo0 :foo1 :foo2 :foo3a] :data "3a"}
     {:path [:foo0 :foo1 :foo2 :foo3b] :data "3b"}]
    (node nil
          {[:foo0 :foo1 :foo2] (node nil
                                     {[:foo3a] (node [{:path [] :data "3a"}])
                                      [:foo3b] (node [{:path [] :data "3b"}])})})

    [{:path [:foo0a :foo1 :foo2 :foo3 :foo4a] :data "4a"}
     {:path [:foo0a :foo1 :foo2 :foo3 :foo4b] :data "4b"}
     {:path [:foo0b :foo1 :foo2 :foo3 :foo4c] :data "4c"}
     {:path [:foo0b :foo1 :foo2 :foo3 :foo4c :foo5] :data "5"}
     {:path [:foo0c] :data "0c"}]
    (node nil
          {[:foo0a :foo1 :foo2 :foo3] (node nil
                                            {[:foo4a] (node [{:path [] :data "4a"}])
                                             [:foo4b] (node [{:path [] :data "4b"}])})
           [:foo0b :foo1 :foo2 :foo3 :foo4c] (node [{:path [] :data "4c"}]
                                                   {[:foo5] (node [{:path [] :data "5"}])})
           [:foo0c] (node [{:path [] :data "0c"}])})

    ))


;; (deftest test-compile-cascade
;;   (let [state (atom [])
;;         state-sym (gensym "state")
;;         old-sym (gensym "old")
;;         new-sym (gensym "new")
;;         on-change (fn [context]
;;                     (fn [old-sym new-sym]
;;                       `(swap! ~state-sym conj [~context ~old-sym ~new-sym])))
;;         paths [{:path [:foo :bar]    :on-change-code (on-change :bar)}
;;                {:path [:foo :baz]    :on-change-code (on-change :baz)}
;;                {:path [:foo :bau]    :on-change-code (on-change :bau)}
;;                {:path [:foo :bau :1] :on-change-code (on-change :bau1)}
;;                {:path [:foo :bau :2] :on-change-code (on-change :bau2)}]
;;         cascade (-> paths
;;                     paths->radix-tree
;;                     collapse-tree
;;                     (compile-cascade old-sym new-sym))
;;         ;;_ (prn cascade)
;;         execute! (eval `(fn [~state-sym ~old-sym ~new-sym]
;;                           ~cascade))
;;         evaluate! (fn [old new]
;;                     (reset! state [])
;;                     (execute! state old new)
;;                     @state)]
;;     (are [old new output]
;;       (= (evaluate! old new) output)

;;       ;; empty
;;       nil
;;       nil
;;       []

;;       ;; single values
;;       {:foo {:bar 1}}
;;       {:foo {:bar 10}}
;;       [[:bar 1 10]]

;;       {:foo {:baz 1}}
;;       {:foo {:baz 10}}
;;       [[:baz 1 10]]

;;       {:foo {:bau {:1 1}}}
;;       {:foo {:bau {:1 10}}}
;;       [[:bau1 1 10]
;;        [:bau {:1 1} {:1 10}]]

;;       {:foo {:bau {:2 1}}}
;;       {:foo {:bau {:2 10}}}
;;       [[:bau2 1 10]
;;        [:bau {:2 1} {:2 10}]]

;;       ;; all together
;;       {:foo {:bar 1
;;              :baz 2
;;              :bau {:1 3
;;                    :2 4}}}
;;       {:foo {:bar 11
;;              :baz 12
;;              :bau {:1 13
;;                    :2 14}}}
;;       [[:bar 1 11]
;;        [:baz 2 12]
;;        [:bau1 3 13]
;;        [:bau2 4 14]
;;        [:bau {:1 3, :2 4} {:1 13, :2 14}]]
;;       ) {}))
