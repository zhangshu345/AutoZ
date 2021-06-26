package com.stardust.autojs.core.floaty;

import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.CallSuper;

import com.stardust.autojs.core.floaty.gesture.DragGesture;


/**
 *悬浮窗口
 */

public abstract class FloatyWindow {
    private WindowManager mWindowManager;
    private FloatyService mFloatyService;
    private WindowBridge mWindowBridge;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private View mWindowView;
    protected DragGesture mDragGesture;
    private  boolean dragenable=false;
    private  boolean canOverNotificationBar;
    private  boolean isclose  = false;
    private boolean focusable = false;

    // 从服务中传入 服务 和窗口管理器  创建这个开始的时候
    @CallSuper
    public void onCreate(FloatyService service, WindowManager manager) {
        mFloatyService = service;
        mWindowManager = manager;
        onCreateWindow(service, manager);
    }

    public int getWindowType(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          return   WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
          return   WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
    }

//所有函数的调用在一起 添加布局到窗口 屏幕
    protected void onCreateWindow(FloatyService service, WindowManager manager) {
        setWindowLayoutParams(onCreateWindowLayoutParams());
        setWindowView(onCreateView(service));
        setWindowBridge(onCreateWindowBridge(getWindowLayoutParams()));
        onViewCreated(getWindowView());
        //attach to window
        attachToWindow(getWindowView(), manager);
    }


    public void setDragEnable(boolean dragEnable){
        if(this.dragenable!=dragEnable){
            this.dragenable=dragEnable;
            if(dragenable){
                if(mDragGesture !=null){
                    mDragGesture.enable();
                }else{
                    mDragGesture = new DragGesture(getWindowBridge(), mWindowView);
                }
            }else{
                if(mDragGesture !=null){
                    mDragGesture.disable();
                }
            }
        }
    }

//    public void setResizeEnable(boolean resizeEable){
//        if(this.resizeenable!=resizeenable){
//            this.resizeenable=resizeEable;
//            if(this.resizeenable){
//                if(mResizer!=null){
//                    mResizer.enable();
//                }else{
//                    mResizer=ResizeGesture.enableResize(mWindowView,mWindowView,getWindowBridge());
//                }
//            }else{
//                if(mResizer!=null){
//                    mResizer.disable();
//                }
//            }
//        }
//    }

    protected void setDragGesture(DragGesture dragGesture) {
        mDragGesture = dragGesture;
    }

    protected DragGesture getDragGesture() {
        return mDragGesture;
    }

    public void show(){

        if(mWindowView!=null){

            if(isclose){
                FloatyService.addWindow(this);
            }
            mWindowView.setVisibility(View.VISIBLE);
        }else{

        }
    };

     public  void hide(){
         if(mWindowView!=null){
             mWindowView.setVisibility(View.GONE);
         }
     };

     public  void dismiss(){
        close();
     };

    protected void onViewCreated(View view) {

    }

    //获得窗口管理者 把布局添加到屏幕上
    protected void attachToWindow(View view, WindowManager manager){
        getWindowManager().addView(view, getWindowLayoutParams());
        onAttachToWindow(view, manager);
    }

    //在添加到桌面之后的操作
    protected void onAttachToWindow(View view, WindowManager manager) {

    }

    //创建布局之后操作
    protected abstract View onCreateView(FloatyService service);

    //创建窗口位置尺寸接口
    protected WindowBridge onCreateWindowBridge(WindowManager.LayoutParams params) {
        return new WindowBridge.DefaultImpl(params, getWindowManager(), getWindowView());
    }

    //创建 窗口元素
    protected abstract WindowManager.LayoutParams onCreateWindowLayoutParams();


    //更新窗口元素
    public void updateWindowLayoutParams(WindowManager.LayoutParams params) {
        setWindowLayoutParams(params);
        mWindowManager.updateViewLayout(getWindowView(), getWindowLayoutParams());
    }


    //设置窗口管理
    protected void setWindowManager(WindowManager windowManager) {
        mWindowManager = windowManager;
    }


    //获得窗口元素
    public WindowManager.LayoutParams getWindowLayoutParams() {
        return mWindowLayoutParams;
    }

    //设置窗口元素
    protected void setWindowLayoutParams(WindowManager.LayoutParams windowLayoutParams) {
        mWindowLayoutParams = windowLayoutParams;
    }

    //获得窗口视图
    public View getWindowView() {
        return mWindowView;
    }

    //设置显示的布局
    protected void setWindowView(View windowView) {
        mWindowView = windowView;
    }

    public FloatyService getFloatyService() {
        return mFloatyService;
    }

    //获取窗口管理者
    public WindowManager getWindowManager() {
        return mWindowManager;
    }

    //获取当前窗口 位置尺寸修改的接口
    public WindowBridge getWindowBridge() {
        return mWindowBridge;
    }

    //设置当前窗口尺寸位置修改的接口
    protected void setWindowBridge(WindowBridge windowBridge) {
        mWindowBridge = windowBridge;
    }

    //当服务关闭
    public void onServiceDestroy(FloatyService service) {
        close();
    }

    public void close() {
        try {
            getWindowManager().removeView(getWindowView());
            FloatyService.removeWindow(this);
            isclose= true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //设置是否可以触摸  获取创建好的布局元素 更改了 参数之后 更新到当前
    public void setWindowFlag(int flag ,boolean  enable) {
        WindowManager.LayoutParams windowLayoutParams = getWindowLayoutParams();
        if (enable) {
            windowLayoutParams.flags |= flag;
        } else {
            windowLayoutParams.flags &= ~flag;
        }
        updateWindowLayoutParams(windowLayoutParams);
    }


    //设置是否可以触摸  获取创建好的布局元素 更改了 参数之后 更新到当前
    public void setTouchable(boolean touchable) {
        WindowManager.LayoutParams windowLayoutParams = getWindowLayoutParams();
        if (touchable) {
            windowLayoutParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        } else {
            windowLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }
        updateWindowLayoutParams(windowLayoutParams);
    }

    public  void setCanOverNotificationBar(boolean canOverNotificationBar){
        this.canOverNotificationBar=canOverNotificationBar;
    }


    public void disableWindowFocus( ) {
        WindowManager.LayoutParams windowLayoutParams = getWindowLayoutParams();
        windowLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        updateWindowLayoutParams(windowLayoutParams);
    }



    public void setWindowLayoutInScreen(boolean windowinscreen) {
        WindowManager.LayoutParams windowLayoutParams = getWindowLayoutParams();
        if(windowinscreen){
            windowLayoutParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        }else{
            windowLayoutParams.flags &=~WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        }
        updateWindowLayoutParams(windowLayoutParams);

    }

    public void requestWindowFocus() {
        WindowManager.LayoutParams windowLayoutParams = getWindowLayoutParams();
        windowLayoutParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        updateWindowLayoutParams(windowLayoutParams);
        getWindowView().requestFocus();
    }

    public void setWindowLayoutNoLimit() {
        WindowManager.LayoutParams windowLayoutParams = getWindowLayoutParams();
        windowLayoutParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        updateWindowLayoutParams(windowLayoutParams);
    }


    public  void setWindowFocus(boolean focusable){
        this.focusable=focusable;
        if(focusable){
            requestWindowFocus();
        }else{
            disableWindowFocus();
        }
    }
}
