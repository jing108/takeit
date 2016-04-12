package com.wt.first.Receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wt.first.Bean.TakeitBill;
import com.wt.first.Bean.TakeitUser;
import com.wt.first.R;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.GetListener;

/**
 * Created by jing107 on 2016/3/25 0025.
 */
public class NewBillReceiver extends BroadcastReceiver {

    private static final String TAG = NewBillReceiver.class.getSimpleName();

    private static final int NOTIFICATION_ID = NewBillReceiver.class.hashCode();
    public static String EXTRA_PUSH_MESSAGE_STRING = "msg";
    //为了显示多条notification，必须指定不同的tag
    private static int my_notification_id = NOTIFICATION_ID;

    @Override
    public void onReceive(final Context context, Intent intent) {
        String message = intent.getStringExtra(EXTRA_PUSH_MESSAGE_STRING);

        final String billId;
        String userId;

        try {
            JSONObject jsonObject = new JSONObject(message);
            userId = jsonObject.getString("who");
            billId = jsonObject.getString("billId");

            BmobQuery<TakeitUser> query = new BmobQuery<TakeitUser>();
            query.getObject(context, userId, new GetListener<TakeitUser>() {
                @Override
                public void onSuccess(TakeitUser takeitUser) {
                    final String userName = takeitUser.getUsername();

                    BmobQuery<TakeitBill> q = new BmobQuery<TakeitBill>();
                    q.getObject(context, billId, new GetListener<TakeitBill>() {
                        @Override
                        public void onSuccess(TakeitBill takeitBill) {

                            String money = takeitBill.getMoney().toString();
                            String time = takeitBill.getCreatedAt();
                            String content = takeitBill.getContent();

//                            Intent i = new Intent(context, StartActivity.class);
//                            PendingIntent pendingIntent = PendingIntent.getActivity(context,0,i,0);
                            Notification.Builder builder = new Notification.Builder(context);
                            //必须设置图标，才能显示出来
                            builder.setSmallIcon(R.mipmap.takeit);
                            builder.setDefaults(Notification.DEFAULT_ALL);
                            builder.setAutoCancel(true);

                            builder.setContentTitle(userName+"又来收款了");
                            builder.setContentText("在"+time+"，你们共花费了"+money+"软妹币。");
                            builder.setSubText(content);
//                            builder.setContentIntent(pendingIntent);
                            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            manager.notify(""+my_notification_id++,NOTIFICATION_ID,builder.build());
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            Log.e(TAG,s);
                        }
                    });
                }

                @Override
                public void onFailure(int i, String s) {
                    Log.e(TAG,s);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
