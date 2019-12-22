package com.example.abubakrsokarno.chaty;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Abubakr Sokarno on 1/9/2018.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//                        .setSmallIcon(R.drawable.defaultavatar)
//                        .setContentTitle("My notification")
//                        .setContentText("Hello World!");

        if (remoteMessage.getData().size()>0)
        {
            String msgTitle = remoteMessage.getNotification().getTitle() ;
            String msgBody = remoteMessage.getNotification().getBody() ;
            String clickAction = remoteMessage.getNotification().getClickAction() ;
            String user_id = remoteMessage.getData().get("from_user_id") ;
            String user_name = remoteMessage.getData().get("from_user_name") ;

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.chaticon)
                    .setContentTitle(msgTitle)
                    .setContentText(msgBody);

            Intent resultIntent = new Intent(clickAction);
            resultIntent.putExtra("userID",user_id);
            resultIntent.putExtra("username",user_name);
            resultIntent.putExtra("click_action",clickAction) ;

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
            Uri alarmSound = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.notification_sound_1) ;
            mBuilder.setSound(alarmSound);

        mBuilder.setContentIntent(resultPendingIntent);

        int mNotificationId = (int) System.currentTimeMillis();
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());


            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this)  ;
            localBroadcastManager.sendBroadcast(resultIntent) ;
        }

    }


}
