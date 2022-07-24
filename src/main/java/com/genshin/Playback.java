package com.genshin;

import com.alibaba.fastjson.JSON;
import com.sun.javafx.application.PlatformImpl;
import com.util.KeyMapper;
import javafx.stage.FileChooser;

import javax.sound.midi.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;

public class Playback {

    private static Robot robot;
    private static final Map<Integer, Integer> key = KeyMapper.init();

    public static List<Map<String, Object>> init(File file) throws Exception {

        Sequence sequence = MidiSystem.getSequence(file);
        int resolution = sequence.getResolution();
        Track[] tracks = sequence.getTracks();
        List<MidiEvent> event_message = new ArrayList<>();
        List<Map<String, Object>> message = new ArrayList<>();
        Map<String, Object> map;
        long tempo = 500000;
        long tick = 0;

        System.out.println("Start processing sequence!");

        //消息按播放顺序返回  就好像它们都在一条轨道上一样。
        for (Track t : tracks) {
            for (int i = 0, len = t.size(); i < len; i++) {
                event_message.add(t.get(i));
            }
        }
        event_message.sort(Comparator.comparing(MidiEvent::getTick));

        //处理midi序列  根据每个序列区间不同的tempo赋予不同的时间差
        //Tick to Millisecond
        //milliseconds = tick difference * (tempo / 1000 / resolution)
        for (MidiEvent midiEvent : event_message) {

            double time;
            map = JSON.parseObject(JSON.toJSONString(midiEvent.getMessage()));

            if (midiEvent.getMessage() instanceof MetaMessage && ((MetaMessage) midiEvent.getMessage()).getType() == 81) {

                byte[] data = midiEvent.getMessage().getMessage();
                tempo = (data[3] & 255) << 16 | (data[4] & 255) << 8 | data[5] & 255;

            } else if (midiEvent.getMessage() instanceof ShortMessage) {

                time = (midiEvent.getTick() - tick) * (tempo / 1000.0 / resolution);
                tick = midiEvent.getTick();
                map.put("time", time);
                message.add(map);
            }
        }
        return message;
    }

    public static void play(List<Map<String, Object>> message, double speed) throws InterruptedException {
        System.out.println("Start playing!");

        //播放所有序列  消息以正确的计时生成时间
        long start_time = System.currentTimeMillis();
        double input_time = 0.0;

        for (Map<String, Object> msg : message) {

            input_time += (double) msg.get("time") / speed;

            long playback_time = System.currentTimeMillis() - start_time;
            long current_time = (long) (input_time - playback_time);

            if (current_time > 0) {
                Thread.sleep(current_time);
            }

            if ((int) msg.get("command") == 144 && key.containsKey((int) msg.get("data1"))) {
                robot.keyPress(key.get((int) msg.get("data1")));
                robot.keyRelease(key.get((int) msg.get("data1")));
            }
        }
    }

    public static void main(String[] args) {

        PlatformImpl.startup(() -> {

            Scanner sc = new Scanner(System.in);
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(null);
            try {
                robot = new Robot();
            } catch (AWTException e) {
                e.printStackTrace();
            }

            double speed;
            while (true) {
                System.out.println("Input play speed x (1.0):");
                if (sc.hasNextDouble()) {
                    speed = sc.nextDouble();
                    break;
                } else {
                    System.out.println("You are not entering numbers!");
                    sc.nextLine();
                }
            }

            long sleep;
            while (true) {
                System.out.println("Input sleep time s (s):");
                if (sc.hasNextLong()) {
                    sleep = sc.nextLong();
                    break;
                } else {
                    System.out.println("You are not entering numbers!");
                    sc.nextLine();
                }
            }

            try {
                for (long i = sleep; i > 0; i--) {
                    System.out.println("Play will be start in " + i + " seconds");
                    Thread.sleep(1000L);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                play(init(file), speed);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println("End playback!");
                System.exit(0);
            }
        });

    }
}
