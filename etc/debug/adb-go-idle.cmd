:: Force device into idle mode
:: See https://developer.android.com/training/monitoring-device-state/doze-standby.html

set device=
::set device=-s emulator-5554

adb %device% shell dumpsys deviceidle force-idle

:: step to IDLE_MAINTENANCE
adb %device% shell dumpsys deviceidle step

:: step to IDLE
adb %device% shell dumpsys deviceidle step

::exit