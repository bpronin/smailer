:: Launches processing of single phone event.
:: Useful to test the app on physical device  when it is dozing.
:: See https://developer.android.com/training/monitoring-device-state/doze-standby.html

adb shell am broadcast -n com.bopr.android.smailer/.DebugReceiver -a PROCESS_EVENT
exit