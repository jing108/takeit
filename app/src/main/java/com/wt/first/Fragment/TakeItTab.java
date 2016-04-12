package com.wt.first.Fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wt.first.Atys.AddBillActivity;
import com.wt.first.Atys.InviteJoinByEr;
import com.wt.first.Atys.JoinAccountActivity;
import com.wt.first.Atys.MainActivity;
import com.wt.first.Bean.TakeitAccount;
import com.wt.first.Bean.TakeitUser;
import com.wt.first.CustomView.CircleHead;
import com.wt.first.LoadAvatar;
import com.wt.first.R;

import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import Utils.Utils;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by jing107 on 2016/3/22 0022.
 */
public class TakeItTab extends Fragment implements View.OnClickListener, Serializable {

    private static final String TAG = TakeItTab.class.getSimpleName();
    private static final String AVATAR_DIR = "/sdcard/Android/data/com.wt.first/avatar/";

    private static final int REQUEST_CODE_ADD_ACCOUNT = 1;
    private static final int REQUEST_CODE_ADD_BILL = 2;
    private static final int RESULT_CODE_OK = 10;
    private static final int RESULT_CODE_CANCELED = 11;

    private TakeitUser userMe;
    public List<TakeitUser> userOthers;

    private Button btnAddAnAccount;

    private List<Button> mBtnBillTypes = new ArrayList<Button>();
    /**
     * 账单类型的button数组，用于产生动画效果
     */
    private int[] buttonIds = new int[]{
            R.id.btn_billtype_takeit,
            R.id.btn_billtype_eat,
            R.id.btn_billtype_life,
            R.id.btn_billtype_other,
            R.id.btn_billtype_play
    };

    private int[] othersLayoutId = new int[]{
            R.id.others_1,
            R.id.others_2,
            R.id.others_3,
            R.id.others_4,
            R.id.others_5,
            R.id.others_6,
    };

    private TextView tvMoney;

    private TakeitAccount account;

    private LayoutInflater inflater;
    /**
     * 保存一下这个Fragment的根布局
     */
    private View root;

    private RelativeLayout layoutAccountName;
    private TextView tvAccountName;

    private boolean mFlag = true;

    //获取屏幕宽度
    private DisplayMetrics metrics;

    TextView tv_quitAccount;
    TextView tv_jieZhang;

    CircleHead myHead;
    private CircleHead ch_tmp;

    public void setUserMe(TakeitUser userMe) {
        this.userMe = userMe;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        root = inflater.inflate(com.wt.first.R.layout.activity_takeit_tab, container, false);
        btnAddAnAccount = (Button) root.findViewById(R.id.btn_add_an_account);
        btnAddAnAccount.setOnClickListener(this);
        Button btn;
        for (int i = 0; i < buttonIds.length; i++) {
            btn = (Button) root.findViewById(buttonIds[i]);
            btn.setOnClickListener(this);
            mBtnBillTypes.add(btn);
        }

        tvMoney = (TextView) root.findViewById(R.id.tv_money);

        tvAccountName = (TextView) root.findViewById(R.id.tv_account_name);
        layoutAccountName = (RelativeLayout) root.findViewById(R.id.layout_account_name);
        layoutAccountName.setOnClickListener(this);

        tv_jieZhang = (TextView) root.findViewById(R.id.tv_jiezhang);
        tv_jieZhang.setOnClickListener(this);
        tv_quitAccount = (TextView) root.findViewById(R.id.tv_quit_account);
        tv_quitAccount.setOnClickListener(this);

        metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        myHead = (CircleHead) root.findViewById(R.id.takeit_my_head);

        return root;
    }

    public Button getBtnAddAnAccount() {
        return btnAddAnAccount;
    }

    public TextView getTvMoney() {
        return tvMoney;
    }

    public TextView getTv_quitAccount() {
        return tv_quitAccount;
    }

    public TextView getTv_jieZhang() {
        return tv_jieZhang;
    }

