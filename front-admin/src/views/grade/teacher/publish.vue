<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>成绩发布管理</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="课程班级">
          <el-select v-model="queryParams.courseClassId" placeholder="请选择课程班级" clearable>
            <el-option
              v-for="item in courseClassList"
              :key="item.id"
              :label="item?.course ? item?.course?.name + '-' + item.className : item.className"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 成绩列表 -->
      <el-table
        v-loading="loading"
        :data="gradeList"
        style="width: 100%"
      >
        <el-table-column label="课程班级" min-width="180">
          <template #default="{ row }">
            <div>{{ row.courseClass?.name }}</div>
            <div class="text-gray-500 text-sm">
              学期：{{ row.courseClass?.semester }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="学生信息" min-width="180">
          <template #default="{ row }">
            <div>{{ row.student?.name }}</div>
            <div class="text-gray-500 text-sm">
              学号：{{ row.student?.studentNo }}
            </div>
            <div class="text-gray-500 text-sm">
              联系方式：{{ row.student?.phone }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="examScore" label="期末考试成绩" width="120">
          <template #default="{ row }">
            <span :class="getScoreClass(row.examScore)">{{ row.examScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="homeworkScore" label="作业平均成绩" width="120">
          <template #default="{ row }">
            <span :class="getScoreClass(row.homeworkScore)">{{ row.homeworkScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="experimentScore" label="实验平均成绩" width="120">
          <template #default="{ row }">
            <span :class="getScoreClass(row.experimentScore)">{{ row.experimentScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总成绩" width="100">
          <template #default="{ row }">
            <span :class="getScoreClass(row.totalScore)">{{ row.totalScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="isPublished" label="发布状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isPublished ? 'success' : 'info'">
              {{ row.isPublished ? '已发布' : '未发布' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isSubmitted" label="提交状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isSubmitted ? 'success' : 'warning'">
              {{ row.isSubmitted ? '已提交' : '未提交' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="canPublish(row)"
              type="primary"
              size="small"
              @click="handlePublish(row)"
            >
              发布
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCourseClassList } from '@/api/course'
import { getClassTotalGrades, publishGrades } from '@/api/grade'
import { formatDateTime } from '@/utils/format'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const courseClassList = ref([])
const gradeList = ref([])
const total = ref(0)
const previewDialogVisible = ref(false)
const previewData = ref({
  courseClassName: '',
  totalStudents: 0,
  gradedStudents: 0,
  status: '',
  students: []
})

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  courseClassId: undefined
})

// 获取课程班级列表
const getCourseClasses = async () => {
  try {
    const { data } = await getCourseClassList()
    courseClassList.value = data.records;
  } catch (error) {
    console.error('获取课程班级列表失败:', error)
    ElMessage.error('获取课程班级列表失败')
  }
}

// 获取成绩列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await getClassTotalGrades({
      ...queryParams.value,
      current: queryParams.value.pageNum,
      userId: userStore?.userInfo?.id || 1
    })
    gradeList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取成绩列表失败:', error)
    ElMessage.error('获取成绩列表失败')
  } finally {
    loading.value = false
  }
}

// 是否可以发布
const canPublish = (row) => {
  return !row.isPublished && row.isSubmitted
}

// 处理发布
const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm('确认要发布该班级的成绩吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await publishGrades(row.courseClassId)
    ElMessage.success('成绩发布成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布成绩失败:', error)
      ElMessage.error('发布成绩失败')
    }
  }
}

// 处理预览
const handlePreview = async (row) => {
  previewDialogVisible.value = true
  previewData.value = {
    ...row,
    students: row.students || []
  }
}

// 查询
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    courseClassId: undefined
  }
  getList()
}

// 处理每页显示数量变化
const handleSizeChange = (val) => {
  queryParams.value.pageSize = val
  getList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.value.pageNum = val
  getList()
}

// 获取成绩颜色
const getScoreClass = (score) => {
  if (score >= 90) {
    return 'score-excellent'
  } else if (score >= 80) {
    return 'score-good'
  } else if (score >= 60) {
    return 'score-pass'
  } else {
    return 'score-fail'
  }
}

onMounted(() => {
  getCourseClasses()
  getList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.text-gray-500 {
  color: #909399;
  font-size: 13px;
  line-height: 1.4;
  margin-top: 4px;
}

.text-sm {
  font-size: 13px;
}

/* 成绩颜色样式 */
.score-excellent {
  color: #67c23a;
  font-weight: bold;
}

.score-good {
  color: #409eff;
  font-weight: bold;
}

.score-pass {
  color: #e6a23c;
  font-weight: bold;
}

.score-fail {
  color: #f56c6c;
  font-weight: bold;
}

/* 表格内容垂直居中 */
:deep(.el-table .cell) {
  padding: 8px 0;
}

:deep(.el-table__row) {
  .el-button {
    padding: 6px 15px;
  }
}

/* 标签样式 */
:deep(.el-tag) {
  border-radius: 12px;
  padding: 0 12px;
  height: 24px;
  line-height: 24px;
}

/* 搜索表单样式 */
.demo-form-inline {
  margin-bottom: 20px;
  
  .el-form-item {
    margin-bottom: 0;
    margin-right: 16px;
  }
  
  .el-select {
    width: 240px;
  }
}
</style>
