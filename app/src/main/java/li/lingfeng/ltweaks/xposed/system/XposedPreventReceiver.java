package li.lingfeng.ltweaks.xposed.system;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageParser;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.Proxy;
import android.net.wifi.WifiManager;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import li.lingfeng.ltweaks.R;
import li.lingfeng.ltweaks.lib.XposedLoad;
import li.lingfeng.ltweaks.prefs.ClassNames;
import li.lingfeng.ltweaks.prefs.IntentActions;
import li.lingfeng.ltweaks.prefs.PackageNames;
import li.lingfeng.ltweaks.utils.Logger;

/**
 * Created by smallville on 2017/3/27.
 */
@XposedLoad(packages = PackageNames.ANDROID, prefs = R.string.key_prevent_running_prevent_receiver)
public class XposedPreventReceiver extends XposedPreventRunning {

    private Field mFieldActions;

    @Override
    protected void handleLoadPackage() throws Throwable {
        super.handleLoadPackage();
        mFieldActions = IntentFilter.class.getDeclaredField("mActions");
        mFieldActions.setAccessible(true);

        hookAllMethods(ClassNames.ACTIVITY_MANAGER_SERVICE, "registerReceiver", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String callerPackage = (String) param.args[1];
                if (!sPreventList.contains(callerPackage)) {
                    return;
                }
                IntentFilter filter = (IntentFilter) param.args[3];
                filterActions(filter, callerPackage);
                if (filter.countActions() == 0) {
                    param.setResult(null);
                }
            }
        });

        hookAllMethods(ClassNames.PACKAGE_PARSER, "parseActivity", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                PackageParser.Package owner = (PackageParser.Package) param.args[0];
                if ((boolean) param.args[param.args.length - 2] == false || !sPreventList.contains(owner.packageName)) {
                    return;
                }
                PackageParser.Activity activity = (PackageParser.Activity) param.getResult();
                for (IntentFilter filter : activity.intents) {
                    filterActions(filter, owner.packageName);
                }
            }
        });
    }

    private void filterActions(IntentFilter filter, String packageName) throws Throwable {
        ArrayList<String> actions = (ArrayList<String>) mFieldActions.get(filter);
        for (int i = actions.size() - 1; i >= 0; --i) {
            String action = actions.get(i);
            if (IntentActions.sReceiverPrevented.contains(action)) {
                Logger.i("Prevent receiver action " + action + " from " + packageName);
                actions.remove(i);
            } else {
                Logger.d("Pass receiver action " + action);
            }
        }
    }
}
