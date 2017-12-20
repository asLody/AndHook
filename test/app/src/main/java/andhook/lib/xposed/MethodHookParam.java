package andhook.lib.xposed;

/**
 * Wraps information about the method call and allows to influence it.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class MethodHookParam {
    MethodHookParam(final int slot, final Object thisObject, final Object[] args) {
        this.methodId = slot;
        this.thisObject = thisObject;
        this.args = args;
    }

    /**
     * The backuped method/constructor.
     */
    public int methodId;

    /**
     * The {@code this} reference for an instance method, or {@code null} for
     * static methods.
     */
    public Object thisObject;

    /**
     * Arguments to the method call.
     */
    public Object[] args;

    public boolean hasResult = false;
    private Object result = null;
    private Throwable throwable = null;

    /**
     * Returns the result of the method call.
     */
    public Object getResult() {
        return result;
    }

    /**
     * Modify the result of the method call.
     * <p>
     * <p>
     * If called from {@link XC_MethodHook#beforeHookedMethod}, it prevents the call to the
     * original method.
     */
    public void setResult(final Object result) {
        this.hasResult = true;
        this.result = result;
        this.throwable = null;
    }

    /**
     * Returns the {@link Throwable} thrown by the method, or {@code null}.
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Returns true if an exception was thrown by the method.
     */
    public boolean hasThrowable() {
        return throwable != null;
    }

    /**
     * Modify the exception thrown of the method call.
     * <p>
     * <p>
     * If called from {@link XC_MethodHook#beforeHookedMethod}, it prevents the call to the
     * original method.
     */
    public void setThrowable(final Throwable throwable) {
        this.throwable = throwable;
        this.result = null;
    }

    /**
     * Returns the result of the method call, or throws the Throwable caused by
     * it.
     */
    public Object getResultOrThrowable() throws Throwable {
        if (throwable != null)
            throw throwable;
        return result;
    }
}
