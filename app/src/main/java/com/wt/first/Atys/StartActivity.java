package com.wt.first.Atys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.wt.first.R;

import cn.bmob.push.BmobPush;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.update.BmobUpdateAgent;

/**
 * Created by jing107 on 2016/3/23 0023.
 *
 * 程序启动逻辑：
 * 1.初始化Bmob sdk，获取本地缓存的用户
 * 2.如果缓存用户为空，则跳转至登录/注册界面
 * 3.如果缓存用户不为空，则跳转至应用主页面
 */
public class StartActivity extends Activity {

    private static final String TAG = StartActivity.class.getSimpleName();

    //bmob的app id
    private static final String BMOB_APPID = "fb31f2cdfdd884065c312d4150186e59";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //获取缓存的本地用户,并将其传递过去
                BmobUser currentUser = BmobUser.getCurrentUser(StartActivity.this);
                if (currentUser != null) {
                    //主界面
                    Intent intent = new Intent(StartActivity.this, MainActivity.class);
                    intent.putExtra("objectID",currentUser.getObjectId());
                    startActivity(intent);
                } else {
                    //登录/注册 界面
                    Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                    startActivity(intent);
                }

                finish();
            }
        },3000);

        /**
         * 初始化Bmob SDK
         */
        Bmob.initialize(StartActivity.this, BMOB_APPID);

        /**
         * 使用推送服务时候的初始化操作
         */
        BmobInstallation.getCurrentInstallation(StartActivity.this).save();

        /**
         * 启动推送服务
         */
        BmobPush.startWork(StartActivity.this);

        /**
         * 初始化APPVersion表，该表生成可以将其注释掉
         */
//        BmobUpdateAgent.initAppVersion(this);

        /**
         * WiFi环境下启动将进行自动更新检测
         */
        BmobUpdateAgent.update(StartActivity.this);
    }

}
