import {createRouter, createWebHistory} from 'vue-router'
import HomeView from '../views/home/index.vue'
import StudentCourseView from "../views/student/StudentCourse.vue";
import StudentExerciseView from "../views/student/StudentExercise.vue";
import StudentAnswerView from "../views/student/StudentAnswer.vue";
import TeacherExerciseView from '../views/teacher/TeacherExercise.vue'
import TeacherGradingView from "../views/teacher/TeacherGrading.vue";
import ErrorView from '../views/error/index.vue'
import PlaceHolerView from '../views/placeHolder/index.vue'
import {useUserStore} from "../stores/user";

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
        { path: '/student/course', name: 'studentCourse', component: StudentCourseView,meta: { requiresAuth: true} },
        { path:'/student/exercise/:courseId/:semesterId',  name: 'studentExercise', component: StudentExerciseView,meta: { requiresAuth: true} },
        { path: '/student/answer/:courseId/:exerciseId', name: 'studentAnswer', component: StudentAnswerView,meta: { requiresAuth: true } },
        { path: '/teacher/exercise', name: 'teacherExercise', component: TeacherExerciseView,meta: { requiresAuth: true,requiresTeacher: true}},
        { path: '/teacher/grading/:exerciseId', name: 'teacherGrading', component: TeacherGradingView,meta: { requiresAuth: true,requiresTeacher: true}},
        { path: '/error', name: 'error', component: ErrorView},
        { path: '/admin/dashboard', name: 'placeHolder', component: PlaceHolerView,meta: { requiresAuth: true,requiresAdmin: true,requiresDev: true}}
    ]
})


// 路由守卫
router.beforeEach((to, from, next) => {
    const userStore = useUserStore()
    const isAuthenticated = userStore.isAuthenticated()

    // 检查需要认证的路由
    if (to.matched.some(record => record.meta.requiresAuth)) {
        if (!isAuthenticated) {
            next({
                path: '/',
            })
        }
    }

    //检查需要教师权限
    if(to.matched.some(record => record.meta.requiresTeacher)){
        if(!userStore.role > 0){
            next({
                path: '/error',
                query: {
                    errorMessage: '您没有教师权限，无法访问此页面',
                    redirectTo: '/'
                }
            })
        }
    }

    //检查需要管理员权限
    if(to.matched.some(record => record.meta.requiresAdmin)){
        if(!userStore.adminToken){
            next({
                path: '/error',
                query: {
                    errorMessage: '您没有管理员权限，无法访问此页面',
                    redirectTo: '/'
                }
            })
        }
    }

    // 检查需要开发者权限
    if (to.matched.some(record => record.meta.requiresDev)) {
        if (1 === 1) {
            next({
                path: '/error',
                query: {
                    errorMessage: '噢，非常抱歉，此界面还在开发中😣',
                    redirectTo: '/'
                }
            })
        }
    }
    next()
})


export default router

