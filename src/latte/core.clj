(ns latte.core
  (:require [latte.presyntax :as stx])
  (:require [latte.typing :as ty])
  (:require [latte.norm :as n])
  )

(defn latte-definition? [v]
  (and (map? v)
       (contains? v :tag)
       (contains? #{::term ::theorem ::axiom} (:tag v))))

(defn build-initial-definition-environment!
  []
  (let [ns-env (ns-map *ns*)]
    ;; (if (contains? ns-env '+latte-definition-environment+)
    ;;   (throw (ex-info "Cannot build initial definition environment: already existing" {:namespace (namespace '+latte-definition-environment+)
    ;;                                                                                    :var '+latte-definition-environment+}))
    (intern (ns-name *ns*) '+latte-definition-environment+
            (atom (reduce (fn [def-env [name definition]]
                            ;; (print "name = " name "def = " definition)
                            (if (latte-definition? definition)
                              (do (print "name = " name "def = " definition)
                                  (println " (... latte definition registered ...)")
                                  (assoc def-env name definition))
                              (do ;; (println " (... not a latte definition ...)")
                                def-env))) {} ns-env)))))
;;)

(defn fetch-def-env-atom
  []
  (let [lvar (let [lv (resolve '+latte-definition-environment+)]
               (if (not lv)
                 (do (build-initial-definition-environment!)
                     (resolve '+latte-definition-environment+))
                 lv))]
    ;;(println "Resolved!" lvar)
    @lvar))


(defn fetch-definition-environment
  []
  @(fetch-def-env-atom))

(defn handle-term-definition [tdef def-env params body]
  (let [params (mapv (fn [[x ty]] [x (stx/parse def-env ty)]) params)
        body (stx/parse def-env body)]
    ;; (println "[handle-term-definition] def-env = " def-env " params = " params " body = " body)
    (let [[status ty] (ty/type-of-term def-env params body)]
      (if (= status :ko)
        (throw (ex-info "Type error" {:def tdef
                                      :error ty}))
        (assoc tdef
               :params params
               :arity (count params)
               :type ty
               :parsed-term body)))))

(defn register-term-definition! [name definition]
   (let [def-atom (fetch-def-env-atom)]
     (swap! def-atom (fn [def-env]
                       (assoc def-env name definition)))))

(defn parse-defterm-args [args]
    (when (> (count args) 4)
      (throw (ex-info "Too many arguments for defterm" {:max-arity 4 :nb-args (count args)})))
    (when (< (count args) 2)
      (throw (ex-info "Not enough arguments for defterm" {:min-arity 2 :nb-args (count args)})))
  (let [body (last args)
        params (if (= (count args) 2)
                 []
                 (last (butlast args)))
        doc (if (= (count args) 4)
              (nth args 1)
              "No documentation.")
        def-name (first args)]
    (when (not (symbol? def-name))
      (throw (ex-info "Name of defterm must be a symbol." {:def-name def-name})))
    (when (not (string? doc))
      (throw (ex-info "Documentation string for defterm must be ... a string." {:def-name def-name :doc doc})))
    (when (not (vector? params))
      (throw (ex-info "Parameters of defterm must be a vector." {:def-name def-name :params params})))
    [def-name doc params body]))

(defmacro defterm
  [& args]
  (let [[def-name doc params body] (parse-defterm-args args)]
    ;;(println "def-name =" def-name " doc =" doc " params =" params " body =" body)
    (let [def-env (fetch-definition-environment)]
      ;;(println "def env = " def-env)
      (do
        (when (contains? def-env def-name)
          ;;(throw (ex-info "Cannot redefine term." {:name def-name})))
          ;; TODO: maybe disallow redefining if type is changed ?
          ;;       otherwise only warn ?
          (println "[Warning] redefinition of term: " def-name))
        (let [definition (as-> {:tag ::term :name def-name :doc doc} $
                           (handle-term-definition $ def-env params body))
              quoted-def (list 'quote definition)]
          (register-term-definition! def-name definition)
          (let [name# (name def-name)]
            `(do
             (def ~def-name ~quoted-def)
             [:registered ~name#])))))))

(defn parse-context-args [def-env args]
  (loop [args args, ctx []]
    (if (seq args)
      (do
        (when (not (and (vector? (first args))
                        (= (count (first args)) 2)))
          (throw (ex-info "Context argument must be a binding pair." {:argument (first args)})))
        (let [[x ty] (first args)
              ty' (stx/parse def-env ty)]
          (when (not (symbol? x))
            (throw (ex-info "Binding variable  must be a symbol." {:argument (first args) :variable x})))
          (when (not (ty/proper-type? def-env ctx ty'))
            (throw (ex-info "Binding type is not a type." {:argument (first args) :binding-type ty})))
          (recur (rest args) (conj ctx [x ty']))))
      ctx)))

(defmacro term [& args]
    (let [def-env (fetch-definition-environment)
          t (stx/parse def-env (last args))
          ctx (parse-context-args def-env (butlast args))]
      ;; (println "[term] t = " t " ctx = " ctx)
      (if (latte.norm/beta-eq? t :kind)
        '□
        (let [ty (ty/type-of def-env ctx t)]
          (list 'quote t)))))

(defmacro type-of [& args]
  (let [def-env (fetch-definition-environment)
        t (stx/parse def-env (last args))
        ctx (parse-context-args def-env (butlast args))]
    (let [ty (ty/type-of def-env ctx t)]
      (list 'quote ty))))

(defn === [t1 t2]
  (let [def-env (fetch-definition-environment)
        t1 (stx/parse def-env t1)
        t2 (stx/parse def-env t2)]
    (n/beta-delta-eq? def-env t1 t2)))


