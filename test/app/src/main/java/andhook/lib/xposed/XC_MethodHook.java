package andhook.lib.xposed;

import java.lang.reflect.Member;

import andhook.lib.AndHook;

public class XC_MethodHook {
    /**
     * Called before the invocation of the method.
     * <p>
     * <p>
     * You can use {@link MethodHookParam#setResult} and
     * {@link MethodHookParam#setThrowable} to prevent the original method from
     * being called.
     * <p>
     * <p>
     * Note that implementations shouldn't call {@code super(param)}, it's not
     * necessary.
     *
     * @param param Information about the method call.
     * @throws Throwable Everything the callback throws is caught and logged.
     */
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    }

    /**
     * Called after the invocation of the method.
     * <p>
     * <p>
     * You can use {@link MethodHookParam#setResult} and
     * {@link MethodHookParam#setThrowable} to modify the return value of the
     * original method.
     * <p>
     * <p>
     * Note that implementations shouldn't call {@code super(param)}, it's not
     * necessary.
     *
     * @param param Information about the method call.
     * @throws Throwable Everything the callback throws is caught and logged.
     */
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
    }

    /**
     * An object with which the method/constructor can be unhooked.
     */
    public static final class Unhook {
        private XC_MethodHook callback;
        private Member method;

        Unhook(final XC_MethodHook callback, final Member method) {
            this.callback = callback;
            this.method = method;
        }

        /**
         * Returns the method/constructor that has been hooked.
         */
        @SuppressWarnings("unused")
        public Member getHookedMethod() {
            return this.method;
        }

        @SuppressWarnings("unused")
        public XC_MethodHook getCallback() {
            return this.callback;
        }

        @SuppressWarnings("all")
        public boolean unhook() {
            return AndHook.restore(this.callback.slot, this.method);
        }
    }

    /**
     * Backup method slot.
     */
    int slot;

    /**
     * Method bridge.
     */
    @SuppressWarnings("unused")
    private static Object bridge(final Object thiz, final Object receiver, final Object[] args)
            throws Throwable {
        final XC_MethodHook xc = (XC_MethodHook) thiz;
        final MethodHookParam param = new MethodHookParam(xc.slot, receiver, args);
        xc.beforeHookedMethod(param);
        if (!param.hasThrowable() && !param.hasResult) {
            param.setResult(AndHook.invokeMethod(xc.slot, receiver, param.args));
            xc.afterHookedMethod(param);
        }
        if (param.hasThrowable())
            throw param.getThrowable();
        return param.getResult();
    }
}
