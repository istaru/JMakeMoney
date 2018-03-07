package com.shhb.jpan.lz.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.shhb.jpan.lz.R;
import com.shhb.jpan.lz.Tools.BaseTools;
import com.shhb.jpan.lz.Tools.Constants;
import com.shhb.jpan.lz.Tools.OkHttpUtils;
import com.shhb.jpan.lz.Tools.PhoneInfo;
import com.shhb.jpan.lz.Tools.PrefShared;
import com.shhb.jpan.lz.Tools.UnZipFile;
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
 * Created by Kiven on 16/10/13.
 */
public class MainActivity extends BaseActivity {

    private long mExitTime;
    private boolean isInitApp;//判断用户是否第一次安装
    private int firstInstall;//1表示第一次安装
    private String addres = "";//手机位置信息
    private int vZip = 0;
    //声明AMapLocationClient类对象
    private AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    private AMapLocationClientOption mLocationOption = null;
    private int dataType = 0;//操作首页数据的方式，0是首次操作 1是后来操作

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//Android6.0以上的系统
            int sdPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);//SD读写权限
            if(sdPermission != PackageManager.PERMISSION_GRANTED){//还没有获取到SD读写权限
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},Constants.SD_CODE);
            } else {//用户已同意SD读写权限
                initContent();
            }
        } else {//Android6.0以下的系统
            initContent();
        }
    }

    private void initContent(){
        setContentView(R.layout.activity_web);
        initBroadcastReceiver();
        initWebView();
        isInitApp();
    }

    /***
     * 如果是6.0的系统就获取权限
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.SD_CODE:
                if(permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED){//用户同意SD读写权限
                    initContent();
                } else {
                    showAlertDialog(requestCode);
                }
                break;
            case Constants.PHONE_CODE:
                if(permissions[0].equals(Manifest.permission.READ_PHONE_STATE) && grantResults[0] == PackageManager.PERMISSION_GRANTED){//用户同意读取手机信息权限
                    requestLocation();
                } else {
                    showAlertDialog(requestCode);
                }
                break;
            case Constants.LOCATION_CODE:
                initLocation();
                break;
            default:
                break;
        }
    }

    /**
     * 弹出提示框
     * @param requestCode
     */
    private void showAlertDialog(final int requestCode){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("注意");
        builder.setMessage("ご同意いただいた場合にのみ、本アプリ及び本サービスをご利用いただくことができます。");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (requestCode){
                    case Constants.SD_CODE:
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},requestCode);
                        break;
                    case Constants.PHONE_CODE:
                        requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},requestCode);
                    break;
                    default:
                        break;
                }
            }
        });
        builder.show();
    }

    /**
     * 开启高德定位
     */
    private void initAMap() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//Android6.0以上的系统
            int phonePermission = checkSelfPermission(Manifest.permission.READ_PHONE_STATE);//手机信息权限
            if(phonePermission != PackageManager.PERMISSION_GRANTED){//还没有获取到读取手机信息的权限
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},Constants.PHONE_CODE);
            } else {//已获取到读取手机信息权限
                requestLocation();
            }
        } else {//Android6.0以下的系统
            initLocation();
        }
    }

    /**
     * 当获取到读取手机信息的权限时开始获取定位权限
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void requestLocation() {
        int locationPermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);//定位权限
        if(locationPermission != PackageManager.PERMISSION_GRANTED){//还没有获取到定位权限
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},Constants.LOCATION_CODE);//定位权限
        } else {//已获取到定位权限
            initLocation();
        }
    }


    /**
     * 开始定位
     */
    private void initLocation(){
        long yqTime = PrefShared.getLong(context,"yqTime");
        String current = (System.currentTimeMillis())+"";
        current = current.substring(0,10);
        long xzTime = Long.parseLong(current);
        long s = (xzTime - yqTime) / 60;
        if(s > 10){
            mLocationClient = new AMapLocationClient(getApplicationContext());//初始化定位
            mLocationClient.setLocationListener(mLocationListener);//设置定位回调监听
            mLocationOption = new AMapLocationClientOption();//初始化AMapLocationClientOption对象
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式。
            mLocationOption.setOnceLocation(true);//获取一次定位结果：该方法默认为false。
            mLocationOption.setOnceLocationLatest(true);//获取最近3s内精度最高的一次定位结果
            mLocationClient.setLocationOption(mLocationOption);//给定位客户端对象设置定位参数
            mLocationClient.startLocation();//启动定位
        } else {
            addres = PrefShared.getString(context,"position");
            login();
        }
    }

    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    //可在其中解析amapLocation获取相应内容。
                    double latitude = aMapLocation.getLatitude();//获取纬度
                    double longitude = aMapLocation.getLongitude();//获取经度
                    float accuracy = aMapLocation.getAccuracy();//获取精度信息
                    String address = aMapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                    String country = aMapLocation.getCountry();//国家信息
                    String province = aMapLocation.getProvince();//省信息
                    String city = aMapLocation.getCity();//城市信息
                    String distric = aMapLocation.getDistrict();//城区信息
                    String street = aMapLocation.getStreet();//街道信息
                    String streetNum = aMapLocation.getStreetNum();//街道门牌号信息
                    String cityCode = aMapLocation.getCityCode();//城市编码
                    String adCode = aMapLocation.getAdCode();//地区编码
                    String aoiName = aMapLocation.getAoiName();//获取当前定位点的AOI信息
                    addres = country + province + city + distric + street + streetNum + aoiName;
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    addres = aMapLocation.getErrorCode()+"";
                }
                PrefShared.saveString(context,"position",addres);
                String current = (System.currentTimeMillis())+"";
                current = current.substring(0,10);
                long xzTime = Long.parseLong(current);
                PrefShared.saveLong(context,"yqTime",xzTime);
            }
            login();
        }
    };

    /**
     * 判断是否第一次安装APP
     */
    private void isInitApp(){
        SharedPreferences preferences = context.getSharedPreferences("isInitApp",0);//读取SharedPreferences中需要的数据
        isInitApp = preferences.getBoolean("isInitApp", true);
        if(isInitApp) {//是第一次安装
            firstInstall = 1;
        } else {//不是第一次安装
            firstInstall = 0;
        }
        SharedPreferences.Editor editor = preferences.edit();//实例化Editor对象
        editor.putBoolean("isInitApp", false);//存入数据
        editor.commit();//提交修改
        updatHtml();
    }

    /**
     * 注册刷新首页数据的广播
     */
    private void initBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter(Constants.SENDMSG_REFRESH);
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    /**
     * 执行刷新首页数据
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dataType = 1;
            initAMap();
        }
    };


    /**
     * 初始化WebView
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initWebView() {
        webViewBg = (RelativeLayout) findViewById(R.id.webView_bg);
        webViewBg.setVisibility(View.GONE);
        webFrameLayout = (FrameLayout) findViewById(R.id.webFrameLayout);
        webView = new WebView(context.getApplicationContext());
        webView.setBackgroundColor(getResources().getColor(R.color.appColor));
        File appDir = new File(Environment.getExternalStorageDirectory(), Constants.APP_FILE_URL + "/download/web");
        if (!appDir.exists()) {//如果没有找到放页面的文件夹就执行下载页面的接口
            loadHtml(Constants.UPLOAD_HTML);
        } else {
            loadHtml("index");
        }
        //禁止长按事件
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

        /**
         * 加载首页数据
         * @param result
         */
        @JavascriptInterface
        public void render(final String result){
            callBack = JSONObject.parseObject(result).getString("callback");
            initAMap();
        }

        /**
         * 提交邀请码
         * @param result
         */
        @JavascriptInterface
        public void get_inviter(String result){
            JSONObject jsonObject = JSONObject.parseObject(result);
            String inviterId = jsonObject.getString("inviter_id");
            callBack = jsonObject.getString("callback");
            subInviterId(inviterId);
        }

        /**
         * 下载页面的代码
         * @param result
         */
        @JavascriptInterface
        public void download_start(String result){
            callBack = JSONObject.parseObject(result).getString("callback");
            downloadHtml();
        }

        /**
         * 跳转方法
         * @param result
         */
        @JavascriptInterface
        public void page_to(String result){
            Intent intent = null;
            JSONObject jsonObject = JSONObject.parseObject(result);
            String link = jsonObject.getString("link");
            String title = jsonObject.getString("title");
            if(TextUtils.equals(link,"task_list")){//开始赚钱
                intent = new Intent(context,TasksActivity.class);
                intent.putExtra("page",link);
                intent.putExtra("title",title);
                startActivity(intent);
            } else if(TextUtils.equals(link,"share")) {//邀请好友
                intent = new Intent(context,ShareActivity.class);
                intent.putExtra("page",link);
                intent.putExtra("title",title);
                startActivity(intent);
            } else if(TextUtils.equals(link,"redeem")) {//钱包提现
                intent = new Intent(context,RedeemActivity.class);
                intent.putExtra("page",link);
                intent.putExtra("title",title);
                startActivity(intent);
            } else if(TextUtils.equals(link,"help")) {//帮助信息
                intent = new Intent(context,HelpActivity.class);
                intent.putExtra("page",link);
                intent.putExtra("title",title);
                startActivity(intent);
            } else if(TextUtils.equals(link,"personal")) {//个人中心
                intent = new Intent(context,PersonalActivity.class);
                intent.putExtra("page",link);
                intent.putExtra("title",title);
                startActivity(intent);
            } else if(TextUtils.equals(link,"wallet")) {//我的钱包
                intent = new Intent(context,WalletActivity.class);
                intent.putExtra("page",link);
                intent.putExtra("title",title);
                startActivity(intent);
            } else {
                if(!TextUtils.equals(link,"")){
                    intent = new Intent(context,BannerActivity.class);//banner的活动页
                    intent.putExtra("page",link);
                    intent.putExtra("title",title);
                    startActivity(intent);
                }
            }
        }
    }

    /**
     * 用户登录接口
     */
    private void login() {
        final JSONObject dataJson = new JSONObject();
        PhoneInfo phoneInfo = new PhoneInfo(context);
        Map<String,Object> map = phoneInfo.getPhoneMsg();
        map.put("address",addres);
        for(Map.Entry<String, Object> m : map.entrySet()){
            dataJson.put(m.getKey(),m.getValue());
        }
        final Map<String,Object> typMap = new HashMap<>();
        typMap.put("login_type","0");
        String parameter = BaseTools.addJson(typMap,dataJson);
        final OkHttpUtils okHttpUtils = new OkHttpUtils(10);
        okHttpUtils.postEnqueue(Constants.LOGIN_PATH, new Callback() {
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
                    JSONObject jsonResult = JSONObject.parseObject(json);
                    int status = jsonResult.getInteger("status");
                    if(status == 10){
                        jsonResult = jsonResult.getJSONObject("data");
                        String userId = jsonResult.getString("uid");
                        String fBId = jsonResult.getString("fbid");
                        String sfuid = jsonResult.getString("sfuid");
                        PrefShared.saveString(context,"userId",userId);
                        PrefShared.saveString(context,"fBId",fBId);
                        PrefShared.saveString(context,"sfuid",sfuid);
                        sendMainData();
                    } else {
                        jsonError.put("errcode",2);
                        jsonError.put("msg",jsonResult.getString("msg"));
                        callBack(result);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                    if(e.toString().contains("SocketTimeoutException")){
                        jsonError.put("errcode",0);
                    } else {
                        jsonError.put("errcode", 2);
                    }
                    result = jsonError.toString();
                    callBack(result);
                }
            }
        }, parameter);
    }

    /**
     * 获取首页数据并发给H5页面
     */
    private void sendMainData() {
        JSONObject jsonObject = new JSONObject();
        Map<String,Object> map = new HashMap<>();
        jsonObject.put("user_id", PrefShared.getString(context,"userId"));
        String parameter = BaseTools.addJson(map,jsonObject);
        OkHttpUtils okHttpUtils = new OkHttpUtils(20);
        okHttpUtils.postEnqueue(Constants.GET_INDEX_INFO, new Callback() {
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
                        int is_inviter;
                        String sfUId = PrefShared.getString(context,"sfuid");
                        if(null != sfUId && !TextUtils.equals(sfUId,"null") && !TextUtils.equals(sfUId,"")){
                            is_inviter = 1;
                        } else {
                            is_inviter = 0;
                        }
                        jsonObject = jsonObject.getJSONObject("data");
                        jsonObject.put("sfuid",is_inviter);
                        jsonObject.put("first_install",firstInstall);
                        result = jsonObject.toString();
                        jsonObject.remove("account");
                        jsonObject.remove("first_install");
                        jsonObject.remove("banner");
                        jsonObject.remove("version");
                        PrefShared.saveString(context,"persionalMsg",jsonObject.toString());
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
                if(dataType == 0){//首次进入的时候
                    callBack(result);
                } else {//更新数据的时候
                    if(null != webView && null != callBack){
                        final String finalResult = result;
                        webView.post(new Runnable() {
                            @Override
                            public void run() {
                                webView.loadUrl("javascript:base.data_refresh(" + finalResult + ");");
                            }
                        });
                    }
                }
            }
        },parameter);
    }

    /**
     * 是否需要更新页面的接口
     */
    private void updatHtml() {
        OkHttpUtils okHttpUtils = new OkHttpUtils(20);
        long time = System.currentTimeMillis();
        okHttpUtils.getEnqueue(Constants.UPDATE_HTML+"?t="+time, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String json = response.body().string();
                    JSONObject jsonObject = JSONObject.parseObject(json);
                    vZip = jsonObject.getInteger("version");
                    int uVZip = PrefShared.getInt(context,"version");
                    PrefShared.saveInt(context,"version",vZip);
                    if(vZip != uVZip && uVZip != 0){
                        loadHtml(Constants.UPLOAD_HTML);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 下载HTML页面
     */
    private void downloadHtml() {
        OkHttpUtils okHttpUtils = new OkHttpUtils(120);
        okHttpUtils.downloadFile(Constants.DOWNLOAD_HTML, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String progres = "{" + "\"num\""+":"+-1+"}";
                callBack(progres);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream inputStream = null;
                byte[] buf = new byte[2048];
                int length = 0;
                FileOutputStream fileOutputStream = null;
                try {
                    inputStream = response.body().byteStream();
                    long total = response.body().contentLength();
                    File dir = new File(Environment.getExternalStorageDirectory(), Constants.APP_FILE_URL + "/download");
                    if (!dir.exists()) dir.mkdirs();
                    File file = new File(dir, "web.zip");
                    fileOutputStream = new FileOutputStream(file);
                    long sum = 0;
                    while ((length = inputStream.read(buf)) != -1) {
                        sum += length;
                        fileOutputStream.write(buf, 0, length);
                        int progress = (int) (sum * 1.0f / total * 100);
                        String progressStr = "{" + "\"num\""+":"+progress+"}";
                        callBack(progressStr);
                    }
                    fileOutputStream.flush();
                    boolean flag = UnZipFile.unZipFiles(Environment.getExternalStorageDirectory() + "/" + Constants.APP_FILE_URL + "/download/web.zip","");//解压zip包
                    if(flag == true){
                        loadHtml("index");
                        PrefShared.saveInt(context,"version",vZip);
                    } else {
                        String progres = "{" + "\"num\""+":"+-1+"}";
                        callBack(progres);
                    }
                    file.delete();//删除压缩包
                } catch (Exception e) {
                    e.printStackTrace();
                    String progres = "{" + "\"num\""+":"+-1+"}";
                    callBack(progres);
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
        });
    }

    /**
     * 加载页面的方法
     * @param htmlType
     */
    private void loadHtml(final String htmlType){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(TextUtils.equals("index",htmlType)){//加载首页
                    webView.loadUrl(Constants.WEB_URL + htmlType + Constants.HTML);
                    StatusBarUtil.setColor(MainActivity.this, getResources().getColor(R.color.mainColor), 0);
                } else {//加载loading页
                    webView.loadUrl(htmlType);
                    StatusBarUtil.setColor(MainActivity.this, getResources().getColor(R.color.appColor), 0);
                }
            }
        });
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
                String json = BaseTools.decryptJson(response.body().string());
                String result = "";
                JSONObject jsonError = new JSONObject();
                try {
                    JSONObject jsonObject = JSONObject.parseObject(json);
                    int status = jsonObject.getInteger("status");
                    if(status == 10){
                        jsonError.put("status",1);
                        dataType = 1;
                        initAMap();
                    } else {
                        jsonError.put("errcode",2);
                        jsonError.put("msg",jsonObject.getString("msg"));
                    }
                    result = jsonError.toString();
                } catch (Exception e){
                    jsonError.put("errcode",2);
                    result = jsonError.toString();
                    e.printStackTrace();
                }
                callBack(result);
            }
        },parameter);
    }

    /**
     * 首页下载进度、数据回调方法、填写师傅的ID回调方法
     * @param result
     */
    private void callBack(final String result){
        if(null != webView && null != callBack){
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:" + callBack + "(" + result + ");");
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "もう一度タップしてアプリを終了する", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                MainApplication.exit();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        if(null != mLocationClient){
            mLocationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁
        }
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);//销毁刷新首页数据的广播
        if (null != mLocationClient) {//销毁定位客户端，同时销毁本地定位服务。
            mLocationClient.onDestroy();
            mLocationClient = null;
            mLocationClient = null;
        }
        if(null != webView){//销毁WebView
            webFrameLayout.removeAllViews();
            webView.onPause();
            webView.destroy();
            webView = null;
        }
        System.gc();
        super.onDestroy();
    }
}
