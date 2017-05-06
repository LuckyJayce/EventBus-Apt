package com.shizhefei.eventbus;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by LuckyJayce on 2017/5/6.
 */

public class Point implements Parcelable{
    protected Point(Parcel in) {
    }

    public static final Creator<Point> CREATOR = new Creator<Point>() {
        @Override
        public Point createFromParcel(Parcel in) {
            return new Point(in);
        }

        @Override
        public Point[] newArray(int size) {
            return new Point[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
