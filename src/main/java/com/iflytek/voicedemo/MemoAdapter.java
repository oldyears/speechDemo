package com.iflytek.voicedemo;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.MemoViewHolder> {

    private Context context;

    private List<Memo> memos;

    public MemoAdapter(Context context) {
        this.context = context;
        this.memos = MemoManager.loadMemos(context);
    }

    @NonNull
    @Override
    public MemoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_memo, parent, false);
        return new MemoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Memo memo = memos.get(position);
        holder.titleTextView.setText(memo.getTitle());
        holder.contentTextView.setText(memo.getContent());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MemoEditActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("position", position);
                intent.putExtra("memo", memo);
                ((MemoActivity) context).startActivityForResult(intent, 2);
            }
        });
    }

    @Override
    public int getItemCount() {
        return memos.size();
    }

    public void addMemo(Memo memo) {
        memos.add(memo);
        MemoManager.saveMemos(context, memos);
        notifyDataSetChanged();
    }

    public void updateMemo(int position, Memo memo) {
        memos.set(position, memo);
        MemoManager.saveMemos(context, memos);
        notifyDataSetChanged();
    }

    public void deleteMemo(int position) {
        memos.remove(position);
        MemoManager.saveMemos(context, memos);
        notifyDataSetChanged();
    }

    public class MemoViewHolder extends RecyclerView.ViewHolder {

        private TextView titleTextView;
        private TextView contentTextView;

        public MemoViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
        }
    }
}
