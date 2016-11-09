package com.android.sjq.customcamera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.graphics.BitmapCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {
    private static int REQUEST_CODE = 1;
    private static int REQUEST_CODE_2 = 2;
    private ImageView photo_img;
    private String mFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        photo_img = (ImageView) findViewById(R.id.photo_img);
        mFilePath = Environment.getExternalStorageDirectory().getPath();
        mFilePath = mFilePath + File.separator + "temp.png";

    }


    public void takePhoto(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CODE);
    }

    public void startCamear2(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(mFilePath);
        if(!file.exists()){
            file.mkdirs();
        }
        Uri uri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_CODE_2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                Bundle bundle = data.getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");
                photo_img.setImageBitmap(bitmap);
            }


            if (requestCode == REQUEST_CODE_2) {
                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(mFilePath);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    photo_img.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        inputStream.close();
                    } catch (Exception e) {

                    }

                }
            }
        }
    }
}
