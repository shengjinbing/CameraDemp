package cn.doumi.camerademp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Administrator on 2017/6/21 0021.
 */

public class CameraUtil {

    public static File openCamera(Activity mActivity, int REQUEST_OPENCAMERA) {
        //请求原图
        if (ContextCompat.checkSelfPermission(mActivity, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity, new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_OPENCAMERA);
        } else {
            return openYuanCamera(mActivity, REQUEST_OPENCAMERA);
        }

        return null;

    }

    public static void pickPhoto(Activity mActivity, int REQUEST_PICKALBUM) {
        //相册选择照片
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity, new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_PICKALBUM);
        } else {
            //一种打开相册的方法,建议使用第一种可以获得图片的路径直接
            openAlbum(mActivity, REQUEST_PICKALBUM);
            //另一种
            //pickopenAlbumPhoto(mActivity,REQUEST_PICKALBUM);
        }

    }

    public static void pickopenAlbumPhoto(Activity mActivity, int REQUEST_PICKALBUM) {
        // 激活系统图库，选择一张图片
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_GALLERY
        mActivity.startActivityForResult(intent, REQUEST_PICKALBUM);
    }

    public static void openAlbum(Activity mActivity, int REQUEST_PICKALBUM) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mActivity.startActivityForResult(intent, REQUEST_PICKALBUM);
    }


    /**
     * 获取原图的打开方式
     *
     * @param mActivity
     * @param REQUEST_OPENCAMERA
     */
    public static File openYuanCamera(Activity mActivity, int REQUEST_OPENCAMERA) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File mTmpFile = null;
        if (intent.resolveActivity(mActivity.getPackageManager()) != null) {
            try {
                mTmpFile = FileUtil.createTmpFile(mActivity);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (mTmpFile != null && mTmpFile.exists()) {
                     /*获取当前系统的android版本号*/
                int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                Log.e("BBBBB", "currentapiVersion====>" + currentapiVersion);
                StartCamera(mActivity, REQUEST_OPENCAMERA, intent, mTmpFile, currentapiVersion);
            } else {
                Toast.makeText(mActivity, "mTmpFile为空", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(mActivity, "resolveActivitygetPackageManager为空", Toast.LENGTH_SHORT).show();
        }

        return mTmpFile;
    }

    private static void StartCamera(Activity mActivity, int REQUEST_OPENCAMERA, Intent intent, File mTmpFile, int currentapiVersion) {
        if (currentapiVersion < 24) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
            mActivity.startActivityForResult(intent, REQUEST_OPENCAMERA);
        } else {
            ContentValues contentValues = new ContentValues(1);
            contentValues.put(MediaStore.Images.Media.DATA, mTmpFile.getAbsolutePath());
            Uri uri = mActivity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            mActivity.startActivityForResult(intent, REQUEST_OPENCAMERA);
        }
    }

    /**
     * 获取从相册选择图片的路径
     *
     * @param mActivity
     * @param data
     * @return
     */
    public static File getPickPhoto(Activity mActivity, Intent data) {
        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = mActivity.getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return new File(picturePath);
    }

    /**
     * 图片裁剪后返回bitmap
     * @param mActivity
     * @param mTmpFile
     * @return
     */
    public static Bitmap getCutTakePhoto(Activity mActivity, File mTmpFile) {
        ContentResolver resolver = mActivity.getContentResolver();
        Bitmap mBitmap = null;
        try {
             mBitmap = BitmapFactory.decodeStream(resolver.openInputStream(Uri.fromFile(mTmpFile)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ImageUtils.rotatingImage(ImageUtils.getImageSpinAngle(mTmpFile.getPath()), mBitmap);
    }
    public static Bitmap getCutPickPhoto(Activity mActivity, Intent data) {
        return null;
    }



    /*
     * 剪切图片
     */
    public static void crop(Activity context, Uri uri, int PHOTO_REQUESTCUT) {
        // 裁剪图片意图
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // 裁剪框的比例，1：1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);

        intent.putExtra("outputFormat", "JPEG");// 图片格式
        intent.putExtra("noFaceDetection", true);// 取消人脸识别
        intent.putExtra("return-data", true);
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
        context.startActivityForResult(intent, PHOTO_REQUESTCUT);
    }

}
