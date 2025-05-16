<script setup>
import { ref, onMounted,onUnmounted,computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { queryQuestionsApi, submitAnswersApi } from '@/api/student/studentAnswer';
import { ElMessage, ElMessageBox } from 'element-plus';
import { formatDateTime,formatDate,getCountdown } from '@/utils/date';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 添加定时器实时更新倒计时
const countdownTimer = ref(null);


// 从路由和用户信息中获取参数
const exerciseId = ref(route.params.exerciseId || route.query.exerciseId);
const courseId = ref(route.params.courseId || route.query.courseId);
const studentId = ref(userStore.id);

// 查询参数
const queryParams = ref({
  exerciseId: exerciseId.value,
  studentId: studentId.value,
  courseId: courseId.value
});

// 练习题目列表
const questionList = ref([]);
// 使用对象存储答案，键为 questionId
const answers = ref({});

// 加载数据
const loading = ref(false);

// 初始化答案对象
const initAnswer = (questionId) => {
  if (!answers.value[questionId]) {
    answers.value[questionId] = {
      answer: '',
      correctStatus: 0
    };
  }
  return answers.value[questionId];
};

// 查询练习题目
const search = async () => {
  try {
    loading.value = true;
    const result = await queryQuestionsApi(queryParams.value);
    if (result.code) {
      questionList.value = result.data;
      // 初始化答案集合
      answers.value = {};
      result.data.forEach(item => {
        answers.value[item.questionId] = {
          answer: item.studentAnswer?.answer || '',
          correctStatus: item.studentAnswer?.correctStatus || 0
        };
      });
    }
  } finally {
    loading.value = false;
  }
};

// 返回上一页
const goBack = () => {
  router.go(-1);
};

// 根据剩余时间获取标签类型
const getCountdownType = (endTime) => {
  if (!endTime) return 'info';

  const now = new Date();
  const end = new Date(endTime);
  const diff = end - now;

  if (diff <= 0) return 'danger'; // 已截止
  if (diff < 60 * 60 * 1000) return 'danger'; // 小于1小时
  if (diff < 24 * 60 * 60 * 1000) return 'warning'; // 小于1天
  return 'success'; // 大于1天
};

//钩子
onMounted(() => {
  search();
  // 每秒更新一次倒计时
  countdownTimer.value = setInterval(() => {
    if (questionList.value.length > 0) {
      // 触发响应式更新
      questionList.value = [...questionList.value];
    }
  }, 1000);
});

onUnmounted(() => {
  clearInterval(countdownTimer.value);
});

// 保存答案
const saveAnswers = async (status) => {
  try {
    // 准备要提交的答案数据
    const answersToSubmit = questionList.value.map(question => {
      const answer = answers.value[question.questionId] || { answer: '', correctStatus: 0 };
      return {
        studentId: studentId.value,
        exerciseId: exerciseId.value,
        questionId: question.questionId,
        answer: answer.answer,
        submitTime: new Date().toISOString(),
        correctStatus: status
      };
    });

    const result = await submitAnswersApi(answersToSubmit);
    if (result.code) {
      ElMessage.success(status === 1 ? '提交成功' : '保存成功');
      await search(); // 重新加载数据
    }
  } catch (error) {
    ElMessage.error('操作失败');
    console.error('提交答案失败:', error);
  }
};

// 提交确认
const confirmSubmit = () => {
  ElMessageBox.confirm('确认提交所有答案吗?提交后将不能再修改', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    saveAnswers(1); // 1表示已提交
  }).catch(() => {
    ElMessage.info('已取消提交');
  });
};

// 格式化题目类型
const formatQuestionType = (type) => {
  const types = {
    0: '单选题',
    1: '多选题',
    2: '简答题'
  };
  return types[type] || '未知类型';
};

// 格式化批改状态
const formatCorrectStatus = (status) => {
  const statusMap = {
    0: '未提交',
    1: '待批改',
    2: '已批改'
  };
  return statusMap[status] || '未知状态';
};

// 格式化分数显示
const formatScore = (score, totalScore) => {
  if (score === null || score === undefined) return '-';
  return `${score}/${totalScore}`;
};

// 计算是否已提交
const isSubmitted = computed(() => {
  return questionList.value.some(item => item.studentAnswer?.correctStatus === 1 || item.studentAnswer?.correctStatus === 2);
});

