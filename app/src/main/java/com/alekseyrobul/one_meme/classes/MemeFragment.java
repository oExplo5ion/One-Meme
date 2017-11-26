package com.alekseyrobul.one_meme.classes;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.alekseyrobul.one_meme.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

interface MemeFragmentDelegate{
    void textViewHasFocus();
    void textViewDoesNotHaveFocus();
}

public class MemeFragment extends Fragment{

    public static String ONE_MEME_PICTURE_DIRECTORY_PATH = new File(Environment.
            getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "One Meme").toString();
    private int shadowColor = Color.BLACK;
    private int shadowWidth = 0;
    private EditText topText;
    private EditText bottomText;
    View superView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        superView = inflater.inflate(R.layout.meme_fragment, container, false);
        topText = (EditText) superView.findViewById(R.id.topText);
        topText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Activity holder = getActivity();
                MemeFragmentDelegate delegate = (MainContainerActivity) holder;
                if (hasFocus){
                    delegate.textViewHasFocus();
                }else {
                    delegate.textViewDoesNotHaveFocus();
                }
            }
        });
        topText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkText(topText);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        bottomText = (EditText) superView.findViewById(R.id.bottomText);
        bottomText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Activity holder = getActivity();
                MemeFragmentDelegate delegate = (MainContainerActivity) holder;
                if (hasFocus){
                    delegate.textViewHasFocus();
                }else {
                    delegate.textViewDoesNotHaveFocus();
                }
            }
        });
        bottomText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkText(bottomText);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        final Button pickImageBtn = (Button) superView.findViewById(R.id.pickImageBtn);
        pickImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
        return  superView;
    }

    public void cleartext() {
        topText.setText("");
        bottomText.setText("");
        topText.clearFocus();
        bottomText.clearFocus();
        setTextBorderClear();
    }

    public void saveImageToGallery() {
        if (isMemeValid()) {
            Uri imgUri = getScreenShot();
            File oneMemePicFolder = new File(ONE_MEME_PICTURE_DIRECTORY_PATH);
            if (!oneMemePicFolder.exists()){
                oneMemePicFolder.mkdir();
            }

            //create file and send media scanner intent
            new File(oneMemePicFolder,imgUri.toString());
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(imgUri);
            getActivity().sendBroadcast(intent);
            showToast();

        }else {
            showInvalidMemeDialog();
        }
    }

    private void showToast(){

        Toast toast = Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.imageSaved), Toast.LENGTH_SHORT);
        toast.show();

    }

    public void shareImage() {
        if (isMemeValid()) {
            Uri imgUri = getScreenShot();
            if (imgUri != null) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("image/jpeg");
                sendIntent.putExtra(Intent.EXTRA_STREAM, imgUri);
                startActivity(Intent.createChooser(sendIntent, null));
            }
        }else {
            showInvalidMemeDialog();
        }
    }

    public void pickImage(){

        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, getResources().getString(R.string.selectImage));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        final int PICK_IMAGE = 0;
        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    public void setTextColor(int c) {

        topText.setTextColor(c);
        updateEditTextShadow(topText, shadowWidth);

        bottomText.setTextColor(c);
        updateEditTextShadow(bottomText, shadowWidth);

    }

    public void setPitrure(Bitmap b) {
        ImageView imageMeme = (ImageView) superView.findViewById(R.id.memeImage);
        imageMeme.setImageBitmap(b);
    }

    public boolean isMemeValid() {
        if (!topText.getText().toString().equals("") || !bottomText.getText().toString().equals("")){
            return true;
        }else {
            return false;
        }
    }

    public void showInvalidMemeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getString(R.string.fillInTextArea));
        builder.setNeutralButton(getResources().getString(R.string.okay), null);
        builder.show();
    }


    public void setTextBorderWidth(int w) {
        shadowWidth = w;
        updateEditTextShadow(topText,w);
        updateEditTextShadow(bottomText,w);
    }

    public void setTextBorderClear() {
        updateEditTextShadow(topText,0);
        updateEditTextShadow(bottomText,0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                ContentResolver applicationContext = getActivity().getApplicationContext().getContentResolver();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(applicationContext, uri);
                ImageView imageView = (ImageView) superView.findViewById(R.id.memeImage);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private Uri getScreenShot(){

        // prepeare ui
        topText.clearFocus();
        bottomText.clearFocus();

        if (topText.getText().toString().equals("")){
            topText.setVisibility(View.INVISIBLE);
        }else if (bottomText.getText().toString().equals("")){
            bottomText.setVisibility(View.INVISIBLE);
        }

        Uri imgUri = null;

        // create screenshot
        superView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(superView.getDrawingCache());
        superView.setDrawingCacheEnabled(false);

        topText.setVisibility(View.VISIBLE);
        bottomText.setVisibility(View.VISIBLE);

        // save screenshot to a local storage
        final String path = ONE_MEME_PICTURE_DIRECTORY_PATH;
        File dir = new File(path);
        if (!dir.exists()){
            dir.mkdir();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File image = new File(path, "OneMeme" + imageFileName + ".jpg");
        try {
            FileOutputStream out = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
            out.flush();
            out.close();
            imgUri = Uri.fromFile(image);
            return imgUri;
        } catch (Throwable e){
            e.printStackTrace();
        }

        return null;
    }

    private void checkText(EditText t){
        if (t.getLayout().getLineCount() >= 4){
            String text = t.getText().toString().substring(0, t.getText().toString().length() - 1);
            text.toUpperCase();
            t.setText(text);
        }
    }

    private void updateEditTextShadow(EditText e, int c){
        int toptextColor = topText.getCurrentTextColor();
        if (toptextColor == Color.WHITE && shadowColor == Color.WHITE){
            shadowColor = Color.BLACK;
        }else if (toptextColor == Color.BLACK && shadowColor == Color.BLACK){
            shadowColor = Color.WHITE;
        }
        e.setShadowLayer(c,0,0,shadowColor);
    }

}
