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
