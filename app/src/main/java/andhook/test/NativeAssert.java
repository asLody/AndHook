package andhook.test;

import java.io.File;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

import andhook.ui.MainActivity;

import android.os.Build;

final class NativeAssert {
    static boolean isBlackList() {
        // YunOS
        return new File("/system/lib/libaoc.so").exists()
                || new File("/system/lib64/libaoc.so").exists();
    }

    static void run(final Member m) {
        if (Modifier.isNative(m.getModifiers())) {
            final String msg = "unexpected native flag "
                    + Integer.toHexString(m.getModifiers());
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP
                    || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1
                    || isBlackList()) {
                MainActivity.alert(msg);
            } else {
                throw new RuntimeException(msg);
            }
        }
    }
}
