package com.ligong.neo4jservice.exception;

/**
 * description: 
 * @author shendaowei
 * @date 2025/10/28 19:47
 */
public class Neo4jRuntimeException extends RuntimeException{
    public Neo4jRuntimeException() {
        super();
    }

    public Neo4jRuntimeException(String message) {
        super(message);
    }

    public Neo4jRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}