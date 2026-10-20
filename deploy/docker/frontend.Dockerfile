ARG NGINX_BASE_IMAGE=m.daocloud.io/docker.io/library/nginx:1.27
FROM ${NGINX_BASE_IMAGE}

ARG DIST_DIR
ARG NGINX_CONF

COPY ${NGINX_CONF} /etc/nginx/conf.d/default.conf
COPY ${DIST_DIR} /usr/share/nginx/html

EXPOSE 80
