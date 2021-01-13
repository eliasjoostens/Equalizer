package equalizer;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.*;
import org.jtransforms.fft.*;

public class WavProcessor {
    public static int log2(int x)
    {
        return (int) (Math.log(x) / Math.log(2) + 1e-10);
    }

    String inputWavFileName;
    // number of channels in WAV file
    int numberOfchannels = 0;
    // sample size WAV file
    int sampleSizeBytes = 0;
    // size of the byte buffer used to read/write the audio stream
    private int bufferSize = 0;

    public WavProcessor() {
    }

    // calculates fft, multiplies each frequency component by filter frequency values,  calculates inverse fft
    public void applyFilter(short[] samples, short[] filterFrequencyValues, int numberOfFrequenciesToShift) {
        int length = samples.length;
        DoubleFFT_1D dfft1d = new DoubleFFT_1D(length);
        double[] fftSamples = new double[length];
        //two elements per frequency component: one real, one imaginary
        int bins = fftSamples.length / 2;
        //copy samples
        for (int i = 0; i < length; i++) fftSamples[i] = samples[i];

        //FT samples
        dfft1d.realForward(fftSamples);

        if (numberOfFrequenciesToShift != 0) {
            double[] fftSamplesShifted = new double[length];

            if (numberOfFrequenciesToShift > 0) {
                System.arraycopy(fftSamples, 0, fftSamplesShifted, numberOfFrequenciesToShift, length - numberOfFrequenciesToShift);
            }
            if (numberOfFrequenciesToShift < 0) {
                System.arraycopy(fftSamples, -numberOfFrequenciesToShift, fftSamplesShifted, 0, length + numberOfFrequenciesToShift);
            }
            System.arraycopy(fftSamplesShifted, 0, fftSamples, 0, length);
        }

        for (int i = 0; i < bins; i++) {
            //apply gain to real...
            fftSamples[2 * i] = (fftSamples[2 * i] * filterFrequencyValues[i]) / 50 ;
            //and imaginary elements of FT-ed samples
            fftSamples[2 * i + 1] = (fftSamples[2 * i + 1] * filterFrequencyValues[i]) / 50;
        }

        //inverse FT samples
        dfft1d.realInverse(fftSamples, true);

        //copy filtered samples back
        for (int i = 0; i < length; i++) {
            if (Math.abs(fftSamples[i]) > 32767) {
                System.out.println("Overflow");
            }
            samples[i] = (short) fftSamples[i];
        }
    }

    short[] calculateFourierFilterCoefficients(short[] inputFilterCoefficients) {
        short[]  FourierFilterCoefficients = new short[1024];
        FourierFilterCoefficients[0] = inputFilterCoefficients[0];
        for(int i=1; i<1024; ++i) {
            FourierFilterCoefficients[i] = inputFilterCoefficients[log2(i)];
        }
        return FourierFilterCoefficients;
    }

