package com.bopr.android.smailer.util;

import android.content.Context;
import android.content.res.Resources;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

/**
 * Tagged string formatter.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class TagFormatter {

    @SuppressWarnings("RegExpRedundantEscape") /* "\\}" works differently in android and pure java */
    private static final Pattern PATTERN = Pattern.compile("\\{(.*?)\\}");

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
        return formatter(context).pattern(patternResourceId);
    }

    public static TagFormatter formatter(int patternResourceId, @NonNull Resources resources) {
        return new TagFormatter(resources).pattern(patternResourceId);
    }

    public static TagFormatter formatter(@NonNull Context context) {
        return new TagFormatter(context);
    }

    public static TagFormatter formatter(@NonNull Resources resources) {
        return new TagFormatter(resources);
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

    public TagFormatter pattern(int resourceId) {
        return pattern(resources.getString(resourceId));
    }

    public TagFormatter put(String key, String value) {
        values.put(key, value);
        return this;
    }

    public TagFormatter put(String key, int resourceId) {
        return put(key, resources.getString(resourceId));
    }

    public String format() {
        StringBuffer sb = new StringBuffer();

        Matcher matcher = PATTERN.matcher(pattern);
        while (matcher.find()) {
            String replacement = values.get(matcher.group(1));
            matcher.appendReplacement(sb, replacement != null ? replacement : "");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    @NonNull
    @Override
    public String toString() {
        return format();
    }

}
