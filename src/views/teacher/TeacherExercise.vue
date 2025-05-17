<script setup>
import { ref, onMounted, reactive } from 'vue';
import {
  queryExerciseListApi,
  addExerciseApi,
  queryExerciseByIdApi,
  updateExerciseApi,
  deleteExerciseByIdApi,
  updateExerciseStatusApi
} from '@/api/teacher/teacherExercise';
import {
  queryTeacherClassesApi,
  queryTeacherCoursesApi,
  queryTeacherSemestersApi
} from '@/api/teacher/teacherLink';
import {queryQuestionByIdApi} from "@/api/teacher/question";
import { ElMessage, ElMessageBox } from 'element-plus';
import dayjs from 'dayjs';
import { useUserStore } from '@/stores/user';
import QuestionDialog from '@/components/QuestionManageDialog.vue';
import router from "@/router";

import {
  Edit,
  Delete,
  DocumentChecked,
  MoreFilled,
  VideoPlay,    // 进行中图标
  CircleCheck,  // 已完成图标
  Clock         // 未开始图标
} from '@element-plus/icons-vue'

// 获取状态图标的方法
const getStatusIcon = (status) => {
  const icons = [Clock, VideoPlay, CircleCheck];
  return icons[status] || MoreFilled;
};


const classesList = ref([]);
const coursesList = ref([]);
const semestersList = ref([]);
const currentSemester = ref(null);
const loading = ref(false);

const userStore = useUserStore(); // 获取userStore实例

// 状态枚举
const statusOptions = [
  { value: 0, label: '未开始' },
  { value: 1, label: '进行中' },
  { value: 2, label: '已结束' }
];

// 分页数据
const pageInfo = reactive({
  page: 1,
  pageSize: 10,
  total: 0
});

// 查询条件
const queryParams = reactive({
  teacherId: userStore.id || null, // 从userStore获取教师ID
  semesterId: null,
  exerciseName: '',
  courseName: '',
  className: '',
  startTime: null,
  endTime: null,
  status: null,
  minQuestionCount: null,
  maxQuestionCount: null
});

// 日期范围
const dateRange = ref([]);

// 练习列表
const exerciseList = ref([]);

// 题目管理弹窗相关
const questionDialogVisible = ref(false);
const selectedQuestions = ref([]); // 已选择的题目

// 获取教师关联的学期
const fetchTeacherSemesters = async () => {
  try {
    if (!userStore.id) return;

    loading.value = true;
    const result = await queryTeacherSemestersApi(userStore.id);
    if (result.code && result.data.length > 0) {
      semestersList.value = result.data;
      // 默认选择第一个学期
      currentSemester.value = result.data[0].id; // 这里改为只存储ID
      queryParams.semesterId = result.data[0].id;
    }
  } catch (error) {
    ElMessage.error('获取学期列表失败');
  } finally {
    loading.value = false;
  }
};

//获取教师关联的班级
const fetchTeacherClasses = async () => {
  try {
    if (!userStore.id || !currentSemester.value) return;

    loading.value = true;
    const result = await queryTeacherClassesApi({
      teacherId: userStore.id,
      semesterId: currentSemester.value
    });
    if (result.code) {
      classesList.value = result.data;
    }
  } catch (error) {
    ElMessage.error('获取班级列表失败');
  } finally {
    loading.value = false;
  }
};

// 获取教师关联的课程
const fetchTeacherCourses = async () => {
  try {
    if (!userStore.id || !currentSemester.value) return;

    loading.value = true;
    const result = await queryTeacherCoursesApi({
      teacherId: userStore.id,
      semesterId: currentSemester.value
    });
    if (result.code) {
      coursesList.value = result.data;
    }
  } catch (error) {
    ElMessage.error('获取课程列表失败');
  } finally {
    loading.value = false;
  }
};

// 学期变化时的处理
const handleSemesterChange = (semesterId) => {
  currentSemester.value = semesterId;
  queryParams.semesterId = semesterId;
  // 重新获取班级和课程
  fetchTeacherClasses();
  fetchTeacherCourses();
  // 重新查询练习列表
  search();
};

