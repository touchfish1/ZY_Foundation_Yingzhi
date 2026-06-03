FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:22-alpine
WORKDIR /app
COPY --from=build /app/.output ./.output
EXPOSE 3000
ENV NUXT_PUBLIC_API_BASE=http://localhost:8080
CMD ["node", ".output/server/index.mjs"]
