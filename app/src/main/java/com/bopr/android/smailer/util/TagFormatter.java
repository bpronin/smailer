package com.bopr.android.smailer.util;

import android.content.Context;
import android.content.res.Resources;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bopr.android.smailer.util.Util.isEmpty;

/**
 * Tagged string formatter.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class TagFormatter {

    private static final String OPEN_BRACKET = "\\{";
    private static final String CLOSE_BRACKET = "\\}";

    private final Map<String, String> values = new LinkedHashMap<>();
    private final String pattern;
    private final Resources resources;

    public static TagFormatter formatter(String pattern) {
        return new TagFormatter(pattern);
    }

    public static TagFormatter formatter(String pattern, Resources resources) {
        return new TagFormatter(pattern, resources);
    }

    public static TagFormatter formatter(String pattern, Context context) {
        return formatter(pattern, context.getResources());
    }

    public static TagFormatter formatter(int patternResourceId, Resources resources) {
        return new TagFormatter(patternResourceId, resources);
    }

    public static TagFormatter formatter(int patternResourceId, Context context) {
        return formatter(patternResourceId, context.getResources());
    }

    public TagFormatter(String pattern, Resources resources) {
        this.pattern = pattern;
        this.resources = resources;
    }

    public TagFormatter(int patternResourceId, Resources resources) {
        this(resources.getString(patternResourceId), resources);
    }

    public TagFormatter(String pattern) {
        this(pattern, null);
    }

    public TagFormatter put(String key, String value) {
        values.remove(key);
        if (!isEmpty(value)) {
            values.put(key, value);
        }
        return this;
    }

    public TagFormatter put(String key, int resourceId) {
        return put(key, resources.getString(resourceId));
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
        String result = pattern;

        Matcher matcher = Pattern.compile(OPEN_BRACKET + "(.*?)" + CLOSE_BRACKET).matcher(pattern);
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
