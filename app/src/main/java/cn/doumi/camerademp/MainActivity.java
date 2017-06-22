package cn.doumi.camerademp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

import cn.doumi.camerademp.utils.CameraUtil;
import cn.doumi.camerademp.utils.FileUtil;

public class MainActivity extends AppCompatActivity {

    File mTmpFile;

    private ImageView mImageCiv;
    private ImageView mImagePhoto;

    //相机
    private static final int REQUEST_OPENCAMERA = 110;
    //相册
    private static final int REQUEST_PICKALBUM = 111;
    //拍照后裁剪
    private static final int PHOTO_REQUESTCUT = 112;
    //相册选择后裁剪
    private static final int PICK_PHOTO_REQUESTCUT = 113;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageCiv = (ImageView) findViewById(R.id.image_civ);
        mImagePhoto = (ImageView) findViewById(R.id.iamge_photo);
    }

    public void openCamera(View view) {
         /* //拍照裁剪后
                Log.d("BBBBB","拍照裁剪后");
                Bitmap cutTakePhoto = CameraUtil.getCutTakePhoto(this, mTmpFile);
                Log.d("BBBBB","cutTakePhoto===>"+cutTakePhoto.getWidth()+","+cutTakePhoto.getHeight());
                mImageCiv.setImageBitmap(cutTakePhoto);
                mImagePhoto.setImageBitmap(cutTakePhoto);*/
        //打开相机拍照
        mTmpFile = CameraUtil.openCamera(this, REQUEST_OPENCAMERA);
    }

    public void pickPhoto(View view) {

        //选择照片
        CameraUtil.pickPhoto(this, REQUEST_PICKALBUM);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_OPENCAMERA:
                    //拍照回来
                    Log.d("BBBBB", "拍照回来");
                    CameraUtil.crop(this, Uri.fromFile(mTmpFile), PHOTO_REQUESTCUT);
                    break;
                case REQUEST_PICKALBUM:
                    //选择照片回来
                    Log.d("BBBBB", "选择照片回来");
                    CameraUtil.crop(this, Uri.fromFile(CameraUtil.getPickPhoto(this, data)), PICK_PHOTO_REQUESTCUT);
                    break;
                case PHOTO_REQUESTCUT:
                case PICK_PHOTO_REQUESTCUT:
                    //选择照片裁剪后
                    Log.d("BBBBB", "选择照片裁剪后");
                    if (data != null) {
                        Bitmap mBitmap = data.getParcelableExtra("data");
                        mImageCiv.setImageBitmap(mBitmap);
                        mImagePhoto.setImageBitmap(mBitmap);
                        Log.d("BBBBB", "getPickPhoto===>" + mBitmap.getWidth() + "," + mBitmap.getHeight());

                        //这里之后就是将头像上传到服务器
                        try {
                            File file = FileUtil.saveBitmap(this, mBitmap, "temp.png");
                            //然后请求网络上传
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_OPENCAMERA) {
            Log.d("BBBBB", "REQUEST_OPENCAMERA");
            mTmpFile = CameraUtil.openYuanCamera(this, REQUEST_OPENCAMERA);
        } else if (requestCode == REQUEST_PICKALBUM) {
            CameraUtil.openAlbum(this, REQUEST_PICKALBUM);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
