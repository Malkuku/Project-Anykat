import {createRouter, createWebHistory} from 'vue-router'
import HomeView from '@/views/home/index.vue'
import StudentCourseView from "@/views/student/StudentCourse.vue";
import StudentExerciseView from "@/views/student/StudentExercise.vue";
import StudentAnswerView from "@/views/student/StudentAnswer.vue";

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
        { path:'/student/exercise/:courseId/:semesterId',  name: 'studentExercise', component: StudentExerciseView},
        { path: '/student/answer/:courseId/:exerciseId', name: 'studentAnswer', component: StudentAnswerView}
    ]
})


export default router

