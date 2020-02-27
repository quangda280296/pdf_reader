package com.pdfreader.editor.view;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.pdfreader.editor.adapter.FragmentPagerAdapter;
import com.pdfreader.editor.base.BaseActivity;
import com.pdfreader.editor.fragment.FragmentAllFile;
import com.pdfreader.editor.fragment.FragmentFavorite;
import com.pdfreader.editor.fragment.FragmentHistory;
import com.pdfreader.editor.fragment.FragmentMoreApp;
import com.pdfreader.editor.model.Favorite;
import com.pdfreader.editor.model.PDFInfo;
import com.pdfreader.editor.splite.DatabaseUtil;
import com.pdfreader.editor.utils.Constants;
import com.pdfreader.editor.utils.Utils;
import com.pdfreader.pdf.reader.editor.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static java.security.AccessController.getContext;

public class MainActivity extends BaseActivity {

    private static final int REQUEST_CODE_PERMISSTION = 21;
    private static final String LINK_MORE_APP = "https://play.google.com/store/apps/developer?id=Smart+Tool+Free";
    @BindView(R.id.tabLayout)
    TabLayout tabLayoutAllFile;

    @BindView(R.id.viewpager_allFile)
    ViewPager viewPagerFileEnrolled;

    @BindView(R.id.img_search)
    ImageView imageView_search;

    @BindView(R.id.linear_share)
    LinearLayout linearLayout_share;

    @BindView(R.id.linear_rate)
    LinearLayout linearLayout_rate;

    @BindView(R.id.linear_moreApp)
    LinearLayout linearLayout_moreApp;

    @BindView(R.id.linear_policy)
    LinearLayout linearLayout_policy ;

    @BindView(R.id.edt_search)
    EditText editText_search;

    @BindView(R.id.img_backSearch)
    ImageView imageView_back;

    @BindView(R.id.img_closeSearch)
    ImageView imageView_closeSearch;

    @BindView(R.id.img_menu)
    ImageView imageView_menuMain;

    @BindView(R.id.layout_search)
    ConstraintLayout constraintLayout_search;

    @BindView(R.id.constraintLayout)
    ConstraintLayout constraintLayout_Top;

    @BindView(R.id.drawer)
    DrawerLayout mDrawerLayout;

    public static boolean isRename = false;
    private FragmentPagerAdapter fragmentPagerAdapter;
    private String mTextSearch = "";
    private boolean doubleBackToExitPressedOnce = false;
    private String tag;
    private Fragment fragment;
    private FragmentManager fm;
    private FragmentMoreApp fragmentMoreApp;
    private DatabaseUtil dbHelper;
    private ArrayList<PDFInfo> pdfInfoArrayList;
    private ArrayList<Favorite> favorites;
    private String pathFileOpen;


    @Override
    public int getViewResource() {
        return R.layout.activity_main;
    }

    @Override
    public void foundView(@Nullable View view) {
        getData();
    }

    private void getData() {
        fragmentPagerAdapter = new FragmentPagerAdapter(MainActivity.this, getSupportFragmentManager(), pathFileOpen);
        viewPagerFileEnrolled.setOffscreenPageLimit(3);
        viewPagerFileEnrolled.setAdapter(fragmentPagerAdapter);
        tabLayoutAllFile.setupWithViewPager(viewPagerFileEnrolled, true);
        setupTabIcons();
        requestPermisstion();
        setEventEdtSearch();
        dbHelper = DatabaseUtil.getInstant(MainActivity.this);
    }

    @Override
    public void notFoundView() {

    }

    @Override
    public void setListener() {

        viewPagerFileEnrolled.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                Log.e("highLightCurrentTab", "onPageScrolled");
            }

