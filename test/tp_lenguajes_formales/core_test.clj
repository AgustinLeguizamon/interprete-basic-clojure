(ns tp-lenguajes-formales.core-test
  (:require [clojure.test :refer :all]
            [tp-lenguajes-formales.core :refer :all]))

(deftest cargar-linea-test
  (testing "Prueba de la funcion: cargar-linea"
    (is (= (cargar-linea '(10 (PRINT X)) [() [:ejecucion-inmediata 0] [] [] [] 0 {}]) '[((10 (PRINT X))) [:ejecucion-inmediata 0] [] [] [] 0 {}]))
    (is (= (cargar-linea '(20 (X = 100)) '[((10 (PRINT X))) [:ejecucion-inmediata 0] [] [] [] 0 {}]) '[((10 (PRINT X)) (20 (X = 100))) [:ejecucion-inmediata 0] [] [] [] 0 {}]))
    (is (= (cargar-linea '(15 (X = X + 1)) '[((10 (PRINT X)) (20 (X = 100))) [:ejecucion-inmediata 0] [] [] [] 0 {}]) '[((10 (PRINT X)) (15 (X = X + 1)) (20 (X = 100))) [:ejecucion-inmediata 0] [] [] [] 0 {}]))
    (is (= (cargar-linea '(15 (X = X - 1)) '[((10 (PRINT X)) (15 (X = X + 1)) (20 (X = 100))) [:ejecucion-inmediata 0] [] [] [] 0 {}]) '[((10 (PRINT X)) (15 (X = X - 1)) (20 (X = 100))) [:ejecucion-inmediata 0] [] [] [] 0 {}]))
    
    ))

(deftest contar-sentencias-test
  (testing "Prueba de la funcion: contar-sentencias-test"
    (is (= (contar-sentencias 10 [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [10 1] [] [] [] 0 {}]) 2))
    (is (= (contar-sentencias 15 [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [10 1] [] [] [] 0 {}]) 1))
    (is (= (contar-sentencias 20 [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [10 1] [] [] [] 0 {}]) 2))
    
    ))

(deftest expandir-nexts-test
  (testing "Prueba de la funcion: expandir-nexts"
    (is (= (expandir-nexts (list '(PRINT 1) (list 'NEXT 'A (symbol ",") 'B))) '((PRINT 1) (NEXT A) (NEXT B))))
    (is (= (expandir-nexts (list '(PRINT X + 10) (list 'NEXT 'A (symbol ",") 'B) (list 'NEXT 'C (symbol ",") 'D))) '((PRINT X + 10) (NEXT A) (NEXT B) (NEXT C) (NEXT D))))
    (is (= (expandir-nexts (list '(PRINT X + 10) (list 'NEXT 'A))) '((PRINT X + 10) (NEXT A))))
    (is (= (expandir-nexts (list (list 'NEXT))) (list (list 'NEXT))))
    ))

(deftest extraer-data-test
  (testing "Prueba de la funcion: extraer-data"
    (is (= (extraer-data '(())) '()))
    (is (= (extraer-data (list '(10 (PRINT X) (REM ESTE NO) (DATA 30)) '(20 (DATA HOLA)) (list 100 (list 'DATA 'MUNDO (symbol ",") 10 (symbol ",") 20)))) '("HOLA" "MUNDO" 10 20)))))

(deftest buscar-lineas-restantes-test
  (testing "Prueba de la fucnion: buscar-lineas-restantes"
    (is (= (buscar-lineas-restantes [() [:ejecucion-inmediata 0] [] [] [] 0 {}]) nil))
    (is (= (buscar-lineas-restantes ['((PRINT X) (PRINT Y)) [:ejecucion-inmediata 2] [] [] [] 0 {}]) nil))
    (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [10 2] [] [] [] 0 {}]) (list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J)))))
    (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [10 1] [] [] [] 0 {}]) (list '(10 (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J)))))
    (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [10 0] [] [] [] 0 {}]) (list '(10) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J)))))
    (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [15 1] [] [] [] 0 {}]) (list '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J)))))
    (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [15 0] [] [] [] 0 {}]) (list '(15) (list 20 (list 'NEXT 'I (symbol ",") 'J)))))
    (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 3] [] [] [] 0 {}]) (list (list 20 (list 'NEXT 'I) (list 'NEXT 'J)))))
    (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 2] [] [] [] 0 {}]) (list (list 20 (list 'NEXT 'I) (list 'NEXT 'J)))))
    (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 1] [] [] [] 0 {}]) (list (list 20 (list 'NEXT 'J)))))
    (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 0] [] [] [] 0 {}]) (list (list 20))))
    (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 -1] [] [] [] 0 {}]) (list (list 20))))
    (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [25 0] [] [] [] 0 {}]) nil)))
  )

