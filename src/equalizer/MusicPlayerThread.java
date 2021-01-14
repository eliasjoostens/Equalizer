package equalizer;

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
            try {
                Thread.sleep( 10 );
            } catch ( InterruptedException e ) {
                return;
            }

            if ( playNow ) {
                playNow = false;
                wavProcessor.processWavFile(wavFileName, filterFrequencyValues, frequencyShift);
            }
        }
    }
}
