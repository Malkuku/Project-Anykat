<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { queryStudentExercisesApi } from '@/api/student/studentExercise';
import { ElMessage } from 'element-plus';

const route = useRoute();
const userStore = useUserStore();

// 从路由参数获取课程ID和学期ID
const courseId = computed(() => route.params.courseId);
const semesterId = computed(() => route.params.semesterId);

// 从userStore获取学生ID
const studentId = computed(() => userStore.id);

// 搜索表单
const searchForm = ref({
  status: 1, // 默认显示"进行中"的练习
  page: 1,
  pageSize: 10
});

// 分页数据
const pagination = ref({
  total: 0,
  currentPage: 1,
  pageSize: 10
});

// 练习列表
const exerciseList = ref([]);

// 状态选项
const statusOptions = [
  { label: '未开始', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已结束', value: 2 }
];

// 钩子函数
onMounted(() => {
  search();
});

// 搜索方法
const search = async () => {
  const params = {
    courseId: courseId.value,
    semesterId: semesterId.value,
    studentId: studentId.value,
    status: searchForm.value.status,
    page: pagination.value.currentPage,
    pageSize: pagination.value.pageSize
  };

  const result = await queryStudentExercisesApi(params);
  if(result.code){
    exerciseList.value = result.data.list;
    pagination.value.total = result.data.total;
  } else {
    ElMessage.error(result.msg);
  }
}

// 分页变化
const handleCurrentChange = (val) => {
  pagination.value.currentPage = val;
  search();
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  return date.toLocaleString();
}

// 计算完成率
const calculateCompletionRate = (exercise) => {
  if (!exercise.totalQuestionCount) return 0;
  return Math.round((exercise.completedQuestionCount / exercise.totalQuestionCount) * 100);
}

// 计算得分率
const calculateScoreRate = (exercise) => {
  if (!exercise.totalExerciseScore) return 0;
  return Math.round((exercise.studentTotalScore / exercise.totalExerciseScore) * 100);
}
</script>

<template>
  <div class="top-container">
    <div class="header-container">
      <div class="header-left">
        <h1>学生练习信息</h1>
        <el-tag type="info" class="status-tag">当前状态: {{ statusOptions.find(s => s.value === searchForm.status)?.label }}</el-tag>
      </div>

      <!-- 状态筛选 -->
      <div class="status-filter">
        <el-select
            v-model="searchForm.status"
            @change="search"
            class="status-selector"
        >
          <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </div>
    </div>
  </div>

  <!-- 练习卡片列表 -->
  <div class="card-container">
    <el-row :gutter="20">
      <el-col :span="24" v-for="exercise in exerciseList" :key="exercise.exerciseId">
        <el-card shadow="hover" class="exercise-card">
          <div class="card-content">
            <div class="card-left">
              <div class="card-header">
                <span class="exercise-name">{{ exercise.exerciseName }}</span>
                <div class="card-actions">
                  <el-tag :type="exercise.status === 0 ? 'info' : exercise.status === 1 ? 'warning' : 'success'">
                    {{ statusOptions.find(s => s.value === exercise.status)?.label || '未知状态' }}
                  </el-tag>
                  <el-button
                      type="primary"
                      size="small"
                      @click="viewExerciseDetails(exercise.exerciseId)"
                      class="detail-btn"
                  >
                    查看做题情况
                  </el-button>
                </div>
              </div>

              <div class="info-grid">
                <div class="info-item">
                  <span class="label">课程:</span>
                  <span class="value">{{ exercise.courseName }}</span>
                </div>
                <div class="info-item">
                  <span class="label">学期:</span>
                  <span class="value">{{ exercise.semesterName }}</span>
                </div>
                <div class="info-item">
                  <span class="label">时间:</span>
                  <span class="value">
                    {{ formatTime(exercise.startTime) }} - {{ formatTime(exercise.endTime) }}
                  </span>
                </div>
                <div class="info-item">
                  <span class="label">出题人:</span>
                  <span class="value">{{ exercise.creatorName }}</span>
                </div>
              </div>

              <div class="score-info">
                <div class="score-item">
                  <div class="score-label">总分</div>
                  <div class="score-value">{{ exercise.totalExerciseScore }}</div>
                </div>
                <div class="score-item">
                  <div class="score-label">得分</div>
                  <div class="score-value highlight">{{ exercise.studentTotalScore }}</div>
                </div>
                <div class="score-item">
                  <div class="score-label">得分率</div>
                  <div class="score-value">{{ calculateScoreRate(exercise) }}%</div>
                </div>
              </div>
            </div>

            <div class="card-right">
              <div class="progress-container">
                <el-progress
                    type="dashboard"
                    :percentage="calculateCompletionRate(exercise)"
                    :color="[
                    { color: '#f56c6c', percentage: 20 },
                    { color: '#e6a23c', percentage: 40 },
                    { color: '#5cb87a', percentage: 60 },
                    { color: '#1989fa', percentage: 80 },
                    { color: '#6f7ad3', percentage: 100 }
                  ]"
                >
                  <template #default>
                    <div class="progress-text">
                      <span class="progress-percent">{{ calculateCompletionRate(exercise) }}%</span>
                      <span class="progress-label">完成率</span>
                    </div>
                  </template>
                </el-progress>
              </div>

              <div class="question-info">
                <div class="question-item">
                  <span>总题数: {{ exercise.totalQuestionCount }}</span>
                </div>
                <div class="question-item">
                  <span>已完成: {{ exercise.completedQuestionCount }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>

  <!-- 分页 -->
  <div class="pagination-container">
    <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="search"
        @current-change="handleCurrentChange"
    />
  </div>
</template>

<style scoped>
.top-container {
  width: 80%;
  margin: 0 auto 20px;
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

/* 新增按钮样式 */
.card-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.detail-btn {
  margin-left: 10px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.status-tag {
  font-size: 14px;
  height: 32px;
  line-height: 30px;
}

.status-filter {
  display: flex;
  align-items: center;
}

.status-selector {
  width: 120px;
}

.card-container {
  width: 80%;
  margin: 0 auto;
}

.exercise-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.card-content {
  display: flex;
  justify-content: space-between;
  padding: 15px;
}

.card-left {
  flex: 1;
  padding-right: 20px;
}

.card-right {
  width: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-left: 1px solid #ebeef5;
  padding-left: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.exercise-name {
  font-size: 18px;
  font-weight: bold;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 15px;
}

.info-item {
  display: flex;
}

.label {
  width: 60px;
  color: #909399;
  font-weight: bold;
}

.value {
  flex: 1;
}

.score-info {
  display: flex;
  justify-content: space-between;
  margin-top: 15px;
}

.score-item {
  text-align: center;
}

.score-label {
  font-size: 14px;
  color: #909399;
}

.score-value {
  font-size: 20px;
  font-weight: bold;
}

.highlight {
  color: #409EFF;
}

.progress-container {
  margin-bottom: 15px;
}

.progress-text {
  text-align: center;
}

.progress-percent {
  font-size: 24px;
  font-weight: bold;
  display: block;
}

.progress-label {
  font-size: 14px;
  color: #909399;
}

.question-info {
  width: 100%;
  text-align: center;
}

.question-item {
  margin-bottom: 8px;
  font-size: 14px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>