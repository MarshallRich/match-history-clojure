(ns win-loss-clojure.core
   (:require [compojure.core :as c]
             [ring.adapter.jetty :as j]
             [ring.middleware.params :as p]
             [ring.util.response :as r]
             [hiccup.core :as h])
  (:gen-class))

(defonce matches (atom []))
(defonce server (atom nil))

(add-watch matches :save-to-disk
  (fn [_ _ _ _]
    (spit "matches.edn" (pr-str @matches))))

(c/defroutes app
  (c/GET "/" request
    (h/html [:html
              [:body
               [:form {:action "/add-match" :method "post"}
                [:input {:type "text" :placeholder "enter the challenger's name" :name "versus"}]
                [:select {:name "win-lose?"}
                 [:option {:value "[win]"} "win"]
                 [:option {:value "[lose]"} "lose"]
                 [:option {:value "[tie]"} "tie"]]
                [:button {:type "submit"} "add match"]]
               [:ol
                (map (fn [match]
                       [:li match])
                  @matches)]]]))

  (c/POST "/add-match" request
      (let [match (str
                    (get (:params request) "versus")
                    " - "
                    (get (:params request) "win-lose?"))]
        (swap! matches conj match)
        (r/redirect "/"))))

(defonce server (atom nil))

(defn -main []
  (try
    (let [matches-str (slurp "matches.edn")
          matches-vec (read-string matches-str)]
      (reset! matches matches-vec))
    (catch Exception _))
  (when @server
    (.stop @server))
  (reset! server (j/run-jetty (p/wrap-params app) {:port 3000 :join? false})))
