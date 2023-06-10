package com.iflytek.voicedemo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.speech.setting.IatSettings;
import com.iflytek.speech.util.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class VoiceManager {
    private static String TAG = IatDemo.class.getSimpleName();
    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();

    private EditText mResultText;
    private EditText showContacts;
    private TextView languageText;
    private Toast mToast;
    private SharedPreferences mSharedPreferences;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    private String[] languageEntries;
    private String[] languageValues;
    private String language = "zh_cn";
    private int selectedNum = 0;

    private String resultType = "json";

    private StringBuffer buffer = new StringBuffer();


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.iatdemo);

        languageEntries = getResources().getStringArray(R.array.iat_language_entries);
        languageValues = getResources().getStringArray(R.array.iat_language_value);
        initLayout();
        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(IatDemo.this, mInitListener);

        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(IatDemo.this, mInitListener);

        mSharedPreferences = getSharedPreferences(IatSettings.PREFER_NAME,
                Activity.MODE_PRIVATE);
        mResultText = ((EditText) findViewById(R.id.iat_text));
        // showContacts = (EditText) findViewById(R.id.iat_contacts);
    }


    /**
     * 初始化Layout。
     */
    private void initLayout() {
        findViewById(R.id.iat_recognize).setOnClickListener(IatDemo.this);
        findViewById(R.id.image_iat_set).setOnClickListener(IatDemo.this);
        findViewById(R.id.languageText).setOnClickListener(IatDemo.this);
    }

    int ret = 0; // 函数调用返回值

    @Override
    public void onClick(View view) {
        if (null == mIat) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            this.showTip("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化");
            return;
        }

        switch (view.getId()) {
            // 进入参数设置页面
            case R.id.image_iat_set:
                Intent intents = new Intent(IatDemo.this, IatSettings.class);
                startActivity(intents);
                break;
            // 开始听写
            // 如何判断一次听写结束：OnResult isLast=true 或者 onError
            case R.id.iat_recognize:
                buffer.setLength(0);
                mResultText.setText(null);// 清空显示内容
                mIatResults.clear();
                // 设置参数
                setParam();
                boolean isShowDialog = mSharedPreferences.getBoolean(
                        getString(R.string.pref_key_iat_show), true);
                if (isShowDialog) {
                    // 显示听写对话框
                    mIatDialog.setListener(mRecognizerDialogListener);
                    mIatDialog.show();
                    showTip(getString(R.string.text_begin));
                } else {
                    // 不显示听写对话框
                    ret = mIat.startListening(mRecognizerListener);
                    if (ret != ErrorCode.SUCCESS) {
                        showTip("听写失败,错误码：" + ret + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
                    } else {
                        showTip(getString(R.string.text_begin));
                    }
                }
                break;
            case R.id.languageText:
                setLanguage(view);
                break;

            default:
                break;
        }
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            }
        }
    };

    /**
     * 在线听写支持多种小语种设置。支持语言类型如下：
     * <item>zh_cn</item> 中文
     * <item>en_us</item> 英文
     * <item>ja_jp</item> 日语
     * <item>ru-ru</item> 俄语
     * <item>es_es</item> 西班牙语
     * <item>fr_fr</item> 法语
     * <item>ko_kr</item> 韩语
     *
     * @param v
     */
    private void setLanguage(View v) {
        new AlertDialog.Builder(v.getContext()).setTitle("语种语言种类")
                .setSingleChoiceItems(languageEntries, // 单选框有几项,各是什么名字
                        0, // 默认的选项
                        new DialogInterface.OnClickListener() { // 点击单选框后的处理
                            public void onClick(DialogInterface dialog,
                                                int which) { // 点击了哪一项
                                language = languageValues[which];
                                ((TextView) findViewById(R.id.languageText)).setText("你选择的是：" + languageEntries[which]);
                                selectedNum = which;
                                dialog.dismiss();
                            }
                        }).show();
        mIat.setParameter(SpeechConstant.LANGUAGE, language);
    }



    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            Log.d(TAG, "onError " + error.getPlainDescription(true));
            showTip(error.getPlainDescription(true));

        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());
            if (isLast) {
                Log.d(TAG, "onResult 结束");
            }
            if (resultType.equals("json")) {
                printResult(results);
                return;
            }
            if (resultType.equals("plain")) {
                buffer.append(results.getResultString());
                mResultText.setText(buffer.toString());
                mResultText.setSelection(mResultText.length());
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小 = " + volume + " 返回音频数据 = " + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    // 读取通讯录
    private class PhoneDto {
        private String name;        //联系人姓名
        private String telPhone;    //电话号码


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTelPhone() {
            return telPhone;
        }

        public void setTelPhone(String telPhone) {
            this.telPhone = telPhone;
        }

        public PhoneDto() {
        }

        public PhoneDto(String name, String telPhone) {
            this.name = name;
            this.telPhone = telPhone;
        }
    }

    private class PhoneUtil {

        // 号码
        public final static String NUM = ContactsContract.CommonDataKinds.Phone.NUMBER;
        // 联系人姓名
        public final static String NAME = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;

        //上下文对象
        private Context context;
        //联系人提供者的uri
        private Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        public PhoneUtil(Context context){
            this.context = context;
        }

        //获取所有联系人
        public List<IatDemo.PhoneDto> getPhone(){
            List<IatDemo.PhoneDto> phoneDtos = new ArrayList<>();
            ContentResolver cr = context.getContentResolver();
            Cursor cursor = cr.query(phoneUri,new String[]{NUM,NAME},null,null,null);
            while (cursor.moveToNext()){
                IatDemo.PhoneDto phoneDto = new IatDemo.PhoneDto(cursor.getString(cursor.getColumnIndex(NAME)),cursor.getString(cursor.getColumnIndex(NUM)));
                phoneDtos.add(phoneDto);
            }
            return phoneDtos;
        }
    }


    private void createAlarm(String message, int hour, int minutes, int resId) {
        ArrayList<Integer> testDays = new ArrayList<>();

        // 获取当前日期
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        testDays.add(dayOfWeek);

        String packageName = getApplication().getPackageName();
        Uri ringtoneUri = Uri.parse("android.resource://" + packageName + "/" + resId);
        // action为AlarmClock.ACTION_SET_ALARM
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                //闹钟的小时
                .putExtra(AlarmClock.EXTRA_HOUR, hour)
                //闹钟的分钟
                .putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                //响铃时提示的信息
                .putExtra(AlarmClock.EXTRA_MESSAGE, message)
                //用于指定该闹铃触发时是否振动
                .putExtra(AlarmClock.EXTRA_VIBRATE, true)
                //一个 content: URI，用于指定闹铃使用的铃声，也可指定 VALUE_RINGTONE_SILENT 以不使用铃声。
                //如需使用默认铃声，则无需指定此 extra。
                .putExtra(AlarmClock.EXTRA_RINGTONE, ringtoneUri)
                //一个 ArrayList，其中包括应重复触发该闹铃的每个周日。
                // 每一天都必须使用 Calendar 类中的某个整型值（如 MONDAY）进行声明。
                //对于一次性闹铃，无需指定此 extra
                .putExtra(AlarmClock.EXTRA_DAYS, testDays)
                //如果为true，则调用startActivity()不会进入手机的闹钟设置界面
                .putExtra(AlarmClock.EXTRA_SKIP_UI, false);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }

    }
    /**
     * 显示结果
     */
    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        mResultText.setText(resultBuffer.toString());
        mResultText.setSelection(mResultText.length());

        String user_text = resultBuffer.toString();

        if (user_text.contains("打开相机")) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 1);
        } else if (user_text.contains("搜索")) {
            int locate = user_text.lastIndexOf("搜索") + 2;
            Uri uri = Uri.parse("https://www.bing.com/search?q="+user_text.substring(locate).replace("。",""));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (user_text.contains("打电话")) {
            // ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CALL_PHONE},2);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 3);
            IatDemo.PhoneUtil pu = new IatDemo.PhoneUtil(this);
            List<IatDemo.PhoneDto> list = pu.getPhone();
            String phone = "13678715605";
            for (int i = 0;i<list.size();i++){
                if(user_text.contains(list.get(i).name))
                    phone = list.get(i).telPhone;
            }
            Intent intent = new Intent(Intent.ACTION_DIAL);
            Uri data = Uri.parse("tel:" + phone);
            intent.setData(data);
            startActivityForResult(intent, 2);
        } else if (user_text.contains("闹钟")) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SET_ALARM},4);
            String hour="",min="";
            int locate = user_text.indexOf(":");
            min = user_text.substring(locate+1,locate+3);
            if(user_text.charAt(locate-2)>='0'&&user_text.charAt(locate-2)<='9')
            {
                hour = user_text.substring(locate-2,locate);
            }
            else {
                hour = user_text.substring(locate-1,locate);
            }
            createAlarm("闹钟",Integer.parseInt(hour),Integer.parseInt(min),0);
        }

    }

    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        // 返回结果
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
        }

        // 识别回调错误
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
        }

    };


    private void showTip(final String str) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT);
        mToast.show();
    }

    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);

        if (language.equals("zh_cn")) {
            String lag = mSharedPreferences.getString("iat_language_preference",
                    "mandarin");
            // 设置语言
            Log.e(TAG, "language = " + language);
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        } else {
            mIat.setParameter(SpeechConstant.LANGUAGE, language);
        }
        Log.e(TAG, "last language:" + mIat.getParameter(SpeechConstant.LANGUAGE));

        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));

        // 设置音频保存路径，保存音频格式支持pcm、wav.
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                getExternalFilesDir("msc").getAbsolutePath() + "/iat.wav");
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIat != null) {
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
    }


}
