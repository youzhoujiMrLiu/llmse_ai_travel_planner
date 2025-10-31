# PostgreSQL JSONB 字段映射修复

## 问题描述

保存旅行计划时出现以下错误:
```
could not execute statement [ERROR: column "ai_generated_plan" is of type jsonb 
but expression is of type character varying 
建议：You will need to rewrite or cast the expression.
```

## 问题原因

在 `TravelPlan` 实体类中,`ai_generated_plan` 字段定义如下:
```java
@Column(name = "ai_generated_plan", columnDefinition = "jsonb")
private String aiGeneratedPlan;
```

虽然使用了 `columnDefinition = "jsonb"`,但 Hibernate 默认将 `String` 类型映射为 `VARCHAR`,导致类型不匹配。

PostgreSQL 的 `jsonb` 类型需要特殊处理,Hibernate 不会自动识别。

## 解决方案

使用 Hibernate 6 (Spring Boot 3+) 提供的 `@JdbcTypeCode` 注解来明确指定 JDBC 类型。

### 修改前
```java
@Column(name = "ai_generated_plan", columnDefinition = "jsonb")
private String aiGeneratedPlan;
```

### 修改后
```java
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@JdbcTypeCode(SqlTypes.JSON)
@Column(name = "ai_generated_plan", columnDefinition = "jsonb")
private String aiGeneratedPlan;
```

### 同时修复 preferences 字段

`preferences` 字段是 PostgreSQL 的 `text[]` 数组类型,也需要类似处理:

```java
@JdbcTypeCode(SqlTypes.ARRAY)
@Column(name = "preferences", columnDefinition = "text[]")
private String[] preferences;
```

## 完整修改

### TravelPlan.java

```java
package com.shingeki.travelplannerbackend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "travel_plans")
public class TravelPlan {
    
    // ... 其他字段 ...
    
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "preferences", columnDefinition = "text[]")
    private String[] preferences;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_generated_plan", columnDefinition = "jsonb")
    private String aiGeneratedPlan;
    
    // ... getters and setters ...
}
```

## 工作原理

### @JdbcTypeCode 注解

`@JdbcTypeCode` 是 Hibernate 6 引入的注解,用于显式指定字段的 JDBC 类型代码。

**SqlTypes.JSON**:
- 告诉 Hibernate 这是一个 JSON 类型
- Hibernate 会使用 PostgreSQL 的 JSONB 处理器
- 自动将 Java String 转换为 PostgreSQL JSONB

**SqlTypes.ARRAY**:
- 告诉 Hibernate 这是一个数组类型
- Hibernate 会使用 PostgreSQL 的数组处理器
- 自动将 Java String[] 转换为 PostgreSQL text[]

### 数据流转

#### 保存数据 (Java → PostgreSQL)
```
Java String aiGeneratedPlan = "{\"summary\": \"...\"}"
    ↓
@JdbcTypeCode(SqlTypes.JSON) 识别为 JSON 类型
    ↓
Hibernate 使用 JSONB 类型处理器
    ↓
PreparedStatement 设置为 PGobject(type: jsonb)
    ↓
PostgreSQL 接收并存储为 jsonb 类型
```

#### 读取数据 (PostgreSQL → Java)
```
PostgreSQL jsonb 字段
    ↓
JDBC ResultSet 读取为 PGobject
    ↓
Hibernate JSONB 处理器转换
    ↓
Java String (JSON 字符串)
```

## 替代方案

如果不想使用 `@JdbcTypeCode`,还有以下方案:

### 方案 1: 使用 Hibernate Types 库 (不推荐)
```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.vladmihalcea</groupId>
    <artifactId>hibernate-types-60</artifactId>
    <version>2.21.1</version>
</dependency>
```

```java
@Type(JsonBinaryType.class)
@Column(name = "ai_generated_plan", columnDefinition = "jsonb")
private String aiGeneratedPlan;
```

**缺点**: 引入额外依赖,而 Hibernate 6 已原生支持

### 方案 2: 使用自定义类型转换器
```java
@Converter
public class JsonbConverter implements AttributeConverter<String, String> {
    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData;
    }
}

@Convert(converter = JsonbConverter.class)
@Column(name = "ai_generated_plan", columnDefinition = "jsonb")
private String aiGeneratedPlan;
```

**缺点**: 需要手动处理类型转换,容易出错

### 方案 3: 使用 JPA 的 @Type (Hibernate 5,已过时)
```java
@Type(type = "jsonb")
@Column(name = "ai_generated_plan")
private String aiGeneratedPlan;
```

**缺点**: 
- Hibernate 6 中已弃用
- 需要注册自定义类型

## 推荐方案对比

| 方案 | Spring Boot 3 兼容 | 简洁性 | 性能 | 推荐度 |
|------|-------------------|--------|------|--------|
| @JdbcTypeCode | ✅ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ✅ 推荐 |
| Hibernate Types | ✅ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⚠️ 可选 |
| 自定义转换器 | ✅ | ⭐⭐ | ⭐⭐⭐⭐ | ❌ 不推荐 |
| @Type (Hibernate 5) | ❌ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ❌ 已弃用 |

## 测试验证

### 1. 重启后端应用
```bash
cd travel-planner-backend
mvn clean spring-boot:run
```

### 2. 测试保存计划
通过前端创建计划并保存,观察控制台日志:

**成功日志**:
```
Hibernate: insert into travel_plans (..., ai_generated_plan, ...) 
values (..., ?::jsonb, ...)
```

**失败日志**(修复前):
```
ERROR: column "ai_generated_plan" is of type jsonb 
but expression is of type character varying
```

### 3. 验证数据库
```sql
-- 查看数据类型
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'travel_plans' 
  AND column_name IN ('ai_generated_plan', 'preferences');

-- 查询保存的数据
SELECT id, destination, ai_generated_plan::text 
FROM travel_plans 
LIMIT 1;
```

## 注意事项

1. **Hibernate 版本要求**:
   - `@JdbcTypeCode` 需要 Hibernate 6.0+
   - Spring Boot 3.x 默认使用 Hibernate 6.x ✅

2. **PostgreSQL JSONB vs JSON**:
   - `jsonb`: 二进制存储,支持索引,查询快(推荐)
   - `json`: 文本存储,不支持索引

3. **JSON 字符串格式**:
   - 确保保存的字符串是有效的 JSON
   - 无效 JSON 会导致 PostgreSQL 插入失败

4. **null 值处理**:
   - `aiGeneratedPlan` 为 null 时,数据库存储为 NULL
   - 不会报错

## 相关文档

- [Hibernate 6 User Guide - Basic Types](https://docs.jboss.org/hibernate/orm/6.0/userguide/html_single/Hibernate_User_Guide.html#basic)
- [PostgreSQL JSONB Documentation](https://www.postgresql.org/docs/current/datatype-json.html)
- [Spring Boot 3 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)

## 总结

✅ 使用 `@JdbcTypeCode(SqlTypes.JSON)` 是 Spring Boot 3 + Hibernate 6 中处理 PostgreSQL JSONB 的**最佳实践**
✅ 无需额外依赖,简洁高效
✅ 同时修复了 `text[]` 数组类型的映射问题
