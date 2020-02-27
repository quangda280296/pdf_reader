package com.pdfreader.editor.fragment;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.pdfreader.editor.adapter.Adapter;
import com.pdfreader.editor.base.BaseFragment;
import com.pdfreader.editor.dialog.DialogDetail;
import com.pdfreader.editor.dialog.DialogRenameFolder;
import com.pdfreader.editor.dialog.DialogShareFolder;
import com.pdfreader.editor.model.Favorite;
import com.pdfreader.editor.model.PDFInfo;
import com.pdfreader.editor.model.Reading;
import com.pdfreader.editor.splite.DatabaseUtil;
import com.pdfreader.editor.utils.Constants;
import com.pdfreader.editor.utils.Utils;
import com.pdfreader.editor.view.MainActivity;
import com.pdfreader.editor.view.ViewPdfActivity;
import com.pdfreader.pdf.reader.editor.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;

public class FragmentAllFile extends BaseFragment implements Adapter.ClickedItemlistener, DialogRenameFolder.ListenerSuccessfully {

    private static final int VIEW_PDF = 1103;
    @BindView(R.id.recyclerview_data)
    RecyclerView recyclerView_data;

    @BindView(R.id.swiperefreshlayout)
    SwipeRefreshLayout swipeRefreshLayout_data;

    @BindView(R.id.textview_empty)
    TextView textView_empty;

    @BindView(R.id.imageview_empty)
    ImageView imageView_empty;


    public static boolean isUpdate = false;
    private boolean isShowEmpty = false;
    private ArrayList<PDFInfo> pdfInfoArrayList = new ArrayList<>();
    private ArrayList<PDFInfo> filteredList = new ArrayList<>();
    private Adapter adapter;
    private boolean isShow = false;
    private DatabaseUtil dbHelper;
    private PDFInfo pdfInfo;
    private String itemName = "";
    private String pathOpen;
    private Reading reading;
    private List<Reading> readings = new ArrayList<>();

    public static Fragment newInstance(String pathOpen) {
        FragmentAllFile fragment = new FragmentAllFile();
        fragment.openFileWithAction(pathOpen);
        return fragment;
    }

    public void openFileWithAction(String pathOpen) {
        this.pathOpen = pathOpen;
    }

    @Override
    public int getViewResource() {
        return R.layout.fragment_all_file;
    }

