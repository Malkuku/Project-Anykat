<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { useUserStore } from '@/stores/user';
import {
  queryGradingDetailsApi,
  queryGradingQuestionsApi,
  queryQuestionDetailsApi,
  updateCorrectionApi
} from '@/api/teacher/teacherGrading';
import { ElMessage } from 'element-plus';

const route = useRoute();
const userStore = useUserStore();

// 是否只显示需要批改的勾选状态
const onlyShowUncorrected = ref(false);

// 获取路由参数中的练习ID
const exerciseId = computed(() => route.params.exerciseId);
// 获取当前教师ID
const teacherId = computed(() => userStore.id);

// 查询表单
const queryForm = ref({
  className: '',
  studentName: '',
  page: 1,
  pageSize: 10
});

// 查询结果
const gradingList = ref([]);
const total = ref(0);

// 计算属性：过滤后的列表
const filteredGradingList = computed(() => {
  if (!onlyShowUncorrected.value) return gradingList.value;
  return gradingList.value.filter(item => item.submittedUncorrectedCount > 0);
});

// 查询批改详情
const search = async () => {
  if (!teacherId.value || !exerciseId.value) return;

  const params = {
    teacherId: teacherId.value,
    exerciseId: exerciseId.value,
    ...queryForm.value
  };

  const result = await queryGradingDetailsApi(params);
  if (result.code) {
    gradingList.value = result.data.list.map(item => ({
      ...item,
      // 处理班级名称显示
      classNames: item.classNames.includes(',')
          ? `${item.classNames.split(',')[0].trim()} 等${item.classNames.split(',').length}个班级`
          : item.classNames
    }));
    total.value = result.data.total;
  }
};

// 重置查询
const resetQuery = () => {
  queryForm.value = {
    className: '',
    studentName: '',
    page: 1,
    pageSize: 10
  };
  search();
};

// 分页变化
const handleCurrentChange = (val) => {
  queryForm.value.page = val;
  search();
};

// 每页条数变化
const handleSizeChange = (val) => {
  queryForm.value.pageSize = val;
  search();
};

// 查看题目批改情况
const questionDialogVisible = ref(false);
const questionList = ref([]);
const currentStudent = ref({});

const viewQuestions = async (student) => {
  currentStudent.value = student;
  const params = {
    exerciseId: exerciseId.value,
    studentId: student.studentId
  };

  const result = await queryGradingQuestionsApi(params);
  if (result.code) {
    questionList.value = result.data.filter(q => q.correctStatus !== 0);
    questionDialogVisible.value = true;
  }
};

// 查看题目详情
const detailDialogVisible = ref(false);
const questionDetail = ref({});
const correctionForm = ref({
  id: '',
  score: '',
  correctStatus: 2,
  correctComment: ''
});

const viewQuestionDetail = async (question) => {
  const params = {
    exerciseId: exerciseId.value,
    studentId: currentStudent.value.studentId,
    questionId: question.questionId
  };

  const result = await queryQuestionDetailsApi(params);
  if (result.code) {
    questionDetail.value = result.data;
    correctionForm.value = {
      id: result.data.answerId,
      score: result.data.currentScore || '',
      correctComment: ''
    };
    detailDialogVisible.value = true;
  }
};

// 保存批改
const saveCorrection = async () => {
  if (!correctionForm.value.id || correctionForm.value.score === '') {
    ElMessage.warning('请填写批改分数');
    return;
  }

  const params = {
    ...correctionForm.value,
    correctStatus: 2
  };

  const result = await updateCorrectionApi(params);
  if (result.code) {
    ElMessage.success('批改信息更新成功');
    detailDialogVisible.value = false;
    await viewQuestions(currentStudent.value);
    await search();
  }
};

// 初始化查询
onMounted(() => {
  if (teacherId.value && exerciseId.value) {
    search();
  }
});
</script>

