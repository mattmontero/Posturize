package edu.sjsu.posturize.posturize.sidenav.sidenavmodals;

import android.app.Dialog;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by Matt on 11/18/2017.
 */

public class ModalWindowManager {
    public static void format(Dialog dialog){
        Window window = dialog.getWindow();
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(window.getAttributes());
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(layoutParams);
    }
}