// 查询练习列表
const search = async () => {
  // 确保有学期ID
  if (!queryParams.semesterId) {
    ElMessage.warning('请先选择学期');
    return;
  }

  // 处理日期范围
  if (dateRange.value && dateRange.value.length === 2) {
    queryParams.startTime = dayjs(dateRange.value[0]).format('YYYY-MM-DD HH:mm:ss');
    queryParams.endTime = dayjs(dateRange.value[1]).format('YYYY-MM-DD HH:mm:ss');
  } else {
    queryParams.startTime = null;
    queryParams.endTime = null;
  }

  const params = {
    ...queryParams,
    page: pageInfo.page,
    pageSize: pageInfo.pageSize
  };

  const result = await queryExerciseListApi(params);
  if (result.code) {
    // 合并同一个练习的不同班级
    exerciseList.value = mergeClassNames(result.data.list);
    pageInfo.total = result.data.total;
  }
};

// 分页变化
const handleCurrentChange = (val) => {
  pageInfo.page = val;
  search();
};

// 每页条数变化
const handleSizeChange = (val) => {
  pageInfo.pageSize = val;
  search();
};

// Dialog对话框
const dialogFormVisible = ref(false);
const formTitle = ref('');
const exerciseForm = reactive({
  id: null,
  name: '',
  courseId: null,
  startTime: null,
  endTime: null,
  creatorId: userStore.id, // 从userStore获取创建者ID
  classIds: [],
  questionIds: [],
  questionScores: [],
  questions: [] // 新增字段，存储完整的题目信息
});

// 重置表单
const resetForm = () => {
  exerciseForm.id = null;
  exerciseForm.name = '';
  exerciseForm.courseId = null;
  exerciseForm.startTime = null;
  exerciseForm.endTime = null;
  exerciseForm.classIds = [];
  exerciseForm.questionIds = [];
  exerciseForm.questionScores = [];
  exerciseForm.questions = [];
  selectedQuestions.value = [];

  if (exerciseFormRef.value) {
    exerciseFormRef.value.resetFields();
  }
  search();
};

// 新增练习
const addExercise = () => {
  dialogFormVisible.value = true;
  formTitle.value = '新增练习';
  resetForm();
};

// 编辑练习
const editExercise = async (id) => {
  const result = await queryExerciseByIdApi(id);
  if (result.code) {
    dialogFormVisible.value = true;
    formTitle.value = '修改练习';

    const data = result.data;
    exerciseForm.id = data.id;
    exerciseForm.name = data.name;
    exerciseForm.courseId = data.courseId;
    exerciseForm.startTime = data.startTime;
    exerciseForm.endTime = data.endTime;
    exerciseForm.creatorId = data.creatorId;
    exerciseForm.classIds = data.classIds || [];
    exerciseForm.questionIds = data.questionIds || [];
    exerciseForm.questionScores = data.questionScores || [];

    // 初始化questions数组为空
    exerciseForm.questions = [];

    // 如果有题目ID，则查询题目详情
    if (data.questionIds && data.questionIds.length > 0) {
      try {
        // 查询所有题目详情
        const questionDetails = await Promise.all(
            data.questionIds.map(id => queryQuestionByIdApi(id))
        );

        // 过滤出查询成功的题目
        exerciseForm.questions = questionDetails
            .filter(res => res.code)
            .map(res => res.data);

        // 如果查询到的题目数量与ID数量不一致，显示警告
        if (exerciseForm.questions.length !== data.questionIds.length) {
          ElMessage.warning('部分题目信息获取失败');
        }
      } catch (error) {
        ElMessage.error('获取题目信息失败');
      }
    }

    // 初始化已选择的题目
    selectedQuestions.value = data.questionIds || [];
  }
};

// 保存练习
const saveExercise = async () => {
  if (!exerciseFormRef.value) return;

  await exerciseFormRef.value.validate(async (valid) => {
    if (valid) {
      // 确保有学期ID
      if (!queryParams.semesterId) {
        ElMessage.warning('请先选择学期');
        return;
      }

      let result;
      const formData = {
        ...exerciseForm,
        semesterId: queryParams.semesterId
      };

      // 确保creatorId是最新的
      formData.creatorId = userStore.id;

      // 转换时间格式
      formData.startTime = dayjs(formData.startTime).format('YYYY-MM-DDTHH:mm:ss');
      formData.endTime = dayjs(formData.endTime).format('YYYY-MM-DDTHH:mm:ss');

      if (formData.id) {
        result = await updateExerciseApi(formData);
      } else {
        result = await addExerciseApi(formData);
      }

      if (result.code) {
        ElMessage.success('操作成功');
        dialogFormVisible.value = false;
        await search();
      } else {
        ElMessage.error(result.msg);
      }
    }
  });
};

