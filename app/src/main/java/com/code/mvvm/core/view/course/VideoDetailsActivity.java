package com.code.mvvm.core.view.course;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.basiclibrary.base.BaseActivity;
import com.bumptech.glide.Glide;
import com.code.mvvm.R;
import com.code.mvvm.core.data.pojo.course.CourseDetailRemVideoVo;
import com.code.mvvm.core.data.pojo.course.CourseDetailVo;
import com.code.mvvm.network.ApiService;
import com.code.mvvm.util.DisplayUtil;
import com.network.HttpHelper;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.trecyclerview.TRecyclerView;
import com.trecyclerview.multitype.Items;
import com.trecyclerview.multitype.MultiTypeAdapter;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.trecyclerview.multitype.MultiTypeAsserts.assertAllRegistered;

/**
 * @author：zhangtianqiu on 18/7/7 15:09
 */
public class VideoDetailsActivity extends BaseActivity {
    StandardGSYVideoPlayer mVideoPlayer;
    OrientationUtils mOrientationUtils;

    protected TRecyclerView mRecyclerView;

    String lessonId;
    private String teacherId;
    private String fCatalogId;
    private String sCatalogId;
    private CourseDetailVo.DataEntity lessonData = null;

    @Override
    protected void onStateRefresh() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_video_details;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        lessonId = getIntent().getStringExtra("course_id");
        mRecyclerView = (TRecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mVideoPlayer = findViewById(R.id.video_player);
        int widthVideo = DisplayUtil.getScreenWidth(this);
        int heightVideo = widthVideo * 9 / 16;
        mVideoPlayer.getLayoutParams().width = widthVideo;
        mVideoPlayer.getLayoutParams().height = heightVideo;

        //外部辅助的旋转，帮助全屏
        mOrientationUtils = new OrientationUtils(this, mVideoPlayer);
        //初始化不打开外部的旋转
        mOrientationUtils.setEnable(false);
        mVideoPlayer.setIsTouchWiget(true);
        //关闭自动旋转
        mVideoPlayer.setRotateViewAuto(false);
        mVideoPlayer.setLockLand(false);
        mVideoPlayer.setShowFullAnimation(false);
        mVideoPlayer.setNeedLockFull(true);
        mVideoPlayer.setEnlargeImageRes(R.drawable.player_controller_full_screen);
        mVideoPlayer.setShrinkImageRes(R.drawable.player_controller_small_screen);
        mVideoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //直接横屏
                mOrientationUtils.resolveByClick();
//                mVideoPlayer.getTitleTextView().setText(((VideoMode) sections.get(sectionPosition).get(position)).getVideoTitle());
                //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                mVideoPlayer.startWindowFullscreen(VideoDetailsActivity.this, true, true);
            }
        });

        mVideoPlayer.setVideoAllCallBack(new GSYSampleCallBack() {
            @Override
            public void onPrepared(String url, Object... objects) {
                super.onPrepared(url, objects);
                mOrientationUtils.setEnable(true);
            }

            @Override
            public void onQuitFullscreen(String url, Object... objects) {
                super.onQuitFullscreen(url, objects);

                if (mOrientationUtils != null) {
                    mOrientationUtils.backToProtVideo();
                }
            }
        });
        getNetWorkData();
    }


    private void getNetWorkData() {
        if (TextUtils.isEmpty(lessonId)) {
            //页面加载错误
            return;
        }
        HttpHelper.getInstance().create(ApiService.class).getLessonData(lessonId, "")
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CourseDetailVo>() {

                    @Override
                    public void onStart() {
                        super.onStart();
                    }

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(final CourseDetailVo lessonDetailObject) {
//                            setUIData();
//                            setPlayerData();
                        lessonData = lessonDetailObject.data;
                        fCatalogId = lessonData.f_catalog_id;
                        sCatalogId = lessonData.s_catalog_id;
                        teacherId = lessonData.teacheruid;

                        //增加封面
                        ImageView imageView = new ImageView(VideoDetailsActivity.this);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        Glide.with(VideoDetailsActivity.this).load(lessonDetailObject.data.thumb_url).into(imageView);
                        mVideoPlayer.setThumbImageView(imageView);
                        mVideoPlayer.setUp(lessonDetailObject.data.sectioin.get(0).getVideos().get(0).getVideo_info().getM3u8url(), false, lessonDetailObject.data.sectioin.get(0).getVideos().get(0).getTitle());
                        mVideoPlayer.startPlayLogic();
                        getAboutData();

                    }
                });


    }

    private void getAboutData() {
        HttpHelper.getInstance().create(ApiService.class).getLessonAboutData(lessonId, fCatalogId, sCatalogId, teacherId, "20")
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CourseDetailRemVideoVo>() {

                    @Override
                    public void onStart() {
                        super.onStart();
                    }

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(CourseDetailRemVideoVo lessonDetailAboutVideoBean) {
                        if (lessonDetailAboutVideoBean != null && lessonDetailAboutVideoBean.errno == 0) {
                            setData(lessonDetailAboutVideoBean);
                            loadManager.showSuccess();
                        }
                    }
                });
    }


    private void setData(CourseDetailRemVideoVo lessonDetailAboutVideoBean) {
        Items items = new Items();
        MultiTypeAdapter adapter = new MultiTypeAdapter();
        adapter.register(CourseDetailRemVideoVo.DataBean.CourseListBean.class, new CourseRecommendViewBinder());
        mRecyclerView.setAdapter(adapter);
        items.addAll(lessonDetailAboutVideoBean.getData().getCourse_list());
        adapter.setItems(items);
        adapter.notifyDataSetChanged();
        assertAllRegistered(adapter, items);
    }

}