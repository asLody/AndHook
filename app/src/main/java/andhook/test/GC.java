package andhook.test;

import andhook.ui.MainActivity;

public final class GC {
    public static void test() {
        MainActivity.clear();
        MainActivity.output("GC test...");

        try {
            final Runtime run = Runtime.getRuntime();
            MainActivity.output("maxMemory: " + run.maxMemory());
            MainActivity.output("totalMemory: " + run.totalMemory());
            MainActivity.output("freeMemory: " + run.freeMemory());

            MainActivity.output("    ");

            // System.gc();
            run.gc();

            MainActivity.output("maxMemory: " + run.maxMemory());
            MainActivity.output("totalMemory: " + run.totalMemory());
            MainActivity.output("freeMemory: " + run.freeMemory());
        } catch (final Throwable t) {
            MainActivity.alert(t);
            return;
        }

        MainActivity.info("GC test passed");
    }
}
