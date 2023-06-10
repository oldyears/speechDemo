package com.iflytek.voicedemo;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MemoManager {
    private static final String FILE_NAME = "memos.txt";

    public static void saveMemos(Context context, List<Memo> memos) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(memos);
            oos.close();
            fos.close();
        } catch (Exception e) {
            Log.e("MemoManager", "Error saving memos: " + e.getMessage());
        }
    }

    public static List<Memo> loadMemos(Context context) {
        List<Memo> memos = new ArrayList<>();
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            memos = (List<Memo>) ois.readObject();
            ois.close();
            fis.close();
        } catch (Exception e) {
            Log.e("MemoManager", "Error loading memos: " + e.getMessage());
        }
        return memos;
    }
}
