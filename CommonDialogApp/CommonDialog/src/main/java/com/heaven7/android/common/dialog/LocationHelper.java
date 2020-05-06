package com.heaven7.android.common.dialog;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

/**
 * help to locate the view. by locate type and gravity.
 * Created by heaven7 on 2018/10/8 0008.
 */
public class LocationHelper {

    public static final byte GRAVITY_CENTER  = 3;
    public static final byte GRAVITY_LEFT    = 1;
    public static final byte GRAVITY_RIGHT   = 2;
    public static final byte GRAVITY_TOP     = 4;
    public static final byte GRAVITY_BOTTOM  = 5;

    public static final byte LOCATE_TOP      = 11;
    public static final byte LOCATE_BOTTOM   = 12;
    public static final byte LOCATE_LEFT     = 13;
    public static final byte LOCATE_RIGHT    = 14;

    /** the retain space of left/right/top/bottom */
    private int retainSpace;
    private byte locate;
    private int locateOffset; // > 0 means to bottom, < 0 means to top
    private byte gravity;
    private int gravityOffset;
    /** the whole view */
    private View placeView;
    /** the mark view which show above the anchor view */
    private View markView;
    /** auto fit edge or not .*/
    private boolean autoFitEdge;
    private Callback callback;

    private int stateBarHeight;

    private LocationHelper(Builder builder) {
        this.retainSpace = builder.retainSpace;
        this.locate = builder.locate;
        this.locateOffset = builder.locateOffset;
        this.gravity = builder.gravity;
        this.gravityOffset = builder.gravityOffset;
        this.placeView = builder.placeView;
        this.markView = builder.markView;
        this.autoFitEdge = builder.autoFitEdge;
        this.callback = builder.callback;
        this.stateBarHeight = builder.stateBarHeight;
    }

    public void applyVertical(View anchor){
        int placeViewWidth = getPlaceViewWidth();
        int placeViewHeight = getPlaceViewHeight();
        //Logger.d("LocationHelper", "applyVertical", "placeViewWidth = " + placeViewWidth + " ,placeViewHeight = " + placeViewHeight);

        int width = anchor.getMeasuredWidth();
        int height = anchor.getMeasuredHeight();
        int[] cors = new int[2];
        anchor.getLocationOnScreen(cors);
        cors[1] -= stateBarHeight;

        int x = 0;
        int y = 0;
        switch (locate){
            case LOCATE_LEFT:{
                switch (gravity){
                    case GRAVITY_TOP:
                        x = cors[0] + locateOffset - placeViewWidth;
                        y = cors[1];
                        break;

                    case GRAVITY_BOTTOM:
                        x = cors[0] + locateOffset - placeViewWidth;
                        y = cors[1] - (placeViewHeight - width);
                        break;

                    case GRAVITY_CENTER:
                        x = cors[0] + locateOffset - placeViewWidth;
                        y = cors[1] + (height - placeViewHeight ) / 2;
                        break;

                     default:
                         throw new RuntimeException("wrong gravity for LOCATE_LEFT");
                }
            }break;

            case LOCATE_RIGHT:{
                switch (gravity){
                    case GRAVITY_TOP:
                        x = cors[0] + width + locateOffset;
                        y = cors[1];
                        break;

                    case GRAVITY_BOTTOM:
                        x = cors[0] + width + locateOffset;
                        y = cors[1] - (placeViewHeight - height);
                        break;

                    case GRAVITY_CENTER:
                        x = cors[0] + width + locateOffset;
                        y = cors[1] + (height - placeViewHeight ) / 2;
                        break;
                    default:
                        throw new RuntimeException("wrong gravity for LOCATE_RIGHT");
                }
            }break;

            default:
                throw new UnsupportedOperationException("locate = " + locate);
        }
        y += gravityOffset;
        int[] xy = fitEdge(x, y);
        callback.applyLocation(xy[0], xy[1]);

        //markView: the indicator of placeView.
        if(markView != null) {
            int markHeight = markView.getMeasuredHeight();
            //permit move
            int marginTop = (cors[1] + height / 2) - (xy[1] + markHeight / 2);
            callback.applyMarkMargin(markView, marginTop);
        }
    }

