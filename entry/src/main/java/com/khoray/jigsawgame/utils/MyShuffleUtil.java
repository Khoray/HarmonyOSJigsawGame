package com.khoray.jigsawgame.utils;

import java.util.*;

public class MyShuffleUtil {
    public static void shuffle(int[] a, int n) {
        Random random = new Random();
        List<Integer> ls = new ArrayList<>();
        for(int i = 0; i < a.length; i++) {
            ls.add(a[i]);
        }

        while(true) {
            Collections.shuffle(ls);
            int inverseNum = 0;
            int posBlank = 0;
            for(int i = 1; i < ls.size(); i++) {
                if(ls.get(i) == n * n - 1) {
                    posBlank = i;
                    continue;
                }
                for(int j = 0; j < i; j++) {
                    if(ls.get(j) == n * n - 1) continue;
                    if(ls.get(j) > ls.get(i)) {
                        inverseNum++;
                    }
                }
            }
            if(n % 2 == 1) {
                if(inverseNum % 2 == 0) {
                    break;
                }
            } else {
                if(inverseNum % 2 == 0) {
                    if((posBlank / n - n + 1) % 2 == 0) {
                        break;
                    }
                } else {
                    if((posBlank / n - n + 1) % 2 == 1) {
                        break;
                    }
                }
            }

        }
        for(int i = 0; i < a.length; i++) {
            a[i] = ls.get(i);
        }

    }
}
