package com.example.myshare;

public class Config {
    public static final String BASE_URL = "https://rkshareapp.herokuapp.com/";
    public static final String SaveFileUrl = BASE_URL + "api/files";
    public static final String sendEmailUrl = SaveFileUrl + "/send";
    public static final String loginURL = BASE_URL + "api/user/login";
    public static final String signUpURL = BASE_URL + "api/user/signup";
}
