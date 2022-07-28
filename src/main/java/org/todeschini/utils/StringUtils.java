package org.todeschini.utils;

import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static String normalize(String s) {
        s = s.toUpperCase();
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

    /**
     * Converte caracteres unicode em char ascii
     * @param text
     * @return
     */
    public static String decode(String text) {
        var p = Pattern.compile("\\\\u(\\p{XDigit}{4})");
        var m = p.matcher(text);
        var buffer = new StringBuilder(text.length());

        while (m.find()) {
            var ch = String.valueOf((char) Integer.parseInt(m.group(1), 16));
            m.appendReplacement(buffer, Matcher.quoteReplacement(ch));
        }
        m.appendTail(buffer);

        return buffer.toString();
    }
}
