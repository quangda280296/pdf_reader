package com.pdfreader.editor.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.TextView;

import com.pdfreader.pdf.reader.editor.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DialogRate {

    @BindView(R.id.btn_cancel)
    TextView textView_cancel;
    @BindView(R.id.btn_ok)
    TextView textView_ok;

    private Dialog dialog;
    private Activity mActivity;

    private ListenerRate listenerRate;

    public void showDialog(Activity activity) {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_rate);
        ButterKnife.bind(activity);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setOnKeyListener((arg0, keyCode, event) -> {
            // TODO Auto-generated method stub
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                dialog.dismiss();
            }
            return false;
        });
        mActivity = activity;

        textView_ok = dialog.findViewById(R.id.btn_ok);
        textView_cancel = dialog.findViewById(R.id.btn_cancel);

        textView_ok.setOnClickListener(view -> {
            listenerRate.CallBackStateRate();
            dialog.dismiss();
        });

        textView_cancel.setOnClickListener(view -> {

            dialog.dismiss();
        });
        dialog.show();
    }

    public void setListenerRate(ListenerRate listenerRate) {
        this.listenerRate = listenerRate;
    }

    public interface ListenerRate {
        void CallBackStateRate();
    }
}
