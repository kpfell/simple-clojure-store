(ns simple-clojure-store.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.core :refer [GET defroutes]]
            [ring.util.response :refer [resource-response response]]
            [ring.middleware.json :as middleware]))

(def store (atom {}))

(defn average
  "Takes a collection of numbers.
   Returns the average of those numbers"
  [nums]
  (if (seq nums)
    (/ (reduce + nums) (count nums))
    nil))

(defn average-for-key
  "Takes a map of keys to collections of numbers, along with a key.
   Returns the average of the collection for the given key."
  [kv-map k]
  (average (kv-map k)))

(defn average-of-averages
  "Takes a map of keys to collections of numbers.
   Calculates and returns the average of the averages of all the collections."
  [kv-map]
  (average (map average (vals kv-map))))

(defn update-kv-map
  "Takes a map of keys to collections of numbers, a key, and numbers.
   If the key exists in the map, adds the key to the associated collection.
   If the key does not exist in the map, creates a new collection containing the number, associated with the given key."
  [kv-map k n]
  (if (k kv-map)
    (update-in kv-map [k] conj n)
    (assoc kv-map k [n])))

(defn update-store
  "Takes a key and a number.
   Updates the atom store with the given key and number.
   Returns the new collection associated with the given key."
  [k n]
  (k (swap! store update-kv-map k n)))

(defn get-average-of-averages
  "Returns a JSON response containing the average of averages for the store."
  []
  (response {:average (average-of-averages @store)}))

(defn get-average-for-key
  "Takes a key.
   Returns a JSON response containing the average the numbers in the collection associated with the key in store."
  [k]
  (response {:average (average-for-key @store k)}))

(defn put-in-store
  "Takes the JSON response POSTed to '/'.
   Parses the key and number from the JSON.
   Adds the given key and number to the store.
   Returns a JSON response of the new collection associated with the given key."
  [body]
  (let [kvp (first body)
        k (key kvp)
        n (val kvp)]
    (response (update-store k n))))

(defroutes app-routes
  (GET "/average" [] (get-average-of-averages))
  (GET "/average/:k" [k] (get-average-for-key (keyword k)))
  (POST "/" {body :body} (put-in-store body))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body {:keywords? true})
      (middleware/wrap-json-response)))
