package com.wind.frida.xposed;

import static com.wind.frida.xposed.BuildConfig.APPLICATION_ID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import com.wind.frida.xposed.helper.NativeLibraryHelperExt;
import com.wind.frida.xposed.utils.AppUtils;

import java.io.File;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@SuppressLint("UnsafeDynamicallyLoadedCode")
public class XposedEntry implements IXposedHookLoadPackage {
    private static final String TAG = "FridaXposed.XposedEntry";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(APPLICATION_ID)) {
            return;
        }

        boolean isSystemApp;
        if (lpparam.appInfo != null) {
            isSystemApp = (lpparam.appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } else {
            isSystemApp = true;
        }

        if (isSystemApp) {
            Log.w(TAG, "handleLoadPackage, but this is systemApp, pkg = " + lpparam.packageName);
            return;
        }

        if (AppUtils.classNameContainedInStackTrace("com.elderdrivers.riru.edxp._hooker.impl.LoadedApkGetCL")) {
            //  use EdXposed may cause java.lang.StackOverflowError  in AppUtils.createAppContext();
            Log.e(TAG, " createAppContext failed, classNameContainedInStackTrace  pkg = " + lpparam.packageName);
            return;
        }

        Context context = AppUtils.createAppContext();
        if (context == null) {
            Log.e(TAG, " createAppContext failed, context is null !!!  pkg = " + lpparam.packageName);
            return;
        }

        String processName = AppUtils.getCurrentProcessName(context);
        boolean isMainProcess = context.getPackageName().equals(processName);

        Log.d(TAG, String.format(" handleLoadPackage, packageName = %s , isMainProcess = %s  processName = %s",
                context.getPackageName(), isMainProcess, processName));

        String dataFilePath = context.getFilesDir().getAbsolutePath();
        AppUtils.ensurePathExist(dataFilePath);

        String libPath = dataFilePath + File.separator + "lib";
        AppUtils.ensurePathExist(libPath);

        String soFilePath = libPath + File.separator + AppUtils.FRIDA_SO_FILE_NAME;
        File soFile = new File(soFilePath);
        String pluginPath = AppUtils.getLocalPluginPath();
        String configPath = AppUtils.getLocalConfigPath();

        // only copy the so file on main process
        if (isMainProcess) {
            if (pluginPath.equals("") || !soFile.exists()) {
                // we copy from the APK only if the update path is empty OR if the soFile does not exists
                pluginPath = AppUtils.getPluginApkPath();
            }
            Log.d(TAG, pluginPath + " --> " + libPath);
            NativeLibraryHelperExt.copyNativeBinaries(new File(pluginPath), new File(libPath));
            new File(pluginPath).renameTo(new File(pluginPath + ".copy"));

            if (!configPath.equals("")) {
                // we also copy the config file if it is present
                AppUtils.copyFile(configPath, libPath + File.separator + AppUtils.FRIDA_CONFIG_FILE_NAME);
            }
        }

        Log.i(TAG, " handleLoadPackage pluginPath = " + pluginPath + " pluginPath exist = "
                + (new File(pluginPath)).exists() + " soFilePath = " + soFilePath + " soFilePath exist = "
                + soFile.exists() + " configPath = " + configPath + " configPath exist = " + (new File(configPath)).exists() );

        if (soFile.exists()) {
            try {
                System.load(soFilePath);
            } catch (UnsatisfiedLinkError ex) {
                Log.e(TAG, String.format(" load so file %s faied", soFilePath), ex);
            }
        } else {
            Log.e(TAG, String.format(" try to load so file %s, but it it not exist.", soFilePath));
        }
    }
}
