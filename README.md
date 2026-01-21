项目背景：由于国内暂时没有成熟的图数据库云产品，因此没有成熟的适用于生产的分布式图数据集群架构。
本项目基于neo4j社区版搭建了一个生产可用的neo4j集群，并完成了多节点数据同步，负载均衡轮询读，心跳检测，多数据源动态切换等，并重写了spring-boot-neo4j框架，解决多数据源框架不可用问题

<img width="869" height="515" alt="image" src="https://github.com/user-attachments/assets/911dad89-f873-48c3-a8a7-f58dab00ed44" />
