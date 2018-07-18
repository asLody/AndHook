-dontwarn andhook.lib.**
-dontwarn andhook.lib.xposed.**
-keep class andhook.lib.AndHook {
    native <methods>;
}
-keep class andhook.lib.AndHook$Dalvik{
    native <methods>;
}
-keep class andhook.lib.YunOSHelper{*;}
-keep class andhook.lib.xposed.XposedBridge{*;}

-dontwarn andhook.test.**
-keep class andhook.test.**{*;}