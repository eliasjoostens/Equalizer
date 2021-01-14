package equalizer;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main extends Application {

    String[] frequencies = {"31.25", "62.5", "125","250", "500", "1K", "2K", "4K", "8K", "16K"};
    HashMap<String, Short> filterFrequencies = new HashMap<String, Short>();

    Text textSongFile;
    Text textNumberOfChannels;
    Text textSampleSizeBits;
    Text textSampleRate;
    Text textBigEndian;

    ArrayList<Slider> frequencySliders;
    Slider frequencyShiftSlider;

    short[] filterFrequencyValues;
    int frequencyShift;

    WavProcessor wavProcessor;
    MusicPlayerThread musicPlayerThread;

    //Make sliders with javafx
    private VBox makeSlider(String caption)
    {
        Text text = new Text();
        Text text2 = new Text(caption);
        text.setFont(new Font("sans-serif", 10));
        Slider s = new Slider();
        frequencySliders.add(s);
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

                });
        s.setValue(50);
        filterFrequencies.put(caption, (short) 50);
        VBox box = new VBox(10, text2, s, text);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(60);
        box.setPrefWidth(60);
        box.setMaxWidth(60);
        return box;
    }

    private VBox makeHorizontalSlider(int value, String caption)
    {
        Text text = new Text();
        Text text2 = new Text(caption);
        text.setFont(new Font("sans-serif", 10));
        frequencyShiftSlider.setOrientation(Orientation.HORIZONTAL);
        frequencyShiftSlider.setPrefWidth(6000);
        frequencyShiftSlider.setShowTickMarks(true);
        frequencyShiftSlider.setMajorTickUnit(500);
        frequencyShiftSlider.setMinorTickCount(-1000);
        frequencyShiftSlider.setShowTickLabels(true);
        frequencyShiftSlider.valueProperty().addListener(
                (observable, oldvalue, newvalue) ->
                {
                    int i = newvalue.intValue();
                    text.setText(Integer.toString(i) + " Hz");
                    frequencyShift = i;

                } );
        frequencyShiftSlider.setValue(value);
        frequencyShiftSlider.setMax(3000);
        frequencyShiftSlider.setMin(-3000);
        VBox box = new VBox(10, text2, frequencyShiftSlider, text);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(600);
        box.setPrefWidth(600);
        box.setMaxWidth(600);
        return box;
    }

    //Get values for each frequency
    private short[] getFilterFrequencyValues() {
        short[] filterFrequencyValues = new short[10];
        for (int i = 0; i < 10; ++i) {
            filterFrequencyValues[i] = filterFrequencies.get(frequencies[i]);
        }
        return filterFrequencyValues;
    }

    //Make pane with sliders and buttons + added some CSS
    @Override
    public void start(Stage primaryStage) throws Exception{
        GridPane gridPane = new GridPane();

        frequencySliders = new ArrayList<Slider>();

        String cssLayout = "-fx-border-color: grey;\n" +
                "-fx-border-insets: 5;\n" +
                "-fx-border-width: 3;\n" +
                "-fx-border-style: none ;\n";

        HBox panelSliderHbox = new HBox();
        panelSliderHbox.setStyle(cssLayout);

        VBox[] sliderVboxes =  new VBox[10];

        for(int i=0; i<10; i++) {
            sliderVboxes[i] = makeSlider(frequencies[i] );
            panelSliderHbox.getChildren().add(sliderVboxes[i]);
        }
        gridPane.add(panelSliderHbox, 0, 0, 6, 1);


        frequencyShiftSlider = new Slider();
        VBox horizontalSlider = makeHorizontalSlider(0, "Frequentie verschuiving (Hz)");
        gridPane.add(horizontalSlider, 0, 1, 6, 1);


        String cssLayoutButton = "-fx-background-color:" +
                "rgba(0,0,0,0.08)," +
                "linear-gradient(#5a61af, #51536d)," +
                "linear-gradient(#e4fbff 0%,#cee6fb 10%, #a5d3fb 50%, #88c6fb 51%, #d5faff 100%);" +
                "-fx-background-insets: 0 0 -1 0,0,1;" +
                "-fx-background-radius: 5,5,4;" +
                "-fx-padding: 3 30 3 30;" +
                "-fx-text-fill: #242d35;" +
                "-fx-font-size: 14px;";

        Button buttonSelect= new Button("Selecteer liedje");
        buttonSelect.setStyle(cssLayoutButton);
        final FileChooser fileChooser = new FileChooser();
        buttonSelect.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File file = fileChooser.showOpenDialog(primaryStage);
                        if (file != null) {
                            System.out.println(file.getAbsolutePath());
                            textSongFile.setText(file.getAbsolutePath());

                            wavProcessor = new WavProcessor();
                            textNumberOfChannels.setText("Number of channels : " + String.valueOf(wavProcessor.getNumberOfChannels(textSongFile.getText())));
                            textSampleSizeBits.setText("Sample size : " + String.valueOf(wavProcessor.getSampleSize(textSongFile.getText()) + " bits"));
                            textSampleRate.setText("Sample rate : " + String.valueOf(wavProcessor.getSampleRate(textSongFile.getText())+" Hz"));
                            textBigEndian.setText(String.valueOf(wavProcessor.getEndianness(textSongFile.getText())));
                        }
                    }
                });

        textSongFile = new Text("");

        HBox panelSelectSong = new HBox();
        panelSelectSong.setPadding(new Insets(15, 12, 15, 12));
        panelSelectSong.setSpacing(10);
        panelSelectSong.getChildren().add(buttonSelect);
        panelSelectSong.getChildren().add(textSongFile);
        gridPane.add(panelSelectSong,0,2, 5, 1);

        textNumberOfChannels = new Text("Number of channels :");
        textSampleSizeBits = new Text("Sample size :");
        textSampleRate = new Text("Sample rate :");
        textBigEndian = new Text("Endianness");

        VBox panelSongInfo = new VBox();
        panelSongInfo.setPadding(new Insets(15, 12, 15, 12));
        panelSongInfo.setSpacing(10);
        panelSongInfo.getChildren().add(textNumberOfChannels);
        panelSongInfo.getChildren().add(textSampleSizeBits);
        panelSongInfo.getChildren().add(textSampleRate);
        panelSongInfo.getChildren().add(textBigEndian);

        gridPane.add(panelSongInfo,0,3);

        Button buttonPlay = new Button("Speel");
        buttonPlay.setStyle(cssLayoutButton);
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

                if (wavProcessor.getNumberOfChannels(textSongFile.getText()) != 0) {
                    musicPlayerThread = new MusicPlayerThread(wavProcessor);
                    musicPlayerThread.start();
                    musicPlayerThread.playNow(textSongFile.getText(), filterFrequencyValues, frequencyShift);
                }
            }
    });

        Button buttonStop = new Button("Stop");

        buttonStop.setStyle(cssLayoutButton);

        buttonStop.setOnAction(value ->  {
            musicPlayerThread.stop();
        });

        Button buttonReset = new Button("Reset");
        buttonReset.setStyle(cssLayoutButton);
        buttonReset.setOnAction(value ->  {
            for (int i = 0; i < 10; ++i) {
                frequencySliders.get(i).setValue(50);
            }
            frequencyShiftSlider.setValue(0);

        });

        HBox panelPlayStopResetButtons = new HBox();
        panelPlayStopResetButtons.setPadding(new Insets(15, 12, 15, 12));
        panelPlayStopResetButtons.setSpacing(10);
        panelPlayStopResetButtons.getChildren().add(buttonPlay);
        panelPlayStopResetButtons.getChildren().add(buttonStop);
        panelPlayStopResetButtons.getChildren().add(buttonReset);

        gridPane.add(panelPlayStopResetButtons,0,4);

        Scene scene1= new Scene(gridPane, 650, 650);

        primaryStage.setTitle("Playing DJ");
        primaryStage.setScene(scene1);

        primaryStage.show();
    }

    public static void main(String[] args) {
       launch(args);
    }
}

