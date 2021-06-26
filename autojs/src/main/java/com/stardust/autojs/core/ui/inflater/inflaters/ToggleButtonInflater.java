package com.stardust.autojs.core.ui.inflater.inflaters;

import android.view.ViewGroup;
import android.widget.ToggleButton;
import com.stardust.autojs.core.ui.inflater.ResourceParser;
import java.util.Map;

public class ToggleButtonInflater<V extends ToggleButton> extends BaseViewInflater<V>{

    public ToggleButtonInflater(ResourceParser resourceParser) {
        super(resourceParser);
    }


    public boolean setAttr(V view, String attr, String value, ViewGroup parent, Map<String, String> attrs) {
        if (super.setAttr(view, attr, value, parent, attrs)) {
            return true;
        }
        switch (attr) {
            case "textOn":
                view.setTextOn(value);
                break;
            case "textOff":
                view.setTextOff(value);
                break;
            default:
                return super.setAttr(view, attr, value, parent, attrs);
        }
        return true;
    }

}
