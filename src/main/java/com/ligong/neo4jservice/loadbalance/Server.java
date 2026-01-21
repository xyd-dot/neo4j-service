package com.ligong.neo4jservice.loadbalance;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * description: 
 * @author shendaowei
 * @date 2025/10/30 17:13
 */
@Data
@AllArgsConstructor
public class Server {
    private volatile boolean healthy;
    private String ds;

    public Server(String ds) {
        this.ds = ds;
        this.healthy = true;
    }
}