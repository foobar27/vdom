(ns vdom.paths)

(defn- node
  "Create a node, given a vector as payload and optionally a map of children."
  ([payloads]
   (node payloads {}))
  
  ([payloads children]
   (assert (not (map? payloads)))
   (assert (map? children))
   {:payloads (vec payloads)
    :children (into {} children)}))

(defn- group-paths [paths]
  "Groups paths by the first element of their path. Returns a map where the
  keys are the identifiers of the groups and the values are the paths (where
  the first element of the path has been removed)."
  (into {}
        (for [[k cs] (group-by (fn [c] (-> c :path first))
                               paths)]
          [k (for [c cs]
               (assoc c :path (-> c :path rest)))])))

;; TODO move to some utility package
(defn- filter-and-remove [pred xs]
  [(filter pred xs) (remove pred xs)])

(defn paths->radix-tree
  "Creates a radix tree for a list of paths, where the nodes are the elements
  of the path, and the payloads are the paths themselves (with an empty path,
  as they are considered relative to the nodes)."
  [paths]
  (let [[children payloads] (filter-and-remove #(-> % :path seq) paths)]
    (node payloads
          (into {}
                (for [[k cs] (group-paths children)]
                  [k (paths->radix-tree cs)])))))

(defn collapse-tree
  "Creates a tree as returned by paths->radix-tree and collapses straight lines. The
  keys are now vectors of the keys from paths->radix-tree."
  [tree]
  (node (:payloads tree)
        (into {}
              (for [[k n] (:children tree)
                    :let [{:keys [payloads children] :as sub-tree} (collapse-tree n)]]
                (if (and (= (count children) 1)
                         (= (count payloads) 0))
                  ;; flatten: combine this node with the child
                  (let [[kk nn] (first children)]
                    [(into [k] kk) nn])
                  ;; standard case: no way to combine
                  [[k] sub-tree])))))
