package com.shhb.jpan.lz.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Kiven on 16/10/25.
 */
public class TaskRecordsActivity extends BaseActivity implements View.OnClickListener{

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
        webView.setBackgroundColor(getResources().getColor(R.color.appColor));
        webView.loadUrl(Constants.WEB_URL + page + Constants.HTML);
        //精致长按事件
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
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
            JSONObject resultJson = JSONObject.parseObject(result);
            callBack = resultJson.getString("callback");
            String page = resultJson.getString("cur_page");
            String size = resultJson.getString("page_size");
            JSONObject jsonObject = new JSONObject();
            Map<String, Object> map = new HashMap<>();
            jsonObject.put("user_id", PrefShared.getString(context, "userId"));
            jsonObject.put("cur_page", page);
            jsonObject.put("page_size", size);
            String parameter = BaseTools.addJson(map, jsonObject);
            OkHttpUtils okHttpUtils = new OkHttpUtils(20);
            okHttpUtils.postEnqueue(Constants.GET_TASK_REV, new Callback() {
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
                        JSONObject jsonObject = JSONObject.parseObject(json);
                        int status = jsonObject.getInteger("status");
                        if(status == 10){
                            jsonObject.remove("status");
                            result = jsonObject.toString();
                        } else {
                            jsonError.put("errcode",2);
                            jsonError.put("msg",jsonObject.getString("msg"));
                            result = jsonError.toString();
                        }
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
    }

    private void callBack(final String result){
        if(null != webView && null != callBack){
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:"+callBack+"(" + result + ");");
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.onBack:
                this.finish();
                break;
            default:
                break;
        }
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
