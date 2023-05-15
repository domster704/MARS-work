package com.amber.armtp.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amber.armtp.R;
import com.amber.armtp.auxiliaryData.CounterAgentInfo;

public class CounterAgentDialogFragment extends DialogFragment {
    private CounterAgentInfo counterAgentInfo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            counterAgentInfo = (CounterAgentInfo) getArguments().getSerializable("counterAgentInfo");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setTitle("Информация о контрагенте");
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);
        View view = inflater.inflate(R.layout.counter_agent_info_layout, container, false);

        TextView login = view.findViewById(R.id.loginContrInfo);
        TextView password = view.findViewById(R.id.passwordContrInfo);
        TextView email = view.findViewById(R.id.emailContrInfo);

        login.setText(counterAgentInfo.login);
        password.setText(counterAgentInfo.password);
        email.setText(counterAgentInfo.email);

        return view;
    }
}
