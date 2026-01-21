package com.ligong.neo4jservice.utils;

import com.ligong.neo4jservice.exception.Neo4jRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description: 
 * @author shendaowei
 * @date 2025/11/14 17:04
 */
@Slf4j
public class CypherUtils3 {

    public static <T> String getSaveCypher(T entity) {
        return getSaveWithRelationsCypher(entity, "n0", new HashMap<>(), 0, new AtomicInteger(0));
    }

    private static <T> String getSaveWithRelationsCypher(T entity, String nodeVariable,
                                                         Map<String, Integer> relationCountMap,
                                                         int depth, AtomicInteger globalNodeCounter) {
        try {
            if (depth > 10) {
                throw new Neo4jRuntimeException("关系深度超过限制，可能存在循环引用");
            }

            StringBuilder cypher = new StringBuilder();

            // 1. 创建主节点
            Node nodeAnnotation = entity.getClass().getAnnotation(Node.class);
            if (nodeAnnotation == null) {
                throw new Neo4jRuntimeException("不合法的neo4j对象，未添加@Node注解");
            }

            String nodeLabel = nodeAnnotation.value().length > 0 ? nodeAnnotation.value()[0] : "";
            if (StringUtils.isBlank(nodeLabel)) {
                throw new Neo4jRuntimeException("不合法的neo4j对象,未添加neo4j节点标识");
            }

            // 查找主键字段和其他字段
            Field idField = null;
            List<Field> normalFields = new ArrayList<>();
            List<Field> relationFields = new ArrayList<>();

            Field[] declaredFields = entity.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Id.class)) {
                    idField = field;
                } else if (field.isAnnotationPresent(Relationship.class)) {
                    relationFields.add(field);
                } else {
                    normalFields.add(field);
                }
            }

            if (idField == null) {
                throw new Neo4jRuntimeException("实体类必须包含@Id注解的字段");
            }

            Object idValue = idField.get(entity);
            if (idValue == null) {
                throw new Neo4jRuntimeException("主键字段值不能为null");
            }

            // 构建 MERGE 语句
            cypher.append("MERGE (").append(nodeVariable).append(":").append(nodeLabel)
                    .append(" {").append(idField.getName()).append(": ")
                    .append(formatPropertyValue(idValue)).append("})");

            // 构建 SET 语句（如果有普通字段）
            List<String> setClauses = new ArrayList<>();
            for (Field field : normalFields) {
                Object value = field.get(entity);
                if (value != null) { // 只设置非空值
                    setClauses.add(nodeVariable + "." + field.getName() + " = " + formatPropertyValue(value));
                }
            }

            if (!setClauses.isEmpty()) {
                cypher.append("\nSET ").append(String.join(", ", setClauses));
            }

            // 2. 递归处理关系字段
            for (Field relationField : relationFields) {
                Object relationValue = relationField.get(entity);
                if (relationValue != null) {
                    Relationship relationAnnotation = relationField.getAnnotation(Relationship.class);
                    String relationType = relationAnnotation.type();
                    Relationship.Direction direction = relationAnnotation.direction();

                    cypher.append(processRelationField(relationValue, relationType, direction,
                            nodeVariable, relationCountMap, depth, globalNodeCounter));
                }
            }

            return cypher.toString();

        } catch (IllegalAccessException e) {
            log.error("生成带关系的save cypher错误:", e);
            throw new Neo4jRuntimeException("生成Cypher语句时发生错误", e);
        }
    }

    private static String processRelationField(Object relationValue, String relationType,
                                               Relationship.Direction direction, String sourceNodeVariable,
                                               Map<String, Integer> relationCountMap, int depth,
                                               AtomicInteger globalNodeCounter) {
        StringBuilder relationCypher = new StringBuilder();

        String relationKey = relationType + "_" + direction;
        int relationIndex = relationCountMap.getOrDefault(relationKey, 0);
        relationCountMap.put(relationKey, relationIndex + 1);

        if (relationValue instanceof Collection) {
            Collection<?> collection = (Collection<?>) relationValue;
            int itemIndex = 0;
            for (Object relatedEntity : collection) {
                String targetNodeVariable = generateUniqueNodeVariable(globalNodeCounter);
                relationCypher.append("\n")
                        .append(processSingleRelation(relatedEntity, relationType, direction,
                                sourceNodeVariable, targetNodeVariable,
                                relationCountMap, depth, globalNodeCounter));
                itemIndex++;
            }
        } else {
            String targetNodeVariable = generateUniqueNodeVariable(globalNodeCounter);
            relationCypher.append("\n")
                    .append(processSingleRelation(relationValue, relationType, direction,
                            sourceNodeVariable, targetNodeVariable,
                            relationCountMap, depth, globalNodeCounter));
        }

        return relationCypher.toString();
    }

    private static String processSingleRelation(Object relatedEntity, String relationType,
                                                Relationship.Direction direction, String sourceNodeVariable,
                                                String targetNodeVariable, Map<String, Integer> relationCountMap,
                                                int depth, AtomicInteger globalNodeCounter) {
        StringBuilder relationCypher = new StringBuilder();

        // 递归创建目标节点
        relationCypher.append(getSaveWithRelationsCypher(relatedEntity, targetNodeVariable,
                relationCountMap, depth + 1, globalNodeCounter));

        // 创建关系
        relationCypher.append("\n");

        switch (direction) {
            case OUTGOING:
                relationCypher.append("MERGE (").append(sourceNodeVariable)
                        .append(")-[:").append(relationType).append("]->(")
                        .append(targetNodeVariable).append(")");
                break;
            case INCOMING:
                relationCypher.append("MERGE (").append(sourceNodeVariable)
                        .append(")<-[:").append(relationType).append("]-(")
                        .append(targetNodeVariable).append(")");
                break;
            default:
                relationCypher.append("MERGE (").append(sourceNodeVariable)
                        .append(")-[:").append(relationType).append("]-(")
                        .append(targetNodeVariable).append(")");
        }

        return relationCypher.toString();
    }

    private static String generateUniqueNodeVariable(AtomicInteger counter) {
        return "node_" + counter.incrementAndGet();
    }

    /**
     * 格式化属性值，用于 Cypher 语句中的属性赋值
     */
    private static String formatPropertyValue(Object value) {
        if (value == null) {
            return "null";
        }

        if (value instanceof String) {
            return "'" + escapeCypherString((String) value) + "'";
        } else if (value instanceof Number) {
            return value.toString();
        } else if (value instanceof Boolean) {
            return Boolean.TRUE.equals(value) ? "true" : "false";
        } else if (value instanceof Date) {
            return "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) value) + "'";
        } else if (value instanceof Enum) {
            return "'" + ((Enum<?>) value).name() + "'";
        } else if (value instanceof Character) {
            return "'" + value.toString() + "'";
        } else {
            // 其他类型转为字符串
            return "'" + escapeCypherString(value.toString()) + "'";
        }
    }

    /**
     * 转义 Cypher 字符串中的特殊字符
     */
    private static String escapeCypherString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

}