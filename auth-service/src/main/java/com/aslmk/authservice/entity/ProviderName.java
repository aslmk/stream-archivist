package com.aslmk.authservice.entity;

public enum ProviderName {
    TWITCH("twitch");

    private final String dbValue;

    ProviderName(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
