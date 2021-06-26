package com.stardust.autojs.core.ui.inflater.inflaters;

import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.Switch;

import com.stardust.autojs.core.ui.inflater.ResourceParser;
import java.util.Map;

public class SwitchInflater<V extends Switch>  extends BaseViewInflater<Switch>{


    public SwitchInflater(ResourceParser resourceParser) {
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
            case "showText":
                    view.setShowText(!TextUtils.isEmpty(value)&&value.equals("true"));
                break;
            default:
                return super.setAttr(view, attr, value, parent, attrs);
        }
        return true;
    }


}
