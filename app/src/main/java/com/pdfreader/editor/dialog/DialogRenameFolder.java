package com.pdfreader.editor.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pdfreader.editor.model.PDFInfo;
import com.pdfreader.editor.utils.Utils;
import com.pdfreader.pdf.reader.editor.R;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DialogRenameFolder {

    @BindView(R.id.btn_canel_rename)
    TextView textView_cancel;
    @BindView(R.id.btn_ok_rename)
    TextView textView_ok;
    @BindView(R.id.edit_rename)
    EditText editText_newNamefolder;
    private Dialog dialog;
    private Activity mActivity;
    private String dotFile = "pdf";
    private ListenerSuccessfully listenerSuccessfully;

    public void showDialog(Activity activity, String path, ArrayList<PDFInfo> pdfInfoArrayList) {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_rename);
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

        textView_ok = dialog.findViewById(R.id.btn_ok_rename);
        textView_cancel = dialog.findViewById(R.id.btn_canel_rename);
        editText_newNamefolder = dialog.findViewById(R.id.edit_rename);

        final String nameFolder = path.substring(path.lastIndexOf("/") + 1);
        final File oldFile = new File(path);


        editText_newNamefolder.setHint(nameFolder);


        textView_ok.setOnClickListener(view -> {
            String newName = editText_newNamefolder.getText().toString();
            if (newName.trim().isEmpty()) {
                editText_newNamefolder.setError("Only space is not allowed");
                return;
            }
            newName = newName.trim();
            if (oldFile.isFile()) {
                File newFile = new File(oldFile.getParentFile(), newName + "." + dotFile);
                if (!checkNameIsExisted(newName, pdfInfoArrayList))return;
                if (oldFile.renameTo(newFile)) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DATA, newFile.getAbsolutePath());
                    Toast.makeText(mActivity, String.format("Rename file to %s successfully.", newName), Toast.LENGTH_SHORT).show();
                    listenerSuccessfully.CallBackStateSuccess(true, newFile);
                    dialog.dismiss();
                } else {
                    Toast.makeText(mActivity, "Can't rename file. Please try again.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
                return;
            }
            dialog.dismiss();
        });

        textView_cancel.setOnClickListener(view -> {
            try {
                Utils.shared().showHideKeyBoard(activity, false);
            } catch (Exception ignored) {
            }
            try {
                InputMethodManager inputMethodManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(editText_newNamefolder.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
            } catch (Exception ignored) {
            }
            dialog.dismiss();
        });
        dialog.show();
    }

    private boolean checkNameIsExisted(String newName, ArrayList<PDFInfo> arrayList){
        for (int i = 0; i<arrayList.size(); i++){
            if (arrayList.get(i).getFileName().equals(newName + ".pdf")){
                Toast.makeText(mActivity, "Choice another name. File is existed.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    public void setListenerSuccessfully(ListenerSuccessfully listenerSuccessfully) {
        this.listenerSuccessfully = listenerSuccessfully;
    }

    public interface ListenerSuccessfully {
        void CallBackStateSuccess(boolean isUpdateData, File newFile);
    }
}
