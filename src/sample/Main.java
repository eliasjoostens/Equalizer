package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;

import java.io.*;

import java.net.SocketOption;
import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.List;

public class Main extends Application {

    String[] frequencies = {"31.25", "62.5", "125","250", "500", "1K", "2K", "4K", "8K", "16K"};

    /* This is how to declare HashMap */
    HashMap<String, Integer> filterFrequencies = new HashMap<String, Integer>();

    Text textSongFile;

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
                    filterFrequencies.put(caption, newvalue.intValue());

                } );
        s.setValue(value);
        filterFrequencies.put(caption, value);
        VBox box = new VBox(10, text2, s, text);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(60);
        box.setPrefWidth(60);
        box.setMaxWidth(60);
        return box;
    }

    private int[] getFilterFrequencyValues() {
        int[] filterFrequencyValues = new int[10];
        for (int i = 0; i < 10; ++i) {
            filterFrequencyValues[i] = filterFrequencies.get(frequencies[i]);
            System.out.println(filterFrequencyValues[i]);
        }
        return filterFrequencyValues;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Button button= new Button("Selecteer liedje");
        final FileChooser fileChooser = new FileChooser();

        button.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent e) {
                    File file = fileChooser.showOpenDialog(primaryStage);
                    if (file != null) {
                        System.out.println(file.getAbsolutePath());
                        textSongFile.setText(file.getAbsolutePath());
                        WavProcessor wavProcessor = new WavProcessor();
                        //wavProcessor.processWavFile("C:\\Dig-X Year 2\\Semester 1\\Java Advanced\\Short burst Project\\muziek\\Beethoven_stereo.wav");
                        //wavProcessor.processWavFile(file.getAbsolutePath());
                    }
                }
            });


        Button buttonFilter = new Button("Speel");

        buttonFilter.setOnAction(value ->  {
            getFilterFrequencyValues();
        });

        GridPane gridPane = new GridPane();

        gridPane.add(button,0,1);

        textSongFile = new Text("liedje");
        gridPane.add(textSongFile,1,1);

        gridPane.add(buttonFilter,0,2);

        VBox[] sliderVboxes =  new VBox[10];

        for(int i=0; i<10; i++) {
            sliderVboxes[i] = makeSlider(50, frequencies[i] );
            gridPane.add(sliderVboxes[i], i, 0);
        }

        //int[] filterFrequencyValues = getFilterFrequencyValues(sliderVboxes);


        Scene scene1= new Scene(gridPane, 600, 500);

        primaryStage.setTitle("Playing DJ");
        primaryStage.setScene(scene1);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

