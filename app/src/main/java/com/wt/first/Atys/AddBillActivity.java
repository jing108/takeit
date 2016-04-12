package com.wt.first.Atys;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.wt.first.Adapter.UserInfo;
import com.wt.first.Adapter.UserInfoAdapter;
import com.wt.first.Bean.TakeitAccount;
import com.wt.first.Bean.TakeitBill;
import com.wt.first.Bean.TakeitUser;
import com.wt.first.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import Utils.Utils;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by jing107 on 2016/4/2 0002.
 */
public class AddBillActivity extends Activity implements View.OnClickListener {

    private static final String TAG = AddBillActivity.class.getSimpleName();
    private static final String REMOTE_UPDATEMONEY_AND_PUSHMSG = "updateMoneyAndPushMsg";
    private static final String REMOTE_FUNCTION_updateMoneyAndPushMsg = "http://cloud.bmob.cn/b943acb61bdf78f6/"
            +REMOTE_UPDATEMONEY_AND_PUSHMSG+"?";

    private TakeitUser userMe;
    private int type;
    private TakeitAccount account;
    private List<TakeitUser> users;

    private Button btn_AddBill;
    private EditText et_money;
    private EditText et_content;
    private ImageButton ibtn_input_money;
    private ImageButton ibtn_content;

    private int numOfSelected = 0;

