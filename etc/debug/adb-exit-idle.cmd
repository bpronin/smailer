:: Force device exit idle mode

set device=
::set device=-s emulator-5554

adb %device% shell dumpsys deviceidle unforce
adb %device% shell dumpsys battery reset

::exit