package com.genshin;

import com.alibaba.fastjson.JSON;
import com.util.KeyMapper;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;

public class Playback {

    private static Robot robot;
    private static final Map<Integer, Integer> key = KeyMapper.init();

    public static void play(String path, double speed) throws Exception {

        Sequence sequence = MidiSystem.getSequence(new File(path));
        int resolution = sequence.getResolution();
        Track[] tracks = sequence.getTracks();
        List<MidiEvent> meta_message = new ArrayList<>();
        List<Map<String, Object>> message = new ArrayList<>();
        Map<String, Object> map;
        long tempo = 500000;
        double time = 0.0;
        long old_tick = 0;

        //合并后进行排序，让它们看起来像全部在一条音轨上
        for (Track t : tracks) {
            for (int i = 0, len = t.size(); i < len; i++) {
                meta_message.add(t.get(i));
            }
        }
        meta_message.sort(Comparator.comparing(MidiEvent::getTick));

        //处理midi序列 Tick to Milliseconds
        //milliseconds = tempo / 1000 / resolution
        for (MidiEvent midiEvent : meta_message) {

            map = JSON.parseObject(JSON.toJSONString(midiEvent.getMessage()));

            if (midiEvent.getMessage() instanceof MetaMessage && ((MetaMessage) midiEvent.getMessage()).getType() == 81) {
                byte[] data = midiEvent.getMessage().getMessage();
                tempo = (((data[3] & 255) << 16) | ((data[4] & 255) << 8) | (data[5] & 255));
            }

            if (midiEvent.getTick() > 0) {
                time = (midiEvent.getTick() - old_tick) * (tempo / 1000.0 / resolution);
                old_tick = midiEvent.getTick();
            }
            map.put("time", time);
            message.add(map);

        }

        long start_time = System.currentTimeMillis();
        double input_time = 0;
        long playback_time;
        long current_time;

        for (Map<String, Object> msg : message) {
            input_time += (double) msg.get("time") / speed;

            playback_time = System.currentTimeMillis() - start_time;
            current_time = (long) (input_time - playback_time);

            if (current_time > 0) {
                Thread.sleep(current_time);
            }

            if (msg.containsKey("command") && ((int) msg.get("command") == 144) && key.containsKey((int) msg.get("data1"))) {
                robot.keyPress(key.get((int) msg.get("data1")));
                robot.keyRelease(key.get((int) msg.get("data1")));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        robot = new Robot();

        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jFileChooser.showOpenDialog(null);

        System.out.println("Input play speed x (1.0):");
        double speed = new Scanner(System.in).nextDouble();

        System.out.println("Input sleep time s (s):");
        long sleep = new Scanner(System.in).nextLong();
        System.out.println("Play will be start in " + sleep + " seconds");
        Thread.sleep(sleep * 1000L);

        Playback.play(jFileChooser.getSelectedFile().getAbsolutePath(), speed);
    }
}
