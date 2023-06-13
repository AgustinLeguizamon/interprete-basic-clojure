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
    (is (= (expandir-nexts (list '(PRINT X + 10) (list 'NEXT 'A (symbol ",") 'B) (list 'NEXT 'C (symbol ",") 'D))) '((PRINT X + 10) (NEXT A) (NEXT B) (NEXT C) (NEXT D))) )
    (is (= (expandir-nexts (list '(PRINT X + 10) (list 'NEXT 'A))) '((PRINT X + 10) (NEXT A))) ) 
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

(deftest anular-invalido-test
  (testing "Prueba de la funcion: anular-invalidos"
    (is (= (anular-invalidos '(IF X & * Y < 12 THEN LET ! X = 0)) '(IF X nil * Y < 12 THEN LET nil X = 0)))))

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
    )
  )

(deftest variable-integer?-test
  (testing "Prueba de la funcion: variable-integer?") 
  (is (= (variable-integer? 'X%) true))
  (is (= (variable-integer? 'X) false))
  (is (= (variable-integer? 'X$) false)))

(deftest variable-string?-test
  (testing "Prueba de la funcion: variable-string?")
  (is (= (variable-string? 'X%) false))
  (is (= (variable-string? 'X) false))
  (is (= (variable-string? 'X$) true)))
