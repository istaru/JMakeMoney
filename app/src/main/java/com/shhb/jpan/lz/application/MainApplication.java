package com.shhb.jpan.lz.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.shhb.jpan.lz.Tools.BaseTools;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Moon on 2016/4/5.
 */
public class MainApplication extends Application {
    private static Context context;

    private static MainApplication instance;
    private static List<Activity> activityList = new LinkedList<Activity>();

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        initFacebook();
        facebookCount();
        BaseTools.createSDCardDir();
    }

    /**
     * 初始化Facebook
     */
    private void initFacebook() {
        FacebookSdk.sdkInitialize(getApplicationContext());
    }

    /**
     * 添加Facebook统计
     */
    private void facebookCount() {
        AppEventsLogger.activateApp(this);
    }

    /**
     * 单例模式中获取唯一的MyApplication实例
     *
     * @return
     */
    public static MainApplication getInstance() {
        if (null == instance) {
            instance = new MainApplication();
        }
        return instance;
    }

    /**
     * 添加Activity到容其中
     *
     * @param activity
     */
    public static void addActivity(Activity activity) {
        activityList.add(activity);
    }

    /**
     * 遍历所有Activity并finish
     */
    public static void exit() {
        for (Activity activity : activityList) {
            activity.finish();
        }
        System.exit(0);
    }

}