(ns example.average-service.repl
  "Functions to operate and exercise the running service at the REPL."
  (:require [clojure.string :as str]
            [org.httpkit.client :as client]))


(defn process-response
  "Returns the status and body of a response, or client error."
  [{:keys [status body error]}]
  (or error
      {:status status
       :body   body}))


#_{:clj-kondo/ignore [:unresolved-var]}
(defn do-get-request
  "Make a GET request to the running system."
  ([path]
   (do-get-request path {}))
  ([path opts]
   (process-response @(client/get (str "http://localhost:8080" path) (assoc opts :as :text)))))


(defn get-ping
  "Request the running system for a ping health check."
  []
  (do-get-request "/ping"))


(defn get-average
  "Request the running system for averages of the given nums."
  [nums]
  (do-get-request "/average"
                  {:query-params {"nums" (str/join "," (map str nums))}}))


(defn unknown-request
  "Make an unknown request to the running system."
  []
  (do-get-request "/foo"))


(comment
  (get-ping)
  (get-average [1 4 8 5 3])
  (get-average [0 1 2 3]) ; 400 response
  (get-average [7 3 3]) ; 500 response
  (unknown-request) ; 404 response
  ;
)
