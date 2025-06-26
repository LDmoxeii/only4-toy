const {defineConfig} = require('@vue/cli-service')

module.exports = defineConfig({
    transpileDependencies: true,
    devServer: {
        port: 8080,
        host: 'localhost',
        open: true,
        client: {
            overlay: {
                warnings: false,
                errors: true
            }
        },
        proxy: {
            '/api': {
                target: 'http://localhost:8989', // 后端API地址
                changeOrigin: true,
                pathRewrite: {
                    '^/api': '/api'
                }
            }
        }
    },
    lintOnSave: false,
    configureWebpack: {
        resolve: {
            fallback: {
                path: false,
                fs: false,
                os: false
            }
        }
    }
})
