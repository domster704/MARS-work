package com.amber.armtp.ui.report;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amber.armtp.R;

public class ReportFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = getArguments();
        SalesReportResultFragment.SentDataToSalesFragment dateInSalesFragment = null;
        if (bundle != null) {
            dateInSalesFragment = (SalesReportResultFragment.SentDataToSalesFragment) bundle.getSerializable("dataToSalesFragment");
        }

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setSubtitle("");

        TabLayout tabLayout = getActivity().findViewById(R.id.reportTab);
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.reportFirstTab)));
//        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.reportSecondTab)));
//        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.reportThirdTab)));

        ViewPager viewPager = getActivity().findViewById(R.id.reportViewPager);

        ReportPageAdapter adapter;
        if (dateInSalesFragment != null) {
            adapter = new ReportPageAdapter(getChildFragmentManager(), tabLayout.getTabCount(), dateInSalesFragment);
        } else {
            adapter = new ReportPageAdapter(getChildFragmentManager(), tabLayout.getTabCount());
        }

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