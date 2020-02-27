package com.pdfreader.editor.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.text.style.ImageSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.pdfreader.editor.model.PDFInfo;
import com.pdfreader.editor.view.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

public class Utils {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final long MAX_DATE = 5;
    private static final Object obj = new Object();
    //    public static OnSetClickCallback onSetClickCallback;
    public static int dpiDevice;
    private static Utils utils;
    private static Context mContext;
    //    private FirebaseAnalytics mFirebaseAnalytics;
    private static List<CharSequence> arrSpecChar = Arrays.asList(
            new CharSequence[]{
                    "!", "`", "~", "#", "$", "@", "%", "^", "&", "*", "(", ")", "-", "=", "+", "[",
                    "]", "{", "}", ";", ":", "\"", "'", "\\", "|", "/", "?", ".", ">", "<", ",", " "
            });
    private volatile static boolean isFlying = false;
    private static Future<SharedPreferences> sReferrerPrefs;
    private long ONE_DAY = 1;
    private int screenWidth;
    private int screenHeight;
    //"04/03/2014 23:41:37",
    private SimpleDateFormat mMachineSdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
    private SimpleDateFormat mHumanSdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US);
    private static final SharedPreferencesLoader sPrefsLoader = new SharedPreferencesLoader();
    Utils() {
        mMachineSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        isFlying = true;
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this.mContext);
    }

    public static boolean isSupportedCam2(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }

        try {
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String[] cameraIds = cameraManager.getCameraIdList();
            for (String id : cameraIds) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                if (characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                        == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                    return false;
                }
            }
            // On Android OS pre 4.4.2, a class will not load because of VerifyError if it contains a
            // catch statement with an Exception from a newer API, even if the code is never executed.
            // https://code.google.com/p/android/issues/detail?id=209129
        } catch (/* CameraAccessException */ Exception e) {
//            Log.e("Camera access exception: " + e.getMessage());
            return false;
        }
        return true;
    }

    public static Utils shared() {
        if (utils == null) {
            utils = new Utils();
        }
        return utils;
    }

    public static void initial(Context context) {
        mContext = context;
        synchronized (obj) {

            if (null == sReferrerPrefs) {
                sReferrerPrefs = sPrefsLoader.loadPreferences(context, Constants.REFERRER_PREFS_NAME, null);
            }
            if (utils == null)
                utils = new Utils();
        }
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null
                && connectivityManager.getActiveNetworkInfo().isAvailable()
                && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public static void showMessageAlertDialog(Context mContext, String setTitle, String setMessage, int setIcon) {
        new AlertDialog.Builder(mContext)
                .setTitle(setTitle)
                .setMessage(setMessage)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
//                        onSetClickCallback.onOkClicked();
                        dialog.dismiss();
                    }
                })
                .setIcon(setIcon)
                .show();
    }
