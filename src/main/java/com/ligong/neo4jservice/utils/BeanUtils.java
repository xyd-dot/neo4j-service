package com.ligong.neo4jservice.utils;

import com.ligong.neo4jservice.exception.Neo4jRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * description: 
 * @author shendaowei
 * @date 2025/10/28 14:04
 */
@Slf4j
public class BeanUtils {

    public static <T> List<T> convertToBeanList(Collection<Map<String, Object>> mapCollection,
                                                Class<T> targetClass) {
        if (mapCollection == null) {
            return new ArrayList<>();
        }

        return mapCollection.stream()
                .map(map -> convertMapToBean(map, targetClass))
                .collect(Collectors.toList());
    }

    public static <T> T convertMapToBean(Map<String, Object> map, Class<T> targetClass) {
        try {
            T instance = targetClass.getDeclaredConstructor().newInstance();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();

                try {
                    if (fieldName.contains(".")){
                        fieldName = StringUtils.substringAfter(fieldName,".");
                        Field field = targetClass.getDeclaredField(fieldName);
                        field.setAccessible(true);

                        if (value != null && !field.getType().isInstance(value)) {
                            value = convertValue(value, field.getType());
                        }

                        field.set(instance, value);
                    }else {
                        Field field = targetClass.getDeclaredField(fieldName);
                        field.setAccessible(true);

                        if (value != null && !field.getType().isInstance(value)) {
                            value = convertValue(value, field.getType());
                        }

                        field.set(instance, value);
                    }
                } catch (NoSuchFieldException e) {
                    log.error("Field not found:{},e" , fieldName,e);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert map to bean error:", e);
        }
    }

    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;

        if (targetType == Integer.class || targetType == int.class) {
            String str = value.toString();
            String s = str.replace("\"", "");
            return Integer.valueOf(s);
        } else if (targetType == Long.class || targetType == long.class) {
            String str = value.toString();
            String s = str.replace("\"", "");
            return Long.valueOf(s);
        } else if (targetType == Double.class || targetType == double.class) {
            String str = value.toString();
            String s = str.replace("\"", "");
            return Double.valueOf(s);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            String str = value.toString();
            String s = str.replace("\"", "");
            return Boolean.valueOf(s);
        } else if (targetType == String.class) {
            return value.toString().replaceAll("^\"|\"$", "");
        } else {
            throw new Neo4jRuntimeException("暂不支持该结果类型的转换:"+value);
        }

    }

}