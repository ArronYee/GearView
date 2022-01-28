package com.arronyee.gearview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;


import androidx.annotation.DrawableRes;

import java.util.ArrayList;
import java.util.List;


public class GearView extends androidx.appcompat.widget.AppCompatImageView {

    public static final int GEARVIEW_POS_GROUP = 0;
    public static final int GEARVIEW_POS_AUDIO = 1;
    public static final int GEARVIEW_POS_PRETTY = 2;
    public static final int GEARVIEW_POS_REWARD = 3;
    public static final int GEARVIEW_POS_AD = 4;
    public static final int GEARVIEW_POS_VIDEO = 5;

    public interface GearViewClickListener {
        void onClick(int tag);
    }

    private GearViewClickListener mGearViewClickListener;

    /**
     * 整个View的宽度
     */
    private int viewWidth = 0;
    /**
     * 整个View的高度
     */
    private int viewHeight = 0;
    /**
     * 外圈小圆的半径
     */
    private int smallCircleRadius = 0;
    /**
     * 中间大圆的半径
     */
    private int middleCircleRadius = 0;
    /**
     * 中间大圆到旁边小圆的两圆心直线距离
     */
    private int twoCircleCenterDistance = 0;
    /**
     * 由中间大圆圆心穿过小圆圆心到小圆外圈的距离
     */
    private int bigCircleRadius = 0;
    /**
     * 中间大圆与外圈小圆之间的偏移量，如果为负，则两圆会重合
     */
    private int drawOffset = 0;
    /**
     * 绘制以及动画区域与view的边距
     */
    private int padding = 0;
    /**
     * 每个小圆圆心与大圆圆心之间形成的角度(360/5)
     */
    private static final int middleAngleWith360 = 72;
    /**
     * 一个画圆，一个写字
     */
    private Paint defaultPaint;
    /**
     * 圆心的坐标
     */
    private float[] middleCircleCenter;
    private float[] smallCircleCenter0;
    private float[] smallCircleCenter72;
    private float[] smallCircleCenter144;
    private float[] smallCircleCenter216;
    private float[] smallCircleCenter288;
    /**
     * 每个圆所占的矩形区域
     */
    private Rect middleCircleRect, smallCircleRect0, smallCircleRect72, smallCircleRect144, smallCircleRect216, smallCircleRect288;
    /**
     * 圆的信息集合
     */
    private List<CircleInfo> circleInfoList;
    /**
     * 当前自转的角度
     */
    private float privateRotateRadius = 0;

    /**
     * 上一次旋转角度
     */
    public float lastPublicRotateRadius = 0;
    /**
     * 当前公转的角度
     */
    private float publicRotateRadius = 0;
    /**
     * 手指触摸时添加的误差偏移量
     */
    private int touchOffset = 0;
    /**
     * 上一个触摸的圆
     */
    private CircleInfo currentTouchCircle;

    private CircleInfo touchDownCircle;
    /**
     * 上一次触摸的坐标
     */
    private float touchX, touchY;

    private Bitmap bitmapMiddle, bitmap0, bitmap72, bitmap144, bitmap216, bitmap288;
    private Bitmap bitmapWholeBg;

    private Rect wholeRect;

    private float scale;
//    /**
//     * 当前是否是公转动画
//     */
//    private boolean isPublicRotate = false;

    private int defaultRotate = 360;

    private class CircleInfo {
        float[] circleCenter;
        Rect circleRect;
        int circleRadius;
        String text;
        Bitmap bitmap;

        public CircleInfo(float[] circleCenter, Rect circleRect, int circleRadius, String text, Bitmap bitmap) {
            this.circleCenter = circleCenter;
            this.circleRect = circleRect;
            this.circleRadius = circleRadius;
            this.text = text;
            this.bitmap = bitmap;
        }
    }

    public GearView(Context context) {
        super(context);
//        init();
    }

