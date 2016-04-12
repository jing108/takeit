package com.wt.first.Atys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wt.first.Bean.TakeitInstallation;
import com.wt.first.Bean.TakeitUser;
import com.wt.first.R;

import java.util.List;

import Utils.Utils;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by jing107 on 2016/3/25 0025.
 *
 *
 */
public class LoginActivity extends Activity implements View.OnClickListener {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private Button btnLogin;
    private Button btnSignin;
    private EditText etUserName;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
    }

    /**
     * 初始化控件
     */
    private void initViews() {
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnSignin = (Button) findViewById(R.id.btn_signin);

        btnLogin.setOnClickListener(this);
        btnSignin.setOnClickListener(this);

        etUserName = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);
    }

    @Override
    public void onClick(View v) {

        String userName = etUserName.getText().toString();
        String userPwd = etPassword.getText().toString();

        if (userName.isEmpty() || userPwd.isEmpty()) {
            Toast.makeText(LoginActivity.this, R.string.nameorpwd_cannot_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        switch (v.getId()) {
            case R.id.btn_login:
                final TakeitUser userLogin = new TakeitUser();
                userLogin.setUsername(userName);
                userLogin.setPassword(userPwd);

                userLogin.login(LoginActivity.this, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        updateInstallInfo(userLogin.getObjectId());
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("objectID",userLogin.getObjectId());
                        startActivity(intent);

                        finish();
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        Log.e(TAG,s);
                        Toast.makeText(LoginActivity.this, "登录失败:"+Utils.getErrorString(i,s), Toast.LENGTH_SHORT).show();
                    }
                });
                break;

            case R.id.btn_signin:
                final TakeitUser userSignin = new TakeitUser();
                userSignin.setUsername(userName);
                userSignin.setPassword(userPwd);
                userSignin.setCoins(1000);
                userSignin.setMoney(0.00f);
                userSignin.setNickName(userName);

                userSignin.signUp(LoginActivity.this, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        updateInstallInfo(userSignin.getObjectId());
                        Toast.makeText(LoginActivity.this, R.string.signin_success, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("objectID",userSignin.getObjectId());
                        startActivity(intent);

                        finish();
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        Toast.makeText(LoginActivity.this, R.string.signin_fail + Utils.getErrorString(i,s), Toast.LENGTH_SHORT).show();
                    }
                });
                break;

            default:
                break;
        }
    }


    /**
     * 当用户使用用户名+密码的方式登录，需要更新一下设备与用户之间的绑定
     * 防止用户登录在新设备上，而推送还是到老设备
     * @param uid
     */
    private void updateInstallInfo(final String uid) {
        BmobQuery<TakeitInstallation> query = new BmobQuery<TakeitInstallation>();
        query.addWhereEqualTo("installationId", BmobInstallation.getInstallationId(this));
        query.findObjects(this, new FindListener<TakeitInstallation>() {
            @Override
            public void onSuccess(List<TakeitInstallation> object) {
                if (object.size() > 0) {
                    TakeitInstallation takeitInstallation = object.get(0);
                    takeitInstallation.setUid(uid);
                    takeitInstallation.update(LoginActivity.this, new UpdateListener() {
                        @Override
                        public void onSuccess() {
                            Log.e(TAG,"设备信息更新成功");
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            Log.e(TAG,"设备信息更新失败:" + s);
                        }
                    });
                }
            }

            @Override
            public void onError(int i, String s) {
                Log.e(TAG,"查询用户设备信息失败："+s);
            }
        });
    }
}
