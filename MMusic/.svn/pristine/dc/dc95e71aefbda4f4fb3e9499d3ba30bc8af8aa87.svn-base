package com.music.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by dell on 2015/3/16.
 */
public class JsonParser {
    private static final Gson gson = new Gson();

    /**
     * 解析list
     *
     * @param json
     * @param clazz
     * @return
     */
    public static <T> List<T> toList(String json, final Class<T> clazz) {
        Type mySuperClassPackaged = new ParameterizedType() {
            public Type getRawType() {
                return List.class;
            }

            public Type[] getActualTypeArguments() {
                return new Type[]{clazz};
            }

            public Type getOwnerType() {
                return null;
            }
        };
        return gson.fromJson(json, mySuperClassPackaged);
    }

    /**
     * 解析list
     *
     * @param jsonObject
     * @param clazz
     * @return
     */
    public static <T> List<T> toList(JsonElement jsonObject, final Class<T> clazz) {
        Type mySuperClassPackaged = new ParameterizedType() {
            public Type getRawType() {
                return List.class;
            }

            public Type[] getActualTypeArguments() {
                return new Type[]{clazz};
            }

            public Type getOwnerType() {
                return null;
            }
        };
        return gson.fromJson(jsonObject, mySuperClassPackaged);
    }

    /**
     * 解析成对象
     *
     * @param object
     * @param clazz
     * @return
     */
    public static <T> T toObject(JsonElement object, Class<T> clazz) {
        return gson.fromJson(object, clazz);
    }

    /**
     * 解析成对象
     *
     * @param clazz
     * @return
     */
    public static <T> T toObject(String json, final Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    /**
     * 解析成对象
     *
     * @param json
     * @param clazz
     * @param objectType
     * @return
     */
    public static <T> T toObject(String json, final Class<T> clazz, final Class objectType) {
        Type mySuperClassPackaged = new ParameterizedType() {
            public Type getRawType() {
                return clazz;
            }

            public Type[] getActualTypeArguments() {
                return new Type[]{objectType};
            }

            public Type getOwnerType() {
                return null;
            }
        };
        return gson.fromJson(json, mySuperClassPackaged);
    }

}
