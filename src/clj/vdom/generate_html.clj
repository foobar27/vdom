(ns vdom.generate-html)

;;
;; Introspection
;;

(defn- literal?
  [o]
  (or (string? o) (number? o) (true? o) (false? o)))

(defn- listener-name? [s] (.startsWith s "on-"))
(defn- listener-name->event-name [s] (.substring s 3))

(defn- form
  [form]
  (if (and (coll? form)
           (symbol? (first form)))
    (first form)))

(defn- text-content?
  [data]
  (or (literal? data)
      (-> data meta :text)))

;;
;; Low-level code generation for creating/updating nodes.
;;

(defn- generate-set-attribute-literal
  [el k v]
  (if (listener-name? k)
    `(.addEventListener ~el ~(listener-name->event-name k) ~v)
    (condp = k
      "id" `(set! (.-id ~el) ~v)
      "class" `(set! (.-class ~el) ~v)
      `(.setAttribute ~el ~k ~v))))

(defmacro compile-set-attribute
  [el k v]
  (let [k (name k)]
    (if (literal? v)
      (if v
        (generate-set-attribute-literal el k v))
      (let [ve-sym (gensym "ve")]
        `(let [~ve-sym ~v]
           (if ~ve-sym
             ~(generate-set-attribute-literal el k ve-sym)))))))

(defn- generate-create-element
  [namespace-uri tag]
  (if namespace-uri
    `(.createElementNS js/document ~namespace-uri ~tag)
    `(.createElement js/document ~tag)))

(defn- generate-create-text-element
  [text]
  `(.createTextNode js/document ~text))

(defn- generate-append-child
  [el child]
  `(.appendChild ~el ~child))

(defmacro compile-set-text
  [el text]
  `(set! (.-textContent ~el) ~text))

;;
;; Handlers to update nodes.
;;

(defn- update-attribute [node-key key]
  (fn [context]
    ;; TODO reuse generate-set-attribute
    (let [node-sym (-> context :symbols :nodes)]
      (condp = key
        :id (fn [old new]
              `(set! (.-id (get ~node-sym ~node-key)) new))
        :class (fn [old-new]
                 `(set! (.-class (get ~node-sym ~node-key)) new))
        (fn [old new]
          (.setAttribute (get ~node-sym ~node-key)
                         key
                         new))))))

(defn- update-text [node-key]
  (fn [context]
    (fn [old new]
      `(set! (.-textContent (get ~(-> context :symbols :nodes) ~node-key)) new))))

