(ns example.rpg-service.main
  (:require
   [example.common.system :as common-system]
   [example.rpg-service.system :as system]))

(defonce ^{:doc "Map of components in the running system."} system
  nil)

(defn -main
  "start me up!"
  [& _args]
  (common-system/start! #'system system/with-system))
