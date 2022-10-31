package com.amber.armtp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

public class ImageAdapter extends PagerAdapter {
    private final Context context;
    private final String[] photoFileNames;
    private final String photoDir;

    public ImageAdapter(Context context, String[] photoFileNames, String photoDir) {
        this.context = context;
        this.photoFileNames = photoFileNames;
        this.photoDir = photoDir;
    }

    @Override
    public int getCount() {
        return photoFileNames.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        SubsamplingScaleImageView imageView = new SubsamplingScaleImageView(context);
        imageView.setImage(ImageSource.uri(photoDir + "/" + photoFileNames[position]));
        imageView.setClickable(true);
        imageView.setMaxScale(100f);
        container.addView(imageView);
        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((SubsamplingScaleImageView) object);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return super.getItemPosition(object);
    }
}
