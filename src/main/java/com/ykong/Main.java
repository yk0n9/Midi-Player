package com.ykong;

import com.util.KeyMapper;
import lombok.SneakyThrows;
import midireader.MidiFileInfo;
import midireader.MidiReader;
import midireader.midievent.MidiEvent;
import midireader.midievent.NoteMidiEvent;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Scanner;

import static midireader.midievent.NoteMidiEvent.NoteEventType.NOTE_ON;

/**
 * @author Ykong
 */
public class Main {

    private static Robot robot;
    private static final Map<Integer, Integer> key = KeyMapper.init();

    @SneakyThrows
    public static void play(String filePath, double speed) {

        long delayMillis;
        MidiReader reader = new MidiReader(filePath);
        MidiFileInfo midiFileInfo = reader.getMidiFileInfo();
        for (MidiEvent nextEvent : reader) {
            delayMillis = (long) (nextEvent.getDeltaTime() * midiFileInfo.getMicrosecondsPerTick() / 1000 / speed);
            Thread.sleep(delayMillis);

            if (nextEvent instanceof NoteMidiEvent && ((NoteMidiEvent) nextEvent).getNoteEventType() == NOTE_ON && key.containsKey(((NoteMidiEvent) nextEvent).getNoteNumber())) {
                robot.keyPress(key.get(((NoteMidiEvent) nextEvent).getNoteNumber()));
                robot.keyRelease(key.get(((NoteMidiEvent) nextEvent).getNoteNumber()));
            }
        }
        reader.close();
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

        Main.play(jFileChooser.getSelectedFile().getAbsolutePath(), speed);


    }
}
