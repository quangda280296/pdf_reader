package com.pdfreader.editor.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.artifex.mupdfdemo.Annotation;
import com.artifex.mupdfdemo.Hit;
import com.artifex.mupdfdemo.MuPDFAlert;
import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;
import com.artifex.mupdfdemo.MuPDFReaderViewListener;
import com.artifex.mupdfdemo.MuPDFView;
import com.artifex.mupdfdemo.OutlineActivityData;
import com.artifex.mupdfdemo.OutlineItem;
import com.artifex.mupdfdemo.ReaderView;
import com.artifex.mupdfdemo.SavePdf;
import com.artifex.mupdfdemo.SearchTask;
import com.artifex.mupdfdemo.SearchTaskResult;
import com.artifex.mupdfdemo.widget.VDHDeepLayout;
import com.lonelypluto.pdflibrary.utils.SharedPreferencesUtil;
import com.pdfreader.editor.base.BaseActivity;
import com.pdfreader.editor.font.ImageViewCustom;
import com.pdfreader.editor.mode.AcceptMode;
import com.pdfreader.editor.mode.TopBarMode;
import com.pdfreader.editor.model.Favorite;
import com.pdfreader.editor.model.PDFInfo;
import com.pdfreader.editor.model.Reading;
import com.pdfreader.editor.splite.DatabaseUtil;
import com.pdfreader.editor.utils.Constants;
import com.pdfreader.editor.utils.SharedPreferencesIsCheckedUserData;
import com.pdfreader.pdf.reader.editor.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import butterknife.BindView;
import butterknife.OnClick;

@SuppressLint("RestrictedApi")
public class ViewPdfActivity extends BaseActivity implements MenuBuilder.Callback, ImageViewCustom.OnImageClickListener {

    private static final String TAG = "ViewPdfActivity";
    private static final int PICK_IMAGE = 1102;
    private static long time = 4000;
    private int mTimeScreen = 0;

    private MuPDFCore muPDFCore;
    @BindView(R.id.pdfView)
    MuPDFReaderView muPDFReaderView;

    @BindView(R.id.img_more)
    ImageView imageView_more;

    @BindView(R.id.img_favorite)
    ImageView imageView_favorite;

    @BindView(R.id.img_nightMode)
    ImageView imageView_night;

    @BindView(R.id.img_back)
    ImageView imageView_back;

    @BindView(R.id.txt_nameFile)
    TextView textView_name;

    @BindView(R.id.txt_pageShow)
    TextView textView_pageShow;

    @BindView(R.id.txt_page)
    TextView textView_page;

    @BindView(R.id.pageSlider)
    SeekBar mPageSlider;

    @BindView(R.id.layout_topDetail)
    ConstraintLayout constraintLayout_layoutTop;

    @BindView(R.id.layout_page)
    ConstraintLayout constraintLayout_layoutBottom;

    @BindView(R.id.layout_toolsPDF)
    RelativeLayout layout_toolPdf;
    // tools
    @BindView(R.id.switcher)
    ViewAnimator mTopBarSwitcher;

    @BindView(R.id.linkButton)
    ImageButton mLinkButton;


    @BindView(R.id.reflowButton)
    ImageButton mAnnotButton;

    @BindView(R.id.outlineButton)
    ImageButton mOutlineButton;

    @BindView(R.id.searchButton)
    ImageButton mSearchButton;

    @BindView(R.id.searchText)
    EditText et_searchText;

    @BindView(R.id.searchBack)
    ImageViewCustom mSearchBack;
    @BindView(R.id.layout_sile)
    RelativeLayout layout_sile;

    @BindView(R.id.searchForward)
    ImageViewCustom mSearchFwd;
    @BindView(R.id.img_cancelEdt)
    ImageViewCustom imageViewCustom_cancelEd;
    @BindView(R.id.img_highlightEdt)
    ImageViewCustom imageViewCustom_highlightEdt;
    @BindView(R.id.img_underLineEdt)
    ImageViewCustom imageViewCustom_underLineEdt;
    @BindView(R.id.img_strikeOutEdt)
    ImageViewCustom imageViewCustom_strikeOutEdt;
    @BindView(R.id.img_inkEdt)
    ImageViewCustom imageViewCustom_img_inkEdt;
    @BindView(R.id.cancelDeleteButton)
    ImageViewCustom imageViewCustom_cancelDelete;
    @BindView(R.id.cancelAcceptButton)
    ImageViewCustom imageViewCustom_cancelAccept;
    @BindView(R.id.acceptButton)
    ImageViewCustom imageViewCustom_accept;
    @BindView(R.id.layout_choicePic)
    ConstraintLayout layout_choicePic;

    @BindView(R.id.cancelSearch)
    ImageViewCustom mCancelSearch;
    @BindView(R.id.annotType)
    TextView mAnnotTypeText;

