package com.alekseyrobul.one_meme.classes;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.alekseyrobul.one_meme.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.IOException;

public class MainContainerActivity extends AppCompatActivity implements MemeFragmentDelegate,SettingsFragmentDelegate{

    MemeFragment memeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_container);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-7225805841617141~5102747918");

        memeFragment = (MemeFragment)getSupportFragmentManager().findFragmentById(R.id.memeFragment);

        Button clearBtn = (Button) findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAd();
                cleartext();
            }
        });

        Button shareButton = (Button) findViewById(R.id.shareBtn);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAd();
                imageAction();
            }
        });

        Button settingsBtn = (Button) findViewById(R.id.settingsBtn);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAd();
                settingsBtnAction();
            }
        });

        Button ovrlBtn = (Button) findViewById(R.id.main_ovrl_btn);
        ovrlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsView();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setNavigationBarColor(ContextCompat.getColor(this.getBaseContext(), R.color.colorPrimary));
        }

        // receive image intent
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {

                Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (imageUri != null) {
                    try {
                        memeFragment.setPitrure(MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

    }

    private void cleartext(){
        memeFragment.cleartext();
        SettingsFragment settingsView = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.settingsFragment);
        settingsView.setDefaults();
    }

    private void settingsBtnAction(){
        showSettingsFragment();
    }

    private void showSettingsFragment(){

        View superView = findViewById(R.id.activity_main_container);
        superView.setClickable(false);

        // move optionsView down
        View optionsView = findViewById(R.id.optionsFrameLayout);
        moveViewDown(optionsView);

        // move settingsFrame up
        View settingsView = findViewById(R.id.settingsFrame);
        moveViewUp(settingsView);

        setOverlayBtnEnabled();
        superView.setClickable(true);

    }

    private void showOptionsView(){

        View superView = findViewById(R.id.activity_main_container);
        superView.setClickable(false);

        // move settingsFrame down
        View settingsView = findViewById(R.id.settingsFrame);
        moveViewDown(settingsView);

        // move settingsFrame up
        View optionsView = findViewById(R.id.optionsFrameLayout);
        moveViewUp(optionsView);

        setOverlayBtnDisabled();
        superView.setClickable(true);

    }

    private void moveViewUp(View v){
        v.animate().translationYBy(-v.getHeight()).setDuration(200);
        v.clearAnimation();
    }

    private void moveViewDown(View v){
        v.animate().translationYBy(v.getHeight()).setDuration(200);
        v.clearAnimation();
    }

    private void imageAction(){
        if (memeFragment.isMemeValid()) {
            // check permission
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            RequestCodes.MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
                }else {
                    showImageDialog();
                }
            } else {
                showImageDialog();
            }
        }else {
            memeFragment.showInvalidMemeDialog();
        }
    }

    private void showPermissionDeniedAlertDialog(String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.warning));
        builder.setMessage(message);
        builder.setPositiveButton(getResources().getString(R.string.grandPermission), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", getPackageName(), null));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        builder.show();

    }

    private void showImageDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.chooseOption));
        builder.setMessage(getResources().getString(R.string.memeOption));
        builder.setNeutralButton(getResources().getString(R.string.cancel),null);
        builder.setPositiveButton(getResources().getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                memeFragment.saveImageToGallery();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.share), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                memeFragment.shareImage();
            }
        });
        builder.show();

    }

    private void showAd(){
        View ad = findViewById(R.id.adView);
        ad.setVisibility(View.VISIBLE);
    }

    private void hideAd(){
        View ad = findViewById(R.id.adView);
        ad.setVisibility(View.INVISIBLE);
    }

    private void setOverlayBtnEnabled(){

        Button ovrlBtn = (Button) findViewById(R.id.main_ovrl_btn);
        ovrlBtn.setEnabled(true);
        ovrlBtn.setVisibility(View.VISIBLE);

    }

    private void setOverlayBtnDisabled(){
        Button ovrlBtn = (Button) findViewById(R.id.main_ovrl_btn);
        ovrlBtn.setEnabled(false);
        ovrlBtn.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case RequestCodes.MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE:{
                if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                    showPermissionDeniedAlertDialog(getResources().getString(R.string.permissionDeniedSave));
                }else{
                    showImageDialog();
                }
                break;
            }
            case RequestCodes.MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE_FROM_FRAGMENT:{
                if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                    showPermissionDeniedAlertDialog(getResources().getString(R.string.permissionDeniedCamera));
                }else {
                    SettingsFragment settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.settingsFragment);
                    settingsFragment.startCamera();
                }
                break;
            }
        }
    }

    @Override
    public void seekBarHasChangedPosition(int p) {
        if (p > 0){
            memeFragment.setTextBorderWidth(p);
        }else {
            memeFragment.setTextBorderClear();
        }
    }

    @Override
    public void chooseImgBtnTapped() {
        memeFragment.pickImage();
        showOptionsView();
    }

    @Override
    public void settingsCancelTapped() {
        showOptionsView();
    }

    @Override
    public void setTextColor(int c) {
        memeFragment.setTextColor(c);
    }

    @Override
    public void imageCaptured(Bitmap b) {
        memeFragment.setPitrure(b);
    }

    @Override
    public void textViewHasFocus() {
        hideAd();
    }

    @Override
    public void textViewDoesNotHaveFocus() {
        showAd();
    }
}
