package com.shlee.soongsilfunnoti;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProgramsAdapter extends RecyclerView.Adapter<ProgramsAdapter.ViewHolder> {
    ArrayList<Program> programArrayList;

    public interface OnItemClickListner{
        void onItemClick(View view, int pos);
    }

    private OnItemClickListner listner = null;

    public void setOnItemClickListener(OnItemClickListner listener){
        this.listner = listener;
    }

    // 아이템 뷰를 저장하는 뷰 홀더 클래스
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        TextView department;
        TextView date;
        TextView d_day;
        TextView url;
        TextView remainingDate;
        RelativeLayout background;

        ViewHolder(View itemView){
            super(itemView);

            //뷰 객체에 대한 참조.
            title = itemView.findViewById(R.id.text_program_title);
            department = itemView.findViewById(R.id.text_department);
            date = itemView.findViewById(R.id.text_date);
            d_day = itemView.findViewById(R.id.text_D_Day);
            url = itemView.findViewById(R.id.text_url);
            remainingDate = itemView.findViewById(R.id.text_remaining_date);
            background = itemView.findViewById(R.id.relative_layout_program);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position!=RecyclerView.NO_POSITION){
                        if(listner != null){
                            listner.onItemClick(view, position);
                        }
                    }
                }
            });
        }
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    ProgramsAdapter(ArrayList<Program> programArrayList){
        this.programArrayList = programArrayList;
    }

    // 아이템 뷰를 위한 뷰홀더 객체를 생성하여 리턴
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.layout_program, parent, false);
        ProgramsAdapter.ViewHolder viewHolder = new ProgramsAdapter.ViewHolder(view);

        return viewHolder;
    }

    // position에 해당하는 데이터를 뷰 홀더의 아이템뷰에 표시
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Program program = programArrayList.get(position);
        holder.title.setText(program.getTitle());
        holder.department.setText(program.getDepartment());
        holder.d_day.setText(program.getD_day());
        holder.date.setText(program.getDate());
        holder.url.setText(program.getURL());
        holder.remainingDate.setText(String.valueOf(program.getRemainingDate()));
        if(program.isHighlight()) holder.background.setBackgroundColor(ContextCompat.getColor(holder.background.getContext(),R.color.colorHighlight));
        else holder.background.setBackgroundColor(ContextCompat.getColor(holder.background.getContext(),R.color.colorPlate));
    }

    // 전체 데이터 갯수 리턴
    @Override
    public int getItemCount() {
        return programArrayList.size();
    }
}
