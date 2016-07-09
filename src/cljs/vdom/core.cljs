(ns vdom.core
  (:require ))

(enable-console-print!)

(println "This text is printed from src/vdom/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))


;; [:div {:id id}
;;  [:span name]
;;  [:div {:class "address"}
;;   [:span street]
;;   [:span city]]]
;; generated: create function
(defn create-tree [data]
  (let [root (.createElement js/document "div")
        name (.createElement js/document "div")
        name-text (.createTextNode js/document (-> data :name))
        address (.createElement js/document "address")
        street (.createElement js/document "span")
        street-text (.createTextNode js/document (-> data :address :street))
        city (.createElement js/document "span")
        city-text (.createTextNode js/document (-> data :address :city))]
    (set! (.-id root) (str (:id data)))
    (set! (.-class address) "address")
    (.appendChild root name)
    (.appendChild name name-text)
    (.appendChild root address)
    (.appendChild address street)
    (.appendChild street street-text)
    (.appendChild address city)
    (.appendChild city city-text)
    {:root root
     :name name-text
     :street street-text
     :city city-text}))

(defn update-tree [nodes old-data new-data]
  (let [old-id (get old-data :id)
        new-id (get new-data :id)]
    (if-not (and (identical? old-id new-id) ;; fast-path; really necessary?
                 (= old-id new-id))
      (set! (.-id (get nodes :root)) new-id)))
  (let [old-name (get old-data :name)
        new-name (get new-data :name)]
    (if-not (and (identical? old-name new-name) ;; fast-path; really necessary?
                 (= old-name new-name))
      (set! (.-textContent (get nodes :name)) new-name)))
  (let [old-address (get old-data :address)
        new-address (get new-data :address)]
    (if-not (identical? old-address new-address) ;; fast-path
      (let [old-street (get old-address :street)
            new-street (get new-address :street)]
        (if-not (and (identical? old-street new-street) ;; fast-path; really necessary?
                     (= old-street new-street))
          (set! (.-textContent (get nodes :street)) new-street)))
      (let [old-city (get old-address :city)
            new-city (get new-address :city)]
        (if-not (and (identical? old-city new-city) ;; fast-path; really necessary?
                     (= old-city new-city))
          (set! (.-textContent (get nodes :city)) new-city))))))

(let [nodes (create-tree {:id 42 :name "John Doe", :address {:street "Main St", :city "Paris"}})
      root (:root nodes)
      app-node (.getElementById js/document "app")]
  (.log js/console "app-node" app-node)
  ;;(set! (.-class app-node) "???")
  (.log js/console "root" root)
  (.appendChild app-node root)
  
  (update-tree nodes
               {:id 42 :name "John Doe", :address {:street "Main St", :city "Paris"}}
               {:id 43 :name "John Doe2", :address {:street "Main St2", :city "Berlin"}})
  )

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