    @BindView(R.id.VDHDeepLayout)
    VDHDeepLayout vdhDeepLayout;
    @BindView(R.id.iv_sign)
    ImageView iv_sign;
    @BindView(R.id.img_cancelPic)
    ImageViewCustom imageViewCustom_cancelPic;
    @BindView(R.id.img_okPic)
    ImageViewCustom imageViewCustom_okPic;

    private Bitmap bitmap;
    private SavePdfTask savePdfTask;

    private boolean mButtonsVisible;
    private TopBarMode mTopBarMode = TopBarMode.Main;
    private AcceptMode mAcceptMode;
    private AsyncTask<Void, Void, MuPDFAlert> mAlertTask;
    private boolean mAlertsActive = false;

    private boolean mLinkHighlight = false;
    private final int OUTLINE_REQUEST = 0;
    private int mPageSliderRes;
    private AlertDialog.Builder mAlertBuilder;
    private AlertDialog mAlertDialog;


    private boolean isHorizontal;
    private boolean isNightMode;
    private boolean isPage;
    private boolean isFavorite;
    private String pathFile = "";
    private String pathAdd = "";
    private CountDownTimer countDownTimer, countDownTimerBack;
    private DatabaseUtil dbHelper;
    private ArrayList<Favorite> favorites = new ArrayList<>();
    private ArrayList<Reading> readings = new ArrayList<>();
    private Favorite favorite;
    private PDFInfo pdfInfo;
    private SearchTask mSearchTask;

    private Reading reading;
    private int readingpage = Constants.POS_DEFAULT;


    @Override
    public int getViewResource() {
        return R.layout.activity_view_pdf;
    }

    @Override
    public void foundView(@Nullable View view) {

        // isNightMode is state background night or light
        isNightMode = SharedPreferencesIsCheckedUserData.getPrefBooblean(Constants.IS_NIGHT_MODE, false, this);
        // isHorizontal is state horizontal or vertical
        isHorizontal = SharedPreferencesIsCheckedUserData.getPrefBooblean(Constants.IS_SCROLL, true, this);
        // isPage is state page or Reversed
        isPage = SharedPreferencesIsCheckedUserData.getPrefBooblean(Constants.IS_PAGE, true, this);
        initView(getData(), isHorizontal);
        animationShowLayoutTop();
        for (int i = 0; i < favorites.size(); i++) {
            if (favorites.get(i).getPathFile().equals(getData())) {
                imageView_favorite.setImageResource(R.drawable.ic_add_favorite);
                favorite = favorites.get(i);
                isFavorite = true;
                break;
            }
        }
        updatePageNumView(readingpage);
        initListener();
        showButtons();
    }

    @Override
    public void notFoundView() {
    }

