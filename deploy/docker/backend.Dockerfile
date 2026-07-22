FROM eclipse-temurin:17-jre

ARG JAR_FILE
ARG APP_NAME=ruoyi-service

ENV APP_NAME="${APP_NAME}" \
    JAVA_OPTS="" \
    HOME=/home/ruoyi \
    TZ=Asia/Shanghai

WORKDIR /opt/qianruo

RUN addgroup --system ruoyi && adduser --system --ingroup ruoyi ruoyi

COPY ${JAR_FILE} /opt/qianruo/app.jar

RUN mkdir -p /home/ruoyi/logs/nacos && \
    chown -R ruoyi:ruoyi /opt/qianruo /home/ruoyi

USER ruoyi

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Duser.home=/home/ruoyi -DJM.LOG.PATH=/home/ruoyi/logs/nacos -jar /opt/qianruo/app.jar"]
