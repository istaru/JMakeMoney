package com.shhb.jpan.lz.Tools;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.webkit.WebView;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Moon on 2016/4/5.
 */
public class BaseTools {
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /** 获取屏幕的宽度 */
    public final static int getWindowsWidth(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /** 获取屏幕的高度 */
    public final static int getWindowsHeight(Activity activity){
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    /** px转化为dp */
    public final static float pxChangeDp(Context context, float pxValue){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /** dp转化为px */
    public final static float dpChangePx(Context context, float dpValue){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /** 将流转化为String */
    public static String toString(Reader input) {
        StringWriter sw = new StringWriter();
        copy(input, sw);
        return sw.toString();
    }

    public static int copy(Reader input, Writer output) {
        long count = copyLarge(input, output);
        return count > Integer.MAX_VALUE ? -1 : (int)count;
    }

    public static long copyLarge(Reader input, Writer output) throws RuntimeException {
        try {
            char[] buffer = new char[DEFAULT_BUFFER_SIZE];
            long count = 0;
            int n;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
                count += n;
            }
            return count;
        } catch( IOException e ) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 验证是否为支付宝账号
     * @param alipayAccount
     * @return
     */
    public static boolean checkAlipay(String alipayAccount){
        boolean flag = false;
        try{
            if(isNumeric(alipayAccount)){
                if(alipayAccount.length() == 11){
                    flag = true;
                } else {
                    flag = false;
                }
            } else {
                String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
                Pattern regex = Pattern.compile(check);
                Matcher matcher = regex.matcher(alipayAccount);
                flag = matcher.matches();
            }
        }catch(Exception e){
            flag = false;
        }
        return flag;
    }

    /** 验证是否为数字 */
    public static boolean isNumeric(String str){
        if(!TextUtils.equals("",str)){
            Pattern pattern = Pattern.compile("[0-9]*");
            Matcher isNum = pattern.matcher(str);
            if(!isNum.matches() ){
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 验证是否为中国人的名字
     * @param str
     * @return
     */
    public static boolean isChinaName(String str){
        Pattern pattern = Pattern.compile("^(([\\u4e00-\\u9fa5]{2,8}))$");
        Matcher isNum = pattern.matcher(str);
        if(!isNum.matches() ){
            return false;
        }
        return true;
    }

    /**
     * 将时间戳转成时间
     * @param str
     * @return
     */
    public static String changeDate(int type,String str){
        long date1 = Long.parseLong(str);
        Date date2 = null;
        SimpleDateFormat sd = null;
        if(type == 0){
            date2 = new Date(date1);
            sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        } else {
            date2 = new Date(date1 * 1000);
            sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        return sd.format(date2);
    }

    /**
     * 判断两个时间是否为同一天
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameData(Date date1, Date date2){
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        boolean isSameYear = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        boolean isSameMonth = isSameYear && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
        boolean isSameDate = isSameMonth && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
        return isSameDate;
    }

    public static String getDense(String rNumber) {
        String sha1 = "";
        if(!TextUtils.equals("", rNumber)){
            sha1 = new StringBuffer(rNumber).reverse().toString();
            try {
                sha1 = AES.md5(sha1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return sha1;
        }
        return sha1;
    }

    /**
     * 将积分换成钱
     * @param point
     * @return
     *  直接删除多余的小数位，如2.35会变成2.3 setScale(1,BigDecimal.ROUND_DOWN)
        进位处理，2.35变成2.4  setScale(1,BigDecimal.ROUND_UP)
        四舍五入，2.35变成2.4  setScale(1,BigDecimal.ROUND_HALF_UP)
        四舍五入，2.35变成2.3，如果是5则向下舍setScaler(1,BigDecimal.ROUND_HALF_DOWN)
     */
    public static double pointhangeMoney(double point){
        double money = (point / 100) * 0.55;
        BigDecimal bigDecimal = new BigDecimal(money);
        bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
        money = bigDecimal.doubleValue();
        return money;
    }

    /** 产生随机数*/
    public static int rNumber() {
        return 1+(int)(Math.random()*50);
    }

    /**
     * 将url进行encode编码
     * @param url
     * @return
     */
    public static String urlEncode(String url) {
        try {
            return  java.net.URLEncoder.encode(url,"utf-8");
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * bitmap转为base64
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * base64转为bitmap
     * @param base64Data
     * @return
     */
    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    //手机号码的格式
    public static void phoneNumAddSpace(final EditText mEditText) {
        mEditText.addTextChangedListener(new TextWatcher() {
            int beforeTextLength = 0;
            int onTextLength = 0;
            boolean isChanged = false;

            int location = 0;// 记录光标的位置
            private char[] tempChar;
            private StringBuffer buffer = new StringBuffer();
            int konggeNumberB = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                beforeTextLength = s.length();
                if (buffer.length() > 0) {
                    buffer.delete(0, buffer.length());
                }
                konggeNumberB = 0;
                for (int i = 0; i < s.length(); i++) {
                    if (s.charAt(i) == ' ') {
                        konggeNumberB++;
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                onTextLength = s.length();
                buffer.append(s.toString());
                if (onTextLength == beforeTextLength || onTextLength <= 3
                        || isChanged) {
                    isChanged = false;
                    return;
                }
                isChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isChanged) {
                    location = mEditText.getSelectionEnd();
                    int index = 0;
                    while (index < buffer.length()) {
                        if (buffer.charAt(index) == ' ') {
                            buffer.deleteCharAt(index);
                        } else {
                            index++;
                        }
                    }

                    index = 0;
                    int konggeNumberC = 0;
                    while (index < buffer.length()) {
                        if ((index == 3 || index == 8)) {
                            buffer.insert(index, ' ');
                            konggeNumberC++;
                        }
                        index++;
                    }

                    if (konggeNumberC > konggeNumberB) {
                        location += (konggeNumberC - konggeNumberB);
                    }

                    tempChar = new char[buffer.length()];
                    buffer.getChars(0, buffer.length(), tempChar, 0);
                    String str = buffer.toString();
                    if (location > str.length()) {
                        location = str.length();
                    } else if (location < 0) {
                        location = 0;
                    }

                    mEditText.setText(str);
                    Editable etable = mEditText.getText();
                    Selection.setSelection(etable, location);
                    isChanged = false;
                }
            }
        });
    }

    /**
     * 创建APP的文件夹
     */
    public static void createSDCardDir() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            // 创建一个文件夹对象，赋值为外部存储器的目录
            File sdcardDir = Environment.getExternalStorageDirectory();
            //得到一个路径，内容是sdcard的文件夹路径和名字
            String path = sdcardDir.getPath() + "/" +Constants.APP_FILE_URL;
            File path1 = new File(path);
            if (!path1.exists()) {
                //若不存在，创建目录，可以在应用启动的时候创建
                path1.mkdirs();
            }
        } else {
            return;
        }
    }

    /**
     * 创建文件夹
     * @return
     */
    public static File makeFile(){
        File appDir = new File(Environment.getExternalStorageDirectory(), Constants.APP_FILE_URL + "/download");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        return appDir;
    }

    /**
     * 固定请求参数并带有加密方法
     * @param map
     * @param dataJson
     * @return
     */
    public static String addJson(Map<String,Object> map, JSONObject dataJson){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token",Constants.TOKEN);
        if(null != map){
            for (Map.Entry<String,Object> m:map.entrySet()){
                jsonObject.put(m.getKey(),m.getValue());
            }
        }
        jsonObject.put("type","1");//1为Android
        if(null != dataJson.getString("isEncrypt")){//不用加密
            dataJson.remove("isEncrypt");
            jsonObject.put("data",dataJson);//data数据
            jsonObject.toString();
            return jsonObject.toString();
        } else {//需要加密
            dataJson.remove("isEncrypt");
            jsonObject.put("data",dataJson);//data数据
            String data = jsonObject.toString();
            long secret = AES.get10Random();
            String content = AES.encrypt(data,AES.md5(AES.longMinusNum(secret+"")));
            JSONObject jsonEncrypt = new JSONObject();
            jsonEncrypt.put("secret",secret);
            jsonEncrypt.put("content",content);
            return jsonEncrypt.toString();
        }
    }

    /**
     * 获取参数并解密
     * @param json
     * @return
     */
    public static String decryptJson(String json){
        String result = "";
        try {
            JSONObject jsonObject = JSONObject.parseObject(json);
            String secret = jsonObject.getString("secret");
            String content = jsonObject.getString("content");
            result = AES.decrypt(content,AES.md5(AES.longMinusNum(secret)));
        } catch (Exception e){
            result = json;
            e.printStackTrace();
        }
        return result;
    }

    public static String addJson(JSONObject dataJson){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token",Constants.TOKEN);
        jsonObject.put("type","1");
        jsonObject.put("data",dataJson);
        return jsonObject.toString();
    }

}

