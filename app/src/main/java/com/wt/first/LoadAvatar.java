package com.wt.first;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.wt.first.CustomView.CircleHead;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by jing107 on 2016/4/7 0007.
 */
public class LoadAvatar extends Thread {

    private static final String AVATAR_DIR = "/sdcard/Android/data/com.wt.first/avatar/";
    private static final int LOAD_AVATAR_COMPLETE = 1;

    private CircleHead ch;
    private Handler handler;
    private String fileName;
    private String fileUrl;

    public LoadAvatar(CircleHead ch, String fileName, Handler handler, String fileUrl) {
        this.ch = ch;
        this.handler = handler;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }

    @Override
    public void run() {
        Bitmap bitmap = ImageLoader.getInstance().loadImageSync(fileUrl);
        File file = new File(AVATAR_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(new File(AVATAR_DIR+fileName))
            );
            bitmap.compress(Bitmap.CompressFormat.PNG,80,bos);
            bos.flush();
            bos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putSerializable("CircleHead",ch);
        bundle.putString("fileName",fileName);
        msg.what = LOAD_AVATAR_COMPLETE;
        msg.setData(bundle);
        handler.sendMessage(msg);
    }
}
