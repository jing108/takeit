package com.wt.first.CustomView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.View;

import com.wt.first.R;

import java.io.Serializable;

/**
 * Created by jing107 on 2016/3/30 0030.
 *
 * 根据指定的资源生成一个圆形的头像
 */
public class CircleHead extends View implements Serializable {

//    private static final String TAG = CircleHead.class.getSimpleName();

    private Bitmap mBitmap = null;

    public CircleHead(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleHead);
        /**
         * 这样直接获得BitmapDrawable应该是有问题的，后期修复
         */
        BitmapDrawable drawable = (BitmapDrawable) a.getDrawable(R.styleable.CircleHead_src);
        if (drawable != null) {
            mBitmap = drawable.getBitmap();
        }

        a.recycle();
    }

    /**
     * 主要对xml文件中wrap_content进行指定
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        /**
         * 如果布局中高度和宽度都指定为wrap_content
         * 则将位图的高度和宽度设置为最终测量的长宽
         */
        if (widthSpecMode == MeasureSpec.AT_MOST &&
                heightSpecMode ==MeasureSpec.AT_MOST) {
            if (mBitmap != null) {
                setMeasuredDimension(mBitmap.getWidth(), mBitmap.getHeight());
            } else {
                setMeasuredDimension(200,200);
            }
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            if (mBitmap != null) {
                setMeasuredDimension(mBitmap.getWidth(), heightSpecSize);
            } else {
                setMeasuredDimension(200,heightSpecSize);
            }
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            if (mBitmap != null) {
                setMeasuredDimension(widthSpecSize, mBitmap.getHeight());
            } else {
                setMeasuredDimension(widthSpecSize,200);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //根据控件的长宽，改变位图的大小
        changeBitmap();

        if (mBitmap != null) {
            canvas.drawBitmap(toRoundBitmap(mBitmap), 0, 0, null);
        }
    }

    /**
     * 将一个位图依据xfermode制作成一个圆形的位图
     * @param bitmap
     * @return
     */
    private Bitmap toRoundBitmap(Bitmap bitmap) {
        /**
         * 这里是根据布局大小来确定高和宽度
         */
        int height = getHeight();
        int width = getWidth();
        int r = Math.min(width,height); //选取最短的作为直径
        //dst
        Bitmap background = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas tmpCanvas = new Canvas(background);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        //dst上面是个圆形
        tmpCanvas.drawCircle(width/2,height/2,r/2,paint);
        //设置xfermode为src_in，这样就可以将src中的图像抠出一个圆形图像出来
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        tmpCanvas.drawBitmap(bitmap,0,0,paint);

        return background;
    }

    /**
     * 根据布局大小，动态缩放位图资源
     */
    private void changeBitmap() {
        if (mBitmap != null) {
            int x = getWidth();
            int y = getHeight();
            int bx = mBitmap.getWidth();
            int by = mBitmap.getHeight();
//            Log.e(TAG,"x:"+x+";y:"+y+";bx:"+bx+";by:"+by);

            float fx = (float) x/bx;
            float fy = (float) y/by;
//            Log.e(TAG,"fx:"+fx+";fy:"+fy);

            Matrix matrix = new Matrix();
            matrix.postScale(fx,fy);
            Bitmap tmp = Bitmap.createBitmap(mBitmap,0,0,bx,
                    by,matrix,true);

            mBitmap = tmp;
        }
    }

    /**
     * 可以通过代码中设置资源，id和bitmap两种方式
     */
    public void setResource(@DrawableRes int resId) {
        mBitmap = BitmapFactory.decodeResource(getResources(),resId);
        invalidate();
    }

    public void setResource(Bitmap bitmap) {
        mBitmap = bitmap;
        invalidate();
    }
}