(comment
  ;; no se puede testear pq print devuelve nil
  (deftest dar-error-test
    (testing "Prueba de la funcion: dar-error"
      (is (= (dar-error 16 [:ejecucion-inmediata 4]) "?SYNTAX  ERROR"))
      (is (= (dar-error "?ERROR DISK FULL" [:ejecucion-inmediata 4]) "?SYNTAX  ERROR"))
      (is (= (dar-error 16 [100 3]) " ?SYNTAX  ERROR IN 100nil"))
      (is (= (dar-error "?ERROR DISK FULL" [100 3]) "?ERROR DISK FULL IN 100nil"))))
  )

(deftest palabra-reservada?-test
  (testing "Prueba de la funcion: palabra-reservada"
    (is (= (palabra-reservada? 'REM) true))
    (is (= (palabra-reservada? 'EXIT) true))
    (is (= (palabra-reservada? 'CLEAR) true))
    (is (= (palabra-reservada? 'RUN) true))
    (is (= (palabra-reservada? 'SPACE) false))
    (is (= (palabra-reservada? 'RUn) false)))
  )

(deftest anular-invalidos-test
  (testing "Prueba de la funcion: anular-invalidos"
    (is (= (anular-invalidos '(IF X & * Y < 12 THEN LET ! X = 0)) '(IF X nil * Y < 12 THEN LET nil X = 0)))
    (is (= (anular-invalidos '(X$ = "")) '(X$ = "")))
    (is (= (anular-invalidos '(X$ = "HOLA")) '(X$ = "HOLA")))
    (is (= (anular-invalidos (list 'PRINT 'MID$ (symbol "(") 'N$ (symbol ",") 'I (symbol ")"))) (list 'PRINT 'MID$ (symbol "(") 'N$ (symbol ",") 'I (symbol ")"))))
    (is (= (anular-invalidos (list 'PRINT "ENTER A" (symbol ":") 'INPUT 'A (symbol ":") 'PRINT "ENTER B" (symbol ":") 'INPUT 'B)) (list 'PRINT "ENTER A" (symbol ":") 'INPUT 'A (symbol ":") 'PRINT "ENTER B" (symbol ":") 'INPUT 'B)))
    (is (= (anular-invalidos (list 'LET 'P '= '.)) (list 'LET 'P '= '.)))
    (is (= (anular-invalidos (list 'IF 'P '= 1 'THEN 'PRINT 'X (symbol ";") " " (symbol ";"))) (list 'IF 'P '= 1 'THEN 'PRINT 'X (symbol ";") " " (symbol ";"))))
    
    ))

(deftest eliminar-cero-entero-test
  (testing "Prueba de la funcion: eliminar-cero-entero"
    (is (= (eliminar-cero-entero nil) nil))
    (is ( = (eliminar-cero-entero 'A) "A"))
    (is ( = (eliminar-cero-entero 0) " 0"))
    (is ( = (eliminar-cero-entero 1.5) " 1.5"))
    (is ( = (eliminar-cero-entero 1) " 1"))
    (is ( = (eliminar-cero-entero -1) "-1"))
    (is ( = (eliminar-cero-entero -1.5) "-1.5"))
    (is ( = (eliminar-cero-entero 0.5) " .5"))
    (is ( = (eliminar-cero-entero -0.5) "-.5"))
    (is (= (eliminar-cero-entero "HOLA") "HOLA"))
    )
  )

(deftest variable-integer?-test
  (testing "Prueba de la funcion: variable-integer?"
  (is (= (variable-integer? 'X%) true))
  (is (= (variable-integer? 'X) false))
  (is (= (variable-integer? 'X$) false))))

(deftest variable-string?-test
  (testing "Prueba de la funcion: variable-string?"
  (is (= (variable-string? 'X%) false))
  (is (= (variable-string? 'X) false))
  (is (= (variable-string? 'X$) true))))

(deftest variable-float?-test
  (testing "Prueba de la funcion: variable-float?"
  (is (= (variable-float? 'X%) false))
  (is (= (variable-float? 'X) true))
  (is (= (variable-float? 'X$) false))
  (is (= (variable-float? "HOLA") false))))

(deftest operador?-test
  (testing "Prueba de la funcion: operador?"
  (is (= (operador? '+) true))
  (is (= (operador? '-) true))
  (is (= (operador? '*) true))
  (is (= (operador? '/) true))
  (is (= (operador? '>=) true))
  (is (= (operador? '>) true))
  (is (= (operador? 'AND) true))
  (is (= (operador? (symbol "+")) true))
  (is (= (operador? (symbol "^")) true))
  (is (= (operador? (symbol "++")) false))
  (is (= (operador? (symbol "%")) false))))

(deftest aridad-test
  (testing "Prueba de la funcion: aridad"
    (is (= (aridad 'ATN) 1))
    (is (= (aridad '+) 2))
    (is (= (aridad '/) 2))
    (is (= (aridad 'AND) 2))
    (is (= (aridad '<) 2))
    (is (= (aridad '<=) 2))
    (is (= (aridad (symbol "STR$")) 1))
    (is (= (aridad (symbol "MID$")) 2))
    (is (= (aridad (symbol "MID3$")) 3))
    (is (= (aridad 'THEN) 0))
    (is (= (aridad '-u) 1))
    )
)

(deftest precedencia-test
  (testing "Prueba de la funcion: precedencia"
    (is (= (precedencia 'OR) 1))
    (is (= (precedencia 'AND) 2))
    (is (= (precedencia '*) 6))
    (is (= (precedencia '-u) 7))
    (is (= (precedencia 'MID$) 8))))

(deftest eliminar-cero-decimal-test
  (testing "Prueba de la funcion: eliminar-cero-decimal"
    (is (= (eliminar-cero-decimal 1.5) 1.5))
    (is (= (eliminar-cero-decimal 1.50) 1.50))
    (is (= (eliminar-cero-decimal 1.504) 1.504))
    (is (= (eliminar-cero-decimal 1.5040) 1.504))
    (is (= (eliminar-cero-decimal 1.0) 1))
    (is (= (eliminar-cero-decimal 10.0000) 10))
    (is (= (eliminar-cero-decimal 'A) 'A))
    (is (= (eliminar-cero-decimal "HOLA") "HOLA"))
    ))

(deftest preprocesar-expresion-test
  (testing "Prueba de la funcion: preprocesar-expresion"
    (is (= (preprocesar-expresion '(X$ + " MUNDO" + Z$) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X$ "HOLA"}]) '("HOLA" + " MUNDO" + "")))
    (is (= (preprocesar-expresion '(X + . / Y% * Z) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X 5 Y% 2}]) '(5 + 0 / 2 * 0)))
    ))

(deftest desambiguar-test
  (testing "Prueba de la funcion: desambiguar"
    (is (= (desambiguar (list '- 2 '*)) '(-u 2 *)))
    (is (= (desambiguar (list (symbol "x") 3 '+ 5 '- (symbol "x"))) '(x 3 + 5 - x)))
    (is (= (desambiguar (list (symbol "x") 3 '+ 5 '- (symbol "x"))) (list (symbol "x") 3 '+ 5 '- (symbol "x"))))
    (is (= (desambiguar (list '- 2 '* (symbol "(") '- 3 '+ 5 '- (symbol "(") '+ 2 '/ 7 (symbol ")") (symbol ")"))) (list '-u 2 '* (symbol "(") '-u 3 '+ 5 '- (symbol "(") 2 '/ 7 (symbol ")") (symbol ")"))))
    (is (= (desambiguar (list 'MID$ (symbol "(") 1 (symbol ",") 2 (symbol ")"))) (list 'MID$ (symbol "(") 1 (symbol ",") 2 (symbol ")"))))
    (is (= (desambiguar (list 'MID$ (symbol "(") 1 (symbol ",") 2 (symbol ",") 3 (symbol ")"))) (list 'MID3$ (symbol "(") 1 (symbol ",") 2 (symbol ",") 3 (symbol ")"))))
    (is (= (desambiguar (list 'MID$ (symbol "(") 1 (symbol ",") '- 2 '+ 'K (symbol ",") 3 (symbol ")"))) (list 'MID3$ (symbol "(") 1 (symbol ",") '-u 2 '+ 'K (symbol ",") 3 (symbol ")"))))
    
    ))

(deftest ejecutar-asignacion-test
  (testing "Prueba de la funcion: ejecutar-asignacion"
    (is (= (ejecutar-asignacion '(X = 5) ['((10 (PRINT X))) [10 1] [] [] [] 0 {}]) '[((10 (PRINT X))) [10 1] [] [] [] 0 {X 5}]))
    (is (= (ejecutar-asignacion '(X = 5) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X 2}]) '[((10 (PRINT X))) [10 1] [] [] [] 0 {X 5}]))
    (is (= (ejecutar-asignacion '(X = X + 1) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X 2}]) '[((10 (PRINT X))) [10 1] [] [] [] 0 {X 3}]))
    (is (= (ejecutar-asignacion '(X$ = X$ + " MUNDO") ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X$ "HOLA"}]) '[((10 (PRINT X))) [10 1] [] [] [] 0 {X$ "HOLA MUNDO"}]))))

(deftest continuar-linea-test
  (testing "Prueba de la funcion: continuar-linea"
    ;; este test imprime el error en pantalla cuando se corre lein test
    (is (= (continuar-linea [(list '(10 (PRINT X)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 3] [] [] [] 0 {}]) [nil [(list (list 10 '(PRINT X)) (list 15 '(X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 3] [] [] [] 0 {}]]))
    (is (= (continuar-linea [(list '(10 (PRINT X)) '(15 (GOSUB 100) (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 3] [[15 2]] [] [] 0 {}]) [:omitir-restante  [(list (list 10 '(PRINT X)) (list 15 '(GOSUB 100) '(X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [15 1] [] [] [] 0 {}]]))
    (is (= (continuar-linea [(list '(10 (PRINT X)) '(15 (GOSUB 100) (X = X + 1)) (list 20 (list 'NEXT 'I))) [20 3] [[15 2]] [] [] 0 {}]) [:omitir-restante  [(list (list 10 '(PRINT X)) (list 15 '(GOSUB 100) '(X = X + 1)) (list 20 '(NEXT I))) [15 1] [] [] [] 0 {}]]))
    (is (= (continuar-linea [(list '(10 (PRINT X)) '(15 (GOSUB 100) (X = X + 1))) [20 3] [[15 2]] [] [] 0 {}]) [:omitir-restante  [(list (list 10 '(PRINT X)) (list 15 '(GOSUB 100) '(X = X + 1))) [15 1] [] [] [] 0 {}]]))
    (is (= (continuar-linea [(list '(15 (GOSUB 100) (X = X + 1))) [20 3] [[15 2]] [] [] 0 {}]) [:omitir-restante  [(list '(15 (GOSUB 100) (X = X + 1))) [15 1] [] [] [] 0 {}]]))))


(deftest aplicar-test
  (testing "Prueba de la funcion: aplicar"
    (is (= (aplicar '< 2 1 [10 1]) 0))
    (is (= (aplicar '< 0 1 [10 1]) -1))
    (is (= (aplicar '> 0 1 [10 1]) 0))
    (is (= (aplicar '<= 1 1 [10 1]) -1))
    (is (= (aplicar '>= 2 1 [10 1]) -1))))


(deftest shunting-yard-test
  (testing "Prueba de la funcion: shunting-yard"
    (is (= (shunting-yard (list 'MID$ (symbol "(") "HOLA" (symbol ",") 1 (symbol ")"))) (list "HOLA" 1 'MID$)))
    
    ))

(deftest calcular-expresion-test
  (testing "Prueba de la funcion: calcular-rpn"
    (is (= (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion (list 0 -1 'OR) [() [:ejecucion-inmediata 0] [] [] [] 0 {'A 4, 'B 2}]))) [:ejecucion-inmediata 0]) -1))
    (is (= (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion (list (symbol "(") 2 (symbol ")") '<> 1) [() [:ejecucion-inmediata 0] [] [] [] 0 {'A 4, 'B 2}]))) [:ejecucion-inmediata 0]) -1))
    (is (= (calcular-expresion (list 'A '- 'INT (symbol "(") 'A '/ 'B (symbol ")") '* 'B) [() [:ejecucion-inmediata 0] [] [] [] 0 {'A 4, 'B 2}]) 0))
    (is (= (calcular-expresion (list 'ASC (symbol "(") 'MID$ (symbol "(") 'W$ (symbol ",") 'I (symbol ",") 1 (symbol ")") (symbol ")") '- 64) [() [:ejecucion-inmediata 0] [] [] [] 0 {'W$ "AMSTRONG", 'I 1}]) 1))
    (is (= (calcular-expresion (list 8 '* 'ATN (symbol "(") 1 (symbol ")")) [() [:ejecucion-inmediata 0] [] [] [] 0 {}]) (* 8 (Math/atan 1))))
    (is (= (calcular-rpn (shunting-yard (desambiguar (preprocesar-expresion (list 'V$ '<> "") [() [:ejecucion-inmediata 0] [] [] [] 0 {'V$ "QUIT"}]))) [:ejecucion-inmediata 0]) -1))
    )) 

(deftest evaluar-test
  (testing "Context of the test assertions"
    (is (= (evaluar (list 'READ 'S$) [() [10 0] [] [] ["ALFA", "ROMEO"] 0 {}]) [:sin-errores [() [10 0] [] [] ["ALFA", "ROMEO"] 1 {'S$ "ALFA"}]]))
    (is (= (evaluar (list 'READ 'S$) [() [10 0] [] [] ["ALFA", "ROMEO"] 1 {}]) [:sin-errores [() [10 0] [] [] ["ALFA", "ROMEO"] 2 {'S$ "ROMEO"}]]))
    (is (= (evaluar (list 'RESTORE) [() [10 0] [] [] ["ALFA", "ROMEO"] 1 {}]) [:sin-errores [() [10 0] [] [] ["ALFA", "ROMEO"] 0 {}]]) "Deberia resetear data-ptr a 0")
    ;; (is (= (evaluar (list 'DATA 'ALFA 'ROMEO) [() [10 0] [] [] [] 0 {}]) [:sin-errores [() [10 0] [] [] [] 0 {}]]) "Deberia no hacer nada")
    (is (= (evaluar (list 'PRINT 'INT (symbol "(") 'SIN (symbol "(") 'A (symbol ")") '* 100000 (symbol ")") '/ 100000) [() [:ejecucion-inmediata 0] [] [] [] 0 {'A 1}]) [:sin-errores [() [:ejecucion-inmediata 0] [] [] [] 0 {'A 1}]]))
    )) 
