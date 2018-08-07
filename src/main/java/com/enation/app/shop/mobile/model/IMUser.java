package com.enation.app.shop.mobile.model;

import com.enation.framework.database.PrimaryKeyField;

/**
 * Author: Dawei
 * Datetime: 2016-10-09 16:57
 */
public class IMUser {

    private int id;

    private int member_id;

    private String username;

    private String password;

    private String nickname;

    @PrimaryKeyField
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMember_id() {
        return member_id;
    }

    public void setMember_id(int member_id) {
        this.member_id = member_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
