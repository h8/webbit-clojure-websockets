(ns webbit-ws.autoreload
    {:author "Naitik Shah"
     :doc "Original example can be found at http://code.daaku.org/auto-reload.clj/"}
    (:use
      [ns-tracker.core :only [ns-tracker]])
    (:import
      [java.nio.file FileSystems StandardWatchEventKinds]))
	
(defn- auto-reload* [dirs]
    (let [modified-namespaces (ns-tracker dirs)
          fs (FileSystems/getDefault)
          watcher (.newWatchService fs)
          events (into-array [StandardWatchEventKinds/ENTRY_MODIFY])
          strs (into-array String [])]
      (doseq [dir dirs]
        (.register (.getPath fs dir strs) watcher events))
      (while true
        (let [key (.take watcher)
              events (.pollEvents key)]
          (if (not-every? #(= (.kind %) StandardWatchEventKinds/OVERFLOW) events)
            (doseq [ns-sym (modified-namespaces)]
              (require ns-sym :reload)))
          (.reset key)))))

(defn auto-reload
    [dirs]
    (.start (Thread. (partial auto-reload* dirs))))