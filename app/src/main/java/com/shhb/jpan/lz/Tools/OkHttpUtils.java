package com.shhb.jpan.lz.Tools;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Kiven on 16/10/19.
 */
public class OkHttpUtils {
//    private static final byte[] LOCKER = new byte[0];
//    private static OkHttpUtils mInstance;
    private static OkHttpClient mOkHttpClient;
    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");

    public OkHttpUtils(int time) {
        okhttp3.OkHttpClient.Builder ClientBuilder = new okhttp3.OkHttpClient.Builder();
        ClientBuilder.readTimeout(time, TimeUnit.SECONDS);//读取超时
        ClientBuilder.connectTimeout(time, TimeUnit.SECONDS);//连接超时
        ClientBuilder.writeTimeout(time, TimeUnit.SECONDS);//写入超时
        mOkHttpClient = ClientBuilder.build();
    }

//    public static OkHttpUtils getInstance() {
//        if (mInstance == null) {
//            synchronized (LOCKER) {
//                if (mInstance == null) {
//                    mInstance = new OkHttpUtils();
//                }
//            }
//        }
//        return mInstance;
//    }

    /**
     * 设置请求头
     * @param headersParams
     * @return
     */
    public Headers setHeaders(Map<String, String> headersParams){
        Headers headers = null;
        okhttp3.Headers.Builder headersbuilder=new okhttp3.Headers.Builder();
        if(headersParams != null) {
            Iterator<String> iterator = headersParams.keySet().iterator();
            String key = "";
            while (iterator.hasNext()) {
                key = iterator.next().toString();
                headersbuilder.add(key, headersParams.get(key));
                Log.e("get http", "get_headers==="+key+"===="+headersParams.get(key));
            }
        }
        headers = headersbuilder.build();
        return headers;
    }

    /**
     * 同步的get方法
     * @param url 请求地址
     * @return
     * @throws Exception
     */
    public static String getExecute(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
       return execute(request);
    }

    /**
     * 同步的get方法
     * @param url 请求地址
     * @return
     * @throws Exception
     */
    public String getExecute(String url,Map<String,Object> map) {
        url = getRequestUrl(url,map);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        return execute(request);
    }

    /**
     * 同步post
     * @param url 请求地址
     * @param requestBody 请求参数
     * @return
     * @throws IOException
     */
    public String postExecute(String url, RequestBody requestBody) {
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        return execute(request);
    }


    /**
     * 异步get请求
     * @param url
     * @param responseCallback
     * @return
     * @throws Exception
     */
    public void getEnqueue(String url, Callback responseCallback) {
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        enqueue(request,responseCallback);
    }

    /**
     * 异步post请求
     * @param url
     * @param responseCallback
     * @param parameter
     */
    public void postEnqueue(String url, Callback responseCallback, String parameter) {
        //把请求的内容字符串转换为json
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_MARKDOWN, parameter);
        Request request = new Request.Builder()
                .post(requestBody)
                .url(url)
                .addHeader("Connection", "close")
                .build();
        enqueue(request,responseCallback);
    }

    /**
     * 下载文件
     * @param url
     * @param responseCallback
     */
    public void downloadFile(String url,Callback responseCallback){
        Request request = new Request.Builder()
                .url(url)
                .tag(this)
                .addHeader("connection", "close")//解决下载时意外关闭的错误，但好像没有效果
                .build();
        enqueue(request,responseCallback);
    }

    /**
     * 同步请求
     * @param request
     * @return
     * @throws IOException
     */
    public static String execute(Request request) {
        try {
            Response response = mOkHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                return "Unexpected code:" + response;
            }
        } catch (Exception e){
            e.printStackTrace();
            return e.toString();
        }
    }

    /**
     * 异步请求
     * @param request
     * @return
     * @throws IOException
     */
    public static void enqueue(Request request,Callback responseCallback) {
        mOkHttpClient.newCall(request).enqueue(responseCallback);
    }

    /**
     * get方式URL拼接
     *
     * @param url
     * @param map
     * @return
     */
    private static String getRequestUrl(String url, Map<String, Object> map) {
        if (map == null || map.size() == 0) {
            return url;
        } else {
            StringBuilder newUrl = new StringBuilder(url);
            if (url.indexOf("?") == -1) {
                newUrl.append("?rd=" + Math.random());
            }
            for (Map.Entry<String, Object> item : map.entrySet()) {
                if (false == TextUtils.isEmpty(item.getKey().trim())) {
                    try {
                        newUrl.append("&" + item.getKey().trim() + "=" + URLEncoder.encode(String.valueOf(item.getValue().toString().trim()), "UTF-8"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return newUrl.toString();
        }
    }

}
