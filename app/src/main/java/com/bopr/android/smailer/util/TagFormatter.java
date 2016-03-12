package com.bopr.android.smailer.util;

import android.content.res.Resources;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tagged string formatter.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
//todo: optional tags markers. like [Title: {key}]
public class TagFormatter {

    private static final String OPEN_BRACKET = "\\{";
    private static final String CLOSE_BRACKET = "\\}";

    private final Map<String, String> values = new LinkedHashMap<>();
    private final String format;
    private final Resources resources;

    public static TagFormatter from(String format) {
        return new TagFormatter(format);
    }

    public static TagFormatter from(String format, Resources resources) {
        return new TagFormatter(format, resources);
    }

    public static TagFormatter from(int formatResourceId, Resources resources) {
        return new TagFormatter(formatResourceId, resources);
    }

    public TagFormatter(String format, Resources resources) {
        this.format = format;
        this.resources = resources;
    }

    public TagFormatter(int formatResourceId, Resources resources) {
        this(resources.getString(formatResourceId), resources);
    }

    public TagFormatter(String format) {
        this(format, null);
    }

    public TagFormatter put(String key, Object value) {
        values.remove(key);
        if (value != null) {
            String s = value.toString();
            if (!s.isEmpty()) {
                values.put(key, s);
            }
        }
        return this;
    }

    public TagFormatter putResource(String tag, int resourceId) {
        return put(tag, resources.getString(resourceId));
    }

    public TagFormatter putList(String key, String separator, Object... values) {
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            if (value != null) {
                list.append(value);
                if (i < values.length - 1) {
                    list.append(separator);
                }
            }
        }
        return put(key, list.toString());
    }

    public String format() {
        String result = format;

        Matcher matcher = Pattern.compile(OPEN_BRACKET + "(.*?)" + CLOSE_BRACKET).matcher(format);
        while (matcher.find()) {
            String tag = matcher.group(1);
            String value = values.get(tag);
            result = result.replaceAll(OPEN_BRACKET + tag + CLOSE_BRACKET, (value != null) ? value : "");
        }

        return result;
    }

    @Override
    public String toString() {
        return format();
    }
}
