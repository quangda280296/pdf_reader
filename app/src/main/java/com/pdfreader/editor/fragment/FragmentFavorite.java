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
import com.pdfreader.editor.utils.Utils;
import com.pdfreader.editor.view.MainActivity;
import com.pdfreader.editor.view.ViewPdfActivity;
import com.pdfreader.pdf.reader.editor.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class FragmentFavorite extends BaseFragment implements Adapter.ClickedItemlistener, DialogRenameFolder.ListenerSuccessfully {


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
    private ArrayList<Favorite> favorites = new ArrayList<>();
    private ArrayList<Favorite> favoriteUpdate = new ArrayList<>();
    private File fileReturnOnClick;
    private Adapter adapter;
    private boolean isShow = false;
    private DatabaseUtil dbHelper;

    public static FragmentFavorite newInstance() {
        FragmentFavorite fragment = new FragmentFavorite();
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
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(listenerEventFile,
                new IntentFilter(Constants.EVENT_ACTION));
        adapter = new Adapter(getActivity());
        adapter.OnClicklistener(this);
        dbHelper = DatabaseUtil.getInstant(getActivity());

    }

    @Override
    public void retrieveData() {
        if (pdfInfoArrayList.size() > 0) pdfInfoArrayList.clear();
        recyclerView_data.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView_data.setLayoutManager(layoutManager);
        recyclerView_data.setAdapter(adapter);

        swipeRefreshLayout_data.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerView_data.setVisibility(View.INVISIBLE);
                retrieveData();
                swipeRefreshLayout_data.setRefreshing(false);
            }
        });
        getDataListFavorite();
    }

    private void getDataListFavorite() {
        favorites = (ArrayList<Favorite>) getList();
        for (int i = 0; i < favorites.size(); i++) {
            PDFInfo pdfInfo = new PDFInfo();
            pdfInfo.setId(favorites.get(i).getId());
            pdfInfo.setFileName(favorites.get(i).getNameFile());
            pdfInfo.setFilePath(favorites.get(i).getPathFile());
            pdfInfoArrayList.add(pdfInfo);
        }
        pdfInfoArrayList = Utils.sortByNameAZ(pdfInfoArrayList);
        checkIsFileEmpty(pdfInfoArrayList);
    }

    private void checkIsFileEmpty(ArrayList<PDFInfo> pdfInfoArrayList) {
        for (int i = 0; i < pdfInfoArrayList.size(); i++) {
            File file = new File(pdfInfoArrayList.get(i).getFilePath());
            if (file.length() == 0) {
                deleteListFavorite(pdfInfoArrayList.get(i).getId());
                pdfInfoArrayList.remove(i);
            }
        }
        adapter.getData(pdfInfoArrayList);
        recyclerView_data.setVisibility(View.VISIBLE);
        setLayoutTextEmpty(pdfInfoArrayList);
    }

    @Override
    public void OnClickedItem(PDFInfo pdfInfo) {
        listenerScrollPage();
        updateListHistory(pdfInfo);
        Intent intent = new Intent(getActivity(), ViewPdfActivity.class);
        intent.putExtra(Constants.PATH_PDF, pdfInfo);
        startActivity(intent);
    }

    private void updateListHistory(PDFInfo pdfInfo) {
        deleteListHistory(pdfInfo, pdfInfo.getId());
        insertTabHistory(pdfInfo);
        FragmentHistory.isUpdate = true;
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
                        if (file != null) {
                            Favorite favorite = new Favorite(pdfInfo.getId(), pdfInfo.getFilePath(), pdfInfo.getFileName());

                            setEventFile(pdfInfo, favorite, Constants.DELETE, null, null);
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
            if (isShow) {
                String txtSearch = intent.getStringExtra(Constants.TXT_SEARCH);
                if (txtSearch.equals("")) {
                    adapter.getData(pdfInfoArrayList);
                    setLayoutTextEmpty(pdfInfoArrayList);
                } else filter(txtSearch);
            }
        }
    };

    private void deleteListFavorite(long id) {
        String KEY_NAME = "id";
        ArrayList<Favorite> favorites = new ArrayList<>();
        favorites = (ArrayList<Favorite>) getList();
        for (int i = 0; i < favorites.size(); i++) {
            if (id == favorites.get(i).getId()) {
                Favorite favorite = favorites.get(i);
                dbHelper.deleteFavorite(favorite, KEY_NAME + " = " + id);
                break;
            }
        }
    }

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

    public List<Favorite> getList() {
        if (dbHelper == null) return null;
        List<Favorite> list = dbHelper.getListFavorite();
        return list;
    }

    private void setLayoutTextEmpty(ArrayList<PDFInfo> pdfInfoArrayList) {
        if (pdfInfoArrayList != null && pdfInfoArrayList.size() == 0) {
            textView_empty.setText(getString(R.string.no_result));
            textView_empty.setVisibility(View.VISIBLE);
            imageView_empty.setVisibility(View.VISIBLE);
        } else {
            textView_empty.setVisibility(View.GONE);
            imageView_empty.setVisibility(View.GONE);
        }
    }


    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            favoriteUpdate = (ArrayList<Favorite>) getList();
            if (favorites.size() != favoriteUpdate.size()) isUpdate = true;
            isShow = true;
            listenerScrollPage();
            if (isUpdate) {
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
        String itemNameFavorite = fileReturnOnClick.getName();
        setEventFile(null, null, Constants.RENAME, newFile, itemNameFavorite);
    }

    private void listenerScrollPage() {
        try {
            ((MainActivity) getActivity()).listenerUpdateLayoutSearch();
        } catch (Exception ex) {
        }
    }

    private void setEventFile(PDFInfo pdfInfo, Favorite favorite, String event, File newFile, String itemName) {
        ((MainActivity) getActivity()).listenerEventFile(pdfInfo, favorite, event, newFile, itemName);
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
                        retrieveData();
                    } else if (favorite != null) {
                        String pathFile = favorite.getPathFile();
                        File file = new File(pathFile);
                        file.delete();

                        // delete db
                        deleteListFavorite(favorite.getId());
                        retrieveData();
                    }
                    break;
                case Constants.RENAME:

                    break;
            }
        }
    };

}
