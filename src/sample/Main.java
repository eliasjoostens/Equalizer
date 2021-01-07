package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;

import java.io.*;

import java.net.SocketOption;

public class Main extends Application {

    private VBox makeSlider(int value, String caption)
    {
        Text text = new Text();
        Text text2 = new Text(caption);
        text.setFont(new Font("sans-serif", 10));
        Slider s = new Slider();
        s.setOrientation(Orientation.VERTICAL);
        s.setPrefHeight(150);
        s.setShowTickMarks(true);
        s.setMajorTickUnit(10);
        s.setMinorTickCount(0);
        s.setShowTickLabels(false);
        s.valueProperty().addListener(
                (observable, oldvalue, newvalue) ->
                {
                    int i = newvalue.intValue();
                    text.setText(Integer.toString(i));
                } );
        s.setValue(value);
        VBox box = new VBox(10, text2, s, text);
        box.setPadding(new Insets(10));
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(30);
        box.setPrefWidth(30);
        box.setMaxWidth(30);
        return box;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Button button= new Button("Muziek");

        button.setOnAction(value ->  {
            System.out.println("hallo");
            WavProcessor wavProcessor = new WavProcessor();
            wavProcessor.processWavFile("C:\\Dig-X Year 2\\Semester 1\\Java Advanced\\Short burst Project\\muziek\\Beethoven_stereo.wav");
        });

        GridPane gridPane = new GridPane();

        //layout.getChildren().add(button);
        String[] frequencies = {"31.25", "62.5", "125","250", "500", "1K", "2K", "4K", "8K", "16K"};
        for(int i=0; i<=5; i++) {
            gridPane.add(makeSlider(50, frequencies[i] ), i, 0);
        }

        Scene scene1= new Scene(gridPane, 300, 250);

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(scene1);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

