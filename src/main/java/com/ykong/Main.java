package com.ykong;

import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
import midireader.MidiFileInfo;
import midireader.MidiReader;
import midireader.midievent.MidiEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class Main {

    private static final Map<Integer, Integer> key = new HashMap<>();
    private static Robot robot;

    @SneakyThrows
    public static void play(String filePath, double speed) {
        key.put(48, KeyEvent.VK_Z);
        key.put(50, KeyEvent.VK_X);
        key.put(52, KeyEvent.VK_C);
        key.put(53, KeyEvent.VK_V);
        key.put(55, KeyEvent.VK_B);
        key.put(57, KeyEvent.VK_N);
        key.put(59, KeyEvent.VK_M);
        key.put(60, KeyEvent.VK_A);
        key.put(62, KeyEvent.VK_S);
        key.put(64, KeyEvent.VK_D);
        key.put(65, KeyEvent.VK_F);
        key.put(67, KeyEvent.VK_G);
        key.put(69, KeyEvent.VK_H);
        key.put(71, KeyEvent.VK_J);
        key.put(72, KeyEvent.VK_Q);
        key.put(74, KeyEvent.VK_W);
        key.put(76, KeyEvent.VK_E);
        key.put(77, KeyEvent.VK_R);
        key.put(79, KeyEvent.VK_T);
        key.put(81, KeyEvent.VK_Y);
        key.put(83, KeyEvent.VK_U);

        MidiReader reader = new MidiReader(filePath);
        MidiFileInfo midiFileInfo = reader.getMidiFileInfo();
        for (MidiEvent nextEvent : reader) {
            Map<String, Object> msg = JSON.parseObject(JSON.toJSONString(nextEvent));
            long delayMillis = (long) (nextEvent.getDeltaTime() * midiFileInfo.getMicrosecondsPerTick() / 1000 / speed);
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