            @Override
            public void onPageSelected(int i) {
                if (i == 0) {
                    setupTabIcons();
                } else if (i == 1) {
                    tabLayoutAllFile.getTabAt(1).setIcon(R.drawable.ic_history);
                    tabLayoutAllFile.getTabAt(0).setIcon(R.drawable.ic_tab_pdf_uncheck);
                    tabLayoutAllFile.getTabAt(2).setIcon(R.drawable.ic_tab_favorite_uncheck);
                } else if (i == 2) {
                    tabLayoutAllFile.getTabAt(2).setIcon(R.drawable.ic_tab_favorite);
                    tabLayoutAllFile.getTabAt(0).setIcon(R.drawable.ic_tab_pdf_uncheck);
                    tabLayoutAllFile.getTabAt(1).setIcon(R.drawable.ic_history_uncheck);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                Log.e("highLightCurrentTab", "onPageScrollStateChanged");
            }
        });

        editText_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                    MainActivity.this.hideKeyboardFrom(editText_search);
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public String handleIntent(Intent intent) {
        String pathFile = intent.getDataString();
        if (pathFile != null){
            Uri appLinkData = intent.getData();
            String appLinkAction = intent.getAction();
            if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
                pathFileOpen = Utils.getFileName(this, appLinkData);
            }
        }
        return super.handleIntent(intent);
    }

    private void setupTabIcons() {
        tabLayoutAllFile.getTabAt(0).setIcon(R.drawable.ic_tab_pdf);
        tabLayoutAllFile.getTabAt(1).setIcon(R.drawable.ic_history_uncheck);
        tabLayoutAllFile.getTabAt(2).setIcon(R.drawable.ic_tab_favorite_uncheck);
    }

    private void requestPermisstion() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        if (getContext() == null || isFinishing()) return;
        if (hasReadPermissions() && hasWritePermissions()) {
            return;
        }

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_CODE_PERMISSTION); // your request code
    }

    private boolean hasReadPermissions() {

        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasWritePermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSTION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getData();
            } else {
                requestPermisstion();
            }
        }
    }

    @OnClick(R.id.img_search)
    public void setEventSerch() {
        toggle(View.VISIBLE);

        editText_search.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    @OnClick(R.id.img_closeSearch)
    public void setEventCloseSearch() {
        editText_search.setText("");
    }

    @OnClick(R.id.img_backSearch)
    public void setEventBackSearch() {
        toggle(View.GONE);
        hideKeyboardFrom(editText_search);
    }

    private void toggle(int visibility) {
        Transition transition = new Slide(Gravity.RIGHT);
        transition.setDuration(300);
        transition.addTarget(R.id.layout_search);

        TransitionManager.beginDelayedTransition(constraintLayout_search, transition);
        constraintLayout_search.setVisibility(visibility);
    }

    private void toggleTop(int visibility) {
        Transition transition = new Slide(Gravity.TOP);
        transition.setDuration(300);
        transition.addTarget(R.id.constraintLayout);

        TransitionManager.beginDelayedTransition(constraintLayout_Top, transition);
        constraintLayout_Top.setVisibility(visibility);
    }

    @OnClick(R.id.linear_share)
    public void eventShare() {
        if (setDoubleClick()) eventShareApp();
    }

    @OnClick(R.id.linear_rate)
    public void eventRate() {
        setEventRate();
    }

    @OnClick(R.id.linear_moreApp)
    public void eventMore() {
        if (setDoubleClick()) eventMoreApp();
    }

    @OnClick(R.id.linear_policy)
    public void eventPolicy() {
        if (setDoubleClick()){
            startLayoutMore(Constants.POLICY);
            mDrawerLayout.closeDrawers();
        }
    }

    private void setEventEdtSearch() {
        editText_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mTextSearch = editable.toString();
                setEventSearch(mTextSearch);
            }
        });
    }

    // sent text search to fragment on viewPager
    private void setEventSearch(String txtSearch) {
        Intent intent = new Intent(Constants.ACTION_SEARCH);
        intent.putExtra(Constants.TYPE_UPDATE, Constants.SHARE);
        intent.putExtra(Constants.SEARCH, Constants.SHARE);
        intent.putExtra(Constants.TXT_SEARCH, txtSearch);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void setEventRate() {
        Uri uri = Uri.parse(Constants.LINK_SHARE_APP);
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, " unable to find market app", Toast.LENGTH_LONG).show();
        }
    }

    private void eventMoreApp() {
        Uri uri = Uri.parse(LINK_MORE_APP);
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, " unable to find market app", Toast.LENGTH_LONG).show();
        }
    }

    public void listenerUpdateLayoutSearch() {
        toggle(View.GONE);
        hideKeyboardFrom(editText_search);
        editText_search.setText("");
    }

    private void startLayoutMore(String typeLayout) {
        constraintLayout_Top.setVisibility(View.GONE);
        tag = FragmentMoreApp.class.getName();
        fm = getSupportFragmentManager();
        fragment = fm.findFragmentByTag(tag);
        FragmentMoreApp fragmentMoreApp = new FragmentMoreApp();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.VERSION_CODE, getVersionCode());
        bundle.putString(Constants.TYPE_LAYOUT, typeLayout);
        fragmentMoreApp.setArguments(bundle);
        fm.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .setCustomAnimations(R.anim.animation_enter_to_left, R.anim.animation_exit_to_left, R.anim.animation_enter_to_left, R.anim.animation_exit_to_left)
                .add(R.id.fragment, fragmentMoreApp, tag)
                .addToBackStack(null)
                .commit();
    }

    public int getVersionCode() {
        int versionCode = 0;
        try {
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {

        }
        return versionCode;
    }

    @OnClick(R.id.img_menu)
    public void eventMenu() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    private void eventShareApp() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                Constants.LINK_SHARE_APP);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    @Override
    public void onBackPressed() {
        if (FragmentMoreApp.isDie) {
            super.onBackPressed();
            toggleTop(View.VISIBLE);
            return;
        }
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        if (constraintLayout_search.getVisibility() == View.VISIBLE) {
            toggle(View.GONE);
            return;
        }
        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.message_exit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public List<PDFInfo> getListHistory() {
        if (dbHelper == null) return null;
        List<PDFInfo> list = dbHelper.getList();
        return list;
    }

    public List<Favorite> getListFavorite() {
        if (dbHelper == null) return null;
        List<Favorite> list = dbHelper.getListFavorite();
        return list;
    }

    // sent data from all fragment
    public void listenerEventFile(PDFInfo pdfInfo, Favorite favorite, String event, File newFile, String itemName) {
        switch (event){
            case Constants.DELETE:
                String pathFile = pdfInfo.getFilePath();
                File file = new File(pathFile);
                file.delete();
                deleteListHistory(pdfInfo);
                deleteListFavorite(pathFile);
                FragmentAllFile.isUpdate = true;
                FragmentHistory.isUpdate = true;
                FragmentFavorite.isUpdate = true;
                break;
            case Constants.RENAME:
                upDateHistory(newFile, itemName);
                upDateFavorite(newFile, itemName);
                FragmentAllFile.isUpdate = true;
                FragmentHistory.isUpdate = true;
                FragmentFavorite.isUpdate = true;
                break;
        }
    }

    private void deleteListHistory(PDFInfo pdfInfo) {
        pdfInfoArrayList = (ArrayList<PDFInfo>) getListHistory();
        for (int i = 0; i < pdfInfoArrayList.size(); i++) {
            if (pdfInfo.getFileName().equals(pdfInfoArrayList.get(i).getFileName())) {
                String KEY_NAME = "id";
                long name = pdfInfoArrayList.get(i).getId();
                dbHelper.deleteHistory(pdfInfo, KEY_NAME + " = " + name);
                break;
            }
        }
    }

    private void deleteListFavorite(String path) {
        String KEY_NAME = "id";
        favorites = new ArrayList<>();
        favorites = (ArrayList<Favorite>) getListFavorite();
        for (int i = 0; i<favorites.size(); i++){
            String pathFavorite = favorites.get(i).getPathFile();
            if (path.equals(pathFavorite)){
                Favorite favorite = favorites.get(i);
                dbHelper.deleteFavorite(favorite, KEY_NAME + " = " + favorite.getId());
                break;
            }
        }
    }

    private void upDateHistory(File newFile, String itemName){
        pdfInfoArrayList = new ArrayList<>();
        pdfInfoArrayList = (ArrayList<PDFInfo>) getListHistory();
        PDFInfo pdfInfo = new PDFInfo();
        long idPdf = -1;
        for (int i = 0; i < pdfInfoArrayList.size(); i++){
            if (itemName.equals(pdfInfoArrayList.get(i).getFileName())){
                idPdf = pdfInfoArrayList.get(i).getId();
                pdfInfo.setId(idPdf);
                pdfInfo.setFilePath(newFile.getPath());
                pdfInfo.setFileName(newFile.getName());
                break;
            }
        }
        String whereClause = "id" + "=" + idPdf;
        dbHelper.upDatePdfInfo(pdfInfo, whereClause);
    }

    private void upDateFavorite(File newFile, String itemName){
        favorites = new ArrayList<>();
        favorites = (ArrayList<Favorite>) getListFavorite();
        Favorite favorite = null;
        long idFavorite = -1;
        for (int i = 0; i < favorites.size(); i++) {
            if (itemName.equals(favorites.get(i).getNameFile())) {
                idFavorite = favorites.get(i).getId();
                favorite = new Favorite(idFavorite, newFile.getPath(), newFile.getName());
                break;
            }
        }
        String whereClause = "id" + "=" + idFavorite;
        dbHelper.upDateFavorite(favorite, whereClause);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}