// 删除练习
const deleteExercise = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该练习吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    const result = await deleteExerciseByIdApi(id);
    if (result.code) {
      ElMessage.success('删除成功');
      await search();
    } else {
      ElMessage.error(result.msg);
    }
  } catch {
    ElMessage.info('已取消删除');
  }
};

// 修改练习状态
const changeStatus = async (id, status) => {
  const statusText = ['未开始', '进行中', '已结束'][status];

  try {
    await ElMessageBox.confirm(`确定要将练习状态修改为"${statusText}"吗?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    const result = await updateExerciseStatusApi({ id, status });
    if (result.code) {
      ElMessage.success('状态修改成功');
      await search();
    } else {
      ElMessage.error(result.msg);
    }
  } catch {
    ElMessage.info('已取消操作');
  }
};

// 处理题目选择确认
const handleQuestionConfirm = (questions) => {
  // 检查是否选择了题目
  if (questions.length === 0) {
    ElMessage.warning('请至少选择一道题目');
    return;
  }

  // 合并已存在的题目和新选择的题目
  const existingQuestionIds = exerciseForm.questions.map(q => q.id);
  const newQuestions = questions.filter(q => !existingQuestionIds.includes(q.id));

  // 如果没有新增题目
  if (newQuestions.length === 0) {
    ElMessage.warning('没有新增题目');
    questionDialogVisible.value = false;
    return;
  }

  // 添加新题目到现有列表
  exerciseForm.questions = [...exerciseForm.questions, ...newQuestions];

  // 更新questionIds
  exerciseForm.questionIds = exerciseForm.questions.map(q => q.id);

  // 从base_question中获取分数信息，作为新题目的初始分值
  const newScores = newQuestions.map(q => q.score);
  exerciseForm.questionScores = [...exerciseForm.questionScores, ...newScores];

  ElMessage.success(`成功添加 ${newQuestions.length} 道题目`);
  questionDialogVisible.value = false;
};

// 合并班级
const mergeClassNames = (exercises) => {
  const merged = {};
  exercises.forEach(ex => {
    if (!merged[ex.exerciseId]) {
      merged[ex.exerciseId] = {
        ...ex,
        classNames: [ex.className],
        classIds: [ex.classId]
      };
    } else {
      if (!merged[ex.exerciseId].classNames.includes(ex.className)) {
        merged[ex.exerciseId].classNames.push(ex.className);
        merged[ex.exerciseId].classIds.push(ex.classId);
      }
    }
  });
  return Object.values(merged);
};

// 打开题目管理弹窗
const openQuestionDialog = () => {
  questionDialogVisible.value = true;
};

// 删除单个题目
const removeQuestion = (index) => {
  exerciseForm.questions.splice(index, 1);
  exerciseForm.questionIds.splice(index, 1);
  exerciseForm.questionScores.splice(index, 1);
};

// 表单校验规则
const rules = reactive({
  name: [
    { required: true, message: '练习名称不能为空', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2到50个字符', trigger: 'blur' }
  ],
  courseId: [
    { required: true, message: '请选择课程', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (exerciseForm.startTime && new Date(value) <= new Date(exerciseForm.startTime)) {
          callback(new Error('结束时间必须晚于开始时间'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ],
  classIds: [
    { required: true, message: '请至少选择一个班级', trigger: 'change' }
  ],
  questionIds: [
    { required: true, message: '请至少选择一个题目', trigger: 'change' }
  ]
});

const exerciseFormRef = ref();

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss');
};

// 获取状态标签类型
const getStatusType = (status) => {
  const types = ['info', 'primary', 'success'];
  return types[status] || '';
};

// 添加跳转批改页面的方法
const navigateToGrading = (exerciseId) => {
  // 这里使用router跳转，请确保已导入useRouter
  router.push({
    path: '/teacher/grading',
    query: { exerciseId }
  });
};

// 钩子函数
onMounted(() => {
  fetchTeacherSemesters().then(() => {
    search();
    fetchTeacherClasses();
    fetchTeacherCourses();
  });
});
</script>

<template>
  <h1>教师练习管理</h1>

  <!-- 查询条件 -->
  <div class="container">
    <el-card>
      <el-form :inline="true" :model="queryParams" class="query-form">
        <el-form-item label="学期" style="width: 280px">
          <el-select
              v-model="currentSemester"
              placeholder="请选择学期"
              @change="handleSemesterChange"
              :loading="loading"
              style="width: 100%"
              filterable
          >
            <el-option
                v-for="semester in semestersList"
                :key="semester.id"
                :label="semester.name"
                :value="semester.id"
            >
              <div style="display: flex; justify-content: space-between; width: 100%">
                <span style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap">
                  {{ semester.name }}
                </span>
                <span style="color: #8492a6; font-size: 12px; margin-left: 10px">
                  {{ dayjs(semester.startTime).format('YYYY-MM-DD') }}至{{ dayjs(semester.endTime).format('MM-DD') }}
                </span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="练习名称" style="width: 280px">
          <el-input v-model="queryParams.exerciseName" placeholder="请输入练习名称" clearable />
        </el-form-item>

        <el-form-item label="课程名称" style="width: 200px">
          <el-input v-model="queryParams.courseName" placeholder="请输入课程名称" clearable />
        </el-form-item>

        <el-form-item label="班级名称" style="width: 200px">
          <el-input v-model="queryParams.className" placeholder="请输入班级名称" clearable />
        </el-form-item>

        <el-form-item label="时间范围" style="width: 200px">
          <el-date-picker
              v-model="dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="状态" style="width: 360px">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="() => {
            dateRange.value = [];
            Object.keys(queryParams).forEach(key => {
              if (key !== 'teacherId') {
                queryParams[key] = null;
              }
            });
          }">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>

  <!-- 操作按钮 -->
  <div class="container">
    <el-button type="primary" @click="addExercise">+ 新增练习</el-button>
  </div>

  <!-- 练习列表 -->
  <div class="container">
    <el-table
        :data="exerciseList"
        border
        style="width: 100%"
        v-loading="loading"
    >
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="exerciseName" label="练习名称" width="180" align="center" />
      <el-table-column prop="courseName" label="课程名称" width="150" align="center" />
      <el-table-column prop="classNames" label="班级" width="180" align="center">
        <template #default="{row}">
          <el-tooltip effect="dark" placement="top">
            <template #content>
              <div v-for="className in row.classNames" :key="className">{{ className }}</div>
            </template>
            <el-tag>{{ row.classNames.length }}个班级</el-tag>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="时间范围" width="300" align="center">
        <template #default="{row}">
          <div>开始: {{ formatTime(row.startTime) }}</div>
          <div>结束: {{ formatTime(row.endTime) }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="questionCount" label="题目数" width="80" align="center" />
      <el-table-column prop="submittedStudentCount" label="提交人数" width="90" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{row}">
          <el-tag :type="getStatusType(row.status)">
            {{ statusOptions.find(item => item.value === row.status)?.label || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="exerciseCreatedAt" label="创建时间" width="180" align="center">
        <template #default="{row}">
          {{ formatTime(row.exerciseCreatedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" align="center" fixed="right">
        <template #default="{row}">
          <div class="action-buttons">
            <!-- 主要操作保持文字按钮 -->
            <el-button
                type="primary"
                size="small"
                @click="editExercise(row.exerciseId)"
            >
              编辑
            </el-button>

            <el-button
                type="success"
                size="small"
                @click="navigateToGrading(row.exerciseId)"
                :disabled="row.status !== 1"
            >
              批改
            </el-button>

            <!-- 状态和删除操作放入下拉菜单 -->
            <el-dropdown>
              <el-button type="info" size="small">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                      v-for="item in statusOptions"
                      :key="item.value"
                      @click="changeStatus(row.exerciseId, item.value)"
                      :disabled="row.status === item.value"
                  >
                    <el-icon>
                      <component :is="getStatusIcon(item.value)" />
                    </el-icon>
                    {{ item.label }}
                  </el-dropdown-item>
                  <el-dropdown-item
                      @click="deleteExercise(row.exerciseId)"
                      :disabled="row.status === 1"
                      class="danger-item"
                  >
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <!-- 分页 -->
  <div class="container">
    <el-pagination
        v-model:current-page="pageInfo.page"
        v-model:page-size="pageInfo.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pageInfo.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
    />
  </div>

  <!-- 新增/编辑对话框 -->
  <el-dialog v-model="dialogFormVisible" :title="formTitle" width="800px">
    <el-form :model="exerciseForm" :rules="rules" ref="exerciseFormRef" label-width="100px">
      <el-row>
        <el-col :span="12">
          <el-form-item label="练习名称" prop="name">
            <el-input v-model="exerciseForm.name" placeholder="请输入练习名称" />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="所属课程" prop="courseId">
            <el-select
                v-model="exerciseForm.courseId"
                placeholder="请选择课程"
                style="width: 100%"
                :loading="loading"
            >
              <el-option
                  v-for="course in coursesList"
                  :key="course.id"
                  :label="course.name"
                  :value="course.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="12">
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
                v-model="exerciseForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker
                v-model="exerciseForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="关联班级" prop="classIds">
        <el-select
            v-model="exerciseForm.classIds"
            multiple
            placeholder="请选择班级"
            style="width: 100%"
            :loading="loading"
        >
          <el-option
              v-for="classItem in classesList"
              :key="classItem.id"
              :label="classItem.name"
              :value="classItem.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="关联题目" prop="questionIds">
        <div style="width: 100%; border: 1px solid #dcdfe6; border-radius: 4px; padding: 10px;">
          <div v-for="(question, index) in exerciseForm.questions" :key="question.id" style="margin-bottom: 10px;">
            <el-row :gutter="10" align="middle">
              <el-col :span="18">
                <div>
                  <el-tag :type="question.type === 0 ? 'success' : question.type === 1 ? 'warning' : 'info'" size="small">
                    {{ question.type === 0 ? '单选' : question.type === 1 ? '多选' : '简答' }}
                  </el-tag>
                  <span style="margin-left: 10px">{{ question.content }}</span>
                </div>
                <div style="color: #909399; font-size: 12px; margin-top: 5px;">
                  难度: <el-rate
                    v-model="question.difficulty"
                    disabled
                    show-score
                    :max="5"
                />
                </div>
              </el-col>
              <el-col :span="4">
                <el-input-number
                    v-model="exerciseForm.questionScores[index]"
                    :min="1"
                    :max="100"
                    placeholder="分值"
                    style="width: 100%"
                />
              </el-col>
              <el-col :span="2">
                <el-button
                    type="danger"
                    circle
                    @click="() => removeQuestion(index)"
                >
                  <el-icon><delete /></el-icon>
                </el-button>
              </el-col>
            </el-row>
          </div>

          <el-button
              type="primary"
              plain
              @click="openQuestionDialog"
          >
            + 添加题目
          </el-button>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="saveExercise">确定</el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 题目管理弹窗 -->
  <QuestionDialog
      v-model:visible="questionDialogVisible"
      :teacher-id="userStore.id"
      :multiple="true"
      :selected-questions="selectedQuestions"
      @confirm="handleQuestionConfirm"
  />
</template>

<style scoped>
.container {
  margin: 15px 0;
}

.query-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.el-table {
  margin-top: 15px;
}

.el-pagination {
  justify-content: center;
}

/* 优化下拉选项样式 */
.el-select-dropdown__item {
  height: auto;
  padding: 8px 20px;
  line-height: 1.5;
}

/* 确保下拉框足够宽 */
.el-select-dropdown {
  min-width: 400px !important;
}

/* 调整表单元素间距 */
.el-form-item {
  margin-bottom: 10px;
}

/* 题目列表样式 */
.question-item {
  padding: 10px;
  border-bottom: 1px solid #ebeef5;
}

.question-content {
  margin-left: 10px;
}

.question-meta {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>