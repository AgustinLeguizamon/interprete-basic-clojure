(ns tp-lenguajes-formales.core
  (:gen-class)
  (:require [clojure.string :as str]))

(declare driver-loop)                     ; NO TOCAR
(declare string-a-tokens)                 ; NO TOCAR
(declare evaluar-linea)                   ; NO TOCAR
(declare buscar-mensaje)                  ; NO TOCAR
(declare seleccionar-destino-de-on)       ; NO TOCAR
(declare leer-data)                       ; NO TOCAR
(declare leer-con-enter)                  ; NO TOCAR
(declare retornar-al-for)                 ; NO TOCAR
(declare continuar-programa)              ; NO TOCAR
(declare ejecutar-programa)               ; NO TOCAR
(declare mostrar-listado)                 ; NO TOCAR
(declare cargar-arch)                     ; NO TOCAR
(declare grabar-arch)                     ; NO TOCAR
(declare calcular-expresion)              ; NO TOCAR
(declare desambiguar-mas-menos)           ; NO TOCAR
(declare desambiguar-mid)                 ; NO TOCAR
(declare shunting-yard)                   ; NO TOCAR
(declare calcular-rpn)                    ; NO TOCAR
(declare imprimir)                        ; NO TOCAR
(declare desambiguar-comas)               ; NO TOCAR

(declare evaluar)                         ; COMPLETAR
(declare aplicar)                         ; COMPLETAR

(declare palabra-reservada?)              ; IMPLEMENTAR
(declare operador?)                       ; IMPLEMENTAR
(declare anular-invalidos)                ; IMPLEMENTAR
(declare cargar-linea)                    ; IMPLEMENTAR
(declare expandir-nexts)                  ; IMPLEMENTAR
(declare dar-error)                       ; IMPLEMENTAR
(declare variable-float?)                 ; IMPLEMENTAR
(declare variable-integer?)               ; IMPLEMENTAR
(declare variable-string?)                ; IMPLEMENTAR
(declare contar-sentencias)               ; IMPLEMENTAR
(declare buscar-lineas-restantes)         ; IMPLEMENTAR
(declare continuar-linea)                 ; IMPLEMENTAR
(declare extraer-data)                    ; IMPLEMENTAR
(declare ejecutar-asignacion)             ; IMPLEMENTAR
(declare preprocesar-expresion)           ; IMPLEMENTAR
(declare desambiguar)                     ; IMPLEMENTAR
(declare precedencia)                     ; IMPLEMENTAR
(declare aridad)                          ; IMPLEMENTAR
(declare eliminar-cero-decimal)           ; IMPLEMENTAR
(declare eliminar-cero-entero)            ; IMPLEMENTAR


; [(prog-mem)  [prog-ptrs]  [gosub-return-stack]  [for-next-stack]  [data-mem]  data-ptr  {var-mem}]
(def indice-amb {:prog-mem 0, :prog-ptrs 1, :gosub-return-stack 2, :data-ptr 5 :hash-map 6})

(defn spy
  ([x] (do (prn x) x))
  ([msg x] (do (print msg) (print ": ") (prn x) x)))

(defn get-hash-map-amb [amb]
  (last amb))

(defn get-amb-prog-ptrs [amb]
  (nth amb (indice-amb :prog-ptrs)))

(defn -main
  "Ejemplo de Proyecto en Clojure"
  [& args]
  (driver-loop))

