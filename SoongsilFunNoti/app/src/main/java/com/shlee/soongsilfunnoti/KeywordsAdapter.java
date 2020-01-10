package com.shlee.soongsilfunnoti;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class KeywordsAdapter extends RecyclerView.Adapter<KeywordsAdapter.ViewHolder> {
    ArrayList<String> keywordArrayList;

    public interface OnItemClickListner{
        void onItemClick(View view, int pos);
    }

    private OnItemClickListner listner = null;

    public void setOnItemClickListener(OnItemClickListner listener){
        this.listner = listener;
    }

    // 아이템 뷰를 저장하는 뷰 홀더 클래스
    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView keyword;
        ImageView minusIcon;

        ViewHolder(View itemView){
            super(itemView);

            //뷰 객체에 대한 참조.
            keyword = itemView.findViewById(R.id.text_keyword);
            minusIcon = itemView.findViewById(R.id.image_minus);

            minusIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(view.getId() == R.id.image_minus){
                        if(listner != null){
                            listner.onItemClick(view, position);
                        }
                    }
                }
            });
        }
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    KeywordsAdapter(ArrayList<String> keywordArrayList){
        this.keywordArrayList = keywordArrayList;
    }

    // 아이템 뷰를 위한 뷰홀더 객체를 생성하여 리턴
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.layout_keyword, parent, false);
        KeywordsAdapter.ViewHolder viewHolder = new KeywordsAdapter.ViewHolder(view);

        return viewHolder;
    }

    // position에 해당하는 데이터를 뷰 홀더의 아이템뷰에 표시
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String string = keywordArrayList.get(position);
        holder.keyword.setText(string);
    }

    // 전체 데이터 갯수 리턴
    @Override
    public int getItemCount() {
        return keywordArrayList.size();
    }
}