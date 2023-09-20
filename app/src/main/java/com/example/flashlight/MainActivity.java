package com.example.flashlight;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    boolean flashStatus = false;                          //to check status of flashlight
    boolean hasCameraFlash = false;                       //to check flashlight is available or not
    String[] permissions = new String[]{Manifest.permission.POST_NOTIFICATIONS};            //string store which permission to check from manifest
    boolean permission_post_notification = false;          //to check notification permission is given or not
    private final ActivityResultLauncher<String> requestPermissionLauncherNotification = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {    // requesting to launch notification permission
        if (isGranted) {                                   //here checking notification permission granted or not
            permission_post_notification = true;           //performing task after permission granted
        } else {
            permission_post_notification = false;
            showPermissionDialog("Notification permission");   //permission not granted asking for dialog to move to setting or exit
        }
    });
    private TextView txt1;                                  //declared a textview variable
    private LinearLayout linear1;                           //declared a LinearLayout variable

    private CameraManager cameraManager;                    //declared a camera manager variable will we use for flashlight check available or not

    private String cameraId;                               //declared a string variable for camera Id
    private final int NOTIFICATION_ID=001;                  //declared a final integer variable as our app notification id

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);               //set layout here mine -> main_layout
        txt1 = findViewById(R.id.txt1);                     //matching declared variable with our textview id
        linear1 = findViewById(R.id.linear1);               //matching declared variable with our linear layout id
        hasCameraFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);              //getting info and storing flashlight available or not
        if (hasCameraFlash) {                               //checking flashlight available or not to perform task
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);        //initiating camera manager to use flashlight
            try {
                cameraId = cameraManager.getCameraIdList()[0];                               //camera id set to front camera -> front camera=0  rear camera =1
            } catch (CameraAccessException e) {
                e.printStackTrace();                                                         //catching error if any here
            }
            linear1.setOnClickListener(new View.OnClickListener() {                          //set linear1 to perform clicking
                @Override
                public void onClick(View v) {
                    if (!flashStatus) {                                                      //checking flashlight status on or off
                        statusON();                                                          //her flashlight is off so flashlight on  -> statusOn function called to turn on flashlight
                    } else {
                        statusOFF();                                                         //her flashlight is off so flashlight on  -> statusOn function called to turn on flashlight
                    }
                }
            });
        } else {                                                                            //performing task when flashlight not available
            linear1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "Flashlight Not Found", Toast.LENGTH_SHORT).show();    //showing message no flashlight found
                }
            });
        }
    }

    public void statusON() {                                                                //statusOn function created to switch on flashlight with effect on UI
        try {
            txt1.setText("ON");                                                             //setting text to turn ON
            linear1.setBackgroundColor(getResources().getColor(R.color.green));             //setting linear layout background color to green
            flashStatus = true;                                                             //setting flashlight status when flashlight is turned on
            cameraManager.setTorchMode(cameraId, true);                             //turning on flashlight
            createNotification();                                                           //for android version over Oreo we need to create a channel to create a notification so a notification created function called
            NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "00100");                   //creating notification
            builder.setSmallIcon(R.drawable.pic);                                           //set small icon in notification
            builder.setContentTitle("Flashlight");                                          //set title in notification
            builder.setContentText("Flashlight is ON, Don't forget to turn OFF");           //set description in notification
            builder.setPriority(NotificationCompat.PRIORITY_MAX);                           //set priority of notification available HIGH,LOW,MEDIUM,MAX,MIN
            builder.setAutoCancel(false);                                                   //when user click on notification, notification will be gone or not
            builder.setOngoing(true);                                                       //set notification sticky

            NotificationManagerCompat nmc = NotificationManagerCompat.from(MainActivity.this);         //setting context
            nmc.notify(1, builder.build());                                             //show notification
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    public void statusOFF() {                                                                // vice-versa of statusON function
        try {
            txt1.setText("OFF");
            linear1.setBackgroundColor(getResources().getColor(R.color.red));
            flashStatus = false;
            cameraManager.setTorchMode(cameraId, false);
            removeNotification(this,NOTIFICATION_ID);   
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {                                                               //performing task when an activity starts showing
        super.onStart();
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();                                                    //removing action bar
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {              //checking android version to ask permission in Api 34 it is mandatory to ask permission
            if (!permission_post_notification) {
                requestPermissionNotification();                                             //requesting procedure to get permission
            } else {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void requestPermissionNotification() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {    //checking permission granted or not
            permission_post_notification = true;                                             //permission granted so setting permission status to true
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            requestPermissionLauncherNotification.launch(permissions[0]);                                                           //asking to launch permission request
        }
    }

    public void showPermissionDialog(String permission_desc) {                  // created a dialog to ask to move to setting page or exit
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Alert")
                .setIcon(R.drawable.img2)
                .setMessage("Allow notification in settings ")
                .setPositiveButton(" Settings ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent rintent = new Intent();
                        rintent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        rintent.setData(uri);
                        startActivity(rintent);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(" Exit ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                    }
                })
                .show();
    }

    private void createNotification() {                                                      //created notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "FlashLight";
            String description = "Flashlight-Dev. N.S.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel("00100", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void removeNotification(Context context,int notificationID) {
        NotificationManager nMgr = (NotificationManager) context.getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(notificationID);                                                         //remove notification with notification id
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeNotification(this,NOTIFICATION_ID);                                    //function called to remove notification
    }
}
