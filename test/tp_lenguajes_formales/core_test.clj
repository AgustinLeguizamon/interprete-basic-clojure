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

