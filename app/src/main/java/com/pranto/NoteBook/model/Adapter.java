package com.pranto.NoteBook.model;

import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.pranto.NoteBook.Note.NoteDetailsActivity;
import com.pranto.NoteBook.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    List<String> titles;
    List<String> content;

    public Adapter(List<String> titles, List<String> content) {
        this.titles = titles;
        this.content = content;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view_layout,parent,false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.noteTitle.setText(titles.get(position));
        holder.noteContent.setText(content.get(position));
        int colorCode = getRandomColor();
        holder.cardView.setCardBackgroundColor(holder.view.getResources().getColor(colorCode,null));

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), NoteDetailsActivity.class);
                intent.putExtra("content",content.get(position));
                intent.putExtra("title",titles.get(position));
                intent.putExtra("colorCode",colorCode);
                v.getContext().startActivity(intent);
            }
        });
    }

    private int getRandomColor() {
        List<Integer> colorCode = new ArrayList<>();
        colorCode.add(R.color.bg0);
        colorCode.add(R.color.bg1);
        colorCode.add(R.color.bg2);
        colorCode.add(R.color.bg3);
        colorCode.add(R.color.bg4);
        colorCode.add(R.color.bg5);
        colorCode.add(R.color.bg6);
        colorCode.add(R.color.bg7);
        colorCode.add(R.color.bg8);
        colorCode.add(R.color.bg9);
        colorCode.add(R.color.bg10);

        Random randomColor = new Random();
        int number = randomColor.nextInt(colorCode.size());
        return colorCode.get(number);
    }


    @Override
    public int getItemCount() {
        return titles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView noteTitle,noteContent;
        View view;
        CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            noteTitle = itemView.findViewById(R.id.titleId);
            noteContent = itemView.findViewById(R.id.contentId);
            view = itemView;
            cardView = itemView.findViewById(R.id.notecardId);
        }
    }
}
