package com.shhb.jpan.lz.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.shhb.jpan.lz.R;
import com.shhb.jpan.lz.Tools.BaseTools;
import com.shhb.jpan.lz.Tools.Constants;
import com.shhb.jpan.lz.Tools.OkHttpUtils;
import com.shhb.jpan.lz.Tools.PrefShared;
import com.shhb.jpan.lz.application.MainApplication;
import com.shhb.jpan.lz.view.StatusBarUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Kiven on 16/10/17.
 */
public class PersonalActivity extends BaseActivity implements View.OnClickListener{
    private JSONObject msgJson = new JSONObject();
    private String apkUrl = "";
    private boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        page = getIntent().getStringExtra("page");
        title = getIntent().getStringExtra("title");
        initView();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void initView() {
        webViewBg = (RelativeLayout) findViewById(R.id.webView_bg);
        webViewTitle = (TextView) findViewById(R.id.webView_title);
        webViewTitle.setText(title);
        webViewBg.setBackgroundColor(getResources().getColor(R.color.appTColor));
        onBack = (LinearLayout) findViewById(R.id.onBack);
        onBack.setOnClickListener(this);

        webFrameLayout = (FrameLayout) findViewById(R.id.webFrameLayout);
        webView = new WebView(context.getApplicationContext());
        webFrameLayout = (FrameLayout) findViewById(R.id.webFrameLayout);
        webView = new WebView(context.getApplicationContext());
        webView.setBackgroundColor(getResources().getColor(R.color.appColor));
        webView.loadUrl(Constants.WEB_URL + page + Constants.HTML);
        //精致长按事件
//        webView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                return true;
//            }
//        });
        webView.getSettings().setJavaScriptEnabled(true);//支持JavaScript
        webView.getSettings().setAllowFileAccess(true);//允许访问文件
        webView.getSettings().setAllowFileAccessFromFileURLs(true);//通过此API可以设置是否允许通过file url加载的Javascript读取其他的本地文件
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);//通过此API可以设置是否允许通过file url加载的Javascript可以访问其他的源，包括其他的文件和http,https等其他的源
        //图片显示
        webView.getSettings().setLoadsImagesAutomatically(true);
        //自适应屏幕
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new JsObject(), "native_android");
        webView.setWebViewClient(new WebViewClient());
        //关闭缩放
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setDisplayZoomControls(false);
        webFrameLayout.addView(webView,0);
    }

    class JsObject{
        @JavascriptInterface
        public void render(final String result) {
            callBack = JSONObject.parseObject(result).getString("callback");
            findByUpdate();
        }

        @JavascriptInterface
        public void get_inviter(String result){
            JSONObject jsonObject = JSONObject.parseObject(result);
            String inviterId = jsonObject.getString("inviter_id");
            callBack = jsonObject.getString("callback");
            subInviterId(inviterId);
        }

        @JavascriptInterface
        public void check_version(String result){
            JSONObject jsonObject = JSONObject.parseObject(result);
            callBack = jsonObject.getString("callback");
            callBack(msgJson.toString());
        }

        @JavascriptInterface
        public void version_refresh(String result){
            if(!TextUtils.equals("",apkUrl)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(null != webView){
                            webViewBg.setVisibility(View.GONE);
                            webView.loadUrl(Constants.UPLOAD_HTML);
                            StatusBarUtil.setColor(PersonalActivity.this, getResources().getColor(R.color.appColor), 0);
                        }
                    }
                });
            }
        }

        @JavascriptInterface
        public void download_start(String result){
            callBack = JSONObject.parseObject(result).getString("callback");
            downloadApp();
        }

        @JavascriptInterface
        public void page_to(String result){
            Intent intent = null;
            JSONObject jsonObject = JSONObject.parseObject(result);
            String link = jsonObject.getString("link");
            String title = jsonObject.getString("title");
            if(TextUtils.equals(link,"login")){//绑定账号
                intent = new Intent(context,BindingActivity.class);
                intent.putExtra("page",link);
                intent.putExtra("title",title);
                startActivity(intent);
            } else if(TextUtils.equals(link,"share")) {//邀请好友
                intent = new Intent(context,ShareActivity.class);
                intent.putExtra("page",link);
                intent.putExtra("title",title);
                startActivity(intent);
            } else if(TextUtils.equals(link,"task_records")) {//任务记录
                intent = new Intent(context,TaskRecordsActivity.class);
                intent.putExtra("page",link);
                intent.putExtra("title",title);
                startActivity(intent);
            } else if(TextUtils.equals(link,"task_friend")) {//好友提成
                intent = new Intent(context,TaskFriendActivity.class);
                intent.putExtra("page",link);
                intent.putExtra("title",title);
                startActivity(intent);
            } else if(TextUtils.equals(link,"redeem_records")) {//兑换记录
                intent = new Intent(context,RedeemRecordsActivity.class);
                intent.putExtra("page",link);
                intent.putExtra("title",title);
                startActivity(intent);
            }
        }
    }

    /**
     * 获取最新的APP
     */
    private void findByUpdate(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("version_id","abc");
        Map<String, Object> map = new HashMap<>();
        String parameter = BaseTools.addJson(map, jsonObject);
        OkHttpUtils okHttpUtils = new OkHttpUtils(20);
        okHttpUtils.postEnqueue(Constants.GET_UPDATE_MSG, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                JSONObject jsonObject = new JSONObject();
                if (!isNetworkInfo) {
                    jsonObject.put("errcode", 0);
                } else {
                    jsonObject.put("errcode", 1);
                }
                String result = jsonObject.toString();
                callBack(result);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = "";
                JSONObject jsonError = new JSONObject();
                try {
                    String json = BaseTools.decryptJson(response.body().string());
                    final JSONObject jsonObject = JSONObject.parseObject(json);
                    int status = jsonObject.getInteger("status");
                    if (status == 10) {
                        int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
                        int number = jsonObject.getInteger("number");
                        result = PrefShared.getString(context,"persionalMsg");
                        if(number > versionCode){//要更新app
                            String version = jsonObject.getString("version");
                            String changelogs = jsonObject.getString("changelogs");
                            apkUrl = jsonObject.getString("apk");
                            result = result.substring(0,result.length()-1)+","+"\"need_refresh\""+":"+1+"}";
                            msgJson.put("status",1);
                            msgJson.put("version",version);
                            msgJson.put("changelogs",changelogs);
                        } else {//不用更新APP
                            result = result.substring(0,result.length()-1)+","+"\"need_refresh\""+":"+0+"}";
                        }
                    } else {
                        jsonError.put("errcode", 2);
                        jsonError.put("msg", jsonObject.getString("msg"));
                        result = jsonError.toString();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if(e.toString().contains("SocketTimeoutException")){
                        jsonError.put("errcode",0);
                    } else {
                        jsonError.put("errcode", 2);
                    }
                    result = jsonError.toString();
                }
                callBack(result);
            }
        }, parameter);
    }

    /**
     * 下载APP
     */
    private void downloadApp() {
        OkHttpUtils okHttpUtils = new OkHttpUtils(120);
        okHttpUtils.downloadFile(Constants.DOWNLOAD_APP, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String progressStr = "{" + "\"num\""+":"+-1+"}";
                callBack(progressStr);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                File file = saveFile(response);
                if(null != file){
                    installAPK(file);
                }
            }
        });
    }

    private File saveFile(Response response) throws IOException {
        InputStream inputStream = null;
        byte[] buf = new byte[2048];
        int length = 0;
        FileOutputStream fileOutputStream = null;
        try {
            inputStream = response.body().byteStream();
            final long total = response.body().contentLength();
            File file = new File(BaseTools.makeFile(), getResources().getString(R.string.app_name)+".apk");
            fileOutputStream = new FileOutputStream(file);
            long sum = 0;
            while ((length = inputStream.read(buf)) != -1) {
                sum += length;
                fileOutputStream.write(buf, 0, length);
                float progress = (float) (sum * 1.0f / total * 100);
                String progressStr = "{" + "\"num\""+":"+progress+"}";
                callBack(progressStr);
            }
            fileOutputStream.flush();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            String progressStr = "{" + "\"num\""+":"+-1+"}";
            callBack(progressStr);
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
            }
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * 安装apk文件
     * @param apk
     */
    public void installAPK(File apk) {
        // 通过Intent安装APK文件
        Intent intents = new Intent();
        intents.setAction("android.intent.action.VIEW");
        intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intents.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
        // 如果不加上这句的话在apk安装完成之后点击单开会崩溃
        startActivity(intents);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(null != webView){
                    webViewBg.setVisibility(View.VISIBLE);
                    webView.loadUrl(Constants.WEB_URL + page + Constants.HTML);
                    StatusBarUtil.setColor(PersonalActivity.this, getResources().getColor(R.color.appTColor), 0);
                }
            }
        });
    }

    /**
     * 个人信息数据回调、填写师傅的ID回调方法
     * @param result
     */
    private void callBack(final String result){
        if(null != webView && null != callBack && flag == false){
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:" + callBack + "(" + result + ");");
                }
            });
        }
    }

    /**
     *绑定师父ID
     * @param inviterId
     */
    private void subInviterId(String inviterId) {
        JSONObject jsonObject = new JSONObject();
        Map<String,Object> map = new HashMap<>();
        jsonObject.put("user_id", PrefShared.getString(context,"userId"));
        jsonObject.put("invite_id", inviterId);
        String parameter = BaseTools.addJson(map,jsonObject);
        OkHttpUtils okHttpUtils = new OkHttpUtils(20);
        okHttpUtils.postEnqueue(Constants.INVITE_RULE, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                JSONObject jsonObject = new JSONObject();
                if(!isNetworkInfo){
                    jsonObject.put("errcode",0);
                } else {
                    jsonObject.put("errcode",1);
                }
                String result = jsonObject.toString();
                callBack(result);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = "";
                JSONObject jsonError = new JSONObject();
                try {
                    String json = BaseTools.decryptJson(response.body().string());
                    JSONObject jsonObject = JSONObject.parseObject(json);
                    int status = jsonObject.getInteger("status");
                    if(status == 10){
                        jsonError.put("status",1);
                        context.sendBroadcast(new Intent(Constants.SENDMSG_REFRESH));
                    } else {
                        jsonError.put("errcode",2);
                        jsonError.put("msg",jsonObject.getString("msg"));
                    }
                    result = jsonError.toString();
                } catch (Exception e){
                    e.printStackTrace();
                    if(e.toString().contains("SocketTimeoutException")){
                        jsonError.put("errcode",0);
                    } else {
                        jsonError.put("errcode", 2);
                    }
                    result = jsonError.toString();
                }
                callBack(result);
            }
        },parameter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.onBack:
                flag = true;
                this.finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        flag = true;
    }

    @Override
    protected void onDestroy() {
        gcWebView();
        super.onDestroy();
    }

    private void gcWebView(){
        if(null != webView) {//销毁WebView
            webFrameLayout.removeView(webView);
            webView.onPause();
            webView.destroy();
            webView = null;
        }
        System.gc();
    }
}
