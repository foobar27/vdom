(ns vdom.core
  (:require ))

(enable-console-print!)

(println "This text is printed from src/vdom/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)


;; example data
(comment
 {:id 42
  :name "John"
  :address {:street "main ave"
            :city "new york"}}

 ;; query
 [:id :name {:address [:street :city]}]
 ;; definition of tree
 [:div {:id id}
  [:span name]
  [:div {:class "address"}
   [:span street]
   [:span city]]]
 ;; generated: create function
 (fn [data]
   (let [root (.createElement js/document "div")
         name (.createElement js/document "span")
         name-text (.createTextNode js/document name)
         address (.createElement js/document "address")
         street (.createElement js/document "street")
         street-text (.createTextNode js/document street)
         city (.createElement js/document "city")
         city-text (.createTextNode js/document city)]
     (set! (.-id root) (str id))
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
 ;; generated: update function
 (fn [nodes old-data new-data]
   (let [old-id (get old-data :id)
         new-id (get new-data :id)]
     (if-not (and (identical? old-id new-id) ;; fast-path; really necessary?
                  (= old-id new-id))
       (set! (.-id (get nodes :root)) new-id)))
   (let [old-name (get old-data :name)
         new-name (get new-data :name)]
     (if-not (and (identical? old-name new-name) ;; fast-path; really necessary?
                  (= old-name new-name))
       (set! (.-textContent (get nodes :id)) old-name)))
   (let [old-address (get old-data :address)
         new-address (get new-data :address)]
     (if-not (identical? old-address new-address) ;; fast-path
       (let [old-street (get old-address :street)
             new-street (get new-address :street)]
         (if-not (and (identical? old-street new-stree) ;; fast-path; really necessary?
                      (= old-street new-street))
           (set! (.-textContent (get nodes :id)) old-street)))
       (let [old-city (get old-address :city)
             new-city (get new-address :city)]
         (if-not (and (identical? old-city new-city) ;; fast-path; really necessary?
                      (= old-city new-city))
           (set! (.-textContent (get nodes :id)) old-city)))))))


(fn gen-trigger-scalar-fn [body-fn]
  (fn [old-value new-value]
    `(if-not (and (identical? old-value new-value)
                  (= old-value new-value))
       ~@(body-fn old-value new-value))))

(fn [nodes]
  #{[:id] [(fn [old-value new-value]
             `(set! (.-id ~(get nodes root)) (str new-value)))]
    [:name] []
    [:address :street] []
    [:address :value] []})


