<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <div class="search-container">
        <el-form :inline="true" :model="queryParams" class="search-form">
          <el-form-item label="课程名称">
            <el-input
              v-model="queryParams.courseName"
              placeholder="请输入课程名称"
              clearable
            />
          </el-form-item>
          <el-form-item label="教师" v-if="isStudent">
            <el-input
              v-model="queryParams.teacherName"
              placeholder="请输入教师姓名"
              clearable
            />
          </el-form-item>
          <el-form-item label="学期">
            <el-select
              v-model="queryParams.semester"
              placeholder="请选择学期"
              style="width: 240px"
              clearable
            >
              <el-option
                v-for="item in semesterOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 选课列表 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>{{ isStudent ? "可选课程" : "课程列表" }}</span>
          <el-button type="primary" @click="refreshList">刷新列表</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="courseList" border>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column
          prop="courseClass.name"
          label="课程班级"
          min-width="150"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <div class="course-info">
              <div>
                <span class="label">教师：</span>
                <span>{{ row.courseClass.teacher?.name || "-" }}</span>
              </div>
              <div>
                <span class="label">班级：</span>
                <span>{{ row.courseClass.classroom || "-" }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="课程信息" min-width="200">
          <template #default="{ row }">
            <div class="course-info">
              <div>
                <span class="label">课程名称：</span>
                <span>{{ row.courseClass.course?.name || "-" }}</span>
              </div>
              <div>
                <span class="label">学分：</span>
                <span>{{ row.courseClass.course?.credit || "-" }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="班级信息" min-width="200">
          <template #default="{ row }">
            <div class="course-info">
              <div>
                <span class="label">上课时间：</span>
                <span>{{ getClassTimeText(row.courseClass.classTime) || "-" }}</span>
              </div>
              <div>
                <span class="label">上课地点：</span>
                <span>{{ row.courseClass.classroom || "-" }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="courseClass.semester"
          label="学期"
          width="130"
        >
          <template #default="{ row }">
            {{ getSemesterText(row.courseClass.semester) }}
          </template>
        </el-table-column>
        <el-table-column
          label="选课人数"
          width="150"
          align="center"
          v-if="isStudent"
        >
          <template #default="{ row }">
            {{ row.courseClass.currentStudents }}/{{
              row.courseClass.maxStudents
            }}
          </template>
        </el-table-column>
        <el-table-column
          label="状态"
          width="100"
          align="center"
          v-if="isStudent"
        >
          <template #default="{ row }">
            <el-tag v-if="!row.courseClass.isActive" type="warning"
              >已关闭选课</el-tag
            >
            <el-tag v-else-if="row.isFull" type="danger">已满</el-tag>
            <el-tag v-else type="success">可选</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="100"
          align="center"
          v-if="isStudent"
        >
          <template #default="{ row }">
            <el-button
              v-if="row.courseClass.isActive && !row.isFull"
              type="primary"
              size="small"
              @click="handleSelect(row)"
            >
              选课
            </el-button>
            <span v-else-if="!row.courseClass.isActive" class="text-gray"
              >不可操作</span
            >
            <span v-else class="text-danger">已满</span>
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

    <!-- 已选课程列表 -->
    <el-card class="list-card" v-if="isStudent">
      <template #header>
        <div class="card-header">
          <span>已选课程</span>
        </div>
      </template>

      <el-table v-loading="selectedLoading" :data="selectedList" border>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column
          prop="courseClass.name"
          label="课程班级"
          min-width="150"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <div class="course-info">
              <div>
                <span class="label">教师：</span>
                <span>{{ row.courseClass.teacher?.name || "-" }}</span>
              </div>
              <div>
                <span class="label">班级：</span>
                <span>{{ row.courseClass.classroom || "-" }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="courseClass.semester" label="学期" width="120">
          <template #default="{ row }">
            {{ getSemesterText(row.courseClass.semester) }}
          </template>
        </el-table-column>
        <el-table-column prop="selectTime" label="选课时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.selectTime) }}
          </template>
        </el-table-column>
        <el-table-column label="成绩" min-width="200">
          <template #default="{ row }">
            <div class="score-info">
              <span>作业：{{ row.homeworkScore || "-" }}</span>
              <span>实验：{{ row.experimentScore || "-" }}</span>
              <span>考试：{{ row.finalExamScore || "-" }}</span>
              <span>总分：{{ row.totalScore || "-" }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'COMPLETED'" type="success"
              >已完成</el-tag
            >
            <el-tag v-else-if="row.status === 'SELECTED'" type="primary"
              >进行中</el-tag
            >
            <el-tag v-else-if="row.status === 'DROPPED'" type="info"
              >已退课</el-tag
            >
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'SELECTED' && row.courseClass.isActive"
              type="danger"
              size="small"
              @click="handleDrop(row)"
            >
              退课
            </el-button>
            <span v-else-if="!row.courseClass.isActive" class="text-gray"
              >已关闭选课</span
            >
            <span v-else-if="row.status === 'COMPLETED'" class="text-success"
              >已完成</span
            >
            <span v-else-if="row.status === 'DROPPED'" class="text-info"
              >已退课</span
            >
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="selectedQueryParams.current"
          v-model:page-size="selectedQueryParams.size"
          :page-sizes="[10, 20, 30, 50]"
          :total="selectedTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSelectedSizeChange"
          @current-change="handleSelectedCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { useUserStore } from "@/stores/user";
import { getCourseClassList } from "@/api/course";
import {
  selectCourse,
  dropCourse,
  getStudentSelections,
  getClassSelections,
  checkSelected,
  checkClassFull,
} from "@/api/course-selection";
import { semesterOptions, getSemesterText, getClassTimeText } from "@/constants/options";

const userStore = useUserStore();
const loading = ref(false);
const selectedLoading = ref(false);
const total = ref(0);
const selectedTotal = ref(0);
const courseList = ref([]);
const selectedList = ref([]);

// 判断是否为学生
const isStudent = computed(() => {
  return userStore.roles.some((role) => role.toLowerCase() === "student");
});

// 查询参数
const queryParams = reactive({
  current: 1,
  size: 10,
  courseName: "",
  teacherName: "",
  semester: "",
});

// 已选课程查询参数
const selectedQueryParams = reactive({
  current: 1,
  size: 10,
});

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return "-";
  const date = new Date(dateStr);
  return date.toLocaleString();
};

// 获取课程列表
const getCourseList = async () => {
  loading.value = true;
  try {
    let response;
    if (isStudent.value) {
      // 获取所有课程班级
      response = await getCourseClassList(queryParams);
      courseList.value = response.data.records.map((courseClass) => ({
        courseClass,
        status: "NOT_SELECTED",
        isFull: false,
      }));
      total.value = response.data.total;

      // 检查每个课程的状态
      const studentId = userStore?.userInfo?.id || 1;
      for (const course of courseList.value) {
        const [selectedRes, fullRes] = await Promise.all([
          checkSelected(studentId, course.courseClass.id),
          checkClassFull(course.courseClass.id),
        ]);
        course.status = selectedRes.data ? "SELECTED" : "NOT_SELECTED";
        course.isFull = fullRes.data;
      }
    } else {
      // 教师查看课程选课情况
      response = await getClassSelections(userStore?.userInfo?.id || 1, queryParams);
      courseList.value = response.data.records;
      total.value = response.data.total;
    }
  } catch (error) {
    console.error("获取课程列表失败:", error);
    ElMessage.error("获取课程列表失败");
  } finally {
    loading.value = false;
  }
};

// 获取已选课程列表
const getSelectedList = async () => {
  if (!isStudent.value) return;

  selectedLoading.value = true;
  try {
    const response = await getStudentSelections(
      userStore?.userInfo?.id || 1,
      selectedQueryParams
    );
    selectedList.value = response.data.records;
    selectedTotal.value = response.data.total;
  } catch (error) {
    console.error("获取已选课程失败:", error);
    ElMessage.error("获取已选课程失败");
  } finally {
    selectedLoading.value = false;
  }
};

// 选课
const handleSelect = async (row) => {
  try {
    if (!row.courseClass.isActive) {
      ElMessage.warning("课程已关闭选课");
      return;
    }

    if (row.status === "COMPLETED") {
      ElMessage.warning("课程已完成，不能选课");
      return;
    }

    if (row.status === "DROPPED") {
      ElMessage.warning("已退课的课程不能再次选择");
      return;
    }

    await ElMessageBox.confirm("确认选择该课程?", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    await selectCourse(userStore?.userInfo?.id || 1, row.courseClass.id);
    ElMessage.success("选课成功");
    refreshList();
  } catch (error) {
    if (error === "cancel") return;
    console.error("选课失败:", error);
    ElMessage.error(error.response?.data?.message || "选课失败");
  }
};

// 退课
const handleDrop = async (row) => {
  try {
    if (!row.courseClass.isActive) {
      ElMessage.warning("课程已关闭选课");
      return;
    }

    if (row.status === "COMPLETED") {
      ElMessage.warning("课程已完成，不能退课");
      return;
    }

    if (row.status === "DROPPED") {
      ElMessage.warning("课程已经退过了");
      return;
    }

    await ElMessageBox.confirm("确认退选该课程?", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    await dropCourse(userStore?.userInfo?.id || 1, row.courseClass.id);
    ElMessage.success("退课成功");
    refreshList();
  } catch (error) {
    if (error === "cancel") return;
    console.error("退课失败:", error);
    ElMessage.error(error.response?.data?.message || "退课失败");
  }
};

// 刷新列表
const refreshList = async () => {
  await Promise.all([getCourseList(), getSelectedList()]);
};

// 查询按钮点击
const handleQuery = () => {
  // queryParams.current = 1;
  getCourseList();
};

// 重置查询
const resetQuery = () => {
  queryParams.current = 1;
  queryParams.courseName = "";
  queryParams.teacherName = "";
  queryParams.semester = "";
  getCourseList();
};

// 可选课程分页
const handleSizeChange = (val) => {
  queryParams.size = val;
  getCourseList();
};

const handleCurrentChange = (val) => {
  queryParams.current = val;
  getCourseList();
};

// 已选课程分页
const handleSelectedSizeChange = (val) => {
  selectedQueryParams.size = val;
  getSelectedList();
};

const handleSelectedCurrentChange = (val) => {
  selectedQueryParams.current = val;
  getSelectedList();
};

onMounted(() => {
  refreshList();
});
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.list-card {
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
  justify-content: flex-end;
}

.score-info {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.score-info span {
  flex: 1;
  text-align: center;
}

.course-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.course-info .label {
  color: #606266;
  margin-right: 4px;
  font-size: 13px;
}

.course-info > div {
  line-height: 1.5;
}
</style>
