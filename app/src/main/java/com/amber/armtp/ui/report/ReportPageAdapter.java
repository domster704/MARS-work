package com.amber.armtp.ui.report;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ReportPageAdapter extends FragmentPagerAdapter {
    private final int countOfTabs;
    private String[] chosenCheckBoxInSalesFragment;
    private String[] dateInSalesFragment;

    public ReportPageAdapter(FragmentManager fm, int countOfTabs) {
        super(fm);
        this.countOfTabs = countOfTabs;
        chosenCheckBoxInSalesFragment = null;
    }

    public ReportPageAdapter(FragmentManager fm, int countOfTabs, String[] chosenCBInSalesFragment, String[] dateInSalesFragment) {
        super(fm);
        this.countOfTabs = countOfTabs;
        this.chosenCheckBoxInSalesFragment = chosenCBInSalesFragment;
        this.dateInSalesFragment = dateInSalesFragment;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                if (chosenCheckBoxInSalesFragment != null) {
                    return new SalesFragment(chosenCheckBoxInSalesFragment, dateInSalesFragment);
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