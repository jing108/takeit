package com.wt.first.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wt.first.Atys.LoginActivity;
import com.wt.first.Bean.TakeitUser;
import com.wt.first.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import Utils.Utils;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.BmobUpdateListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import cn.bmob.v3.update.BmobUpdateAgent;
import cn.bmob.v3.update.UpdateResponse;
import cn.bmob.v3.update.UpdateStatus;

/**
 * Created by jing107 on 2016/3/22 0022.
 */
public class MeTab extends Fragment implements View.OnClickListener {

    private static final String TAG = MeTab.class.getSimpleName();

    private static final String AVATAR_DIR = "/sdcard/Android/data/com.wt.first/avatar/";

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_LOCATION = 2;
    private static final int REQUEST_CROP = 3;

    private Button btnLogout;

    private TakeitUser userMe;

    private LinearLayout llUpdate;
    private LinearLayout llChangeNick;
    private LinearLayout llAbout;

    private ImageView ivAvatar;

    //弹出拍照或相册的对话框
    private AlertDialog mDialog;
    private EditText etNick;

    private Uri imageUri;

    private TextView tvNickShow;
    private TextView tvUserNameShow;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(com.wt.first.R.layout.activity_me_tab, container, false);
        btnLogout = (Button) view.findViewById(R.id.btn_logout);

        btnLogout.setOnClickListener(this);

        llUpdate = (LinearLayout) view.findViewById(R.id.check_update);
        llUpdate.setOnClickListener(this);

        llChangeNick = (LinearLayout) view.findViewById(R.id.change_nickname);
        llChangeNick.setOnClickListener(this);

        llAbout = (LinearLayout) view.findViewById(R.id.about);
        llAbout.setOnClickListener(this);

        ivAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
        if (userMe.getAvatar() != null) {
            String name = Utils.getFileNameByUrl(userMe.getAvatar().getUrl());
            ivAvatar.setImageBitmap(BitmapFactory.decodeFile(AVATAR_DIR + name));
        }
        ivAvatar.setOnClickListener(this);

        tvNickShow = (TextView) view.findViewById(R.id.nick_show);
        tvNickShow.setText(userMe.getNickName());

        tvUserNameShow = (TextView) view.findViewById(R.id.username_show);
        tvUserNameShow.setText("账号：" + userMe.getUsername());

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_logout:
                /**
                 * 1.清除登录用户
                 * 2.结束所有Activity
                 * 3.打开登录Activity
                 */
                BmobUser.logOut(getActivity());
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);

                getActivity().finish();

                break;

            case R.id.change_nickname:
                etNick = new EditText(getContext());
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("编辑昵称");
                builder.setView(etNick);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String nick_new = etNick.getText().toString();
                        if (nick_new.equals("") || nick_new.equals(userMe.getNickName()) || nick_new == null) {
                            return;
                        }
                        userMe.setValue("nickName", nick_new);
                        userMe.update(getContext(), new UpdateListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getContext(), "昵称修改成功", Toast.LENGTH_SHORT).show();
                                tvNickShow.setText(nick_new);
                            }

                            @Override
                            public void onFailure(int i, String s) {
                                Toast.makeText(getContext(), "昵称修改失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.show();
                break;

            case R.id.about:
                new AlertDialog.Builder(getContext()).setTitle("关于作者")
                        .setMessage("您使用时有什么不爽，欢迎来吐槽。\n邮箱：xiaochenking@qq.com")
                        .setNegativeButton("确定", null)
                        .show();
                break;

            case R.id.check_update:
                //设置更新检测结果监听器
                BmobUpdateAgent.setUpdateListener(new BmobUpdateListener() {
                    @Override
                    public void onUpdateReturned(int i, UpdateResponse updateResponse) {
                        if (i == UpdateStatus.No) {
                            Toast.makeText(getContext(),"当前已是最新版本",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                BmobUpdateAgent.update(getContext());
                break;

            case R.id.iv_avatar:
                showAvatarDialog();
                break;

            case R.id.btn_take_pic:
                takePhoto();
                dismissDialog();
                break;

            case R.id.btn_choose_album:
                getAlbumn();
                dismissDialog();
                break;

            case R.id.btn_choose_cancel:
                dismissDialog();
                break;

            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (resultCode == getActivity().RESULT_OK) {
                    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(getContext(), "SD不可用", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(imageUri, "image/*");
                    intent.putExtra("scale", true);
                    intent.putExtra("aspectX",1);
                    intent.putExtra("aspectY",1);
                    intent.putExtra("outputX",200);
                    intent.putExtra("outputY",200);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, REQUEST_CROP);
                }
                break;

            case REQUEST_LOCATION:
                if (resultCode == getActivity().RESULT_OK) {
                    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(getContext(), "SD不可用", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(data.getData(), "image/*");
                    intent.putExtra("scale", true);
                    intent.putExtra("scale", true);
                    intent.putExtra("aspectX",1);
                    intent.putExtra("aspectY",1);
                    intent.putExtra("outputX",200);
                    intent.putExtra("outputY",200);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, REQUEST_CROP);
                }
                break;
            case REQUEST_CROP:
                if (resultCode == getActivity().RESULT_OK) {
                    final Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath());
                    ivAvatar.setImageBitmap(bitmap);
                    final BmobFile bmobFile = new BmobFile(new File(imageUri.getPath()));
                    bmobFile.upload(getContext(), new UploadFileListener() {
                        @Override
                        public void onSuccess() {
                            Log.e(TAG,"文件上传成功");
                            userMe.setValue("avatar",bmobFile);
                            userMe.update(getContext(), new UpdateListener() {
                                @Override
                                public void onSuccess() {
                                    Log.e(TAG,"头像更新成功");
                                    File file = new File(AVATAR_DIR,Utils.getFileNameByUrl(bmobFile.getUrl()));
                                    try {
                                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                                        bitmap.compress(Bitmap.CompressFormat.PNG,80,bos);
                                        bos.flush();
                                        bos.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(int i, String s) {
                                    Log.e(TAG,"头像更新失败："+s);
                                }
                            });
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            Log.e(TAG,"文件上传失败："+s);
                        }
                    });
                }
                break;
        }
    }

    private void dismissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    public void setUserMe(TakeitUser userMe) {
        this.userMe = userMe;
    }

    private void showAvatarDialog() {
        if (mDialog == null) {
            mDialog = new AlertDialog.Builder(getContext()).create();
        }

        View view = LayoutInflater.from(getContext()).inflate(
                R.layout.avatar_choose_dialog, null
        );

        mDialog.show();
        mDialog.setContentView(view);
        mDialog.getWindow().setGravity(Gravity.BOTTOM);

        Button takePic = (Button) view.findViewById(R.id.btn_take_pic);
        Button getAlbum = (Button) view.findViewById(R.id.btn_choose_album);
        Button cancel = (Button) view.findViewById(R.id.btn_choose_cancel);
        takePic.setOnClickListener(this);
        getAlbum.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }

    private void takePhoto() {
        File dir = new File(AVATAR_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, "myAvatar.png");
        imageUri = Uri.fromFile(file);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void getAlbumn() {
        File dir = new File(AVATAR_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, "myAvatar.png");
        imageUri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*"
        );
        startActivityForResult(intent, REQUEST_LOCATION);
    }
}
