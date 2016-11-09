package com.android.sjq.mycamera;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class CustomCamera extends AppCompatActivity {
    private Camera mCamera;
    private SurfaceView mPreview;
    private SurfaceHolder mHolder;
    private String mFilepath = "/mnt/sdcard/AAAAA";
    private int[] sizes = new int[]{300, 200, 100, 90, 80, 70, 60, 50, 40, 30, 20, 10};
    private Camera.PictureCallback mCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i("size", "------------------------------>" + data.length + "");
            File tempFile = new File("/sdcard/AAAAAA");
            if (!tempFile.exists()) {
                tempFile.mkdirs();
            }
            String fileName = "temp.jpg";
            tempFile = new File(tempFile, fileName);
            Log.i("size", "------------------------------>" + data.length + "");
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//            try {
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(tempFile));
//                getSmallBitmap(tempFile.getAbsolutePath());
//                CustomCamera.this.finish();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
               bitmap = zoomImage(bitmap);
            //compressImage(bitmap, 300);
            for (int size : sizes) {
                compress(bitmap,size);
            }
             CustomCamera.this.finish();

        }
    };
    private DisplayMetrics dm;

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

        android.hardware.Camera camera = null;
        camera = android.hardware.Camera.open();
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


    public static Bitmap zoomImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        if (baos.toByteArray().length / 1024 > 1024*2) {// 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
//            baos.reset();// 重置baos即清空baos
//            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);// 这里压缩50%，把压缩后的数据存放到baos中
//        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 1280;// 这里设置高度为800f
        float ww = 720;// 这里设置宽度为480f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        if (isBm != null) {
            try {
                isBm.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (image != null && !image.isRecycled()) {
            image.recycle();
            image = null;
        }
        return bitmap;
    }


    public void compress(Bitmap bitmap, int size) {
        Matrix matrix = new Matrix();
        matrix.setRotate(90);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        System.out.println(baos.toByteArray().length);
        while (baos.toByteArray().length > size * 1024) {
            baos.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            quality -= 2;
        }
        File tempFile = new File("/sdcard/AAAAA");
        if (!tempFile.exists()) {
            tempFile.mkdirs();
        }
        String name = UUID.randomUUID() + ".jpg";
        tempFile = new File(tempFile, name);
        try {
            baos.writeTo(new FileOutputStream(tempFile));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                baos.flush();
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static Bitmap getSmallBitmap(String filePath) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 480, 800);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap bm = BitmapFactory.decodeFile(filePath, options);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.toByteArray().length, options);

        if (bm == null) {
            return null;
        }
        int degree = readPictureDegree(filePath);
        bm = rotateBitmap(bm, 90);
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 30, baos);

        } finally {
            try {
                if (baos != null)
                    baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        writeToSDcard(bm);



        return bm;

    }

    private static void writeToSDcard(Bitmap bm) {
        File file = new File("/sdcard/BBBBBBBBB");
        if (!file.exists()) {
            file.mkdirs();
        }
        String fileName = UUID.randomUUID() + ".jpg";
        file = new File(file, fileName);
        try {
            bm.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
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
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }


}
