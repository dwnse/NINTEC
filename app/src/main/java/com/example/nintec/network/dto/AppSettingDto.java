package com.example.nintec.network.dto;

import com.google.gson.annotations.SerializedName;

public class AppSettingDto {
    @SerializedName("setting_key")
    public String settingKey;

    @SerializedName("value")
    public String value;

    @SerializedName("value_type")
    public String valueType;
}
