package dev.deepseek.runtimeprobe;

import android.content.pm.ApplicationInfo;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

final class HostProfile {
    private HostProfile() {}

    static String describe(XC_LoadPackage.LoadPackageParam param) {
        ApplicationInfo appInfo = param.appInfo;
        if (appInfo == null) {
            return "package=" + safe(param.packageName)
                    + " process=" + safe(param.processName)
                    + " appInfo=unavailable";
        }
        return "package=" + safe(param.packageName)
                + " process=" + safe(param.processName)
                + " uid=" + appInfo.uid
                + " targetSdk=" + appInfo.targetSdkVersion
                + " sourceDir=" + safe(appInfo.sourceDir);
    }

    static String fromApplication(ClassLoader ignored) {
        return "target=com.deepseek.chat; classloader="
                + (ignored == null ? "null" : ignored.getClass().getName())
                + "; channel=unclassified; mapping=runtime-unverified";
    }

    private static String safe(String value) {
        return value == null ? "unknown" : value;
    }
}
