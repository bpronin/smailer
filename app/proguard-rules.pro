# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Tools\android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#-ignorewarnings
#-dontobfuscate

# Logback
-keepattributes *Annotation*
-keep class ch.qos.** { *; }
-keep class org.slf4j.** { *; }
-dontwarn ch.qos.logback.core.net.*

# Google API client
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault,*Annotation*
-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}
-dontwarn sun.misc.Unsafe

# Java mail (used to build email messages)
-keep public class * extends javax.mail.Provider

# Application
-keepattributes SourceFile, LineNumberTable
-keep public class com.bopr.android.smailer.ui.DebugActionProvider
-keepclassmembers public class com.bopr.android.smailer.ui.DebugActionProvider {
    public <init>(...);
}
