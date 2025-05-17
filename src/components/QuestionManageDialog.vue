<script setup>
import {ref, onMounted,defineProps, defineEmits, watch, nextTick} from 'vue';
import {
  queryQuestionsApi,
  deleteQuestionsApi,
  addQuestionApi,
  queryQuestionByIdApi,
  updateQuestionApi,
  queryChoiceByQuestionIdApi,
  addChoiceApi,
  updateChoiceApi,
  querySubjectiveByQuestionIdApi,
  addSubjectiveApi,
  updateSubjectiveApi
} from '@/api/teacher/question';
import { ElMessage, ElMessageBox } from 'element-plus';

// loading状态
const loading = ref(false);
const tableLoading = ref(false);
const dialogLoading = ref(false);

const props = defineProps({
  // 是否显示弹窗
  visible: {
    type: Boolean,
    default: false
  },
  // 教师ID
  teacherId: {
    type: Number,
    required: true
  },
  // 是否多选模式
  multiple: {
    type: Boolean,
    default: false
  },
  // 已选择的题目ID数组
  selectedQuestions: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['update:visible', 'update:selectedQuestions', 'confirm']);

// 题目类型选项
const typeOptions = [
  { value: 0, label: '单选题' },
  { value: 1, label: '多选题' },
  { value: 2, label: '简答题' }
];

// 难度选项
const difficultyOptions = [
  { value: 1, label: '1星' },
  { value: 2, label: '2星' },
  { value: 3, label: '3星' },
  { value: 4, label: '4星' },
  { value: 5, label: '5星' }
];

// 查询条件
const searchForm = ref({
  type: null,
  description: '',
  content: '',
  minDifficulty: null,
  maxDifficulty: null,
  minScore: null,
  maxScore: null,
  page: 1,
  pageSize: 10
});

// 题目列表
const questionList = ref([]);
const total = ref(0);
const questionTable = ref();

// 查询题目
const search = async () => {
  tableLoading.value = true;
  try {
    const result = await queryQuestionsApi(searchForm.value);
    if(result.code){
      questionList.value = result.data.list;
      total.value = result.data.total;
    }
  } catch (error) {
    ElMessage.error('获取题目列表失败');
  } finally {
    tableLoading.value = false;
  }
};

// 重置查询条件
const resetSearch = () => {
  searchForm.value = {
    type: null,
    description: '',
    content: '',
    minDifficulty: null,
    maxDifficulty: null,
    minScore: null,
    maxScore: null,
    page: 1,
    pageSize: 10
  };
  search();
};

// 分页变化
const handleCurrentChange = (val) => {
  searchForm.value.page = val;
  search();
};

// Dialog对话框
const dialogFormVisible = ref(false);
const formTitle = ref('');
const questionForm = ref({
  id: null,
  type: null,
  description: '',
  content: '',
  difficulty: 1,
  score: 0,
  creatorId: props.teacherId
});

// 选择题表单
const choiceForm = ref({
  id: null,
  questionId: null,
  isMulti: false,
  options: {
    A: '',
    B: '',
    C: '',
    D: ''
  },
  correctAnswer: '',
  analysis: ''
});

// 简答题表单
const subjectiveForm = ref({
  id: null,
  questionId: null,
  referenceAnswer: '',
  wordLimit: 500
});

// 当前活动tab
const activeTab = ref('basic');

// 表单校验规则
const rules = ref({
  type: [{ required: true, message: '请选择题型', trigger: 'change' }],
  content: [{ required: true, message: '请输入题干内容', trigger: 'blur' }],
  creatorId: [{ required: true, message: '创建人不能为空', trigger: 'blur' }]
});

const choiceRules = ref({
  options: {
    A: [{ required: true, message: '请输入选项A', trigger: 'blur' }],
    B: [{ required: true, message: '请输入选项B', trigger: 'blur' }],
    C: [{ required: true, message: '请输入选项C', trigger: 'blur' }],
    D: [{ required: true, message: '请输入选项D', trigger: 'blur' }]
  },
  correctAnswer: [{ required: true, message: '请选择正确答案', trigger: 'change' }]
});

const subjectiveRules = ref({
  referenceAnswer: [{ required: true, message: '请输入参考答案', trigger: 'blur' }]
});

const questionFormRef = ref();
const choiceFormRef = ref();
const subjectiveFormRef = ref();

// 新增题目
const addQuestion = () => {
  dialogFormVisible.value = true;
  formTitle.value = '新增题目';
  activeTab.value = 'basic';
  questionForm.value = {
    id: null,
    type: null,
    description: '',
    content: '',
    difficulty: 1,
    score: 0,
    creatorId: props.teacherId
  };
  choiceForm.value = {
    id: null,
    questionId: null,
    isMulti: false,
    options: {
      A: '',
      B: '',
      C: '',
      D: ''
    },
    correctAnswer: '',
    analysis: ''
  };
  subjectiveForm.value = {
    id: null,
    questionId: null,
    referenceAnswer: '',
    wordLimit: 500
  };

  if (questionFormRef.value) {
    questionFormRef.value.resetFields();
  }
};

// 编辑题目
const editQuestion = async (id) => {
  dialogLoading.value = true;
  try {
    const result = await queryQuestionByIdApi(id);
    if(result.code){
      dialogFormVisible.value = true;
      formTitle.value = '编辑题目';
      activeTab.value = 'basic';
      questionForm.value = result.data;

      // 根据题型加载扩展信息
      if (result.data.type === 0 || result.data.type === 1) {
        const choiceResult = await queryChoiceByQuestionIdApi(id);
        if (choiceResult.code) {
          choiceForm.value = choiceResult.data;
        }
      } else if (result.data.type === 2) {
        const subjectiveResult = await querySubjectiveByQuestionIdApi(id);
        if (subjectiveResult.code) {
          subjectiveForm.value = subjectiveResult.data;
        }
      }
    }
  } catch (error) {
    ElMessage.error('获取题目详情失败');
  } finally {
    dialogLoading.value = false;
  }
};

// 保存题目
const saveQuestion = async () => {
  if (!questionFormRef.value) return;

  dialogLoading.value = true;
  try {
    await questionFormRef.value.validate(async (valid) => {
      if (valid) {
        let result;
        if (questionForm.value.id) {
          // 更新题目
          result = await updateQuestionApi(questionForm.value);
        } else {
          // 新增题目
          questionForm.value.creatorId = props.teacherId;
          result = await addQuestionApi(questionForm.value);
        }

        if (result.code) {
          const questionId = questionForm.value.id || result.data.id;

          // 根据题型保存扩展信息
          if (questionForm.value.type === 0 || questionForm.value.type === 1) {
            await saveChoice(questionId);
          } else if (questionForm.value.type === 2) {
            await saveSubjective(questionId);
          }

          ElMessage.success('操作成功');
          dialogFormVisible.value = false;
          await search();
        } else {
          ElMessage.error(result.msg);
        }
      }
    });
  } catch (error) {
    ElMessage.error('保存题目失败');
  } finally {
    dialogLoading.value = false;
  }
};

// 保存选择题
const saveChoice = async (questionId) => {
  if (!choiceFormRef.value) return;

  await choiceFormRef.value.validate(async (valid) => {
    if (valid) {
      let result;
      if (choiceForm.value.id) {
        // 更新选择题
        result = await updateChoiceApi(choiceForm.value);
      } else {
        // 新增选择题
        const formData = {
          ...choiceForm.value,
          questionId: questionId
        };
        result = await addChoiceApi(formData);
      }

      if (!result.code) {
        ElMessage.error(result.msg);
      }
    }
  });
};

// 保存简答题
const saveSubjective = async (questionId) => {
  if (!subjectiveFormRef.value) return;

  await subjectiveFormRef.value.validate(async (valid) => {
    if (valid) {
      let result;
      if (subjectiveForm.value.id) {
        // 更新简答题
        result = await updateSubjectiveApi(subjectiveForm.value);
      } else {
        // 新增简答题
        const formData = {
          ...subjectiveForm.value,
          questionId: questionId
        };
        result = await addSubjectiveApi(formData);
      }

      if (!result.code) {
        ElMessage.error(result.msg);
      }
    }
  });
};

// 删除题目
const deleteQuestion = async (id) => {
  loading.value = true;
  try {
    await ElMessageBox.confirm('确定删除该题目吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    const result = await deleteQuestionsApi(id);
    if (result.code) {
      ElMessage.success('删除成功');
      await search();
    } else {
      ElMessage.error(result.msg);
    }
  } catch {
    ElMessage.info('已取消删除');
  } finally {
    loading.value = false;
  }
};

// 多选
const selectedQuestions = ref([...props.selectedQuestions]);
const handleSelectionChange = (val) => {
  selectedQuestions.value = val.map(item => item.id);
  emit('update:selectedQuestions', selectedQuestions.value);
};

// 确认选择
const confirmSelection = () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请至少选择一道题目');
    return;
  }

  const selectedQuestionsInfo = questionList.value
      .filter(item => selectedQuestions.value.includes(item.id))
      .map(item => ({
        id: item.id,
        type: item.type,
        description: item.description,
        content: item.content,
        difficulty: item.difficulty,
        score: item.score
      }));

  emit('confirm', selectedQuestionsInfo);
  emit('update:visible', false);
};

