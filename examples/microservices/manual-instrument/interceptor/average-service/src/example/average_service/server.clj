(ns example.average-service.server
  "HTTP server components."
  (:require [example.average-service.bound-async.routes :as bound-async-routes]
            [example.average-service.explicit-async.routes :as explicit-async-routes]
            [example.average-service.sync.routes :as sync-routes]
            [example.common.interceptor.utils :as interceptor-utils]
            [io.pedestal.http :as http]
            [io.pedestal.interceptor :as interceptor]
            [steffan-westcott.clj-otel.api.metrics.http.server :as metrics-http-server]
            [steffan-westcott.clj-otel.api.trace.http :as trace-http]))


(defn- sync?
  [config]
  (case (:server-impl config)
    "sync"           true
    "bound-async"    false
    "explicit-async" false))



(defn- routes
  "Route data for all routes, according to configured server implementation."
  [config]
  (case (:server-impl config)
    "sync"           (sync-routes/routes)
    "bound-async"    (bound-async-routes/routes)
    "explicit-async" (explicit-async-routes/routes)))



(defn- update-interceptors
  "Returns modified default interceptors."
  [default-interceptors
   {:keys [config]
    :as   components}]
  (map interceptor/interceptor
       (concat (;; As this application is not run with the OpenTelemetry instrumentation
                ;; agent, create a server span for each request. The current context is
                ;; set if all request handling is processed synchronously.
                trace-http/server-span-interceptors {:create-span?         true
                                                     :set-current-context? (sync? config)})

               ;; Add metric that records the number of active HTTP requests
               [(metrics-http-server/active-requests-interceptor)]

               ;; Default Pedestal interceptor stack
               default-interceptors

               ;; Adds matched route data to server spans
               [(trace-http/route-interceptor)]

               ;; Adds metrics that include http.route attribute
               (metrics-http-server/metrics-by-route-interceptors)

               ;; Convert exception to HTTP response
               [(interceptor-utils/exception-response-interceptor)]

               ;; Add exception event to server span
               [(trace-http/exception-event-interceptor)]

               ;; Add system components to context
               [(interceptor-utils/components-interceptor components)])))



(defn service-map
  "Returns a service map ready for creating an HTTP server."
  [{:keys [config]
    :as   components}]
  (-> {::http/routes #(routes config) ; rebuild routes on every request
       ::http/type   :jetty
       ::http/host   "0.0.0.0"
       ::http/join?  false}
      (merge (:service-map config))
      (http/default-interceptors)
      (update ::http/interceptors update-interceptors components)))



(defn server
  "Starts the server."
  [service-map]
  (http/start (http/create-server service-map)))



(defn stop-server
  "Stops the server."
  [initialized-service-map]
  (http/stop initialized-service-map))
