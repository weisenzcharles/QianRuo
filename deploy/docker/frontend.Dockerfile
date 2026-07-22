FROM nginx:1.27-alpine

ARG DIST_DIR
ARG NGINX_CONF

COPY ${NGINX_CONF} /etc/nginx/conf.d/default.conf
COPY ${DIST_DIR} /usr/share/nginx/html

EXPOSE 80
