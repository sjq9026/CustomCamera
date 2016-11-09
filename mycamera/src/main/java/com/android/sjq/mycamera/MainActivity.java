package com.android.sjq.mycamera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
ImageView photo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        photo = (ImageView)findViewById(R.id.photo);
        Button start_camera = (Button) findViewById(R.id.start_my_camear);
        start_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CustomCamera2.class);
                //startActivity(intent,0);
                startActivityForResult(intent,0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("TAG", "onActivityResult: ");
        if(resultCode == RESULT_OK){
           String path = data.getStringExtra("path");
            Log.i("TAG", path);
            Bitmap bitmap = BitmapFactory.decodeFile(path);
//            Matrix matrix = new Matrix();
//            matrix.setRotate(90);
//            bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
            photo.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i("TAG", "onNewIntent: ");

    }
}
