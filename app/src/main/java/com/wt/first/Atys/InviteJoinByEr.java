package com.wt.first.Atys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.google.zxing.WriterException;
import com.wt.first.R;
import com.wt.first.zxing.encoding.EncodingHandler;

/**
 * Created by jing107 on 2016/4/4 0004.
 */
public class InviteJoinByEr extends Activity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saowo);

        Intent intent = getIntent();
        String accountId = intent.getStringExtra("accountId");

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        imageView = (ImageView) findViewById(R.id.iv_yaoqingerweima);
        try {
            imageView.setImageBitmap(EncodingHandler.createQRCode(accountId,metrics.widthPixels));
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
