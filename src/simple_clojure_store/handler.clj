(ns simple-clojure-store.handler
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.core :refer [GET defroutes]]
            [ring.util.response :refer [resource-response response]]
            [ring.middleware.json :as middleware]))

(def store (atom {}))

(defn average
  [nums]
  (if (seq nums)
    (/ (reduce + nums) (count nums))
    nil))

(defn average-for-key
  [kv-map k]
  (average (kv-map k)))

(defn average-of-averages
  [kv-map]
  (average (map average (vals kv-map))))

(defn update-kv-map
  [kv-map k n]
  (if (k kv-map)
    (update-in kv-map [k] conj n)
    (assoc kv-map k [n])))

(defn update-store
  [k n]
  (swap! store update-kv-map k n)
  (@store k))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))
