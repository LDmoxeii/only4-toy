# 资源管理与同步系统

## 项目描述

这是一个基于Vue和Element UI开发的资源树管理与同步系统，用于管理和同步两个不同表之间的资源数据。

## 主要功能

1. 并联展示源表树和目标表树
2. 提供数据编辑、新增、删除功能
3. 支持资源的启用/停用操作
4. 根据同步状态显示不同底色
    - 白色：相同的数据
    - 绿色：新增的数据
    - 黄色：修改的数据
    - 红色：删除的数据
5. 支持切换源表和目标表
6. 勾选资源进行同步
7. 支持同步预览和确认

## 技术栈

- Vue 2.x
- Vuex
- Vue Router
- Element UI
- Axios

## 安装与运行

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run serve

# 构建生产版本
npm run build
```

## 项目结构

```
resource-sync-app/
├── public/              # 静态资源
├── src/
│   ├── api/             # API接口
│   ├── assets/          # 项目资源
│   ├── components/      # 公共组件
│   ├── router/          # 路由配置
│   ├── store/           # Vuex存储
│   ├── utils/           # 工具函数
│   ├── views/           # 页面组件
│   ├── App.vue          # 根组件
│   └── main.js          # 入口文件
├── .gitignore           # Git忽略文件
├── babel.config.js      # Babel配置
├── package.json         # 项目配置
└── README.md            # 项目说明
``` 
