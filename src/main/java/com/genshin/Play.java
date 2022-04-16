package com.genshin;

import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.swing.JFileChooser;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Play {

    public static List<Integer> find(List<Map<String, Object>> arr, long time) {
        List<Integer> result = new ArrayList<>();
        for (Map<String, Object> map : arr)
            if ((long) map.get("time") == time)
                result.add((int) map.get("data1"));
        return result;
    }

    public static Map<Integer, Integer> key = new HashMap<>();
    public static Robot robot;
    public static double tempo = 0;

    @SneakyThrows
    public static void init(String file) {
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

        Sequence sequence = MidiSystem.getSequence(new File(file));
        boolean flag = false;
        List<Map<String, Object>> tracks = new ArrayList<>();
        List<Map<String, Object>> end_tracks = new ArrayList<>();

        for (Track track : sequence.getTracks()) {
            for (int i = 0, len = track.size(); i < len; i++) {
                MidiEvent midiEvent = track.get(i);
                Map<String, Object> msg = JSON.parseObject(JSON.toJSONString(midiEvent.getMessage()));
                byte[] data = midiEvent.getMessage().getMessage();
                if ((int) msg.get("length") == 6) {
                    tempo = ((data[3] & 255) << 16) | ((data[4] & 255) << 8) | (data[5] & 255);
                    flag = true;
                }
            }
            if (flag)
                break;
        }

        double speed = 60000000 / tempo / 4.8;

        for (Track track : sequence.getTracks()) {
            long new_time;
            long old_time = 0;
            long last_time = 0;
            for (int j = 0, lent = track.size(); j < lent; j++) {
                MidiEvent midiEvent = track.get(j);
                Map<String, Object> map = JSON.parseObject(JSON.toJSONString(midiEvent.getMessage()));

                new_time = midiEvent.getTick();
                map.put("time", new_time - old_time);
                old_time = new_time;
                map.put("time", (long) map.get("time") + last_time);
                last_time = (long) map.get("time");

                //command:144  note_on   command:128  note_off
                if (map.containsKey("command") && ((int) map.get("command") == 144 || (int) map.get("command") == 128)) {
                    map.put("time", Math.round((long) map.get("time") / speed));
                    map.remove("data2");
                    map.remove("channel");
                    map.remove("length");
                    map.remove("message");
                    map.remove("status");
                    if ((int) map.get("command") == 144) {
                        map.remove("command");
                        tracks.add(map);
                    } else {
                        map.remove("command");
                        end_tracks.add(map);
                    }
                }
            }
        }

        long max = 0;
        for (Map<String, Object> map : end_tracks)
            max = Math.max(max, ((long) map.get("time") + 1));

        Map<Integer, List<Integer>> start = new HashMap<>();
        for (int i = 0; i < max; i++)
            start.put(i, find(tracks, i));

        System.out.println("Play will be start in 3 seconds");
        Thread.sleep(3000);

        for (int i = 0; i < max; i++) {
            if (i != 0)
                for (Integer note : start.get(i - 1))
                    if (key.containsKey(note))
                        robot.keyRelease(key.get(note));
            for (Integer note : start.get(i))
                if (key.containsKey(note))
                    robot.keyPress(key.get(note));
            Thread.sleep(25);
        }
    }

    @SneakyThrows
    public static void main(String[] args) {

        robot = new Robot();
        JFileChooser jFileChooser = new JFileChooser();

        jFileChooser.showOpenDialog(null);

        Play.init(jFileChooser.getSelectedFile().getAbsolutePath());
    }
}