(defn driver-loop
  ([]
   (println)
   (println "Interprete de BASIC en Clojure")
   (println "Trabajo Practico de 75.14/95.48 Lenguajes Formales - 2023")
   (println)
   (println "Inspirado en:  ******************************************")
   (println "               *                                        *")
   (println "               *    **** COMMODORE 64 BASIC V2 ****     *")
   (println "               *                                        *")
   (println "               * 64K RAM SYSTEM  38911 BASIC BYTES FREE *")
   (println "               *                                        *")
   (println "               ******************************************")
   (flush)
   (driver-loop ['() [:ejecucion-inmediata 0] [] [] [] 0 {}]))  ; [(prog-mem)  [prog-ptrs]  [gosub-return-stack]  [for-next-stack]  [data-mem]  data-ptr  {var-mem}]
  ([amb]
   (prn) (println "READY.") (flush)
   (try (let [linea (string-a-tokens (read-line)), cabeza (first linea)]
          (cond (= cabeza '(EXIT)) 'GOODBYE
                (= cabeza '(ENV)) (do (prn amb) (flush) (driver-loop amb))
                (integer? cabeza) (if (and (>= cabeza 0) (<= cabeza 63999))
                                    (driver-loop (cargar-linea linea amb))
                                    (do (dar-error 16 (amb 1)) (driver-loop amb))) ; Syntax error
                (empty? linea) (driver-loop amb)
                :else (driver-loop (second (evaluar-linea linea (assoc amb 1 [:ejecucion-inmediata (count (expandir-nexts linea))]))))))
        (catch Exception e (dar-error (str "?ERROR " (clojure.string/trim (clojure.string/upper-case (get (Throwable->map e) :cause)))) (amb 1)) (driver-loop amb)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; string-a-tokens: analisis lexico y traduccion del codigo a la
; representacion intermedia (listas de listas de simbolos) que
; sera ejecutada (o vuelta atras cuando deba ser mostrada)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn string-a-tokens [s]
  (let [nueva (str s ":"),
        mayu (clojure.string/upper-case nueva),
        sin-cad (clojure.string/replace mayu #"\"(.*?)\"" #(clojure.string/join (take (+ (count (% 1)) 2) (repeat "@")))),
        ini-rem (clojure.string/index-of sin-cad "REM"),
        pre-rem (subs mayu 0 (if (nil? ini-rem) (count mayu) ini-rem))
        pos-rem (subs mayu (if (nil? ini-rem) (- (count mayu) 1) (+ ini-rem 3)) (- (count mayu) 1))
        sin-rem (->> pre-rem
                     (re-seq #"EXIT|ENV|DATA[^\:]*?\:|REM|NEW|CLEAR|LIST|RUN|LOAD|SAVE|LET|AND|OR|NOT|ABS|SGN|INT|SQR|SIN|COS|TAN|ATN|EXP|LOG|LEN|LEFT\$|MID\$|RIGHT\$|STR\$|VAL|CHR\$|ASC|GOTO|ON|IF|THEN|FOR|TO|STEP|NEXT|GOSUB|RETURN|END|INPUT|READ|RESTORE|PRINT|\<\=|\=\<|\>\=|\=\>|\<\>|\>\<|\<|\>|\=|\(|\)|\?|\;|\:|\,|\+|\-|\*|\/|\^|\"[^\"]*\"|\d+\.\d+E[+-]?\d+|\d+\.E[+-]?\d+|\.\d+E[+-]?\d+|\d+E[+-]?\d+|\d+\.\d+|\d+\.|\.\d+|\.|\d+|[A-Z][A-Z0-9]*[\%\$]?|[A-Z]|\!|\"|\#|\$|\%|\&|\'|\@|\[|\\|\]|\_|\{|\||\}|\~")
                     (map #(if (and (> (count %) 4) (= "DATA" (subs % 0 4))) (clojure.string/split % #":") [%]))
                     (map first)
                     (remove nil?)
                     (replace '{"?" "PRINT"})
                     (map #(if (and (> (count %) 1) (clojure.string/starts-with? % ".")) (str 0 %) %))
                     (map #(if (and (>= (count %) 4) (= "DATA" (subs % 0 4))) (let [provisorio (interpose "," (clojure.string/split (clojure.string/triml (subs % 4)) #",[ ]*"))] (list "DATA" (if (= ((frequencies %) \,) ((frequencies provisorio) ",")) provisorio (list provisorio ",")) ":")) %))
                     (flatten)
                     (map #(let [aux (try (clojure.edn/read-string %) (catch Exception e (symbol %)))] (if (or (number? aux) (string? aux)) aux (symbol %))))
                     (#(let [aux (first %)] (if (and (integer? aux) (not (neg? aux))) (concat (list aux) (list (symbol ":")) (rest %)) %)))
                     (partition-by #(= % (symbol ":")))
                     (remove #(.contains % (symbol ":")))
                     (#(if (and (= (count (first %)) 1) (number? (ffirst %))) (concat (first %) (rest %)) %)))]
    (if (empty? pos-rem)
      sin-rem
      (concat sin-rem (list (list 'REM (symbol (clojure.string/trim pos-rem))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; evaluar-linea: recibe una lista de sentencias y las evalua
; mientras sea posible hacerlo
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment

  (evaluar-linea (list '(IF N < 1 THEN GOTO 90)) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])
  ;; OK
  (expandir-nexts (list '(IF N < 1 THEN GOTO 90)))
  ;; OK
  (anular-invalidos '(IF N < 1 THEN GOTO 90))
  ;; aca esta el null pointer
  (evaluar '(IF N < 1 THEN GOTO 90) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])

  (evaluar-linea (list (list 'INPUT "HOW MANY STARS DO YOU WANT" (symbol ";") 'N)) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])

  ;; OK
  (evaluar-linea (list '(S = 3)) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])
  (evaluar-linea (list '(S$ = "")) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])

  ;; OK
  (expandir-nexts (list '(S$ = "")))

  (anular-invalidos '(S$ = ""))

  ;; OK
  (evaluar-linea (list (list 'L '= 'LEN (symbol "(") 'N$ (symbol ")"))) [() [:ejecucion-inmediata 0] [] [] [] 0 {'N$ "HOLA"}])
  (expandir-nexts (list '(L = LEN (symbol "(") N$ (symbol ")"))))
  (evaluar (list 'L '= 'LEN (symbol "(") 'N$ (symbol ")")) [() [:ejecucion-inmediata 0] [] [] [] 0 {'N$ "HOLA"}])

  ;; OK
  (evaluar-linea (list (list 'PRINT 'MID$ (symbol "(") 'N$ (symbol ",") 'I (symbol ")"))) [() [:ejecucion-inmediata 0] [] [] [] 0 {'N$ "HOLA", 'L 3, 'I 1}])

  ;; OK
  (evaluar-linea (list (list 'LET 'N '= '1)) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])
  (evaluar (list (list 'LET 'N '= '1)) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])

  ;; IMPORTANTE: la linea primero pasa por strings a tokens que hace cosas con simbolos como :
  ;; para crear varias sentencias asi que CUIDADO al copy pastear las entradas en evaluar-linea

  ;; OK pero falta definir linea 20 para que no rompa GOTO
  (evaluar-linea (list (list 'IF 'A '<= '0 'OR 'B '<= '0 'OR 'INT (symbol "(") 'A (symbol ")") '<> 'A 'OR 'INT (symbol "(") 'B (symbol ")") '<> 'B 'THEN 'GOTO 20)) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])
  (expandir-nexts (list (list 'IF 'A '<= '0 'OR 'B '<= '0 'OR 'INT (symbol "(") 'A (symbol ")") '<> 'A 'OR 'INT (symbol "(") 'B (symbol ")") '<> 'B 'THEN 'GOTO 20)))
  (anular-invalidos (list 'IF 'A '<= '0 'OR 'B '<= '0 'OR 'INT (symbol "(") 'A (symbol ")") '<> 'A 'OR 'INT (symbol "(") 'B (symbol ")") '<> 'B 'THEN 'GOTO 20))
  (evaluar (list 'IF 'A '<= '0 'OR 'B '<= '0 'OR 'INT (symbol "(") 'A (symbol ")") '<> 'A 'OR 'INT (symbol "(") 'B (symbol ")") '<> 'B 'THEN 'GOTO 20) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])

  ;; OK
  (evaluar-linea (list (list 'LET 'C '= 'A '- 'INT (symbol "(") 'A '/ 'B (symbol ")") '* 'B)) [() [:ejecucion-inmediata 0] [] [] [] 0 {'A 4, 'B 2}])
  (expandir-nexts (list (list 'LET 'C '= 'A '- 'INT (symbol "(") 'A '/ 'B (symbol ")") '* 'B)))
  (anular-invalidos (list 'LET 'C '= 'A '- 'INT (symbol "(") 'A '/ 'B (symbol ")") '* 'B))

  ;; OK
  (evaluar-linea (list (list 'LET 'P '= '.)) [() [:ejecucion-inmediata 0] [] [] [] 0 {'P 4, 'B 2}])

  ;; OK
  (evaluar-linea (list (list 'IF 'P '= 1 'THEN 'PRINT 'X (symbol ";") " " (symbol ";"))) [() [:ejecucion-inmediata 0] [] [] [] 0 {'P 2}])

  ;; OK
  (evaluar-linea (list (list 'L '= 'ASC (symbol "(") 'MID$ (symbol "(") 'W$ (symbol ",") 'I (symbol ",") 1 (symbol ")") (symbol ")") '- 64)) [() [:ejecucion-inmediata 0] [] [] [] 0 {'W$ "tomato", 'I 1}])

  ;; OK
  (evaluar-linea (list (list 'IF 'L '< 1 'OR 'L '> 26 'THEN 'PRINT "??? " (symbol ";") (symbol ":") 'GOTO 190)) [() [:ejecucion-inmediata 0] [] [] [] 0 {'L 4}])

  ;; OK, pero recordar que evaluar-linea recibe las 3 expresiones por separado, sin el :
  (evaluar-linea (list (list 'FOR 'J '= '1 'TO 'L (symbol ":") 'READ 'S$ (symbol ":") 'NEXT 'J)) [() [:ejecucion-inmediata 0] [] [] [] 0 {'L 4}])
  (expandir-nexts (list (list 'FOR 'J '= '1 'TO 'L (symbol ":") 'READ 'S$ (symbol ":") 'NEXT 'J)))
  (anular-invalidos (list 'FOR 'J '= '1 'TO 'L (symbol ":") 'READ 'S$ (symbol ":") 'NEXT 'J))

  ;; OK
  (evaluar-linea (list (list 'FOR 'A '= 0 'TO 8 '* 'ATN (symbol "(") 1 (symbol ")") 'STEP 0.1)) [() [:ejecucion-inmediata 0] [] [] [] 0 {'L 4}])
  (expandir-nexts (list (list 'FOR 'A '= 0 'TO 8 '* 'ATN (symbol "(") 1 (symbol ")") 'STEP 0.1)))
  (anular-invalidos (list 'FOR 'A '= 0 'TO 8 '* 'ATN (symbol "(") 1 (symbol ")") 'STEP 0.1))

  (evaluar-linea (list (list 'PRINT 'INT (symbol "(") 'A '* '100 (symbol ")") '/ 100 (symbol ",") "   " (symbol ";") 'INT (symbol "(") 'SIN (symbol "(") 'A (symbol ")") '* 100000 (symbol ")") '/ 100000)) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])
  (anular-invalidos (list 'PRINT 'INT (symbol "(") 'A '* '100 (symbol ")") '/ 100 (symbol ",") "   " (symbol ";") 'INT (symbol "(") 'SIN (symbol "(") 'A (symbol ")") '* 100000 (symbol ")") '/ 100000))

  ;; fix
  (evaluar-linea (list (list 'NEXT)) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])
  
  :rcf)

(defn evaluar-linea
  ([sentencias amb]
   (let [sentencias-con-nexts-expandidos (expandir-nexts sentencias)]
     (evaluar-linea sentencias-con-nexts-expandidos sentencias-con-nexts-expandidos amb)))
  ([linea sentencias amb]
   (if (empty? sentencias)
     [:sin-errores amb]
     (let [sentencia (anular-invalidos (first sentencias)), par-resul (evaluar sentencia amb)]
       (if (or (nil? (first par-resul)) (contains? #{:omitir-restante, :error-parcial, :for-inconcluso} (first par-resul)))
         (if (and (= (first (amb 1)) :ejecucion-inmediata) (= (first par-resul) :for-inconcluso))
           (recur linea (take-last (second (second (second par-resul))) linea) (second par-resul))
           par-resul)
         (recur linea (next sentencias) (assoc (par-resul 1) 1 [(first (amb 1)) (count (next sentencias))])))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; buscar-mensaje: retorna el mensaje correspondiente a un error
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn buscar-mensaje [cod]
  (case cod
    0 "?NEXT WITHOUT FOR  ERROR"
    6 "FILE NOT FOUND"
    15 "NOT DIRECT COMMAND"
    16 "?SYNTAX  ERROR"
    22 "?RETURN WITHOUT GOSUB  ERROR"
    42 "?OUT OF DATA  ERROR"
    53 "?ILLEGAL QUANTITY  ERROR"
    69 "?OVERFLOW  ERROR"
    90 "?UNDEF'D STATEMENT  ERROR"
    100 "?ILLEGAL DIRECT  ERROR"
    133 "?DIVISION BY ZERO  ERROR"
    163 "?TYPE MISMATCH  ERROR"
    176 "?STRING TOO LONG  ERROR"
    200 "?LOAD WITHIN PROGRAM  ERROR"
    201 "?SAVE WITHIN PROGRAM  ERROR"
    cod))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; seleccionar-destino-de-on: recibe una lista de numeros
; separados por comas, un indice y el ambiente, y retorna el
; numero a que hace referencia el indice (se cuenta desde 1)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn seleccionar-destino-de-on
  ([destinos indice amb]
   (cond
     (or (neg? indice) (> indice 255)) (do (dar-error 53 (amb 1)) nil)  ; Illegal quantity error
     (zero? indice) :omitir-restante
     :else (seleccionar-destino-de-on (if (= (last destinos) (symbol ",")) (concat destinos [0]) destinos) indice amb 1)))
  ([destinos indice amb contador]
   (cond
     (nil? destinos) :omitir-restante
     (= contador indice) (if (= (first destinos) (symbol ",")) 0 (first destinos))
     (= (first destinos) (symbol ",")) (recur (next destinos) indice amb (inc contador))
     (or (= (count destinos) 1)
         (and (> (count destinos) 1) (= (second destinos) (symbol ",")))) (recur (nnext destinos) indice amb (inc contador))
     :else (do (dar-error 16 (amb 1)) nil)))  ; Syntax error
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; leer-data: recibe una lista de variables separadas por comas
; y un ambiente, y retorna una dupla (un vector) con un
; resultado (usado luego por evaluar-linea) y un ambiente
; actualizado incluyendo las variables cargadas con los valores
; definidos en la(s) sentencia(s) DATA
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn leer-data
  ([param-de-read amb]
   (cond
     (= (first (amb 1)) :ejecucion-inmediata) (do (dar-error 15 (amb 1)) [nil amb])  ; Not direct command
     (empty? param-de-read) (do (dar-error 16 (amb 1)) [nil amb])  ; Syntax error
     :else (leer-data param-de-read (drop (amb 5) (amb 4)) amb)))
  ([variables entradas amb]
   (cond
     (empty? variables) [:sin-errores amb]
     (empty? entradas) (do (dar-error 42 (amb 1)) [:error-parcial amb])  ; Out of data error
     :else (let [res (ejecutar-asignacion (list (first variables) '= (if (variable-string? (first variables)) (str (first entradas)) (if (= (first entradas) "") 0 (first entradas)))) amb)]
             (if (nil? res)
               [nil amb]
               (if (or (= (count (next variables)) 1)
                       (and (> (count (next variables)) 1) (not= (fnext variables) (symbol ","))))
                 (do (dar-error 16 (amb 1)) [:error-parcial res])  ; Syntax error
                 (recur (nnext variables) (next entradas) (assoc res 5 (inc (res 5))))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; leer-con-enter: recibe una lista con una cadena opcional
; seguida de variables separadas por comas y un ambiente, y
; retorna una dupla (un vector) con un resultado (usado luego
; por evaluar-linea) y un ambiente actualizado incluyendo las
; variables cargadas con los valores leidos del teclado
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment
  (leer-con-enter (list 'V$) [() [] [] [] [] 0 {}]) 

  :rcf)

(defn leer-con-enter
  ([param-de-input amb]
   (leer-con-enter param-de-input param-de-input amb))
  ([param-orig param-actualizados amb]
   (let [prim-arg (first param-actualizados), es-cadena (string? prim-arg)]
     (if (and es-cadena (not= (second param-actualizados) (symbol ";")))
       (do (dar-error 16 (amb 1)) [nil amb])  ; Syntax error
       (do (if es-cadena
             (print (str prim-arg "? "))
             (print "? "))
           (flush)
           (if (= (first (amb 1)) :ejecucion-inmediata)
             (do (dar-error 100 (amb 1)) [nil amb])  ; Illegal direct error
             (let [variables (if es-cadena (nnext param-actualizados) param-actualizados),
                   valores (butlast (map clojure.string/trim (.split (apply str (.concat (read-line) ",.")) ","))),
                   entradas (map #(let [entr (try (clojure.edn/read-string %) (catch Exception e (str %)))] (if (number? entr) entr (clojure.string/upper-case (str %)))) valores)]
               (if (empty? variables)
                 (do (dar-error 16 (amb 1)) [nil amb])  ; Syntax error
                 (leer-con-enter variables entradas param-orig param-actualizados amb amb))))))))
  ([variables entradas param-orig param-actualizados amb-orig amb-actualizado]
   (cond
     (and (empty? variables) (empty? entradas)) [:sin-errores amb-actualizado]
     (and (empty? variables) (not (empty? entradas))) (do (println "?EXTRA IGNORED") (flush) [:sin-errores amb-actualizado])
     (and (not (empty? variables)) (empty? entradas)) (leer-con-enter param-orig (concat (list "?? " (symbol ";")) variables) amb-actualizado)
     (and (not (variable-string? (first variables))) (string? (first entradas))) (do (println "?REDO FROM START") (flush) (leer-con-enter param-orig param-orig amb-orig))
     :else (let [res (ejecutar-asignacion (list (first variables) '= (if (variable-string? (first variables)) (str (first entradas)) (first entradas))) amb-actualizado)]
             (if (nil? res)
               [nil amb-actualizado]
               (if (or (= (count (next variables)) 1)
                       (and (> (count (next variables)) 1) (not= (fnext variables) (symbol ","))))
                 (do (dar-error 16 (amb-actualizado 1)) [:error-parcial res])  ; Syntax error
                 (recur (nnext variables) (next entradas) param-orig param-actualizados amb-orig res)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; retornar-al-for: implementa la sentencia NEXT, retornando una
; dupla (un vector) con un resultado (usado luego por
; evaluar-linea) y un ambiente actualizado con el nuevo valor
; de la variable de control
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 
(defn retornar-al-for [amb var-next]
  (if (empty? (amb 3))
    (do (dar-error 0 (amb 1)) [nil amb])  ; Next without for error
    (let [datos-for (peek (amb 3)),
          var-for (nth datos-for 0),
          valor-final (nth datos-for 1),
          valor-step (nth datos-for 2),
          origen (nth datos-for 3)]
      (if (and (some? var-next) (not= var-next var-for))
        (retornar-al-for (assoc amb 3 (pop (amb 3))) var-next)
        (let [var-actualizada (+ (calcular-expresion (list var-for) amb) valor-step),
              res (ejecutar-asignacion (list var-for '= var-actualizada) amb)]
          (if (or (and (neg? valor-step) (>= var-actualizada valor-final))
                  (and (pos? valor-step) (<= var-actualizada valor-final)))
            [:for-inconcluso (assoc res 1 [(origen 0) (dec (origen 1))])]
            [:sin-errores (assoc res 3 (pop (amb 3)))]))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; continuar-programa: recibe un ambiente que fue modificado por
; GOTO o GOSUB y continua la ejecucion del programa a partir de
; ese ambiente
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn continuar-programa [amb]
  (ejecutar-programa amb (buscar-lineas-restantes amb)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; ejecutar-programa: recibe un ambiente e inicia la ejecucion
; del programa a partir de ese ambiente 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn ejecutar-programa
  ([amb]
   (let [ini [(amb 0) (amb 1) [] [] (vec (extraer-data (amb 0))) 0 {}]]  ; [(prog-mem)  [prog-ptrs]  [gosub-return-stack]  [for-next-stack]  [data-mem]  data-ptr  {var-mem}]
     (ejecutar-programa ini (buscar-lineas-restantes ini))))
  ([amb prg]
   (if (or (nil? prg) (= (first (amb 1)) :ejecucion-inmediata))
     [:sin-errores amb]
     (let [antes (assoc amb 1 [(ffirst prg) (second (amb 1))]), res (evaluar-linea (nfirst prg) antes), nuevo-amb (second res)]
       (cond (nil? (first res)) [nil amb]   ; hubo error total 
             (= (first res) :error-parcial) [nil (second res)]   ; hubo error parcial
             :else (let [proximo (if (and (= (first (antes 1)) (first (nuevo-amb 1))) (not= (first res) :for-inconcluso))
                                   (next prg)   ; no hubo quiebre de secuencia
                                   (buscar-lineas-restantes nuevo-amb)),
                         nueva-posic (if (nil? proximo) (nuevo-amb 1) [(ffirst proximo) (count (expandir-nexts (nfirst proximo)))])]
                     (recur (assoc nuevo-amb 1 nueva-posic) proximo)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; mostrar-listado: recibe la representacion intermedia de un
; programa y lo lista usando la representacion normal
; (usualmente mas legible que la ingresada originalmente)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn mostrar-listado
  ([lineas]
   (if (empty? lineas)
     nil
     (mostrar-listado (next lineas) (first lineas))))
  ([lineas sentencias]
   (if (empty? sentencias)
     (do (prn) (mostrar-listado lineas))
     (mostrar-listado lineas (next sentencias) (first sentencias))))
  ([lineas sentencias elementos]
   (if (and (not (seq? elementos)) (integer? elementos))
     (do (pr elementos) (print "  ") (mostrar-listado lineas sentencias))
     (if (empty? elementos)
       (do (if (not (empty? sentencias)) (print ": "))
           (mostrar-listado lineas sentencias))
       (do (pr (first elementos))
           (if (not (or (contains? #{(symbol "(") (symbol ",") (symbol ";")} (first elementos))
                        (contains? #{(symbol ")") (symbol ",") (symbol ";")} (fnext elementos))
                        (nil? (fnext elementos)))) (print " "))
           (recur lineas sentencias (next elementos)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; cargar-arch: recibe un nombre de archivo y retorna la
; representacion intermedia del codigo contenido en el
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn cargar-arch [nom nro-linea]
  (if (.exists (clojure.java.io/file nom))
    (remove empty? (with-open [rdr (clojure.java.io/reader nom)] (doall (map string-a-tokens (line-seq rdr)))))
    (dar-error 6 nro-linea))  ; File not found
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; grabar-arch: recibe un nombre de archivo, graba en el
; el listado del programa usando la representacion normal y
; retorna el ambiente
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn grabar-arch [nom amb]
  (let [arch (clojure.java.io/writer nom)]
    (do (binding [*out* arch] (mostrar-listado (amb 0)))
        (.close arch)
        amb)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; calcular-expresion: recibe una expresion y un ambiente, y
; retorna el valor de la expresion, por ejemplo:
; user=> (calcular-expresion '(X + 5) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X 2}])
; 7
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion '(X$) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X$ "HOLA"}]))) [10 1])
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion '("HOLA") ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X$ "HOLA"}]))) [10 1])
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion '("HOLA" + "MUNDO") ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X$ "HOLA"}]))) [10 1])
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion '(X$ + " MUNDO" + Z$) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X$ "HOLA"}]))) [10 1])
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion '(X$ + " MUNDO") ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X$ "HOLA"}]))) [10 1])
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion (list 'LEN (symbol "(") 'N$ (symbol ")")) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{N$ "HOLA"}]))) [10 1])

  ;; OK
  (calcular-expresion (list 'MID$ (symbol "(") 'N$ (symbol ",") 'I (symbol ")")) [() [:ejecucion-inmediata 0] [] [] [] 0 {'N$ "HOLA", 'L 3, 'I 2}])
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion (list 'MID$ (symbol "(") 'N$ (symbol ",") 'I (symbol ")")) [() [:ejecucion-inmediata 0] [] [] [] 0 {'N$ "HOLA", 'L 3, 'I 1}]))) [10 1])

  ;; OK
  (calcular-expresion (list 'A '<= '0 'OR 'B '<= '0 'OR 'INT (symbol "(") 'A (symbol ")") '<> 'A 'OR 'INT (symbol "(") 'B (symbol ")") '<> 'B) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion (list 'A '<= '0 'OR 'B '<= '0 'OR 'INT (symbol "(") 'A (symbol ")") '<> 'A 'OR 'INT (symbol "(") 'B (symbol ")") '<> 'B) [() [:ejecucion-inmediata 0] [] [] [] 0 {'A 4, 'B 2}]))) ([() [:ejecucion-inmediata 0] [] [] [] 0 {'A 4, 'B 2}] 1))

  ;; OK
  (calcular-expresion (list 'A '- 'INT (symbol "(") 'A '/ 'B (symbol ")") '* 'B) [() [:ejecucion-inmediata 0] [] [] [] 0 {'A 4, 'B 2}])

  ;; OK
  (calcular-expresion (list 'MID$ (symbol "(") 'W$ (symbol ",") 'I (symbol ",") 1 (symbol ")")) [() [:ejecucion-inmediata 0] [] [] [] 0 {'W$ "tomato", 'I 1}])
  (calcular-expresion (list 'MID$ (symbol "(") 'W$ (symbol ",") 'I (symbol ",") 3 (symbol ")")) [() [:ejecucion-inmediata 0] [] [] [] 0 {'W$ "tomato", 'I 1}])

  ;; OK
  (calcular-expresion (list 'ASC (symbol "(") 'MID$ (symbol "(") 'W$ (symbol ",") 'I (symbol ",") 1 (symbol ")") (symbol ")") '- 64) [() [:ejecucion-inmediata 0] [] [] [] 0 {'W$ "AMSTRONG", 'I 1}])

  ;; OK
  (calcular-expresion (list 'ATN (symbol "(") 1 (symbol ")")) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])
  (calcular-expresion (list 8 '* 'ATN (symbol "(") 1 (symbol ")")) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])

  (calcular-expresion (list 'INT (symbol "(") 'A '* '100 (symbol ")") '/ 100 (symbol ",") "   " (symbol ";") 'INT (symbol "(") 'SIN (symbol "(") 'A (symbol ")") '* 100000 (symbol ")") '/ 100000) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])

  ;; OK
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion (list 'INT (symbol "(") 'A '* '100 (symbol ")") '/ 100 ) [() [:ejecucion-inmediata 0] [] [] [] 0 {'A 1}]))) [:ejecucion-inmediata 0])
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion (list 'INT (symbol "(") 'SIN (symbol "(") 'A (symbol ")") '* 100000 (symbol ")") '/ 100000) [() [:ejecucion-inmediata 0] [] [] [] 0 {'A 1}]))) [:ejecucion-inmediata 0])

  ;;OK MID$ (STR$ (B), 2, 1)
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion (list 'MID$ (symbol "(") 'STR$ (symbol "(") 'B (symbol ")") (symbol ",") 2 (symbol ",") 1(symbol ")")) [() [:ejecucion-inmediata 0] [] [] [] 0 {'B 12}]))) [:ejecucion-inmediata 0]) 
  
  ;; CHR$ (65 + B - 10)
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion (list 'CHR$ (symbol "(") 65 '+ 'B '- 10 (symbol ")")) [() [:ejecucion-inmediata 0] [] [] [] 0 {'B 12}]))) [:ejecucion-inmediata 0])
  
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion (list 'V$ '<> "") [() [:ejecucion-inmediata 0] [] [] [] 0 {'V$ "QUIT"}]))) [:ejecucion-inmediata 0])
  
  ;; S + INT (0.1 + EXP (P * LOG (2)))
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion (list 'S '+ 'INT (symbol "(") 0.1 '+ 'EXP (symbol "(") 'P '* 'LOG (symbol "(") 2 (symbol ")") (symbol ")") (symbol ")") ) [() [:ejecucion-inmediata 0] [] [] [] 0 {'S 4 'P 1}]))) [:ejecucion-inmediata 0])

  :rcf)

(defn calcular-expresion [expr amb]
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion expr amb))) (amb 1)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; desambiguar-mas-menos: recibe una expresion y la retorna sin
; los + unarios y con los - unarios reemplazados por -u  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn desambiguar-mas-menos
  ([expr] (desambiguar-mas-menos expr nil []))
  ([expr ant res]
   (if (nil? expr)
     (remove nil? res)
     (let [act (first expr), nuevo (if (or (nil? ant) (and (symbol? ant) (operador? ant)) (= (str ant) "(") (= (str ant) ","))
                                     (case act
                                       + nil
                                       - '-u
                                       act)
                                     act)]
       (recur (next expr) act (conj res nuevo))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; desambiguar-mid: recibe una expresion y la retorna con los
; MID$ ternarios reemplazados por MID3$ 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment
  
  (desambiguar-mid (list 'PRINT 'MID$ (symbol "(") 'N$, 'I (symbol ")")))

  :rcf)

(defn desambiguar-mid
  ([expr]
   (cond
     (contains? (set expr) 'MID$) (desambiguar-mid expr 0 (count expr) 0 0 0 true)
     (contains? (set expr) 'MID2$) (apply list (replace '{MID2$ MID$} expr))
     :else (apply list expr)))
  ([expr act fin pos cont-paren cont-comas buscando]
   (if (= act fin)
     (desambiguar-mid expr)
     (let [nuevo (nth expr act)]
       (cond
         (and (= nuevo 'MID$) buscando) (recur expr (inc act) fin act cont-paren cont-comas false)
         (and (= nuevo (symbol "(")) (not buscando)) (recur expr (inc act) fin pos (inc cont-paren) cont-comas buscando)
         (and (= nuevo (symbol ")")) (not buscando))
         (if (= cont-paren 1)
           (if (= cont-comas 2)
             (recur (assoc (vec expr) pos 'MID3$) (inc act) fin 0 0 0 true)
             (recur (assoc (vec expr) pos 'MID2$) (inc act) fin 0 0 0 true))
           (recur expr (inc act) fin pos (dec cont-paren) cont-comas buscando))
         (and (= nuevo (symbol ",")) (= cont-paren 1)) (recur expr (inc act) fin pos cont-paren (inc cont-comas) buscando)
         :else (recur expr (inc act) fin pos cont-paren cont-comas buscando))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; shunting-yard: implementa el algoritmo del Patio de Maniobras
; de Dijkstra que convierte una expresion a RPN (Reverse Polish
; Notation), por ejemplo:
; user=> (shunting-yard '(1 + 2))
; (1 2 +)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment
  (shunting-yard '("HOLA" + "MUNDO"))

  (shunting-yard '(LEN "HOLA"))

  (shunting-yard (desambiguar (preprocesar-expresion (list 'LEN (symbol "(") 'N$ (symbol ")")) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{N$ "HOLA"}])))

  (count (desambiguar (preprocesar-expresion (list 'LEN (symbol "(") 'N$ (symbol ")")) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{N$ "HOLA"}])))

  (shunting-yard (list 'LEN (symbol "(") "HOLA" (symbol ")")))
  (shunting-yard (list 'LEN "HOLA"))

  (shunting-yard (list (symbol "(") 1 '+ 2 (symbol ")") '* 3))

  (count (shunting-yard (list 'LEN (symbol "(") "HOLA" (symbol ")"))))

  (shunting-yard (desambiguar (preprocesar-expresion (list 'MID$ (symbol "(") 'N$ (symbol ",") 'I (symbol ")")) [() [:ejecucion-inmediata 0] [] [] [] 0 {'N$ "HOLA", 'L 3, 'I 1}])))
  (shunting-yard (list 'MID$ (symbol "(") "HOLA" (symbol ",") 1 (symbol ")")))
  (calcular-rpn (list "HOLA" 1 'MID$) [10 1])

  (shunting-yard (desambiguar (preprocesar-expresion (list 'MID$ (symbol "(") 'W$ (symbol ",") 'I (symbol ",") 1 (symbol ")")) [() [:ejecucion-inmediata 0] [] [] [] 0 {'W$ "tomato", 'I 1}])))

  ;; OK
  (shunting-yard (desambiguar (preprocesar-expresion (list 'MID$ (symbol "(") 'STR$ (symbol "(") 'B (symbol ")") (symbol ",") 2 (symbol ",") 1 (symbol ")")) [() [:ejecucion-inmediata 0] [] [] [] 0 {'B 12}]))) 

  :rcf)

(defn shunting-yard [tokens]
  (remove #(= % (symbol ","))
          (flatten
           (reduce
            (fn [[rpn pila] token]
              (let [op-mas? #(and (some? (precedencia %)) (>= (precedencia %) (precedencia token)))
                    no-abre-paren? #(not= (str %) "(")]
                (cond
                  (= (str token) "(") [rpn (cons token pila)]
                  (= (str token) ")") [(vec (concat rpn (take-while no-abre-paren? pila))) (rest (drop-while no-abre-paren? pila))]
                  (some? (precedencia token)) [(vec (concat rpn (take-while op-mas? pila))) (cons token (drop-while op-mas? pila))]
                  :else [(conj rpn token) pila])))
            [[] ()]
            tokens))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; calcular-rpn: Recibe una expresion en RPN y un numero de linea
; y retorna el valor de la expresion o un mensaje de error en la
; linea indicada
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment

  (calcular-rpn '("HOLA" " MUNDO" +) (['((10 (PRINT X))) [10 1] [] [] [] 0 '{X$ "HOLA"}] 1))
  (calcular-rpn '(3 4 +) [(['() [10 1] [] [] [] 0 '{}] 1)])
  (calcular-rpn '(3 4 -) (['() [10 1] [] [] [] 0 '{}] 1))
  (calcular-rpn '(2 1 <) [10 1])
  (calcular-rpn '(1 2 <) [10 1])

  (aplicar 'LEN "HOLA" [10 1])

  ;; OK
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion (list 'LEN (symbol "(") 'N$ (symbol ")")) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{N$ "HOLA"}]))) [10 1])
  ;; OK
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion (list 'MID$ (symbol "(") 'N$ (symbol ",") 'I (symbol ")")) [() [:ejecucion-inmediata 0] [] [] [] 0 {'N$ "HOLA", 'L 3, 'I 1}]))) [10 1])

  ;; OK
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion (list 'A '<= '0 'OR 'B '<= '0 'OR 'INT (symbol "(") 'A (symbol ")") '<> 'A 'OR 'INT (symbol "(") 'B (symbol ")") '<> 'B) [() [:ejecucion-inmediata 0] [] [] [] 0 {'A 4, 'B 2}]))) [:ejecucion-inmediata 0])
  
  ;; OK
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion (list 'A '<= '0 'OR 'B '<= '0) [() [:ejecucion-inmediata 0] [] [] [] 0 {'A 4, 'B 2}]))) [:ejecucion-inmediata 0])

  ;; OK
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion (list 'A '<= '0 'OR 'B '<= '0 'OR 'INT (symbol "(") 'A (symbol ")") '<> 'A) [() [:ejecucion-inmediata 0] [] [] [] 0 {'A 4, 'B 2}]))) [:ejecucion-inmediata 0])
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion (list 'INT (symbol "(") 'A (symbol ")")) [() [:ejecucion-inmediata 0] [] [] [] 0 {'A 4, 'B 2}]))) [:ejecucion-inmediata 0]) 
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion (list (symbol "(") 'A (symbol ")") '<> 'A) [() [:ejecucion-inmediata 0] [] [] [] 0 {'A 4, 'B 2}]))) [:ejecucion-inmediata 0])
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion (list (symbol "(") 2 (symbol ")") '<> 1) [() [:ejecucion-inmediata 0] [] [] [] 0 {'A 4, 'B 2}]))) [:ejecucion-inmediata 0]) 
  (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion (list 'INT (symbol "(") 2 (symbol ")") '<> 4) [() [:ejecucion-inmediata 0] [] [] [] 0 {'A 4, 'B 2}]))) [:ejecucion-inmediata 0])
  (calcular-rpn (list '2 'INT '4 '<>) [:ejecucion-inmediata 0])



  
  :rcf)

(defn calcular-rpn [tokens nro-linea]
  (try
    (let [resu-redu
          (reduce
           (fn [pila token]
             (let [ari (aridad token),
                   resu (eliminar-cero-decimal
                         (case ari
                           1 (aplicar token (first pila) nro-linea)
                           2 (aplicar token (second pila) (first pila) nro-linea)
                           3 (aplicar token (nth pila 2) (nth pila 1) (nth pila 0) nro-linea)
                           token))]
               (if (nil? resu)
                 (reduced resu)
                 (cons resu (drop ari pila)))))
           [] tokens)]
      (if (> (count resu-redu) 1)
        (dar-error 16 nro-linea)  ; Syntax error
        (first resu-redu)))
    (catch NumberFormatException e 0)
    (catch ClassCastException e (dar-error 163 nro-linea)) ; Type mismatch error
    (catch UnsupportedOperationException e (dar-error 163 nro-linea)) ; Type mismatch error
    (catch IllegalArgumentException e (dar-error 69 nro-linea))  ; Overflow error
    (catch Exception e (dar-error 16 nro-linea)))  ; Syntax error
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; imprimir: recibe una lista de expresiones (separadas o no
; mediante puntos y comas o comas) y un ambiente, y las muestra
; interpretando los separadores como tabulaciones (las comas) o
; concatenaciones (los puntos y comas). Salvo cuando la lista
; termina en punto y coma, imprime un salto de linea al terminar
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; PREGUNTA RESPONDIDA: ejemplo de una lista de expresiones
;; expresión: todo lo que viene a la derecha de PRINT

(comment

  (imprimir '(X) [() [:ejecucion-inmediata 0] [] [] [] 0 {'X -05.50}])
  (imprimir '("HOLA") [() [:ejecucion-inmediata 0] [] [] [] 0 {}])
  (imprimir '("HOLA" + "CHAU") [() [:ejecucion-inmediata 0] [] [] [] 0 {}])
  
  
  :rcf)

(defn imprimir
  ([v]
   (let [expresiones (v 0), amb (v 1)]
     (cond
       (empty? expresiones) (do (prn) (flush) :sin-errores)
       (and (empty? (next expresiones)) (= (first expresiones) (list (symbol ";")))) (do (pr) (flush) :sin-errores)
       (and (empty? (next expresiones)) (= (first expresiones) (list (symbol ",t")))) (do (printf "\t\t") (flush) :sin-errores)
       (= (first expresiones) (list (symbol ";"))) (do (pr) (flush) (recur [(next expresiones) amb]))
       (= (first expresiones) (list (symbol ",t"))) (do (printf "\t\t") (flush) (recur [(next expresiones) amb]))
       :else (let [resu (eliminar-cero-entero (calcular-expresion (first expresiones) amb))]
               (if (nil? resu)
                 resu
                 (do (print resu) (flush) (recur [(next expresiones) amb])))))))
  ([lista-expr amb]
   (let [nueva (cons (conj [] (first lista-expr)) (rest lista-expr)),
         variable? #(or (variable-integer? %) (variable-float? %) (variable-string? %)),
         funcion? #(and (> (aridad %) 0) (not (operador? %))),
         interc (reduce #(if (and (or (number? (last %1)) (string? (last %1)) (variable? (last %1)) (= (symbol ")") (last %1)))
                                  (or (number? %2) (string? %2) (variable? %2) (funcion? %2) (= (symbol "(") %2)))
                           (conj (conj %1 (symbol ";")) %2) (conj %1 %2)) nueva),
         ex (partition-by #(= % (symbol ",t")) (desambiguar-comas interc)),
         expresiones (apply concat (map #(partition-by (fn [x] (= x (symbol ";"))) %) ex))]
     (imprimir [expresiones amb]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; desambiguar-comas: recibe una expresion en forma de lista y
; la devuelve con las comas que esten afuera de los pares de
; parentesis remplazadas por el simbolo ,t (las demas, que se
; usan para separar argumentos, se mantienen intactas)  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn desambiguar-comas
  ([lista-expr]
   (desambiguar-comas lista-expr 0 []))
  ([lista-expr cont-paren res]
   (if (nil? lista-expr)
     res
     (let [act (first lista-expr),
           paren (cond
                   (= act (symbol "(")) (inc cont-paren)
                   (= act (symbol ")")) (dec cont-paren)
                   :else cont-paren),
           nuevo (if (and (= act (symbol ",")) (zero? paren)) (symbol ",t") act)]
       (recur (next lista-expr) paren (conj res nuevo))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; A PARTIR DE ESTE PUNTO HAY QUE COMPLETAR LAS FUNCIONES DADAS ;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; evaluar: ejecuta una sentencia y retorna una dupla (un vector)
; con un resultado (usado luego por evaluar-linea) y un ambiente
; actualizado
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment

  (evaluar '(S$ = "") [() [:ejecucion-inmediata 0] [] [] [] 0 {}])
  (evaluar '(S = 3) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])

  (evaluar (list 'L '= 'LEN (symbol "(") 'N$ (symbol ")")) [() [:ejecucion-inmediata 0] [] [] [] 0 {'N$ "HOlA"}])

  ;; OK
  (evaluar (list 'PRINT 'MID$ (symbol "(") 'N$ (symbol ",") 'I (symbol ")")) [() [:ejecucion-inmediata 0] [] [] [] 0 {'N$ "HOLA", 'L 3, 'I 1}])
  ;; OK
  (imprimir (list 'MID$ (symbol "(") 'N$ (symbol ",") 'I (symbol ")")) [() [:ejecucion-inmediata 0] [] [] [] 0 {'N$ "HOLA", 'L 3, 'I 1}])

  ;; OK
  (evaluar (list 'LET 'N '= '1) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])
  (evaluar (list 'N '= '1) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])

  ;; tira undef statemenet pq no tengo definida la linea 20, pero deberia funcionar
  (evaluar (list 'IF 'A '<= '0 'OR 'B '<= '0 'OR 'INT (symbol "(") 'A (symbol ")") '<> 'A 'OR 'INT (symbol "(") 'B (symbol ")") '<> 'B 'THEN 'GOTO 20) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])
  ;; OK
  (calcular-expresion (list 'A '<= '0 'OR 'B '<= '0 'OR 'INT (symbol "(") 'A (symbol ")") '<> 'A 'OR 'INT (symbol "(") 'B (symbol ")") '<> 'B) [() [:ejecucion-inmediata 0] [] [] [] 0 {'A 2, 'B 4}])

  ;; OK
  (evaluar (list 'LET 'C '= 'A '- 'INT (symbol "(") 'A '/ 'B (symbol ")") '* 'B) [() [:ejecucion-inmediata 0] [] [] [] 0 {'A 4, 'B 2}])

  ;; OK
  (evaluar (list 'L '= 'ASC (symbol "(") 'MID$ (symbol "(") 'W$ (symbol ",") 'I (symbol ",") 1 (symbol ")") (symbol ")") '- 64) [() [:ejecucion-inmediata 0] [] [] [] 0 {'W$ "AMSTRONG", 'I 1}])

  ;; OJO: tengo que evaluar por separado pq el : no llega a evaluar linea
  (evaluar (list 'FOR 'J '= '1 'TO 'L (symbol ":") 'READ 'S$ (symbol ":") 'NEXT 'J) [() [10 0] [] [] ["ALFA"] 0 {'L 1}])

  ;; OK
  (evaluar (list 'READ 'S$) [() [10 0] [] [] ["ALFA", "ROMEO"] 0 {'L 4}])
  (evaluar (list 'READ 'S$) [() [10 0] [] [] ["ALFA", "ROMEO"] 1 {'L 4}])

  ;; OK
  (evaluar (list 'RESTORE) [() [10 0] [] [] ["ALFA", "ROMEO"] 1 {'L 1}])

  ;; TODO: por el momento lo dejo asi, pero podria hacer que agarre el array y lo meto en data-mem
  ;; ver si esto appendea o pisa lo anterior (seguro que appendea), ver si permite repetir, etc
  (evaluar (list 'DATA 'ALFA 'ROMEO) [() [10 0] [] [] [] 0 {}])

  ;; OK
  (evaluar (list 'FOR 'A '= 0 'TO 8 '* 'ATN (symbol "(") 1 (symbol ")") 'STEP 0.1) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])
  ;; es en calcular expresion con valor-final (8 * ATN (1))

  ;; OK, evaluo la parte del SIN que es lo que da el error
  (evaluar (list 'PRINT 'INT (symbol "(") 'SIN (symbol "(") 'A (symbol ")") '* 100000 (symbol ")") '/ 100000) [() [:ejecucion-inmediata 0] [] [] [] 0 {'A 1}])

  (evaluar (list 'GOSUB 200) [(list (list 'PRINT 10)) [:ejecucion-inmediata 0] [] [] [] 0 {'A 1}])

  (evaluar (list 'END) [(list (list 'PRINT 10) (list 'PRINT 20)) [10 1] [] [] [] 0 {'A 1}])
  
  (list 'S '+ 'INT (symbol "(") 0.1 '+ 'EXP (symbol "(") 'P '* 'LOG (symbol "(") 2 (symbol ")") (symbol ")") (symbol ")"))
  [() [:ejecucion-inmediata 0] [] [] [] 0 {'S 4 'P 1}]

  ;; S = S + INT (0.1 + EXP (P * LOG (2)))
  (evaluar (list 'S '= 'S '+ 'INT (symbol "(") 0.1 '+ 'EXP (symbol "(") 'P '* 'LOG (symbol "(") 2 (symbol ")") (symbol ")") (symbol ")")) [() [:ejecucion-inmediata 0] [] [] [] 0 {'S 1 'P 0}])
  
  (+ 1 (int (+ (Math/exp (* (Math/log 2) 0)) 0.1)))

  :rcf)

(defn evaluar [sentencia amb]
  (if (or (contains? (set sentencia) nil) (and (palabra-reservada? (first sentencia)) (= (second sentencia) '=)))
    (do (dar-error 16 (amb 1)) [nil amb])  ; Syntax error  
    (case (first sentencia)
      PRINT (let [args (next sentencia), resu (imprimir args amb)]
              (if (and (nil? resu) (some? args))
                [nil amb]
                [:sin-errores amb]))
      LOAD (if (= (first (amb 1)) :ejecucion-inmediata)
             (let [nuevo-amb (cargar-arch (apply str (next sentencia)) (amb 1))]
               (if (nil? nuevo-amb)
                 [nil amb]
                 [:sin-errores [nuevo-amb [:ejecucion-inmediata 0] [] [] [] 0 {}]]))  ; [(prog-mem)  [prog-ptrs]  [gosub-return-stack]  [for-next-stack]  [data-mem]  data-ptr  {var-mem}]
             (do (dar-error 200 (amb 1)) [nil amb]))  ; Load within program error
      SAVE (if (= (first (amb 1)) :ejecucion-inmediata)
             (let [resu (grabar-arch (apply str (next sentencia)) amb)]
               (if (nil? resu)
                 [nil amb]
                 [:sin-errores amb]))
             (do (dar-error 201 (amb 1)) [nil amb]))  ; Save within program error
      REM [:omitir-restante amb]
      NEW [:sin-errores ['() [:ejecucion-inmediata 0] [] [] [] 0 {}]]  ; [(prog-mem)  [prog-ptrs]  [gosub-return-stack]  [for-next-stack]  [data-mem]  data-ptr  {var-mem}]
      RUN (cond
            (empty? (amb 0)) [:sin-errores amb]  ; no hay programa
            (= (count sentencia) 1) (ejecutar-programa (assoc amb 1 [(ffirst (amb 0)) (count (expandir-nexts (nfirst (amb 0))))]))  ; no hay argumentos   
            (= (count (next sentencia)) 1) (ejecutar-programa (assoc amb 1 [(fnext sentencia) (contar-sentencias (fnext sentencia) amb)]))  ; hay solo un argumento
            :else (do (dar-error 16 (amb 1)) [nil amb]))  ; Syntax error
      GOTO (let [num-linea (if (some? (second sentencia)) (second sentencia) 0)]
             (if (not (contains? (into (hash-set) (map first (amb 0))) num-linea))
               (do (dar-error 90 (amb 1)) [nil amb])  ; Undef'd statement error
               (let [nuevo-amb (assoc amb 1 [num-linea (contar-sentencias num-linea amb)])]
                 (if (= (first (amb 1)) :ejecucion-inmediata)
                   (continuar-programa nuevo-amb)
                   [:omitir-restante nuevo-amb]))))
      IF (let [separados (split-with #(not (contains? #{"THEN" "GOTO"} (str %))) (next sentencia)),
               condicion-de-if (first separados),
               resto-if (second separados),
               sentencia-de-if (cond
                                 (= (first resto-if) 'GOTO) resto-if
                                 (= (first resto-if) 'THEN) (if (number? (second resto-if))
                                                              (cons 'GOTO (next resto-if))
                                                              (next resto-if))
                                 :else (do (dar-error 16 (amb 1)) nil)),  ; Syntax error
               resu (calcular-expresion condicion-de-if amb)]
           (if (zero? resu)
             [:omitir-restante amb]
             (recur sentencia-de-if amb)))
      INPUT (leer-con-enter (next sentencia) amb)
      ON (let [separados (split-with #(not (contains? #{"GOTO" "GOSUB"} (str %))) (next sentencia)),
               indice-de-on (calcular-expresion (first separados) amb),
               sentencia-de-on (first (second separados)),
               destino-de-on (seleccionar-destino-de-on (next (second separados)) indice-de-on amb)]
           (cond
             (nil? destino-de-on) [nil amb]
             (= destino-de-on :omitir-restante) [:sin-errores amb]
             :else (recur (list sentencia-de-on destino-de-on) amb)))
      GOSUB (let [num-linea (if (some? (second sentencia)) (second sentencia) 0)]
              (if (not (contains? (into (hash-set) (map first (amb 0))) num-linea))
                (do (dar-error 90 (amb 1)) [nil amb])  ; Undef'd statement error
                (let [pos-actual (amb 1),
                      nuevo-amb (assoc (assoc amb 1 [num-linea (contar-sentencias num-linea amb)]) 2 (conj (amb 2) pos-actual))]
                  (if (= (first (amb 1)) :ejecucion-inmediata)
                    (continuar-programa nuevo-amb)
                    [:omitir-restante nuevo-amb]))))
      RETURN (continuar-linea amb)
      FOR (let [separados (partition-by #(contains? #{"TO" "STEP"} (str %)) (next sentencia))]
            (if (not (or (and (= (count separados) 3) (variable-float? (ffirst separados)) (= (nth separados 1) '(TO)))
                         (and (= (count separados) 5) (variable-float? (ffirst separados)) (= (nth separados 1) '(TO)) (= (nth separados 3) '(STEP)))))
              (do (dar-error 16 (amb 1)) [nil amb])  ; Syntax error
              (let [valor-final (calcular-expresion (nth separados 2) amb),
                    valor-step (if (= (count separados) 5) (calcular-expresion (nth separados 4) amb) 1)]
                (if (or (nil? valor-final) (nil? valor-step))
                  [nil amb]
                  (recur (first separados) (assoc amb 3 (conj (amb 3) [(ffirst separados) valor-final valor-step (amb 1)])))))))
      NEXT (if (<= (count (next sentencia)) 1)
             (retornar-al-for amb (fnext sentencia))
             (do (dar-error 16 (amb 1)) [nil amb]))  ; Syntax error
      END (if (<= (count (next sentencia)) 0)
            [nil amb]
            ;; [:omitir-restante (assoc amb 1 [(first (last (nth amb (indice-amb :prog-mem)))) 0])]
            ;;(evaluar (list 'GOTO (first (last (nth amb (indice-amb :prog-mem))))) amb) 
            (do (dar-error 16 (amb 1)) [nil amb]))
      LET (if (= (second (rest sentencia)) '=)
             (let [resu (ejecutar-asignacion (rest sentencia) amb)]
               (if (nil? resu)
                 [nil amb]
                 [:sin-errores resu]))
             (do (dar-error 16 (amb 1)) [nil amb]))
      READ (if (>= (count (next sentencia)) 1)
             (leer-data (next sentencia) amb)
             (do (dar-error 16 (amb 1)) [nil amb]))
      RESTORE (if (= (count (next sentencia)) 0)
                [:sin-errores (assoc amb (indice-amb :data-ptr) 0)]
                (do (dar-error 16 (amb 1)) [nil amb]))
      (if (= (second sentencia) '=)
        (let [resu (ejecutar-asignacion sentencia amb)]
          (if (nil? resu)
            [nil amb]
            [:sin-errores resu]))
        (do (dar-error 16 (amb 1)) [nil amb]))))  ; Syntax error
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; aplicar: aplica un operador a sus operandos y retorna el valor
; resultante (si ocurre un error, muestra un mensaje y retorna
; nil)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment
  (aplicar '+ "HOLA" " MUNDO" [10 1])
  (aplicar '< 2 1 [10 1])
  (aplicar '< 0 1 [10 1])

  (aplicar 'AND 0 0 [10 1])
  
  (true? -1)
  (true? 0)

  (if (and (not= 1 0) (not= 1 0)) -1 0)
  
  (int (first (char-array "A")))

  (aplicar 'ASC "ABC" [])

  (aplicar 'SIN 1 [])
  
  (not= "hola" "hola")

  :rcf)

(defn aplicar
  ([operador operando nro-linea]
   (if (nil? operando)
     (dar-error 16 nro-linea)  ; Syntax error
     (case operador
       -u (- operando)
       LEN (count operando)
       STR$ (if (not (number? operando)) (dar-error 163 nro-linea) (eliminar-cero-entero operando)) ; Type mismatch error
       CHR$ (if (or (< operando 0) (> operando 255)) (dar-error 53 nro-linea) (str (char operando)))
       INT (int operando)
       ATN (Math/atan operando)
       SIN (Math/sin operando)
       EXP (Math/exp operando)
       LOG (Math/log operando)
       ASC (int (first (char-array operando)))))) ; Illegal quantity error  
  ([operador operando1 operando2 nro-linea]
   (if (or (nil? operando1) (nil? operando2))
     (dar-error 16 nro-linea)  ; Syntax error
     (case operador
       = (if (and (string? operando1) (string? operando2))
           (if (= operando1 operando2) -1 0)
           (if (= (+ 0 operando1) (+ 0 operando2)) -1 0))
       + (if (and (string? operando1) (string? operando2))
           (str operando1 operando2)
           (+ operando1 operando2))
       - (- operando1 operando2)
       * (* operando1 operando2)
       < (if (< (+ 0 operando1) (+ 0 operando2)) -1 0)
       > (if (> (+ 0 operando1) (+ 0 operando2)) -1 0)
       >= (if (>= (+ 0 operando1) (+ 0 operando2)) -1 0)
       <= (if (<= (+ 0 operando1) (+ 0 operando2)) -1 0)
       <> (if (and (string? operando1) (string? operando2))
            (if (not= operando1 operando2) -1 0)
            (if (not= (+ 0 operando1) (+ 0 operando2)) -1 0)) 
       / (if (= operando2 0) (dar-error 133 nro-linea) (/ operando1 operando2))  ; Division by zero error
       AND (let [op1 (+ 0 operando1), op2 (+ 0 operando2)] (if (and (not= op1 0) (not= op2 0)) -1 0))
       OR (let [op1 (+ 0 operando1), op2 (+ 0 operando2)] (if (or (= op1 -1) (= op2 -1)) -1 0))
       MID$ (if (< operando2 1)
              (dar-error 53 nro-linea)  ; Illegal quantity error
              (let [ini (dec operando2)] (if (>= ini (count operando1)) "" (subs operando1 ini)))))))
  ([operador operando1 operando2 operando3 nro-linea]
   (if (or (nil? operando1) (nil? operando2) (nil? operando3)) (dar-error 16 nro-linea)  ; Syntax error
       (case operador
         MID3$ (let [tam (count operando1), ini (dec operando2), fin (+ (dec operando2) operando3)]
                 (cond
                   (or (< operando2 1) (< operando3 0)) (dar-error 53 nro-linea)  ; Illegal quantity error
                   (>= ini tam) ""
                   (>= fin tam) (subs operando1 ini tam)
                   :else (subs operando1 ini fin)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; A PARTIR DE ESTE PUNTO HAY QUE IMPLEMENTAR LAS FUNCIONES DADAS ;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; palabra-reservada?: predicado para determinar si un
; identificador es una palabra reservada, por ejemplo:
; user=> (palabra-reservada? 'REM)
; true
; user=> (palabra-reservada? 'SPACE)
; false
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment

  (palabra-reservada? 'REM)
  (palabra-reservada? 'EXIT)
  (palabra-reservada? 'CLEAR)
  (palabra-reservada? 'RUN)
  (palabra-reservada? 'SPACE)
  (palabra-reservada? 'RUn)
  (palabra-reservada? 'PRINT)
  (palabra-reservada? 'END)

  :rcf)

(defn palabra-reservada? [x]
  (not (empty? (re-seq #"EXIT|ENV|DATA|REM|NEW|CLEAR|LIST|RUN|LOAD|SAVE|LET|AND|OR|NOT|ABS|SGN|INT|SQR|SIN|COS|TAN|ATN|EXP|LOG|LEN|LEFT\$|MID\$|RIGHT\$|STR\$|VAL|CHR\$|ASC|GOTO|ON|IF|THEN|FOR|TO|STEP|NEXT|GOSUB|RETURN|END|INPUT|READ|RESTORE|PRINT" (str x)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; operador?: predicado para determinar si un identificador es un
; operador, por ejemplo:
; user=> (operador? '+)
; true
; user=> (operador? (symbol "+"))
; true
; user=> (operador? (symbol "%"))
; false
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; PREGUNTA: como hago regex de phi?
;; RESPUESTA: No tenemos phi y tampoco la potencia es decir la flechita para arriba

(comment

  (operador? '+)
  (operador? '-)
  (operador? '*)
  (operador? '/)
  (operador? (symbol "+"))
  (operador? (symbol "++"))
  (operador? (symbol "^"))
  (operador? (symbol "%"))

  (operador? '>=)
  (operador? '>)
  (operador? 'AND)

  :rcf)

(defn operador? [x]
  (not (nil? (re-matches #"\+|\-|\*|\/|\^|\<|\=|\>|\<\=|\>\=|\<\>|AND|OR|NOT" (str x)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; anular-invalidos: recibe una lista de simbolos y la retorna con
; aquellos que son invalidos reemplazados por nil, por ejemplo:
; user=> (anular-invalidos '(IF X & * Y < 12 THEN LET ! X = 0))
; (IF X nil * Y < 12 THEN LET nil X = 0)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; PREGUNTA RESPONDIDA: lista de simbolos equivale a expresión??
;; numero, variable, string, palabra-reservada son simbolos validos lo que no entra, es invalido

;; TODO: ver de integrar las funcioens variable-string? float? integer?

(comment

  (re-seq #"\;|\=|\+|\-|\*|\/|\^" "a")

  (anular-invalido '+)
  (anular-invalido 'Y)
  (anular-invalido '&)

  (anular-invalidos '(IF X & * Y < 12 THEN LET ! X = 0))
  (anular-invalidos '(X$ = ""))
  (anular-invalidos '(X$ = "HOLA"))
  (anular-invalidos '(L = LEN (symbol "(") N$ (symbol ")")))
  (anular-invalidos (list 'PRINT 'MID$ (symbol "(") 'N$ (symbol ",") 'I (symbol ")")))
  (anular-invalidos (list 'PRINT "ENTER A" (symbol ":") 'INPUT 'A (symbol ":") 'PRINT "ENTER B" (symbol ":") 'INPUT 'B))
  
  (anular-invalidos (list 'LET 'P '= '.))
  (anular-invalidos (list 'IF 'P '= 1 'THEN 'PRINT 'X (symbol ";") " " (symbol ";")))

  (anular-invalidos (list 'IF 'L '< 1 'OR 'L '> 26 'THEN 'PRINT "??? " (symbol ";") (symbol ":") 'GOTO 190))
  :rcf)

(defn anular-invalido [simbolo]
  (cond 
    (palabra-reservada? simbolo) simbolo
    (string? simbolo) simbolo
    (empty? (re-seq #"\;|\=|\+|\-|\*|\/|\^|\<|\>|[A-Z]|[0-9]|^$|^\s+$|\(|\)|\,|\:|\." (str simbolo))) nil
    :else simbolo))

(defn anular-invalidos [sentencia]
  (map anular-invalido sentencia))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; cargar-linea: recibe una linea de codigo y un ambiente y retorna
; el ambiente actualizado, por ejemplo:
; user=> (cargar-linea '(10 (PRINT X)) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])
; [((10 (PRINT X))) [:ejecucion-inmediata 0] [] [] [] 0 {}]
; user=> (cargar-linea '(20 (X = 100)) ['((10 (PRINT X))) [:ejecucion-inmediata 0] [] [] [] 0 {}])
; [((10 (PRINT X)) (20 (X = 100))) [:ejecucion-inmediata 0] [] [] [] 0 {}]
; user=> (cargar-linea '(15 (X = X + 1)) ['((10 (PRINT X)) (20 (X = 100))) [:ejecucion-inmediata 0] [] [] [] 0 {}])
; [((10 (PRINT X)) (15 (X = X + 1)) (20 (X = 100))) [:ejecucion-inmediata 0] [] [] [] 0 {}]
; user=> (cargar-linea '(15 (X = X - 1)) ['((10 (PRINT X)) (15 (X = X + 1)) (20 (X = 100))) [:ejecucion-inmediata 0] [] [] [] 0 {}])
; [((10 (PRINT X)) (15 (X = X - 1)) (20 (X = 100))) [:ejecucion-inmediata 0] [] [] [] 0 {}]
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



(defn iguales? [nro-nueva-linea linea-amb]
  (= (first linea-amb) nro-nueva-linea))

(defn cargar-linea [linea amb]
  (let
   [lineas-amb (nth amb 0)]
    (assoc amb 0 (sort-by first (concat (remove (partial iguales? (first linea)) lineas-amb) (list linea))))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; expandir-nexts: recibe una lista de sentencias y la devuelve con
; las sentencias NEXT compuestas expresadas como sentencias NEXT
; simples, por ejemplo:
; user=> (def n (list '(PRINT 1) (list 'NEXT 'A (symbol ",") 'B)))
; #'user/n
; user=> n
; ((PRINT 1) (NEXT A , B))
; user=> (expandir-nexts n)
; ((PRINT 1) (NEXT A) (NEXT B))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (def n (list '(PRINT 1) (list 'NEXT 'A (symbol ",") 'B)))

  (def sentencia (list 'NEXT 'A (symbol ",") 'B))

  sentencia

  (expandir-nexts (list '(PRINT 1) (list 'NEXT 'A (symbol ",") 'B)))
  (expandir-nexts (list '(PRINT X + 10) (list 'NEXT 'A (symbol ",") 'B) (list 'NEXT 'C (symbol ",") 'D)))
  (expandir-nexts (list '(PRINT X + 10) (list 'NEXT 'A)))
  
  (expandir-nexts (list '(LET S = S + 10)))

  (expandir-nexts (list (list 'IF 'N '< 1 'THEN 'GOTO 90)))

  ;; borra el next si esta solo
  (expandir-nexts (list (list 'NEXT)))
  
  
  :rcf)

(defn in?
  "true if coll contains elm"
  [coll elm]
  (some #(= elm %) coll))

(defn crear-lista-con-next [x]
  (list 'NEXT x))

(defn no-es-coma? [x]
  (not (= (symbol ",") x)))

(defn expandir-sentencia-con-next [n]
  (map crear-lista-con-next (filter no-es-coma? n)))

(defn expandir-nexts [n]
  (cond
    (empty? n) n
    (and (= 'NEXT (first (first n))) (= (count (first n)) 1)) n
    (= 'NEXT (first (first n))) (concat (expandir-sentencia-con-next (rest (first n))) (expandir-nexts (rest n)))
    :else (concat (list (first n)) (expandir-nexts (rest n)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; dar-error: recibe un error (codigo o mensaje) y el puntero de 
; programa, muestra el error correspondiente y retorna nil, por
; ejemplo:
; user=> (dar-error 16 [:ejecucion-inmediata 4])
;
; ?SYNTAX  ERRORnil
; user=> (dar-error "?ERROR DISK FULL" [:ejecucion-inmediata 4])
;
; ?ERROR DISK FULLnil
; user=> (dar-error 16 [100 3])
;
; ?SYNTAX  ERROR IN 100nil
; user=> (dar-error "?ERROR DISK FULL" [100 3])
;
; ?ERROR DISK FULL IN 100nil
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment

  (dar-error 16 [:ejecucion-inmediata 4])
  (dar-error "?ERROR DISK FULL" [:ejecucion-inmediata 4])
  (dar-error 16 [100 3])
  (dar-error "?ERROR DISK FULL" [100 3])

  :rcf)

(defn dar-error-aux [prog-ptrs]
  (if (number? (first prog-ptrs)) (str " IN " (first prog-ptrs)) nil)
  )

(defn dar-error [cod prog-ptrs]
  (let [nro-linea (dar-error-aux prog-ptrs)]
    (cond
      (string? cod) (print (str cod nro-linea))
      :else (print (str (buscar-mensaje cod) nro-linea)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; variable-float?: predicado para determinar si un identificador
; es una variable de punto flotante, por ejemplo:
; user=> (variable-float? 'X)
; true
; user=> (variable-float? 'X%)
; false
; user=> (variable-float? 'X$)
; false
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment

  (re-matches #".*[A-Z0-9]$" (str 'X))
  (re-matches #".*[A-Z0-9]$" (str 'X1))
  (not (nil? (re-matches #".*[A-Z0-9]$" (str '1))))
  (re-matches #".*[A-Z0-9]$" (str 'X%))
  (re-matches #".*[A-Z0-9]$" (str 'X$))

  (variable-float? 'X)
  (variable-float? 'X1)
  (variable-float? 1)
  (variable-float? 'X%)
  (variable-float? 'X$)
  
  (variable-float? "HOLA")

  :rcf)

(defn empieza-con-letra? [x]
  (re-matches #"^[A-Z].*" (str x)))

(defn variable-float? [x]
  (cond
    (palabra-reservada? x) false
    (and (symbol? x) (not (nil? (empieza-con-letra? x)))) (not (nil? (re-matches #".*[A-Z0-9]$" (str x))))
    :else false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; variable-integer?: predicado para determinar si un identificador
; es una variable entera, por ejemplo:
; user=> (variable-integer? 'X%)
; true
; user=> (variable-integer? 'X)
; false
; user=> (variable-integer? 'X$)
; false
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment

  (palabra-reservada? 'X%)
  (palabra-reservada? 'X)
  (palabra-reservada? 'X$)

  (first "hola")
  (if (re-matches #"^[A-Z].*" (str 'X%)) "si" "no")
  (re-matches #"^[A-Z].*" (str (symbol "1%")))

  (re-matches #".*%$" (str 'X%))
  (re-matches #".*%$" (str 'X$))
  (re-matches #".*%$" (str 'X))


  (and (not (nil? (empieza-con-letra? 'X%))) (not (nil? (re-matches #".*%$" (str 'X%)))))

  (not (nil? (empieza-con-letra? 1)))
  (not (nil? (re-matches #".*%$" (str 1))))

  (variable-integer? 'X%)
  (variable-integer? 'X)
  (variable-integer? '$)
  (variable-integer? 'REM)
  (variable-integer? 1)
  (variable-integer? '<)

  (nil? true)
  (nil? false)

  :rcf)



(defn variable-integer? [x]
  (cond
    (palabra-reservada? x) false
    (not (nil? (empieza-con-letra? x))) (not (nil? (re-matches #".*%$" (str x))))
    :else false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; variable-string?: predicado para determinar si un identificador
; es una variable de cadena, por ejemplo:
; user=> (variable-string? 'X$)
; true
; user=> (variable-string? 'X)
; false
; user=> (variable-string? 'X%)
; false
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment
  (variable-string? 'X%)
  (variable-string? 'X)
  (variable-string? 'X$)
  (variable-string? 'REM)
  (variable-string? 1)

  :rcf)

(defn variable-string? [x]
  (cond
    (palabra-reservada? x) false
    (not (nil? (empieza-con-letra? x))) (not (nil? (re-matches #".*\$$" (str x))))
    :else false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; contar-sentencias: recibe un numero de linea y un ambiente y
; retorna la cantidad de sentencias que hay en la linea indicada,
; por ejemplo:
; user=> (contar-sentencias 10 [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [10 1] [] [] [] 0 {}])
; 2
; user=> (contar-sentencias 15 [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [10 1] [] [] [] 0 {}])
; 1
; user=> (contar-sentencias 20 [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [10 1] [] [] [] 0 {}])
; 2
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment


  (def amb [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [10 1] [] [] [] 0 {}])



  (count (expandir-nexts (next (first (filter (partial iguales? 20) (nth amb 0))))))

  (contar-sentencias 10 amb)
  (contar-sentencias 15 amb)
  (contar-sentencias 20 amb)

  :rcf)

(defn contar-sentencias [nro-linea amb]
  (count (expandir-nexts (next (first (filter (partial iguales? nro-linea) (nth amb 0)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; buscar-lineas-restantes: recibe un ambiente y retorna la
; representacion intermedia del programa a partir del puntero de
; programa (que indica la linea y cuantas sentencias de la misma
; aun quedan por ejecutar), por ejemplo:
; user=> (buscar-lineas-restantes [() [:ejecucion-inmediata 0] [] [] [] 0 {}])
; nil
; user=> (buscar-lineas-restantes ['((PRINT X) (PRINT Y)) [:ejecucion-inmediata 2] [] [] [] 0 {}])
; nil
; user=> (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [10 2] [] [] [] 0 {}])
; ((10 (PRINT X) (PRINT Y)) (15 (X = X + 1)) (20 (NEXT I , J)))
; user=> (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [10 1] [] [] [] 0 {}])
; ((10 (PRINT Y)) (15 (X = X + 1)) (20 (NEXT I , J)))
; user=> (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [10 0] [] [] [] 0 {}])
; ((10) (15 (X = X + 1)) (20 (NEXT I , J)))
; user=> (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [15 1] [] [] [] 0 {}])
; ((15 (X = X + 1)) (20 (NEXT I , J)))
; user=> (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [15 0] [] [] [] 0 {}])
; ((15) (20 (NEXT I , J)))
; user=> (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 3] [] [] [] 0 {}])
; ((20 (NEXT I) (NEXT J)))
; user=> (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 2] [] [] [] 0 {}])
; ((20 (NEXT I) (NEXT J)))
; user=> (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 1] [] [] [] 0 {}])
; ((20 (NEXT J)))
; user=> (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 0] [] [] [] 0 {}])
; ((20))
; user=> (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 -1] [] [] [] 0 {}])
; ((20))
; user=> (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [25 0] [] [] [] 0 {}])
; nil
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment

  (def _amb [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [10 2] [] [] [] 0 {}])

  ; obtener sentencia a partir de linea
  (def _lineas (list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))))

  (remover-lineas-hasta 10  _lineas)
  (remover-lineas-hasta 15  _lineas)
  (remover-lineas-hasta 20  _lineas)
  (remover-lineas-hasta 25  _lineas)

  (obtener-nro-linea [() [:ejecucion-inmediata 0] [] [] [] 0 {}])
  (obtener-cant-sentencias-restantes [() [:ejecucion-inmediata 0] [] [] [] 0 {}])

  (buscar-lineas-restantes [() [:ejecucion-inmediata 0] [] [] [] 0 {}])

  (expandir-nexts (rest (nth _lineas 2)))

  (obtener-lineas-amb _amb)
  (obtener-nro-linea _amb)
  (obtener-cant-sentencias-restantes _amb)

  (buscar-lineas-restantes [() [:ejecucion-inmediata 0] [] [] [] 0 {}])
  (buscar-lineas-restantes ['((PRINT X) (PRINT Y)) [:ejecucion-inmediata 2] [] [] [] 0 {}])
  (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [10 2] [] [] [] 0 {}])
  (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [10 1] [] [] [] 0 {}])
  (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [10 0] [] [] [] 0 {}])

  (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [15 1] [] [] [] 0 {}])
  (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [15 0] [] [] [] 0 {}])
  (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 3] [] [] [] 0 {}])
  (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 2] [] [] [] 0 {}])

  (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 1] [] [] [] 0 {}])

  (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 0] [] [] [] 0 {}])
  (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 -1] [] [] [] 0 {}])
  (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [25 0] [] [] [] 0 {}])

  :rcf)

(defn obtener-nro-linea [amb]
  (first (nth amb 1)))

(defn obtener-cant-sentencias-restantes [amb]
  (second (nth amb 1)))

(defn obtener-lineas-amb [amb]
  (nth amb 0))

(defn seleccionar-sentencias [cant-sentencias-restantes, linea]
  (cons (first linea) (reverse (take (if (neg? cant-sentencias-restantes) 0 cant-sentencias-restantes)  (reverse (expandir-nexts (rest linea)))))))

(defn remover-lineas-hasta [nro-linea lineas-amb]
  (cond
    (empty? lineas-amb) lineas-amb
    (= nro-linea (first (first lineas-amb))) lineas-amb
    :else (remover-lineas-hasta nro-linea (rest lineas-amb))))

(defn buscar-lineas-restantes [amb]
  (let
   [puntero_linea (obtener-nro-linea amb),
    lineas-amb (obtener-lineas-amb amb),
    lineas-restantes (remover-lineas-hasta puntero_linea lineas-amb)]
    (cond
      (not (number? puntero_linea)) nil
      (empty? lineas-restantes) nil
      :else (cons (seleccionar-sentencias (obtener-cant-sentencias-restantes amb) (first lineas-restantes)) (rest lineas-restantes)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; continuar-linea: implementa la sentencia RETURN, retornando una
; dupla (un vector) con un resultado (usado luego por
; evaluar-linea) y un ambiente actualizado con el nuevo valor del
; puntero de programa, por ejemplo:
; user=> (continuar-linea [(list '(10 (PRINT X)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 3] [] [] [] 0 {}])
; 
; ?RETURN WITHOUT GOSUB ERROR IN 20[nil [((10 (PRINT X)) (15 (X = X + 1)) (20 (NEXT I , J))) [20 3] [] [] [] 0 {}]]
; user=> (continuar-linea [(list '(10 (PRINT X)) '(15 (GOSUB 100) (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 3] [[15 2]] [] [] 0 {}])
; [:omitir-restante [((10 (PRINT X)) (15 (GOSUB 100) (X = X + 1)) (20 (NEXT I , J))) [15 1] [] [] [] 0 {}]]
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment

  (continuar-linea [(list '(10 (PRINT X)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 3] [] [] [] 0 {}])

  (continuar-linea [(list '(10 (PRINT X)) '(15 (GOSUB 100) (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 3] [[15 2]] [] [] 0 {}])

  (def amb [(list '(10 (PRINT X)) '(15 (GOSUB 100) (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 3] [[15 2]] [] [] 0 {}])
  
  ;; [(prog-mem)  [prog-ptrs]  [gosub-return-stack]  [for-next-stack]  [data-mem]  data-ptr  {var-mem}]
  
  (assoc amb (indice-amb :prog-ptrs) (get-amb-gosub-return-stack amb))

  (get-amb-gosub-return-stack [(list '(10 (PRINT X)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 3] [] [] [] 0 {}])

  :rcf)


(defn get-amb-gosub-return-stack
  "devuelve un prog-ptrs"
  [amb] 
  (first (nth amb (indice-amb :gosub-return-stack)))
  )

(defn reducir-sentencias-restantes [gosub-return-stack]
  (let [sentencias-restantes (dec (nth gosub-return-stack 1))]
  (assoc gosub-return-stack 1 (if (neg? sentencias-restantes) 0 sentencias-restantes))
  ))

(defn continuar-linea-aux [amb]
  (cond
    (nil? (get-amb-gosub-return-stack amb)) nil
    :else (assoc (assoc amb (indice-amb :prog-ptrs) (reducir-sentencias-restantes (get-amb-gosub-return-stack amb))) (indice-amb :gosub-return-stack) '[])
    )
  
  )

(defn continuar-linea [amb]
  (let [resu (continuar-linea-aux amb)]
    (if (nil? resu)
      [(dar-error 22 (get-amb-prog-ptrs amb)) amb]
      [:omitir-restante resu]))
  
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; extraer-data: recibe la representación intermedia de un programa
; y retorna una lista con todos los valores embebidos en las
; sentencias DATA, por ejemplo:
; user=> (extraer-data '(()))
; ()
; user=> (extraer-data (list '(10 (PRINT X) (REM ESTE NO) (DATA 30)) '(20 (DATA HOLA)) (list 100 (list 'DATA 'MUNDO (symbol ",") 10 (symbol ",") 20))))
; ("HOLA" "MUNDO" 10 20)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn stringify [x]
  (if (number? x) x (str x)))

(defn data? [x]
  (= 'DATA x))

(defn extraer-data-sentencia [sentencia]
  (map stringify (filter #(not (data? %1)) (filter no-es-coma? sentencia))))

(defn extraer-data-linea [linea]
  (cond
    (empty? linea) linea
    (= 'REM (first (first linea))) '()
    (= 'DATA (first (first linea))) (cons (extraer-data-sentencia (first linea)) (extraer-data-linea (rest linea)))
    :else (extraer-data-linea (rest linea))))

(defn extraer-data [prg]
  (flatten (map #(extraer-data-linea (rest %1)) prg)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; ejecutar-asignacion: recibe una asignacion y un ambiente, y
; retorna el ambiente actualizado al efectuar la asignacion, por
; ejemplo:
; user=> (ejecutar-asignacion '(X = 5) ['((10 (PRINT X))) [10 1] [] [] [] 0 {}])
; [((10 (PRINT X))) [10 1] [] [] [] 0 {X 5}]
; user=> (ejecutar-asignacion '(X = 5) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X 2}])
; [((10 (PRINT X))) [10 1] [] [] [] 0 {X 5}]
; user=> (ejecutar-asignacion '(X = X + 1) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X 2}])
; [((10 (PRINT X))) [10 1] [] [] [] 0 {X 3}]
; user=> (ejecutar-asignacion '(X$ = X$ + " MUNDO") ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X$ "HOLA"}])
; [((10 (PRINT X))) [10 1] [] [] [] 0 {X$ "HOLA MUNDO"}]
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment

  (first '(X = X + 1))
  (drop 2 '(X = X + 1))
  (drop 2 '(X$ = X$ + " MUNDO"))

  (ejecutar-asignacion '(X = 5) ['((10 (PRINT X))) [10 1] [] [] [] 0 {}])
  (ejecutar-asignacion '(X = 5) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X 2}])
  (ejecutar-asignacion '(X = X + 1) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X 2}])
  (ejecutar-asignacion '(X$ = X$ + " MUNDO") ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X$ "HOLA"}])

  (ejecutar-asignacion (list 'L '= 'LEN (symbol "(") 'N$ (symbol ")")) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{N$ "HOLA"}]) 
  
  ;; OK
  (ejecutar-asignacion (list 'C '= 'A '- 'INT (symbol "(") 'A '/ 'B (symbol ")") '* 'B) [() [:ejecucion-inmediata 0] [] [] [] 0 {'A 4, 'B 2}])
  
  ;; MID3$ funciona
  (ejecutar-asignacion (list 'L '= 'MID$ (symbol "(") 'W$ (symbol ",") 'I (symbol ",") 1 (symbol ")")) [() [:ejecucion-inmediata 0] [] [] [] 0 {'W$ "tomato", 'I 1}])
  
  :rcf)




(indice-amb :hash-map)

(defn ejecutar-asignacion [sentencia amb]
  (let [variable (first sentencia)
        expresion (drop 2 sentencia)
        calculo-expresion (calcular-expresion expresion amb)]
    (assoc amb (indice-amb :hash-map) (assoc (get-hash-map-amb amb) variable calculo-expresion))
    ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; preprocesar-expresion: recibe una expresion y la retorna con
; las variables reemplazadas por sus valores y el punto por el
; cero, por ejemplo:
; user=> (preprocesar-expresion '(X$ + " MUNDO" + Z$) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X$ "HOLA"}])
; ("HOLA" + " MUNDO" + "")
; user=> (preprocesar-expresion '(X + . / Y% * Z) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X 5 Y% 2}])
; (5 + 0 / 2 * 0)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment

  (preprocesar-expresion '(X$ + " MUNDO" + Z$) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X$ "HOLA"}])
  (preprocesar-expresion '(X + . / Y% * Z) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X 5 Y% 2}]) 
  (preprocesar-expresion '("HOLA") ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X$ "HOLA"}])
  
  (variable-string? "HOLA")
  (variable-integer? "HOLA")
  (variable-float? "HOLA")

  (preprocesar-expresion '(LEN (N$)) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{N$ "HOLA"}])
  
  (first (second '(LEN (N$))))
  
  :rcf)



(defn obtener-variable-amb [valor-variable simbolo]
  (cond
    (and (nil? valor-variable) (or (variable-float? simbolo) (variable-integer? simbolo))) 0
    (not (nil? valor-variable)) valor-variable
    :else ""))

(defn preprocesar-expresion-aux [hm-amb simbolo]
  (let
   [es-entero (variable-integer? simbolo),
    es-float (variable-float? simbolo),
    es-string (variable-string? simbolo)
    valor-variable (hm-amb simbolo)]
    (cond
      (= simbolo '.) 0
      (or es-entero es-float es-string) (obtener-variable-amb valor-variable simbolo)
      :else simbolo)))

(defn preprocesar-expresion [expr amb]
  (map (partial preprocesar-expresion-aux (get-hash-map-amb amb)) expr))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; desambiguar: recibe un expresion y la retorna sin los + unarios,
; con los - unarios reemplazados por -u y los MID$ ternarios
; reemplazados por MID3$, por ejemplo: 
; user=> (desambiguar (list '- 2 '* (symbol "(") '- 3 '+ 5 '- (symbol "(") '+ 2 '/ 7 (symbol ")") (symbol ")")))
; (-u 2 * ( -u 3 + 5 - ( 2 / 7 ) ))
; user=> (desambiguar (list 'MID$ (symbol "(") 1 (symbol ",") 2 (symbol ")")))
; (MID$ ( 1 , 2 ))
; user=> (desambiguar (list 'MID$ (symbol "(") 1 (symbol ",") 2 (symbol ",") 3 (symbol ")")))
; (MID3$ ( 1 , 2 , 3 ))
; user=> (desambiguar (list 'MID$ (symbol "(") 1 (symbol ",") '- 2 '+ 'K (symbol ",") 3 (symbol ")")))
; (MID3$ ( 1 , -u 2 + K , 3 ))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment


  (list '- 2 '* (symbol "(") '- 3 '+ 5 '- (symbol "(") '+ 2 '/ 7 (symbol ")") (symbol ")"))
  ;; ( - 2 * (- 3 + 5 - ( + 2 / 7)) )
  ;; (-u 2 * (-u 3 + 5 - (2 / 7)))

  (count (list '- 2 '* (symbol "(") '- 3 '+ 5 '- (symbol "(") '+ 2 '/ 7 (symbol ")") (symbol ")")))
  (desambiguar-mas-menos (list '- 2 '* (symbol "(") '- 3 '+ 5 '- (symbol "(") '+ 2 '/ 7 (symbol ")") (symbol ")")))

  (desambiguar (list '- 2 '* (symbol "(") '- 3 '+ 5 '- (symbol "(") '+ 2 '/ 7 (symbol ")") (symbol ")")))
  (desambiguar (list 'MID$ (symbol "(") 1 (symbol ",") 2 (symbol ")")))
  (desambiguar (list 'MID$ (symbol "(") 1 (symbol ",") 2 (symbol ",") 3 (symbol ")")))
  (desambiguar (list 'MID$ (symbol "(") 1 (symbol ",") '- 2 '+ 'K (symbol ",") 3 (symbol ")")))
  (desambiguar (list "HOLA" '+ "MUNDO"))

  (= 'x (symbol "x"))

  :rcf)

(defn desambiguar [expr]
  (desambiguar-mid (desambiguar-mas-menos expr)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; precedencia: recibe un token y retorna el valor de su
; precedencia, por ejemplo:
; user=> (precedencia 'OR)
; 1
; user=> (precedencia 'AND)
; 2
; user=> (precedencia '*)
; 6
; user=> (precedencia '-u)
; 7
; user=> (precedencia 'MID$)
; 8
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; PREGUNTA: pq MID tiene 8, es un por defecto?? todo lo que no sea un operador tiene 8??
;; RESPUESTA: Si

(comment
  (precedencia 'OR)
  (precedencia 'AND)
  (precedencia '*)
  (precedencia '-u)
  (precedencia 'MID$)
  ;; TODO: quitar los  ^ pq no se usan en este tp
  (precedencia (symbol "^"))

  :rcf)

;; TODO: a LEN le puse una precedencia de 7 para que este antes de los strings que tienen 8
(defn precedencia [token]
  (cond
    (= token (symbol ",")) 0
    :else (case token
            OR 1
            AND 2
            NOT 3
            > 4
            < 4
            = 4
            <= 4
            >= 4
            <> 4
            + 5
            - 5
            * 6
            / 6 
            -u 7
            STR$ 8
            CHR$ 8
            LEN 8 
            MID$ 8
            MID3$ 8
            EXP 8
            LOG 8
            ASC 8
            INT 8
            ATN 8
            SIN 8
            nil)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; aridad: recibe un token y retorna el valor de su aridad, por
; ejemplo:
; user=> (aridad 'THEN)
; 0
; user=> (aridad 'SIN)
; 1
; user=> (aridad '*)
; 2
; user=> (aridad 'MID$)
; 2
; user=> (aridad 'MID3$)
; 3
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment
  (aridad 'INPUT)

  (apply str (drop-last (str 'INPUT)))

  (aridad 'ATN)
  (aridad '+)
  (aridad '/)
  (aridad 'AND)
  (aridad '<)
  (aridad '<=)
  (aridad (symbol "STR$"))
  (aridad (symbol "MID$"))
  (aridad (symbol "MID3$"))
  (aridad 'THEN)
  (aridad 'LET/=)

  (apply str (drop-last (str 'ATN)))

  (re-matches #".*[^0-9]\$$" (str (symbol "MID$")))
  (re-matches #"MID[0-9]\$$" (str (symbol "MID$")))
  (re-matches #"MID[0-9]\$$" (str (symbol "MID3$")))
  (re-matches #".*[0-9]\$$" (str (symbol "MID3$")))


  :rcf)

;; PREGUNTA RESPONDIDA: como se la aridad? que es MID3$?
;; ¿que implica un token?

;; todo lo que no sea un operador o funcion tiene aridad 0, es decir las sentencias
;; aridad -> token es lo que te da la funcion de strings-a-tokens

(defn aridad [token]
  (case token
    MID3$ 3
    MID$ 2
    CHR$ 1
    STR$ 1
    ATN 1
    INT 1
    SIN 1
    EXP 1
    LOG 1
    LEN 1
    ASC 1
    + 2
    -u 1
    - 2
    * 2
    / 2
    = 2
    < 2
    > 2
    <= 2
    >= 2
    <> 2
    AND 2
    OR 2
    0))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; eliminar-cero-decimal: recibe un numero y lo retorna sin ceros
; decimales no significativos, por ejemplo: 
; user=> (eliminar-cero-decimal 1.5)
; 1.5
; user=> (eliminar-cero-decimal 1.50)
; 1.5
; user=> (eliminar-cero-decimal 1.0)
; 1
; user=> (eliminar-cero-decimal 'A)
; A
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment

  (eliminar-cero-decimal-aux 1.50)
  (eliminar-cero-decimal-aux 1.550)

  (println 1.50)

  ;; cuando no tiene decimales significativos lo tengo que castear a entero


  (and (float? 1.0) (str/ends-with? (str 1.0) ".0"))

  (str/ends-with? (str 1.0000) ".0")

  (eliminar-cero-decimal 1.5)
  (eliminar-cero-decimal 1.50)
  (eliminar-cero-decimal 1.504)
  (eliminar-cero-decimal 1.5040)
  (eliminar-cero-decimal 1.0)
  (eliminar-cero-decimal 10.0000)
  (eliminar-cero-decimal 'A)
  (eliminar-cero-decimal "HOLA")
  
  ;; NO deberia recibir un booleano nunca
  (eliminar-cero-decimal false)

  :rcf)

(defn eliminar-cero-decimal-aux [n]
  (cond
    (and (float? n) (str/ends-with? (str n) ".0")) (int n)
    :else n))

(defn eliminar-cero-decimal [n]
  (cond
    (string? n) n
    (symbol? n) n
    (number? n) (eliminar-cero-decimal-aux n)
    :else nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; eliminar-cero-entero: recibe un simbolo y lo retorna convertido
; en cadena, omitiendo para los numeros del intervalo (-1..1) el
; cero a la izquierda del punto, por ejemplo:
; user=> (eliminar-cero-entero nil)
; nil
; user=> (eliminar-cero-entero 'A)
; "A"
; user=> (eliminar-cero-entero 0)
; " 0"
; user=> (eliminar-cero-entero 1.5)
; " 1.5"
; user=> (eliminar-cero-entero 1)
; " 1"
; user=> (eliminar-cero-entero -1)
; "-1"
; user=> (eliminar-cero-entero -1.5)
; "-1.5"
; user=> (eliminar-cero-entero 0.5)
; " .5"
; user=> (eliminar-cero-entero -0.5)
; "-.5"
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment

  (str 'A)
  (str 0)

  (symbol? 'A)
  (symbol? 0)
  (number? 0)
  (number? -1)
  (number? -1.5)
  (str " " -1.5)



  (eliminar-cero-entero-aux -0.5)
  (eliminar-cero-entero-aux 0.5)
  (eliminar-cero-entero-aux 0)


  (eliminar-cero-entero "HOLA")
  (eliminar-cero-entero 'A)
  (eliminar-cero-entero 0)
  (eliminar-cero-entero 1.5)
  (eliminar-cero-entero 1)
  (eliminar-cero-entero -1)
  (eliminar-cero-entero -1.5)
  (eliminar-cero-entero 0.5)
  (eliminar-cero-entero -0.5)

  :rcf)

(defn eliminar-cero-entero-aux [n]
  (cond
    (not (empty? (re-seq #"^-0." (str n)))) (str/replace-first (str n) #"^-0." "-.")
    (not (empty? (re-seq #"^0." (str n)))) (str/replace-first (str n) #"^0." " .")
    (pos? n) (str " " n)
    (zero? n) (str " " n)
    :else (str n)))

(defn eliminar-cero-entero [n]
  (cond
    (string? n) n
    (symbol? n) (str n)
    (number? n) (eliminar-cero-entero-aux n)
    :else nil))

true
