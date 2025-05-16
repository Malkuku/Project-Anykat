import {createRouter, createWebHistory} from 'vue-router'
import HomeView from '@/views/home/index.vue'
import StudentCourseView from "@/views/student/StudentCourse.vue"; '@/views/student/StudentCourse.vue'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'root',
            component: HomeView,
            redirect: '/home'
        },
        { path: '/home', name: 'home', component: HomeView },
        { path: '/student/course', name: 'studentCourse', component: StudentCourseView },
    ]
})


export default router

