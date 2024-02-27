FROM node:12-alpine as builder

WORKDIR /app
# install and cache app dependencies
COPY package.json package-lock.json ./
RUN npm install

COPY . .

RUN npm run build

# ------------------------------------------------------
# Production Build
# ------------------------------------------------------
FROM nginx:1.16.0-alpine
COPY --from=builder /app/dist /var/www/html
RUN rm /etc/nginx/conf.d/default.conf
COPY default.conf /etc/nginx/conf.d
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]