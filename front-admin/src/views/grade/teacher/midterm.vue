<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>期中成绩管理</span>
          <div class="header-operations">
            <el-button type="primary" @click="handleImport">导入成绩</el-button>
            <el-button type="success" @click="handleExport">导出成绩</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="课程班级">
          <el-select v-model="queryParams.courseClassId" placeholder="请选择课程班级" clearable>
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

      <!-- 成绩列表 -->
      <el-table
        v-loading="loading"
        :data="gradeList"
        style="width: 100%"
      >
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column prop="midtermScore" label="期中成绩" width="120" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.midtermScore"
              :min="0"
              :max="100"
              :precision="1"
              :step="0.5"
              :disabled="row.locked"
              @change="handleScoreChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.locked ? 'success' : 'warning'">
              {{ row.locked ? '已锁定' : '未锁定' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button
              :type="row.locked ? 'warning' : 'success'"
              link
              @click="handleToggleLock(row)"
            >
              {{ row.locked ? '解锁' : '锁定' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <!-- 导入成绩对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入成绩"
      width="500px"
    >
      <el-upload
        class="upload-demo"
        action="/api/grade/midterm/import"
        :headers="uploadHeaders"
        :data="uploadData"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        accept=".xls,.xlsx"
      >
        <el-button type="primary">选择文件</el-button>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 Excel 文件，且文件大小不超过 10MB
          </div>
        </template>
      </el-upload>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCourseClassList } from '@/api/course'
import { getMidtermGrades, updateMidtermGrade, toggleGradeLock } from '@/api/grade'
import { formatDateTime } from '@/utils/format'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const courseClassList = ref([])
const gradeList = ref([])
const total = ref(0)
const importDialogVisible = ref(false)

const queryParams = ref({
  current: 1,
  size: 10,
  courseClassId: undefined
})

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const uploadData = computed(() => ({
  courseClassId: queryParams.value.courseClassId
}))

// 获取课程班级列表
const getCourseClasses = async () => {
  try {
    const { data } = await getCourseClassList()
    courseClassList.value = data
  } catch (error) {
    console.error('获取课程班级列表失败:', error)
    ElMessage.error('获取课程班级列表失败')
  }
}

// 获取成绩列表
const getList = async () => {
  if (!queryParams.value.courseClassId) {
    gradeList.value = []
    total.value = 0
    return
  }

  loading.value = true
  try {
    const { data } = await getMidtermGrades(queryParams.value)
    gradeList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取成绩列表失败:', error)
    ElMessage.error('获取成绩列表失败')
  } finally {
    loading.value = false
  }
}

// 成绩变更
const handleScoreChange = async (row) => {
  if (row.locked) return

  try {
    await updateMidtermGrade({
      studentId: row.studentId,
      courseClassId: queryParams.value.courseClassId,
      midtermScore: row.midtermScore
    })
    ElMessage.success('更新成功')
  } catch (error) {
    console.error('更新成绩失败:', error)
    ElMessage.error('更新成绩失败')
  }
}

// 锁定/解锁成绩
const handleToggleLock = async (row) => {
  try {
    await toggleGradeLock({
      studentId: row.studentId,
      courseClassId: queryParams.value.courseClassId,
      locked: !row.locked
    })
    row.locked = !row.locked
    ElMessage.success(row.locked ? '锁定成功' : '解锁成功')
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

// 导入成绩
const handleImport = () => {
  if (!queryParams.value.courseClassId) {
    ElMessage.warning('请先选择课程班级')
    return
  }
  importDialogVisible.value = true
}

// 导出成绩
const handleExport = () => {
  if (!queryParams.value.courseClassId) {
    ElMessage.warning('请先选择课程班级')
    return
  }
  window.open(`/api/grade/midterm/export?courseClassId=${queryParams.value.courseClassId}`)
}

const handleUploadSuccess = () => {
  ElMessage.success('导入成功')
  importDialogVisible.value = false
  getList()
}

const handleUploadError = () => {
  ElMessage.error('导入失败')
}

const handleQuery = () => {
  // queryParams.value.current = 1
  getList()
}

const resetQuery = () => {
  queryParams.value = {
    current: 1,
    size: 10,
    courseClassId: undefined
  }
  getList()
}

const handleSizeChange = (val) => {
  queryParams.value.size = val
  getList()
}

const handleCurrentChange = (val) => {
  queryParams.value.current = val
  getList()
}

onMounted(() => {
  getCourseClasses()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-operations {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.upload-demo {
  text-align: center;
}

.el-upload__tip {
  margin-top: 10px;
  color: #666;
}
</style>
