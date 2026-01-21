package com.ligong.neo4jservice.utils;

import com.ligong.neo4jservice.common.enums.QueryResultType;
import com.ligong.neo4jservice.exception.Neo4jRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.InternalEntity;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * description: 
 * @author shendaowei
 * @date 2025/10/28 15:24
 */
@Slf4j
public class CypherUtils {

    private static final ThreadLocal<Set<Object>> processedNodes = ThreadLocal.withInitial(HashSet::new);

    public static String replaceParams(String cypher, Map<String, Object> params) {
        Pattern pattern = Pattern.compile("\\$(\\w+)");
        Matcher matcher = pattern.matcher(cypher);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String paramName = matcher.group(1);
            Object paramValue = params.get(paramName);

            if (paramValue == null) {
                throw new IllegalArgumentException("参数未找到: " + paramName);
            }

            String replacement = formatParameterValue(paramValue);
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public static <T> String getSaveCypher(T t) {
        try {
            Node annotation = t.getClass().getAnnotation(Node.class);
            if (annotation == null) {
                throw new Neo4jRuntimeException("不合法的neo4j对象，未添加@Node注解");
            }
            String[] value = annotation.value();
            if (value.length == 0) {
                throw new Neo4jRuntimeException("不合法的neo4j对象,未添加neo4j节点标识");
            }
            String nodeName = value[0];
            Field keyField = null;
            List<Field> fields = new ArrayList<>();
            Field[] declaredFields = t.getClass().getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                if (declaredField.isAnnotationPresent(Id.class)) {
                    keyField = declaredField;
                } else {
                    fields.add(declaredField);
                }
            }
            if (keyField == null || StringUtils.isBlank(keyField.getName())) {
                throw new Neo4jRuntimeException();
            }
            StringBuilder cypher = new StringBuilder("MERGE (d:" + nodeName + "{" + keyField.getName() + ": " + formatSaveValue(keyField.get(t)) + "}) ");
            cypher.append(" SET ");
            for (Field field : fields) {
                cypher.append("d.").append(field.getName())
                        .append(" = ").append(formatSaveValue(field.get(t)))
                        .append(", ");
            }
            if (cypher.length() > 0) {
                cypher.setLength(cypher.length() - 2);
            }
            return cypher.toString();
        } catch (IllegalAccessException e) {
            log.error("生成save cypher错误:", e);
        }
        return "";
    }

    public static <T> String getDeleteCypher(T t){
        try {
            Node annotation = t.getClass().getAnnotation(Node.class);
            if (annotation == null) {
                throw new Neo4jRuntimeException("不合法的neo4j对象，未添加@Node注解");
            }
            String[] value = annotation.value();
            if (value.length == 0) {
                throw new Neo4jRuntimeException("不合法的neo4j对象,未添加neo4j节点标识");
            }
            Field[] declaredFields = t.getClass().getDeclaredFields();
            Field keyField = null;
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                if (declaredField.isAnnotationPresent(Id.class)) {
                    keyField = declaredField;
                }
            }
            if (keyField == null){
                throw new Neo4jRuntimeException();
            }
            String s = formatSaveValue(keyField.get(t));

            String substring = value[0].substring(0, 1);

            StringBuilder cypher = new StringBuilder("MATCH ");
            cypher.append("(")
                    .append(substring).append(":").append(value[0])
                    .append(") ")
                    .append(" where ")
                    .append(substring)
                    .append(".")
                    .append(keyField.getName())
                    .append(" = ")
                    .append(s)
                    .append(" DETACH DELETE ")
                    .append(substring);
            return cypher.toString();


        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> String getSaveCypherWithRelation(T t) {
        processedNodes.get().clear();
        try {
            if (isSingleCypher(t)){
                return getSaveCypher(t);
            }else {
                return getSaveCypherRecursive(t, "n", new HashMap<>());
            }
        } finally {
            processedNodes.get().clear();
        }
    }

    public static <T> String getUpdateCypher(T t) {

        try {
            Node annotation = t.getClass().getAnnotation(Node.class);
            if (annotation == null) {
                throw new Neo4jRuntimeException("不合法的neo4j对象，未添加@Node注解");
            }
            String[] value = annotation.value();
            if (value.length == 0) {
                throw new Neo4jRuntimeException("不合法的neo4j对象,未添加neo4j节点标识");
            }
            String nodeName = value[0];

            Field keyField = null;
            List<Field> updateFields = new ArrayList<>();

            Field[] declaredFields = t.getClass().getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);

                if (declaredField.isAnnotationPresent(Id.class)) {
                    keyField = declaredField;
                } else {
                    Object fieldValue = declaredField.get(t);
                    if (fieldValue != null) {
                        updateFields.add(declaredField);
                    }
                }
            }

            if (keyField == null || StringUtils.isBlank(keyField.getName())) {
                throw new Neo4jRuntimeException("未找到主键字段");
            }

            if (updateFields.isEmpty()) {
                throw new Neo4jRuntimeException("没有需要更新的字段");
            }

            StringBuilder cypher = new StringBuilder();
            cypher.append("MATCH (d:")
                    .append(nodeName)
                    .append("{")
                    .append(keyField.getName())
                    .append(": ")
                    .append(formatUpdateValue(keyField.get(t)))
                    .append("}) ");

            StringBuilder setClause = new StringBuilder("SET ");
            for (Field field : updateFields) {
                setClause.append("d.")
                        .append(field.getName())
                        .append(" = ")
                        .append(formatUpdateValue(field.get(t)))
                        .append(", ");
            }

            if (setClause.length() > 0) {
                setClause.setLength(setClause.length() - 2);
            }

            cypher.append(setClause);

            return cypher.toString();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static QueryResultType determineResultType(Map<String, Object> record) {
        for (Object value : record.values()) {
            if (value instanceof InternalEntity) {
                return QueryResultType.ENTITY;
            } else if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                return QueryResultType.PRIMITIVE;
            } else if (value instanceof Collection){
                return QueryResultType.COLLECTION;
            }
        }
        return QueryResultType.UNKNOWN;
    }