// 处理多选题的答案
const handleCheckboxChange = (questionId, value) => {
  if (!answers.value[questionId]) {
    answers.value[questionId] = { answer: '', correctStatus: 0 };
  }
  answers.value[questionId].answer = Array.isArray(value) ? value.join(',') : value;
};

// 获取多选题当前值
const getCheckboxValues = computed(() => {
  return (questionId) => {
    const answer = answers.value[questionId]?.answer || '';
    return answer ? answer.split(',') : [];
  };
});

</script>

<template>
  <div class="container">
    <div class="header">
      <el-button type="primary" plain @click="goBack" class="back-btn">
        <i class="el-icon-arrow-left"></i> 返回
      </el-button>
      <h1>练习题目</h1>
    </div>

    <el-card v-if="questionList.length > 0" class="exercise-info">
      <div class="exercise-header">
        <h2>{{ questionList[0].exerciseName }}</h2>
        <el-tag :type="questionList[0].exerciseStatus === 2 ? 'success' : 'warning'">
          {{ formatCorrectStatus(questionList[0].exerciseStatus) }}
        </el-tag>
      </div>
      <div class="exercise-meta">
        <p><span class="meta-label">课程:</span> {{ questionList[0].courseName }}</p>
        <p><span class="meta-label">时间:</span> {{ formatDate(questionList[0].startTime) }} 至 {{ formatDate(questionList[0].endTime) }}</p>
        <p><span class="meta-label">剩余时间:</span>
          <el-tag
              :type="getCountdownType(questionList[0].endTime)"
              size="small"
              :class="{ 'blink-animation': getCountdownType(questionList[0].endTime) === 'danger' && getCountdown(questionList[0].endTime) !== '已截止' }"
          >
            {{ getCountdown(questionList[0].endTime) }}
          </el-tag>
        </p>
      </div>
    </el-card>

    <div class="action-buttons">
      <el-button type="primary" @click="saveAnswers(0)" :disabled="isSubmitted">
        <i class="el-icon-upload2"></i> 保存答案
      </el-button>
      <el-button type="success" @click="confirmSubmit" :disabled="isSubmitted">
        <i class="el-icon-check"></i> 提交答案
      </el-button>
    </div>

    <div v-loading="loading" class="question-list">
      <el-card
          v-for="(question, index) in questionList"
          :key="question.questionId"
          class="question-item"
          :class="{
            'has-corrected': question.studentAnswer?.correctStatus === 2
          }"
      >
        <div class="question-header">
          <span class="question-index">第{{ index + 1 }}题</span>
          <el-tag :type="question.questionType === 2 ? 'warning' : 'primary'" size="small">
            {{ formatQuestionType(question.questionType) }}
          </el-tag>
          <el-tag type="danger" size="small">
            {{ question.score }}分
          </el-tag>
          <el-tag
              :type="question.studentAnswer?.correctStatus === 2 ? 'success' :
                     question.studentAnswer?.correctStatus === 1 ? 'warning' : 'info'"
              size="small"
          >
            {{ formatCorrectStatus(question.studentAnswer?.correctStatus || 0) }}
          </el-tag>
          <span v-if="question.studentAnswer?.correctStatus === 2" class="question-result">
            得分: <strong>{{ formatScore(question.studentAnswer?.score, question.score) }}</strong>
          </span>
        </div>

        <div class="question-content">
          <h3>{{ question.questionContent }}</h3>
          <p v-if="question.questionDescription" class="question-desc">
            {{ question.questionDescription }}
          </p>
        </div>

        <!-- 单选题 -->
        <div v-if="question.questionType === 0" class="question-options">
          <el-radio-group
              :model-value="answers[question.questionId]?.answer"
              @update:model-value="val => {
        if (!answers[question.questionId]) {
          answers[question.questionId] = { answer: '', correctStatus: 0 };
        }
        answers[question.questionId].answer = val;
      }"
              :disabled="isSubmitted"
          >
            <el-radio
                v-for="(option, key) in question.questionOptions"
                :key="key"
                :label="key"
                class="option-item"
            >
              <span class="option-key">{{ key }}.</span> {{ option }}
            </el-radio>
          </el-radio-group>
        </div>

        <!-- 多选题 -->
        <div v-if="question.questionType === 1" class="question-options">
          <el-checkbox-group
              :model-value="getCheckboxValues(question.questionId)"
              @update:model-value="val => handleCheckboxChange(question.questionId, val)"
              :disabled="isSubmitted"
          >
            <el-checkbox
                v-for="(option, key) in question.questionOptions"
                :key="key"
                :label="key"
                class="option-item"
            >
              <span class="option-key">{{ key }}.</span> {{ option }}
            </el-checkbox>
          </el-checkbox-group>
        </div>

        <!-- 简答题 -->
        <div v-if="question.questionType === 2" class="question-textarea">
          <el-input
              type="textarea"
              :rows="5"
              :model-value="answers[question.questionId]?.answer"
              @update:model-value="val => {
        if (!answers[question.questionId]) {
          answers[question.questionId] = { answer: '', correctStatus: 0 };
        }
        answers[question.questionId].answer = val;
      }"
              :disabled="isSubmitted"
              :maxlength="question.wordLimit"
              show-word-limit
              placeholder="请输入您的答案"
          ></el-input>
          <div v-if="question.wordLimit" class="word-limit">
            字数限制: {{ question.wordLimit }}字
          </div>
        </div>

        <!-- 批改信息 -->
        <div v-if="question.studentAnswer?.correctStatus === 2" class="correct-info">
          <el-divider />
          <h4><i class="el-icon-tickets"></i> 批改信息</h4>
          <div class="correct-detail">
            <p><span class="detail-label">正确答案:</span> {{ question.correctAnswer || question.referenceAnswer || '无' }}</p>
            <p><span class="detail-label">解析:</span> {{ question.answerAnalysis || '无' }}</p>
            <p><span class="detail-label">评语:</span> {{ question.studentAnswer?.correctComment || '无' }}</p>
            <div class="time-info">
              <span><span class="detail-label">提交时间:</span> {{ formatDateTime(question.studentAnswer?.submitTime) }}</span>
              <span><span class="detail-label">批改时间:</span> {{ formatDateTime(question.studentAnswer?.correctTime) }}</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
