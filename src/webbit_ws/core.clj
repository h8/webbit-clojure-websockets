(ns webbit-ws.core
    (:use clojure.walk)
    (:use webbit-ws.say)
    (:require [clojure.data.json :as json]
              [clojure.string :as s])
    (:import [org.webbitserver WebServer WebServers WebSocketHandler]
             [org.webbitserver.handler StaticFileHandler]))

(def webserver (atom nil))
(def chanels (atom '()))

(defn on-message [chanel json-message]
    (say)
    (let [message (-> json-message json/read-str (keywordize-keys) (get-in [:data :message]))]
        (doseq [c @chanels]
            (.send c (json/write-str
                    {:type "upcased" :message (s/upper-case message) })))))

(defn stop-server []
  (.stop @webserver)
  (reset! webserver nil))

(defn -main []
    (reset! webserver 
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
            (.start))))
