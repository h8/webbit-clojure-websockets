(ns webbit-ws.core
    (:use clojure.walk)
    (:require [clojure.data.json :as json]
              [clojure.string :as s])
    (:import [org.webbitserver WebServer WebServers WebSocketHandler]
             [org.webbitserver.handler StaticFileHandler]))

(def chanels (atom '()))

(defn on-message [chanel json-message]
    (let [message (-> json-message json/read-str (keywordize-keys) (get-in [:data :message]))]
        (doseq [c @chanels]
            (.send c (json/write-str
                    {:type "upcased" :message (s/upper-case message) })))))

(defn -main []
    (doto (WebServers/createWebServer 8080)
        (.add "/websocket"
            (proxy [WebSocketHandler] []
                (onOpen [c] (do 
                        (prn "opened" c)
                        (swap! chanels conj c)))
                (onClose [c] (do 
                        (prn "closed" c)
                        (swap! chanels (fn [v] (remove #(= %1 c) v)))))
                (onMessage [c j] (on-message c j))))
        
        (.add (StaticFileHandler. "./public/"))
        (.start)))
