package com.pdfreader.editor.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.format.Formatter;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pdfreader.pdf.reader.editor.R;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DialogDetail {

    private String mNewNameFolder = "";
    private Activity activity;
    private Dialog dialog;
    private String[] mPathStringList;
    private String[] mNewPathStringList;


    @BindView(R.id.btn_ok_info)
    RelativeLayout relativeLayout_ok;

    public void showDialog(Activity activity, String path, String pathFile){
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_infomation);
        ButterKnife.bind(activity);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setOnKeyListener((arg0, keyCode, event) -> {
            // TODO Auto-generated method stub
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                dialog.dismiss();
            }
            return true;
        });
        activity = activity;

        relativeLayout_ok =  dialog.findViewById(R.id.btn_ok_info);
        TextView textView_name = dialog.findViewById(R.id.txt_nameinfo);
        TextView textView_size = dialog.findViewById(R.id.txt_sizeinfo);
        TextView textView_date = dialog.findViewById(R.id.txt_dateinfo);
        TextView textView_link = dialog.findViewById(R.id.txt_linksinfo);
        TextView textView_ok = dialog.findViewById(R.id.btn_ok);
        textView_ok.setTypeface(textView_ok.getTypeface(), Typeface.BOLD);

        File directory = new File(path);
        File file = new File(pathFile);
        Date lastModDate = new Date(directory.lastModified());
        DateFormat dateFormat = new SimpleDateFormat("MMM  dd, yyyy kk:mm");
        String artDate = dateFormat.format(lastModDate);
        textView_date.setText(artDate);
        TextView textView_title = dialog.findViewById(R.id.font_UTMAvo2);
        textView_title.setTypeface(textView_title.getTypeface(), Typeface.BOLD);

        textView_size.setText(Formatter.formatFileSize(activity, file.length()));
        textView_name.setText(file.getName());
        textView_link.setText(pathFile);

        final Activity finalActivity = activity;
        relativeLayout_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
