package com.pdfreader.editor.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
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
import com.pdfreader.editor.splite.DatabaseUtil;
import com.pdfreader.editor.utils.Constants;
import com.pdfreader.editor.view.MainActivity;
import com.pdfreader.editor.view.ViewPdfActivity;
import com.pdfreader.pdf.reader.editor.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;


public class FragmentHistory extends BaseFragment implements Adapter.ClickedItemlistener, DialogRenameFolder.ListenerSuccessfully {
//implements Adapter.ClickedItemlistener, DialogRenameFolder.ListenerSuccessfully

    public static boolean isUpdate = false;
    @BindView(R.id.recyclerview_data)
    RecyclerView recyclerView_data;

    @BindView(R.id.swiperefreshlayout)
    SwipeRefreshLayout swipeRefreshLayout_data;

    @BindView(R.id.textview_empty)
    TextView textView_empty;

    @BindView(R.id.imageview_empty)
    ImageView imageView_empty;

    private ArrayList<PDFInfo> pdfInfoArrayList = new ArrayList<>();
    private ArrayList<PDFInfo> filteredList = new ArrayList<>();
    private Adapter adapter;
    private boolean isShow = false;
    private DatabaseUtil dbHelper;
    private File fileReturnOnClick;


    public static FragmentHistory newInstance() {
        FragmentHistory fragment = new FragmentHistory();
        return fragment;
    }

    @Override
    public int getViewResource() {
        return R.layout.fragment_all_file;
    }

    @Override
    public void setListener() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(listenerEventSearch,
                new IntentFilter(Constants.ACTION_SEARCH));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(listenerEventUpdateItem,
                new IntentFilter(Constants.ACTION_HISTORY));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(listenerEventFile,
                new IntentFilter(Constants.EVENT_ACTION));
        adapter = new Adapter(getActivity());
        adapter.OnClicklistener(this);
        dbHelper = DatabaseUtil.getInstant(getActivity());
    }

    @Override
    public void retrieveData() {

        recyclerView_data.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView_data.setLayoutManager(layoutManager);
        recyclerView_data.setAdapter(adapter);
//        ((SimpleItemAnimator)recyclerView_data.getItemAnimator()).setSupportsChangeAnimations(false);

        pdfInfoArrayList = (ArrayList<PDFInfo>) getList();
        checkIsFileEmpty(pdfInfoArrayList);
        swipeRefreshLayout_data.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerView_data.setVisibility(View.INVISIBLE);
                retrieveData();
                swipeRefreshLayout_data.setRefreshing(false);
            }
        });

    }

    private void checkIsFileEmpty(ArrayList<PDFInfo> pdfInfoArrayList) {
        for (int i = 0; i<pdfInfoArrayList.size(); i++){
            File file = new File(pdfInfoArrayList.get(i).getFilePath());
            if (file.length() == 0){
                deleteHistory(pdfInfoArrayList.get(i).getId());
                pdfInfoArrayList.remove(i);
        }
        }
        adapter.getData(pdfInfoArrayList);
        recyclerView_data.setVisibility(View.VISIBLE);
        setLayoutTextEmpty(pdfInfoArrayList);
    }

    private BroadcastReceiver listenerEventSearch = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

                    if (isShow) {
                        String txtSearch = intent.getStringExtra(Constants.TXT_SEARCH);
                        if (txtSearch.equals("")) {
                            adapter.getData(pdfInfoArrayList);
                            setLayoutTextEmpty(pdfInfoArrayList);
                        } else filter(txtSearch);
                    }
        }
    };

    private void deleteHistory(long id) {
        String KEY_NAME = "id";
        ArrayList<PDFInfo> pdfInfos = new ArrayList<>();
        pdfInfos = (ArrayList<PDFInfo>) getList();
        for (int i = 0; i<pdfInfos.size(); i++){
            if (id == pdfInfos.get(i).getId()){
                PDFInfo pdfInfo = pdfInfos.get(i);
                dbHelper.deleteHistory(pdfInfo, KEY_NAME + " = " + id);
                break;
            }
        }
    }
    private BroadcastReceiver listenerEventUpdateItem = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            retrieveData();
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
        setLayoutTextEmpty(filteredList);
    }

    private void setLayoutTextEmpty(ArrayList<PDFInfo> pdfInfoArrayList){
        if (pdfInfoArrayList != null && pdfInfoArrayList.size() == 0){
            textView_empty.setText(getString(R.string.no_result));
            textView_empty.setVisibility(View.VISIBLE);
            imageView_empty.setVisibility(View.VISIBLE);
        }else {
            textView_empty.setVisibility(View.GONE);
            imageView_empty.setVisibility(View.GONE);
        }
    }

    public List<PDFInfo> getList() {
        if (dbHelper == null) return null;
        List<PDFInfo> list = dbHelper.getList();
        Collections.reverse(list);
        return list;
    }

    @Override
    public void OnClickedItem(PDFInfo pdfInfo) {
        listenerScrollPage();
        updateList(pdfInfo);
        isUpdate = true;
        Intent intent = new Intent(getActivity(), ViewPdfActivity.class);
        intent.putExtra(Constants.PATH_PDF, pdfInfo);
        startActivity(intent);
    }

    private void updateList(PDFInfo pdfInfo){
        deleteListHistory(pdfInfo, pdfInfo.getId());
        insertTabHistory(pdfInfo);
    }

    private void deleteListHistory(PDFInfo pdfInfo, long id) {
        String KEY_NAME = "id";
        dbHelper.deleteHistory(pdfInfo, KEY_NAME + " = " + id);
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

    @Override
    public void OnClickedMoreItem(File file, String eventWorking, PDFInfo pdfInfo) {
        listenerScrollPage();
        fileReturnOnClick = file;
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
                        if (file != null){
                            setEventFile(pdfInfo, null, Constants.DELETE, null, null);
                        }
                        dialog.cancel();
                    }
                });

        if (title.equals(getString(R.string.title_delete))){
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

    @Override
    public void onResume() {
        super.onResume();
        if (isUpdate){
            retrieveData();
            isUpdate = false;
        }
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            isShow = true;
            listenerScrollPage();
            if (isUpdate){
                retrieveData();
                isUpdate = false;
            }
        } else isShow = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(listenerEventSearch);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(listenerEventUpdateItem);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(listenerEventFile);
    }

    @Override
    public void CallBackStateSuccess(boolean isUpdateData, File newFile) {
        String itemNameFavorite = fileReturnOnClick.getName();
//        isUpDateRename(newFile, itemNameFavorite);
//        ArrayList<Favorite> favorites = new ArrayList<>();
//        upDateListFavorite(favorites, newFile);
        setEventFile(null, null, Constants.RENAME, newFile,itemNameFavorite);
    }

    private void listenerScrollPage(){
        ((MainActivity)getActivity()).listenerUpdateLayoutSearch();
    }
    private void setEventFile(PDFInfo pdfInfo, Favorite favorite, String event, File newFile, String itemName) {
        ((MainActivity) getActivity()).listenerEventFile(pdfInfo, null, event, newFile, itemName);
        retrieveData();
    }

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
                        // delete db
                        deleteListHistory(pdfInfo);
                        retrieveData();
                    } else if (favorite != null) {
                        String pathFile = favorite.getPathFile();
                        File file = new File(pathFile);
                        file.delete();
                    }
                    break;
                case Constants.RENAME:

                    break;
            }
        }
    };

    private void deleteListHistory(PDFInfo pdfInfo) {
        String KEY_NAME = "id";
        long name = pdfInfo.getId();
        dbHelper.deleteHistory(pdfInfo, KEY_NAME + " = " + name);
    }
}