    public CircleHead getMyHead() {
        return myHead;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_add_an_account:
                Intent intent = new Intent(getActivity(), JoinAccountActivity.class);
//                intent.putExtra("user",userMe);
                startActivityForResult(intent, REQUEST_CODE_ADD_ACCOUNT);
                break;

            case R.id.btn_billtype_play:
                startAddBillAty(1);
                break;

            case R.id.btn_billtype_eat:
                startAddBillAty(2);
                break;

            case R.id.btn_billtype_life:
                startAddBillAty(3);
                break;

            case R.id.btn_billtype_other:
                startAddBillAty(4);
                break;

            case R.id.btn_billtype_takeit:
                if (mFlag) {
                    startAnim();
                } else {
                    closeAnim();
                }
                break;

            case R.id.tv_quit_account:
                /**
                 * 清除账本信息需要当前的用户对象userMe
                 * 1.需要判断当前用户的金额是否为0，如果不为0，则不允许离开账本
                 * 2.通过setValue将account值赋为null
                 * 3.提交更新
                 */
                if (userMe.getMoney().compareTo(0.0f) != 0) {
                    Toast.makeText(getActivity(), R.string.not_allow_leave_account, Toast.LENGTH_SHORT).show();
                } else {
                    userMe.remove("account");
                    userMe.update(getActivity(), new UpdateListener() {
                        @Override
                        public void onSuccess() {
                            tvMoney.setText("");
                            tv_jieZhang.setVisibility(View.GONE);
                            tv_quitAccount.setVisibility(View.GONE);
                            btnAddAnAccount.setVisibility(View.VISIBLE);
                            Toast.makeText(getActivity(), "已成功离开账本", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            Log.e(TAG, "清除用户账本信息失败：" + s);
                            Toast.makeText(getActivity(), "离开账本失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;

            case R.id.layout_account_name:
                if (account == null) {
                    Toast.makeText(getActivity(),"当前没有加入账本",Toast.LENGTH_SHORT).show();
                    break;
                }
                final EditText et = new EditText(getContext());
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("编辑账本名称");
                builder.setView(et);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String name = et.getText().toString();
                        if (name.equals("") && name.equals(account.getAccountName()) && name==null) {
                            return;
                        }
                        account.setValue("accountName",name);
                        account.update(getContext(), new UpdateListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getActivity(), "账本名称修改成功", Toast.LENGTH_SHORT).show();
                                tvAccountName.setText(name);
                            }

                            @Override
                            public void onFailure(int i, String s) {
                                Toast.makeText(getActivity(), "账本名称修改失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.setNegativeButton("取消",null);
                builder.show();
                break;

            case R.id.tv_jiezhang:
                break;

            default:
                break;
        }
    }

    private void startAddBillAty(int type) {
        if (account == null) {
            Toast.makeText(getContext(),"当前没有加入账本",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getActivity(), AddBillActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("userMe", userMe);
        intent.putExtra("account", account);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_ADD_ACCOUNT:
                if (resultCode == RESULT_CODE_OK) {
                    account = (TakeitAccount) data.getSerializableExtra("account");
                    setUserAccount();
                    getAccountOthers(account);
                } else if (resultCode == RESULT_CODE_CANCELED) {
//                    Toast.makeText(getActivity(), "加入账本的操作取消了", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }

    //设置账本名称
    private void setAccountName() {
        BmobQuery<TakeitAccount> query = new BmobQuery<TakeitAccount>();
        query.getObject(getActivity(), account.getObjectId(), new GetListener<TakeitAccount>() {
            @Override
            public void onSuccess(TakeitAccount takeitAccount) {
                account = takeitAccount;
                tvAccountName.setText(takeitAccount.getAccountName());
            }

            @Override
            public void onFailure(int i, String s) {

            }
        });
    }

    /**
     * 获取账本中的其他用户
     */
    public void getAccountOthers(TakeitAccount acc) {
        if (acc != null) {
            account = acc;
            setAccountName();
            BmobQuery<TakeitUser> query = new BmobQuery<TakeitUser>();
            query.addWhereEqualTo("account", acc);
            query.addWhereNotEqualTo("objectId", userMe.getObjectId());
            query.setLimit(10);

            query.findObjects(getActivity(), new FindListener<TakeitUser>() {
                @Override
                public void onSuccess(List<TakeitUser> list) {
                    Log.e(TAG, "查询当前账本的其他用户成功");
                    userOthers = list;

                    /**
                     * 将用户更新要界面上
                     */
                    View ll;
                    CircleHead ch;
                    TextView name;
                    for (int i = 0; i < list.size(); i++) {
                        ll = root.findViewById(othersLayoutId[i]);
                        ch = (CircleHead) ll.findViewById(R.id.test_tx);
                        name = (TextView) ll.findViewById(R.id.tv_others_name);
                        BmobFile avatar = list.get(i).getAvatar();
                        if (avatar != null) {
                            String fileName = getFileNameByUrl(avatar.getUrl());
                            final File file = new File(AVATAR_DIR,fileName);
                            if (file.exists()) {
                                ch.setResource(BitmapFactory.decodeFile(AVATAR_DIR+fileName));
                            } else {
                                Log.e(TAG,avatar.getFileUrl(getActivity()));
                                new LoadAvatar(ch,getFileNameByUrl(avatar.getUrl()),
                                        ((MainActivity)getActivity()).getHandler(),
                                        avatar.getFileUrl(getContext())).start();
                            }
                        } else {
                            ch.setResource(R.drawable.def_headimg);
                        }
                        name.setText(list.get(i).getNickName());
                    }

                    //更新邀请
                    if (list.size() < 6) {
                        ll = root.findViewById(othersLayoutId[list.size()]);
                        ll.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), InviteJoinByEr.class);
                                intent.putExtra("accountId", account.getObjectId());
                                startActivity(intent);
                            }
                        });
                        ch = (CircleHead) ll.findViewById(R.id.test_tx);
                        name = (TextView) ll.findViewById(R.id.tv_others_name);
                        ch.setResource(R.drawable.add_friend);
                        name.setText("邀请");
                        name.setTextColor(getResources().getColor(R.color.primary_color));
                    }
                }

                @Override
                public void onError(int i, String s) {
                    Log.e(TAG, "查询当前账本的其他用户失败：" + s);
                    Toast.makeText(getActivity(), "查询当前账本的其他用户失败" + s, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String getFileNameByUrl(String url) {
        int index = url.lastIndexOf("/");
        if (index > 0) {
            return url.substring(index+1);
        }

        return null;
    }

    /**
     * 设置用户的账本信息
     */
    private void setUserAccount() {
        userMe.setValue("account", account);
        userMe.update(getActivity(), new UpdateListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "更新用户：" + userMe.getUsername() + "的账本信息成功");
                Toast.makeText(getActivity(), "账本入驻成功", Toast.LENGTH_SHORT).show();

                /**隐藏加入账本按钮，显示当前用户金额
                 * 这里是用户第一次加入账本，默认用户金额为0.00
                 */
                btnAddAnAccount.setVisibility(View.GONE);
                tv_jieZhang.setVisibility(View.VISIBLE);
                tv_quitAccount.setVisibility(View.VISIBLE);
                tvMoney.setText(new DecimalFormat("0.00").format(userMe.getMoney()));
            }

            @Override
            public void onFailure(int i, String s) {
                Log.e(TAG, "更新用户：" + userMe.getUsername() + "的账本信息失败");
                Toast.makeText(getActivity(), "账本入驻失败：" + s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 开始动画
     */
    private void startAnim() {
        int w = Utils.dp2px(getContext(), 50);
        float py = (float) ((metrics.widthPixels / 2 - w * 2.5) / 3);
        //记上
        ObjectAnimator animator0 = ObjectAnimator.ofFloat(
                mBtnBillTypes.get(0),
                "alpha",
                1f,
                0.3f
        );

        ObjectAnimator animator1 = ObjectAnimator.ofFloat(
                mBtnBillTypes.get(1),
                "translationX",
                -2 * (py + w)
        );

        ObjectAnimator animator2 = ObjectAnimator.ofFloat(
                mBtnBillTypes.get(2),
                "translationX",
                -(py + w)
        );

        ObjectAnimator animator3 = ObjectAnimator.ofFloat(
                mBtnBillTypes.get(3),
                "translationX",
                2 * (py + w)
        );

        ObjectAnimator animator4 = ObjectAnimator.ofFloat(
                mBtnBillTypes.get(4),
                "translationX",
                py + w
        );

        AnimatorSet set = new AnimatorSet();
        set.setDuration(500);
        set.setInterpolator(new AnticipateOvershootInterpolator());
        set.playTogether(
                animator0,
                animator1,
                animator2,
                animator3,
                animator4
        );
        set.start();
        mFlag = false;
    }

    /**
     * 关闭动画
     */
    private void closeAnim() {
        ObjectAnimator animator0 = ObjectAnimator.ofFloat(
                mBtnBillTypes.get(0),
                "alpha",
                0.3f,
                1f
        );

        ObjectAnimator animator1 = ObjectAnimator.ofFloat(
                mBtnBillTypes.get(1),
                "translationX",
                0f
        );

        ObjectAnimator animator2 = ObjectAnimator.ofFloat(
                mBtnBillTypes.get(2),
                "translationX",
                0f
        );

        ObjectAnimator animator3 = ObjectAnimator.ofFloat(
                mBtnBillTypes.get(3),
                "translationX",
                0f
        );

        ObjectAnimator animator4 = ObjectAnimator.ofFloat(
                mBtnBillTypes.get(4),
                "translationX",
                0f
        );

        AnimatorSet set = new AnimatorSet();
        set.setDuration(500);
        set.setInterpolator(new AnticipateOvershootInterpolator());
        set.playTogether(
                animator0,
                animator1,
                animator2,
                animator3,
                animator4
        );
        set.start();
        mFlag = true;
    }
}
