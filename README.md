# Project-Anykat

**Anyview项目二轮考核作品 | [在线预览](http://8.138.29.57) | [后端源码](https://github.com/Malkuku/Project-Anykat/tree/release-1.1) | [前端源码](https://github.com/Malkuku/Project-Anykat/tree/vue-release-1.1)**

> 注意：本分支仅包含Docker部署配置，完整源码请切换至对应的release分支

## 📬 技术博客
- [CSDN技术专栏](https://blog.csdn.net/2301_79760424)
- [GitHub博客仓库](https://github.com/Malkuku/MyBlog)


## 🚀 快速部署指南

### 前置要求
获取JDK17二进制包：
   - 官方下载：[Oracle JDK 17 Archive](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
   - 将下载的`jdk17.tar.gz`放置于`compose/`目录

### 启动服务
```bash
git clone https://github.com/Malkuku/Project-Anykat.git
sudo mkdir -p /usr/local/app
sudo cp -r Project-Anykat/compose/* /usr/local/app/
cd /usr/local/app
docker-compose up -d
```

## 🌟 项目亮点

### 架构设计
- **模块化工程**：基于Maven的多模块架构
- **核心容器**：自主研发DI容器，支持注解依赖注入
- **AOP体系**：实现声明式事务（`@Transactional`）与切面编程

### 数据层
- **智能连接池**：动态扩缩容+乐观锁并发控制
- **SQL映射工具**：支持：
    - 自动结果集映射
    - 动态参数绑定

### 安全体系
- **JWT认证**：过滤器链实现无状态鉴权
- **上下文安全**：线程级用户身份传递

### 工程化
- **配置中心**：YAML统一配置管理
- **日志系统**：按日期/级别双维度日志归档
- **全链路日志**：请求追踪与诊断支持

### 部署方案
- **Nginx**：前端静态资源托管
- **Docker**：容器化微服务部署
- **云端交付**：公网可访问部署实例

## 📚 功能矩阵

### 学生端
- 📖 课程目录浏览
- 📝 练习任务列表
- ✍️ 多题型支持：
    - 单选题
    - 多选题
    - 简答题

### 教师端
- ✨ 题库管理系统
    - 题目增删改查
- 🎯 练习管理
    - 练习编排
    - 自动发布控制
- 📊 作业批改
    - 自动阅卷（客观题）
    - 手动评分（主观题）
      
>ps：文档仅供参考，请以实物为准<br>
>大D老师，我让你润色，没让你吹水啊，我都不知道自己这么厉害😰👍

## 🛠️ 版本演进

### Release 1.1
- 修复了Release-1.0的多个bug
- 优化分页查询界面
- 基于mysql事务调度器实现自动练习状态更新

