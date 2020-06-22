package algebralinear.helper;

import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

/**
 *
 * @author LeonardoViana I
 */
public class Matriz {

    private double matriz[][];

    public boolean Operador(double cond1, String op, double cond2) {
        boolean verdade = false;

        switch (op) {
            case ">":
                verdade = cond1 > cond2;
                break;
            case "<":
                verdade = cond1 < cond2;
                break;
            case "=":
                verdade = (cond1 == cond2);
                break;
            case "<=":
                verdade = cond1 <= cond2;
                break;
            case ">=":
                verdade = cond1 >= cond2;
                break;
            case "!=":
                verdade = cond1 != cond2;
                break;
        }

        return verdade;
    }

    public double[][] criarMatriz(int linha, AnchorPane matriz) {
        this.matriz = new double[linha][linha];
        int posicao = 0;

        for (int i = 0; i < linha; i++) {
            for (int j = 0; j < linha; j++) {
                Button btn = (Button) matriz.getChildren().get(posicao);

                if (btn.getText().equals("")) {
                    this.matriz[j][i] = 0;
                } else {
                    this.matriz[j][i] = Double.parseDouble(btn.getText().replace(",", "."));
                }
                posicao++;
            }
        }

        return this.matriz;

    }

    public static double detLaplace(int n, double[][] matrizP) {
        if (n == 1) {
            //tudo serÃ¡ reduzido a matriz de ordem 1
            return matrizP[0][0];
        } else {
            double det = 0;
            int i, linhas, colunas, j_aux, i_aux;

            //Exclui a primeira linha para calcular os cofatores
            for (i = 0; i < n; i++) {
                //ignora os zeros (SIM ,IGNORA, PRA FICAR MAIS RAPIDO, JA IMAGINOU O TANTO DE MATRIZ ?)
                if (matrizP[0][i] != 0) {
                    double[][] aux = new double[n - 1][n - 1]; //diminuindo a ordem da matriz
                    i_aux = 0;
                    j_aux = 0;
                    //Gera as matrizes para calcular os cofatores
                    for (linhas = 1; linhas < n; linhas++) {
                        for (colunas = 0; colunas < n; colunas++) {
                            if (colunas != i) {
                                aux[i_aux][j_aux] = matrizP[linhas][colunas];
                                j_aux++;
                            }
                        }
                        i_aux++;
                        j_aux = 0;
                    }
                    double cofator = (i % 2 == 0) ? matrizP[0][i] : -matrizP[0][i];
                    det = (det + cofator * detLaplace(n - 1, aux));
                }
            }
            return det;
        }
    }

}
