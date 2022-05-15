package com.genshin;

import com.util.KeyMapper;
import lombok.SneakyThrows;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Map;
import java.util.Scanner;

import static javax.sound.midi.ShortMessage.NOTE_ON;

public class Playback {

    private static Robot robot;
    private static final Map<Integer, Integer> key = KeyMapper.init();

    @SneakyThrows
    public static void play(String path, double speed) {

        Sequence sequence = MidiSystem.getSequence(new File(path));
        int resolution = sequence.getResolution();
        Track[] tracks = sequence.getTracks();
        int track_number = tracks.length;
        Track main_track = tracks[0];
        Track track;
        for (int i = 1; i < track_number; i++) {
            track = tracks[i];
            for (int j = 0, lent = track.size(); j < lent; j++) {
                main_track.add(track.get(j));
            }
        }

        MidiEvent midiEvent;
        MidiMessage midiMessage;
        long time = 0;
        long skip = 0;
        int tempo;
        byte[] data;
        for (int i = 0, len = main_track.size(); i < len; i++) {
            midiEvent = main_track.get(i);
            midiMessage = midiEvent.getMessage();
            if (midiMessage instanceof MetaMessage && ((MetaMessage) midiMessage).getType() == 81) {
                data = midiMessage.getMessage();
                tempo = (((data[3] & 255) << 16) | ((data[4] & 255) << 8) | (data[5] & 255));
                skip = tempo / resolution;
            }
            if (midiMessage instanceof ShortMessage && (((ShortMessage) midiMessage).getCommand() == NOTE_ON) && key.containsKey(((ShortMessage) midiMessage).getData1())) {
                robot.keyPress(key.get(((ShortMessage) midiMessage).getData1()));
                robot.keyRelease(key.get(((ShortMessage) midiMessage).getData1()));
            }

            Thread.sleep((long) ((midiEvent.getTick() - time) * skip / 1000 / speed));
            time = midiEvent.getTick();
        }

    }

    @SneakyThrows
    public static void main(String[] args) {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        robot = new Robot();

        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jFileChooser.showOpenDialog(null);

        System.out.println("Input play speed x (1.0):");
        double speed = new Scanner(System.in).nextDouble();

        System.out.println("Play will be start in 3 seconds");
        Thread.sleep(3000);

        Playback.play(jFileChooser.getSelectedFile().getAbsolutePath(), speed);
    }
}
