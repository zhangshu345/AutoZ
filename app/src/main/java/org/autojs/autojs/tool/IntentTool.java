package org.autojs.autojs.tool;

import android.content.Context;
import android.widget.Toast;

import org.autojs.autojs.R;
import com.stardust.util.IntentUtil;

/**
 *
 */

public class IntentTool {

    public static void browse(Context context, String url){
        if (!IntentUtil.browse(context, url)) {
            Toast.makeText(context, R.string.text_no_brower, Toast.LENGTH_SHORT).show();
        }
    }
}
