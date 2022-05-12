package com.ykong;

import com.alibaba.fastjson.JSON;
import com.util.KeyMapper;
import lombok.SneakyThrows;
import midireader.MidiFileInfo;
import midireader.MidiReader;
import midireader.midievent.MidiEvent;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Scanner;


public class Main {

    private static Robot robot;
    private static final Map<Integer, Integer> key = KeyMapper.init();

    @SneakyThrows
    public static void play(String filePath, double speed) {

        Map<String, Object> msg;
        long delayMillis;
        MidiReader reader = new MidiReader(filePath);
        MidiFileInfo midiFileInfo = reader.getMidiFileInfo();
        for (MidiEvent nextEvent : reader) {
            msg = JSON.parseObject(JSON.toJSONString(nextEvent));
            delayMillis = (long) (nextEvent.getDeltaTime() * midiFileInfo.getMicrosecondsPerTick() / 1000 / speed);
            Thread.sleep(delayMillis);

            if (msg.containsValue("NOTE_ON")) {
                if (key.containsKey((int) msg.get("noteNumber"))) {
                    robot.keyPress(key.get((int) msg.get("noteNumber")));
                    robot.keyRelease(key.get((int) msg.get("noteNumber")));
                }
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

        System.out.println("Input play speed *(multiple):");
        double speed = new Scanner(System.in).nextDouble();

        System.out.println("Play will be start in 3 seconds");
        Thread.sleep(3000);

        Main.play(jFileChooser.getSelectedFile().getAbsolutePath(), speed);


    }
}
