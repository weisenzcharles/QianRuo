ARG JAVA_BASE_IMAGE=mcr.microsoft.com/openjdk/jdk:17-ubuntu
FROM ${JAVA_BASE_IMAGE}

ARG JAR_FILE
ARG APP_NAME=ruoyi-service

ENV APP_NAME="${APP_NAME}" \
    JAVA_OPTS="-Duser.timezone=Asia/Shanghai" \
    HOME=/home/ruoyi \
    TZ=Asia/Shanghai

WORKDIR /opt/qianruo

USER root
RUN addgroup --system ruoyi && adduser --system --ingroup ruoyi ruoyi

COPY ${JAR_FILE} /opt/qianruo/app.jar

RUN mkdir -p /opt/qianruo/logs/ruoyi-gateway \
        /opt/qianruo/logs/ruoyi-auth \
        /opt/qianruo/logs/ruoyi-platform-service \
        /opt/qianruo/logs/ruoyi-merchant-service \
        /opt/qianruo/logs/ruoyi-rental-business \
        /opt/qianruo/logs/ruoyi-gen \
        /opt/qianruo/logs/ruoyi-job \
        /opt/qianruo/logs/ruoyi-file \
        /opt/qianruo/logs/ruoyi-visual-monitor \
        /home/ruoyi/logs/nacos/nacos && \
    chown -R ruoyi:ruoyi /opt/qianruo /home/ruoyi

USER ruoyi

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Duser.timezone=Asia/Shanghai -Duser.home=/home/ruoyi -DJM.LOG.PATH=/home/ruoyi/logs/nacos -jar /opt/qianruo/app.jar"]
