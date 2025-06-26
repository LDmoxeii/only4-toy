import Vue from 'vue'
import VueRouter from 'vue-router'
import ResourceSync from '../views/ResourceSync.vue'

Vue.use(VueRouter)

const routes = [
    {
        path: '/',
        name: 'ResourceSync',
        component: ResourceSync
    }
]

const router = new VueRouter({
    mode: 'history',
    base: process.env.BASE_URL,
    routes
})

export default router
