package com.pdfreader.editor.base;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import butterknife.ButterKnife;

public abstract class BaseViewHolder extends RecyclerView.ViewHolder{

    protected Context mContext;
    private int mCurrentPosition;


    public BaseViewHolder(View itemView, Context context) {
        super(itemView);
        mContext = context;
        ButterKnife.bind(this,itemView);

    }

    protected abstract void clear();

    public void onBind(int position) {
        mCurrentPosition = position;
        clear();
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }


}