// 钩子函数
onMounted(() => {
  search();
});

// 监听visible变化
watch(() => props.visible, (val) => {
  if (val) {
    search();
  }
});

// 监听selectedQuestions变化
watch(() => props.selectedQuestions, (newVal) => {
  if (questionList.value && questionList.value.length > 0) {
    nextTick(() => {
      // 清除所有选中状态
      questionTable.value.clearSelection();

      // 设置新的选中状态
      questionList.value.forEach(row => {
        if (newVal.includes(row.id)) {
          questionTable.value.toggleRowSelection(row, true);
        }
      });
    });
  }
}, { deep: true });

// 监听questionList变化
watch(() => questionList.value, (newVal) => {
  if (newVal && newVal.length > 0) {
    nextTick(() => {
      // 清除所有选中状态
      questionTable.value.clearSelection();

      // 设置新的选中状态
      questionList.value.forEach(row => {
        if (props.selectedQuestions.includes(row.id)) {
          questionTable.value.toggleRowSelection(row, true);
        }
      });
    });
  }
}, { deep: true });
</script>

<template>
  <el-dialog
      v-model="props.visible"
      title="题目管理"
      width="80%"
      top="5vh"
      @close="emit('update:visible', false)"
  >
    <!-- 查询表单 -->
    <div class="container">
      <el-card>
        <el-form :model="searchForm" label-width="80px">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="题型">
                <el-select v-model="searchForm.type" placeholder="请选择题型" clearable>
                  <el-option
                      v-for="item in typeOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="题目描述">
                <el-input v-model="searchForm.description" placeholder="请输入题目描述" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="题干内容">
                <el-input v-model="searchForm.content" placeholder="请输入题干内容" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="难度范围">
                <div style="display: flex; align-items: center">
                  <el-select v-model="searchForm.minDifficulty" placeholder="最小" clearable style="width: 100px">
                    <el-option
                        v-for="item in difficultyOptions"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                  </el-select>
                  <span style="margin: 0 5px">-</span>
                  <el-select v-model="searchForm.maxDifficulty" placeholder="最大" clearable style="width: 100px">
                    <el-option
                        v-for="item in difficultyOptions"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                  </el-select>
                </div>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="分值范围">
                <div style="display: flex; align-items: center">
                  <el-input-number v-model="searchForm.minScore" :min="0" placeholder="最小" controls-position="right" style="width: 100px" />
                  <span style="margin: 0 5px">-</span>
                  <el-input-number v-model="searchForm.maxScore" :min="0" placeholder="最大" controls-position="right" style="width: 100px" />
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="6" :offset="12">
              <div style="text-align: right">
                <el-button type="primary" @click="search">查询</el-button>
                <el-button @click="resetSearch">重置</el-button>
              </div>
            </el-col>
          </el-row>
        </el-form>
      </el-card>
    </div>

    <!-- 操作按钮 -->
    <div class="container">
      <el-button type="primary" @click="addQuestion" :loading="loading">
        <template #loading>
          <span>新增中...</span>
        </template>
        + 新增题目
      </el-button>
      <el-button
          type="danger"
          @click="deleteQuestion"
          :disabled="selectedQuestions.length === 0"
          :loading="loading"
      >
        删除选中
      </el-button>
      <el-button
          type="success"
          @click="confirmSelection"
          :disabled="selectedQuestions.length === 0"
          style="float: right"
          :loading="loading"
      >
        确认选择({{ selectedQuestions.length }})
      </el-button>
    </div>

    <!-- 表格 -->
    <div class="container">
      <el-table
          :data="questionList"
          border
          style="width: 100%"
          @selection-change="handleSelectionChange"
          row-key="id"
          ref="questionTable"
          v-loading="tableLoading"
          element-loading-text="加载中..."
          element-loading-spinner="el-icon-loading"
          element-loading-background="rgba(255, 255, 255, 0.7)"
      >
        <el-table-column v-if="props.multiple" type="selection" width="55" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="type" label="题型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 0 ? 'success' : row.type === 1 ? 'warning' : 'info'">
              {{ row.type === 0 ? '单选题' : row.type === 1 ? '多选题' : '简答题' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="题目描述" width="200" show-overflow-tooltip />
        <el-table-column prop="content" label="题干内容" show-overflow-tooltip />
        <el-table-column prop="difficulty" label="难度" width="100" align="center">
          <template #default="{ row }">
            <el-rate v-model="row.difficulty" disabled show-score :max="5" />
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="80" align="center" />
        <el-table-column prop="updatedAt" label="更新时间" width="180" align="center" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editQuestion(row.id)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteQuestion(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="container" style="text-align: right">
      <el-pagination
          v-model:current-page="searchForm.page"
          v-model:page-size="searchForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handleCurrentChange"
          @size-change="search"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
        v-model="dialogFormVisible"
        :title="formTitle"
        width="80%"
        v-loading="dialogLoading"
        element-loading-text="处理中..."
        element-loading-spinner="el-icon-loading"
        element-loading-background="rgba(255, 255, 255, 0.7)"
    >
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-form
              ref="questionFormRef"
              :model="questionForm"
              :rules="rules"
              label-width="100px"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="题型" prop="type">
                  <el-select v-model="questionForm.type" placeholder="请选择题型">
                    <el-option
                        v-for="item in typeOptions"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="难度" prop="difficulty">
                  <el-rate v-model="questionForm.difficulty" :max="5" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="分值" prop="score">
                  <el-input-number v-model="questionForm.score" :min="0" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="创建人ID" prop="creatorId">
                  <el-input v-model="questionForm.creatorId" disabled />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="题目描述" prop="description">
              <el-input v-model="questionForm.description" type="textarea" :rows="2" placeholder="请输入题目描述" />
            </el-form-item>
            <el-form-item label="题干内容" prop="content">
              <el-input v-model="questionForm.content" type="textarea" :rows="3" placeholder="请输入题干内容" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 选择题扩展信息 -->
        <el-tab-pane
            label="选择题设置"
            name="choice"
            v-if="questionForm.type === 0 || questionForm.type === 1"
        >
          <el-form
              ref="choiceFormRef"
              :model="choiceForm"
              :rules="choiceRules"
              label-width="100px"
          >
            <el-form-item label="是否多选" prop="isMulti">
              <el-switch v-model="choiceForm.isMulti" />
            </el-form-item>
            <el-form-item label="选项A" prop="options.A">
              <el-input v-model="choiceForm.options.A" placeholder="请输入选项A内容" />
            </el-form-item>
            <el-form-item label="选项B" prop="options.B">
              <el-input v-model="choiceForm.options.B" placeholder="请输入选项B内容" />
            </el-form-item>
            <el-form-item label="选项C" prop="options.C">
              <el-input v-model="choiceForm.options.C" placeholder="请输入选项C内容" />
            </el-form-item>
            <el-form-item label="选项D" prop="options.D">
              <el-input v-model="choiceForm.options.D" placeholder="请输入选项D内容" />
            </el-form-item>
            <el-form-item label="正确答案" prop="correctAnswer">
              <el-checkbox-group v-model="choiceForm.correctAnswer">
                <el-checkbox label="A">A</el-checkbox>
                <el-checkbox label="B">B</el-checkbox>
                <el-checkbox label="C">C</el-checkbox>
                <el-checkbox label="D">D</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="答案解析" prop="analysis">
              <el-input v-model="choiceForm.analysis" type="textarea" :rows="3" placeholder="请输入答案解析" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 简答题扩展信息 -->
        <el-tab-pane
            label="简答题设置"
            name="subjective"
            v-if="questionForm.type === 2"
        >
          <el-form
              ref="subjectiveFormRef"
              :model="subjectiveForm"
              :rules="subjectiveRules"
              label-width="100px"
          >
            <el-form-item label="参考答案" prop="referenceAnswer">
              <el-input
                  v-model="subjectiveForm.referenceAnswer"
                  type="textarea"
                  :rows="6"
                  placeholder="请输入参考答案"
              />
            </el-form-item>
            <el-form-item label="字数限制" prop="wordLimit">
              <el-input-number v-model="subjectiveForm.wordLimit" :min="0" controls-position="right" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false">取消</el-button>
          <el-button type="primary" @click="saveQuestion">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<style scoped>
.container {
  margin: 15px 0;
}
</style>