package com.pdfreader.editor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfreader.editor.model.PDFInfo;
import com.pdfreader.editor.utils.Constants;
import com.pdfreader.editor.utils.Utils;
import com.pdfreader.pdf.reader.editor.R;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private Context mContext;
    private String artDate = "";
    private DateFormat dateFormat;
    private ArrayList<PDFInfo> pdfInfoArrayList = new ArrayList<>();
    private ClickedItemlistener clickeditemlistener;

    public Adapter(Context context){
        this.mContext = context;
    }

    public void getData (ArrayList<PDFInfo> pdfInfoArrayList){
//        for (int i = 0; i<pdfInfoArrayList.size(); i++){
//            if (pdfInfoArrayList.get(i).getId() == 0)return;
//        }
        this.pdfInfoArrayList = pdfInfoArrayList;
        dateFormat = new SimpleDateFormat("dd/MMM/yyyy");
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_data, parent, false);
        return new Adapter.ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PDFInfo pdfInfo = pdfInfoArrayList.get(position);
        holder.displayItemView(pdfInfo);

    }

    @Override
    public int getItemCount() {
        return pdfInfoArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener{

        private ImageView imageView_icon;
        private ImageView imageView_more;
        private TextView textView_nameFile;
        private TextView textView_date;
        private TextView textView_size;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView_icon = itemView.findViewById(R.id.img_icon);
            imageView_more = itemView.findViewById(R.id.img_more);
            textView_nameFile = itemView.findViewById(R.id.txt_nameFile);
            textView_date = itemView.findViewById(R.id.txt_date);
            textView_size = itemView.findViewById(R.id.txt_size);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickeditemlistener.OnClickedItem(pdfInfoArrayList.get(getAdapterPosition()));
                }
            });

            imageView_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(mContext, imageView_more);
                    // This activity implements OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(ViewHolder.this::onMenuItemClick);
                    popup.inflate(R.menu.actions_more);
                    popup.show();
                }
            });
        }

        private void displayItemView(PDFInfo pdfInfo){
            File file = new File(pdfInfo.getFilePath());
            Date lastModDate = new Date(file.lastModified());
            artDate = dateFormat.format(lastModDate);

            textView_nameFile.setText(pdfInfo.getFileName());
            textView_size.setText(Utils.formatFileSize(file.length()));
            textView_date.setText(artDate);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            File file = new File(pdfInfoArrayList.get(getAdapterPosition()).getFilePath());
            switch (item.getItemId()) {
                case R.id.resume:
                    clickeditemlistener.OnClickedMoreItem(file, Constants.RENAME, pdfInfoArrayList.get(getAdapterPosition()));
                    return true;
                case R.id.share:
                    clickeditemlistener.OnClickedMoreItem(file, Constants.SHARE, null);
                    return true;
                case R.id.delete:
                    clickeditemlistener.OnClickedMoreItem(file, Constants.DELETE, pdfInfoArrayList.get(getAdapterPosition()));
                    return true;
                case R.id.detail:
                    clickeditemlistener.OnClickedMoreItem(file, Constants.DETAIL, null);
                    return true;
                default:
                    return false;
            }
        }
    }

    public void OnClicklistener(ClickedItemlistener listener){
        this.clickeditemlistener = listener;
    }

    public interface ClickedItemlistener{
        void OnClickedItem(PDFInfo pdfInfo);
        void OnClickedMoreItem(File file, String eventWorking, PDFInfo pdfInfo);
    }
}
