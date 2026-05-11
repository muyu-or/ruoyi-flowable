FROM nginx:1.27-alpine

ENV TZ=Asia/Shanghai

COPY docker/nginx/default.conf /etc/nginx/conf.d/default.conf
COPY ruoyi-ui/dist/ /usr/share/nginx/html/

EXPOSE 80
