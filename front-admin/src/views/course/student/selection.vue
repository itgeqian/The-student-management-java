<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>选课管理</span>
        </div>
      </template>

      <!-- 查询表单 -->
      <div class="filter-container">
        <el-form :inline="true" :model="queryParams" class="form-inline">
          <el-form-item label="课程名称">
            <el-input v-model="queryParams.courseName" placeholder="请输入课程名称" clearable />
          </el-form-item>
          <el-form-item label="教师">
            <el-input v-model="queryParams.teacherName" placeholder="请输入教师姓名" clearable />
          </el-form-item>
          <el-form-item label="课程类型">
            <el-select v-model="queryParams.courseType" placeholder="请选择课程类型" clearable>
              <el-option label="必修课" value="REQUIRED" />
              <el-option label="选修课" value="ELECTIVE" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 课程列表 -->
      <div class="table-container">
        <el-table
          v-loading="loading"
          :data="courseList"
          border
          style="width: 100%"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="courseName" label="课程名称" min-width="180" show-overflow-tooltip />
          <el-table-column prop="courseCode" label="课程代码" width="120" show-overflow-tooltip />
          <el-table-column prop="teacherName" label="任课教师" width="120" show-overflow-tooltip />
          <el-table-column prop="courseType" label="课程类型" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.courseType === 'REQUIRED' ? 'danger' : 'success'">
                {{ row.courseType === 'REQUIRED' ? '必修课' : '选修课' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="credit" label="学分" width="80" align="center" />
          <el-table-column prop="capacity" label="容量" width="100" align="center">
            <template #default="{ row }">
              <span>{{ row.selectedCount }}/{{ row.capacity }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="schedule" label="上课时间" min-width="180" show-overflow-tooltip />
          <el-table-column prop="classroom" label="上课地点" width="120" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="!row.selected"
                type="primary"
                link
                :disabled="!canSelect(row)"
                @click="handleSelect(row)"
              >
                选课
              </el-button>
              <el-button
                v-else
                type="danger"
                link
                :disabled="!canWithdraw(row)"
                @click="handleWithdraw(row)"
              >
                退课
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.limit"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCourseSelectionList,
  selectCourse,
  withdrawCourse
} from '@/api/course'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 查询参数
const queryParams = reactive({
  page: 1,
  limit: 10,
  courseName: '',
  teacherName: '',
  courseType: '',
  studentId: userStore.userId // 当前登录学生的ID
})

// 数据列表
const loading = ref(false)
const total = ref(0)
const courseList = ref([])

// 获取课程列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await getCourseSelectionList(queryParams)
    courseList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

// 查询按钮点击
const handleQuery = () => {
  queryParams.page = 1
  getList()
}

// 重置查询
const resetQuery = () => {
  queryParams.courseName = ''
  queryParams.teacherName = ''
  queryParams.courseType = ''
  handleQuery()
}

// 选课
const handleSelect = async (row) => {
  try {
    await ElMessageBox.confirm('确定要选择该课程吗？', '提示', {
      type: 'warning'
    })
    await selectCourse({
      courseId: row.id,
      studentId: userStore.userId
    })
    ElMessage.success('选课成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('选课失败:', error)
      ElMessage.error('选课失败')
    }
  }
}

// 退课
const handleWithdraw = async (row) => {
  try {
    await ElMessageBox.confirm('确定要退选该课程吗？', '提示', {
      type: 'warning'
    })
    await withdrawCourse(row.selectionId)
    ElMessage.success('退课成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退课失败:', error)
      ElMessage.error('退课失败')
    }
  }
}

// 分页大小改变
const handleSizeChange = (val) => {
  queryParams.limit = val
  getList()
}

// 页码改变
const handleCurrentChange = (val) => {
  queryParams.page = val
  getList()
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    AVAILABLE: 'success',
    FULL: 'warning',
    CLOSED: 'info'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    AVAILABLE: '可选',
    FULL: '已满',
    CLOSED: '已关闭'
  }
  return statusMap[status] || status
}

// 判断是否可以选课
const canSelect = (row) => {
  return row.status === 'AVAILABLE' && row.selectedCount < row.capacity
}

// 判断是否可以退课
const canWithdraw = (row) => {
  return row.status !== 'CLOSED'
}

// 初始化
onMounted(() => {
  getList()
})
</script>

<style scoped>
.filter-container {
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
</style>
