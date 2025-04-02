<template>
  <div class="app-container">
    <div class="filter-container">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="课程班级">
          <el-select
            v-model="queryParams.classId"
            placeholder="请选择课程班级"
            @change="handleClassChange"
          >
            <el-option
              v-for="item in classList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="提交状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择提交状态"
            clearable
          >
            <el-option label="待提交" value="pending" />
            <el-option label="已提交" value="submitted" />
            <el-option label="已发布" value="published" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>成绩审核列表</span>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="gradeList"
        border
        style="width: 100%"
      >
        <el-table-column prop="className" label="课程班级" />
        <el-table-column prop="teacherName" label="任课教师" />
        <el-table-column prop="studentCount" label="学生人数" width="100" />
        <el-table-column prop="submittedCount" label="已提交人数" width="120" />
        <el-table-column label="平均分" width="100">
          <template #default="{ row }">
            {{ row.averageGrade?.toFixed(1) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="提交状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="180">
          <template #default="{ row }">
            {{ row.submitTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleView(row)"
            >
              查看
            </el-button>
            <el-button
              v-if="row.status === 'submitted'"
              type="success"
              link
              @click="handlePublish(row)"
            >
              发布
            </el-button>
            <el-button
              v-if="row.status === 'published'"
              type="warning"
              link
              @click="handleUnpublish(row)"
            >
              撤回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 成绩详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="成绩详情"
      width="80%"
      :before-close="handleDialogClose"
    >
      <el-table
        v-loading="detailLoading"
        :data="detailList"
        border
        style="width: 100%"
      >
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column label="作业成绩" width="120">
          <template #default="{ row }">
            {{ row.homeworkGrade || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="实验成绩" width="120">
          <template #default="{ row }">
            {{ row.practiceGrade || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="期末成绩" width="120">
          <template #default="{ row }">
            {{ row.finalGrade || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="总评成绩" width="120">
          <template #default="{ row }">
            {{ row.totalGrade || '-' }}
          </template>
        </el-table-column>
      </el-table>

      <div class="dialog-footer">
        <el-button @click="handleDialogClose">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getClassList,
  getGradeList,
  publishGrades,
  unpublishGrades
} from '@/api/grade'

const loading = ref(false)
const detailLoading = ref(false)
const dialogVisible = ref(false)
const classList = ref([])
const gradeList = ref([])
const detailList = ref([])
const total = ref(0)

const queryParams = reactive({
  classId: undefined,
  status: undefined,
  pageNum: 1,
  pageSize: 10
})

// 获取课程班级列表
const loadClassList = async () => {
  try {
    const response = await getClassList()
    classList.value = response.data
  } catch (error) {
    console.error('获取课程班级列表失败：', error)
    ElMessage.error('获取课程班级列表失败')
  }
}

// 获取成绩列表
const getGrades = async () => {
  loading.value = true
  try {
    const response = await getGradeList(queryParams)
    gradeList.value = response.data.list
    total.value = response.data.total
  } catch (error) {
    console.error('获取成绩列表失败：', error)
    ElMessage.error('获取成绩列表失败')
  } finally {
    loading.value = false
  }
}

// 获取状态标签类型
const getStatusType = (status) => {
  const types = {
    pending: 'info',
    submitted: 'warning',
    published: 'success'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    pending: '待提交',
    submitted: '已提交',
    published: '已发布'
  }
  return texts[status] || '未知'
}

// 查看成绩详情
const handleView = async (row) => {
  dialogVisible.value = true
  detailLoading.value = true
  try {
    const response = await getGradeList({ classId: row.classId })
    detailList.value = response.data.list
  } catch (error) {
    console.error('获取成绩详情失败：', error)
    ElMessage.error('获取成绩详情失败')
  } finally {
    detailLoading.value = false
  }
}

// 发布成绩
const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm('确定要发布该班级的成绩吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await publishGrades(row.classId)
    ElMessage.success('成绩发布成功')
    getGrades()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布成绩失败：', error)
      ElMessage.error('发布成绩失败')
    }
  }
}

// 撤回成绩
const handleUnpublish = async (row) => {
  try {
    await ElMessageBox.confirm('确定要撤回该班级的成绩吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await unpublishGrades(row.classId)
    ElMessage.success('成绩撤回成功')
    getGrades()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤回成绩失败：', error)
      ElMessage.error('撤回成绩失败')
    }
  }
}

// 关闭对话框
const handleDialogClose = () => {
  dialogVisible.value = false
  detailList.value = []
}

const handleClassChange = () => {
  queryParams.pageNum = 1
  getGrades()
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getGrades()
}

const resetQuery = () => {
  queryParams.classId = undefined
  queryParams.status = undefined
  queryParams.pageNum = 1
  getGrades()
}

const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getGrades()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  getGrades()
}

onMounted(() => {
  loadClassList()
  getGrades()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  margin-top: 20px;
  text-align: right;
}
</style>
