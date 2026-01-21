package com.ligong.neo4jservice.loadbalance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * description: 
 * @author shendaowei
 * @date 2025/10/30 17:14
 */
@Service
@Slf4j
public class HealthCheckService {

    @Autowired
    @Qualifier("ds1Neo4jClient")
    private Neo4jClient ds1Neo4jClient;

    @Autowired
    @Qualifier("ds2Neo4jClient")
    private Neo4jClient ds2Neo4jClient;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
            r -> {
                Thread t = new Thread(r, "Health-Checker");
                t.setDaemon(true);
                return t;
            }
    );

    public boolean checkServerHealth(String ds) {
        try {
            if (Objects.equals(ds, "ds1")) {
                List<Map<String, Object>> records = (List<Map<String, Object>>) ds1Neo4jClient.query("CALL db.ping()").fetch().all();
                if (!CollectionUtils.isEmpty(records)) {
                    return true;
                }
            } else if (Objects.equals(ds, "ds2")) {
                List<Map<String, Object>> records = (List<Map<String, Object>>) ds2Neo4jClient.query("CALL db.ping()").fetch().all();
                if (!CollectionUtils.isEmpty(records)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("服务器 {} 健康检查失败: {}", ds, e.getMessage());
            return false;
        }
    }

    /**
     * 批量检查服务器健康状态
     */
    public void checkServersHealth(List<Server> servers) {
        servers.forEach(server -> {
            boolean isHealthy = checkServerHealth(server.getDs());
            if (server.isHealthy() != isHealthy) {
                log.info("服务器 {} 状态变化: {} -> {}",
                        server.getDs(),
                        server.isHealthy() ? "健康" : "不健康",
                        isHealthy ? "健康" : "不健康"
                );
            }
            server.setHealthy(isHealthy);
        });
    }

    /**
     * 启动定时健康检查
     */
    public void startHealthCheck(List<Server> servers) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkServersHealth(servers);
            } catch (Exception e) {
                log.error("健康检查任务执行异常", e);
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void destroy() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("健康检查服务已关闭");
    }
}