package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.Scene;
import javafx.scene.control.Label;

import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    private static TextArea read = new TextArea();
    private static TextArea information = new TextArea();
    private static TextArea debug = new TextArea("Enter port name");
    private boolean needClean = true;
    private String buf = new String();
    private int destination;
    private int source;
    private String needError;

    @Override
    public void start(Stage primaryStage) {
        FlowPane root = new FlowPane();
        Com com = new Com();
        Scene scene = new Scene(root, 290, 1000, Color.BLACK);
        primaryStage.setResizable(false);//
        TextArea write = new TextArea();
        write.setWrapText(true);
        write.setMaxWidth(300);
        write.setMinHeight(260);
        //устанавливаю цвет заднего фона,цвет букв и их размер
        write.setStyle("-fx-control-inner-background: black;-fx-text-fill: cadetblue; ");
        write.setTranslateY(5);
        ////////////////////////////////////////////////
        debug.setWrapText(true);
        debug.setMaxWidth(300);
        debug.setMinHeight(260);
        debug.setStyle("-fx-control-inner-background: black;-fx-text-fill: red;");
        debug.setTranslateY(45);
        /////////////////////////////////////////////////////
        read.setEditable(false);
        read.setWrapText(true);
        read.setMaxWidth(300);
        read.setMinHeight(260);
        read.setStyle("-fx-control-inner-background: black;-fx-text-fill: green;");
        read.setTranslateY(25);
        //////////////////////////////////////////////
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < 256; i++) {
            list.add(i);
        }
        ObservableList<Integer> langs = FXCollections.observableArrayList(list);
        ComboBox<Integer> destinationAdress = new ComboBox<Integer>(langs);
        destinationAdress.setValue(1);
        destination=1;
        destinationAdress.setTranslateX(200);
        destinationAdress.setTranslateY(-10);
        destinationAdress.setOnAction(event -> destination=(destinationAdress.getValue()));
        //////////////////////////////////////////////
        ComboBox<Integer> sourseAdress = new ComboBox<Integer>(langs);
        sourseAdress.setValue(0);
        sourseAdress.setTranslateX(27);
        sourseAdress.setTranslateY(20);
        sourseAdress.setOnAction(event -> source=(sourseAdress.getValue()));
        //////////////////////////////////////////////
        information.setEditable(false);
        information.setWrapText(true);
        information.setMaxWidth(300);
        information.setMaxHeight(60);
        information.setStyle("-fx-control-inner-background: black;-fx-text-fill: green;");
        information.setTranslateY(135);
        //////////////////////////////////////////////
        Label simulateErrorL = new Label("Симулировать ошибку?");
        simulateErrorL.setTranslateY(23);
        simulateErrorL.setTranslateX(-73);
        simulateErrorL.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        //////////////////////////////////////////////
        Label destinationAdressL = new Label("Адресс назначения");
        destinationAdressL.setTranslateY(-10);
        destinationAdressL.setTranslateX(-40);
        destinationAdressL.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        //////////////////////////////////////////////
        List<String> stringlist = new ArrayList<String>();
        stringlist.add("Да");
        stringlist.add("Нет");
        ObservableList<String> strList = FXCollections.observableArrayList(stringlist);
        ComboBox<String> error = new ComboBox<String>(strList);
        error.setTranslateY(25);
        error.setTranslateX(-31);
        error.setValue("Нет");
        error.setOnAction(event -> needError=(error.getValue()));
        /////////////////////////////////////////////   /
        Label sourseAdressL = new Label("Адресс источника");
        sourseAdressL.setTranslateY(-3);
        sourseAdressL.setTranslateX(27);
        sourseAdressL.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        //////////////////////////////////////////////
        Label writeL = new Label("Введите данные");
        writeL.setTranslateY(0);
        writeL.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        ////////////////////////////////////////////////////////////
        Label readL = new Label("Читать");
        readL.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        readL.setTranslateY(285);
        ///////////////////////////////////////////////////////////
        Label debugL = new Label("Дебаг/коммандная строка");
        debugL.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        debugL.setTranslateY(40);
        ///////////////////////////////////////////////////////////
        root.setStyle("-fx-background-color: #000000");
        root.getChildren().addAll(writeL, readL, write, read, debugL, debug, information, destinationAdress,
                destinationAdressL,sourseAdress,sourseAdressL,simulateErrorL,error);
        primaryStage.setScene(scene);
        primaryStage.show();
        write.setOnKeyPressed(new EventHandler<KeyEvent>() {//отслеживаем нажатие на Enter в поле ввода
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    if(destination==source){
                        debug.setText("Destination address and source address must not match!");
                        return;
                    }
                    if (com.activateSend(write.getText())) {
                        needClean = true;
                    } else {
                        debug.setText("something going wrong");
                        needClean = true;
                    }
                }
            }
        });

        debug.setOnKeyPressed(new EventHandler<KeyEvent>() {//отслеживаем нажатие на Enter в поле контроля
            @Override
            public void handle(KeyEvent event) {
                if (needClean) {
                    debug.setText(null);
                    needClean = false;
                }
                if (event.getCode() == KeyCode.ENTER) {
                    String text = debug.getText();//сохраняем первоначальное значение
                    if (com.connect(text)) {//проверяем на команду подключения к ком портам
                        needClean = true;
                    } else {
                        if (com.control(text)) {//потом проверяем на способ управления
                            debug.setText("Mode set");
                        }
                        needClean = true;
                    }
                }
            }
        });
    }

    public void output(String str) {//выводим переданный текст
        try {
            if (str.equals("$")) {
                buf = "";
                return;
            }
            buf += str;
            javafx.application.Platform.runLater(() -> read.setText(buf));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void debugInformation(String str) {//выводим информацию в дебагер
        debug.setText(str);
        needClean = true;
    }

    public void getInformation(String mode, String comeName) {
        information.setText("Режим обмена данными " + mode + " Название ком порта " + comeName + " Для установки RTS напишите RTS,DTR-DTR,без режимов - noMode");
    }


    public static void main(String[] args) {
        launch(args);
    }
}
