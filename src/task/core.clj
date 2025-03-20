(ns task.core
  (:require 
    [clojure.java.io :refer (make-parents copy file)]
    [clojure.pprint :refer (pprint)]
    [clojure.string :as s]
    [clojure.edn :as edn]
    
    ))

(def file-name ".task/config")

(defn write-config [configs]
  (let [contents (pr-str configs)]
    (make-parents file-name)
    (spit file-name contents)))

(defn read-config []
  (edn/read-string (slurp file-name)))

(defn mkdir [dir]
    (make-parents file-name)
    (.mkdir (file dir)))


(defn task-name [file]
  (let [fname (.getName file)
        idx  (s/index-of fname "_")]
      (if (nil? idx) fname
      (.substring fname (inc idx)))))

(defn task-file [file]
  (.getPath file))

(defn task-contents [file]
  (slurp file))

(defn find-task [args]
  (let [strfiltr (first args) 
        config (read-config)
        store-location  (get-in config [:tasks :store-location])
        files (rest (file-seq (file store-location)))]
      (filter (fn[file] 
                (if (s/blank? strfiltr) true
                  (s/includes? (task-name file) strfiltr))) files)))

(defn task-delete [args]
  (let [tasks (find-task args)]
    (cond (empty? tasks) "No tasks found"
     (not (nil? (second tasks))) (str "Multiple tasks found\n" (s/join "\n" (map task-name tasks)))
    :else 
    (let 
      [task (first tasks) 
       tname (task-name task)] 
      (.delete task) 
      (str "Task '" tname "' deleted")))))

(defn task-show [args]
  (let [tasks (find-task args) div "\n----------------------------------------\n"]
    (if (empty? tasks) "No tasks found"
      (s/join div (map task-contents tasks)))))

(defn task-files [args]
  (let [tasks (find-task args) div "\n----------------------------------------\n"]
    (if (empty? tasks) "No tasks found"
      (s/join div (map #(str "'" (task-file %) "'")  tasks)))))

(defn list-all [args]
  (let [tasks (find-task args)]
  (if (empty? tasks) "No tasks found"
    (s/join "\n" (map task-name tasks)))))

(defn init-config [_]
  (let [store-location ".task/tasks/" 
        done-location ".task/done/"
        config {:tasks {:id 0 :store-location store-location :done-location done-location :id-gen :uuid}}]

  (write-config config)
  (mkdir store-location)
  (mkdir done-location)

  "Initializing .tasks "))

(defn get-id-fn [id-gen]
  (cond
    (= :inc id-gen) #(format "%03d" (inc %))
    (= :uuid id-gen) (fn [_] (random-uuid))))

(defn new-task [args]
  (let [desc (first args) 
        body (second args) 
        config (read-config)
        current (get-in config [:tasks :id])
        store-location  (get-in config [:tasks :store-location])
        id-fn (get-id-fn (get-in config [:tasks :id-gen]))
        id (id-fn current)               
        fname (str id "_" desc)]

    (write-config (update-in config [:tasks :id] (fn[_] id)))
    
    (spit (str store-location "/" fname) 
          (str "# " desc "\n\n" body "\n"))


    (str "Task \"" desc "\" created")))

(defn set-current [args]
  (let [t (first (find-task args))]
    (copy t (file ".task/current"))
    (str "Setting current task to '"(task-name t)"'")))

(defn get-help [args]
  (let [strfm "%10s - %s\n"]
    (str 
      "Simple task manager using files\n"
      (format strfm "init" "Creates config directory")
      (format strfm "new" "<task desc> [task body] Creates new task")
      (format strfm "list" "[task filter] List tasks")
      (format strfm "show" "[task filter] Show tasks")
      (format strfm "file" "[task filter] Show task files")
      (format strfm "delete" "[task filter] Delete tasks")
      (format strfm "current" "<task filter> "))))

(defn parse-args [option args]
  (cond 
    (= "init" option) (init-config args)
    (= "current" option) (set-current args)
    (= "new" option) (new-task args)
    (= "delete" option) (task-delete args)
    (= "show" option) (task-show args)
    (= "file" option) (task-files args)
    (= "list" option) (list-all args)
    :else (get-help args)))

(defn -main[& args ]
    (print (str (parse-args (first args) (rest args)) "\n")))

