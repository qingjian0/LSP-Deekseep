package dev.deepseek.runtimeprobe;

import android.content.pm.PackageInfo;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

final class HostProfile {
    private HostProfile() {}

    static String describe(XC_LoadPackage.LoadPackageParam param) {
        return "package=" + param.packageName + " process=" + param.processName
                + " versionName=" + safe(param.versionName) + " versionCode=" + param.versionCode;
    }

    static String fromApplication(ClassLoader ignored) {
        return "target=com.deepseek.chat; classloader=" + (ignored == null ? "null" : ignored.getClass().getName())
                + "; channel=unclassified; mapping=runtime-unverified";
    }

    private static String safe(String value) { return value == null ? "unknown" : value; }
}
