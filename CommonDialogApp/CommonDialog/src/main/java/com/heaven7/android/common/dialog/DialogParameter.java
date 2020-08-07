package com.heaven7.android.common.dialog;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * the dialog parameter
 * @author heaven7
 * @since 1.0.3
 */
public class DialogParameter implements Parcelable {

    private boolean animationEnabled = true;
    private boolean activityReceiveEventOnOutSide;
    private float dimAmount = -1;          //-1 means default

    public DialogParameter() {
    }

    protected DialogParameter(DialogParameter.Builder builder) {
        this.animationEnabled = builder.animationEnabled;
        this.activityReceiveEventOnOutSide = builder.activityReceiveEventOnOutSide;
        this.dimAmount = builder.dimAmount;
    }

    public void setAnimationEnabled(boolean animationEnabled) {
        this.animationEnabled = animationEnabled;
    }

    public void setActivityReceiveEventOnOutSide(boolean activityReceiveEventOnOutSide) {
        this.activityReceiveEventOnOutSide = activityReceiveEventOnOutSide;
    }

    public void setDimAmount(float dimAmount) {
        this.dimAmount = dimAmount;
    }

    public boolean isAnimationEnabled() {
        return this.animationEnabled;
    }

    public boolean isActivityReceiveEventOnOutSide() {
        return this.activityReceiveEventOnOutSide;
    }

    public float getDimAmount() {
        return this.dimAmount;
    }

    public static class Builder {
        private boolean animationEnabled = true;
        private boolean activityReceiveEventOnOutSide;
        private float dimAmount = -1;          //-1 means default

        public Builder setAnimationEnabled(boolean animationEnabled) {
            this.animationEnabled = animationEnabled;
            return this;
        }

        public Builder setActivityReceiveEventOnOutSide(boolean activityReceiveEventOnOutSide) {
            this.activityReceiveEventOnOutSide = activityReceiveEventOnOutSide;
            return this;
        }

        public Builder setDimAmount(float dimAmount) {
            this.dimAmount = dimAmount;
            return this;
        }

        public DialogParameter build() {
            return new DialogParameter(this);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.animationEnabled ? (byte) 1 : (byte) 0);
        dest.writeByte(this.activityReceiveEventOnOutSide ? (byte) 1 : (byte) 0);
        dest.writeFloat(this.dimAmount);
    }

    protected DialogParameter(Parcel in) {
        this.animationEnabled = in.readByte() != 0;
        this.activityReceiveEventOnOutSide = in.readByte() != 0;
        this.dimAmount = in.readFloat();
    }

    public static final Creator<DialogParameter> CREATOR = new Creator<DialogParameter>() {
        @Override
        public DialogParameter createFromParcel(Parcel source) {
            return new DialogParameter(source);
        }

        @Override
        public DialogParameter[] newArray(int size) {
            return new DialogParameter[size];
        }
    };
}
