package com.shhb.jpan.lz.activity;

import android.annotation.TargetApi;
import android.content.Intent;
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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;

import com.shhb.jpan.lz.R;
import com.shhb.jpan.lz.Tools.BaseTools;
import com.shhb.jpan.lz.Tools.Constants;
import com.shhb.jpan.lz.Tools.FaceBookUtils;
import com.shhb.jpan.lz.Tools.OkHttpUtils;
import com.shhb.jpan.lz.Tools.PrefShared;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Kiven on 16/10/20.
 */
public class BindingActivity extends BaseActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        title = getIntent().getStringExtra("title");
        page = getIntent().getStringExtra("page");
        callbackManager = CallbackManager.Factory.create();
        initView();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @JavascriptInterface
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
        webView.addJavascriptInterface(this, "native_android");
        webView.setWebViewClient(new WebViewClient());
        //关闭缩放
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setDisplayZoomControls(false);
        webFrameLayout.addView(webView,0);
    }

    @JavascriptInterface
    public void login_facebook(String result){
        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(result);
        String faceBookCallBack = jsonObject.getString("callback");
        callBack = faceBookCallBack;
        FaceBookUtils faceBookUtils = new FaceBookUtils(this,callbackManager);
        faceBookUtils.login(facebookLoginCallback);
//        new FaceBookUtils(this,callbackManager,facebookLoginCallback,"login");
//        List<String> permissions = Arrays.asList("public_profile", "user_friends", "user_status");
//        LoginManager.getInstance().logInWithReadPermissions(this, permissions);
//        LoginManager.getInstance().registerCallback(callbackManager, facebookLoginCallback);
    }

    /**
     * facebook登录状态回调
     */
    FacebookCallback<LoginResult> facebookLoginCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            getUserMsg(loginResult.getAccessToken());
        }

        @Override
        public void onCancel() {
            removeLoading(webView);
            com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
            jsonObject.put("status",2);
            callBaceWeb(jsonObject.toString());
        }

        @Override
        public void onError(FacebookException error) {
            removeLoading(webView);
            com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
            jsonObject.put("status",0);
            callBaceWeb(jsonObject.toString());
        }
    };

    /**
     * 获取Facebook登录后的用户信息
     * @param accessToken
     */
    private void getUserMsg(AccessToken accessToken) {
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                if(null == response){
                    removeLoading(webView);
                    com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
                    jsonObject.put("status",0);
                    callBaceWeb(jsonObject.toString());
                } else {
                    //获取登录成功之后的用户详细信息
                    Map<String,Object> map = new HashMap<String, Object>();
                    String facebookId = object.optString("id");
                    String facebookName = object.optString("name");
                    String iconUrl = "http://graph.facebook.com/"+facebookId+"/picture?type=small";
                    map.put("fbid",facebookId);
                    map.put("fbname",facebookName);
                    map.put("fbimg",iconUrl);
                    map.put("imei",PrefShared.getString(context,"imei"));
                    map.put("user_id", PrefShared.getString(context,"userId"));
                    map.put("deviceVer","");
                    map.put("uuid","");
                    map.put("bdid","");
                    map.put("idfa","");
                    facebookLogin(map);
                }
            }
        });
        request.executeAsync();
    }

    /**
     * 绑定Facebook与服务交互
     * @param map
     */
    private void facebookLogin(Map<String,Object> map) {
        showLoading(webView);
        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        for(Map.Entry<String,Object> m : map.entrySet()){
            jsonObject.put(m.getKey(), m.getValue());
        }
        String parameter = BaseTools.addJson(null,jsonObject);
        OkHttpUtils okHttpUtils = new OkHttpUtils(20);
        okHttpUtils.postEnqueue(Constants.BIND_FACEBOOK, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                removeLoading(webView);
                com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
                if(!isNetworkInfo){
                    jsonObject.put("errcode",0);
                } else {
                    jsonObject.put("errcode",1);
                }
                String result = jsonObject.toString();
                callBaceWeb(result);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                removeLoading(webView);
                String result = "";
                com.alibaba.fastjson.JSONObject jsonError = new com.alibaba.fastjson.JSONObject();
                try {
                    String json = BaseTools.decryptJson(response.body().string());
                    com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(json);
                    int status = jsonObject.getInteger("status");
                    try {
                        if(status == 10 || status == 9){
                            String userId = jsonObject.getJSONObject("data").getString("uid");
                            String fBId = jsonObject.getJSONObject("data").getString("fbid");
                            String sfuid = jsonObject.getJSONObject("data").getString("sfuid");
                            PrefShared.saveString(context,"userId",userId);
                            PrefShared.saveString(context,"fBId",fBId);
                            PrefShared.saveString(context,"sfuid",sfuid);
                            context.sendBroadcast(new Intent(Constants.SENDMSG_REFRESH));
                        }
                    } catch (Exception e){

                    }
                    if(status == 10){
                        jsonObject.put("status",1);
                        result = jsonObject.toString();
                    } else if(status == 9){
                        jsonError.put("status",9);
                        jsonError.put("msg",jsonObject.getString("msg"));
                        result = jsonError.toString();
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
                callBaceWeb(result);
            }
        },parameter);
    }

    /**
     * Facebook分享结果给页面
     * @param type
     */
    private void callBaceWeb(final String type) {
        if(null != webView && null != callBack){
            webView.post(new Runnable() {
                @Override
                public void run() {
                    String url = "javascript:"+callBack+"("+type+");";
                    webView.loadUrl(url);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(callbackManager != null){ //facebook回调（登录、分享）
            callbackManager.onActivityResult(requestCode, resultCode, data);
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