    public void applyHorizontal(View anchor){
        int placeViewWidth = getPlaceViewWidth();
        int placeViewHeight = getPlaceViewHeight();
       // Logger.d("LocationHelper", "applyHorizontal", "placeViewWidth = " + placeViewWidth + " ,placeViewHeight = " + placeViewHeight);

        //the anchor width and height
        int width = anchor.getMeasuredWidth();
        int height = anchor.getMeasuredHeight();
        // the anchor locations on screen
        int[] cors = new int[2];
        anchor.getLocationOnScreen(cors);
        cors[1] -= stateBarHeight;

        //the center x, y
        int x = 0;
        int y = 0;
        switch (locate){
            case LOCATE_TOP:{
                switch (gravity){
                    case GRAVITY_LEFT:
                        x = cors[0];
                        y = cors[1] + locateOffset - placeViewHeight;
                        break;

                    case GRAVITY_RIGHT:
                        x = cors[0] + width - placeViewWidth;
                        y = cors[1] + locateOffset - placeViewHeight;
                        break;

                    case GRAVITY_CENTER:
                        x = cors[0]  + (width - placeViewWidth) / 2;
                        y = cors[1] + locateOffset - placeViewHeight;
                        break;
                    default:
                        throw new RuntimeException("wrong gravity for LOCATE_TOP");
                }
            }break;

            case LOCATE_BOTTOM:{
                switch (gravity){
                    case GRAVITY_LEFT:
                        x = cors[0];
                        y = cors[1] + height + locateOffset;
                        break;

                    case GRAVITY_RIGHT:
                        x = cors[0] + width - placeView.getMeasuredWidth();
                        y = cors[1] + height + locateOffset;
                        break;

                    case GRAVITY_CENTER:
                        x = cors[0] + ( width - placeView.getMeasuredWidth()) / 2;
                        y = cors[1] + height + locateOffset;
                        break;
                    default:
                        throw new RuntimeException("wrong gravity for LOCATE_BOTTOM");
                }
            }break;

            default:
                throw new UnsupportedOperationException("locate = " + locate);
        }
        x += gravityOffset;
        //x, y is the dest center.
        int[] xy = fitEdge(x, y);
        callback.applyLocation(xy[0], xy[1]);
        //markView: the indicator of placeView.
        if(markView != null) {
            int mWidth = markView.getMeasuredWidth();
            //permit move
            int marginLeft = (cors[0] + width / 2) - (xy[0] + mWidth / 2);
            callback.applyMarkMargin(markView, marginLeft);
        }
    }

    private int[] fitEdge(int x, int y) {
        int[] cors = new int[2];
        cors[0] = x;
        cors[1] = y;

        DisplayMetrics dm = getDisplayMetrics(placeView.getContext());
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        if(autoFitEdge){
            if(x < retainSpace){
                cors[0] = retainSpace;
            }else{
                int delta = x + getPlaceViewWidth() -(screenWidth - retainSpace);
                if(delta > 0) {
                    cors[0] = x - delta;
                }
            }
            if(y < retainSpace){
                cors[1] = retainSpace;
            }else{
                int delta = y + getPlaceViewHeight() - (screenHeight - retainSpace);
                if(delta > 0){
                    cors[1] = y - delta;
                }
            }
        }
        return cors;
    }

    private DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        return dm;
    }

    private int getPlaceViewWidth(){
        int width = placeView.getMeasuredWidth();
        if(width == 0){
            throw new IllegalStateException();
        }
        return width;
    }
    private int getPlaceViewHeight(){
        int height = placeView.getMeasuredHeight();
        if(height == 0){
            throw new IllegalStateException();
        }
        return height;
    }

    /**
     * the callback of location
     */
    public interface Callback{
        /**
         * apply location
         * @param x the x. relative to left-top.
         * @param y the y. relative to left-top.
         */
        void applyLocation(int x, int y);
        /**
         * apply the margin of mark view
         * @param markView the mark view
         * @param margin the margin start(often be left) or top. may be negative
         */
        void applyMarkMargin(View markView, int margin);
    }

    //-------------------------------------------------
    public int getRetainSpace() {
        return this.retainSpace;
    }

    public byte getLocate() {
        return this.locate;
    }

    public int getLocateOffset() {
        return this.locateOffset;
    }

    public byte getGravity() {
        return this.gravity;
    }

    public int getGravityOffset() {
        return this.gravityOffset;
    }

    public View getPlaceView() {
        return this.placeView;
    }

    public boolean isAutoFitEdge() {
        return this.autoFitEdge;
    }

    public Callback getCallback() {
        return this.callback;
    }

    public static class Builder {
        /** the retain space of left/right/top/bottom */
        private int retainSpace;
        private byte locate;
        private int locateOffset; // > 0 means to bottom, < 0 means to top
        private byte gravity;
        private int gravityOffset;
        /** the content view to position. */
        private View placeView;
        private View markView;
        /** auto fit edge or not .*/
        private boolean autoFitEdge;
        private Callback callback;

        private int stateBarHeight;

        public Builder setStateBarHeight(int stateBarHeight){
            this.stateBarHeight = stateBarHeight;
            return this;
        }

        public Builder setRetainSpace(int retainSpace) {
            this.retainSpace = retainSpace;
            return this;
        }

        public Builder setLocate(byte locate) {
            this.locate = locate;
            return this;
        }

        public Builder setLocateOffset(int locateOffset) {
            this.locateOffset = locateOffset;
            return this;
        }

        public Builder setGravity(byte gravity) {
            this.gravity = gravity;
            return this;
        }
        public Builder setGravityOffset(int gravityOffset) {
            this.gravityOffset = gravityOffset;
            return this;
        }
        public Builder setPlaceView(View placeView) {
            this.placeView = placeView;
            return this;
        }
        public Builder setAutoFitEdge(boolean autoFitEdge) {
            this.autoFitEdge = autoFitEdge;
            return this;
        }
        public Builder setCallback(Callback callback) {
            this.callback = callback;
            return this;
        }
        public Builder setMarkView(View view){
            this.markView = view;
            return this;
        }
        public LocationHelper build() {
            if(placeView == null || callback == null){
                throw new NullPointerException();
            }
            return new LocationHelper(this);
        }
    }
}
