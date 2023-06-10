package com.iflytek.voicedemo;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.text.HtmlCompat;

import com.iflytek.speech.setting.IatSettings;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private SharedPreferences mSharedPreferences;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        initView();
        mSharedPreferences = getSharedPreferences(IatSettings.PREFER_NAME, Activity.MODE_PRIVATE);
        boolean privacyConfirm = mSharedPreferences.getBoolean(SpeechApp.PRIVACY_KEY, false);
        if (!privacyConfirm) {
            showPrivacyDialog();
        }
    }

    private Intent intent;

    private void isPrivacyConfirm(Intent i) {
        boolean privacyConfirm = mSharedPreferences.getBoolean(SpeechApp.PRIVACY_KEY, false);
        if (privacyConfirm) {
            intent = i;
            mPermissionResult.launch(Manifest.permission.RECORD_AUDIO);
        }
        // mPermissionResult.launch(Manifest.permission.CAMERA);
    }

    private void initView() {
        TextView tipTv = (TextView) findViewById(R.id.tip);
        String buf = "欢迎使用";
        tipTv.setText(buf);
        // 语音助手
        findViewById(R.id.iatBtn).setOnClickListener(v -> {
            isPrivacyConfirm(new Intent(MainActivity.this, IatDemo.class));
        });
        //语音备忘录
        findViewById(R.id.memoBtn).setOnClickListener(v -> {
            Intent tmp = new Intent(MainActivity.this, MemoActivity.class);

            isPrivacyConfirm(tmp);
        });

    }

    private Toast mToast;

    private void showTip(final String str) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT);
        mToast.show();
    }

    private void showPrivacyDialog() {
        AppCompatTextView textView = new AppCompatTextView(this);
        textView.setPadding(100, 50, 100, 50);
        textView.setText(
                HtmlCompat.fromHtml("我们非常重视对您个人信息的保护，承诺严格按照讯飞开放平台<font color='#3B5FF5'>《隐私政策》</font>保护及处理您的信息，是否确定同意？",
                        HtmlCompat.FROM_HTML_MODE_LEGACY));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://www.xfyun.cn/doc/policy/sdk_privacy.html"));
                startActivity(intent);
            }
        });
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("温馨提示")
                .setView(textView)
                .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSharedPreferences.edit().putBoolean(SpeechApp.PRIVACY_KEY, true).apply();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("不同意", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSharedPreferences.edit().putBoolean(SpeechApp.PRIVACY_KEY, false).apply();
                        finish();
                        System.exit(0);
                    }
                })
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private ActivityResultLauncher<String> mPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    if (intent !=null){
                        MainActivity.this.startActivity(intent);
                    }
                    SpeechApp.initializeMsc(MainActivity.this);
                }
            });

}