    public GearView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        init();
    }

    public GearView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        init();
    }

    public GearViewClickListener getGearViewClickListener() {
        return mGearViewClickListener;
    }

    public void setGearViewClickListener(GearViewClickListener mGearViewClickListener) {
        this.mGearViewClickListener = mGearViewClickListener;
    }

    Handler autoHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (defaultRotate > 0) {
                defaultRotate = defaultRotate - 2;
                publicRotateRadius = 360 - defaultRotate;
                publicAnim();
                sendEmptyMessageDelayed(0, 10);
            }
        }
    };

    public void beginAutoRotate() {
//        for (int i = 0; i < 360; i++) {
//            publicRotateRadius = i;
//            publicAnim();
//        }
        autoHandler.sendEmptyMessageDelayed(0, 5);
    }

    //    /**
//     * 正方形
//     *
//     * @param l
//     * @param t
//     * @param r
//     * @param b
//     */
    @Override
    public void layout(int l, int t, int r, int b) {
        Log.d("layout", "gearview l " + l + " t" + t + " r" + r + " b" + b);
        int width = r - l;
        int height = b - t;
        if (height > width) {
            t = t + (height / 2 - width / 2);
            b = b - (height / 2 - width / 2);
        } else {
            l = l + (width / 2 - height / 2);
            r = r - (width / 2 - height / 2);
        }

        if (b - t != viewWidth || viewHeight == 0) {
            viewWidth = b - t;
            init();
        }
//        Log.d("layout", "l " + l + " t" + t + " r" + r + " b" + b);
        super.layout(l, t, r, b);
    }


    /**
     * 效果图 750X1334
     * 大圆半径157，小圆半径106
     * 大圆小圆重合42，圆心距216
     * 小圓圖片比126:104
     * 选择的是xhdpi文件夹图片
     */
    private void init() {
//        viewWidth = getMeasuredWidth();//高度
//        viewHeight = getMeasuredHeight();
        Log.d("test", "viewWidth" + getWidth() + "getMewidth" + getMeasuredWidth() + " getMeHeight" + getMeasuredHeight());
//        viewWidth = viewWidth == 0 ? viewHeight : viewWidth;
        viewHeight = viewWidth;
        scale = viewWidth / 685f;//
        Log.d("paint", "scale is " + scale);
        padding = 0;//整个View距离边缘的距离的1/2
        drawOffset = (int) (-scale * 50);//大圆小圆之间的间距
        touchOffset = 25;//触摸圆圈边缘的偏移量
//        smallCircleRadius = (viewWidth / 2 - drawOffset - padding * 2) / 4;
        smallCircleRadius = (int) (scale * 100);
        middleCircleRadius = (int) (scale * 157);
//        twoCircleCenterDistance = middleCircleRadius + smallCircleRadius + drawOffset;
        twoCircleCenterDistance = (int) (scale * 216);
        bigCircleRadius = twoCircleCenterDistance + smallCircleRadius;//公转大圆，以view中心为圆点
        Log.d("test", "test this is GearView the screenWidth is " + viewWidth + " the smallCircleRadius is" + smallCircleRadius);

//        updateCircleData();

        defaultPaint = new Paint();
        defaultPaint.setStrokeWidth(2);
        defaultPaint.setColor(getResources().getColor(android.R.color.black));
        defaultPaint.setAntiAlias(true);
        defaultPaint.setStyle(Paint.Style.STROKE);

//        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
//        textPaint.setColor(getResources().getColor(android.R.color.black));
//        textPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.text_paint));
//        textPaint.setStyle(Paint.Style.FILL);
//        textPaint.setAntiAlias(true);

        initBitmap();
        wholeRect = new Rect();
        wholeRect.set(padding, 0 + padding, viewWidth - padding, viewWidth - padding);

    }

    private void initBitmap() {
//        bitmapMiddle = getBitmap(R.mipmap.bg_circle_group, middleCircleRadius * 2, 1);
//        bitmap0 = getBitmap(R.mipmap.bg_circle_audio, smallCircleRadius * 2, 126f / 104f);
//        bitmap72 = getBitmap(R.mipmap.bg_circle_pretty, smallCircleRadius * 2, 126f / 104f);
//        bitmap144 = getBitmap(R.mipmap.bg_circle_lottery, smallCircleRadius * 2, 126f / 104f);
//        bitmap216 = getBitmap(R.mipmap.bg_circle_ad, smallCircleRadius * 2, 126f / 104f);
//        bitmap288 = getBitmap(R.mipmap.bg_circle_video, smallCircleRadius * 2, 126f / 104f);

        bitmapMiddle = fromText("middle", 126, 104);
        bitmap0 = fromText("one", 126, 104);
        bitmap72 = fromText("two", 126, 104);
        bitmap144 = fromText("three", 126, 104);
        bitmap216 = fromText("four", 126, 104);
        bitmap288 = fromText("five", 126, 104);

        bitmapWholeBg = getBitmap(R.mipmap.bg_whole_circle, viewWidth - padding * 2, 1);
    }

    public static Bitmap fromText(String text,int width,int height) {
        Paint paint = new Paint();
        paint.setTextSize(22);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.BLACK);

        Paint.FontMetricsInt fm = paint.getFontMetricsInt();

        int textWidth = (int)paint.measureText(text);
        int textHeight = fm.descent - fm.ascent;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(text, (width-textWidth)/2, height/2, paint);
        canvas.save();

        return bitmap;
    }

    /**
     * 生成bitmap
     *
     * @param origin
     * @param width  指定bitmap正方形大小
     * @return
     */
    private Bitmap getBitmap(@DrawableRes int origin, int width, float ratio) {
        if (origin == -1) {
            return null;
        }
//        Log.d("bitmap", "width " + width);
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeResource(getResources(), origin, options);
//        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeResource(getResources(), origin);
//        return Bitmap.createScaledBitmap(src, width, (int) (width / ratio), false);
        return src;
    }

    private void updateCircleData() {

        if (circleInfoList == null || circleInfoList.size() == 0) {

            middleCircleCenter = new float[2];
            smallCircleCenter0 = new float[2];
            smallCircleCenter72 = new float[2];
            smallCircleCenter144 = new float[2];
            smallCircleCenter216 = new float[2];
            smallCircleCenter288 = new float[2];

            middleCircleRect = new Rect();
            smallCircleRect0 = new Rect();
            smallCircleRect72 = new Rect();
            smallCircleRect144 = new Rect();
            smallCircleRect216 = new Rect();
            smallCircleRect288 = new Rect();

            circleInfoList = new ArrayList<>();
            CircleInfo middleCircleInfo = new CircleInfo(middleCircleCenter, middleCircleRect, middleCircleRadius, "0", bitmapMiddle);
            CircleInfo smallCircleInfo0 = new CircleInfo(smallCircleCenter0, smallCircleRect0, smallCircleRadius, "1", bitmap0);
            CircleInfo smallCircleInfo72 = new CircleInfo(smallCircleCenter72, smallCircleRect72, smallCircleRadius, "2", bitmap72);
            CircleInfo smallCircleInfo144 = new CircleInfo(smallCircleCenter144, smallCircleRect144, smallCircleRadius, "3", bitmap144);
            CircleInfo smallCircleInfo216 = new CircleInfo(smallCircleCenter216, smallCircleRect216, smallCircleRadius, "4", bitmap216);
            CircleInfo smallCircleInfo288 = new CircleInfo(smallCircleCenter288, smallCircleRect288, smallCircleRadius, "5", bitmap288);

            circleInfoList.add(middleCircleInfo);
            circleInfoList.add(smallCircleInfo0);
            circleInfoList.add(smallCircleInfo72);
            circleInfoList.add(smallCircleInfo144);
            circleInfoList.add(smallCircleInfo216);
            circleInfoList.add(smallCircleInfo288);

        }
        middleCircleCenter[0] = viewWidth / 2;
        middleCircleCenter[1] = viewHeight / 2;

        smallCircleCenter0[0] = (float) (middleCircleCenter[0] + Math.sin(publicRotateRadius * Math.PI / 180) * twoCircleCenterDistance);
        smallCircleCenter0[1] = (float) (middleCircleCenter[1] - Math.cos(publicRotateRadius * Math.PI / 180) * twoCircleCenterDistance);

        smallCircleCenter72[0] = (float) (Math.sin((middleAngleWith360 + publicRotateRadius) * Math.PI / 180) * twoCircleCenterDistance + middleCircleCenter[0]);
        smallCircleCenter72[1] = (float) (middleCircleCenter[1] - Math.cos((middleAngleWith360 + publicRotateRadius) * Math.PI / 180) * twoCircleCenterDistance);

        smallCircleCenter144[0] = (float) (Math.cos((middleAngleWith360 * 2 + publicRotateRadius - 90) * Math.PI / 180) * twoCircleCenterDistance + middleCircleCenter[0]);
        smallCircleCenter144[1] = (float) (middleCircleCenter[1] + Math.sin(((middleAngleWith360 * 2 + publicRotateRadius) - 90) * Math.PI / 180) * twoCircleCenterDistance);

        smallCircleCenter216[0] = (float) (middleCircleCenter[0] - Math.sin((middleAngleWith360 * 3 + publicRotateRadius - 180) * Math.PI / 180) * twoCircleCenterDistance);
        smallCircleCenter216[1] = (float) (middleCircleCenter[1] + Math.cos((middleAngleWith360 * 3 + publicRotateRadius - 180) * Math.PI / 180) * twoCircleCenterDistance);

        smallCircleCenter288[0] = (float) (middleCircleCenter[0] - Math.sin((middleAngleWith360 - publicRotateRadius) * Math.PI / 180) * twoCircleCenterDistance);
        smallCircleCenter288[1] = (float) (middleCircleCenter[1] - Math.cos((middleAngleWith360 - publicRotateRadius) * Math.PI / 180) * twoCircleCenterDistance);

        Log.d("test", "publicRotateRadius " + publicRotateRadius);
        Log.d("test", "smallCircleCenter0 " + middleCircleCenter[0] + "  " + middleCircleCenter[1]);
        Log.d("test", "smallCircleCenter72 " + smallCircleCenter72[0] + "  " + smallCircleCenter72[1]);
        Log.d("test", "smallCircleCenter144 " + smallCircleCenter144[0] + "  " + smallCircleCenter144[1]);
        Log.d("test", "smallCircleCenter216 " + smallCircleCenter216[0] + "  " + smallCircleCenter216[1]);
        Log.d("test", "smallCircleCenter288 " + smallCircleCenter288[0] + "  " + smallCircleCenter288[1]);

//        middleCircleRect.set((int) (middleCircleCenter[0] - middleCircleRadius), (int) (middleCircleCenter[1] - middleCircleRadius), (int) (middleCircleCenter[0] + middleCircleRadius), (int) (middleCircleCenter[1] + middleCircleRadius));
//        smallCircleRect0.set((int) (smallCircleCenter0[0] - smallCircleRadius / 2), (int) (smallCircleCenter0[1] - smallCircleRadius * 104 / 126 / 2), (int) (smallCircleCenter0[0] + smallCircleRadius / 2), (int) (smallCircleCenter0[1] + smallCircleRadius * 104 / 126 / 2));
//        smallCircleRect72.set((int) (smallCircleCenter72[0] - smallCircleRadius / 2), (int) (smallCircleCenter72[1] - smallCircleRadius * 104 / 126 / 2), (int) (smallCircleCenter72[0] + smallCircleRadius / 2), (int) (smallCircleCenter72[1] + smallCircleRadius * 104 / 126 / 2));
//        smallCircleRect144.set((int) (smallCircleCenter144[0] - smallCircleRadius / 2), (int) (smallCircleCenter144[1] - smallCircleRadius * 104 / 126 / 2), (int) (smallCircleCenter144[0] + smallCircleRadius / 2), (int) (smallCircleCenter144[1] + smallCircleRadius * 104 / 126 / 2));
//        smallCircleRect216.set((int) (smallCircleCenter216[0] - smallCircleRadius / 2), (int) (smallCircleCenter216[1] - smallCircleRadius * 104 / 126 / 2), (int) (smallCircleCenter216[0] + smallCircleRadius / 2), (int) (smallCircleCenter216[1] + smallCircleRadius * 104 / 126 / 2));
//        smallCircleRect288.set((int) (smallCircleCenter288[0] - smallCircleRadius / 2), (int) (smallCircleCenter288[1] - smallCircleRadius * 104 / 126 / 2), (int) (smallCircleCenter288[0] + smallCircleRadius / 2), (int) (smallCircleCenter288[1] + smallCircleRadius * 104 / 126 / 2));

        middleCircleRect.set((int) (middleCircleCenter[0] - 110 * scale), (int) (middleCircleCenter[1] - 110 * scale), (int) (middleCircleCenter[0] + 110 * scale), (int) (middleCircleCenter[1] + 110 * scale));
        smallCircleRect0.set((int) (smallCircleCenter0[0] - 62 * scale), (int) (smallCircleCenter0[1] - 52 * scale), (int) (smallCircleCenter0[0] + 62 * scale), (int) (smallCircleCenter0[1] + 52 * scale));
        smallCircleRect72.set((int) (smallCircleCenter72[0] - 62 * scale), (int) (smallCircleCenter72[1] - 52 * scale), (int) (smallCircleCenter72[0] + 62 * scale), (int) (smallCircleCenter72[1] + 52 * scale));
        smallCircleRect144.set((int) (smallCircleCenter144[0] - 62 * scale), (int) (smallCircleCenter144[1] - 52 * scale), (int) (smallCircleCenter144[0] + 62 * scale), (int) (smallCircleCenter144[1] + 52 * scale));
        smallCircleRect216.set((int) (smallCircleCenter216[0] - 62 * scale), (int) (smallCircleCenter216[1] - 52 * scale), (int) (smallCircleCenter216[0] + 62 * scale), (int) (smallCircleCenter216[1] + 52 * scale));
        smallCircleRect288.set((int) (smallCircleCenter288[0] - 62 * scale), (int) (smallCircleCenter288[1] - 52 * scale), (int) (smallCircleCenter288[0] + 62 * scale), (int) (smallCircleCenter288[1] + 52 * scale));

        Log.d("test", "rect " + middleCircleRect.toString());
        Log.d("test", "rect " + smallCircleRect0.toString());
        Log.d("test", "rect " + smallCircleRect72.toString());
        Log.d("test", "rect " + smallCircleRect144.toString());
        Log.d("test", "rect " + smallCircleRect216.toString());
        Log.d("test", "rect " + smallCircleRect288.toString());

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        updateCircleData();
        canvas.save();
        canvas.rotate(publicRotateRadius, circleInfoList.get(0).circleCenter[0], circleInfoList.get(0).circleCenter[1]);
        canvas.drawBitmap(bitmapWholeBg, null, wholeRect, null);
        canvas.restore();
        drawMiddleCircle(canvas);
        drawSmallCircle(canvas);
        if (Math.abs(publicRotateRadius - lastPublicRotateRadius) > 72) {
            Log.d("uoload", "publicRotateRadius" + publicRotateRadius);
            Log.d("uoload", "lastPublicRotateRadius" + lastPublicRotateRadius);
            play();
            lastPublicRotateRadius = publicRotateRadius;
        }
    }


    public static final int HANDLER_SPEED = 1000;
    public static final int CHECK_TIME = 10;

    private float speed = 0;

    private Handler ratioHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (Math.abs(speed) > 0.1f) {
                if (onTouch) {
                    speed = speed / 2;
                } else {
                    speed *= 0.9f;
//                    if (Math.abs(speed) > 20) {
//                        speed = speed * 4 / 5;
//                    } else {
//                        if (speed > 0) {
//                            speed--;
//                        } else {
//                            speed++;
//                        }
//                    }
                }
                publicAnim();
                ratioHandler.sendEmptyMessageDelayed(HANDLER_SPEED, CHECK_TIME);
            } else {
                speed = 0;
            }
            if (speed != 0) {
                Log.d("speed", "speed is " + speed);
            }
