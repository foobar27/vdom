(ns vdom.core
  (:require-macros [vdom.generate-html :refer [compile-set-attribute compile-set-text]]))

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
          (compile-set-attribute root-node "id" new-id)))
      (let [old-name (get old-data :name)
            new-name (get new-data :name)]
        (when-not (and (identical? old-name new-name) ;; fast-path; really necessary?
                       (= old-name new-name))
          (compile-set-text name-node new-name)
          (set! (.-textContent name-node) new-name)))
      (let [old-address (get old-data :address)
            new-address (get new-data :address)]
        (when-not (identical? old-address new-address) ;; fast-path
          (let [old-street (get old-address :street)
                new-street (get new-address :street)]
            (when-not (and (identical? old-street new-street) ;; fast-path; really necessary?
                           (= old-street new-street))
              (compile-set-text street-node new-street)))
          (let [old-city (get old-address :city)
                new-city (get new-address :city)]
            (when-not (and (identical? old-city new-city) ;; fast-path; really necessary?
                           (= old-city new-city))
              (compile-set-text city-node new-city))))))))

;; (defn update-tree2 [nodes old-data new-data]
;;   (compile-diff [nodes]
;;                 [{:path [:id]
;;                   :data (update-attribute :root :id)}
;;                  {:path [:name]
;;                   :data (update-text :name)}
;;                  {:path [:address :street]
;;                   :data (update-text :street)}
;;                  {:path [:address :city]
;;                   :data (update-text :city)}]))


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
    (compile-set-attribute root "id" (:id data))
    (compile-set-attribute root "class" "address")
    (.appendChild root name)
    (.appendChild name name-text)
    (.appendChild root address)
    (.appendChild address street)
    (.appendChild street street-text)
    (.appendChild address city)
    (.appendChild city city-text)
    {:nodes nodes
     :update-fn (update-fn nodes)}))

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