    public static <T> List<T> processRecordsByType(List<Map<String, Object>> records,
                                                   QueryResultType resultType,
                                                   Class<T> clazz) {
        switch (resultType) {
            case ENTITY:
                List<Map<String, Object>> entityMaps = extractEntityProperties(records);
                return BeanUtils.convertToBeanList(entityMaps, clazz);
            case PRIMITIVE:
                return extractPrimitiveValues(records);
            case COLLECTION:
                throw new Neo4jRuntimeException("暂不支持该参数类型:"+clazz);
            case UNKNOWN:
            default:
                return Collections.emptyList();
        }
    }


    private static List<Map<String, Object>> extractEntityProperties(List<Map<String, Object>> records) {
        return records.stream()
                .flatMap(record -> record.values().stream())
                .filter(InternalEntity.class::isInstance)
                .map(entity -> convertInternalEntityToMap((InternalEntity) entity))
                .collect(Collectors.toList());
    }

    private static Map<String, Object> convertInternalEntityToMap(InternalEntity entity) {
        Map<String, Object> propertyMap = new HashMap<>();
        for (String key : entity.keys()) {
            Value value = entity.get(key);
            propertyMap.put(key, value);
        }
        return propertyMap;
    }


    @SuppressWarnings("unchecked")
    private static <T> List<T> extractPrimitiveValues(List<Map<String, Object>> records) {
        return records.stream()
                .flatMap(record -> record.values().stream())
                .filter(value -> value instanceof String || value instanceof Number || value instanceof Boolean)
                .map(value -> (T) value)
                .collect(Collectors.toList());
    }


