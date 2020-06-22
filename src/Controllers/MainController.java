package Controllers;

import algebralinear.helper.Matriz;
import com.singularsys.jep.JepException;
import java.io.IOException;

import javafx.scene.Cursor;
import javafx.scene.text.Text;
import org.nfunk.jep.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainController implements Initializable {

    //variaveis que recebem o valor que o usuario digitar nas caixas de texto
    @FXML
    private int linha = 0;
    @FXML
    private int coluna = 0;
    //variaveis das caixas de texto que recebem as linhas e colunas
    @FXML
    private TextField linhaTextField;
    @FXML
    private TextField colunaTextField;
    //lugar onde vai aparecer a matriz
    @FXML
    private AnchorPane matrizAnchorPane;
    //caixa de texto onde o usuário vai digitar o nome da matriz
    @FXML
    private TextField nomeMatrizTextField;
    //lugar onde vai aparecer o nome da matriz
    @FXML
    private Label nomeLabel;
    //caixas de textos referentes a primeira condição
    @FXML
    private TextField cond_A_1TextField;
    @FXML
    private TextField operador_1TextField;
    @FXML
    private TextField cond_B_1TextField;
    @FXML
    private TextField lei_1TextField;
    //caixas de textos referentes a segunda condição
    @FXML
    private TextField cond_A_2TextField;
    @FXML
    private TextField operador_2TextField;
    @FXML
    private TextField cond_B_2TextField;
    @FXML
    private TextField lei_2TextField;
    //caixas de textos referentes a terceira condição
    @FXML
    private TextField cond_A_3TextField;
    @FXML
    private TextField operador_3TextField;
    @FXML
    private TextField cond_B_3TextField;
    @FXML
    private TextField lei_3TextField;
    @FXML
    private TextField determinanteTextField;
    @FXML
    private AnchorPane fundo;
    @FXML
    private AnchorPane nomeAnchorPane;

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    

    }

    @FXML
    private void okButton(ActionEvent event) {
        matrizAnchorPane.getChildren().clear();
        linha = Integer.parseInt(linhaTextField.getText());
        coluna = Integer.parseInt(colunaTextField.getText());
        nomeMatrizTextField.getText();

        if (linha > 10 || coluna > 10) {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Definição inválida");
            alert.setContentText("Matriz definida muito grande");
            alert.showAndWait();
        } else {

            for (int i = 1; i <= coluna; i++) {
                for (int j = 1; j <= linha; j++) {
                    Button btn = new Button();
                    TextField tfnome = new TextField();

                    btn.setOnAction((ActionEvent evento) -> {
                        TextInputDialog dialog = new TextInputDialog();

                        dialog.getTitle();
                        Optional<String> result = dialog.showAndWait();
                        btn.setText(result.get());

                    });

                    tfnome.setText(String.valueOf(nomeMatrizTextField));
                    tfnome.setLayoutX(j * 30);
                    btn.setPrefWidth(55);
                    btn.setPrefHeight(29);
                    btn.setCursor(Cursor.HAND);
                    btn.setFont(Font.font("System", 10));
                    btn.setStyle("-fx-border-color: #808080;");
                    btn.setLayoutX(i * 60);
                    btn.setLayoutY(j * 34);
                    
                    matrizAnchorPane.getChildren().add(btn);
                }
            }
        }

    }

    @FXML
    private void preencherAutomaticamene(ActionEvent event) throws JepException {

        nomeLabel.setText(nomeMatrizTextField.getText());

        int tamanho = linha * coluna;
        int razao = tamanho / coluna;

        Matriz cm = new Matriz();

        String op1 = (operador_1TextField.getText());
        String op2 = (operador_2TextField.getText());
        String op3 = (operador_3TextField.getText());

        for (int i = 0; i < tamanho; i++) {

            int c = i / razao;
            int l = i % razao;

            JEP myJep = new JEP();
            myJep.setAllowUndeclared(true);
            myJep.addVariable("l", (l + 1));
            myJep.addVariable("c", (c + 1));
            myJep.addVariable("L", (l + 1));
            myJep.addVariable("C", (c + 1));

            //primeira condicao
            myJep.parseExpression(cond_A_1TextField.getText());
            double num1 = myJep.getValue();
            myJep.parseExpression(cond_B_1TextField.getText());
            double num2 = myJep.getValue();
            boolean condicao1 = cm.Operador(num1, op1, num2);

            //segunda condicao
            myJep.parseExpression(cond_A_2TextField.getText());
            double num3 = myJep.getValue();
            myJep.parseExpression(cond_B_2TextField.getText());
            double num4 = myJep.getValue();
            boolean condicao2 = cm.Operador(num3, op2, num4);

            //terceira condicao
            myJep.parseExpression(cond_A_3TextField.getText());
            double num5 = myJep.getValue();
            myJep.parseExpression(cond_B_3TextField.getText());
            double num6 = myJep.getValue();
            boolean condicao3 = cm.Operador(num5, op3, num6);

            Button btn = (Button) matrizAnchorPane.getChildren().get(i);
            DecimalFormat d = new DecimalFormat("0.0");

            if ((condicao1 == true && condicao2 == true) || (condicao1 == true && condicao3 == true) || (condicao2 == true && condicao3 == true)) {
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("Definição inválida");
                alerta.setContentText("Impossível preencher matriz com mais de uma condição verdadeira!!");
                alerta.showAndWait();
                break;
            }

            if (condicao1 == true) {
                myJep.parseExpression(lei_1TextField.getText());
                btn.setText(String.valueOf(d.format(myJep.getValue())));
            } else {
                if (condicao2 == true) {
                    myJep.parseExpression(lei_2TextField.getText());
                    btn.setText(String.valueOf(d.format(myJep.getValue())));
                } else {
                    if (condicao3 == true) {
                        myJep.parseExpression(lei_3TextField.getText());
                        btn.setText(String.valueOf(d.format(myJep.getValue())));
                    }
                }
            }

            if (condicao1 == false && condicao2 == false && condicao3 == false) {
                btn.setText("");

            }

        }

    }

    @FXML
    private void determinante(ActionEvent event) {

        DecimalFormat dm = new DecimalFormat("0.0");
        if (linha != coluna) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Definição inválida");
            alert.setContentText("Impossível calcular determinante de uma matriz que não é quadrada!!");
            alert.showAndWait();
        } else {
            Matriz m = new Matriz();
            double[][] matrizNormal = m.criarMatriz(linha, matrizAnchorPane);
            determinanteTextField.setText(String.valueOf(dm.format(Matriz.detLaplace(linha, matrizNormal))));
        }

    }
    
    @FXML
    private void limparDeterminante(ActionEvent event) {
    determinanteTextField.clear();
    }
}
