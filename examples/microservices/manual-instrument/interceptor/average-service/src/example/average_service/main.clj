(ns example.average-service.main
  "Main application state and entry point, used when service is run as a
   standalone Java application."
  (:require [example.average-service.system :as system]
            [example.common.system.main :as main])
  (:gen-class))


(defonce ^{:doc "Component map of the running system, exposed for inspection in the REPL."} system
  nil)

(defn -main
  "Main application entry point."
  [& _args]
  (main/main #'system system/with-system))
