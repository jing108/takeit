package com.wt.first.Atys;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.wt.first.Bean.TakeitAccount;
import com.wt.first.Bean.TakeitUser;
import com.wt.first.CustomView.CircleHead;
import com.wt.first.Fragment.DetailTab;
import com.wt.first.Fragment.MeTab;
import com.wt.first.Fragment.TakeItTab;
import com.wt.first.LoadAvatar;
import com.wt.first.R;

import java.io.File;
import java.text.DecimalFormat;

import Utils.Utils;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.GetListener;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private FragmentManager fragmentManager;

    private LinearLayout tabBottomTakeit;
    private LinearLayout tabBottomMe;
    private LinearLayout tabBottomDetail;

    private MeTab tabMe;
    private TakeItTab tabTakeit;
    private DetailTab tabDetail;

    private long exitTime = 0;

    //actionbar上的标题
    private TextView tvTitle;

    private static final int LOAD_AVATAR_COMPLETE = 1;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            switch (msg.what) {
                case LOAD_AVATAR_COMPLETE:
                    CircleHead ch = (CircleHead) bundle.getSerializable("CircleHead");
                    String fileName = bundle.getString("fileName");
                    ch.setResource(BitmapFactory.decodeFile(Utils.AVATAR_DIR+fileName));
                    break;
            }
            return false;
        }
    });

    /**
     * 唯一标识用户的ID
     */
    private String objectID;
    private TakeitAccount account;
    /**
     * 保存一个当前用户的对象
     */
    private TakeitUser userMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * 初始化ImageLoader
         */
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(this);
        ImageLoader.getInstance().init(configuration);


        initViews();

        fragmentManager = getSupportFragmentManager();

        setSelectionTab(0);

        getUserInfoByUserId();

    }

    /**
     * 根据ObjectId更新用户的信息
     * 暂定：每次加载TabTakeit页面的时候，都更新一下用户的信息
     */
    public void getUserInfoByUserId() {
        /**
         * 更新当前用户的金额数
         */
        objectID = getIntent().getStringExtra("objectID");
        BmobQuery<TakeitUser> query = new BmobQuery<TakeitUser>();
        query.getObject(this, objectID, new GetListener<TakeitUser>() {
            @Override
            public void onSuccess(TakeitUser takeitUser) {
                Log.e(TAG,"查询成功");
                userMe = takeitUser;
                tabTakeit.setUserMe(userMe);
                //更新头像
                BmobFile avatar = takeitUser.getAvatar();
                if (avatar != null) {
                    String name = Utils.getFileNameByUrl(avatar.getUrl());
                    File file = new File(Utils.AVATAR_DIR,name);
                    if (file.exists()) {
                        tabTakeit.getMyHead().setResource(BitmapFactory.decodeFile(Utils.AVATAR_DIR+name));
                    } else {
                        new LoadAvatar(tabTakeit.getMyHead(),
                                Utils.getFileNameByUrl(avatar.getUrl()),
                                handler,avatar.getFileUrl(MainActivity.this)).start();
                    }
                } else {
                    tabTakeit.getMyHead().setResource(R.drawable.def_headimg);
                }
                account = takeitUser.getAccount();
                if (account != null) {
                    tabTakeit.getBtnAddAnAccount().setVisibility(View.GONE);
                    tabTakeit.getTv_jieZhang().setVisibility(View.VISIBLE);
                    tabTakeit.getTv_quitAccount().setVisibility(View.VISIBLE);
                    tabTakeit.getTvMoney().setText(new DecimalFormat("0.00").format(userMe.getMoney()));
                } else {
                    tabTakeit.getBtnAddAnAccount().setVisibility(View.VISIBLE);
                }

                /**
                 * 更新当前用户所属账单的其他用户到界面
                 */
                tabTakeit.getAccountOthers(account);
            }

            @Override
            public void onFailure(int i, String s) {
                Log.e(TAG,"查询失败：" + s);
                userMe = BmobUser.getCurrentUser(MainActivity.this,TakeitUser.class);
            }
        });
    }

    private void setSelectionTab(int index) {

        ImageButton imageBtn;
        TextView tv;

        //重设按钮的状态
        resetBtn();

        //开启一个事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        hideFragment(transaction);

        switch (index) {
            case 0:
                imageBtn = (ImageButton) tabBottomTakeit.findViewById(R.id.btn_takeit);
                tv = (TextView) tabBottomTakeit.findViewById(R.id.tv_takeit);
                imageBtn.setImageResource(R.drawable.free_pressed);
                tv.setTextColor(getResources().getColor(R.color.primary_color));
                tvTitle.setText("先记上");

                if(tabTakeit == null) {
                    tabTakeit = new TakeItTab();
                    transaction.add(R.id.content,tabTakeit);
                }else {
                    /**
                     * 不一定要在这里刷新，后续需要进行优化。
                     * 会增加下拉刷新的功能。
                     */

                    getUserInfoByUserId();
                    transaction.show(tabTakeit);
                }
                break;
            case 1:
                imageBtn = (ImageButton) tabBottomDetail.findViewById(R.id.btn_detail);
                tv = (TextView) tabBottomDetail.findViewById(R.id.tv_detail);
                imageBtn.setImageResource(R.drawable.find_pressed);
                tv.setTextColor(getResources().getColor(R.color.primary_color));
                tvTitle.setText("明细");

                if(tabDetail == null) {
                    tabDetail = new DetailTab();
                    transaction.add(R.id.content,tabDetail);
                }else {
                    transaction.show(tabDetail);
                }
                tabDetail.setUserMe(userMe);
                break;
            case 2:
                imageBtn = (ImageButton) tabBottomMe.findViewById(R.id.btn_me);
                tv = (TextView) tabBottomMe.findViewById(R.id.tv_me);
                imageBtn.setImageResource(R.drawable.me_pressed);
                tv.setTextColor(getResources().getColor(R.color.primary_color));
                tvTitle.setText("我");

                if(tabMe == null) {
                    tabMe = new MeTab();
                    transaction.add(R.id.content,tabMe);
                }else {
                    transaction.show(tabMe);
                }
                tabMe.setUserMe(userMe);
                break;

            default:
                break;
        }

        transaction.commit();
    }

    private void hideFragment(FragmentTransaction transaction) {
        if(tabTakeit != null) {
            transaction.hide(tabTakeit);
        }

        if(tabDetail != null) {
            transaction.hide(tabDetail);
        }

        if(tabMe != null) {
            transaction.hide(tabMe);
        }
    }

    //清除所有选中状态
    private void resetBtn() {

        ImageButton imageBtn;
        TextView tv;

        imageBtn = (ImageButton) tabBottomTakeit.findViewById(R.id.btn_takeit);
        tv = (TextView) tabBottomTakeit.findViewById(R.id.tv_takeit);
        imageBtn.setImageResource(R.drawable.free);
        tv.setTextColor(Color.DKGRAY);

        imageBtn = (ImageButton) tabBottomDetail.findViewById(R.id.btn_detail);
        tv = (TextView) tabBottomDetail.findViewById(R.id.tv_detail);
        imageBtn.setImageResource(R.drawable.find);
        tv.setTextColor(Color.DKGRAY);

        imageBtn = (ImageButton) tabBottomMe.findViewById(R.id.btn_me);
        tv = (TextView) tabBottomMe.findViewById(R.id.tv_me);
        imageBtn.setImageResource(R.drawable.me);
        tv.setTextColor(Color.DKGRAY);
    }

    private void initViews() {

        tabBottomDetail = (LinearLayout) findViewById(R.id.tabBtn_detail);
        tabBottomMe = (LinearLayout) findViewById(R.id.tabBtn_me);
        tabBottomTakeit = (LinearLayout) findViewById(R.id.tabBtn_takeit);

        tabBottomTakeit.setOnClickListener(this);
        tabBottomMe.setOnClickListener(this);
        tabBottomDetail.setOnClickListener(this);

        tvTitle = (TextView) findViewById(R.id.tv_actionbar_title);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tabBtn_takeit:
                setSelectionTab(0);
                break;
            case R.id.tabBtn_detail:
                setSelectionTab(1);
                break;
            case R.id.tabBtn_me:
                setSelectionTab(2);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - exitTime > 2500) {
            Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }

    //用于在别处向此handler发送消息
    public Handler getHandler() {
        return handler;
    }
}
