FROM node:24-alpine

WORKDIR /build

EXPOSE 4000

ENV NITRO_PORT=4000
ENV NITRO_HOST=0.0.0.0
ENV CHOKIDAR_USEPOLLING=true
ENV WATCHPACK_POLLING=true

COPY .docker/frontend-dev-start.sh /usr/local/bin/frontend-dev-start.sh

CMD ["sh", "/usr/local/bin/frontend-dev-start.sh"]