    private static String formatUpdateValue(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            String stringValue = value.toString();
            stringValue = stringValue.replace("'", "\\'");
            return "'" + stringValue + "'";
        } else if (value instanceof Boolean) {
            return value.toString().toLowerCase();
        } else if (value instanceof Number) {
            return value.toString();
        } else if (value instanceof Enum) {
            return "'" + value + "'";
        } else {
            return "'" + value+ "'";
        }
    }

    public static <T1, T2, E1, E2> String getCreateRelationCypher(T1 data1, T2 data2, E1 primary1, E2 primary2, String relationName) {
        Field field1 = getPrimaryFiled(data1);

        String name1 = field1.getName();
        String typeName1 = field1.getType().getName();

        if (!Objects.equals(typeName1, primary1.getClass().getName())) {
            throw new Neo4jRuntimeException("主键类型不匹配: " + typeName1 + " vs " + primary1.getClass().getName());
        }
        String nodeName1 = getNodeName(data1);

        Field field2 = getPrimaryFiled(data2);
        String name2 = field2.getName();
        String typeName2 = field2.getType().getName();
        if (!Objects.equals(typeName2, primary2.getClass().getName())) {
            throw new Neo4jRuntimeException("主键类型不匹配: " + typeName2 + " vs " + primary2.getClass().getName());
        }
        String nodeName2 = getNodeName(data2);

        StringBuilder cypher = new StringBuilder("MATCH (a:");
        cypher.append(nodeName1)
                .append(" {")
                .append(name1).append(": ")
                .append(primary1)
                .append("}), (b:")
                .append(nodeName2)
                .append(" {")
                .append(name2).append(": ")
                .append(primary2)
                .append("}) MERGE (a)-[:")
                .append(relationName)
                .append("]-(b)");
        return cypher.toString();
    }

    private static <T> String getNodeName(T node) {
        Node annotation = node.getClass().getAnnotation(Node.class);
        if (annotation == null) {
            throw new Neo4jRuntimeException("不合法的neo4j对象，未添加@Node注解");
        }
        String[] value = annotation.value();
        if (value.length == 0) {
            throw new Neo4jRuntimeException("不合法的neo4j对象,未添加neo4j节点标识");
        }
        return value[0];
    }

    private static <T> Field getPrimaryFiled(T node) {
        Node annotation = node.getClass().getAnnotation(Node.class);
        if (annotation == null) {
            throw new Neo4jRuntimeException("不合法的neo4j对象，未添加@Node注解");
        }
        String[] value = annotation.value();
        if (value.length == 0) {
            throw new Neo4jRuntimeException("不合法的neo4j对象,未添加neo4j节点标识");
        }
        Field[] declaredFields = node.getClass().getDeclaredFields();
        Field keyField = null;
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            if (declaredField.isAnnotationPresent(Id.class)) {
                keyField = declaredField;
            }
        }
        if (keyField == null) {
            throw new Neo4jRuntimeException();
        }
        return keyField;
    }



    private static <T> String getSaveCypherRecursive(T t, String currentAlias,
                                                     Map<String, Integer> aliasCounter) {
        try {
            if (processedNodes.get().contains(t)) {
                return "";
            }
            processedNodes.get().add(t);

            Node nodeAnnotation = t.getClass().getAnnotation(Node.class);
            if (nodeAnnotation == null) {
                throw new RuntimeException("不合法的neo4j对象，未添加@Node注解: " + t.getClass().getName());
            }

            String[] value = nodeAnnotation.value();
            if (value.length == 0) {
                throw new RuntimeException("不合法的neo4j对象,未添加neo4j节点标识: " + t.getClass().getName());
            }

            String nodeName = value[0];

            List<FieldInfo> propertyFields = new ArrayList<>();
            List<RelationshipFieldInfo> relationshipFields = new ArrayList<>();
            Field keyField = null;

            Field[] declaredFields = t.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);

                if (field.isAnnotationPresent(Id.class)) {
                    keyField = field;
                } else if (field.isAnnotationPresent(Relationship.class)) {
                    RelationshipFieldInfo relInfo = processRelationshipField(field, t);
                    if (relInfo != null) {
                        relationshipFields.add(relInfo);
                    }
                } else {
                    Object valueObj = field.get(t);
                    if (valueObj != null) {
                        propertyFields.add(new FieldInfo(field.getName(), valueObj));
                    }
                }
            }

            if (keyField == null) {
                throw new RuntimeException("未找到@Id注解的主键字段: " + t.getClass().getName());
            }

            Object keyValue = keyField.get(t);
            if (keyValue == null || (keyValue instanceof String && ((String) keyValue).trim().isEmpty())) {
                throw new RuntimeException("主键值不能为空: " + t.getClass().getName());
            }

            StringBuilder cypher = new StringBuilder();

            cypher.append("MERGE (").append(currentAlias).append(":").append(nodeName)
                    .append(" {").append(keyField.getName()).append(": ")
                    .append(formatSaveValue(keyValue)).append("})");

            if (!propertyFields.isEmpty()) {
                cypher.append("\nON CREATE SET ")
                        .append(buildSetClause(currentAlias, propertyFields))
                        .append("\nON MATCH SET ")
                        .append(buildSetClause(currentAlias, propertyFields));
            }

            if (!relationshipFields.isEmpty()) {
                for (RelationshipFieldInfo relInfo : relationshipFields) {
                    String relationshipCypher = buildRelationshipCypher(
                            currentAlias, relInfo, aliasCounter);
                    if (!relationshipCypher.isEmpty()) {
                        cypher.append("\n").append(relationshipCypher);
                    }
                }
            }

            return cypher.toString();

        } catch (Exception e) {
            throw new RuntimeException("生成Cypher语句失败: " + e.getMessage(), e);
        }
    }

    private static String buildRelationshipCypher(String mainNodeAlias,
                                                  RelationshipFieldInfo relInfo,
                                                  Map<String, Integer> aliasCounter) {
        StringBuilder relationshipCypher = new StringBuilder();
        String relationshipType = relInfo.getRelationshipType();

        for (int i = 0; i < relInfo.getRelatedObjects().size(); i++) {
            Object relatedObj = relInfo.getRelatedObjects().get(i);

            String baseAlias = relInfo.getFieldName();
            int counter = aliasCounter.getOrDefault(baseAlias, 0);
            String relatedNodeAlias = baseAlias + counter;
            aliasCounter.put(baseAlias, counter + 1);

            String relatedNodeCypher = getSaveCypherRecursive(relatedObj, relatedNodeAlias, aliasCounter);

            if (!relatedNodeCypher.isEmpty()) {
                relationshipCypher.append(relatedNodeCypher).append("\n");
            }

            relationshipCypher.append("WITH ").append(mainNodeAlias).append(", ").append(relatedNodeAlias).append("\n");

            if (relInfo.getDirection() == Relationship.Direction.OUTGOING) {
                relationshipCypher.append("MERGE (").append(mainNodeAlias)
                        .append(")-[:").append(relationshipType)
                        .append("]->(").append(relatedNodeAlias).append(")");
            } else if (relInfo.getDirection() == Relationship.Direction.INCOMING) {
                relationshipCypher.append("MERGE (").append(mainNodeAlias)
                        .append(")<-[:").append(relationshipType)
                        .append("]-(").append(relatedNodeAlias).append(")");
            } else {
                relationshipCypher.append("MERGE (").append(mainNodeAlias)
                        .append(")-[:").append(relationshipType)
                        .append("]-(").append(relatedNodeAlias).append(")");
            }

            if (i < relInfo.getRelatedObjects().size() - 1) {
                relationshipCypher.append("\n");
            }
        }

        return relationshipCypher.toString();
    }

    private static <T> RelationshipFieldInfo processRelationshipField(Field field, T t)
            throws IllegalAccessException {
        Relationship relAnnotation = field.getAnnotation(Relationship.class);
        Object value = field.get(t);

        if (value == null) {
            return null;
        }

        RelationshipFieldInfo relInfo = new RelationshipFieldInfo();
        relInfo.setFieldName(field.getName());
        relInfo.setRelationshipType(relAnnotation.type());
        relInfo.setDirection(relAnnotation.direction());

        // 单个节点
        if (!Collection.class.isAssignableFrom(field.getType())) {
            relInfo.setRelatedObjects(Collections.singletonList(value));
            relInfo.setSingleObject(true);
        }
        // 集合
        else if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            if (collection.isEmpty()) {
                return null;
            }
            relInfo.setRelatedObjects(new ArrayList<>(collection));
            relInfo.setSingleObject(false);
        }

        return relInfo;
    }

    private static String buildSetClause(String alias, List<FieldInfo> fields) {
        return fields.stream()
                .map(field -> alias + "." + field.getName() + " = " + formatSaveValue(field.getValue()))
                .collect(Collectors.joining(", "));
    }



    private static <T> boolean isSingleCypher(T t) {
        try {
            List<Field> relationFields = new ArrayList<>();
            Field[] declaredFields = t.getClass().getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                if (declaredField.isAnnotationPresent(Relationship.class)) {
                    if (declaredField.get(t) != null) {
                        relationFields.add(declaredField);
                    }
                }
            }
            if (CollectionUtils.isEmpty(relationFields)) {
                return true;
            }
        } catch (Exception e) {
            log.error("isSingleCypher error:",e);
        }
        return false;
    }

    private static String escapeString(String str) {
        return str.replace("'", "\\'");
    }

