package sample;

public class MusicPlayerThread extends Thread {
    WavProcessor wavProcessor;
    String wavFileName;
    short[] filterFrequencyValues;
    int frequencyShift;

    boolean quit;
    boolean playNow;

    MusicPlayerThread(WavProcessor wavProc) {
        wavProcessor = wavProc;
        quit    = false;
        playNow = false;
    }

    public void playNow(String fileName, short[] filterFreqValues, int freqShift) {
        frequencyShift = freqShift;
        filterFrequencyValues = filterFreqValues;
        wavFileName = fileName;
        playNow = true;
    }

    public void quit() {
        quit = true;
    }

    public void run() {
        while ( !quit ) {
            // wait 10 ms, then check if need to play
            try {
                Thread.sleep( 10 );
            } catch ( InterruptedException e ) {
                return;
            }

            // if we have to play the sound, do it!
            if ( playNow ) {
                playNow = false;
                wavProcessor.processWavFile(wavFileName, filterFrequencyValues, frequencyShift);

            }
            // go back and wait again for 10 ms...
        }
    }
}
