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

    public static List find(java.util.List<Map> arr, long time) {
        List<Integer> result = new ArrayList();
        for (Map map : arr) {
            if ((int) map.get("time") == time)
                result.add((int) map.get("data1"));
        }
        return result;
    }

    public static HashMap<Integer, Integer> key = new HashMap() {
        {
            put(48, KeyEvent.VK_Z);
            put(50, KeyEvent.VK_X);
            put(52, KeyEvent.VK_C);
            put(53, KeyEvent.VK_V);
            put(55, KeyEvent.VK_B);
            put(57, KeyEvent.VK_N);
            put(59, KeyEvent.VK_M);
            put(60, KeyEvent.VK_A);
            put(62, KeyEvent.VK_S);
            put(64, KeyEvent.VK_D);
            put(65, KeyEvent.VK_F);
            put(67, KeyEvent.VK_G);
            put(69, KeyEvent.VK_H);
            put(71, KeyEvent.VK_J);
            put(72, KeyEvent.VK_Q);
            put(74, KeyEvent.VK_W);
            put(76, KeyEvent.VK_E);
            put(77, KeyEvent.VK_R);
            put(79, KeyEvent.VK_T);
            put(81, KeyEvent.VK_Y);
            put(83, KeyEvent.VK_U);
        }
    };

    public static Robot robot;
    static long skip = 0;
    static double speed = 0;

    @SneakyThrows
    public static void init(String file) {

        Sequence sequence = MidiSystem.getSequence(new File(file));
        boolean flag = false;
        List<Map> tracks = new ArrayList<>();
        List<Map> end_tracks = new ArrayList<>();

        for (Track track : sequence.getTracks()) {
            for (int i = 0, len = track.size(); i < len; i++) {
                MidiEvent midiEvent = track.get(i);
                Map<String, Object> msg = JSON.parseObject(JSON.toJSONString(midiEvent.getMessage()));
                byte[] data = midiEvent.getMessage().getMessage();
                if ((int) msg.get("length") == 6) {
                    skip = ((data[3] & 0xFF) << 16) | ((data[4] & 0xFF) << 8) | (data[5] & 0xFF);
                    flag = true;
                }
            }
            if (flag) {
                break;
            }
        }

        for (Track track : sequence.getTracks()) {
            long new_time;
            long old_time = 0;
            long last_time = 0;
            for (int j = 0, lent = track.size(); j < lent; j++) {
                MidiEvent midiEvent = track.get(j);
                Map<String, Object> map = JSON.parseObject(JSON.toJSONString(midiEvent.getMessage()));

                speed = 60000000 / skip / 4.8;

                new_time = midiEvent.getTick();
                map.put("time", new_time - old_time);
                old_time = last_time;

                map.put("time", (long) map.get("time") + last_time);

                //command:144  note_on   command:128  note_off
                if (map.containsKey("command") && ((int) map.get("command") == 144 || (int) map.get("command") == 128)) {
                    map.put("time", Math.round((long) map.get("time") / (long) speed));
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
        for (Map map : end_tracks) {
            max = Math.max(max, ((int) map.get("time") + 1));
        }

        Map<String, List<Integer>> start = new HashMap();
        for (int i = 0; i < max; i++) {
            start.put("" + i, find(tracks, i));
        }

        for (int i = 0; i < max; i++) {
            if (i != 0)
                for (Integer note : start.get(String.valueOf(i - 1)))
                    if (key.containsKey(note))
                        robot.keyRelease(key.get(note));
            for (Integer note : start.get(String.valueOf(i)))
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

        System.out.println("3秒后开始");

        Thread.sleep(3000);
        Play.init(jFileChooser.getSelectedFile().getAbsolutePath());
    }
}
