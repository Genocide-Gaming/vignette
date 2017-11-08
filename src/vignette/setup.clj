(ns vignette.setup
  (:require [cheshire.core :refer :all]
            [vignette.http.legacy.routes :as hlr]
            [vignette.http.api-routes :refer [def-api-context wiki-context uuid-context]]
            [vignette.util.integration :as i]
            [vignette.storage.s3 :refer [create-s3-storage-system storage-creds]]
            [vignette.storage.core :refer [create-image-storage]]
            [vignette.storage.local :refer [create-local-storage-system]]
            [vignette.util.services :refer [materialize-static-asset-url]]
            [vignette.storage.static-assets :refer [create-static-image-storage]]))


(defn- create-object-storage [opts]
  (if (= (:mode opts) "local")
    (do
      (i/create-integration-env)
      (create-local-storage-system i/integration-path))
    (create-s3-storage-system storage-creds)))

(defn create-stores [opts]
  {
    :wikia-store  (create-image-storage (create-object-storage opts) (:cache-thumbnails opts))
    :static-store (create-image-storage (create-static-image-storage materialize-static-asset-url) (:cache-thumbnails opts))
   })

(defn image-routes [stores]
  (concat
    (list
      (def-api-context wiki-context (:wikia-store stores))
      (def-api-context uuid-context (:static-store stores)))
    (hlr/legacy-routes (:wikia-store stores))))