//    public void OnSetClickCallback(OnSetClickCallback onsetOkClicked) {
//        this.onSetClickCallback = onsetOkClicked;
//    }
//
//    public interface OnSetClickCallback {
//        void onOkClicked();
//
//    }


    public static String miliSecondsToTimer(long milliseconds) {
        String finalTimerString;
        String minutesString;
        String secondsString;

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there

        // Prepending 0 to hours if it is one digit
        if (hours < 10) {
            finalTimerString = "0" + hours;
        } else {
            finalTimerString = "" + hours;
        }

        // Prepending 0 to minutes if it is one digit
        if (minutes < 10) {
            minutesString = "0" + minutes;
        } else {
            minutesString = "" + minutes;
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + ":" + minutesString + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    public static boolean checkNickValidate(CharSequence input) {
        return arrSpecChar.contains(input);
    }

    public static boolean checkValidate(String s) {
        if (s == null || s.trim().isEmpty())
            return false;
        Pattern p = Pattern.compile("[^A-Za-z0-9_]*");
        Matcher m = p.matcher(s);
        return m.find();
    }

    public static boolean checkSpecChar(String input) {
        String patternStr = ".*[\\p{Punct}\\p{Space}\\p{javaWhitespace}].*";
        return input.matches(patternStr);
    }

    public static boolean checkPermission(String permissionStr) {
        // Here, thisActivity is the current activity
        return ContextCompat.checkSelfPermission(mContext, permissionStr) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0) {
            return false;
        } else {
            etText.requestFocus();
            etText.setError("Vui lòng điền thông tin!");
            return true;
        }
    }

    public static boolean isEmailValid(String email) {
        boolean isValid = true;
        String expression = "[a-zA-Z0-9._-]+@[a-z]+(\\.+[a-z]+)+";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = false;
        }
        return isValid;
    }

    public static String getFilename(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        String uriString = uri.toString();
        File myFile = new File(uriString);
        String path = myFile.getAbsolutePath();
        String displayName = null;

        if (uriString.startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        } else if (uriString.startsWith("file://")) {
            displayName = myFile.getName();
        }
        return displayName;
    }

    public static boolean isFlying() {
        return isFlying;
    }

    /**
     * Waits for Upushmessagelib to takeOff and be ready.
     *
     * @param millis Time to wait for Upushmessagelib to be ready in milliseconds or {@code 0} to wait
     *               forever.
     * @return The ready MyApi instance, or {@code null} if Upushmessagelib
     * is not ready by the specified wait time.
     * @hide
     */
    public static Utils waitForTakeOff(long millis) {
        synchronized (utils) {
            if (isFlying) {
                return utils;
            }

            boolean interrupted = false;

            try {
                while (!isFlying) {
                    try {
                        utils.wait(millis);
                    } catch (InterruptedException ignored) {
                        interrupted = true;
                    }
                }

                return utils;
            } finally {
                if (interrupted) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static String getPackageName() {
        return mContext.getPackageName();
    }

    public static PackageManager getPackageManager() {
        return shared().getContext().getPackageManager();
    }

    public static ApplicationInfo getAppInfo() {
        return shared().getContext().getApplicationInfo();
    }

    public static String getAppName() {
        if (getAppInfo() != null) {
            return getPackageManager().getApplicationLabel(getAppInfo()).toString();
        } else {
            return null;
        }
    }

    public static String getMyLibPermission() {
        return getPackageName() + ".permission.SH_DATA";
    }

    public static void shareInApp(Context context, String text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }

    public static final String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
//            Log.d(e.getMessage());
        }
        return s.toUpperCase();
    }

    public static int getDiffYears(Date first, Date last) {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(last);
        int diff = b.get(YEAR) - a.get(YEAR);
        if (a.get(MONTH) > b.get(MONTH) ||
                (a.get(MONTH) == b.get(MONTH) && a.get(DATE) > b.get(DATE))) {
            diff--;
        }
        return diff;
    }

    private static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(date);
        return cal;
    }

    public static String formatDay(int year, int month, int day, String separate) {
        String monthString;
        String dayString;
        if (month < 10)
            monthString = 0 + "" + month;
        else
            monthString = month + "";

        if (day < 10)
            dayString = 0 + "" + day;
        else
            dayString = day + "";

        return dayString + separate + monthString + separate + year;
    }

    public static String formatDate(int year, int month, int day, String separate) {
        String monthString;
        String dayString;
        if (month < 10)
            monthString = 0 + "" + month;
        else
            monthString = month + "";

        if (day < 10)
            dayString = 0 + "" + day;
        else
            dayString = day + "";

        return year + separate + monthString + separate + dayString;
    }

    public static String getTypeData() {
        return (String) Utils.shared().getSharedPreference(Constants.TOKEN, "dateAscending");
    }

    public static void setTypeData(String typeData) {
        Utils.shared().setSharedPreference(Constants.TOKEN, typeData);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getPathFromUri(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static MultipartBody.Part convertStringToMultipart(Context context, String path, String name) {
        File file;
//        String path = getPathFromUri(activity, uri);
        if (path == null)
            return null;
        file = new File(path);
//        file = new File(uri.getPath());
        RequestBody requestFile = null;
        int i = path.lastIndexOf('.');
        if (i >= 0) {
            String extension = path.substring(i + 1);
            extension = extension.toLowerCase();
            switch (extension) {
                case "png": {
                    requestFile = RequestBody.create(MediaType.parse("image/png"), file);
                    break;
                }
                case "jpg": {
                    requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
                    break;
                }
                case "jpeg": {
                    requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
                    break;
                }
                case "bmp": {
                    requestFile = RequestBody.create(MediaType.parse("image/bmp"), file);
                    break;
                }
                case "tiff": {
                    requestFile = RequestBody.create(MediaType.parse("image/tiff"), file);
                    break;
                }
                case "dicom": {
                    requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                    break;
                }
                case "doc": {
                    requestFile = RequestBody.create(MediaType.parse("application/msword"), file);
                    break;
                }
                case "docx": {
                    requestFile = RequestBody.create(MediaType.parse("application/vnd.openxmlformats-officedocument.wordprocessingml.document"), file);
                    break;
                }
                case "pdf": {
                    requestFile = RequestBody.create(MediaType.parse("application/pdf"), file);
                    break;
                }
                case "txt": {
                    requestFile = RequestBody.create(MediaType.parse("text/plain"), file);
                    break;
                }

            }
        }
        if (requestFile == null)
            return null;
//        RequestBody requestFile = RequestBody.create(MediaType.convertToTournament("multipart/form-Data"), file);

        return MultipartBody.Part.createFormData(name, file.getName(), requestFile);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static MultipartBody.Part convertUriToMultipart(Context context, Uri uri) {
        File file;
        String path = getPathFromUri(context, uri);
        if (path == null)
            return null;
        file = new File(path);
//        file = new File(uri.getPath());
        RequestBody requestFile = null;
        int i = path.lastIndexOf('.');
        if (i >= 0) {
            String extension = path.substring(i + 1);
            extension = extension.toLowerCase();
            switch (extension) {
                case "png": {
                    requestFile = RequestBody.create(MediaType.parse("image/png"), file);
                    break;
                }
                case "jpg": {
                    requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
                    break;
                }
                case "jpeg": {
                    requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
                    break;
                }
                case "bmp": {
                    requestFile = RequestBody.create(MediaType.parse("image/bmp"), file);
                    break;
                }
                case "tiff": {
                    requestFile = RequestBody.create(MediaType.parse("image/tiff"), file);
                    break;
                }
                case "dicom": {
                    requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                    break;
                }
                case "doc": {
                    requestFile = RequestBody.create(MediaType.parse("application/msword"), file);
                    break;
                }
                case "docx": {
                    requestFile = RequestBody.create(MediaType.parse("application/vnd.openxmlformats-officedocument.wordprocessingml.document"), file);
                    break;
                }
                case "pdf": {
                    requestFile = RequestBody.create(MediaType.parse("application/pdf"), file);
                    break;
                }
                case "txt": {
                    requestFile = RequestBody.create(MediaType.parse("text/plain"), file);
                    break;
                }

            }
        }
        if (requestFile == null)
            return null;
//        RequestBody requestFile = RequestBody.create(MediaType.convertToTournament("multipart/form-Data"), file);

        return MultipartBody.Part.createFormData("file", file.getName(), requestFile);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static RequestBody convertUriToRequestBody(Context context, Uri uri) {
        File file;
        String path = getPathFromUri(context, uri);
        file = new File(path);
//        file = new File(uri.getPath());
        return RequestBody.create(MediaType.parse("multipart/form-Data"), file);

    }

    public static MultipartBody.Part convertFileToRequestBody(Context context, File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        return MultipartBody.Part.createFormData("file", file.getName(), requestFile);
    }

    public static boolean isPhoneOrEmail(String phoneOrEmail) {
        if (TextUtils.isEmpty(phoneOrEmail))
            return false;
        if (phoneOrEmail.contains("@")) {
            if (!Utils.isEmailValid(phoneOrEmail))
                return true;
        } else {
            String expression = "[a-z]+";
            CharSequence inputStr = phoneOrEmail;

            Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(inputStr);
            if (matcher.matches()) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

//    public static String getUserId() {
//        if (User.getCache() != null) {
//            return User.getCache().getId();
//        }
//        return "";
//    }

    public static String getExactUrl(String url) {
//        if (url == null)
//            return "";
//        if (!url.startsWith("http:") && !url.startsWith("https:")) {
//            url = BuildConfig.BASE_URL_IMG + url;
//        }
        return url;
    }

    public static String getFirstNameFromName(String name) {
        int position = name.lastIndexOf(" ");
        if (position == -1) {
            return name;
        } else {
            return name.substring(position + 1, name.length());
        }
    }

    public static String convertTimeToTimeChat(String createdAt) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
            SimpleDateFormat hhmma = new SimpleDateFormat("HH:mm a");
            SimpleDateFormat ddMM = new SimpleDateFormat("dd/MM");


            Date date = sdf.parse(createdAt);
            Date today = new Date();
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
            if (fmt.format(date).equals(fmt.format(today))) {
                return hhmma.format(date);
            } else {
                return ddMM.format(date);
            }
        } catch (Exception ex) {
            return "";
        }
    }


    public static String convertTimeServerToddMMyyy(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
            SimpleDateFormat ddMMyyy = new SimpleDateFormat("dd/MM/yyy");


            Date date = sdf.parse(time);
            return ddMMyyy.format(date);
        } catch (Exception ex) {
            return "";
        }
    }

    public static String formatMoney(Integer price) {
        String moneyStr = String.valueOf(price);
        StringBuilder result = new StringBuilder();
        int count = 1;
        while (moneyStr.length() >= 3 * count) {
            int firstPosition = moneyStr.length() - 3 * count;
            result.insert(0, moneyStr.substring(firstPosition, firstPosition + 3) + ".");
            count++;
        }
        if (moneyStr.length() % 3 != 0) {
            result.insert(0, moneyStr.substring(0, moneyStr.length() - 3 * (count - 1)) + ".");
        }
        return result.toString().substring(0, result.toString().length() - 1);
    }


    public Object getSharedPreference(String keyPref, Object defaultValue) {
        SharedPreferences pref;
        try {
            pref = sReferrerPrefs.get();

            if (defaultValue instanceof String) {
                return pref.getString(keyPref, (String) defaultValue);
            } else if (defaultValue instanceof Integer) {
                return pref.getInt(keyPref, (Integer) defaultValue);
            } else if (defaultValue instanceof Float) {
                return pref.getFloat(keyPref, (Float) defaultValue);
            } else if (defaultValue instanceof Boolean) {
                return pref.getBoolean(keyPref, (Boolean) defaultValue);
            } else if (defaultValue instanceof Long) {
                return pref.getLong(keyPref, (Long) defaultValue);
            }
        } catch (InterruptedException | ExecutionException ignored) {
        }
        return null;
    }

    /**
     * Set Data pref into file save pref with multitype Data
     *
     * @param keyPref   Key to map with column in file pref
     * @param valuePref value to input column value in file pref
     * @Link String, Integer, Float, Boolean
     */
    public void setSharedPreference(String keyPref, Object valuePref) {
        try {
            SharedPreferences pref = sReferrerPrefs.get();
            SharedPreferences.Editor editor = pref.edit();
            if (valuePref == null) return;
            if (valuePref instanceof String) {
                editor.putString(keyPref, (String) valuePref);
            } else if (valuePref instanceof Integer) {
                editor.putInt(keyPref, (Integer) valuePref);
            } else if (valuePref instanceof Float) {
                editor.putFloat(keyPref, (Float) valuePref);
            } else if (valuePref instanceof Boolean) {
                editor.putBoolean(keyPref, (Boolean) valuePref);
            } else if (valuePref instanceof Long) {
                editor.putLong(keyPref, (Long) valuePref);
            }
            editor.apply();
        } catch (InterruptedException | ExecutionException ignored) {
        }
    }

    public void removeKey(String key) {
        try {
            SharedPreferences pref = sReferrerPrefs.get();
            SharedPreferences.Editor editor = pref.edit();
            editor.remove(key);
            editor.apply();
        } catch (InterruptedException | ExecutionException ignored) {
        }
    }

    public boolean cachedDataObject(Context context, Object objectCached, String fileCache) {
        File cacheDir;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            cacheDir = new File(Environment.getExternalStorageDirectory(), "CachedObject");
        else
            cacheDir = context.getCacheDir();
        if (!cacheDir.exists())
            cacheDir.mkdirs();

        final File suspend_f = new File(cacheDir, fileCache);

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        boolean keep = true;

        try {
            fos = new FileOutputStream(suspend_f);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(objectCached);
        } catch (Exception e) {
            keep = false;
        } finally {
            try {
                if (oos != null) oos.close();
                if (fos != null) fos.close();
                if (!keep) suspend_f.delete();
            } catch (Exception e) { /* do nothing */ }
        }

        return keep;

    }

    public Object getCachedData(Context context, String fileCache) {
        File cacheDir;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            cacheDir = new File(Environment.getExternalStorageDirectory(), "CachedObject");
        else
            cacheDir = context.getCacheDir();
        if (!cacheDir.exists())
            cacheDir.mkdirs();


        Object objectCached = null;
        final File suspend_f = new File(cacheDir, fileCache);

        FileInputStream fis = null;
        ObjectInputStream is = null;

        try {
            fis = new FileInputStream(suspend_f);
            is = new ObjectInputStream(fis);
            objectCached = is.readObject();
        } catch (Exception e) {
            String val = e.getMessage();
        } finally {
            try {
                if (fis != null) fis.close();
                if (is != null) is.close();
            } catch (Exception ignored) {
            }
        }

        return objectCached;
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new AbsListView.LayoutParams(desiredWidth, AbsListView.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight() + 15;
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount()));
        listView.setLayoutParams(params);
    }

    /**
     * @param listView
     * @param heightItem height of item listview
     * @param numbItem   numb item of list fill to listview
     */
    public void setListViewHeightBasedOnChildren(ListView listView, int heightItem, int numbItem) {
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = heightItem * numbItem + (listView.getDividerHeight() * (listView.getAdapter().getCount()));
        listView.setLayoutParams(params);
    }

    public int convertPxToDp(int size) {
        if (dpiDevice == 0) {
            WindowManager windowManager = (WindowManager) mContext
                    .getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            display.getMetrics(dm);
            dpiDevice = dm.densityDpi;
        }
        return size * dpiDevice / 160;
    }

    public float convertPxToDp(float size) {
        if (dpiDevice == 0) {
            WindowManager windowManager = (WindowManager) mContext
                    .getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            display.getMetrics(dm);
            dpiDevice = dm.densityDpi;
        }
        return size * dpiDevice / 160.0f;
    }

    public int convertDpToPx(int size) {
        if (dpiDevice == 0) {
            WindowManager windowManager = (WindowManager) mContext
                    .getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            display.getMetrics(dm);
            dpiDevice = dm.densityDpi;
        }
        return size * 160 / dpiDevice;
    }

    public float convertDpToPx(float size) {
        if (dpiDevice == 0) {
            WindowManager windowManager = (WindowManager) mContext
                    .getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            display.getMetrics(dm);
            dpiDevice = dm.densityDpi;
        }
        return (1.0f) * (size * 160.0f / dpiDevice);
    }

    public String convertStringToSecond(String strTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SSS");
            Date date = sdf.parse(strTime);
            long timeInMillisSinceEpoch = date.getTime();
//            long timeInMinutesSinceEpoch = timeInMillisSinceEpoch / (60 * 1000);
            long secondTime = TimeUnit.MILLISECONDS.toSeconds(timeInMillisSinceEpoch);
            return secondTime + "";
        } catch (Exception ex) {
            return "";
        }
    }

    public long convertStrToSec(String strTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SSS");
            Date date = sdf.parse(strTime);
            long timeInMillisSinceEpoch = date.getTime();
//            long timeInMinutesSinceEpoch = timeInMillisSinceEpoch / (60 * 1000);
            return TimeUnit.MILLISECONDS.toSeconds(timeInMillisSinceEpoch);
        } catch (Exception ex) {
            return 0;
        }
    }


    /**
     * @param second int long type
     * @return string of time
     */
    public String convertSecondToStringTime(String second) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd.MM.yyyy");
            Date date = new Date(Long.parseLong(second) * 1000);
            return sdf.format(date);
        } catch (Exception ex) {
            return "";
        }
    }

    public String convertSecondToMMss(String second) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
            Date date = new Date(Long.parseLong(second) * 1000);
            return sdf.format(date);
        } catch (Exception ex) {
            return "";
        }
    }

    public boolean isKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public String getHumanDateString() {
        return mHumanSdf.format(new Date());
    }

    public String getHumanRelativeDateStringFromString(String machineDateStr) {
        String result = "";
        try {
            result = DateUtils.getRelativeTimeSpanString(mMachineSdf.parse(machineDateStr).getTime()).toString();
            result = result.replace("in 0 minutes", "just now");
        } catch (ParseException ignored) {
        }
        return result;
    }

    public void showHideKeyBoard(boolean isShow, EditText editText) {
        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (isShow) {
            inputMethodManager.showSoftInput(editText, InputMethodManager.RESULT_UNCHANGED_SHOWN);
        } else {
            inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }
    }

    public void showHideKeyBoard(Activity activity, boolean isShow) {
        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (isShow) {
            showKeyboard(activity);
        } else {
            hideSoftKeyboard(activity);
        }
    }

    public boolean checkKeyboardVisible() {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.isAcceptingText();
    }

    private void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow(activity.getWindow().getDecorView()
                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void showKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void restartFirstActivity(Activity activity) {
        Intent i = new Intent(activity, MainActivity.class);

        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(i);
        activity.finishAffinity();
//        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public String convertResource2String(String beforeText, int resource) {
        SpannableString ss = new SpannableString(beforeText);
        Drawable d = mContext.getResources().getDrawable(resource);
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
        ss.setSpan(span, 0, beforeText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return ss.toString();
    }

    public String convertDrawable2String(String beforeText, Drawable d) {
        SpannableString ss = new SpannableString(beforeText);
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
        ss.setSpan(span, 0, beforeText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return ss.toString();
    }

    public void viewTextAsHTML(TextView textView, String preText, String endText, int colorText, String embedText) {
        String result = new StringBuilder()
                .append("<p>")
                .append(preText)
                .append("<font color=\"")
                .append(colorText)
                .append("\">")
                .append(embedText)
                .append("</font><br/>")
                .append(endText)
                .append("</p>")
                .toString();
        textView.setText(Html.fromHtml(result));
    }

    public String getKeyHash() {
        String keyHash = "";
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
//                Log.i("KeyHash:", keyHash);
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
        }
        return keyHash;
    }

    public boolean checkAppInstalled(String packageName) {
        PackageManager packageManager = mContext.getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public String getDeviceId() {

        try {
            String android_id = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            String deviceId = md5(android_id).toUpperCase();
            return deviceId;
        } catch (Exception e) {
//            Log.e("Exception deviceId " + e.getMessage());
            return "";
        }
    }

    public String getDeviceName() {

        if (!checkPermission(Manifest.permission.BLUETOOTH))
            return Build.MODEL;
        try {
            BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
            return myDevice.getName();
        } catch (Exception e) {
//            Log.e("Exception bluetooth " + e.getMessage());
            return Build.MODEL;
        }

    }

    public void showWifiSetting(Context context) {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        context.startActivity(intent);
    }

    /**
     * Create a file Uri for saving an image or video
     */
    private Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    public File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "SkyCam/" + ((type == MEDIA_TYPE_IMAGE)
                ? "Images" : (type == MEDIA_TYPE_VIDEO) ? "Videos" : "Medias"));
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
//                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyy-MMdd_HH-mm-ss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    public Context getContext() {
        return mContext;
    }

    public String processTime(String time) {

//        Log.d("Time before: " + updatedAt);
        Date dateLog = null;
        try {
            dateLog = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(time);
        } catch (Exception e) {
//            Log.d(e.getMessage());
        }
        if (dateLog == null) {
            return "Unknown";
        }

        Date dateNow = new Date();
        long timeDistance = (dateNow.getDate() - dateLog.getDate());
        String date = "Unknown";
        if (dateNow.getYear() == dateLog.getYear() && dateNow.getMonth() == dateLog.getMonth()) {
            if (timeDistance >= ONE_DAY && timeDistance < MAX_DATE)
                date = timeDistance + " ngày trước ";
            else if (timeDistance < ONE_DAY) {
                int hour = dateLog.getHours() - dateNow.getHours();
                int minute = dateLog.getMinutes() - dateNow.getMinutes();
                int second = dateLog.getSeconds() - dateNow.getSeconds();
                if (hour > 0) {
                    date = hour + " giờ trước";
                } else if (minute > 0) {
                    date = minute + " phút trước";
                } else {
                    date = second + " giây trước";
                }
            } else {
                date = dateLog.getDate() + "/" + (dateLog.getMonth() + 1) + "/" + String.valueOf(dateLog.getYear() + 1900);
            }
        } else {
            date = dateLog.getDate() + "/" + (dateLog.getMonth() + 1) + "/" + String.valueOf(dateLog.getYear() + 1900);
        }
        return date;

    }

    public void logout(Activity activity) {
//        Utils.shared().setSharedPreference(Constants.TOKEN, "");
//        Utils.shared().setSharedPreference(Constants.USER_ID, "");
//        Utils.shared().setSharedPreference(Constants.USER_OBJECT, "");
//        User.logOut();
//        logoutGoogle(activity);
//        logoutFacebook();
//        Intent i = new Intent(activity, HomeActivity.class);
//
//        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        activity.startActivity(i);
//        activity.finishAffinity();
//        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void connectToRealtime() {
//        String userId = Utils.getUserId();
//        String token = Utils.getToken();
//
//        if (userId != null && token != null) {
//            RealtimeController.shared().setAppId(userId);
//            RealtimeController.shared().register(token);
//        }
    }

//    public void logoutGoogle(Activity activity) {
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
//                .build();
//
//        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
//        mGoogleSignInClient.signOut();
//        mGoogleSignInClient.revokeAccess();
//    }

//    public void logoutFacebook() {
//        LoginManager.getInstance().logOut();
//    }

    public static String getDayOfWeek(int value) {
        String day = "";
        switch (value) {
            case 1:
                day = "Chủ nhật";
                break;
            case 2:
                day = "Thứ 2";
                break;
            case 3:
                day = "Thứ 3";
                break;
            case 4:
                day = "Thứ 4";
                break;
            case 5:
                day = "Thứ 5";
                break;
            case 6:
                day = "Thứ 6";
                break;
            case 7:
                day = "Thứ 7";
                break;
        }
        return day;
    }

    public static boolean isEqualsDay(Calendar calendar1, Calendar calendar2) {
        if (calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH)) {
            if (calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)) {
                if (calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)) {
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    public void logEvent(String eventName, Bundle bundle) {
//        mFirebaseAnalytics.logEvent(eventName, bundle);
    }

    public boolean onRemoveFile(String path) {
        File file = new File(path);
        try {
            removeFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private static void removeFile(File file) throws IOException {
        if (file != null && file.exists()) {
            if (file.isDirectory()) {
//                Log.e("size", "size = " + file.listFiles());
                for (File sub : file.listFiles()) {
                    removeFile(sub);
                }
            }
            file.delete();
        }
    }

    public static ArrayList<PDFInfo> sortByNameAZ(ArrayList<PDFInfo> files) {

        Collections.sort(files, new Comparator<PDFInfo>() {
            @Override
            public int compare(PDFInfo lhs, PDFInfo rhs) {
                return lhs.getFileName().compareToIgnoreCase(rhs.getFileName());
            }
        });

        return files;
    }

    public static String lengthFile(Context mContext, String path) {

        String textLength = "";
        File file = new File(path);
        int file_size;
//            long length = (int) file.length();
        if (file.isDirectory()) {
            file_size = file.listFiles().length;
            textLength = file_size + " " + "Item";
        } else {
            textLength = Formatter.formatFileSize(mContext, file.length());
        }
//                if (length >= 1000000) {
//                    length = length / 1000000;
//                    textLength = length + " " + "MB";
//                } else if (length >= 1024) {
//                    length = length / 1024;
//                    textLength = length + " " + "KB";
//                } else {
//                    textLength = length + " " + "B";
//                }
//            }
        return textLength;
    }

    public static String getRealPathFromURI(Context context, Uri uri) {
        String path = null, image_id = null;

        Cursor cursor1 = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor1 != null) {
            cursor1.moveToFirst();
            image_id = cursor1.getString(0);
            image_id = image_id.substring(image_id.lastIndexOf(":") + 1);
            cursor1.close();
        }

        Cursor cursor = context.getContentResolver().query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{image_id}, null);
        if (cursor!=null) {
            cursor.moveToFirst();
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        }
        return path;
    }

    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static String formatFileSize(long size) {
        String hrSize = null;

        double b = size;
        double k = size/1024.0;
        double m = ((size/1024.0)/1024.0);
        double g = (((size/1024.0)/1024.0)/1024.0);
        double t = ((((size/1024.0)/1024.0)/1024.0)/1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if ( t>1 ) {
            hrSize = dec.format(t).concat(" TB");
        } else if ( g>1 ) {
            hrSize = dec.format(g).concat(" GB");
        } else if ( m>1 ) {
            hrSize = dec.format(m).concat(" MB");
        } else if ( k>1 ) {
            hrSize = dec.format(k).concat(" KB");
        } else {
            hrSize = dec.format(b).concat(" Bytes");
        }

        return hrSize;
    }
}
