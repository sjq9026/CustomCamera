package com.android.sjq.mycamera;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class CustomCamera2 extends AppCompatActivity {
    private Camera mCamera;
    private SurfaceView mPreview;
    private SurfaceHolder mHolder;
    private String mFilepath = "/mnt/sdcard/AAAAA";
    private int[] sizes = new int[]{1024, 900, 800, 700, 600, 500, 400, 300, 200, 100, 90, 80, 70, 60, 50, 40, 30, 20, 10};
    private Camera.PictureCallback mCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i("length", "原图大小---->" + (data.length / 1024 / 1024) + "MB");
            File tempFile = new File("/sdcard/AAAAAA");
            if (!tempFile.exists()) {
                tempFile.mkdirs();
            }
            String fileName = "temp.jpg";
            tempFile = new File(tempFile, fileName);
            Log.i("size", "------------------------------>" + data.length + "");
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            try {
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(tempFile.getAbsolutePath()));
                getSmallBitmap(tempFile.getAbsolutePath());
                CustomCamera2.this.finish();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_camera);
        initView();
    }

    private void initView() {
        mPreview = (SurfaceView) findViewById(R.id.surfaceview);
        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.autoFocus(null);
            }
        });
        mHolder = mPreview.getHolder();
        mHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                setStartPreview(mCamera, mHolder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mCamera.stopPreview();
                setStartPreview(mCamera, mHolder);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                releaseCamera();
            }
        });
    }

    //点击拍照
    public void take_photo(View view) {
        Camera.Parameters param = getParameters();
        mCamera.setParameters(param);
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    mCamera.takePicture(null, null, mCallback);
                }
            }
        });

    }

    /**
     * 设置相机的参数
     *
     * @return
     */
    @NonNull
    private Camera.Parameters getParameters() {
        Camera.Parameters param = mCamera.getParameters();
        param.setPictureFormat(ImageFormat.JPEG);
        //获取摄像头支持的所有尺寸
        List<Camera.Size> sizes = param.getSupportedPictureSizes();
        int pic_width = 0;
        int pic_height = 0;
        int MIN_SIZE = 1280 * 720;
        if (sizes.size() > 1) {
            for (Camera.Size size : sizes) {
                Log.i("size", "width-->" + size.width + "   height-->" + size.height);
                if (size.width > 1280 && size.height > 720) {
                    pic_width = size.width;
                    pic_height = size.height;
                    break;
                }
            }
        } else {
            pic_width = sizes.get(0).width;
            pic_height = sizes.get(0).height;
        }
        //此处设置的尺寸一定要是摄像头支持的尺寸，否则会抛出异常
        param.setPictureSize(pic_width, pic_height);
        Log.i("length", "pic_width" + pic_width + "   pic_height" + pic_height);
        Log.i("TAG", pic_height + "" + pic_width);
        param.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        param.set("jpeg-quality", 85);
        return param;
    }

    //获取一个camera
    private Camera getCamera() {
        PackageManager pm = getPackageManager();
        // FEATURE_CAMERA - 后置相机
        // FEATURE_CAMERA_FRONT - 前置相机
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.i("TAG", "后置相机");
        }
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            Log.i("TAG", "前置相机");
        }

        Camera camera = null;
        camera = Camera.open();
        return camera;

    }

    //开始预览相机内容
    private void setStartPreview(Camera camrea, SurfaceHolder holder) {
        try {
            camrea.setPreviewDisplay(holder);
            //将系统camrea预览角度进行调整
            camrea.setDisplayOrientation(90);
            camrea.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //释放相机资源
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            mCamera = getCamera();
            if (mHolder != null) {
                setStartPreview(mCamera, mHolder);
            }
        }
        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TAG", "mCamera.autoFocus(null)");
                mCamera.autoFocus(null);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    /**
     * 最终缩略图加载过程：
     * 1. 使用inJustDecodeBounds，读bitmap的长和宽。
     * 2. 根据bitmap的长款和目标缩略图的长和宽，计算出inSampleSize的大小。
     * 3. 使用inSampleSize，载入一个大一点的缩略图A
     * 4. 使用createScaseBitmap，将缩略图A，生成我们需要的缩略图B。
     * 5. 回收缩略图A。
     *
     * @param filePath
     */
    public void getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        //将图片的宽高尺寸加载到options中
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        //设置采样率,裁剪图片大小
        options.inSampleSize = calculateInSampleSize(options, 480, 800);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap bm = BitmapFactory.decodeFile(filePath, options);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //质量压缩，不会改变像素数，把bitmap读取到stream里
        bm.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, outputStream);
        BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.toByteArray().length, options);

        if (bm == null) {
            return;
        }
        int degree = readPictureDegree(filePath);
        bm = rotateBitmap(bm, 90);
        int option = 100;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(android.graphics.Bitmap.CompressFormat.JPEG, option, baos);
        for (int size : sizes) {
            while (baos.toByteArray().length > 1024 * size) {
                Log.i("length", (baos.toByteArray().length / 1024) + "kb");
                baos.reset();
                option -= 5;
                bm.compress(Bitmap.CompressFormat.JPEG, option, baos);
                if (option < 10) {
                    break;
                }
            }
//            Bitmap bitmap1 = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length);
//            Bitmap bitmap2 = bitmap1.createScaledBitmap(bitmap1, 400, 800, true);
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, out);
//            writeToSDFrombyte(out);
//            writeToSDcard(bm);
            writeToSDFrombyte(baos);
        }
        try {
            if (baos != null) {
                baos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void writeToSDFrombyte(ByteArrayOutputStream output) {
        //判断SDCARD状态
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //判断剩余空间
            String sdcard = Environment.getExternalStorageDirectory().getParent();
            StatFs mStatfs = new StatFs(sdcard);
            long blockSize = mStatfs.getBlockSize();
            long blocks = mStatfs.getAvailableBlocks();
            long availableSpare = (blocks * blockSize) / (1024 * 1024);
            //小于10M
            if (availableSpare < 10) {
                Toast.makeText(CustomCamera2.this, "内存不足", Toast.LENGTH_SHORT).show();
                return;
            }
            File file = new File("/sdcard/BBBBBBBBB");
            if (!file.exists()) {
                file.mkdirs();
            }
            String fileName = UUID.randomUUID() + ".jpg";
            file = new File(file, fileName);
            try {
                output.writeTo(new FileOutputStream(file.getAbsolutePath()));
            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            //SDCARD状态异常
            Dialog dialog = new AlertDialog.Builder(this)
                    .setMessage("SD卡状态异常")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    }).show();
        }


    }


    private void writeToSDcard(Bitmap bm) {
        File file = new File("/sdcard/BBBBBBBBB");
        if (!file.exists()) {
            file.mkdirs();
        }
        String fileName = UUID.randomUUID() + ".jpg";
        file = new File(file, fileName);
        try {
            bm.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
        }
        Log.i("length", "inSampleSize----->" + inSampleSize);
        return inSampleSize;
    }

    private static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int rotate) {
        if (bitmap == null)
            return null;

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        // Setting post rotate to 90
        //旋转90度
        Matrix mtx = new Matrix();
        mtx.postRotate(rotate);
        return android.graphics.Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }


    /**
     * 照片写上水印
     */
    private Bitmap drawTextToBitmap(Bitmap bm, int flag) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 初始化画布
        Canvas bmCanvas = new Canvas(bm);
        // 设置画笔
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG
                | Paint.DEV_KERN_TEXT_FLAG);
        textPaint.setTextSize(15.0f);// 字体大小
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);// 采用默认的宽度
        textPaint.setColor(Color.RED);// 采用的颜色
        // 当前日期和时间
        String str = getDateAndTime();
        if (flag == 0) {
            bmCanvas.drawText(str, width - 160, height - 40, textPaint);// 绘制上去字，开始未知x,y采用那只笔绘制
        }
        if (flag == 1) {
            bmCanvas.drawText(str, width - 160, height - 40, textPaint);// 绘制上去字，开始未知x,y采用那只笔绘制
        }
        bmCanvas.save(Canvas.ALL_SAVE_FLAG);
        bmCanvas.restore();
        return bm;
    }

    private String getDateAndTime() {
        String strDate = null;
        // 获取当前时间
        // long currentTime=System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        strDate = formatter.format(curDate);
        return strDate;
    }


}
