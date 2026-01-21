package com.ligong.neo4jservice.domain.node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * description:
 * @author shendaowei
 * @date 2025/9/29 15:53
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Node("drug")
public class DrugDataNode implements Serializable {

    private String id;

    //标准库id
    @Id
    private Integer standard_id;

    //通用名称
    private String general_name;

    //标准成分
    private String standard_ingredient;

    //SPU分类 1-普通药品 2-中药 3-医疗器械 4-非药品 5-赠品
    private String spu_category;

    //是否是处方药
    private Integer medicines_is_prescribe;

    //启用禁用状态1是0否
    private Integer state;

    //用药性质 0-空 1-急性病用药 2-慢性病用药'
    private Integer drug_property;

    //适用人群
    private String intended_population;

    //剂型
    private String dosage_form;

    //是否含糖
    private Boolean is_sugar;

    //是否含酒精
    private Boolean is_alcohol;

    //禁忌人群
    private String taboo_population;

    //禁忌年龄
    private String taboo_age;

    //禁忌症状
    private String taboo_symptom;

    //禁忌生活习惯
    private String taboo_habit;

    //禁忌过敏史
    private String taboo_allergy;

    //禁忌联合用药
    private String taboo_drug;

    //慎用情况
    private String caution;

    //药品别名
    private String drug_alias_name;

    //适用疾病名称
    private String apply_disease_name;

    //使用症状名称
    private String apply_symptom_name;

    //药物类型
    private String drug_type;

    //适用性别
    private String apply_gender;

    //禁忌基础疾病
    private String taboo_basic_disease;

    //禁忌过往病史
    private String taboo_history_disease;

    //成分归属
    private String ingredient_type;

    //用法用量
    private String take_drug_type;

    //药品性别属性
    private String drug_gender_attribute;

    //是否儿童用药
    private Boolean is_pediatric_drug;

    //是否是儿童禁用
    private Boolean is_pediatric_contraindicated;

    //是否为高血压
    private Boolean is_hypertension;

    //是否为高血糖
    private Boolean is_hyperglycemia;

    //是否为高血脂
    private Boolean is_hyperlipidemia;

    //是否为心脏病
    private Boolean is_heart_disease;

    private String createTime;

    private String updateTime;

    //适用疾病
    @Relationship(type = "HAS_INDICATION", direction = Relationship.Direction.OUTGOING)
    private List<DiseaseDataNode> apply_diseases;

    //适用症状名称
    @Relationship(type = "RELIEF_SYMPTOMS", direction = Relationship.Direction.OUTGOING)
    private List<SymptomDataNode> apply_symptoms;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DrugDataNode that = (DrugDataNode) o;
        return Objects.equals(standard_id, that.standard_id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(standard_id);
    }



}