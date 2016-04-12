package com.wt.first.CustomView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wt.first.R;

/**
 * Created by jing107 on 2016/3/24 0024.
 */
public class RefreshableView extends LinearLayout implements View.OnTouchListener{

    public static final String TAG = RefreshableView.class.getName();
    //下拉状态
    public static final int STATUS_PULL_TO_REFRESH = 0;
    //释放立即刷新状态
    public static final int STATUS_RELEASE_TO_REFRESH = 1;
    //正在刷新状态
    public static final int STATUS_REFRESHING = 2;
    //刷新完成或未刷新状态
    public static final int STATUS_REFRESH_FINISHED = 3;

    //下拉头部回滚的速度
    public static final int SCROLL_SPEED = -20;

    /**
     * 当前处于什么状态，可选值有STATUS_PULL_TO_REFRESH, STATUS_RELEASE_TO_REFRESH,
     * STATUS_REFRESHING 和 STATUS_REFRESH_FINISHED
     */
    private int mCurrentStatus = STATUS_REFRESH_FINISHED;

    /**
     * 记录上一次的状态是什么，避免进行重复操作
     */
    private int mLastStatus = mCurrentStatus;

    //用于存储上次更新时间
    private SharedPreferences mPreferences;

    //下拉头的view
    private View mHeader;

    //刷新时显示的进度条
    private ProgressBar mProgressBar;

    //指示下拉和释放的箭头
    private ImageView mArrow;

    //指示下拉和释放的文字描述
    private TextView mDescription;

    //上次更新时间的文字描述
    private TextView mUpdateAt;

    //在被判定为滚动之前用户手指可以移动的最大值
    private int mTouchSlop;

    //上次更新时间的毫秒值
    private long mLastUpdateTime;

    //上次更新时间的字符串常量，用于作为SharedPrefrences的键值
    private static final String UPDATED_AT = "updated_at";

    //为了防止不同界面的下拉刷新在上次更新时间上互相有冲突，使用id来做区分
    private int mId = -1;

    //一分钟的毫秒值，用于判断上次的更新时间
    private static final long ONE_MINUTE = 60 * 1000;
    //一小时的毫秒值，用于判断上次的更新时间
    private static final long ONE_HOUR = 60* ONE_MINUTE;
    //一天的毫秒值，用于判断上次的更新时间
    private static final long ONE_DAY = 24 * ONE_HOUR;
    //一月的毫秒值，用于判断上次的更新时间
    private static final long ONE_MONTH = 30 * ONE_DAY;
    //一年的毫秒值，用于判断上次的更新时间
    private static final long ONE_YEAR = 12 * ONE_MONTH;

    //是否已加载过一次layout，这里onLayout中的初始化只需加载一次
    private boolean loadOnce;

    //下拉刷新的回调接口
    private PullToRefreshListener mListener;

    /**
    下拉头的高度
     */
    private int hideHeaderHeight;

    //下拉头的布局参数
    private MarginLayoutParams mHeaderLayoutParams;

    //需要去下拉刷新的ListView
    private ListView mlistView;

    //当前是否可以下拉，只有ListView滚动到头的时候才允许下拉
    private boolean mbAbleToPull;

    //手指按下时的屏幕纵坐标
    private float yDown;

    public RefreshableView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        mHeader = LayoutInflater.from(context).inflate(R.layout.pull_to_refresh,null);

        mProgressBar = (ProgressBar) mHeader.findViewById(R.id.progressbar);
        mArrow = (ImageView) mHeader.findViewById(R.id.arrow);
        mDescription = (TextView) mHeader.findViewById(R.id.description);
        mUpdateAt = (TextView) mHeader.findViewById(R.id.updated_at);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        refreshUpdatedAtValue();

