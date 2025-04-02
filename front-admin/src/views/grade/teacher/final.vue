<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>期末成绩管理</span>
          <!-- <div class="header-operations">
            <el-button type="primary" @click="handleImport">导入成绩</el-button>
            <el-button type="success" @click="handleExport">导出成绩</el-button>
          </div> -->
        </div>
      </template>
      <el-form :inline="true" :model="homeworkGradesQueryParams" class="demo-form-inline">
            <el-form-item label="学生姓名">
              <el-input 
                v-model="homeworkGradesQueryParams.studentName" 
                placeholder="学生姓名" 
                clearable 
              />
            </el-form-item>
            <el-form-item label="学生学号">
              <el-input 
                v-model="homeworkGradesQueryParams.studentNo" 
                placeholder="学生学号" 
                clearable 
              />
            </el-form-item>
            <el-form-item label="课程名称">
              <el-input 
                v-model="homeworkGradesQueryParams.courseName" 
                placeholder="课程名称" 
                clearable 
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleHomeworkGradesQuery">查询</el-button>
              <el-button @click="resetHomeworkGradesQuery">重置</el-button>
            </el-form-item>
          </el-form>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="期末成绩" name="finalGrade">
         <el-table
            v-loading="homeworkGradesLoading"
            :data="homeworkGradesList"
            style="width: 100%"
          >
            <el-table-column prop="student.name" label="学生姓名" width="120" />
            <el-table-column prop="student.studentNo" label="学生学号" width="120" />
            <el-table-column prop="homework.title" label="作业标题" />
            <el-table-column prop="homework.courseClass.course.name" label="课程名称" width="150" />
            <el-table-column prop="submitTime" label="提交时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.submitTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="score" label="作业成绩" width="100">
              <template #default="{ row }">
                {{ row.score !== null ? row.score : '未批改' }}
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="homeworkGradesQueryParams.current"
              v-model:page-size="homeworkGradesQueryParams.size"
              :page-sizes="[10, 20, 30, 50]"
              :total="homeworkGradesTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleHomeworkGradesSizeChange"
              @current-change="handleHomeworkGradesCurrentChange"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="作业提交成绩" name="homeworkGrades">
          

          <el-table
            v-loading="homeworkGradesLoading"
            :data="homeworkGradesList"
            style="width: 100%"
          >
            <el-table-column prop="student.name" label="学生姓名" width="120" />
            <el-table-column prop="student.studentNo" label="学生学号" width="120" />
            <el-table-column prop="homework.title" label="作业标题" />
            <el-table-column prop="homework.courseClass.course.name" label="课程名称" width="150" />
            <el-table-column prop="submitTime" label="提交时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.submitTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="score" label="作业成绩" width="100">
              <template #default="{ row }">
                {{ row.score !== null ? row.score : '未批改' }}
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="homeworkGradesQueryParams.current"
              v-model:page-size="homeworkGradesQueryParams.size"
              :page-sizes="[10, 20, 30, 50]"
              :total="homeworkGradesTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleHomeworkGradesSizeChange"
              @current-change="handleHomeworkGradesCurrentChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 导入成绩对话框 -->
      <el-dialog
        v-model="importDialogVisible"
        title="导入成绩"
        width="500px"
      >
        <el-upload
          class="upload-demo"
          action="/api/grade/final/import"
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
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getCourseClassList } from '@/api/course'
import { getFinalGrades, updateFinalGrade, toggleGradeLock } from '@/api/grade'
import { getTeacherHomeworkGrades } from '@/api/homework'
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

const activeTab = ref('finalGrade')

// 作业提交成绩查询参数
const homeworkGradesQueryParams = ref({
  studentName: '',
  studentNo: '',
  courseName: '',
  current: 1,
  size: 10
})

const homeworkGradesLoading = ref(false)
const homeworkGradesList = ref([])
const homeworkGradesTotal = ref(0)

// 获取课程班级列表
const getCourseClasses = async () => {
  try {
    const { data } = await getCourseClassList()
    courseClassList.value = data.records
  } catch (error) {
    console.error('获取课程班级列表失败:', error)
    ElMessage.error('获取课程班级列表失败')
  }
}

// 获取成绩列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await getFinalGrades({
      ...queryParams.value
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

// 成绩变更
const handleScoreChange = async (row) => {
  if (row.locked) return

  try {
    await updateFinalGrade({
      studentId: row.studentId,
      courseClassId: queryParams.value.courseClassId,
      finalScore: row.finalScore
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
  window.open(`/api/grade/final/export?courseClassId=${queryParams.value.courseClassId}`)
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
  handleHomeworkGradesQuery()
}

const resetQuery = () => {
  queryParams.value = {
    current: 1,
    size: 10,
    courseClassId: undefined
  }
  handleHomeworkGradesQuery()
}

const handleSizeChange = (val) => {
  queryParams.value.size = val
  handleHomeworkGradesQuery()
}

const handleCurrentChange = (val) => {
  queryParams.value.current = val
  handleHomeworkGradesQuery()
}

// 查询作业提交成绩
const handleHomeworkGradesQuery = async () => {
  // 防御性检查：确保用户信息存在
  if (!userStore?.userInfo || !userStore?.userInfo?.id) {
    ElMessage.error('用户信息获取失败，请重新登录')
    return
  }

  // 根据当前激活的 Tab 设置作业类型
  const homeworkType = activeTab.value === 'finalGrade' ? 'EXAM' : null

  homeworkGradesLoading.value = true
  try {
    const { data } = await getTeacherHomeworkGrades(userStore?.userInfo?.id || 1, {
      ...homeworkGradesQueryParams.value,
      homeworkType
    })
    homeworkGradesList.value = data.records || []
    homeworkGradesTotal.value = data.total || 0
  } catch (error) {
    console.error('获取作业提交成绩失败:', error)
    ElMessage.error('获取作业提交成绩失败')
  } finally {
    homeworkGradesLoading.value = false
  }
}

// 重置作业提交成绩查询
const resetHomeworkGradesQuery = () => {
  homeworkGradesQueryParams.value = {
    studentName: '',
    studentNo: '',
    courseName: '',
    current: 1,
    size: 10
  }
  handleHomeworkGradesQuery()
}

// 作业提交成绩分页大小变更
const handleHomeworkGradesSizeChange = (val) => {
  homeworkGradesQueryParams.value.size = val
  handleHomeworkGradesQuery()
}

// 作业提交成绩当前页变更
const handleHomeworkGradesCurrentChange = (val) => {
  homeworkGradesQueryParams.value.current = val
  handleHomeworkGradesQuery()
}

// 监听 Tab 切换
watch(activeTab, () => {
  handleHomeworkGradesQuery()
})

// 在 onMounted 中添加初始化检查
onMounted(() => {
  getCourseClasses()
  
  // 检查用户信息并初始化作业提交成绩查询
  if (userStore?.userInfo && userStore?.userInfo?.id) {
    handleHomeworkGradesQuery()
  } else {
    ElMessage.warning('用户信息未加载，请重新登录')
  }
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
