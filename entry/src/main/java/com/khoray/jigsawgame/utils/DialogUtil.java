package com.khoray.jigsawgame.utils;

import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.ToastDialog;
import ohos.app.Context;

public class DialogUtil {
    public static void showToast(Context ctx, String msg) {
        ToastDialog td = new ToastDialog(ctx);
        td.setText(msg);
        td.setAlignment(LayoutAlignment.BOTTOM);
        td.setDuration(200);
        td.show();
    }
}
