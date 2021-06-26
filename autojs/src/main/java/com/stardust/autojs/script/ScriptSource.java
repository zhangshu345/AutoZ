package com.stardust.autojs.script;
import java.io.Serializable;

/**
 *
 */

public abstract class ScriptSource implements Serializable {

    private String mName;

    public ScriptSource(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public abstract String getEngineName();
}
