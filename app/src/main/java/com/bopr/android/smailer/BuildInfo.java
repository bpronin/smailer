package com.bopr.android.smailer;

import android.content.Context;

import java.io.InputStream;
import java.util.Properties;

public class BuildInfo {

    public final String number;
    public final String time;
    public final String name;

    public BuildInfo(Context context) {
        Properties properties = new Properties();
        try {
            try (InputStream stream = context.getAssets().open("release.properties")) {
                properties.load(stream);
                number = properties.getProperty("build_number");
                time = properties.getProperty("build_time");
            }
            name = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception x) {
            throw new RuntimeException("Cannot read build info", x);
        }
    }

}
