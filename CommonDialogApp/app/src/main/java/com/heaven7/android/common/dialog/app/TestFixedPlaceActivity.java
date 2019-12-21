package com.heaven7.android.common.dialog.app;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.heaven7.android.common.dialog.FixedPlaceHorizontalDialogManager;
import com.heaven7.android.common.dialog.FixedPlaceVerticalDialogManager;
import com.heaven7.android.common.dialog.LocationHelper;
import com.heaven7.android.common.dialog.app.utils.CommonUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TestFixedPlaceActivity extends AppCompatActivity {

    @BindView(R.id.tv_anchor)
    View mAnchor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_test_fix_place);
        ButterKnife.bind(this);
    }

    public void onClickFixHorizontal(View view) {
        new HorDialog().show(this);
    }

    public void onClickFixVertical(View view) {
        new VerDialog().show(this);
    }

    private class HorDialog extends FixedPlaceHorizontalDialogManager{

        public HorDialog() {
            super(new LocationHelper.Builder()
                    .setAutoFitEdge(true)
                    .setLocate(LocationHelper.LOCATE_TOP)
                    .setGravity(LocationHelper.GRAVITY_CENTER)
                    .setRetainSpace(30)
                    .setStateBarHeight(CommonUtils.getStatusBarHeight(getApplicationContext()))
                    , mAnchor);
        }
        @Override
        protected int getLayoutId() {
            return R.layout.dialog_test_hor;
        }
        @Override
        protected void onBindDataImpl(Context context, View view, Bundle arguments) {

        }
    }
    private class VerDialog extends FixedPlaceVerticalDialogManager {

        public VerDialog() {
            super(new LocationHelper.Builder()
                            .setAutoFitEdge(true)
                            .setLocate(LocationHelper.LOCATE_LEFT)
                            .setGravity(LocationHelper.GRAVITY_CENTER)
                            .setRetainSpace(30)
                            .setStateBarHeight(CommonUtils.getStatusBarHeight(getApplicationContext()))
                    , mAnchor);
        }
        @Override
        protected int getLayoutId() {
            return R.layout.dialog_test_vertical;
        }
        @Override
        protected void onBindDataImpl(Context context, View view, Bundle arguments) {

        }
    }
}
