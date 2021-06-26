package com.stardust.autojs.core.floaty;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.stardust.autojs.R;
import com.stardust.autojs.core.floaty.gesture.DragGesture;
import com.stardust.autojs.core.floaty.gesture.ResizeGesture;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


/**
 *可调整悬浮窗口
 */

public class ResizableFloatyWindow extends FloatyWindow {

    private static final String TAG = "ResizableFloatyWindow";

    private View mView;
    private View mResizer;
    private View mMoveCursor;
    private ResizableFloaty mFloaty;

    public ResizableFloatyWindow(ResizableFloaty floaty) {
        if (floaty == null) {
            throw new NullPointerException("floaty == null");
        }
        mFloaty = floaty;
    }

    @Override
    public void onCreate(FloatyService service, WindowManager manager) {
        super.onCreate(service, manager);
    }


    @Override
    protected View onCreateView(FloatyService service) {
        Context context = service.getApplicationContext();
//        RelativeLayout windowView = new RelativeLayout(context);
//        windowView.setLayoutParams(new RelativeLayout.LayoutParams(WRAP_CONTENT,WRAP_CONTENT));
        ViewGroup windowView = (ViewGroup) View.inflate(context, R.layout.floaty_container, null);
        mView = mFloaty.inflateView(service, this);
        mResizer = mFloaty.getResizerView(mView);
        mMoveCursor = mFloaty.getMoveCursorView(mView);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        windowView.addView(mView, params);
        windowView.setFocusableInTouchMode(true);
        return windowView;
    }

    @Override
    protected void onViewCreated(View view) {
        super.onViewCreated(view);
        initGesture();
    }

    @Override
    protected WindowManager.LayoutParams onCreateWindowLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WRAP_CONTENT,
                WRAP_CONTENT,
                WindowTypeCompat.getPhoneWindowType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.TOP | Gravity.START;
        return layoutParams;
    }

    public View getRootView() {
        return mView;
    }

    public View getResizer() {
        return mResizer;
    }

    public View getMoveCursor() {
        return mMoveCursor;
    }


    private void initGesture() {
        if (mResizer != null) {
            ResizeGesture.enableResize(mResizer, mView, getWindowBridge());
        }

        if (mMoveCursor != null) {
            DragGesture gesture = new DragGesture(getWindowBridge(), mMoveCursor);
            gesture.setPressedAlpha(1.0f);
        }
    }

}
