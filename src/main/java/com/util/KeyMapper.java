package com.util;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class KeyMapper {

    private static final Map<Integer, Integer> key = new HashMap<>();

    public static Map<Integer, Integer> init() {
        key.put(24, KeyEvent.VK_Z);
        key.put(26, KeyEvent.VK_X);
        key.put(28, KeyEvent.VK_C);
        key.put(29, KeyEvent.VK_V);
        key.put(31, KeyEvent.VK_B);
        key.put(33, KeyEvent.VK_N);
        key.put(35, KeyEvent.VK_M);
        key.put(36, KeyEvent.VK_Z);
        key.put(38, KeyEvent.VK_X);
        key.put(40, KeyEvent.VK_C);
        key.put(41, KeyEvent.VK_V);
        key.put(43, KeyEvent.VK_B);
        key.put(45, KeyEvent.VK_N);
        key.put(47, KeyEvent.VK_M);
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
        key.put(84, KeyEvent.VK_Q);
        key.put(86, KeyEvent.VK_W);
        key.put(88, KeyEvent.VK_E);
        key.put(89, KeyEvent.VK_R);
        key.put(91, KeyEvent.VK_T);
        key.put(93, KeyEvent.VK_Y);
        key.put(95, KeyEvent.VK_U);

        return key;
    }
}
