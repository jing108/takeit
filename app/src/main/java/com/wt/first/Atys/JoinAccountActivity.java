package com.wt.first.Atys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wt.first.Bean.TakeitAccount;
import com.wt.first.R;
import com.wt.first.zxing.activity.CaptureActivity;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by jing107 on 2016/3/25 0025.
 */
public class JoinAccountActivity extends Activity implements View.OnClickListener {

    private static final String TAG = JoinAccountActivity.class.getSimpleName();

    private static final int REQUEST_FOR_ER = 1;

    private static final int RESULT_CODE_OK = 10;
    private static final int RESULT_CODE_CANCELED = 11;

    private Button btnJoinAccount;
    private Button btnCreateAccount;
    private EditText etAccountName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_join_account);

        initViews();
    }

    private void initViews() {
        btnCreateAccount = (Button) findViewById(R.id.btn_create_account);
        btnJoinAccount = (Button) findViewById(R.id.btn_join_account);
        btnCreateAccount.setOnClickListener(this);
        btnJoinAccount.setOnClickListener(this);

        etAccountName = (EditText) findViewById(R.id.et_accountName);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_join_account:
                /**
                 * 1.从输入框中获取到账本编号
                 * 2.首先查询该账本是否存在
                 * 3.如果存在，将其返回
                 *
                 * 已经修改为扫描二维码获取到账本编号
                 */
                Intent intent = new Intent(this, CaptureActivity.class);
                startActivityForResult(intent,REQUEST_FOR_ER);
                break;

            case R.id.btn_create_account:
                /**
                 * 1.从输入框中获取到账本名称(账本名可以为空)
                 * 2.将该账本返回
                 */
                String accountName = etAccountName.getText().toString();
                final TakeitAccount account = new TakeitAccount();
                account.setAccountName(accountName);

                account.save(JoinAccountActivity.this, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(JoinAccountActivity.this, "创建账本成功", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "向TakeitAccount表添加数据成功，返回objectId为："+account.getObjectId()+
                                "，数据在服务端的创建时间为：" + account.getCreatedAt());
                        Intent data = new Intent();
                        data.putExtra("account",account);
                        setResult(RESULT_CODE_OK,data);
                        finish();
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        Toast.makeText(JoinAccountActivity.this, "创建账本失败：" + s, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "创建账本失败：" + s);
                    }
                });
                break;

            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CODE_CANCELED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_FOR_ER:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String accountId = bundle.getString("result");

                    if (accountId.isEmpty()) {
                        Toast.makeText(JoinAccountActivity.this, "扫描获取账本失败", Toast.LENGTH_SHORT).show();
                    } else {
                        BmobQuery<TakeitAccount> query = new BmobQuery<TakeitAccount>();
                        query.getObject(JoinAccountActivity.this, accountId, new GetListener<TakeitAccount>() {
                            @Override
                            public void onSuccess(TakeitAccount takeitAccount) {
                                Log.e(TAG,"查询账本成功");
                                //将账本返回
                                Intent data = new Intent();
                                data.putExtra("account",takeitAccount);
                                setResult(RESULT_CODE_OK,data);
                                finish();
                            }

                            @Override
                            public void onFailure(int i, String s) {
                                Log.e(TAG,"查询账本失败：" + s);
                                Toast.makeText(JoinAccountActivity.this, "加入账本失败：" + s, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                break;
        }
    }
}
