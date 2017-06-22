package cn.doumi.camerademp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Base64;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class ImageUtils {
	private static final String TAG = ImageUtils.class.getSimpleName();

	/**图片变为灰色
	 * @param img
	 * @return
	 */
	public static Bitmap convertGreyImg(Bitmap img) {
		int width = img.getWidth();
		int height = img.getHeight();

		int[] pixels = new int[width * height];

		img.getPixels(pixels, 0, width, 0, 0, width, height);
		int alpha = 0xFF << 24;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int grey = pixels[width * i + j];

				int red = ((grey & 0x00FF0000) >> 16);
				int green = ((grey & 0x0000FF00) >> 8);
				int blue = (grey & 0x000000FF);

				grey = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
				grey = alpha | (grey << 16) | (grey << 8) | grey;
				pixels[width * i + j] = grey;
			}
		}
		Bitmap result = Bitmap.createBitmap(width, height, Config.RGB_565);
		result.setPixels(pixels, 0, width, 0, 0, width, height);
		return result;
	}

	/**
	 * 缩放Bitmap
	 * @param bgimage
	 * @param newWidth
	 * @param newHeight
	 * @return
	 */
	public static Bitmap scaleBitmap(Bitmap bgimage, int newWidth, int newHeight) {
		int width = bgimage.getWidth();
		int height = bgimage.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, width, height, matrix, true);
		return bitmap;
	}

	/**质量压缩
	 * @param fromFile 路径
	 * @param toFile
	 * @param quality
	 * @return
	 */
	public static Bitmap compressBitmap(String fromFile, String toFile, int quality) {
		Bitmap bitmap = BitmapFactory.decodeFile(fromFile);
		return compressBitmap(bitmap, toFile, quality);
	}

	/**质量压缩
	 * @param inputstream 输入流
	 * @param toFile
	 * @param quality
	 * @return
	 */
	public static Bitmap compressBitmap(InputStream inputstream, String toFile, int quality) {
		Bitmap bitmap = BitmapFactory.decodeStream(inputstream);
		return compressBitmap(bitmap, toFile, quality);
	}

	/**
	 * 质量压缩的方法
	 * @param bitmap
	 * @param toFile
	 * @param quality
	 * @return
	 */
	public static Bitmap compressBitmap(Bitmap bitmap, String toFile, int quality) {
		Bitmap newBM = null;
		FileOutputStream out = null;
		try {
			File myCaptureFile = new File(toFile);
			if (!myCaptureFile.exists()) {
				File dir = myCaptureFile.getParentFile();
				if (!dir.exists()) {
					//mkdir()：只能创建一级目录，且父目录必须存在，否则无法成功创建一个目录。
					//mkdirs()：可以创建多级目录，父目录不一定存在。
					//必须先创建父目录
					dir.mkdirs();
				}
				myCaptureFile.createNewFile();
			}
			out = new FileOutputStream(myCaptureFile);
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
				out.flush();
				out.close();
			}
			// Release memory resources
			if (!bitmap.isRecycled()) {
				bitmap.recycle();
			}
			if (!bitmap.isRecycled()) {
				bitmap.recycle();
			}
			newBM = BitmapFactory.decodeFile(toFile);
		} catch (Exception e) {
			LogUtils.i(TAG, e.toString());
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				LogUtils.i(TAG, e.toString());
			}
		}
		return newBM;
	}

	/**
	 * 获取一个圆角图片
	 * @param bitmap
	 * @param roundPx
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		// 规避当创建图片失败时，Canvas绘制图片异常问题；直接返回原图片
		if (output == null) {
			return bitmap;
		}
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	/**view转化为bitmap
	 * @param v
	 * @return
	 */
	public static Bitmap getViewBitmap(View v) {

		v.clearFocus();
		v.setPressed(false);

		boolean willNotCache = v.willNotCacheDrawing();
		v.setWillNotCacheDrawing(false);
		int color = v.getDrawingCacheBackgroundColor();
		v.setDrawingCacheBackgroundColor(0);
		if (color != 0) {
			v.destroyDrawingCache();
		}
		v.buildDrawingCache();
		Bitmap cacheBitmap = v.getDrawingCache();
		if (cacheBitmap == null) {
			return null;
		}
		Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
		// Restore the view
		v.destroyDrawingCache();
		v.setWillNotCacheDrawing(willNotCache);
		v.setDrawingCacheBackgroundColor(color);
		return bitmap;
	}

	public static Bitmap decodeBitmap(Context context, File imageFile) {
		return decodeBitmap(context, imageFile, 32);
	}

	public static Bitmap decodeBitmap(Context context, File imageFile, int maxInSampleSize) {
		Bitmap bitmap = null;
		Options options = new Options();
		options.inJustDecodeBounds = false;
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inDither = true;
		options.inTempStorage = new byte[12 * 1024];
		options.inSampleSize = 1;

		// 设置最大缩放比例
		if (maxInSampleSize < 1) {
			maxInSampleSize = 64;
		}

		while (null == bitmap) {
			bitmap = getScaledBitmap(context, imageFile, options);

			options.inSampleSize = options.inSampleSize * 2;

			if (options.inSampleSize > maxInSampleSize * 2) {
				return null;
			}
		}
		return bitmap;
	}

	public static Bitmap getScaledBitmap(Context context, File imageFile, Options options) {
		Bitmap bitmap = null;
		// 获取资源图片
		FileInputStream fileInputStream = null;
		InputStream inputStream = null;
		try {
			fileInputStream = new FileInputStream(imageFile);
			inputStream = new BufferedInputStream(fileInputStream);
			bitmap = BitmapFactory.decodeStream(inputStream, null, options);
		} catch (OutOfMemoryError err) {
			LogUtils.i(TAG, "inSampleSize" + "-----" + options.inSampleSize + "-------" + err);
			return null;
		} catch (FileNotFoundException e) {
			LogUtils.i(TAG, e.getLocalizedMessage());
		} finally {
			closeInputStream(inputStream);
			closeInputStream(fileInputStream);
		}

		return bitmap;
	}

	private static void closeInputStream(InputStream stream) {
		if (null != stream) {
			try {
				stream.close();
			} catch (IOException e) {
				LogUtils.i(TAG, e.getLocalizedMessage());
			}
		}
	}

	/**
	 * bitmap-->byte[]
	 * @return
	 */
	public static byte[] bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	/**
	 * bytes[]->bitmap
	 * 
	 * @param bytes
	 * @return
	 */
	public static Bitmap bytes2Bitmap(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		BitmapFactory bitmapFactory = new BitmapFactory();
		Bitmap bitMap = bitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		return bitMap;
	}

	/**
	 * 保存图片到sd卡 /data/data/com.maizuo.main/files/
	 * 
	 * @param bitmap
	 * @param fileName
	 * @return
	 */
	public boolean saveImage(Context context, Bitmap bitmap, String fileName) {
		boolean bool = false;
		BufferedOutputStream bos = null;
		BufferedInputStream bis = null;
		ByteArrayOutputStream baos = null;
		try {
			// if (FREE_SD_SPACE_NEEDED_TO_CACHE > freeSpaceOnSd()) {
			// Logger.w(TAG, "Low free space onsd, do not cache");
			// return false;
			// }
			bos = new BufferedOutputStream(context.openFileOutput(fileName, 0));

			baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
			bis = new BufferedInputStream(new ByteArrayInputStream(baos.toByteArray()));
			int b = -1;
			while ((b = bis.read()) != -1) {
				bos.write(b);
			}
			bool = true;
		} catch (Exception e) {
			bool = false;
			//itheima-debugLogger.i(TAG, "the local storage is not available");

		} finally {
			try {
				if (bos != null) {
					bos.close();
				}
				if (bis != null) {
					bis.close();
				}
			} catch (IOException e) {
				bool = false;
				//itheima-debugLogger.i(TAG, "the local storage is not available");

			}
		}
		return bool;
	}

	/**
	 * 删掉图片
	 * 
	 * @param fileName
	 * @return
	 */
	public boolean deleteImage(Context context, String fileName) {
		return context.deleteFile(fileName);
	}

	/**
	 * 把bitmap转换成String
	 * 
	 * @param bm
	 * @return
	 */
	public static String bitmapToString(Bitmap bm) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 40, baos);
		byte[] b = baos.toByteArray();

		return Base64.encodeToString(b, Base64.DEFAULT);

	}


	/**
	 * 通过降低图片的质量来压缩图片
	 *
	 * @param bmp
	 *            要压缩的图片
	 * @param maxSize
	 *            压缩后图片大小的最大值,单位KB
	 * @return 压缩后的图片
	 */
	public static Bitmap compressByQuality(Bitmap bitmap, int maxSize) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int quality = 100;
		bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
		System.out.println("图片压缩前大小：" + baos.toByteArray().length + "byte");
		while (baos.toByteArray().length / 1024 > maxSize) {
			quality -= 10;
			baos.reset();
			bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
			System.out.println("质量压缩到原来的" + quality + "%时大小为："
					+ baos.toByteArray().length + "byte");
		}
		System.out.println("图片压缩后大小：" + baos.toByteArray().length + "byte");
		bitmap = BitmapFactory.decodeByteArray(baos.toByteArray(), 0,
				baos.toByteArray().length);
		return bitmap;
	}

	/**
	 * 算法一：图片长与目标长比，图片宽与目标宽比，取最大值
	 * （不过也有人取最小值，怕压缩的太多吗？取最小值会遇到的问题举个例子，
	 * 满屏加载长截图的时候，图片宽与屏幕宽为1：1，这样inSampleSize就为1，
	 * 没有压缩那么很容易就内存溢出了），
	 * 不管怎么都欠妥；因为如果手机是横屏拍摄，
	 * 或者是拍摄的全景图，那么图片宽与目标宽的比例会很增大，这样压缩的比例会偏大。
	 *
	 * @param pathName
	 *            图片的完整路径
	 * @param targetWidth
	 *            缩放的目标宽度
	 * @param targetHeight
	 *            缩放的目标高度
	 * @return 缩放后的图片
	 */
	public static Bitmap compressBySize(String pathName, int targetWidth,
										int targetHeight) {
		//inSampleSize只能是2的次方，如计算结果是7会按4进行压缩，计算结果是15会按8进行压缩。
		Options opts = new Options();
		opts.inJustDecodeBounds = true;// 不去真的解析图片，只是获取图片的头部信息，包含宽高等；
		Bitmap bitmap = BitmapFactory.decodeFile(pathName, opts);
		// 得到图片的宽度、高度；
		int imgWidth = opts.outWidth;
		int imgHeight = opts.outHeight;
		// 分别计算图片宽度、高度与目标宽度、高度的比例；取大于等于该比例的最小整数；
		int widthRatio = (int) Math.ceil(imgWidth / (float) targetWidth);
		int heightRatio = (int) Math.ceil(imgHeight / (float) targetHeight);
		if (widthRatio > 1 || widthRatio > 1) {
			if (widthRatio > heightRatio) {
				opts.inSampleSize = widthRatio;
			} else {
				opts.inSampleSize = heightRatio;
			}
		}
		// 设置好缩放比例后，加载图片进内容；
		opts.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(pathName, opts);
		return bitmap;
	}

	/**取目标长宽的最大值来计算，这样会减少过度的尺寸压缩，
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int compressBySize(Options options, int reqWidth, int reqHeight) {
		final int width = options.outWidth;
		final int height = options.outHeight;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			//使用需要的宽高的最大值来计算比率
			final int suitedValue = reqHeight > reqWidth ? reqHeight : reqWidth;
			final int heightRatio = Math.round((float) height / (float) suitedValue);
			final int widthRatio = Math.round((float) width / (float) suitedValue);

			inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;//用最大
		}

		return inSampleSize;
	}

	/**
	 * 通过压缩图片的尺寸来压缩图片大小
	 *
	 * @param bitmap
	 *            要压缩图片
	 * @param targetWidth
	 *            缩放的目标宽度
	 * @param targetHeight
	 *            缩放的目标高度
	 * @return 缩放后的图片
	 */
	public static Bitmap compressBySize(Bitmap bitmap, int targetWidth,
										int targetHeight) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		bitmap = BitmapFactory.decodeByteArray(baos.toByteArray(), 0,
				baos.toByteArray().length, opts);
		// 得到图片的宽度、高度；
		int imgWidth = opts.outWidth;
		int imgHeight = opts.outHeight;
		// 分别计算图片宽度、高度与目标宽度、高度的比例；取大于该比例的最小整数；
		int widthRatio = (int) Math.ceil(imgWidth / (float) targetWidth);
		int heightRatio = (int) Math.ceil(imgHeight / (float) targetHeight);
		if (widthRatio > 1 && widthRatio > 1) {
			if (widthRatio > heightRatio) {
				opts.inSampleSize = widthRatio;
			} else {
				opts.inSampleSize = heightRatio;
			}
		}
		// 设置好缩放比例后，加载图片进内存；
		opts.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeByteArray(baos.toByteArray(), 0,
				baos.toByteArray().length, opts);
		return bitmap;
	}

	/**
	 * 通过压缩图片的尺寸来压缩图片大小，通过读入流的方式，可以有效防止网络图片数据流形成位图对象时内存过大的问题；
	 *
	 * @param InputStream
	 *            要压缩图片，以流的形式传入
	 * @param targetWidth
	 *            缩放的目标宽度
	 * @param targetHeight
	 *            缩放的目标高度
	 * @return 缩放后的图片
	 * @throws IOException
	 *             读输入流的时候发生异常
	 */
	public static Bitmap compressBySize(InputStream is, int targetWidth,
										int targetHeight) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		int len = 0;
		while ((len = is.read(buff)) != -1) {
			baos.write(buff, 0, len);
		}

		byte[] data = baos.toByteArray();
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
		// 得到图片的宽度、高度；
		int imgWidth = opts.outWidth;
		int imgHeight = opts.outHeight;
		// 分别计算图片宽度、高度与目标宽度、高度的比例；取大于该比例的最小整数；
		int widthRatio = (int) Math.ceil(imgWidth / (float) targetWidth);
		int heightRatio = (int) Math.ceil(imgHeight / (float) targetHeight);
		if (widthRatio > 1 && widthRatio > 1) {
			if (widthRatio > heightRatio) {
				opts.inSampleSize = widthRatio;
			} else {
				opts.inSampleSize = heightRatio;
			}
		}
		// 设置好缩放比例后，加载图片进内存；
		opts.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
		return bitmap;
	}

	/**
	 * 获取图片角度
	 * obtain the image rotation angle
	 *
	 * @param path path of target image
	 */
	public static int getImageSpinAngle(String path) {
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

	/**
	 * 旋转图片
	 * rotate the image with specified angle
	 *
	 * @param angle  the angle will be rotating 旋转的角度
	 * @param bitmap target image               目标图片
	 */
	public static Bitmap rotatingImage(int angle, Bitmap bitmap) {
		//rotate image
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);

		//create a new image
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	}


	/*
     * 剪切图片
     */
	public static void crop(Activity context, Uri uri, int PHOTO_REQUEST_CUT) {
		// 裁剪图片意图
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		// 裁剪框的比例，1：1
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// 裁剪后输出图片的尺寸大小
		intent.putExtra("outputX", 250);
		intent.putExtra("outputY", 250);

		intent.putExtra("outputFormat", "JPEG");// 图片格式
		intent.putExtra("noFaceDetection", true);// 取消人脸识别
		intent.putExtra("return-data", true);
		// 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
		context.startActivityForResult(intent, PHOTO_REQUEST_CUT);
	}

}
