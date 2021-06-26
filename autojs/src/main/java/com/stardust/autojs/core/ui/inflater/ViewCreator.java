package com.stardust.autojs.core.ui.inflater;

import android.content.Context;
import android.view.View;

import java.util.Map;

/**
 *
 */

public interface ViewCreator<V extends View> {

    V create(Context context, Map<String, String> attrs);

}
