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

        for (Track track : sequence.getTracks()) {
            long new_time;
            long old_time = 0;
            long last_time = 0;
            for (int j = 0, lent = track.size(); j < lent; j++) {
                MidiEvent midiEvent = track.get(j);
                byte[] data = midiEvent.getMessage().getMessage();
                Map<String, Object> map = JSON.parseObject(JSON.toJSONString(midiEvent.getMessage()));
                if ((int) map.get("length") == 6) {
                    System.out.println(Arrays.toString(data));
                    bpm = Math.floor(60000000 / (double) (((data[3] & 255) << 16) | ((data[4] & 255) << 8) | (data[5] & 255)));
                    System.out.println("bpm = " + bpm);
                }

                new_time = midiEvent.getTick();
                map.put("time", new_time - old_time);
                old_time = new_time;
                map.put("time", (long) map.get("time") + last_time);
                last_time = (long) map.get("time");

                //command:144  note_on   command:128  note_off
                //  ((bpm + 20) / 25)
                if (map.containsKey("command") && ((int) map.get("command") == 144 || (int) map.get("command") == 128)) {
                    map.put("time", Math.round((long) map.get("time") / (bpm / skip)));
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
                    robot.keyRelease(key.get(note));
            for (Integer note : start.get(i))
//                System.out.println(start.get(i));
                robot.keyPress(key.get(note));
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
