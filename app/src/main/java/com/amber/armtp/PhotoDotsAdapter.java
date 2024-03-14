package com.amber.armtp;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class PhotoDotsAdapter {
    private final Context context;
    private final int count;
    private int currentPosition;
    private int previousPosition;
    private LinearLayout dotsLayout;

    public PhotoDotsAdapter(Context context, int count, int currentPosition) {
        this.context = context;
        this.count = count;
        this.currentPosition = previousPosition = currentPosition;
    }

    public void fillLayout(LinearLayout view) {
        dotsLayout = view;
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(R.drawable.circle_for_photo);
            imageView.setPadding(5, 0, 0, 5);
            dotsLayout.addView(imageView);
        }
        setCurrentView();
    }

    public void changePosition(int pos) {
        previousPosition = currentPosition;
        currentPosition = pos;
        setCurrentView();
    }

    private void setCurrentView() {
        ImageView viewPrevious = (ImageView) dotsLayout.getChildAt(previousPosition);
        viewPrevious.setImageResource(R.drawable.circle_for_photo);

        ImageView view = (ImageView) dotsLayout.getChildAt(currentPosition);
        view.setImageResource(R.drawable.circle_selected_for_photo);
    }
}
