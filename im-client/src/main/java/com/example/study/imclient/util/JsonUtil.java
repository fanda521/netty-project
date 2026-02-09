package com.example.study.imclient.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * JSON序列化/反序列化工具类
 * @author 编程导师
 */
public class JsonUtil {

    /**
     * 对象转JSON字符串
     * @param obj 待序列化对象
     * @return JSON字符串
     */
    public static String toJson(Object obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteDateUseDateFormat);
    }

    /**
     * JSON字符串转对象
     * @param json JSON字符串
     * @param clazz 目标类
     * @param <T> 泛型
     * @return 目标对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }
}