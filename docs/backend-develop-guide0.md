## 0. 关联文件

apifox-restful-api.yaml

tables.sql

## 1. 框架要求

采用 Springboot + Mybatis + Lombok 进行开发，其中Mybatis依赖已经配置完成

要求至少包含以下文件夹：entity、vo、controller、service、mapper

## 2. 实例要求

根据tables.sql文件（里面是相关的建表语句）和apifox-restful-api.yaml，建立对应的实例，包含

1. entity —— 和数据库内容一致
2. vo —— 传给前端的数据实例
3. result —— 统一封装传给前端的内容

## 3. 三层架构要求

controller 层负责接受请求

service 层，分为interface 以及它的实现（service/serviceImpl目录下）

mapper 层，数据调用，对于复杂语句可以在resources/mapper目录下建立相应的xml

## 4. 其他

暂时就做这些，TTD要求的test先不写，统一拦截请求、日志等也不写，主要目标是跑通业务流程，可以在apifox的接口文档测试下通过