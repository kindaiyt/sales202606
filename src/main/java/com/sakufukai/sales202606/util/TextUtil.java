package com.sakufukai.sales202606.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {

    // http/https のURLを検出
    private static final Pattern URL_PATTERN =
            Pattern.compile("(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)");

    public static String linkify(String text) {
        if (text == null) return "";

        Matcher m = URL_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            String url = m.group(1);

            String link = "<a href=\"" + url +
                    "\" target=\"_blank\" rel=\"noopener noreferrer\">" +
                    url + "</a>";

            m.appendReplacement(sb, link);
        }

        m.appendTail(sb);

        // 改行も表示
        return sb.toString().replace("\n", "<br>");
    }
}