    @Override
    public void setListener() {
        dbHelper = DatabaseUtil.getInstant(this);
        favorites = (ArrayList<Favorite>) getList();
        readings = (ArrayList<Reading>) getListReading();


        mPageSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                muPDFReaderView.setDisplayedViewIndex((seekBar.getProgress() + mPageSliderRes / 2) / mPageSliderRes);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) updatePageNumView(((progress + mPageSliderRes / 2) / mPageSliderRes) + 1);
            }
        });
    }

    private void listenerPageReading(String path) {
        try {
            for (int i = 0; i < readings.size(); i++) {
                Reading reading = readings.get(i);
                if (reading.getPath().equals(path)) {
                    readingpage = reading.getPage();
                    break;
                }
            }
        } catch (Exception ex) {
        }
    }

    private String getData() {
        Intent intent = getIntent();
        pdfInfo = (PDFInfo) intent.getSerializableExtra(Constants.PATH_PDF);
        File file = new File(pdfInfo.getFilePath());
        textView_name.setText(file.getName());
        pathFile = pdfInfo.getFilePath();
        listenerPageReading(pdfInfo.getFilePath());
        return pathFile;
    }

    private void initListener() {
        mSearchBack.setOnImageClickListener(this);
        mSearchFwd.setOnImageClickListener(this);
        mCancelSearch.setOnImageClickListener(this);
        imageViewCustom_cancelEd.setOnImageClickListener(this);
        imageViewCustom_highlightEdt.setOnImageClickListener(this);
        imageViewCustom_img_inkEdt.setOnImageClickListener(this);
        imageViewCustom_strikeOutEdt.setOnImageClickListener(this);
        imageViewCustom_underLineEdt.setOnImageClickListener(this);
        imageViewCustom_cancelDelete.setOnImageClickListener(this);
        imageViewCustom_cancelAccept.setOnImageClickListener(this);
        imageViewCustom_accept.setOnImageClickListener(this);
        imageViewCustom_cancelPic.setOnImageClickListener(this);
        imageViewCustom_okPic.setOnImageClickListener(this);
    }

    private List<Favorite> getList() {
        if (dbHelper == null) return null;
        List<Favorite> list = dbHelper.getListFavorite();
        return list;
    }

    private List<Reading> getListReading() {
        if (dbHelper == null) return null;
        List<Reading> list = dbHelper.getReading();
        return list;
    }

    public void insertTabFavorite(Favorite tableFavorite) {
        try {
            if (dbHelper != null) {
                dbHelper.insertFavorite(tableFavorite);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("RestrictedApi")
    @OnClick(R.id.img_more)
    public void setEventMore() {

        @SuppressLint("RestrictedApi") MenuBuilder menuBuilder = new MenuBuilder(this);
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.actions_more_pdf, menuBuilder);
        @SuppressLint("RestrictedApi") MenuPopupHelper optionsMenu = new MenuPopupHelper(this, menuBuilder, imageView_more);
        optionsMenu.setForceShowIcon(true);

        // Set Item Click Listener
        menuBuilder.setCallback(this);
        if (isHorizontal)
            menuBuilder.getItem(0).setTitle(getResources().getString(R.string.title_vertical)).setIcon(getResources().getDrawable(R.drawable.ic_vertical));
        else
            menuBuilder.getItem(0).setTitle(getResources().getString(R.string.title_horizontal)).setIcon(getResources().getDrawable(R.drawable.ic_horizontal));
        optionsMenu.show();
    }

    @OnClick(R.id.img_nightMode)
    public void setEventNightMore() {
        isNightMode = !isNightMode;
        SharedPreferencesIsCheckedUserData.setPrefBoolean(Constants.IS_NIGHT_MODE, isNightMode, this);
        setUpLayoutNight(isNightMode);
    }

    @OnClick(R.id.img_favorite)
    public void setEventFavorite() {
        isFavorite = !isFavorite;
        if (isFavorite) {
            imageView_favorite.setImageResource(R.drawable.ic_add_favorite);
            favorite = new Favorite(pdfInfo.getId(), pdfInfo.filePath, pdfInfo.getFileName());
            insertTabFavorite(favorite);
        } else {
            imageView_favorite.setImageResource(R.drawable.ic_favorite_uncheck);
            String KEY_NAME = "id";
            long name = favorite.getId();
            dbHelper.deleteFavorite(favorite, KEY_NAME + " = " + name);
        }

    }


    public void setEventShowLayoutControl() {
        if (constraintLayout_layoutTop.getVisibility() == View.VISIBLE) {
            toggle(View.GONE);
        } else {
            toggle(View.VISIBLE);
            if (countDownTimer != null) countDownTimer.cancel();
            animationShowLayoutTop();
        }
    }

    private void setUpLayoutNight(boolean isNightMode) {
        if (isNightMode) imageView_night.setImageResource(R.drawable.ic_night_more_check);
        else imageView_night.setImageResource(R.drawable.ic_night_more_uncheck);
//        setShowViewPDF(isNightMode, isHorizontal, isPage);
    }

    private void toggle(int view) {
        toggleBottom(view);
        Transition transition = new Slide(Gravity.TOP);
        transition.setDuration(300);
        transition.addTarget(R.id.layout_topDetail);

        TransitionManager.beginDelayedTransition(constraintLayout_layoutTop, transition);
        constraintLayout_layoutTop.setVisibility(view);
    }

    private void toggleChoice(int view) {
        Transition transition = new Slide(Gravity.TOP);
        transition.setDuration(300);
        transition.addTarget(R.id.layout_choicePic);

        TransitionManager.beginDelayedTransition(layout_choicePic, transition);
        layout_choicePic.setVisibility(view);
    }

    private void toggleTool(int view) {
        Transition transition = new Slide(Gravity.RIGHT);
        transition.setDuration(300);
        transition.addTarget(R.id.layout_toolsPDF);
        TransitionManager.beginDelayedTransition(layout_toolPdf, transition);
        layout_toolPdf.setVisibility(view);
    }

    private void animationShowLayoutTop() {
        countDownTimer = new CountDownTimer(time, 800) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                toggle(View.GONE);
            }
        }.start();
    }

    @OnClick(R.id.img_back)
    public void eventBack() {
        onBackPressed();
    }

    private void toggleBottom(int view) {
        constraintLayout_layoutBottom.setVisibility(view);
        Transition transition = new Slide(Gravity.BOTTOM);
        transition.setDuration(300);
        transition.addTarget(R.id.layout_sile);

        TransitionManager.beginDelayedTransition(layout_sile, transition);
        layout_sile.setVisibility(view);
    }

    private void initView(String path, boolean isScroll) {
        SharedPreferencesUtil.init(getApplication());
        createPDF(path, isScroll);
    }

    private void createPDF(String path, boolean isScroll) {
        mAlertBuilder = new AlertDialog.Builder(this);
        muPDFCore = openFile(path);
        SearchTaskResult.set(null);
        if (muPDFCore == null) {
            return;
        }
        muPDFReaderView.setAdapter(new MuPDFPageAdapter(this, muPDFCore));
        if (readingpage != Constants.POS_DEFAULT)
            muPDFReaderView.setDisplayedViewIndex(readingpage - 1);
        else readingpage = 1;
        // Set up the page slider
        int smax = Math.max(muPDFCore.countPages() - 1, 1);
        mPageSliderRes = ((10 + smax - 1) / smax) * 2;
        muPDFReaderView.setHorizontalScrolling(isScroll);
//        muPDFReaderView.set
        mSearchTask = new SearchTask(this, muPDFCore) {
            @Override
            protected void onTextFound(SearchTaskResult result) {
                SearchTaskResult.set(result);
                // Ask the ReaderView to move to the resulting page
                muPDFReaderView.setDisplayedViewIndex(result.pageNumber);
                // Make the ReaderView act on the change to SearchTaskResult
                // via overridden onChildSetup method.
                muPDFReaderView.resetupChildren();
            }
        };

        // Search invoking buttons are disabled while there is no text specified
        mSearchBack.setEnabled(false);
        mSearchFwd.setEnabled(false);
        mSearchBack.setColorFilter(Color.argb(0xFF, 250, 250, 250));
        mSearchFwd.setColorFilter(Color.argb(0xFF, 250, 250, 250));

        setListenerPDF();
    }

    private MuPDFCore openFile(String path) {
        try {
            muPDFCore = new MuPDFCore(this, path);
            OutlineActivityData.set(null);
        } catch (Exception e) {
            Log.e(TAG, "openFile catch:" + e.toString());
            return null;
        } catch (OutOfMemoryError e) {
            //  out of memory is not an Exception, so we catch it separately.
            Log.e(TAG, "openFile catch: OutOfMemoryError " + e.toString());
            return null;
        }
        return muPDFCore;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListenerPDF() {
        setMuPDFReaderViewListener();
        et_searchText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                boolean haveText = s.toString().length() > 0;
                setButtonEnabled(mSearchBack, haveText);
                setButtonEnabled(mSearchFwd, haveText);
                // Remove any previous search results
                if (SearchTaskResult.get() != null && !et_searchText.getText().toString().equals(SearchTaskResult.get().txt)) {
                    SearchTaskResult.set(null);
                    muPDFReaderView.resetupChildren();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }
        });

        //React to Done button on keyboard
        et_searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    search(1);
                return false;
            }
        });

        et_searchText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)
                    search(1);
                return false;
            }
        });
    }

    private void setMuPDFReaderViewListener() {
        muPDFReaderView.setListener(new MuPDFReaderViewListener() {
            @Override
            public void onMoveToChild(int i) {
                if (muPDFCore == null) {
                    return;
                }
                updatePageNumView(i + 1);
                mPageSlider.setMax((muPDFCore.countPages() - 1) * mPageSliderRes);
                mPageSlider.setProgress(i * mPageSliderRes);
            }

            @Override
            public void onTapMainDocArea() {
                setEventShowLayoutControl();
                if (!mButtonsVisible) {
                    showButtons();
                } else {
                    if (mTopBarMode == TopBarMode.Main)
                        hideButtons();
                }
            }

            @Override
            public void onDocMotion() {
            }

            @Override
            public void onHit(Hit item) {
                switch (mTopBarMode) {
                    case Annot:
                        if (item == Hit.Annotation) {
                            showButtons();
                            mTopBarMode = TopBarMode.Delete;
                            mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
                        }
                        break;
                    case Delete:
                        mTopBarMode = TopBarMode.Annot;
                        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
                        // fall through
                    default:
                        // Not in annotation editing mode, but the pageview will
                        // still select and highlight hit annotations, so
                        // deselect just in case.
                        MuPDFView pageView = (MuPDFView) muPDFReaderView.getDisplayedView();
                        if (pageView != null) {
                            pageView.deselectAnnotation();
                        }
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case OUTLINE_REQUEST:
                if (resultCode >= 0)
                    muPDFReaderView.setDisplayedViewIndex(resultCode);
                break;
            case PICK_IMAGE:
                if (resultCode == RESULT_OK) {
//                    OnAddPictureButtonClick();
//                    Uri selectedImage = data.getData();
//                    String path = Utils.getRealPathFromURI(this, selectedImage);
//                    pathAdd = path;
//                    bitmap = getBitmap(path);
//
//                    int widthViewPDF = muPDFReaderView.getWidth();
//                    int heightViewPDF = muPDFReaderView.getHeight();
//                    ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(widthViewPDF, heightViewPDF);
//                    vdhDeepLayout.setLayoutParams(params);
//                    vdhDeepLayout.setVisibility(View.VISIBLE);
//                    iv_sign.setImageBitmap(bitmap);

                    Intent intent = new Intent(this, AddPictureActivity.class);
                    intent.putExtra(Constants.PATH_PDF, pathFile);
                    intent.putExtra(Constants.INT_PAGE, muPDFReaderView.getDisplayedViewIndex());
                    startActivity(intent);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private static Bitmap getBitmap(String vectorDrawableId) {
        Drawable vectorDrawable = Drawable.createFromPath(vectorDrawableId);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    private void saveBitmap(Bitmap bitmap) {

        String out_path = pathFile.substring(0, pathFile.length() - 4) + "_t2.pdf";
        SavePdf savePdf = new SavePdf(pathFile, out_path);
        savePdf.setScale(muPDFReaderView.getCurrentScale());
        savePdf.setPageNum(muPDFReaderView.getDisplayedViewIndex() + 1);

        savePdf.setWidthScale(1.0f * muPDFReaderView.getScaleX() / muPDFReaderView.getDisplayedView().getWidth()); // Tính tỷ lệ phần trăm bù rộng
        savePdf.setHeightScale(1.0f * muPDFReaderView.getScaleY() / muPDFReaderView.getDisplayedView().getHeight()); // Tính phần trăm bù dài

        Log.e("zyw", "scaleX = " + 1.0f * muPDFReaderView.getScaleX() / muPDFReaderView.getDisplayedView().getWidth());
        Log.e("zyw", "scaleY = " + 1.0f * muPDFReaderView.getScaleY() / muPDFReaderView.getDisplayedView().getHeight());
        Log.e("zyw", "CurrentScale = " + muPDFReaderView.getCurrentScale());
        Log.e("zyw", " X = " + iv_sign.getX());
        Log.e("zyw", " Y = " + iv_sign.getY());
        savePdf.setWH(iv_sign.getX(), iv_sign.getY() + 80);

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);

        float density = metric.density;
        savePdf.setDensity(density);
        Bitmap bitmap1 = getBitmap(pathAdd);
        try {
            // set size picture
            Bitmap bitmapPicture = Bitmap.createScaledBitmap(bitmap1, iv_sign.getWidth(), iv_sign.getHeight(), true);
            savePdf.setBitmap(bitmapPicture);
        } catch (Exception ex) {
            savePdf.setBitmap(bitmap1);
        }
        savePdfTask = new SavePdfTask(savePdf);
        savePdfTask.execute();
    }

    private void showButtons() {
        if (muPDFCore == null)
            return;
        if (!mButtonsVisible) {
            mButtonsVisible = true;
            // Update page number text and slider
            int index = muPDFReaderView.getDisplayedViewIndex();
            updatePageNumView(index + 1);
            mPageSlider.setMax((muPDFCore.countPages() - 1) * mPageSliderRes);
            mPageSlider.setProgress(index * mPageSliderRes);
            if (mTopBarMode == TopBarMode.Search) {
                et_searchText.requestFocus();
                showKeyboard();
            }

            Animation anim = new TranslateAnimation(0, 0, -mTopBarSwitcher.getHeight(), 0);
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mTopBarSwitcher.setVisibility(View.VISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                }
            });
            mTopBarSwitcher.startAnimation(anim);
        }
    }

    private void hideButtons() {
        if (mButtonsVisible) {
            mButtonsVisible = false;
            hideKeyboard();
            Animation anim = new TranslateAnimation(0, 0, 0, -mTopBarSwitcher.getHeight());
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
//                    mTopBarSwitcher.setVisibility(View.INVISIBLE);
                }
            });
            mTopBarSwitcher.startAnimation(anim);
        }
    }

    private void updatePageNumView(int index) {
        if (muPDFCore == null) return;
        textView_page.setText("" + muPDFCore.countPages());
        textView_pageShow.setText(index + "");
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.showSoftInput(et_searchText, 0);
    }

    public void OnEditAnnotButtonClick(View v) {
        mTopBarMode = TopBarMode.Main;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    private void OnCopyTextButtonClick() {
        mTopBarMode = TopBarMode.Accept;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mAcceptMode = AcceptMode.CopyText;
        muPDFReaderView.setMode(MuPDFReaderView.Mode.Selecting);
        mAnnotTypeText.setText(getString(R.string.copy_text));
        showInfo(getString(R.string.select_text));
    }

    private void OnAddPictureButtonClick() {
        toggleChoice(View.VISIBLE);
    }

    private void searchModeOn() {
        if (mTopBarMode != TopBarMode.Search) {
            mTopBarMode = TopBarMode.Search;
            //Focus on EditTextWidget
            et_searchText.requestFocus();
            showKeyboard();
            mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        }
    }

    private void searchModeOff() {
        if (mTopBarMode == TopBarMode.Search) {
            mTopBarMode = TopBarMode.Main;
            hideKeyboard();
//            mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
            SearchTaskResult.set(null);
            // Make the ReaderView act on the change to mSearchTaskResult
            // via overridden onChildSetup method.
            muPDFReaderView.resetupChildren();
            toggleTool(View.GONE);
        }
    }

    private void OnHighlightButtonClick() {
        mTopBarMode = TopBarMode.Accept;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mAcceptMode = AcceptMode.Highlight;
        muPDFReaderView.setMode(MuPDFReaderView.Mode.Selecting);
        mAnnotTypeText.setText(R.string.pdf_tools_highlight);
        showInfo(getString(R.string.select_text));
    }

    private void OnUnderlineButtonClick() {
        mTopBarMode = TopBarMode.Accept;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mAcceptMode = AcceptMode.Underline;
        muPDFReaderView.setMode(MuPDFReaderView.Mode.Selecting);
        mAnnotTypeText.setText(R.string.pdf_tools_underline);
        showInfo(getString(R.string.select_text));
    }

    private void OnStrikeOutButtonClick() {
        mTopBarMode = TopBarMode.Accept;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mAcceptMode = AcceptMode.StrikeOut;
        muPDFReaderView.setMode(MuPDFReaderView.Mode.Selecting);
        mAnnotTypeText.setText(R.string.pdf_tools_strike_out);
        showInfo(getString(R.string.select_text));
    }

    private void OnInkButtonClick() {
        mTopBarMode = TopBarMode.Accept;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mAcceptMode = AcceptMode.Ink;
        muPDFReaderView.setMode(MuPDFReaderView.Mode.Drawing);
        mAnnotTypeText.setText(R.string.pdf_tools_ink);
        showInfo(getString(R.string.pdf_tools_draw_annotation));
    }

    public void OnDeleteButtonClick(View v) {
        MuPDFView pageView = (MuPDFView) muPDFReaderView.getDisplayedView();
        if (pageView != null)
            pageView.deleteSelectedAnnotation();
        mTopBarMode = TopBarMode.Annot;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    private void OnCancelDeleteButtonClick() {
        MuPDFView pageView = (MuPDFView) muPDFReaderView.getDisplayedView();
        if (pageView != null)
            pageView.deselectAnnotation();
        mTopBarMode = TopBarMode.Annot;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    private void OnCancelAcceptButtonClick() {
        MuPDFView pageView = (MuPDFView) muPDFReaderView.getDisplayedView();
        if (pageView != null) {
            pageView.deselectText();
            pageView.cancelDraw();
        }
        muPDFReaderView.setMode(MuPDFReaderView.Mode.Viewing);
        switch (mAcceptMode) {
            case CopyText:
                mTopBarMode = TopBarMode.Main;
                break;
            default:
                mTopBarMode = TopBarMode.Annot;
                break;
        }
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    private void OnAcceptButtonClick() {
        MuPDFView pageView = (MuPDFView) muPDFReaderView.getDisplayedView();
        boolean success = false;
        switch (mAcceptMode) {
            case CopyText:
                if (pageView != null)
                    success = pageView.copySelection();
                mTopBarMode = TopBarMode.Main;
                showInfo(success ? getString(R.string.copied_to_clipboard) : getString(R.string.no_text_selected));
                break;
            case Highlight:
                if (pageView != null) {
                    success = pageView.markupSelection(Annotation.Type.HIGHLIGHT);
                }
                mTopBarMode = TopBarMode.Annot;
                if (!success) {
                    showInfo(getString(R.string.no_text_selected));
                }
                break;
            case Underline:
                if (pageView != null)
                    success = pageView.markupSelection(Annotation.Type.UNDERLINE);
                mTopBarMode = TopBarMode.Annot;
                if (!success)
                    showInfo(getString(R.string.no_text_selected));
                break;

            case StrikeOut:
                if (pageView != null)
                    success = pageView.markupSelection(Annotation.Type.STRIKEOUT);
                mTopBarMode = TopBarMode.Annot;
                if (!success)
                    showInfo(getString(R.string.no_text_selected));
                break;

            case Ink:
                if (pageView != null)
                    success = pageView.saveDraw();
                mTopBarMode = TopBarMode.Annot;
                if (!success)
                    showInfo(getString(R.string.nothing_to_save));
                break;
        }
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        muPDFReaderView.setMode(MuPDFReaderView.Mode.Viewing);
        toggleTool(View.GONE);
    }

    private void setButtonEnabled(ImageViewCustom button, boolean enabled) {
        button.setEnabled(enabled);
        button.setColorFilter(enabled ? Color.argb(0xFF, 250, 250, 250) : Color.argb(0xFF, 250, 250, 250));
    }

    private void search(int direction) {
        hideKeyboard();
        int displayPage = muPDFReaderView.getDisplayedViewIndex();
        SearchTaskResult r = SearchTaskResult.get();
        int searchPage = r != null ? r.pageNumber : -1;
        mSearchTask.go(et_searchText.getText().toString(), direction, displayPage, searchPage);
    }

    private void setLinkHighlight(boolean highlight) {
        mLinkHighlight = highlight;
        // LINK_COLOR tint
        mLinkButton.setColorFilter(highlight ? Color.argb(0xFF, 255, 160, 0) : Color.argb(0xFF, 255, 255, 255));
        // Inform pages of the change.
        muPDFReaderView.setLinksEnabled(highlight);
    }

    private void showInfo(String message) {

        LayoutInflater inflater = getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.toast, findViewById(R.id.toast_root_view));

        TextView header = toastLayout.findViewById(R.id.toast_message);
        header.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        toast.show();
    }

    @SuppressLint("StaticFieldLeak")
    public void createAlertWaiter() {
        mAlertsActive = true;
        // All mupdf library calls are performed on asynchronous tasks to avoid stalling
        // the UI. Some calls can lead to javascript-invoked requests to display an
        // alert dialog and collect a reply from the user. The task has to be blocked
        // until the user's reply is received. This method creates an asynchronous task,
        // the purpose of which is to wait of these requests and produce the dialog
        // in response, while leaving the core blocked. When the dialog receives the
        // user's response, it is sent to the core via replyToAlert, unblocking it.
        // Another alert-waiting task is then created to pick up the next alert.
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
        }
        mAlertTask = new AsyncTask<Void, Void, MuPDFAlert>() {

            @Override
            protected MuPDFAlert doInBackground(Void... arg0) {
                if (!mAlertsActive)
                    return null;
                Log.e("AsyncTask", "doInBackground");
                return muPDFCore.waitForAlert();
            }

            @Override
            protected void onPostExecute(final MuPDFAlert result) {
                // core.waitForAlert may return null when shutting down
                Log.e("AsyncTask", "onPostExecute");
                if (result == null)
                    return;
                final MuPDFAlert.ButtonPressed pressed[] = new MuPDFAlert.ButtonPressed[3];
                for (int i = 0; i < 3; i++)
                    pressed[i] = MuPDFAlert.ButtonPressed.None;
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mAlertDialog = null;
                        if (mAlertsActive) {
                            int index = 0;
                            switch (which) {
                                case AlertDialog.BUTTON1:
                                    index = 0;
                                    break;
                                case AlertDialog.BUTTON2:
                                    index = 1;
                                    break;
                                case AlertDialog.BUTTON3:
                                    index = 2;
                                    break;
                            }
                            result.buttonPressed = pressed[index];
                            // Send the user's response to the core, so that it can
                            // continue processing.
                            muPDFCore.replyToAlert(result);
                            // Create another alert-waiter to pick up the next alert.
                            createAlertWaiter();
                        }
                    }
                };
                mAlertDialog = mAlertBuilder.create();
                mAlertDialog.setTitle(result.title);
                mAlertDialog.setMessage(result.message);
                switch (result.iconType) {
                    case Error:
                        break;
                    case Warning:
                        break;
                    case Question:
                        break;
                    case Status:
                        break;
                }
                switch (result.buttonGroupType) {
                    case OkCancel:
                        mAlertDialog.setButton(AlertDialog.BUTTON2, getString(R.string.cancel), listener);
                        pressed[1] = MuPDFAlert.ButtonPressed.Cancel;
                    case Ok:
                        mAlertDialog.setButton(AlertDialog.BUTTON1, getString(R.string.okay), listener);
                        pressed[0] = MuPDFAlert.ButtonPressed.Ok;
                        break;
                    case YesNoCancel:
                        mAlertDialog.setButton(AlertDialog.BUTTON3, getString(R.string.cancel), listener);
                        pressed[2] = MuPDFAlert.ButtonPressed.Cancel;
                    case YesNo:
                        mAlertDialog.setButton(AlertDialog.BUTTON1, getString(R.string.yes), listener);
                        pressed[0] = MuPDFAlert.ButtonPressed.Yes;
                        mAlertDialog.setButton(AlertDialog.BUTTON2, getString(R.string.no), listener);
                        pressed[1] = MuPDFAlert.ButtonPressed.No;
                        break;
                }
                mAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        mAlertDialog = null;
                        if (mAlertsActive) {
                            result.buttonPressed = MuPDFAlert.ButtonPressed.None;
                            muPDFCore.replyToAlert(result);
                            createAlertWaiter();
                        }
                    }
                });

                mAlertDialog.show();
            }
        };
        mAlertTask.executeOnExecutor(new ThreadPerTaskExecutor());
    }

    public void destroyAlertWaiter() {
        mAlertsActive = false;
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
        }
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
    }

    @Override
    protected void onStart() {
        if (muPDFCore != null) {
            muPDFCore.startAlerts();
            createAlertWaiter();
        }
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSearchTask != null) {
            mSearchTask.stop();
        }
        for (int i = 0; i < readings.size(); i++) {
            Reading reading = readings.get(i);
            String pathWithPos = reading.getPath();
            String pathView = pdfInfo.getFilePath();
            if (pathWithPos.equals(pathView)) {
                long name = reading.getId();
                String KEY_NAME = "id";
                dbHelper.deleteReading(reading, KEY_NAME + " = " + name);
                break;
            }
        }
        try {
            if (dbHelper != null) {
                Reading reading = (new Reading(pdfInfo.getId(), Integer.parseInt(textView_pageShow.getText().toString()), pdfInfo.getFilePath()));
                dbHelper.insertPageReading(reading);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        if (muPDFCore != null) {
            destroyAlertWaiter();
            muPDFCore.stopAlerts();
        }
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        if (muPDFReaderView != null) {
            muPDFReaderView.applyToChildren(new ReaderView.ViewMapper() {
                public void applyToView(View view) {
                    ((MuPDFView) view).releaseBitmaps();
                }
            });
        }
        if (muPDFCore != null)
            muPDFCore.onDestroy();
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
        muPDFCore = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (muPDFCore != null && muPDFCore.hasChanges()) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == AlertDialog.BUTTON_POSITIVE) {
                        muPDFCore.save();
                    }
                    finish();
                }
            };
            AlertDialog alert = mAlertBuilder.create();
            alert.setTitle(R.string.dialog_title);
            alert.setMessage(getString(R.string.document_has_changes_save_them));
            alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), listener);
            alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), listener);
            alert.show();
        } else {
            if (mAlertTask != null) {
                mAlertTask.cancel(true);
                mAlertTask = null;
            }
            super.onBackPressed();
        }
    }

    @Override
    public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.horizontal:
                isHorizontal = !isHorizontal;
                SharedPreferencesIsCheckedUserData.setPrefBoolean(Constants.IS_SCROLL, isHorizontal, this);
                readingpage = muPDFReaderView.getDisplayedViewIndex() + 1;
                createPDF(pathFile, isHorizontal);
                return true;
            case R.id.moreButton:
                toggleTool(View.VISIBLE);
                OnCopyTextButtonClick();
                return true;
            case R.id.searchButton:
                toggleTool(View.VISIBLE);
                searchModeOn();
                return true;
            case R.id.outlineButton:
                if (muPDFCore.hasOutline()) {
                    OutlineItem outline[] = muPDFCore.getOutline();
                    if (outline != null) {
                        OutlineActivityData.get().items = outline;
                        Intent intent = new Intent(ViewPdfActivity.this, OutlineActivity.class);
                        startActivityForResult(intent, OUTLINE_REQUEST);
                    }
                } else {
                    showInfo(getResources().getString(R.string.message_no_support));
                }
                return true;
            case R.id.reflowButton:
                toggleTool(View.VISIBLE);
                mTopBarMode = TopBarMode.Annot;
                mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
                return true;
