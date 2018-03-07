package com.shhb.jpan.lz.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.shhb.jpan.lz.R;
import com.shhb.jpan.lz.application.MainApplication;
import com.shhb.jpan.lz.view.StatusBarUtil;

/**
 * Created by Moon on 2016/4/5.
 */
public class BaseActivity extends AppCompatActivity{
    public static Context context;
    public boolean processFlag = true; //默认可以点击
    public static boolean isNetworkInfo;//网络连接的状态
    public static String networkType;//网络连接的状态

    public CallbackManager callbackManager;
    public RelativeLayout webViewBg;
    public TextView webViewTitle;
    public LinearLayout onBack;
    public FrameLayout webFrameLayout;
    public WebView webView;
    public String callBack;
    public String page,title;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setStatusBar();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        setStatusBar();
    }

    /**
     * 设置沉浸式状态栏
     */
    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.appTColor),0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        MainApplication.getInstance().addActivity(this);
    }

    /**
     * 设置按钮在短时间内被重复点击的有效标识（true表示点击有效，false表示点击无效）
     */
    protected synchronized void setProcessFlag() {
        processFlag = false;
    }

    /**
     * 计时线程（防止在一定时间段内重复点击按钮）
     */
    protected class TimeThread extends Thread {
        public void run() {
            try {
                sleep(1000);
                processFlag = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    /**
     * 开始转菊花
     * @param webView
     */
    protected void showLoading(final WebView webView) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                String url = "javascript:base.addLoading();";
                webView.loadUrl(url);
            }
        });
    }

    /**
     * 关闭菊花
     * @param webView
     */
    protected void removeLoading(final WebView webView) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                String url = "javascript:base.removeLoading();";
                webView.loadUrl(url);
            }
        });
    }

}
