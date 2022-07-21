package com.amber.armtp.ui.report;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amber.armtp.R;

public class ReportFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        android.support.v7.widget.Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setSubtitle("");

        TabLayout tabLayout = getActivity().findViewById(R.id.reportTab);
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.reportFirstTab)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.reportSecondTab)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.reportThirdTab)));

        final ViewPager viewPager = getActivity().findViewById(R.id.reportViewPager);
        final ReportPageAdapter adapter = new ReportPageAdapter(getChildFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }
}