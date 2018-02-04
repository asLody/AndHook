package andhook.test;

import andhook.lib.HookHelper;
import andhook.lib.HookHelper.Hook;

import android.util.Log;

@SuppressWarnings("all")
public final class Constructor {
    public Constructor() {
        Log.i(AndTest.LOG_TAG, "Original constructor hit, this is " + this);
    }

    @Hook(clazz = Constructor.class, name = "<init>")
    private static void FakeConstructor(final Object objConstructor) {
        Log.i(AndTest.LOG_TAG, "Fake constructor hit, this is " + objConstructor);
        HookHelper.invokeVoidOrigin(objConstructor);
    }

    public static void doHook() {
        // test call constructor
        new Constructor();

        // hook using HookHelper
        HookHelper.applyHooks(Constructor.class);

        // test call constructor
        new Constructor();
    }
}
