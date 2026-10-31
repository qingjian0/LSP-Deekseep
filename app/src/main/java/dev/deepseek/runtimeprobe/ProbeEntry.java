package dev.deepseek.runtimeprobe;

import android.app.Application;
import android.content.Context;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class ProbeEntry implements IXposedHookLoadPackage {
    private static final String TARGET_PACKAGE = "com.deepseek.chat";
    private static final AtomicBoolean ATTACH_HOOKED = new AtomicBoolean(false);
    private static final AtomicBoolean CANDIDATES_INSTALLED = new AtomicBoolean(false);
    private static final ConcurrentHashMap<String, Boolean> HOOKED = new ConcurrentHashMap<>();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!TARGET_PACKAGE.equals(lpparam.packageName)) return;
        SafeTrace.event("load_package", "package=" + lpparam.packageName
                + " process=" + lpparam.processName + " version=" + HostProfile.describe(lpparam));
        if (!ATTACH_HOOKED.compareAndSet(false, true)) return;

        try {
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class,
                    new XC_MethodHook() {
                        @Override protected void afterHookedMethod(MethodHookParam param) {
                            Object app = param.thisObject;
                            if (!(app instanceof Application)) return;
                            Application application = (Application) app;
                            if (!TARGET_PACKAGE.equals(application.getPackageName())) return;
                            installCandidates(application.getClassLoader());
                        }
                    });
            SafeTrace.event("attach_hook", "Application.attach(Context) installed");
        } catch (Throwable t) {
            SafeTrace.failure("attach_hook_failed", t);
        }
    }

    private static void installCandidates(ClassLoader loader) {
        if (!CANDIDATES_INSTALLED.compareAndSet(false, true)) return;
        SafeTrace.event("host_profile", HostProfile.fromApplication(loader));
        hookNamedMethods(loader, "rx3", "g");
        hookNamedMethods(loader, "h60", "b");
        hookNamedMethods(loader, "nw1", "j");
        hookNamedMethods(loader, "nw1", "e");
        hookNamedMethods(loader, "nw1", "a");
        hookNamedMethods(loader, "st1", "A");
    }

    private static void hookNamedMethods(ClassLoader loader, String className, String methodName) {
        try {
            Class<?> type = Class.forName(className, false, loader);
            int found = 0;
            for (Method method : type.getDeclaredMethods()) {
                if (!method.getName().equals(methodName)) continue;
                String signature = signature(method);
                if (HOOKED.putIfAbsent(signature, Boolean.TRUE) != null) continue;
                method.setAccessible(true);
                XposedBridge.hookMethod(method, new MetadataHook(signature));
                found++;
            }
            SafeTrace.event("candidate", className + "#" + methodName
                    + " class=present hooked_overloads=" + found);
            if (found == 0) SafeTrace.event("candidate_note",
                    className + "#" + methodName + " no matching declared method");
        } catch (ClassNotFoundException e) {
            SafeTrace.event("candidate", className + "#" + methodName + " class=absent");
        } catch (Throwable t) {
            SafeTrace.failure("candidate_hook_failed " + className + "#" + methodName, t);
        }
    }

    private static String signature(Method method) {
        StringBuilder out = new StringBuilder();
        out.append(method.getDeclaringClass().getName()).append('#').append(method.getName()).append('(');
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) out.append(',');
            out.append(types[i].getName());
        }
        out.append("):").append(method.getReturnType().getName());
        out.append(Modifier.isStatic(method.getModifiers()) ? " static" : " instance");
        return out.toString();
    }

    private static final class MetadataHook extends XC_MethodHook {
        private final String signature;
        MetadataHook(String signature) { this.signature = signature; }

        @Override protected void beforeHookedMethod(MethodHookParam param) {
            param.setObjectExtra("dsrp_start_ns", System.nanoTime());
            SafeTrace.event("method_enter", signature + " arg_types=" + SafeTrace.argumentTypes(param.args));
        }

        @Override protected void afterHookedMethod(MethodHookParam param) {
            Object started = param.getObjectExtra("dsrp_start_ns");
            long elapsed = started instanceof Long ? System.nanoTime() - (Long) started : -1L;
            if (param.hasThrowable()) {
                SafeTrace.event("method_exit", signature + " throwable="
                        + param.getThrowable().getClass().getName() + " elapsed_ns=" + elapsed);
            } else {
                Object result = param.getResult();
                SafeTrace.event("method_exit", signature + " result_type="
                        + (result == null ? "null" : result.getClass().getName())
                        + " elapsed_ns=" + elapsed);
            }
        }
    }
}
