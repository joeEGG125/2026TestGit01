package com.syscom.fep.vo.communication;

import com.google.gson.Gson;

import java.util.Map;

public class BaseJsonCommu extends BaseCommu {

    @Override
    public String toString() {
        // 預設轉為JSON字串
        return this.toJSON();
    }

    /**
     * 1. 轉換成JSON字串
     * 2. 若有壓縮則進行壓縮處理, 轉成壓縮後的hex字串
     *
     * @return
     */
    protected String toJSON() {
        return serializedTo(serializedTo());
    }

    /**
     * 將序列化的JSON字串轉為物件
     *
     * @param json
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T extends BaseCommu> T fromJson(String json) throws Exception {
        return fromJson(json, null);
    }

    /**
     * 將序列化的JSON字串轉為物件
     *
     * @param json
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T extends BaseCommu> T fromJson(String json, Class<T> clazz) throws Exception {
        Gson gson = new Gson();
        return serializedFrom(json, clazz, str -> (String) gson.fromJson(str, Map.class).get("classname"), gson::fromJson);
    }

    /**
     * 是否對序列化後的字串, 轉為HEX字串
     *
     * @return
     */
    @Override
    protected boolean isSerializedToHex() {
        return isCompressed(); // 如果需要壓縮, 那肯定是要轉HEX的
    }

    /**
     * 是否對序列化的字串進行壓縮, 預設是false
     *
     * @return
     */
    @Override
    protected boolean isCompressed() {
        return false;
    }

    /**
     * 轉換成JSON字串
     *
     * @return
     */
    @Override
    public String serializedTo() {
        return new Gson().toJson(this);
    }
}