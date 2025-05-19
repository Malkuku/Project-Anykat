<script setup>
import { ref, onMounted } from 'vue';
import { queryUsersApi } from '@/api/user';
import { ElMessage } from 'element-plus';
import { CopyDocument } from "@element-plus/icons-vue";

const props = defineProps({
  visible: {
    type: Boolean,
    required: true
  }
});

const emit = defineEmits(['update:visible', 'copy-user']);

// 查询参数
const queryParams = ref({
  name: '',
  username: '',
  role: null,
  page: 1,
  pageSize: 10
});

// 分页数据
const pagination = ref({
  total: 0,
  list: []
});

// 角色选项
const roleOptions = [
  { label: '学生', value: 0 },
  { label: '教师', value: 1 },
  { label: '管理员', value: 2 }
];

// 查询方法
const search = async () => {
  const result = await queryUsersApi(queryParams.value);
  if (result.code) {
    pagination.value = {
      total: result.data.total,
      list: result.data.list
    };
  }
};

// 重置查询
const resetQuery = () => {
  queryParams.value = {
    name: '',
    username: '',
    role: null,
    page: 1,
    pageSize: 10
  };
  search();
};

// 分页变化
const handlePageChange = (page) => {
  queryParams.value.page = page;
  search();
};

// 每页条数变化
const handleSizeChange = (size) => {
  queryParams.value.pageSize = size;
  queryParams.value.page = 1;
  search();
};

// 格式化角色显示
const formatRole = (role) => {
  const roleMap = {
    0: '学生',
    1: '教师',
    2: '管理员'
  };
  return roleMap[role] || '未知';
};


// 关闭弹窗
const handleClose = () => {
  emit('update:visible', false);
};

const handleVisibleUpdate = (newVisible) => {
  emit('update:visible', newVisible);
};

// 复制用户信息
const copyUserInfo = (user) => {
  // 准备要复制的信息
  const userInfo = {
    username: user.username,
    password: user.password
  };
  // 触发事件将信息传递给父组件
  emit('copy-user', userInfo);

  // 提示用户
  ElMessage.success('已复制用户信息');

  //关闭弹窗
  handleClose();
};

// 初始化时查询数据
onMounted(() => {
  search();
});
</script>

<template>
  <el-dialog
      :model-value="visible"
      title="用户管理"
      width="80%"
      top="5vh"
      @update:model-value="handleClose"
      @close="handleClose"
  >
    <!-- 查询表单 -->
    <div class="container">
      <el-form :model="queryParams" inline class="query-form">
        <el-form-item label="用户姓名" class="form-item">
          <el-input
              v-model="queryParams.name"
              placeholder="请输入用户姓名"
              clearable
              @clear="search"
          />
        </el-form-item>

        <el-form-item label="用户名" class="form-item">
          <el-input
              v-model="queryParams.username"
              placeholder="请输入用户名"
              clearable
              @clear="search"
          />
        </el-form-item>

        <el-form-item label="身份" class="form-item role-select">
          <el-select
              v-model="queryParams.role"
              placeholder="请选择身份"
              clearable
              @change="search"
              style="width: 150px"
          >
            <el-option
                v-for="item in roleOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item class="form-item actions">
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格 -->
    <div class="container">
      <el-table :data="pagination.list" border style="width: 100%">
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column prop="name" label="用户姓名" width="150" align="center" />
        <el-table-column prop="username" label="用户名" width="150" align="center" />
        <el-table-column prop="role" label="身份" width="120" align="center">
          <template #default="scope">
            {{ formatRole(scope.row.role) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button
                type="primary"
                size="small"
                @click="copyUserInfo(scope.row)"
            >
              <el-icon><CopyDocument /></el-icon> 复制信息
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="container">
      <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
      />
    </div>
  </el-dialog>
</template>

<style scoped>

/* 查询表单优化 */
.query-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.form-item {
  margin-bottom: 0;
  flex: 1;
  min-width: 180px;
}

.role-select {
  min-width: 200px; /* 给选择器更多空间 */
}

.actions {
  flex: none;
  min-width: auto;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .form-item {
    min-width: 100%;
  }

  .actions {
    justify-content: flex-end;
    width: 100%;
  }
}

.container {
  margin: 15px 0;
}
</style>