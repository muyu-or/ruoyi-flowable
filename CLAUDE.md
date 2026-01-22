# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## High-Level Architecture

This repository is a multi-module project based on the RuoYi framework, integrating both backend (Java/Spring Boot) and frontend (Vue.js) applications:

### Backend
- **Modules:** ruoyi-admin, ruoyi-common, ruoyi-framework, ruoyi-system, ruoyi-generator, ruoyi-quartz, ruoyi-flowable, ruoyi-manage
- **Build tool:** Maven (multi-module, pom.xml at root and per-module)
- **Core tech:** Spring Boot 2.5.x, various Spring ecosystem libraries, Flowable workflow, supporting libraries (Druid, MyBatis/PageHelper, Redis, Swagger, Quartz, Velocity, etc.)
- **Configuration:**
  - Main port: 8080 (see ruoyi-admin/src/main/resources/application.yml)
  - Data source: Druid, configured in `application-druid.yml`
- **Entry point:** Each module with a main Java application entry; ruoyi-admin typically the API entry for frontend

### Frontend
- **Module:** ruoyi-ui
- **Framework:** Vue.js 2.x (element-ui, vue-router, vuex)
- **Dev commands:**
  - Install dependencies: `npm install`
  - Start dev server: `npm run dev` (default port 80, see vue.config.js)
  - Production build: `npm run build:prod`
  - Staging build: `npm run build:stage`
  - Lint: `npm run lint`
- **Proxy to backend:** Dev server proxies API requests to `http://localhost:8080` (see vue.config.js, VUE_APP_BASE_API)
- **API structure:** All API requests handled via `src/api/` with logical submodules for different backend domains

### Integration and Conventions
- **Frontend-backend interaction:**
  - API interactions are RESTful, primarily with ruoyi-admin backend services
  - CORS, cookies, and authentication integrated by default RuoYi implementation
- **Configuration and deployment:**
  - Java modules use Maven profiles and YAML/properties config
  - Vue frontend is separately served and built; output in `dist/` for deployment, with static resources under `static/`
  - For production, reverse proxy (Nginx/etc.) or serve via backend static resource mapping
- **Coding conventions:**
  - Backend follows standard Spring Boot/Maven module separation
  - Frontend component structure under `ruoyi-ui/src/components/` and views under `ruoyi-ui/src/views/`
  - No .cursor or copilot rules present; follow conventional project structure and best practices

## Common Development Tasks

### Backend (Java)
- Build all modules: `mvn clean package`
- Run all tests (if present): `mvn test`
- Run a specific module (example: ruoyi-admin): `mvn spring-boot:run -pl ruoyi-admin`

### Frontend (Vue)
- Install: `npm install`
- Develop: `npm run dev`
- Lint: `npm run lint`
- Build production: `npm run build:prod`
- Build staging: `npm run build:stage`

### Testing
- No explicit test scripts found for main frontend. Some submodules contain placeholder or minimal test entries. Backend uses Maven for Java tests where present.

---

> When performing operations that affect both frontend and backend, be aware of their separate build, run, and deploy processes. Most business logic/API integration will require changes to both ruoyi-admin and ruoyi-ui modules.
