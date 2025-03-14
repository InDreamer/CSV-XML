# CSV to XML Converter

基于Spring Boot和Apache Camel的CSV到XML转换工具。

## 功能特点

- 支持多种日期格式自动识别
- 智能处理中文和西方姓名格式
- 基于模板的XML生成
- 完善的错误处理机制

## 目录结构

```
.
├── input/          # CSV文件输入目录
├── output/         # XML文件输出目录
├── error/          # 错误文件目录
└── src/
    └── main/
        ├── java/
        │   └── com/csvxml/
        │       ├── config/       # 配置类
        │       ├── model/        # 数据模型
        │       ├── processor/    # 处理器
        │       ├── service/      # 服务类
        │       └── util/         # 工具类
        └── resources/
            ├── templates/        # XML模板
            └── application.yml   # 应用配置
```

## 使用方法

1. 准备CSV文件
   - 文件格式：无标题行，固定三列
   - 列顺序：用户ID,姓名,注册日期
   - 示例：
     ```csv
     1001,张三,2024-03-20
     1002,John Smith,03/21/2024
     1003,李四,March 22, 2024
     ```

2. 启动应用
   ```bash
   mvn spring-boot:run
   ```

3. 处理流程
   - 将CSV文件放入`input`目录
   - 转换后的XML文件将出现在`output`目录
   - 处理失败的文件会移动到`error`目录

## 错误处理

- 列数不匹配：文件移至error目录
- 姓名格式错误：记录错误日志
- 日期格式错误：尝试多种格式解析

## 配置说明

可在`application.yml`中配置：
- 日志级别
- 文件监控间隔
- 错误处理策略

## 开发环境

- Java 8
- Spring Boot 2.7.5
- Apache Camel 3.14.0

## 构建部署

1. 构建项目
   ```bash
   mvn clean package
   ```

2. 运行应用
   ```bash
   java -jar target/csv-xml-converter-1.0.0-SNAPSHOT.jar
   ``` 