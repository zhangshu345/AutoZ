package org.autojs.autojs.ui.widget;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 *
 */

public interface OnItemClickListener {

    void onItemClick(RecyclerView parent, View item, int position);
}
