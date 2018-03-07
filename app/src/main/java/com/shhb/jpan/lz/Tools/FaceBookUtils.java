package com.shhb.jpan.lz.Tools;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.shhb.jpan.lz.R;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Kiven on 16/10/12.
 */
public class FaceBookUtils {
    private Activity mActivity ;
    private ShareDialog shareDialog;
    private CallbackManager callBackManager;
    private ShareLinkContent.Builder shareLinkContentBuilder;

    /**
     * 进行Facebook登录前的操作
     * @param activity
     * @param callBackManager
     */
    public FaceBookUtils(Activity activity, CallbackManager callBackManager){
        this.mActivity = activity ;
        this.callBackManager = callBackManager;
    }

    /**
     * 进行Facebook分享前的操作
     * @param activity
     * @param callBackManager
     */
    public FaceBookUtils(Activity activity, CallbackManager callBackManager,FacebookCallback facebookCallback){
        this.mActivity = activity ;
        this.callBackManager = callBackManager;
        shareDialog = new ShareDialog(mActivity);
        //注册分享状态监听回调接口
        shareDialog.registerCallback(callBackManager, facebookCallback);
        shareLinkContentBuilder = new ShareLinkContent.Builder();
    }

    /**
     * Facebook登录
     * @param facebookCallback
     */
    public void login(FacebookCallback facebookCallback){
        List<String> permissions = Arrays.asList("public_profile", "user_friends", "user_status");
        LoginManager.getInstance().logInWithReadPermissions(mActivity, permissions);
        LoginManager.getInstance().registerCallback(callBackManager, facebookCallback);
    }

    /**
     * Facebook分享
     */
    public void share(String contentTitle,String imageUrl,String content,String contentUrl) {
        shareLinkContentBuilder.setContentTitle(contentTitle)
                .setImageUrl(Uri.parse(imageUrl))
                .setContentDescription(content)
                .setContentUrl(Uri.parse(contentUrl));
        ShareLinkContent shareLinkContent = shareLinkContentBuilder.build();
        if(shareDialog.canShow(ShareLinkContent.class)) {
            shareDialog.show(mActivity,shareLinkContent);
        }
    }
}
