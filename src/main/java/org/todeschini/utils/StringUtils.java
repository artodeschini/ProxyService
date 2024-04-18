package org.todeschini.utils;

import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static String normalize(String s) {
        s = s.toUpperCase();
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        return s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
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

    public static String removerCaracteresEspeciais(String str) {
        return str.replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ");
    }
    public static String removerAcentos(final String str) {
        final String valor = Normalizer.normalize(str, Normalizer.Form.NFD).trim();
        return removerCaracteresEspeciais(valor.replaceAll("[^\\p{ASCII}]", ""));
        //return valor;
    }

    public static boolean isNotEmpty(String str) {
        if (str == null) {
            return true;
        } else {
            return !str.isEmpty();
        }

    }
}
