package com.stardust.autojs.util;
import java.lang.reflect.Field;

/**
 *
 */

public class ProcessUtils {
    private static final String LOG_TAG = "ProcessUtils";

    public static int getProcessPid(Process process) {
        try {
            Field pid = process.getClass().getDeclaredField("pid");
            pid.setAccessible(true);
            return (int) pid.get(process);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

}
