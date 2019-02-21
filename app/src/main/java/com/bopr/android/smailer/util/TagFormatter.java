package com.bopr.android.smailer.util;

import android.content.Context;
import android.content.res.Resources;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

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
    private String pattern;
    private Resources resources;

    public static TagFormatter formatter(String pattern) {
        return new TagFormatter().pattern(pattern);
    }

    public static TagFormatter formatter(String pattern, @NonNull Context context) {
        return new TagFormatter(context).pattern(pattern);
    }
    public static TagFormatter formatter(int patternResourceId, @NonNull Context context) {
        return new TagFormatter(context).patternRes(patternResourceId);
    }

    public static TagFormatter formatter(String pattern, @NonNull Resources resources) {
        return new TagFormatter(resources).pattern(pattern);
    }

    public static TagFormatter formatter(int patternResourceId, @NonNull Resources resources) {
        return new TagFormatter(resources).patternRes(patternResourceId);
    }

    public static TagFormatter formatter(@NonNull Context context) {
        return new TagFormatter(context);
    }

    public TagFormatter() {
    }

    public TagFormatter(@NonNull Resources resources) {
        this.resources = resources;
    }

    public TagFormatter(@NonNull Context context) {
        this(context.getResources());
    }

    public TagFormatter pattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public TagFormatter patternRes(int resourceId) {
        return pattern(resources.getString(resourceId));
    }

    public TagFormatter put(String key, String value) {
        values.remove(key);
        if (!isEmpty(value)) {
            values.put(key, value);
        }
        return this;
    }

    public TagFormatter putRes(String key, int resourceId) {
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

    @NonNull
    @Override
    public String toString() {
        return format();
    }

}