//            case R.id.linkButton:
//                setLinkHighlight(!mLinkHighlight);
//                return true;
//            case R.id.picture:
//                showInfo(getResources().getString(R.string.message_func));
////                visitLibraryPicture();
//                return true;
            default:
                return false;
        }
    }

    @Override
    public void onMenuModeChange(MenuBuilder menu) {
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.searchForward:
                search(1);
                break;
            case R.id.searchBack:
                search(-1);
                break;
            case R.id.cancelSearch:
                searchModeOff();
                break;
            case R.id.img_cancelEdt:
                mTopBarMode = TopBarMode.Main;
                mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
                toggleTool(View.GONE);
                break;
            case R.id.img_highlightEdt:
                OnHighlightButtonClick();
                break;
            case R.id.img_underLineEdt:
                OnUnderlineButtonClick();
                break;
            case R.id.img_strikeOutEdt:
                OnStrikeOutButtonClick();
                break;
            case R.id.img_inkEdt:
                OnInkButtonClick();
                break;
            case R.id.cancelDeleteButton:
                OnCancelDeleteButtonClick();
                break;
            case R.id.cancelAcceptButton:
                OnCancelAcceptButtonClick();
                toggleTool(View.GONE);
                break;
            case R.id.acceptButton:
                OnAcceptButtonClick();
                break;
            case R.id.img_cancelPic:
                toggleChoice(View.GONE);
                vdhDeepLayout.setVisibility(View.GONE);
                createPDF(pathFile, isHorizontal);
                break;
            case R.id.img_okPic:
                saveBitmap(bitmap);
                toggleChoice(View.GONE);
                break;
        }
    }

    private void visitLibraryPicture() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    class ThreadPerTaskExecutor implements Executor {
        public void execute(Runnable r) {
            new Thread(r).start();
        }
    }

    class SavePdfTask extends AsyncTask {

        SavePdf savePdf;

        public SavePdfTask(SavePdf savePdf) {
            showProgressDialog();
            this.savePdf = savePdf;
        }

        @Override
        protected Object doInBackground(Object[] params) {
            savePdf.addText();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                hideProgressDialog();
                showInfo(getResources().getString(R.string.message_add_pic));
//                finish();
                bitmap = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
