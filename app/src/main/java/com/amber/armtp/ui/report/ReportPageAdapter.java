package com.amber.armtp.ui.report;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ReportPageAdapter extends FragmentPagerAdapter {
    private final int countOfTabs;

    public ReportPageAdapter(FragmentManager fm, int countOfTabs) {
        super(fm);
        this.countOfTabs = countOfTabs;
    }


    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return new SalesFragment();
            case 1:
                return new PromotionFragment();
            case 2:
                return new KPIFragmentOld();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return countOfTabs;
    }
}