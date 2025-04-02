<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card
      v-if="!$route.path.includes('/detail/')"
      class="filter-container"
      shadow="never"
    >
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="课程班级">
          <el-select
            v-model="queryParams.courseClassId"
            placeholder="请选择课程班级"
            clearable
          >
            <el-option
              v-for="item in courseClassList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 作业列表 -->
    <el-card
      v-if="!$route.path.includes('/detail/')"
      class="list-container"
      shadow="never"
    >
      <template #header>
        <div class="card-header">
          <span>作业列表</span>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="homeworkList"
        style="width: 100%"
        border
        @row-dblclick="handleRowDblclick"
      >
        <el-table-column
          prop="title"
          label="作业标题"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column label="课程班级" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div>{{ row.courseClass?.name }}</div>
            <div class="text-gray-500 text-sm">
              教师：{{ row.courseClass?.teacher.name }}
            </div>
            <div class="text-gray-500 text-sm">
              学科：{{ row.courseClass?.course.name }}
            </div>
            <div class="text-gray-500 text-sm">
              学期：{{ row.courseClass?.semester }}
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="content"
          label="作业内容"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column label="附件" width="100" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.attachmentUrl"
              type="primary"
              link
              @click="handleDownload(row.attachmentUrl)"
            >
              下载
            </el-button>
            <span v-else>无</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'HOMEWORK' ? 'success' : 'info'">
              {{
                row.type === "HOMEWORK"
                  ? "平时作业"
                  : row.type === "EXAM"
                  ? "期末作业"
                  : "实验作业"
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="截止日期" width="180" align="center">
          <template #default="{ row }">
            <span>{{ formatDateTime(row.deadline) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.needSubmit ? 'warning' : 'success'">
              {{ row.needSubmit ? "需要提交" : "无需提交" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.needSubmit"
              type="primary"
              link
              @click="handleSubmit(row)"
            >
              提交作业
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 子路由出口 -->
    <router-view v-if="$route.path.includes('/detail/')" />
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { getStudentAvailableHomeworks } from "@/api/homework";
import { getStudentCourseClasses } from "@/api/course";
import { formatDateTime } from "@/utils/format";
import { useUserStore } from "@/stores/user";

const router = useRouter();
const userStore = useUserStore();

// 查询参数
const queryParams = ref({
  current: 1,
  size: 10,
  courseClassId: undefined,
});

// 数据列表
const loading = ref(false);
const total = ref(0);
const homeworkList = ref([]);
const courseClassList = ref([]);

// 获取作业列表
const getList = async () => {
  if (!userStore.token) {
    ElMessage.warning("请先登录");
    router.push("/login");
    return;
  }

  if (!userStore.userInfo) {
    try {
      await userStore.getUserInfo();
    } catch (error) {
      console.error("获取用户信息失败:", error);
      ElMessage.error("获取用户信息失败");
      return;
    }
  }

  if (!userStore.userInfo?.id) {
    ElMessage.warning("用户信息不完整，请重新登录");
    router.push("/login");
    return;
  }

  loading.value = true;
  try {
    const { data } = await getStudentAvailableHomeworks(
      userStore?.userInfo?.id || 1,
      queryParams.value
    );
    homeworkList.value = data.records;
    total.value = data.total;
  } catch (error) {
    console.error("获取作业列表失败:", error);
    ElMessage.error("获取作业列表失败");
  } finally {
    loading.value = false;
  }
};

// 下载附件
const handleDownload = (url) => {
  if (!url) {
    ElMessage.warning("附件地址为空");
    return;
  }
  window.open(`/api/api/file/download?fileUrl=${encodeURIComponent(url)}`, '_blank')
};

// 获取课程班级列表
const getCourseClasses = async () => {
  if (!userStore?.userInfo?.id) {
    console.warn("用户信息不完整");
    return;
  }

  try {
    const { data } = await getStudentCourseClasses(userStore?.userInfo?.id || 1);
    if (Array.isArray(data)) {
      courseClassList.value = data;
    } else {
      courseClassList.value = [];
      console.warn("课程班级数据格式不正确:", data);
    }
  } catch (error) {
    console.error("获取课程班级列表失败:", error);
    ElMessage.error("获取课程班级列表失败");
    courseClassList.value = [];
  }
};

// 查询操作
const handleQuery = () => {
  // queryParams.value.current = 1;
  getList();
};

const resetQuery = () => {
  queryParams.value = {
    current: 1,
    size: 10,
    courseClassId: undefined,
  };
  getList();
};

// 分页操作
const handleSizeChange = (val) => {
  queryParams.value.size = val;
  getList();
};

const handleCurrentChange = (val) => {
  queryParams.value.current = val;
  getList();
};

// 提交作业
const handleSubmit = (row) => {
  router.push(`/homework/submit/${row.id}`);
};

// 双击查看详情
const handleRowDblclick = (row) => {
  router.push(`/homework/student/detail/${row.id}`);
};

onMounted(async () => {
  if (!userStore.token) {
    ElMessage.warning("请先登录");
    router.push("/login");
    return;
  }

  try {
    await userStore.getUserInfo();
  } catch (error) {
    console.error("获取用户信息失败:", error);
    ElMessage.error("获取用户信息失败");
    return;
  }

  if (!userStore.userInfo?.id) {
    ElMessage.warning("用户信息不完整，请重新登录");
    router.push("/login");
    return;
  }

  // 先获取课程班级列表，再获取作业列表
  await getCourseClasses();
  await getList();
});
</script>

<style scoped>
.filter-container {
  margin-bottom: 20px;
}

.list-container {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
