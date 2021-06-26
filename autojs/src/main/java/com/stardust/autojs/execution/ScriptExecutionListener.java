package com.stardust.autojs.execution;

import java.io.Serializable;

/**
 *
 */

public interface ScriptExecutionListener extends Serializable {

    void onStart(ScriptExecution execution);

    void onSuccess(ScriptExecution execution, Object result);

    void onException(ScriptExecution execution, Throwable e);
}
