package org.todeschini.utils;

import java.text.Normalizer;

public class StringUtils {

    public static String normalize(String s) {
        s = s.toUpperCase();
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }
}
