<script setup>
import { ref, onMounted } from 'vue';
import { queryStudentCoursesApi } from '@/api/student/studentCourse';
import { useUserStore } from '@/stores/user';
import { ElMessage } from 'element-plus';
import {Calendar, Collection, Document} from "@element-plus/icons-vue";

const userStore = useUserStore();

// 查询参数
const queryParams = ref({
  studentId: userStore.id,
  page: 1,
  pageSize: 5
});

// 课程数据
const courses = ref([]);
const total = ref(0);
const semesterOptions = ref([{ value: null, label: "全部学期" }]);
const activeSemester = ref(null);

// 获取学期选项
const fetchSemesterOptions = async () => {
  try {
    const result = await queryStudentCoursesApi({
      studentId: userStore.id,
      page: 1,
      pageSize: 100
    });

    if (result.code && result.data.list.length > 0) {
      const semesters = new Map();
      result.data.list.forEach(course => {
        if (course.semesterId) {
          semesters.set(course.semesterId, {
            value: course.semesterId,
            label: course.semesterName
          });
        }
      });
      semesterOptions.value = [
        semesterOptions.value[0],
        ...Array.from(semesters.values())
      ];
    }
  } catch (error) {
    ElMessage.error('获取学期数据失败');
  }
};

// 获取课程数据
const fetchCourses = async () => {
  try {
    const result = await queryStudentCoursesApi({
      ...queryParams.value,
      semesterId: activeSemester.value
    });

    if (result.code) {
      courses.value = result.data.list;
      total.value = result.data.total;
    }
  } catch (error) {
    ElMessage.error('获取课程数据失败');
  }
};

// 初始化加载
onMounted(async () => {
  await fetchSemesterOptions();
  await fetchCourses();
});

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).replace(/\//g, '-');
};
</script>

<template>
  <!-- 顶层菜单栏 -->
  <div class="top-navbar">
    <h1 class="navbar-title">我的课程</h1>
    <div class="navbar-controls">
      <el-select
          v-model="activeSemester"
          placeholder="选择学期"
          @change="fetchCourses"
          class="semester-select"
      >
        <el-option
            v-for="semester in semesterOptions"
            :key="semester.value"
            :label="semester.label"
            :value="semester.value"
        />
      </el-select>
    </div>
  </div>

  <!-- 课程内容区域 -->
  <div class="course-content">
    <!-- 课程卡片列表 -->
    <div class="course-cards-container">
      <!-- 添加 v-for 循环 -->
      <div
          v-for="course in courses"
          :key="course.courseId"
          class="course-card"
      >
        <!-- 卡片头部 -->
        <div class="card-header">
          <div class="course-badge">
            <el-icon class="course-icon"><Document /></el-icon>
          </div>
          <div class="course-info">
            <h3>{{ course.courseName }}</h3>
            <div class="meta-info">
              <span class="semester">
                <el-icon><Calendar /></el-icon>
                {{ course.semesterName }}
              </span>
              <span class="exercise-total">
                <el-icon><Collection /></el-icon>
                共 {{ course.exerciseCount || 0 }} 个练习
              </span>
            </div>
          </div>
        </div>

        <!-- 卡片底部 -->
        <div class="card-footer">
          <el-tooltip content="进入课程" placement="top">
            <el-button
                type="primary"
                circle
                class="play-btn"
                @click="$router.push(`/course/${course.courseId}`)"
            >
              <span class="play-icon"></span>
            </el-button>
          </el-tooltip>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="fetchCourses"
      />
    </div>
  </div>
</template>

<style scoped>
/* 顶层菜单栏样式 */
.top-navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: white;
  padding: 15px 10%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.navbar-title {
  margin: 0;
  font-size: 20px;
  color: #333;
  font-weight: 500;
}

.navbar-controls {
  display: flex;
  align-items: center;
  gap: 15px;
}

.semester-select {
  width: 200px;
}


/* 进入课程按钮样式 */
.play-btn {
  display: flex;
  align-items: center;
  justify-content: center;
}

.play-icon {
  display: inline-block;
  width: 0;
  height: 0;
  border-top: 6px solid transparent;
  border-left: 10px solid white;
  border-bottom: 6px solid transparent;
  margin-left: 2px; /* 微调三角形位置 */
}

.enter-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 0 15px rgba(64, 158, 255, 0.5);
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

/* 练习总数样式 */
.exercise-total {
  display: inline-flex;
  align-items: center;
  background: #f5f7fa;
  padding: 4px 10px;
  border-radius: 16px;
  font-size: 13px;
  color: #606266;
}

.exercise-total .el-icon {
  margin-right: 5px;
  color: #409eff;
}

/* 卡片信息调整 */
.meta-info {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 8px;
}


/* 课程内容区域 */
.course-content {
  padding: 20px 10%;
  margin-top: 20px;
}

/* 卡片容器 */
.course-cards-container {
  width: 80%;
  margin: 0 auto;
}

/* 简化后的课程卡片 */
.course-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 20px;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.course-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

/* 卡片头部 */
.card-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.course-badge {
  background: #409eff;
  width: 50px;
  height: 50px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.course-icon {
  color: white;
  font-size: 24px;
}

.course-info h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
  color: #333;
}

.meta-info {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 13px;
  color: #666;
}

.meta-info .el-icon {
  margin-right: 4px;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  justify-content: flex-end;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  width: 80%;
  margin-left: auto;
  margin-right: auto;
}

@media (max-width: 768px) {
  .top-navbar, .course-content {
    padding-left: 5%;
    padding-right: 5%;
  }
}
</style>