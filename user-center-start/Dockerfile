FROM openjdk:17-slim

WORKDIR /app

# 复制编译好的 jar
COPY target/*.jar app.jar

# 设置 JVM 参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

# 暴露端口
EXPOSE 8081 10080

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]