//            ratioHandler.sendEmptyMessageDelayed(HANDLER_SPEED, CHECK_TIME);
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ratioHandler.sendEmptyMessage(HANDLER_SPEED);
        initAudio();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        defaultRotate = 0;
        ratioHandler.removeMessages(HANDLER_SPEED);
        mSoundPlayer.release();
    }

    private boolean onTouch = false;

    public SoundPool mSoundPlayer = new SoundPool(5,
            AudioManager.STREAM_MUSIC, 0);
    // 上下文

    /**
     * 初始化
     */
    public void initAudio() {

        // 初始化声音

        mSoundPlayer.load(getContext(), R.raw.tock, 1);// 1
        mSoundPlayer.load(getContext(), R.raw.tock, 2);// 1
        mSoundPlayer.load(getContext(), R.raw.tock, 3);// 1
        mSoundPlayer.load(getContext(), R.raw.tock, 4);// 1
        mSoundPlayer.load(getContext(), R.raw.tock, 5);// 1
    }

    public long playTime = 0;
    public int index = 1;

    /**
     * 播放声音
     */
    public void play() {
//        if (currentTime - playTime > 1000) {
        mSoundPlayer.play((index++) % 5 + 1, 1, 1, 0, 0, 1);
//            playTime = currentTime;
//        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (middleCircleCenter == null) {
            return false;
        }
        toPublicRotateGesture(x, y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouch = true;
                touchDownCircle = getCircleIndex(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (touchDownCircle != null && getCircleIndex(x, y) == touchDownCircle && speed == 0) {
                    if (mGearViewClickListener != null) {
                        mGearViewClickListener.onClick(circleInfoList.indexOf(touchDownCircle));
                    }
                    touchDownCircle = null;
                    Log.d("onTouch", "this is onTouch tag is " + circleInfoList.indexOf(touchDownCircle));
                }
                onTouch = false;
                resetTouch();
                break;
            default:
                break;
        }
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                CircleInfo circleInfo = getPrivateGestureCircle(x, y, true);
//                if (circleInfo == null) {
//                    //表示当前手指没有触摸到任何自转圆圈
//                    //该情况下只判断是否为公转手势
//                    isPublicRotate = toPublicRotateGesture(x, y);
//                } else {
//                    //该情况下为自转手势
//                    isPublicRotate = false;
//                    //自转手势判断开始
//                    toPrivateRotateGesture(x, y, circleInfo);
//                }
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if (isPublicRotate) {
//                    toPublicRotateGesture(x, y);
//                } else {
//                    toPrivateRotateGesture(x, y, getPrivateGestureCircle(x, y, false));
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//                if (isPublicRotate) {
//                    toPublicRotateGesture(x, y);
//                } else {
//                    toPrivateRotateGesture(x, y, getPrivateGestureCircle(x, y, false));
//                }
//                resetTouch();
//                break;
//            default:
//                break;
//        }
        return true;
    }

    /**
     * 公转的手势判断
     *
     * @param x
     * @param y
     * @return
     */
    private boolean toPublicRotateGesture(float x, float y) {
        //算出改点到大圆心的距离
        float dis = distance(x, y, middleCircleCenter[0], middleCircleCenter[1]);
        //判断是否为有效触摸,大于中心圆半径，小与大圆半径
        if (dis >= middleCircleRadius * 2 / 3 && dis <= bigCircleRadius + touchOffset) {
            //是公转
            boolean clockwise = true;//时针方向
            //属于公转触摸
            if (touchX != 0 && touchY != 0) {
                //已經有过公转触摸了
                int radius = (int) distance(x, y, touchX, touchY);
                clockwise = getClockWise(touchX, touchY, x, y, circleInfoList.get(0));
                //通過公轉偏移量獲取公轉旋轉角度
//                publicAnim(clockwise ? radius : -radius);
                speed = speed + (clockwise ? radius : -radius) / 2;
                if (!ratioHandler.hasMessages(HANDLER_SPEED)) {
                    ratioHandler.sendEmptyMessageDelayed(HANDLER_SPEED, CHECK_TIME);
                }
            }

            touchX = x;
            touchY = y;
            return true;
        } else {
            touchX = 0;
            touchY = 0;
            return false;
            //不是公轉的有效觸摸
        }
    }

    /**
     * 自转的手势判断
     *
     * @param x
     * @param y
     * @param circleInfo
     */
    private void toPrivateRotateGesture(float x, float y, CircleInfo circleInfo) {
        boolean clockwise = true;//时针方向
        //之前没有有效触摸或不是同一个圆
        if (currentTouchCircle == null || currentTouchCircle != circleInfo) {
            currentTouchCircle = circleInfo;
        } else {
            float distance = distance(x, y, touchX, touchY);
//            int radius = (int) (distance / 5);
            clockwise = getClockWise(touchX, touchY, x, y, circleInfo);
            int radius = (int) distance;
            Log.d("test", "touchX " + touchX + " touchY" + touchY + " x" + x + " y" + y);
            Log.d("test", "test clockwise is " + clockwise);
            Log.d("radius", "test distance is " + distance);
            if (circleInfo == circleInfoList.get(0)) {
                //如果触摸的是大圓
                privateAnim(clockwise ? radius : -radius);
            } else {
                // 小圆的顺时针就是大圆的逆时针
                privateAnim(clockwise ? -radius : radius);
            }
        }
        touchX = x;
        touchY = y;
    }

    private CircleInfo getCircleIndex(float x, float y) {
        for (int i = 1; i < circleInfoList.size(); i++) {
            CircleInfo circleInfo = circleInfoList.get(i);
            float distance = distance(x, y, circleInfo.circleCenter[0], circleInfo.circleCenter[1]);
            Log.d("test", "test this is getNearlyCircle " + distance + " circleInfo.circleRadius " + circleInfo.circleRadius);
            if (distance <= circleInfo.circleRadius + touchOffset) {
                return circleInfo;
            }
        }
        //再判断大圆
        float distance = distance(x, y, circleInfoList.get(0).circleCenter[0], circleInfoList.get(0).circleCenter[1]);
        Log.d("test", "test this is getNearlyCircle " + distance + " circleInfo.circleRadius " + circleInfoList.get(0).circleRadius);
        if (distance <= circleInfoList.get(0).circleRadius + touchOffset) {
            return circleInfoList.get(0);
        }
        return null;
    }

    /**
     * 根据自转判断是否有符合条件的小圆
     *
     * @param x
     * @param y
     * @param isStrict 是否是严格控制触碰位置
     * @return
     */
    private CircleInfo getPrivateGestureCircle(float x, float y, boolean isStrict) {
        //首先判断是否已经有已滑过的圆
        if (currentTouchCircle != null) {
            float distance = distance(x, y, currentTouchCircle.circleCenter[0], currentTouchCircle.circleCenter[1]);
            if (distance <= currentTouchCircle.circleRadius + touchOffset && distance > currentTouchCircle.circleRadius * (isStrict ? 2 : 1) / 3) {
                return currentTouchCircle;
            } else {
                //如果超出已滑过的圆就reset
                resetTouch();
            }
        }
        for (int i = 1; i < circleInfoList.size(); i++) {
            CircleInfo circleInfo = circleInfoList.get(i);
            float distance = distance(x, y, circleInfo.circleCenter[0], circleInfo.circleCenter[1]);
            Log.d("test", "test this is getNearlyCircle " + distance + " circleInfo.circleRadius " + circleInfo.circleRadius);
            if (distance <= circleInfo.circleRadius + touchOffset && distance > circleInfo.circleRadius * (isStrict ? 2 : 1) / 3) {
                return circleInfo;
            }
        }
        //再判断大圆
        float distance = distance(x, y, circleInfoList.get(0).circleCenter[0], circleInfoList.get(0).circleCenter[1]);
        Log.d("test", "test this is getNearlyCircle " + distance + " circleInfo.circleRadius " + circleInfoList.get(0).circleRadius);
        if (distance <= circleInfoList.get(0).circleRadius + touchOffset && distance > circleInfoList.get(0).circleRadius * (isStrict ? 2 : 1) / 3) {
            return circleInfoList.get(0);
        }
        return null;
    }

    /**
     * 根据手势获取当前旋转的方向
     *
     * @param touchX
     * @param touchY
     * @param x
     * @param y
     * @param circleInfo
     * @return
     */
    private boolean getClockWise(float touchX, float touchY, float x, float y, CircleInfo circleInfo) {
        float dis_x = Math.abs(touchX - x);
        float dis_y = Math.abs(touchY - y);
        if (dis_x > dis_y) {// 用户主要是在x轴上的手势操作根据y坐标判断属于圆的上半部分还是下半部分
            float middle_y = (touchY + y) / 2;
            if (middle_y <= circleInfo.circleCenter[1]) {
                //上,
                return x >= touchX;
            } else {
                return x <= touchX;
            }
        } else {// 用户主要是在y轴上的手势操作根据x坐标判断属于圆的上半部分还是下半部分
            float middle_x = (touchX + x) / 2;
            if (middle_x <= circleInfo.circleCenter[0]) {
                //左邊,
                return y <= touchY;
            } else {
                //右邊
                return y >= touchY;
            }
        }
    }

    /**
     * 重置保存的手势信息
     */
    private void resetTouch() {
        currentTouchCircle = null;
        touchX = 0;
        touchY = 0;
    }


    /**
     * 绘画中间的圆
     *
     * @param canvas
     */
    private void drawMiddleCircle(Canvas canvas) {
//        canvas.drawCircle(middleCircleCenter[0], middleCircleCenter[1], middleCircleRadius, defaultPaint);
        canvas.drawBitmap(circleInfoList.get(0).bitmap, null, circleInfoList.get(0).circleRect, null);
//        canvas.save();
//        canvas.rotate(privateRotateRadius, circleInfoList.get(0).circleCenter[0], circleInfoList.get(0).circleCenter[1]);
//        drawCircleText(canvas, circleInfoList.get(0));
//        canvas.restore();
    }

    /**
     * 绘画周边的小圆
     *
     * @param canvas
     */
    private void drawSmallCircle(Canvas canvas) {
        for (int i = 1; i < circleInfoList.size(); i++) {
            CircleInfo circleInfo = circleInfoList.get(i);
//            canvas.drawCircle(circleInfo.circleCenter[0], circleInfo.circleCenter[1], circleInfo.circleRadius, defaultPaint);
            canvas.drawBitmap(circleInfo.bitmap, null, circleInfo.circleRect, null);
//            canvas.save();
//            canvas.rotate(-privateRotateRadius, circleInfo.circleCenter[0], circleInfo.circleCenter[1]);
//            drawCircleText(canvas, circleInfo);
//            canvas.restore();

        }
    }

    /**
     * 画圆当中的字
     *
     * @param canvas
     * @param circleInfo
     */
    private void drawCircleText(Canvas canvas, CircleInfo circleInfo) {
//        float fontWidth = textPaint.measureText("0");
//        canvas.drawText(circleInfo.text, circleInfo.circleCenter[0] - fontWidth / 2, circleInfo.circleCenter[1] + fontWidth / 2, textPaint);
    }

    /**
     * 自转动画
     * 动画两种 中间顺时针，其他小盘逆时针
     * 中间逆时针，其他小盘顺时针
     */
    private void privateAnim(int radius) {
        Log.d("anim", "privateAnim radius is " + radius);
        privateRotateRadius += radius;
//        publicRotateRadius -= radius;
        invalidate();
    }

    /**
     * 公转动画，主要是小圆围绕大圆转动，同时小圆自身也转动
     *
     * @param radius
     */
    private void publicAnim(float radius) {
        Log.d("publicAnim", "publicAnim radius is " + radius);
        radius = radius / 4;//公转速度太快
        privateRotateRadius += radius;//公转同时也带自转角度
        publicRotateRadius += radius;
        invalidate();
    }

    private void publicAnim() {
        Log.d("publicAnim", "publicAnim radius is ");
        privateRotateRadius += speed;//公转同时也带自转角度
        publicRotateRadius += speed;
        invalidate();
    }

    /**
     * 計算兩點距離
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static float distance(float x1, float y1, float x2, float y2) {
        Log.d("test ", "" + Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)));
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

}