//    private static String formatSaveValue(Object value) {
//        if (value == null) {
//            return "null";
//        } else if (value instanceof String) {
//            return "'" + value + "'";
//        } else if (value instanceof Boolean) {
//            return value.toString().toLowerCase();
//        } else {
//            return value.toString();
//        }
//    }

    private static <T> String formatSaveValue(T value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return "'" + ((String) value).replace("'", "\\'") + "'";
        } else if (value instanceof Number ) {
            return value.toString();
        } else if (value instanceof Boolean){
            return value.toString().toLowerCase();
        } else if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            return collection.stream()
                    .map(CypherUtils::formatSaveValue)
                    .collect(Collectors.joining(", ", "[", "]"));
        } else {
            return "'" + value.toString().replace("'", "\\'") + "'";
        }
    }

    private static String formatParameterValue(Object value) {
        if (value instanceof String) {
            return "\"" + escapeString((String) value) + "\"";
        } else if (value instanceof Number) {
            return value.toString();
        } else if (value instanceof Boolean) {
            return value.toString();
        } else if (value instanceof List) {
            List value1 = (List) value;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[");
            for (Object obj : value1) {
                if (obj instanceof String) {
                    String val = (String) obj;
                    stringBuilder.append("\"")
                            .append(val)
                            .append("\"")
                            .append(",");
                }else if (obj instanceof Number){
                    Number val = (Number) obj;
                    stringBuilder
                            .append(val)
                            .append(",");
                }else {
                    throw new Neo4jRuntimeException("不支持该参数类型:"+value);
                }
            }
            String substring = stringBuilder.substring(0, stringBuilder.length() - 1);
            stringBuilder = new StringBuilder(substring);
            stringBuilder.append("]");
            return stringBuilder.toString();
        } else {
            return "'" + escapeString(value.toString()) + "'";
        }
    }



}