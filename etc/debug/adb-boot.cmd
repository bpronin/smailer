:: Launches processing of single phone event.

set device=
::set device=-s emulator-5554

adb %device% shell am broadcast -n com.bopr.android.smailer/.DebugReceiver -a BOOT_COMPLETED

::adb %device% shell am broadcast -n com.bopr.android.smailer/.BootReceiver -a android.intent.action.BOOT_COMPLETED
::adb %device% shell am broadcast -a android.intent.action.BOOT_COMPLETED

exit