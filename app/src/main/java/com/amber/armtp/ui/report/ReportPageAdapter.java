package com.amber.armtp.ui.report;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ReportPageAdapter extends FragmentPagerAdapter {
    private final int countOfTabs;
    private SalesReportResultFragment.SentDataToSalesFragment dataToSalesFragment = null;

    public ReportPageAdapter(FragmentManager fm, int countOfTabs) {
        super(fm);
        this.countOfTabs = countOfTabs;
    }

    public ReportPageAdapter(FragmentManager fm, int countOfTabs, SalesReportResultFragment.SentDataToSalesFragment data) {
        super(fm);
        this.countOfTabs = countOfTabs;
        this.dataToSalesFragment = data;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                if (dataToSalesFragment != null) {
                    return new SalesFragment(dataToSalesFragment);
                } else {
                    return new SalesFragment();
                }
            case 1:
                return new PromotionFragment();
            case 2:
                return new KPIFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return countOfTabs;
    }
}