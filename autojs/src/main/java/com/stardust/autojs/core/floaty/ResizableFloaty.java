package com.stardust.autojs.core.floaty;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * idea 可调节
 */

public interface ResizableFloaty {


    View inflateView(FloatyService floatyService, ResizableFloatyWindow service);

    @Nullable
    View getResizerView(View view);

    @Nullable
    View getMoveCursorView(View view);

    abstract class AbstractResizableFloaty implements ResizableFloaty {
        @Nullable
        public View getResizerView(View view) {
            return null;
        }

        @Nullable
        public View getMoveCursorView(View view) {
            return null;
        }

    }
}
