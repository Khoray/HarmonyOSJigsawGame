package com.khoray.jigsawgame.utils;

public class TimeToStrUtil {
    static public String t2s(int time) {
        StringBuilder sb = new StringBuilder();
        int s = time % 60;
        int m = time / 60;
        if(m < 10) {
            sb.append("0" + m);
        } else {
            sb.append(m);
        }
        sb.append(":");
        if(s < 10) {
            sb.append("0" + s);
        } else {
            sb.append(s);
        }
        return sb.toString();
    }
}
