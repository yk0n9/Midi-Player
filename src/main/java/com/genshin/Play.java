package com.genshin;

import com.alibaba.fastjson.JSON;
import com.util.KeyMapper;
import lombok.SneakyThrows;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;


/**
 * @author Ykong
 */
public class Play {

    public static List<Integer> find(List<Map<String, Object>> arr, long time) {
        List<Integer> result = new ArrayList<>();
        for (Map<String, Object> map : arr)
            if ((long) map.get("time") == time)
                if (key.containsKey((int) map.get("data1")))
                    result.add((int) map.get("data1"));
        return result;
    }

    private static final Map<Integer, Integer> key = KeyMapper.init();
    private static Robot robot;
    private static double bpm = 0;

    @SneakyThrows
    public static void init(File file, double skip) {

        Sequence sequence = MidiSystem.getSequence(file);
        List<Map<String, Object>> tracks = new ArrayList<>();
        List<Map<String, Object>> end_tracks = new ArrayList<>();
        Map<String, Object> msg;
        MidiEvent midiEvent;
        byte[] data;

        for (Track track : sequence.getTracks()) {
            long new_time;
            long old_time = 0;
            long last_time = 0;
            for (int j = 0, lent = track.size(); j < lent; j++) {
                midiEvent = track.get(j);
                data = midiEvent.getMessage().getMessage();
                msg = JSON.parseObject(JSON.toJSONString(midiEvent.getMessage()));
                if ((int) msg.get("length") == 6) {
                    System.out.println(Arrays.toString(data));
                    bpm = Math.floor(60000000 / (double) (((data[3] & 255) << 16) | ((data[4] & 255) << 8) | (data[5] & 255)));
                    System.out.println("bpm = " + bpm);
                }

                new_time = midiEvent.getTick();
                msg.put("time", new_time - old_time);
                old_time = new_time;
                msg.put("time", (long) msg.get("time") + last_time);
                last_time = (long) msg.get("time");

                //command:144  note_on   command:128  note_off
                if (msg.containsKey("command") && ((int) msg.get("command") == 144 || (int) msg.get("command") == 128)) {
                    msg.put("time", Math.round((long) msg.get("time") / (bpm / skip)));
                    msg.remove("data2");
                    msg.remove("channel");
                    msg.remove("length");
                    msg.remove("message");
                    msg.remove("status");
                    if ((int) msg.get("command") == 144) {
                        msg.remove("command");
                        tracks.add(msg);
                    } else {
                        msg.remove("command");
                        end_tracks.add(msg);
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
            for (Integer note : start.get(i)) {
                robot.keyPress(key.get(note));
                robot.keyRelease(key.get(note));
            }
            Thread.sleep(25);
        }
    }

    @SneakyThrows
    public static void main(String[] args) {

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        robot = new Robot();
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jFileChooser.showOpenDialog(null);

        System.out.println("Input skip : (Default : 4.8)");
        double skip = new Scanner(System.in).nextDouble();

        Play.init(jFileChooser.getSelectedFile(), skip);
    }
}
