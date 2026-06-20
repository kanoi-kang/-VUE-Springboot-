package com.example.backend.dto;

public class AdminLoginResponse {
    private String token;
    private AdminVO admin;

    public AdminLoginResponse() {}

    public AdminLoginResponse(String token, AdminVO admin) {
        this.token = token;
        this.admin = admin;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AdminVO getAdmin() {
        return admin;
    }

    public void setAdmin(AdminVO admin) {
        this.admin = admin;
    }
}