package com.amber.armtp.auxiliaryData;

import java.io.Serializable;

public class CounterAgentInfo implements Serializable {
    public String login;
    public String password;
    public String email;

    public CounterAgentInfo(String login, String password, String email) {
        this.login = login;
        this.password = password;
        this.email = email;
    }
}