    @Override
    public void setListener() {

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(listenerEventSearch,
                new IntentFilter(Constants.ACTION_SEARCH));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(listenerEventFile,
                new IntentFilter(Constants.EVENT_ACTION));
        adapter = new Adapter(getActivity());
        adapter.OnClicklistener(this);
        dbHelper = DatabaseUtil.getInstant(getActivity());
        try {
            recyclerView_data.setHasFixedSize(true);
        } catch (Exception ex) {
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView_data.setLayoutManager(layoutManager);
        recyclerView_data.setAdapter(adapter);
    }

    @Override
    public void retrieveData() {
        pdfInfoArrayList.clear();
        swipeRefreshLayout_data.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerView_data.setVisibility(View.INVISIBLE);
                retrieveData();
                swipeRefreshLayout_data.setRefreshing(false);
            }
        });
        if (getContext() == null || isDetached()) return ;
        if (hasReadPermissions() && hasWritePermissions()) {
            new AsyncLoadListPDF().execute();
        }
    }

    private void getAllFilePDF(File dir) {
        File[] files = dir.listFiles();
        if (files == null)
            return;
        for (File file : files) {
            if (file.isFile()) {
                PDFInfo pdfInfo = new PDFInfo();
                String suffix = getSuffix(file.getName());
                if (suffix.equals("pdf")) {
                    pdfInfo.setId(System.currentTimeMillis() + getRandomNumber());
                    pdfInfo.setFileName(file.getName());
                    pdfInfo.setFilePath(file.getPath());
                    pdfInfoArrayList.add(pdfInfo);
                }
            } else if (file.isDirectory()) {
                getAllFilePDF(file.getAbsoluteFile());
            }
        }
        pdfInfoArrayList = Utils.sortByNameAZ(pdfInfoArrayList);
    }

    private long getRandomNumber() {
        return (new Random()).nextInt((5000000 - 50) + 1) + 10000;
    }

    private String getSuffix(String nameFile) {
        String suffix = nameFile.substring(nameFile.lastIndexOf(".") + 1).toLowerCase();
        return suffix;
    }


    @Override
    public void OnClickedItem(PDFInfo pdfInfo) {
        Log.e("OnClickedItem", "OnClickedItem //// ");
        listenerScrollPage();
        setEventUpdateList();
//        if (getList().size() == 0) insertTabHistory(pdfInfo);
//        if (!isCheckFileExists(pdfInfo)) insertTabHistory(pdfInfo);
        isCheckFileExists(pdfInfo);
        insertTabHistory(pdfInfo);
        openViewFile(pdfInfo);
    }

    private void openViewFile(PDFInfo pdfInfo) {
        Intent intent = new Intent(getActivity(), ViewPdfActivity.class);
        intent.putExtra(Constants.PATH_PDF, pdfInfo);
        startActivityForResult(intent, VIEW_PDF);
    }

    public List<PDFInfo> getList() {
        if (dbHelper == null) return null;
        List<PDFInfo> list = dbHelper.getList();
        return list;
    }

    private void setLayoutTextEmpty(ArrayList<PDFInfo> pdfInfoArrayList) {
        if (getContext() == null || isDetached()) return ;
        if (!hasReadPermissions() && !hasWritePermissions()) {
            textView_empty.setVisibility(View.GONE);
            imageView_empty.setVisibility(View.GONE);
            return;
        }
        if (pdfInfoArrayList != null && pdfInfoArrayList.size() == 0) {
            textView_empty.setText(getString(R.string.no_result));
            textView_empty.setVisibility(View.VISIBLE);
            imageView_empty.setVisibility(View.VISIBLE);
        } else {
            textView_empty.setVisibility(View.GONE);
            imageView_empty.setVisibility(View.GONE);
        }
    }

    private boolean hasReadPermissions() {
        return (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasWritePermissions() {
        return (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private void isCheckFileExists(PDFInfo pdfInfo) {
        for (int i = 0; i < getList().size(); i++) {
            if (getList().get(i).getFileName().equals(pdfInfo.getFileName())) {
                deleteListHistory(pdfInfo, getList().get(i).getId());
            }
        }
    }

    private void deleteListHistory(PDFInfo pdfInfo, long id) {
        String KEY_NAME = "id";
        dbHelper.deleteHistory(pdfInfo, KEY_NAME + " = " + id);
    }

    @Override
    public void OnClickedMoreItem(File file, String eventWorking, PDFInfo pdfInfo) {
        listenerScrollPage();
        if (pdfInfo != null) itemName = pdfInfo.getFileName();
        switch (eventWorking) {
            case Constants.RENAME:
                DialogRenameFolder dialogRenameFolder = new DialogRenameFolder();
                dialogRenameFolder.setListenerSuccessfully(this);
                dialogRenameFolder.showDialog(getActivity(), file.getPath(), pdfInfoArrayList);
                break;
            case Constants.SHARE:
                setEventShare(file);
                break;
            case Constants.DELETE:
                setViewDialog(getString(R.string.title_delete), getString(R.string.message_delete), file, pdfInfo);
                break;
            case Constants.DETAIL:
                DialogDetail dialogDetail = new DialogDetail();
                dialogDetail.showDialog(getActivity(), file.getParent(), file.getPath());
                break;
            default:
                break;
        }
    }

    private void setViewDialog(String title, String message, File file, PDFInfo pdfInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (file != null) {
                            file.delete();
                            isUpdate = true;
                            setEventFile(pdfInfo, null, Constants.DELETE, null, null);
                        }

                        dialog.cancel();
                    }
                });

        if (title.equals(getString(R.string.title_delete))) {
            builder.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
        }

        AlertDialog alert11 = builder.create();
        alert11.show();
    }

    private void setEventShare(File file) {
        String pathShare = file.getPath();
        DialogShareFolder dialogShareFolder = new DialogShareFolder();
        dialogShareFolder.startShare(getActivity(), pathShare);
    }

    private BroadcastReceiver listenerEventSearch = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isShow && isShowEmpty) {
                String txtSearch = intent.getStringExtra(Constants.TXT_SEARCH);
                if (txtSearch.equals("")) {
                    adapter.getData(pdfInfoArrayList);
                    setLayoutTextEmpty(pdfInfoArrayList);
                    Log.e("setLayoutTextEmpty", "listenerEventSearch");
                } else filter(txtSearch);
            }
        }
    };

    private BroadcastReceiver listenerEventFile = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String typeEvent = intent.getStringExtra(Constants.TYPE_ACTION);
            switch (typeEvent) {
                case Constants.DELETE:
                    PDFInfo pdfInfo = (PDFInfo) intent.getSerializableExtra(Constants.DATA_PDF);
                    Favorite favorite = (Favorite) intent.getSerializableExtra(Constants.DATA_FAVORITE);
                    if (pdfInfo != null) {
                        String pathFile = pdfInfo.getFilePath();
                        File file = new File(pathFile);
                        file.delete();
                        isUpdate = true;
                        retrieveData();
                    } else if (favorite != null) {
                        String pathFile = favorite.getPathFile();
                        File file = new File(pathFile);
                        file.delete();
                        isUpdate = true;
                        retrieveData();
                    }
                    break;
                case Constants.RENAME:

                    break;
            }
        }
    };

    private void filter(String text) {
        filteredList = new ArrayList<>();
        for (PDFInfo item : pdfInfoArrayList) {
            if (item.getFileName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapter.getData(filteredList);
        Log.e("setLayoutTextEmpty", "filter");
        setLayoutTextEmpty(filteredList);
    }

    public void insertTabHistory(PDFInfo tablePDFInfo) {
        try {
            if (dbHelper != null) {
                dbHelper.insertPDFInfo(tablePDFInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // sent action update to fragmentHistory on viewPager
    private void setEventUpdateList() {
        Intent intent = new Intent(Constants.ACTION_HISTORY);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

//    public void upDateLayout(boolean isUpdate){
//        isUpdateData = isUpdate;
//    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            isShow = true;
            listenerScrollPage();
            if (isUpdate) {
                isUpdate = true;
                retrieveData();
                isUpdate = false;
            }
        } else isShow = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(listenerEventSearch);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(listenerEventFile);
    }

    @Override
    public void CallBackStateSuccess(boolean isUpdateData, File newFile) {
        this.isUpdate = isUpdateData;
        setEventFile(null, null, Constants.RENAME, newFile, itemName);
    }

    private void listenerScrollPage() {
        try {
            ((MainActivity) getActivity()).listenerUpdateLayoutSearch();
        } catch (Exception ex) {
        }
    }

    private void setEventFile(PDFInfo pdfInfo, Favorite favorite, String event, File newFile, String itemName) {
        ((MainActivity) getActivity()).listenerEventFile(pdfInfo, null, event, newFile, itemName);
        retrieveData();
    }

    private class AsyncLoadListPDF extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getAllFilePDF(Environment.getExternalStorageDirectory());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (isCancelled()) return;
            if (getActivity() != null && !getActivity().isFinishing()) {
                try {
                    adapter.getData(pdfInfoArrayList);
                } catch (Exception ex) {
                }
                isShowEmpty = true;
                setLayoutTextEmpty(pdfInfoArrayList);
                hideProgressDialog();
                recyclerView_data.setVisibility(View.VISIBLE);
                if (pathOpen != null) {
                    for (int i = 0; i < pdfInfoArrayList.size(); i++) {
                        PDFInfo pdfInfo = pdfInfoArrayList.get(i);
                        if (pdfInfo.getFileName().equals(pathOpen)) {
                            setEventUpdateList();
                            isCheckFileExists(pdfInfo);
                            insertTabHistory(pdfInfo);
                            openViewFile(pdfInfo);
                            pathOpen = null;
                            break;
                        }
                    }
                }
            }
        }
    }

}
