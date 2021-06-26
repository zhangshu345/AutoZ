package com.stardust.autojs.core.floaty;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.stardust.autojs.core.accessibility.AccessibilityService;
import com.stardust.autojs.core.pref.Pref;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 *idea 悬浮窗服务
 */

public class FloatyService extends Service {

    private static CopyOnWriteArraySet<FloatyWindow> windows = new CopyOnWriteArraySet<>();

    public static void addWindow(FloatyWindow window) {
        if (windows.add(window) && instance != null) {
            if(instance.isSystemFloat&&instance.mAccessibilityWindowManager!=null){
                window.onCreate(instance, instance.mAccessibilityWindowManager);
            }else{
                window.onCreate(instance, instance.mWindowManager);
            }
        }
    }

    public static void addSystemWindow(FloatyWindow window,boolean isSystemFloat) {
        if (windows.add(window) && instance != null) {
            if(isSystemFloat){
                if(instance.mAccessibilityWindowManager!=null){
                    window.onCreate(instance, instance.mAccessibilityWindowManager);
                }else{
                    instance.mAccessibilityWindowManager = AccessibilityService.Companion.getInstance()!=null? (WindowManager) AccessibilityService.Companion.getInstance().getSystemService(WINDOW_SERVICE) :null;
                    if(instance.mAccessibilityWindowManager!=null){
                        window.onCreate(instance, instance.mAccessibilityWindowManager);
                    }else {
                        window.onCreate(instance, instance.mWindowManager);
                    }
                }

            }else{
                window.onCreate(instance, instance.mWindowManager);
            }
        }
    }

    public static void setSystemWindowManager(WindowManager windowManager){
        if(instance!=null){
            instance.mAccessibilityWindowManager=windowManager;
        }
    }


    //隐藏所有悬浮窗
    public static void hideAllWindows(){
        for (FloatyWindow delegate : windows) {
            delegate.hide();
        }
    }

    public static void showAllWindows(){
        for (FloatyWindow delegate : windows) {
            delegate.show();
        }
    }

    public static void  dismissAllWindows(){
        for (FloatyWindow delegate : windows) {
            delegate.dismiss();
        }
    }

    public static void removeWindow(FloatyWindow window) {
        windows.remove(window);
    }

    private static FloatyService instance;

    private WindowManager mWindowManager;
    private WindowManager mAccessibilityWindowManager;
    public  boolean isSystemFloat;

    @Override
    public void onCreate() {
        super.onCreate();
        isSystemFloat= Pref.INSTANCE.isSystemFloat();
        mWindowManager =   (WindowManager) getSystemService(WINDOW_SERVICE);
        mAccessibilityWindowManager = AccessibilityService.Companion.getInstance()!=null? (WindowManager) AccessibilityService.Companion.getInstance().getSystemService(WINDOW_SERVICE) :null;
        if(isSystemFloat&&mAccessibilityWindowManager==null){
           for (FloatyWindow delegate : windows) {
              delegate.onCreate(this, mWindowManager);
            }
        }else{
            for (FloatyWindow delegate : windows) {
                delegate.onCreate(this, mWindowManager);
            }
        }
        instance = this;
    }




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        for (FloatyWindow delegate : windows) {
            delegate.onServiceDestroy(this);
        }
    }

}
