package com.shhb.jpan.lz.Tools;

import android.os.Environment;

/**
 * Created by Kiven on 16/10/14.
 */
public class Constants {

    /**
     * SD卡权限返回的code
     */
    public static final int SD_CODE = 101;
    /**
     * 手机权限返回的code
     */
    public static final int PHONE_CODE = 102;
    /**
     * 定位权限返回的code
     */
    public static final int LOCATION_CODE = 103;

    /**
     * 服务器地址
     */
    public static final String REQUEST = "http://item.mssoft.info/jp_item/";//正式服务器
    public static final String REQUEST_DOWNLOAD = "http://item.mssoft.info/resource/download/";//下载的头部
//    public static final String REQUEST = "http://es2.laizhuan.com/jp_item/";//测试服务器
//    public static final String REQUEST = "http://192.168.1.210/jp_item/";//本地服务器

    public static final String HTML = ".html";

    public static final String APP_FILE_URL = "JMakeMoney";

    /**
     * 刷新首页的余额
     */
    public static final String SENDMSG_REFRESH = "com.main.refresh.login";

    public static final String WEB_URL = "file:///" + Environment.getExternalStorageDirectory() + "/" + Constants.APP_FILE_URL + "/download/web/html/";
//    public static final String WEB_URL = "file:///mnt/sdcard/JMakeMoney/download/web/html/";
//    public static final String WEB_URL = "http://192.168.1.211:5222/html/";
//    public static String WEB_URL = "http://git.bramble.wang/rblz/html/";

    public static String UPLOAD_HTML = "file:///android_asset/html/upload_loading.html";

    public static final String TOKEN = "4c6d819ae6d50125158d253622f43868";

    /**
     * 查询是否要更新HTML的接口
     */
    public static final String UPDATE_HTML = REQUEST_DOWNLOAD + "html/rblz/ui.json";
//    public static final String UPDATE_HTML = "http://es2.laizhuan.com/jp_item/resource/download/html/ui.json";

    /**
     * 下载HTML的地址
     */
    public static final String DOWNLOAD_HTML  = REQUEST_DOWNLOAD + "html/rblz/build.zip";
//    public static final String DOWNLOAD_HTML  = "http://mxfc.github.io/rblz/build.zip";

    /**
     * 下载apk文件的地址
     */
    public static final String DOWNLOAD_APP  = REQUEST_DOWNLOAD + "apk/app-release.apk";
//    public static final String DOWNLOAD_APP  = "http://gdown.baidu.com/data/wisegame/fd84b7f6746f0b18/baiduyinyue_4802.apk";

    /**
     * 登录
     */
    public static final String LOGIN_PATH = REQUEST + "user/login2";

    /**
     *绑定师傅ID
     */
    public static final String INVITE_RULE = REQUEST + "user/inviteRule";

    /**
     * 查找首页信息
     */
    public static final String GET_INDEX_INFO = REQUEST + "user/getIndexInfo";

    /**
     * 我的钱包
     */
    public static final String GET_WALLET_INFO = REQUEST + "user/walletInfo";

    /**
     * 绑定FaceBook
     */
    public static final String BIND_FACEBOOK = REQUEST + "user/bindFb";

    /**
     * 钱包提现
     */
    public static final String GET_REDEEM_INFO = REQUEST + "user/getRedeemInfo";

    /**
     * 兑换记录
     */
    public static final String GET_REDEEM_RECORDS_INFO = REQUEST + "user/exchangeLog";

    /**
     * 申请积分兑换
     */
    public static final String GET_EXCHANGE_LOG = REQUEST + "user/exchangeOrder";

    /**
     * 任务列表
     */
    public static final String GET_TASKS_INFO = REQUEST + "task/taskList";

    /**
     * 任务详情
     */
    public static final String GET_TASKS_DETAIL_INFO = REQUEST + "task/taskDetail";

    /**
     * 开始任务
     */
    public static final String GET_START_TASK = REQUEST + "task/taskStart";

    /**
     * 任务记录
     */
    public static final String GET_TASK_REV = REQUEST + "task/taskRev";

    /**
     * 好友提成
     */
    public static final String GET_FRIEND_REV= REQUEST + "user/friendRev";

    /**
     * 获取分享信息
     */
    public static final String GET_SHARE_MSG = REQUEST + "common/share";

    /**
     * 获取APP是更新的信息
     */
    public static final String GET_UPDATE_MSG = REQUEST + "/common/ckVer";

}
