package com.pdfreader.editor.adapter;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.pdfreader.editor.fragment.FragmentAllFile;
import com.pdfreader.editor.fragment.FragmentFavorite;
import com.pdfreader.editor.fragment.FragmentHistory;
import com.pdfreader.editor.model.PDFInfo;

import java.util.ArrayList;
import java.util.List;

public class FragmentPagerAdapter extends androidx.fragment.app.FragmentPagerAdapter {

    private Context mContext;
    private String pathOpen;
    private List<PDFInfo> pdfInfoList = new ArrayList<>();

    public FragmentPagerAdapter(Context context, FragmentManager fm, String pathOpen) {
        super(fm);
        this.mContext = context;
        this.pathOpen = pathOpen;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (position == 0) {
            fragment = FragmentAllFile.newInstance(pathOpen);
        } else if (position == 1){
            fragment = FragmentHistory.newInstance();
        } else if (position == 2){
            fragment = FragmentFavorite.newInstance();
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

//    @Nullable
//    @Override
//    public CharSequence getPageTitle(int position) {
//        // Generate title based on item position
//        switch (position) {
//            case 0:
//                return mContext.getString(R.string.category_);
//            case 1:
//                return mContext.getString(R.string.category_places);
//            case 2:
//                return mContext.getString(R.string.category_food);
//            default:
//                return null;
//        }
//    }
//    }
}
