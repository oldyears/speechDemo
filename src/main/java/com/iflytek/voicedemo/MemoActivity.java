// MemoActivity.java
package com.iflytek.voicedemo;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class MemoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MemoAdapter adapter;
    private Button addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo_layout);
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE},10);

        recyclerView = findViewById(R.id.recyclerView);
        adapter = new MemoAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MemoActivity.this, MemoEditActivity.class);
                intent.putExtra("mode", "add");
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {

            String title = data.getStringExtra("title");
            String content = data.getStringExtra("content");
            Memo memo = new Memo(title, content);
            adapter.addMemo(memo);

        }
        else if (requestCode == 2 && resultCode == RESULT_OK) {
            int position = data.getIntExtra("position", -1);
            if(data.getStringExtra("operation")=="save") {
                String title = data.getStringExtra("title");
                String content = data.getStringExtra("content");
                Memo memo = new Memo(title, content);
                adapter.updateMemo(position, memo);
            }
            else
                adapter.deleteMemo(position);
        }

    }
}
