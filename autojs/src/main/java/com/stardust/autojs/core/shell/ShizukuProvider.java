package com.stardust.autojs.core.shell;
// 应该在这里实现cmd 命令的执行和 检测是否可以
public class ShizukuProvider extends moe.shizuku.api.ShizukuProvider {

    public static volatile boolean shizukuactive=false;
    @Override
    public boolean onCreate() {
        boolean res = super.onCreate();
//        LogUtils.d("Shizuku", getClass().getSimpleName() + " onCreate | Process=" + ProcessUtils.getCurrentProcessName());
        shizukuactive=true;
        enableMultiProcessSupport(true);
        return res;
    }
}