    private ListView listView;
    private List<UserInfo> userInfos = new ArrayList<UserInfo>();
    UserInfoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill);

        Intent intent = getIntent();
        type = intent.getIntExtra("type",-1);
        userMe = (TakeitUser) intent.getSerializableExtra("userMe");
        account = (TakeitAccount) intent.getSerializableExtra("account");

        initListView();

        btn_AddBill = (Button) findViewById(R.id.btn_add_an_bill);
        btn_AddBill.setOnClickListener(this);
        et_money = (EditText) findViewById(R.id.et_input_money);
        et_content = (EditText) findViewById(R.id.et_content);
        ibtn_content = (ImageButton) findViewById(R.id.ibtn_content);
        ibtn_content.setOnClickListener(this);
        ibtn_input_money = (ImageButton) findViewById(R.id.ibtn_input_money);
        ibtn_input_money.setOnClickListener(this);

        et_money.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() == 0) {
                    ibtn_input_money.setVisibility(View.GONE);
                } else {
                    ibtn_input_money.setVisibility(View.VISIBLE);
                }
                setButtonEnabled();
            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if (str.contains(".")) {
                    int pos = str.indexOf(".");
                    if (pos == 0) { //小数点不能开头
                        s.delete(0, 1);
                    }
                    if (str.length() - pos - 1 > 2) {
                        s.delete(pos + 3, pos + 4);
                    }
                }
            }
        });

        et_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() == 0) {
                    ibtn_content.setVisibility(View.GONE);
                } else {
                    ibtn_content.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initListView() {
        listView = (ListView) findViewById(R.id.lv_chose_user);

        BmobQuery<TakeitUser> query = new BmobQuery<TakeitUser>();
        query.addWhereEqualTo("account", account);
        query.addWhereNotEqualTo("objectId", userMe.getObjectId());
        query.setLimit(10);

        query.findObjects(this, new FindListener<TakeitUser>() {
            @Override
            public void onSuccess(List<TakeitUser> list) {
                Log.e(TAG, "查询当前账本的其他用户成功");
                users = list;

                UserInfo info;
                for (TakeitUser u: users) {
                    info = new UserInfo();
                    if (u.getAvatar() != null) {
                        String name = Utils.getFileNameByUrl(u.getAvatar().getUrl());
                        info.setBitmap(BitmapFactory.decodeFile(Utils.AVATAR_DIR+name));
                    } else {
                        info.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.def_headimg));
                    }
                    info.setBeSelected(false);
                    info.setUserName(u.getNickName());
                    userInfos.add(info);
                }

                adapter = new UserInfoAdapter(AddBillActivity.this,R.layout.userinfo_item,userInfos);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (userInfos.get(position).isBeSelected()) {
                            userInfos.get(position).setBeSelected(false);
                            numOfSelected--;
                        } else {
                            userInfos.get(position).setBeSelected(true);
                            numOfSelected++;
                        }

                        adapter.notifyDataSetChanged();

                        //设置按钮 确认添加  的可点击状态
                        setButtonEnabled();
                    }
                });
            }

            @Override
            public void onError(int i, String s) {
                Log.e(TAG, "查询当前账本的其他用户失败：" + s);
                Toast.makeText(AddBillActivity.this, "获取当前账本的其他用户失败" + s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setButtonEnabled() {
        if ((numOfSelected!=0) && (!et_money.getText().toString().isEmpty())) {
            btn_AddBill.setEnabled(true);
            btn_AddBill.setTextColor(0xffffffff);
        } else {
            btn_AddBill.setEnabled(false);
            btn_AddBill.setTextColor(Color.parseColor("#dadada"));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibtn_content:
                et_content.setText("");
                break;

            case R.id.ibtn_input_money:
                et_money.setText("");
                break;

            case R.id.btn_add_an_bill:
                if (type>0) {
                    createBill();
                    finish();
                } else {
                    Toast.makeText(this,"无效的账单类型",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * 生成账单
     *
     * 由于向服务端请求云端方法这种实现存在bug
     * bug为：会执行两次云端方法，造成数据重复，目前还不清楚原因
     * 修改成，所有功能都客户端来做，虽然不合理，但是只能先这样做了
     * bug已经解决：
     */
    private void createBill() {
        final TakeitBill bill = new TakeitBill();
        final Float money = Float.parseFloat(et_money.getText().toString());
        bill.setMoney(money);
        bill.setAccount(account);
        bill.setContent(et_content.getText().toString());
        //1---KTV
        /**
         * 新增账单逻辑：
         * 1.先在账单表bill中新增一条数据
         * 2.向RelationUserAndBill中增加N条数据
         * 3.向此账单的参与人员发送一条推送
         *    3.1应该修改成，将此订单的信息发送到服务端，然后服务端修改各个用户的余额，
         *       然后向其他用户发送推送消息
         *
         *  4.发送的Json数据格式
         *  {
         *    "billId",bill.getObjectId(),
         *    "whoTakeit",userId,
         *    "others",[user1Id,user2Id,user3Id...]
         *   }
         */
        bill.setType(type);
        bill.setUser(userMe);
        bill.save(this, new SaveListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG,"用户："+userMe.getUsername()+"更新数据成功："+bill.toString());
                Toast.makeText(AddBillActivity.this,"添加账单成功",
                        Toast.LENGTH_SHORT).show();
                /**
                 * 生成Json数据，发送到服务端
                 */
                JSONObject data = new JSONObject();
                try {

                    data.put("billId",bill.getObjectId());
                    data.put("whoTakeit",userMe.getObjectId());
                    data.put("howmuch",money);
                    JSONArray array = new JSONArray();
                    for (int i=0;i<userInfos.size();i++) {
                        if (userInfos.get(i).isBeSelected()) {
                            array.put(users.get(i).getObjectId());
                        }
                    }
                    data.put("whoJoinin",array);

                    new Thread(new RequestRemoteFunc(REMOTE_FUNCTION_updateMoneyAndPushMsg,
                            data.toString())).start();

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                /**
                 * 使用sdk方式访问云端代码会出现  云端代码重复执行的bug
                 * 已经改成通过http 方式调用云端代码
                 */
//                AsyncCustomEndpoints ace = new AsyncCustomEndpoints();
//                ace.callEndpoint(getActivity(),
//                        REMOTE_UPDATEMONEY_AND_PUSHMSG,
//                        param, new CloudCodeListener() {
//                    @Override
//                    public void onSuccess(Object o) {
//                        Log.e(TAG,o.toString());
////                                Toast.makeText(getActivity(),o.toString(),Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onFailure(int i, String s) {
//
//                    }
//                });
            }

            @Override
            public void onFailure(int i, String s) {
                Log.e(TAG,"用户："+userMe.getUsername()+"更新账单("+bill.toString()+")失败");
                Toast.makeText(AddBillActivity.this,"不好意思，你更新账单("+bill.toString()+")失败",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    class RequestRemoteFunc implements Runnable {

        private String funcName;
        private String data;

        public RequestRemoteFunc(String funcName, String data) {
            this.funcName = funcName;
            this.data = data;
        }

        @Override
        public void run() {
            String param = funcName + "data=" + data;
            URL url = null;
            try {
                url = new URL(param);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                InputStream is = connection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line=br.readLine())!=null) {
                    sb.append(line);
                }

                Log.e(TAG,sb.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
