package com.ligong.neo4jservice.loadbalance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description: 
 * @author shendaowei
 * @date 2025/10/30 17:14
 */
@Service
@Slf4j
public class LoadBalancerService {

    @Autowired
    private HealthCheckService healthCheckService;

    private final List<Server> servers = new CopyOnWriteArrayList<>();
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        Server server1 = new Server("ds1");
        Server server2 = new Server("ds2");
        List<Server> serverList = Arrays.asList(server1,server2);
        healthCheckService.startHealthCheck(serverList);
        servers.addAll(serverList);
    }

    @Retryable(value = Exception.class, maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 1.5))
    public Server getAvailableServer() {
        if (servers.isEmpty()) {
            throw new IllegalStateException("没有可用的服务器");
        }

        int startIndex = currentIndex.get();
        int attempts = 0;

        while (attempts < servers.size()) {
            int index = (startIndex + attempts) % servers.size();
            Server server = servers.get(index);

            if (server.isHealthy()) {
                currentIndex.set((index + 1) % servers.size());
                return server;
            }
            attempts++;
        }
        throw new IllegalStateException("没有健康的服务器可用");
    }

}