package com.alekseyrobul.one_meme.classes;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

import com.alekseyrobul.one_meme.BuildConfig;
import com.alekseyrobul.one_meme.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static com.alekseyrobul.one_meme.classes.RequestCodes.MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE_FROM_FRAGMENT;
import static com.alekseyrobul.one_meme.classes.RequestCodes.REQUEST_IMAGE_CAPTURE;

interface SettingsFragmentDelegate{
    void seekBarHasChangedPosition(int p);
    void setTextColor(int c);
    void imageCaptured(Bitmap b);
    void chooseImgBtnTapped();
    void settingsCancelTapped();
}

public class SettingsFragment extends Fragment {

    static final String TEMP_DIRECTORY = "/.temp/";
    private Uri imageUri;
    View superView;
    SettingsFragmentDelegate mSettingsDelegate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        superView = inflater.inflate(R.layout.settings_fragment, container, false);

        Activity holder = this.getActivity();
        if (holder != null && holder instanceof SettingsFragmentDelegate){
            mSettingsDelegate = (MainContainerActivity) holder;
        }

        SeekBar seekBar = (SeekBar) superView.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSettingsDelegate.seekBarHasChangedPosition(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Button whiteClrBtn = (Button) superView.findViewById(R.id.whiteColorBtn);
        whiteClrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettingsDelegate.setTextColor(Color.WHITE);
            }
        });

        Button blacklrBtn = (Button) superView.findViewById(R.id.blackColorBtn);
        blacklrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettingsDelegate.setTextColor(Color.BLACK);
            }
        });

        Button tPicBtn = (Button) superView.findViewById(R.id.tpicBtn);
        tPicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        Button choosePicBtn = (Button) superView.findViewById(R.id.choosePicBtn);
        choosePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettingsDelegate.chooseImgBtnTapped();
            }
        });

        Button cancelBtn = (Button) superView.findViewById(R.id.cancelSettingsBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsCancel();
            }
        });

        return superView;
    }

    public void setDefaults(){
        SeekBar seekBar = (SeekBar) superView.findViewById(R.id.seekBar);
        seekBar.setProgress(0);
    }

    private void dispatchTakePictureIntent() {

        if (getActivity().getBaseContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE_FROM_FRAGMENT);
                } else {
                    startCamera();
                }
            }else {
                startCamera();
            }

        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getResources().getString(R.string.warning));
            builder.setMessage(getResources().getString(R.string.cameraIsNotSuported));
            builder.setNeutralButton(getResources().getString(R.string.cancel), null);
        }
    }

    private void startCamera(){

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {

            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (imageFile != null){
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    imageUri = Uri.fromFile(imageFile);
                }else {
                    // file provider
                    imageUri= FileProvider.getUriForFile(this.getActivity(),
                            BuildConfig.APPLICATION_ID + ".provider",
                            imageFile);
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE);
            }

        }

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath() + TEMP_DIRECTORY);
        if(!tempDir.exists())
        {
            tempDir.mkdirs();
        }
        return File.createTempFile(imageFileName, ".jpg", tempDir);
    }

    private void settingsCancel(){
        mSettingsDelegate.settingsCancelTapped();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Bitmap bitmap = null;
            try
            {
                InputStream image_stream = getActivity().getContentResolver().openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(image_stream);
            }
            catch (Exception e)
            {
            }
            mSettingsDelegate.imageCaptured(bitmap);
        }
    }
}
