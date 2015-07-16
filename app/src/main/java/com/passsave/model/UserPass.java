package com.passsave.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by GaoBiao on 2015/7/13.
 */
@DatabaseTable(tableName = "t_userpass")
public class UserPass {
    @DatabaseField(generatedId=true,useGetSet=true,columnName="_id")
    private int id;
    @DatabaseField(useGetSet=true,columnName="username")
    private String username;
    @DatabaseField(useGetSet=true,columnName="password")
    private String password;
    @DatabaseField(useGetSet=true,columnName="domain")
    private String domain;
    @DatabaseField(useGetSet=true,columnName="name")
    private String name;
    @DatabaseField(useGetSet=true,columnName="user_id")
    private int userId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
