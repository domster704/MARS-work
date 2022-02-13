package com.amber.armtp.interfaces;

import android.graphics.drawable.ColorDrawable;
import android.view.View;

import com.amber.armtp.GlobalVars;
import com.amber.armtp.R;

public interface ChooseNomen{
    default void setChosenNomen(int countOfOrder, View view, int position) {
        int selectedColor = GlobalVars.CurAc.getResources().getColor(R.color.selectedNomen);

        int viewBackgroundColor = ((ColorDrawable) view.getBackground()).getColor();
        System.out.print(selectedColor + " " + viewBackgroundColor + " ");
        if (viewBackgroundColor != selectedColor) {
            view.setBackgroundColor(selectedColor);
            System.out.println(((ColorDrawable) view.getBackground()).getColor());
        } else if (countOfOrder == 0) {
            if (position % 2 != 0) {
                view.setBackgroundColor(GlobalVars.CurAc.getResources().getColor(R.color.gridViewFirstColor));
            } else {
                view.setBackgroundColor(GlobalVars.CurAc.getResources().getColor(R.color.gridViewSecondColor));
            }
        }
    }
}
