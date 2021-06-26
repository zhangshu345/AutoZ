package com.stardust.autojs.core.graphics;

public  class Paint extends android.graphics.Paint {

    public Paint(){
        super(0);
    }
    public Paint(int i) {
        super(i);
    }

    public Paint(Paint paint) {
        super(paint);
    }

    public void setColor(long j) {
        int i = (int) j;
        if (((long) i) == j) {
            setColor(i);
        } else {
            super.setColor(j);
        }
    }


    public final void setColorLong(long j) {
        super.setColor(j);
    }

}