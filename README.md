# FridaXposedModule

## Why?

Because there are wonderful Frida scripts and utilities that I wanna run as standalone on multiple apps, but I'm too lazy to port them to Xposed.
So instead this Xposed module can automagically inject the Frida gadget dynamic library inside apps and load Frida scripts.

## Known Bugs

Once you inject the Frida gadge inside an application, it will stay there forever until you manually remove it.
Disabling or uninstalling the Xposed module will not restore the application.

To remove the gadget, open an `adb shell` and do the following, making sure to replace `$PACKAGE` with the package you wanna restore:
```
su
cd /data/user/0/$PACKAGE/files/lib/
rm libfrida-gadget.*
```

----

# Original Readme

## What is this
This is an Xposed module for using [Frida](https://github.com/frida/frida) on non-rooted Android devices.

## How to use it
In order to use this Xposed module in a Root-free environment, there are two ways to use this module.
1. Use the [Xpatch](https://github.com/WindySha/Xpatch) tool developed by the author, or its app version Xposed Tool [(click to download)](https://xposed-tool-app.oss-cn-beijing.aliyuncs.com/data/xposed_tool_v2.0.2.apk).
   Repackage the app that needs to use Frida to implant the code that loads the Xposed module. Then uninstall the original app on your device, install the repackaged app, and then install the Xposed module.
2. Launch the original app in a dual-boot environment that supports Xposed, such as [SandVXposed](https://github.com/ganyao114/SandVXposed), then install this Xposed module in the dual-boot environment and enable it;
   This method is not tested, but it works in principle.

## Principle of implementation
[A program to use Frida in a non-Rooted environment](https://windysha.github.io/2020/05/28/%E9%9D%9ERoot%E7%8E%AF%E5%A2%83%E4%B8%8B%E4%BD%BF%E7%94%A8Frida%E7%9A%84%E4%B8%80%E7%A7%8D%E6%96%B9%E6%A1%88/)
