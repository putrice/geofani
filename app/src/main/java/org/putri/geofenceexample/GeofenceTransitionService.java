package org.putri.geofenceexample;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by putri on 1/23/17.
 */

public class GeofenceTransitionService extends IntentService {

    private static final String TAG = GeofenceTransitionService.class.getSimpleName();
    public static final int GEOFENCE_NOTIFICATION_ID = 0;

    public GeofenceTransitionService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if(geofencingEvent.hasError()) {
            String errorMessage = getErrorString(geofencingEvent.getErrorCode());
            return;
        }

        int geoFenceTransition = geofencingEvent.getGeofenceTransition();
        if(geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            String geoFenceTransitionDetails = getGeoFenceTransitionDetails(geoFenceTransition, triggeringGeofences);
            sendNotification(geoFenceTransitionDetails);
        }
    }

    private String getGeoFenceTransitionDetails(int geoFenceTransition, List<Geofence> triggeringGeoFences) {
        ArrayList<String> triggeringGeofencesList = new ArrayList<>();
        for(Geofence geofence : triggeringGeoFences) {
            triggeringGeofencesList.add(geofence.getRequestId());
        }

        String status = null;
        if(geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            status = "entering";
        } else if(geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            status = "exiting";
        }

        return status + TextUtils.join(", " , triggeringGeofencesList);
    }

    private void sendNotification(String message) {
        Intent notificationIntent = MainActivity.makeNotificationIntent(getApplicationContext(), message);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(MainActivity.class);
        taskStackBuilder.addNextIntent(notificationIntent);

        PendingIntent notificationPendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(GEOFENCE_NOTIFICATION_ID, createNotification(message, notificationPendingIntent));
    }

    private Notification createNotification(String message, PendingIntent notificationPendingIntent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setColor(Color.RED)
                .setContentTitle(message)
                .setContentText("Geofani Notification")
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setAutoCancel(true);
        return notificationBuilder.build();
    }

    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "Geofence not available";

            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";

            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many Pending Intents";

            default:
                return "unknown error";
        }
    }

}