/* 倒计时紧急时闪烁动画 */
@keyframes blink {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

.blink-animation {
  animation: blink 1s infinite;
}

.exercise-meta .el-tag {
  margin-left: 5px;
  min-width: 100px;
  text-align: center;
}

.container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  margin: 0 auto;
  text-align: center;
  flex-grow: 1;
  transform: translateX(-16px);
}

.back-btn {
  margin-right: 10px;
}

.exercise-info {
  margin-bottom: 20px;
  padding: 20px;
}

.exercise-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.exercise-header h2 {
  margin: 0;
  color: #333;
}

.exercise-meta {
  color: #666;
  font-size: 14px;
}

.exercise-meta p {
  margin: 5px 0;
}

.meta-label {
  display: inline-block;
  width: 50px;
  color: #888;
  font-weight: bold;
}

.action-buttons {
  margin: 20px 0;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.question-list {
  margin-top: 20px;
}

.question-item {
  margin-bottom: 30px;
  padding: 20px;
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
}

.question-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.question-item.has-corrected {
  border-left-color: #67c23a;
}

.question-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.question-index {
  font-weight: bold;
  font-size: 16px;
  margin-right: 5px;
}

.question-result {
  margin-left: auto;
  color: #67c23a;
  font-weight: bold;
}

.question-content h3 {
  margin: 15px 0 10px;
  font-size: 16px;
  color: #333;
}

.question-desc {
  color: #666;
  font-size: 14px;
  margin-bottom: 15px;
  line-height: 1.6;
}

.question-options {
  margin: 15px 0;
}

.option-item {
  display: flex;
  align-items: flex-start;
  margin: 10px 0;
  font-size: 15px;
  line-height: 1.6;
}

.option-key {
  font-weight: bold;
  margin-right: 5px;
}

.question-textarea {
  margin: 15px 0;
}

.word-limit {
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.correct-info {
  margin-top: 20px;
}

.correct-info h4 {
  margin-top: 0;
  color: #333;
  display: flex;
  align-items: center;
}

.correct-info h4 i {
  margin-right: 8px;
}

.correct-detail {
  font-size: 14px;
  color: #555;
  line-height: 1.8;
}

.detail-label {
  display: inline-block;
  width: 60px;
  color: #888;
  font-weight: bold;
}

.time-info {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 13px;
  color: #666;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header h1 {
    margin: 10px 0;
    transform: none;
    width: 100%;
    text-align: left;
  }

  .question-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .question-result {
    margin-left: 0;
  }

  .time-info {
    flex-direction: column;
    gap: 5px;
  }
}
</style>