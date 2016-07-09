(ns vdom.core
  (:require ))

(enable-console-print!)

(println "This text is printed from src/vdom/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))


(defn- update-fn [nodes]
  (let [root-node (:root nodes)
        name-node (:name nodes)
        street-node (:street nodes)
        city-node (:city nodes)]
    (fn [old-data new-data]
      (let [old-id (get old-data :id)
            new-id (get new-data :id)]
        (when-not (and (identical? old-id new-id) ;; fast-path; really necessary?
                     (= old-id new-id))
          (set! (.-id root-node) new-id)))
      (let [old-name (get old-data :name)
            new-name (get new-data :name)]
        (when-not (and (identical? old-name new-name) ;; fast-path; really necessary?
                     (= old-name new-name))
          (set! (.-textContent name-node) new-name)))
      (let [old-address (get old-data :address)
            new-address (get new-data :address)]
        (when-not (identical? old-address new-address) ;; fast-path
          (let [old-street (get old-address :street)
                new-street (get new-address :street)]
            (when-not (and (identical? old-street new-street) ;; fast-path; really necessary?
                         (= old-street new-street))
              (set! (.-textContent street-node) new-street)))
          (let [old-city (get old-address :city)
                new-city (get new-address :city)]
            (when-not (and (identical? old-city new-city) ;; fast-path; really necessary?
                         (= old-city new-city))
              (set! (.-textContent city-node) new-city))))))))

;; [:div {:id id}
;;  [:span name]
;;  [:div {:class "address"}
;;   [:span street]
;;   [:span city]]]
(defn create-tree [data]
  (let [root (.createElement js/document "div")
        name (.createElement js/document "div")
        name-text (.createTextNode js/document (-> data :name))
        address (.createElement js/document "address")
        street (.createElement js/document "span")
        street-text (.createTextNode js/document (-> data :address :street))
        city (.createElement js/document "span")
        city-text (.createTextNode js/document (-> data :address :city))
        nodes {:root root
               :name name-text
               :street street-text
               :city city-text}]
    (set! (.-id root) (str (:id data)))
    (set! (.-class address) "address")
    (.appendChild root name)
    (.appendChild name name-text)
    (.appendChild root address)
    (.appendChild address street)
    (.appendChild street street-text)
    (.appendChild address city)
    (.appendChild city city-text)
    {:nodes nodes
     :update-fn (update-fn nodes)}))

;; ;; TODO generic set attribute
;; (defn- update-attribute [key node-key]
;;   (fn [context]
;;     (condp = key
;;       :id (fn [(:keys [nodes])]
;;             (fn [old new]
;;               `(set! (.-id (get nodes :root))))))))

;; (defn update-tree2 [nodes old-data new-data]
;;   (compile-diff [nodes]
;;                 [{:path [:id]
;;                   :data (fn [context]
;;                           (fn [old new]
;;                             (set! (.-id (get nodes :root)) ???)))}
;;                  {:path [:name]
;;                   :data (fn [old new]
;;                           )}]))

(let [nodes (create-tree {:id 42 :name "John Doe", :address {:street "Main St", :city "Paris"}})
      root (-> nodes :nodes :root)
      update-tree (-> nodes :update-fn)
      app-node (.getElementById js/document "app")]
  (.appendChild app-node root)
  
  (update-tree {:id 42 :name "John Doe", :address {:street "Main St", :city "Paris"}}
               {:id 43 :name "John Doe2", :address {:street "Main St2", :city "Berlin"}}))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