<template>
  <h1>练习批改管理</h1>

  <!-- 查询表单 -->
  <div class="container">
    <el-form :inline="true" :model="queryForm" class="demo-form-inline">
      <el-form-item label="班级名称">
        <el-input v-model="queryForm.className" placeholder="班级名称" clearable />
      </el-form-item>
      <el-form-item label="学生姓名">
        <el-input v-model="queryForm.studentName" placeholder="学生姓名" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="filter-container">
      <el-checkbox v-model="onlyShowUncorrected">只显示需要批改的部分</el-checkbox>
    </div>
  </div>

  <!-- 批改列表 -->
  <div class="container">
    <el-table :data="filteredGradingList" border style="width: 100%">
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="classNames" label="班级" width="180" align="center" />
      <el-table-column prop="studentName" label="学生" width="120" align="center" />
      <el-table-column label="答题情况" align="center">
        <template #default="{row}">
          <el-tag type="info">{{ row.answeredQuestions }}/{{ row.totalQuestions }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="批改状态" align="center">
        <template #default="{row}">
          <el-tag type="warning">未提交:{{ row.savedUnsubmittedCount }}</el-tag>
          <el-tag type="danger">待批改:{{ row.submittedUncorrectedCount }}</el-tag>
          <el-tag type="success">已批改:{{ row.correctedCount }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="分数" align="center">
        <template #default="{row}">
          <el-tag :type="row.currentScore >= row.maxScore * 0.6 ? 'success' : 'danger'">
            {{ row.currentScore }}/{{ row.maxScore }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastSubmitTime" label="最后提交时间" width="180" align="center" />
      <el-table-column prop="lastCorrectTime" label="最后批改时间" width="180" align="center" />
      <el-table-column label="操作" width="150" align="center">
        <template #default="{row}">
          <el-button
              type="primary"
              size="small"
              @click="viewQuestions(row)"
          >
            批改
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>
  </div>

  <!-- 题目批改对话框 -->
  <el-dialog v-model="questionDialogVisible" :title="`${currentStudent.studentName} - 题目批改`" width="80%">
    <el-table :data="questionList" border style="width: 100%">
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="questionName" label="题目名称" align="center" />
      <el-table-column label="批改状态" width="120" align="center">
        <template #default="{row}">
          <el-tag :type="row.correctStatus === 2 ? 'success' : 'danger'">
            {{ row.correctStatus === 2 ? '已批改' : '未批改' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="gradingScore" label="得分" width="100" align="center">
        <template #default="{row}">
          {{ row.gradingScore || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center">
        <template #default="{row}">
          <el-button
              type="primary"
              size="small"
              @click="viewQuestionDetail(row)"
          >
            {{ row.correctStatus === 2 ? '更正批改' : '批改' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>

  <!-- 题目详情批改对话框 -->
  <el-dialog v-model="detailDialogVisible" :title="`${currentStudent.studentName} - 题目批改`" width="70%">
    <el-descriptions :column="2" border>
      <el-descriptions-item label="题目类型">
        <el-tag>
          {{ questionDetail.questionType === 0 ? '单选题' :
            questionDetail.questionType === 1 ? '多选题' : '简答题' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="满分">{{ questionDetail.maxScore }}</el-descriptions-item>
      <el-descriptions-item label="题目内容" :span="2">
        {{ questionDetail.questionContent }}
      </el-descriptions-item>
    </el-descriptions>

    <el-divider />

    <h3>学生答案</h3>
    <div class="answer-content">{{ questionDetail.studentAnswer }}</div>

    <el-divider />

    <h3>参考答案</h3>
    <div class="answer-content">{{ questionDetail.referenceAnswer }}</div>

    <el-divider />

    <h3>批改</h3>
    <el-form :model="correctionForm" label-width="100px">
      <el-form-item label="批改分数" required>
        <el-input-number
            v-model="correctionForm.score"
            :min="0"
            :max="questionDetail.maxScore"
            placeholder="请输入分数"
        />
        <span style="margin-left: 10px;">/ {{ questionDetail.maxScore }}</span>
      </el-form-item>
      <el-form-item label="批改备注">
        <el-input
            v-model="correctionForm.correctComment"
            type="textarea"
            :rows="3"
            placeholder="请输入批改备注"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="detailDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCorrection">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.container {
  margin: 15px 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.answer-content {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  white-space: pre-wrap;
}

.filter-container {
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style>