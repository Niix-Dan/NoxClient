package com.noxclient.utils.misc;

import com.noxclient.utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.noxclient.NoxClient.mc;

public class Placeholders {
    private static final Pattern pattern = Pattern.compile("(%player%|%username%|%server%)");

    public static String apply(String string) {
        Matcher matcher = pattern.matcher(string);
        StringBuffer sb = new StringBuffer(string.length());

        while (matcher.find()) {
            matcher.appendReplacement(sb, getReplacement(matcher.group(1)));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private static String getReplacement(String placeholder) {
        return switch (placeholder) {
            case "%player%", "%username%" -> mc.getSession().getUsername();
            case "%server%" -> Utils.getWorldName();
            case "%health%" -> String.valueOf(Utils.getPlayerHealth());
            default -> "";
        };
    }
}
