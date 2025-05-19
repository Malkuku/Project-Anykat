# 使用 CentOS 7 作为基础镜像
FROM centos:7

# 替换为阿里云镜像源（关键修复）
RUN rm -rf /etc/yum.repos.d/* && \
    curl -o /etc/yum.repos.d/CentOS-Base.repo http://mirrors.aliyun.com/repo/Centos-7.repo && \
    sed -i 's/gpgcheck=1/gpgcheck=0/g' /etc/yum.repos.d/CentOS-Base.repo && \
    yum clean all && \
    yum makecache

# 安装基础依赖和配置语言环境
RUN yum install -y tar glibc-common && \
    yum clean all && \
    localedef -i en_US -f UTF-8 en_US.UTF-8

# 添加 JDK 17
COPY jdk17.tar.gz /usr/local/
RUN tar -xzf /usr/local/jdk17.tar.gz -C /usr/local/ && \
    rm -f /usr/local/jdk17.tar.gz && \
    ln -s /usr/local/jdk-17.* /usr/local/jdk

# 设置环境变量
ENV JAVA_HOME=/usr/local/jdk
ENV PATH=$JAVA_HOME/bin:$PATH
ENV LANG=en_US.UTF-8
ENV LC_ALL=en_US.UTF-8

# 创建应用目录结构
RUN mkdir -p /anykat/{logs,webapp/WEB-INF} && \
    chmod -R 755 /anykat/webapp

# 复制应用文件
COPY anykat.jar /anykat/
COPY config.yml /anykat/ 

# 设置工作目录
WORKDIR /anykat

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "anykat.jar"]