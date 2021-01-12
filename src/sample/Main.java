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
    HashMap<String, Short> filterFrequencies = new HashMap<String, Short>();

    Text textSongFile;

    short[] filterFrequencyValues;

    WavProcessor wavProcessor;
    MusicPlayerThread musicPlayerThread;

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
                    filterFrequencies.put(caption, newvalue.shortValue());

                } );
        s.setValue(value);
        filterFrequencies.put(caption, (short) value);
        VBox box = new VBox(10, text2, s, text);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(60);
        box.setPrefWidth(60);
        box.setMaxWidth(60);
        return box;
    }

    private short[] getFilterFrequencyValues() {
        short[] filterFrequencyValues = new short[10];
        for (int i = 0; i < 10; ++i) {
            filterFrequencyValues[i] = filterFrequencies.get(frequencies[i]);
        }
        return filterFrequencyValues;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Button butonSelect= new Button("Selecteer liedje");
        final FileChooser fileChooser = new FileChooser();
        butonSelect.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File file = fileChooser.showOpenDialog(primaryStage);
                        if (file != null) {
                            System.out.println(file.getAbsolutePath());
                            textSongFile.setText(file.getAbsolutePath());
                        }
                    }
                });

        Button buttonPlay = new Button("Speel");

        buttonPlay.setOnAction(value ->  {
            boolean startThread = false;

            if (musicPlayerThread == null) {
                startThread = true;
            } else if (! musicPlayerThread.isAlive()) {
                startThread = true;
            }

            if (startThread) {
                filterFrequencyValues = getFilterFrequencyValues();
                wavProcessor = new WavProcessor();
                musicPlayerThread = new MusicPlayerThread(wavProcessor);
                musicPlayerThread.start();
                musicPlayerThread.playNow(textSongFile.getText(), filterFrequencyValues);
            }
    });

        Button buttonStop = new Button("Stop");

        buttonStop.setOnAction(value ->  {
            musicPlayerThread.stop();
        });


        GridPane gridPane = new GridPane();

        gridPane.add(butonSelect,0,1, 2, 1);

        textSongFile = new Text("D:\\development\\filter\\muziek\\Beethoven_stereo.wav");
        gridPane.add(textSongFile,2,1, 5, 1);

        gridPane.add(buttonPlay,0,2);
        gridPane.add(buttonStop,0,3);

        VBox[] sliderVboxes =  new VBox[10];

        for(int i=0; i<10; i++) {
            sliderVboxes[i] = makeSlider(50, frequencies[i] );
            gridPane.add(sliderVboxes[i], i, 0);
        }

        Scene scene1= new Scene(gridPane, 600, 500);

        primaryStage.setTitle("Playing DJ");
        primaryStage.setScene(scene1);

        primaryStage.show();
    }

    public static void main(String[] args) {
       launch(args);
    }
}

