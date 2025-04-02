<template>
  <div class="assignment-list-container">
    <!-- 搜索过滤区 -->
    <el-card class="filter-container">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="作业标题">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入作业标题"
            clearable
          />
        </el-form-item>
        <el-form-item label="课程">
          <el-select
            v-model="queryParams.courseId"
            placeholder="选择课程"
            clearable
            filterable
            remote
            :remote-method="handleCourseSearch"
            :loading="courseLoading"
          >
            <el-option
              v-for="item in courseOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="选择状态" clearable>
            <el-option
              v-for="item in statusOptions"
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
    </el-card>

    <!-- 作业列表 -->
    <el-card class="mt-20">
      <template #header>
        <div class="card-header">
          <span>作业列表</span>
          <div class="header-operations">
            <el-button type="primary" @click="handleAdd">新建作业</el-button>
          </div>
        </div>
      </template>

      <el-table :data="assignmentList" style="width: 100%" border>
        <el-table-column prop="title" label="作业标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="courseName" label="所属课程" width="150" />
        <el-table-column prop="teacherName" label="发布教师" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="截止时间" width="180" />
        <el-table-column prop="submitCount" label="提交情况" width="120">
          <template #default="{ row }">
            {{ row.submitCount }}/{{ row.totalCount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="primary" link @click="handleSubmissions(row)">
              提交记录
            </el-button>
            <el-button type="primary" link @click="handleGrade(row)">
              评分
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              删除
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()

// 查询参数
const queryParams = ref({
  title: '',
  courseId: undefined,
  status: undefined,
  pageNum: 1,
  pageSize: 10
})

// 状态选项
const statusOptions = ref([
  { value: 0, label: '未开始' },
  { value: 1, label: '进行中' },
  { value: 2, label: '已结束' }
])

// 课程选项
const courseOptions = ref([])
const courseLoading = ref(false)

// 作业列表数据
const assignmentList = ref([
  {
    id: 1,
    title: '第一次作业 - Java基础练习',
    courseName: 'Java程序设计',
    teacherName: '张老师',
    startTime: '2023-12-20 00:00:00',
    endTime: '2023-12-27 23:59:59',
    submitCount: 35,
    totalCount: 40,
    status: 1
  },
  {
    id: 2,
    title: '第二次作业 - 面向对象编程',
    courseName: 'Java程序设计',
    teacherName: '张老师',
    startTime: '2023-12-28 00:00:00',
    endTime: '2024-01-04 23:59:59',
    submitCount: 0,
    totalCount: 40,
    status: 0
  }
])

const total = ref(50)

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'info',    // 未开始
    1: 'success', // 进行中
    2: 'warning'  // 已结束
  }
  return typeMap[status]
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    0: '未开始',
    1: '进行中',
    2: '已结束'
  }
  return textMap[status]
}

// 搜索课程
const handleCourseSearch = (query) => {
  if (query) {
    courseLoading.value = true
    // 模拟远程搜索
    setTimeout(() => {
      courseOptions.value = [
        { value: 1, label: 'Java程序设计' },
        { value: 2, label: '数据结构' },
        { value: 3, label: '操作系统' }
      ].filter(item => item.label.includes(query))
      courseLoading.value = false
    }, 200)
  } else {
    courseOptions.value = []
  }
}

// 查询
const handleQuery = () => {
  queryParams.value.pageNum = 1
  fetchData()
}

// 重置查询
const resetQuery = () => {
  queryParams.value = {
    title: '',
    courseId: undefined,
    status: undefined,
    pageNum: 1,
    pageSize: 10
  }
  fetchData()
}

// 获取数据
const fetchData = () => {
  // 这里应该调用后端API获取数据
  console.log('获取作业列表数据')
}

// 新建作业
const handleAdd = () => {
  router.push('/assignment/create')
}

// 编辑作业
const handleEdit = (row) => {
  router.push(`/assignment/create?id=${row.id}`)
}

// 查看提交记录
const handleSubmissions = (row) => {
  // 实现查看提交记录逻辑
  console.log('查看提交记录', row)
}

// 评分
const handleGrade = (row) => {
  router.push(`/assignment/grade?id=${row.id}`)
}

// 删除作业
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确认要删除该作业吗？删除后将无法恢复。',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      // 这里应该调用删除API
      ElMessage({
        type: 'success',
        message: '删除成功'
      })
    })
    .catch(() => {})
}

// 分页
const handleSizeChange = (val) => {
  queryParams.value.pageSize = val
  fetchData()
}

const handleCurrentChange = (val) => {
  queryParams.value.pageNum = val
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.assignment-list-container {
  padding: 20px;
}

.mt-20 {
  margin-top: 20px;
}

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
</style>
