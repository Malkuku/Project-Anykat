import {createRouter, createWebHistory} from 'vue-router'
import HomeView from '@/views/home/index.vue'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'root',
            component: HomeView,
            redirect: '/home'
        }
    ]
})


export default router

