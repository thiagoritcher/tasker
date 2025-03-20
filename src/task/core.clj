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

(defn find-task [args]
  (let [strfiltr (first args) files (file-seq (file "."))]
      (filter (fn[file] (s/starts-with? (.getName file) strfiltr )) files)))

(defn init-config [_]
  (write-config {:tasks {:number 1 :store-location ".tasks"}})
  "Initializing .tasks dir" 
  )

(defn read-config []
  (edn/read-string (slurp file-name)))


(defn new-task [args]
  (let [desc (first args) 
        body (second args) 
        config (read-config)
        current (format "%03d" (get-in config [:tasks :number]))
        store-location  (get-in config [:tasks :store-location])
        fname (str current "-" desc)]

    (spit (str store-location "/" fname ".md") 
          (str "# " fname "\n\n" body "\n"))

    (write-config (update-in config [:tasks :number] inc))
    (str "Task \"" fname "\" created")))

(defn set-current [args]
  (let [t (first (find-task args))]
    (copy t (file ".task/current"))
    (str "Setting current task to '"(.getName t)"'")))

(defn get-help [args]
  (let [strfm "%10s - %s\n"]
    (str 
      "Simple task manager using files\n"
      (format strfm "init" "Creates config directory")
      (format strfm "new" "<task desc> [task body] Creates new task")
      (format strfm "current" "<task number> Set current task"))))

(defn parse-args [option args]
  (cond 
    (= "init" option) (init-config args)
    (= "current" option) (set-current args)
    (= "new" option) (new-task args)
    :else (get-help args)))

(defn -main[& args ]
    (print (str (parse-args (first args) (rest args)) "\n")))