    int getNumberOfChannels(String inputWavFileName) {
        File audioFile = new File(inputWavFileName);
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat format = audioStream.getFormat();

            return format.getChannels();
        } catch (UnsupportedAudioFileException ex) {
            System.out.println("The specified audio file is not supported.");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Error playing the audio file.");
            ex.printStackTrace();
        }
        return 0;
    }

    int getSampleSize(String inputWavFileName) {
        File audioFile = new File(inputWavFileName);
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat format = audioStream.getFormat();

            return format.getSampleSizeInBits();
        } catch (UnsupportedAudioFileException ex) {
            System.out.println("The specified audio file is not supported.");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Error playing the audio file.");
            ex.printStackTrace();
        }
        return 0;
    }

    String getEndianness(String inputWavFileName) {
        File audioFile = new File(inputWavFileName);
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat format = audioStream.getFormat();
            if (format.isBigEndian()) {
                return "Big Endian";
            } else return "Little Endian";
        } catch (UnsupportedAudioFileException ex) {
            System.out.println("The specified audio file is not supported.");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Error playing the audio file.");
            ex.printStackTrace();
        }
        return "Invalid";
    }

    float getSampleRate(String inputWavFileName) {
        File audioFile = new File(inputWavFileName);
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat format = audioStream.getFormat();

            return format.getSampleRate();
        } catch (UnsupportedAudioFileException ex) {
            System.out.println("The specified audio file is not supported.");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Error playing the audio file.");
            ex.printStackTrace();
        }
        return 0;
    }

    void processWavFile(String inputWavFileName, short[] filterFrequencyValues, int frequencyShift) {
        short[] FourierFilterCoefficients = calculateFourierFilterCoefficients(filterFrequencyValues);

        File audioFile = new File(inputWavFileName);
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat format = audioStream.getFormat();

            numberOfchannels = format.getChannels();
            sampleSizeBytes = format.getFrameSize() / format.getChannels();

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine audioLine = (SourceDataLine) AudioSystem.getLine(info);
            audioLine.open(format);
            audioLine.start();

            bufferSize = 1024*numberOfchannels*sampleSizeBytes;

            byte[] bytesBuffer = new byte[bufferSize];
            byte[] bytesBufferOut = new byte[bufferSize];
            int bytesRead = -1;

            while ((bytesRead = audioStream.read(bytesBuffer)) != -1) {
                short[] shortsRead = new short[bytesRead / 2];
                // to turn bytes to shorts as either big endian or little endian.

                if (format.isBigEndian()) {
                    ByteBuffer.wrap(bytesBuffer).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(shortsRead);
                } else {
                    ByteBuffer.wrap(bytesBuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortsRead);
                }

                short[] shortArrayChannel1 = new short[1024];
                short[] shortArrayChannel2 = new short[1024];

                for (int i = 0; i < 1024; ++i) {
                    shortArrayChannel1[i] = shortsRead[i*numberOfchannels];
                }

                int numberOfFrequenciesToShift = Math.round(frequencyShift/(format.getSampleRate() / 1024));
                System.out.println("numberOfFrequenciesToShift = " + numberOfFrequenciesToShift);

                if (numberOfchannels == 2) {
                    for (int i = 0; i < 1024; ++i) {
                        shortArrayChannel2[i] = shortsRead[i*numberOfchannels+1];
                    }
                }

                applyFilter(shortArrayChannel1, FourierFilterCoefficients, numberOfFrequenciesToShift);
                if (numberOfchannels == 2) {
                    applyFilter(shortArrayChannel2, FourierFilterCoefficients, numberOfFrequenciesToShift);
                }

                // to turn shorts back to bytes.
                byte[] bytesArrayChannel1 = new byte[shortArrayChannel1.length * 2];
                byte[] bytesArrayChannel2 = new byte[shortArrayChannel1.length * 2];

                if (format.isBigEndian()) {
                    ByteBuffer.wrap(bytesArrayChannel1).order(ByteOrder.BIG_ENDIAN).asShortBuffer().put(shortArrayChannel1);
                } else {
                    ByteBuffer.wrap(bytesArrayChannel1).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shortArrayChannel1);
                }

                if (numberOfchannels == 2) {
                    if (format.isBigEndian()) {
                        ByteBuffer.wrap(bytesArrayChannel2).order(ByteOrder.BIG_ENDIAN).asShortBuffer().put(shortArrayChannel2);
                    } else {
                        ByteBuffer.wrap(bytesArrayChannel2).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shortArrayChannel2);
                    }
                }

                if (numberOfchannels == 1) {
                    bytesBufferOut = bytesArrayChannel1;
                }

                if (numberOfchannels == 2) {
                    for (int i=0; i< 1024; ++i) {
                        bytesBufferOut[2*numberOfchannels*i]=bytesArrayChannel1[2*i];
                        bytesBufferOut[2*numberOfchannels*i+1]=bytesArrayChannel1[2*i+1];
                        bytesBufferOut[2*numberOfchannels*i+2]=bytesArrayChannel2[2*i];
                        bytesBufferOut[2*numberOfchannels*i+3]=bytesArrayChannel2[2*i+1];
                    }
                }
                audioLine.write(bytesBufferOut, 0, bytesRead);
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
