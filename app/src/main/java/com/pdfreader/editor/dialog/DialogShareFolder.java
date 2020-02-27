package com.pdfreader.editor.dialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.net.URLConnection;

public class DialogShareFolder {

    public void startShare(Activity activity, String myFilePath) {

//        Log.e("share", "share = " + myFilePath);
        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
        File fileWithinMyDir = new File(myFilePath);
        if (fileWithinMyDir.exists()) {
            Uri uriShare = FileProvider.getUriForFile
                    (activity, activity.getApplicationContext().getPackageName() + ".my.package.name.provider", fileWithinMyDir);
            intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intentShareFile.setType(URLConnection.guessContentTypeFromName(fileWithinMyDir.getName()));
            intentShareFile.putExtra(Intent.EXTRA_STREAM, uriShare);

            intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                    "Sharing File...");
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...");

            activity.startActivity(Intent.createChooser(intentShareFile, "Share File"));
        }
    }
}
