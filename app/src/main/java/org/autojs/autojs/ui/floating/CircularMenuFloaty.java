package org.autojs.autojs.ui.floating;

import android.view.View;

import com.stardust.autojs.core.floaty.FloatyService;


public interface CircularMenuFloaty {

    View inflateActionView(FloatyService service, CircularMenuWindow window);

    CircularActionMenu inflateMenuItems(FloatyService service, CircularMenuWindow window);
}
