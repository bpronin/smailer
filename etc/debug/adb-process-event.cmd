:: Launches processing of single phone event.

set device=
::set device=-s emulator-5554

adb %device% shell am broadcast -n com.bopr.android.smailer/.DebugReceiver -a PROCESS_PHONE_EVENT

exit