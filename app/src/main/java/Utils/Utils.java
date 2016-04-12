package Utils;

import android.content.Context;

/**
 * Created by jing107 on 2016/4/5 0005.
 */
public class Utils {

    public static final String AVATAR_DIR = "/sdcard/Android/data/com.wt.first/avatar/";

    public static int dp2px(Context context,float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;

        return (int) (dpValue*scale+0.5f);
    }

    public static int px2dp(Context context,float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;

        return (int) (pxValue/scale+0.5f);
    }

    /**
     * 如果是其他的错误码，就用bmob自己的错误说明
     * @param errorCode bmob提供的错误码
     * @param errStr bmob提供的默认的错误说明
     * @return
     */
    public static String getErrorString(int errorCode, String errStr) {
        String err;
        switch (errorCode) {
            case 9001:
                err = "Application Id为空，请初始化";
                break;
            case 9002:
                err = "解析返回数据出错";
                break;
            case 9003:
                err = "上传文件出错";
                break;
            case 9004:
                err = "文件上传失败";
                break;
            case 9005:
                err = "批量操作只支持最多50条";
                break;
            case 9006:
                err = "Object Id为空";
                break;
            case 9007:
                err = "文件大小超过10M";
                break;
            case 9008:
                err = "上传文件不存在";
                break;
            case 9009:
                err = "没有缓存数据";
                break;
            case 9010:
                err = "网络超时";
                break;
            case 9011:
                err = "BmobUser类不支持批量操作";
                break;
            case 9012:
                err = "上下文为空";
                break;
            case 9013:
                err = "BmobObject(数据表名称)格式不正确";
                break;
            case 9014:
                err = "第三方账号授权失败";
                break;
            case 9015:
                err = "其他未知错误";
                break;
            case 9016:
                err = "无网络连接，请检查您的手机网络";
                break;
            case 9017:
                err = "第三方登录出错";
                break;
            case 9018:
                err = "参数不能为空";
                break;
            case 9019:
                err = "手机、邮箱、验证码的格式不正确";
                break;
            default:
                err = errStr;
                break;
        }

        return err;
    }

    /**
     * 通过文件的URL得到文件名
     * @param url
     * @return
     */
    public static String getFileNameByUrl(String url) {
        int index = url.lastIndexOf("/");
        if (index > 0) {
            return url.substring(index+1);
        }

        return null;
    }
}
