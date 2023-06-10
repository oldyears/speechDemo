package com.iflytek.voicedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MemoEditActivity extends AppCompatActivity {

    private EditText titleEditText;
    private EditText contentEditText;
    private Button saveButton;
    private Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);

        titleEditText = findViewById(R.id.titleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);

        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");
        if (mode.equals("edit")) {
            int position = intent.getIntExtra("position", -1);
            Memo memo = (Memo) intent.getSerializableExtra("memo");
            titleEditText.setText(memo.getTitle());
            contentEditText.setText(memo.getContent());
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title = titleEditText.getText().toString();
                    String content = contentEditText.getText().toString();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("position", position);
                    resultIntent.putExtra("title", title);
                    resultIntent.putExtra("content", content);
                    resultIntent.putExtra("operation", "save");
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }

            });
            deleteButton.setText("删除备忘录");
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title = titleEditText.getText().toString();
                    String content = contentEditText.getText().toString();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("position", position);
                    resultIntent.putExtra("title", title);
                    resultIntent.putExtra("content", content);
                    resultIntent.putExtra("operation", "delete");
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            });
        } else if (mode.equals("add")) {
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title = titleEditText.getText().toString();
                    String content = contentEditText.getText().toString();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("title", title);
                    resultIntent.putExtra("content", content);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            });
            deleteButton.setText("取消备忘录");
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
        }


    }
}