        //下拉刷新View中，包含一个ListView和一个下拉刷新头，下拉刷新头是动态添加的
        setOrientation(VERTICAL);
        addView(mHeader);
    }


    /**
     * 进行一些关键性的初始化操作，比如：将下拉头向上偏移进行隐藏，给ListView注册
     * onTouch事件
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (changed && !loadOnce) {
            hideHeaderHeight = -mHeader.getHeight();
            /**
             * 那么问题来了，MarginLayoutParams继承了LayoutParams
             * 新增加了什么东西呢？
             */
            mHeaderLayoutParams = (MarginLayoutParams) mHeader.getLayoutParams();
            mHeaderLayoutParams.topMargin = hideHeaderHeight;
            mHeader.setLayoutParams(mHeaderLayoutParams);

            mlistView = (ListView) getChildAt(1);
            mlistView.setOnTouchListener(this);
            loadOnce = true;
        }
    }

    /**
     * 刷新下拉头中上次更新时间的文字描述
     */
    private void refreshUpdatedAtValue() {
        mLastUpdateTime = mPreferences.getLong(UPDATED_AT + mId,-1);
        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - mLastUpdateTime;
        long timeIntoFormat;
        String updateAtValue;

        if (mLastUpdateTime == -1) { //说明没有更新过
            updateAtValue = getResources().getString(R.string.not_updated_yet);
        } else if (timePassed < 0) {
            updateAtValue = getResources().getString(R.string.time_error);
        } else if (timePassed < ONE_MINUTE) {
            updateAtValue = getResources().getString(R.string.updated_just_now);
        } else if (timePassed < ONE_HOUR) {
            timeIntoFormat = timePassed / ONE_MINUTE;
            String value = timeIntoFormat + "分钟";
            updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
        } else if (timePassed < ONE_DAY) {
            timeIntoFormat = timePassed / ONE_HOUR;
            String value = timeIntoFormat + "小时";
            updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
        } else if (timePassed < ONE_MONTH) {
            timeIntoFormat = timePassed / ONE_DAY;
            String value = timeIntoFormat + "天";
            updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
        } else if (timePassed < ONE_YEAR) {
            timeIntoFormat = timePassed / ONE_MONTH;
            String value = timeIntoFormat + "个月";
            updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
        } else {
            timeIntoFormat = timePassed / ONE_YEAR;
            String value = timeIntoFormat + "年";
            updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
        }

        mUpdateAt.setText(updateAtValue);
    }


    /**
     * 当ListView被触摸时调用，其中处理了各种下拉刷新的逻辑
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        setIsAbleToPull(event);

        //如果可以下拉
        if(mbAbleToPull) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    yDown = event.getRawY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    float yMove = event.getRawY();
                    int distance = (int) (yMove - yDown);
                    //如果手指是下滑状态，并且下拉头是完全隐藏的，就屏蔽下拉事件
                    if (distance < 0 && mHeaderLayoutParams.topMargin <= hideHeaderHeight) {
                        return false;
                    }
                    if (distance < mTouchSlop) {
                        return false;
                    }
                    if (mCurrentStatus != STATUS_REFRESHING) {
                        if (mHeaderLayoutParams.topMargin > 0) {
                            mCurrentStatus = STATUS_RELEASE_TO_REFRESH;
                        } else {
                            mCurrentStatus = STATUS_PULL_TO_REFRESH;
                        }
                        mHeaderLayoutParams.topMargin = (distance / 2) + hideHeaderHeight;
                        mHeader.setLayoutParams(mHeaderLayoutParams);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                default:
                    if (mCurrentStatus == STATUS_RELEASE_TO_REFRESH) {
                        new RefreshingTask().execute();
                    } else if (mCurrentStatus == STATUS_PULL_TO_REFRESH) {
                        new HideHeaderTask().execute();
                    }
                    break;
            }
            //时刻记得更新下拉头中的信息
            if (mCurrentStatus == STATUS_PULL_TO_REFRESH
                || mCurrentStatus == STATUS_RELEASE_TO_REFRESH) {
                updateHeaderView();
                /**当前正处于下拉或释放状态，要让ListView失去焦点，否则被点击的那一项会一直处于选中状态
                 * 此处需要实践才能知道为嘛
                 */
                mlistView.setPressed(false);
                mlistView.setFocusable(false);
                mlistView.setFocusableInTouchMode(false);
                mLastStatus = mCurrentStatus;
                //当前处于下拉或释放状态，通过返回true屏蔽掉ListView的滚动事件
                return true;
            }
        }

        return false;
    }

    /**
     * 给下拉刷新控件注册一个监听器
     * @param listener  监听器的实现
     * @param id  为了防止不同界面的下拉刷新在上次更新时间上互相有冲突，
     *            不同界面在注册下拉刷新监听器的时候需要传入不同的id。
     */
    public void setOnRefreshListener(PullToRefreshListener listener, int id) {
        mListener = listener;
        mId = id;
    }

    /**
     * 当所有的刷新逻辑完成后，记录调用一下，否则ListView将一直处于正在刷新状态
     */
    public void finishRefreshing() {
        mCurrentStatus = STATUS_REFRESH_FINISHED;
        mPreferences.edit().putLong(UPDATED_AT + mId, System.currentTimeMillis()).commit();
        new HideHeaderTask().execute();
    }

    /**
     * 根据当前ListView的滚动状态来设定{@link #mbAbleToPull}
     * 的值，每次都需要在onTouch中第一个执行，这样可以判断出当前
     * 应该是滚动ListView，还是应该进行下拉
     *
     * @param event
     */
    private void setIsAbleToPull(MotionEvent event) {
        /**
         * getChildAt(int index) 这个函数返回的是：从当前可见的item为0开始的索引为index的view
         * 其实getChildAt和getFirstVisiblePosition返回的都是同一个view，只是表示这个view的
         * 数据不一样：getChildAt返回的是该view本身，而getVisiblePosition返回的是该view在适配器中的索引
         *
         */
        View firstChild = mlistView.getChildAt(0);
        if (firstChild != null) {
            int firstVisiblePos = mlistView.getFirstVisiblePosition();

//            Log.e(TAG,"fisrtVisiblePosition:" + firstVisiblePos);
//            Log.e(TAG,"firstChild.getTop() = " + firstChild.getTop());

            if (firstVisiblePos == 0 && firstChild.getTop() == 0) {
                if (!mbAbleToPull) {
                    yDown = event.getRawY();
                }
                mbAbleToPull = true;
            } else {
                if (mHeaderLayoutParams.topMargin != hideHeaderHeight) {
                    mHeaderLayoutParams.topMargin = hideHeaderHeight;
                    mHeader.setLayoutParams(mHeaderLayoutParams);
                }
                mbAbleToPull = false;
            }
        } else {    //如果列表中没有元素，则也可以刷新
            mbAbleToPull = true;
        }
//        Log.e(TAG,"是否可以更新：" + mbAbleToPull);
    }

    /**
     * 正在刷新的任务，在此任务中会去回调注册进来的下拉刷新监听器
     */
    class RefreshingTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            int topMargin = mHeaderLayoutParams.topMargin;
            while (true) {
                topMargin = topMargin + SCROLL_SPEED;
                if (topMargin <= 0) {
                    topMargin = 0;
                    break;
                }
                publishProgress(topMargin);
//                sleep(10);  ?为什么这样就会报错呢，而直接写就不会呢？
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mCurrentStatus = STATUS_REFRESHING;
            publishProgress(0);
            if (mListener != null) {
                mListener.onRefresh();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... topMargin) {
            updateHeaderView();
            mHeaderLayoutParams.topMargin = topMargin[0];
            mHeader.setLayoutParams(mHeaderLayoutParams);
        }
    }

    /**
     * 隐藏下拉头的任务，当未进行下拉刷新或下拉刷新完成后，此任务将会使下拉头重新隐藏
     */
    class HideHeaderTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            int topMargin = mHeaderLayoutParams.topMargin;
            while (true) {
                topMargin = topMargin + SCROLL_SPEED;
                if (topMargin <= hideHeaderHeight) {
                    topMargin = hideHeaderHeight;
                    break;
                }
                publishProgress(topMargin);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return topMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... topMargin) {
            mHeaderLayoutParams.topMargin = topMargin[0];
            mHeader.setLayoutParams(mHeaderLayoutParams);
        }

        @Override
        protected void onPostExecute(Integer topMargin) {
            mHeaderLayoutParams.topMargin = topMargin;
            mHeader.setLayoutParams(mHeaderLayoutParams);
            mCurrentStatus = STATUS_REFRESH_FINISHED;
        }
    }

    /**
     * 更新下拉头中的信息。
     */
    private void updateHeaderView() {
        if (mLastStatus != mCurrentStatus) {
            if (mCurrentStatus == STATUS_PULL_TO_REFRESH) {
                mDescription.setText(getResources().getString(R.string.pull_to_fresh));
                mArrow.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                rotateArrow();
            } else if (mCurrentStatus == STATUS_RELEASE_TO_REFRESH) {
                mDescription.setText(getResources().getString(R.string.release_to_fresh));
                mArrow.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                rotateArrow();
            } else if (mCurrentStatus == STATUS_REFRESHING) {
                mDescription.setText(getResources().getString(R.string.refreshing));
                mProgressBar.setVisibility(View.VISIBLE);
                mArrow.clearAnimation();
                mArrow.setVisibility(View.GONE);
            }
            refreshUpdatedAtValue();
        }
    }

    /**
     * 根据当前的状态来旋转箭头
     */
    private void rotateArrow() {
        float pivotX = mArrow.getWidth() / 2f;
        float pivotY = mArrow.getHeight() / 2f;

        float fromDegrees = 0f;
        float toDegrees = 0f;

        if (mCurrentStatus == STATUS_PULL_TO_REFRESH) {
            fromDegrees = 180f;
            toDegrees = 360f;
        } else if (mCurrentStatus == STATUS_RELEASE_TO_REFRESH) {
            fromDegrees = 0f;
            toDegrees = 180f;
        }
        RotateAnimation animation = new RotateAnimation(fromDegrees,toDegrees,pivotX,pivotY);
        animation.setDuration(100);
        animation.setFillAfter(true);
        mArrow.startAnimation(animation);
    }

    /**
     * 使当前线程睡眠指定的毫秒数
     *
     * @param time 指定当前线程睡眠多久，以毫秒为单位
     */
    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public interface PullToRefreshListener {
        /**
         * 刷新时会去回调此方法，在方法内编写具体的刷新逻辑。
         * 此方法是在子线程中调用的，可以不必另开线程来进行耗时操作
         */
        void onRefresh();
    }
}
