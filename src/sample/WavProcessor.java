package sample;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.*;
import org.jtransforms.fft.*;

public class WavProcessor {
    // size of the byte buffer used to read/write the audio stream
    private int bufferSize = 4096;

    String inputWavFileName;

    public WavProcessor() {
    }

    public void applyFilter(short[] samples) {
        int length = samples.length;
        DoubleFFT_1D dfft1d = new DoubleFFT_1D(length);
        double[] fftSamples = new double[length];
        //two elements per bin: one real, one imaginary
        int bins = fftSamples.length / 2;
        //copy samples
        for (int i = 0; i < length; i++) fftSamples[i] = samples[i];
        //FT samples
        dfft1d.realForward(fftSamples);
        for (int i = 0; i < bins; i++) {
            //apply gain to real...
            fftSamples[2 * i] /= 2;
            //and imaginary elements of FT-ed samples
            fftSamples[2 * i + 1] /= 2;
        }
        //inverse FT samples
        dfft1d.realInverse(fftSamples, true);
        //copy filtered samples back
        for (int i = 0; i < length; i++) samples[i] = (short) fftSamples[i];
    }

    void processWavFile(String inputWavFileName) {
        File audioFile = new File(inputWavFileName);
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat format = audioStream.getFormat();

            System.out.println("Aantal kanalen = " + format.getChannels());
            System.out.println("Sample size in bits = " + format.getSampleSizeInBits());
            System.out.println("Frame size in bytes = " + format.getFrameSize());
            System.out.println("Big endian = " + format.isBigEndian());
            System.out.println("Sample rate = " + format.getSampleRate());

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine audioLine = (SourceDataLine) AudioSystem.getLine(info);
            audioLine.open(format);
            audioLine.start();
            byte[] bytesBuffer = new byte[bufferSize];
            int bytesRead = -1;

            while ((bytesRead = audioStream.read(bytesBuffer)) != -1) {
                /*if (format.isBigEndian()) {
                    ByteBuffer bb = ByteBuffer.allocate(2);
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                    bb.put(firstByte);
                    bb.put(secondByte);
                    short shortVal = bb.getShort(0);
                } else {

                }*/

                //applyFilter(bytesBuffer);
                audioLine.write(bytesBuffer, 0, bytesRead);
            }

            audioLine.drain();
            audioLine.close();
            audioStream.close();

            System.out.println("Playback completed.");
        } catch (UnsupportedAudioFileException ex) {
            System.out.println("The specified audio file is not supported.");
            ex.printStackTrace();
        } catch (LineUnavailableException ex) {
            System.out.println("Audio line for playing back is unavailable.");
            ex.printStackTrace();
        }
        catch (IOException ex) {
            System.out.println("Error playing the audio file.");
            ex.printStackTrace();
        }
    }
}
