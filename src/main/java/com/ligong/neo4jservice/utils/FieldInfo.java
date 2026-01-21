package com.ligong.neo4jservice.utils;

import lombok.Data;

/**
 * description: 
 * @author shendaowei
 * @date 2025/10/31 15:43
 */
@Data
public class FieldInfo {

    private String name;
    private Object value;

    public FieldInfo(String name, Object value) {
        this.name = name;
        this.value = value;
    }

}