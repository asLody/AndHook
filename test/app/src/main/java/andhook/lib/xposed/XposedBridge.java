package andhook.lib.xposed;

import andhook.lib.AndHook;

import java.lang.reflect.Member;

final class XposedBridge {
    static XC_MethodHook.Unhook hookMethod(final Member m, final XC_MethodHook callback) {
        if (m == null || callback == null) return null;
        callback.slot = AndHook.backup(m);
        if (callback.slot == -1 || !AndHook.hook(m, callback, callback.slot))
            return null;
        return new XC_MethodHook.Unhook(callback, m);
    }
}