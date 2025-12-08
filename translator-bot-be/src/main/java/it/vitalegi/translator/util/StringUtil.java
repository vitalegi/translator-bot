package it.vitalegi.translator.util;

public class StringUtil {
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNotNullOrEmpty(String str) {
        return !isNullOrEmpty(str);
    }

    public static String leftPadding(String str, int len, char character) {
        var strBuilder = new StringBuilder(str);
        while (strBuilder.length() < len) {
            strBuilder.insert(0, character);
        }
        return strBuilder.toString();
    }

}
