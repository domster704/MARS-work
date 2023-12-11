package com.amber.armtp.ui.report;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amber.armtp.extra.Config;
import com.amber.armtp.R;
import com.amber.armtp.dbHelpers.DBHelper;
import com.amber.armtp.ui.SettingFragment;
import com.amber.armtp.ui.UpdateDataFragment;

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

        String tradeRepresentativeID = Config.getTPId(getActivity());
        String tpName = new DBHelper(getActivity()).getNameOfTpById(tradeRepresentativeID);

        if (tradeRepresentativeID.equals("") || tpName.equals("")) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Неправильный идентификатор")
                    .setMessage("Неправильно введен или отсутсвует ID торгового представителя. Попробуйте ввести правильное ID или обновить базу данных")
                    .setCancelable(false)
                    .setPositiveButton("Ввести ID", (dialogInterface, i) -> {
                        SettingFragment fragment = new SettingFragment();
                        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();

                        Bundle bundle = new Bundle();
                        bundle.putIntArray("Layouts", new int[]{R.id.reportLayoutsMain});

                        fragment.setArguments(bundle);
                        fragmentTransaction.replace(R.id.frame, fragment);
                        fragmentTransaction.commit();
                    })
                    .setNegativeButton("Обновить БД", ((dialogInterface, i) ->
                            getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frame, new UpdateDataFragment())
                            .commit()))
                    .show();
            return;
        }

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Отчёт");
        toolbar.setSubtitle(tpName);

        TabLayout tabLayout = getActivity().findViewById(R.id.reportTab);
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.reportFirstTab)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.reportSecondTab)));
//        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.reportThirdTab)));

        ViewPager viewPager = getActivity().findViewById(R.id.reportViewPager);

        ReportPageAdapter adapter = new ReportPageAdapter(getChildFragmentManager(), tabLayout.getTabCount());

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