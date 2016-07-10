(ns vdom.diff
  (:require [vdom.paths :refer [paths->radix-tree collapse-tree]]))

(defn- generate-cascade
  "Compiles code which dispatches the changes as indicates by the tree which
  summarizes the paths."
  [{:keys [payloads children]} old new]
  ;; We compare by identity and for scalars additional by value.
  `(when-not ~(if (seq payloads)
                `(identical? ~old ~new)
                `(or (identical? ~old ~new)
                     (= ~old ~new)))
     ;; Recursively generate code for all children.
       ~@(for [[k n] children]
           `(let [~old (-> ~old ~@k)
                  ~new (-> ~new ~@k)]
              ~(generate-cascade n old new)))
       ;; Generate code for the payloads of the current node.
       ~@(for [payload payloads]
           ((:data payload) old new))))

;; (defmacro compile-diff [paths]
;;   "Compiles paths to code which executes actions on diffs"
;;   (if (seq paths)
;;     (let [old (gensym "old")
;;           new (gensym "new")]
;;       `(fn [~old ~new]
;;          ~(generate-cascade (-> paths paths->radix-tree collapse-tree) old new)))
;;     `(fn [old new]
;;        nil)))

;;(println (macroexpand-1 '(compile-diff [{:path [:foo :bar] :data `(println "foo bar")}])))

;; (compile-diff [{:path [:foo :bar] :data `(println "foo bar")
;;                 {:path [:foo :baz] :data `(println "foo baz")}])
