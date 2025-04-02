<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>班级作业列表</span>
          <el-button type="primary" @click="handleAdd">发布作业</el-button>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="homeworkList"
        style="width: 100%"
      >
        <el-table-column prop="title" label="作业标题" min-width="180" />
        <el-table-column prop="description" label="作业描述" show-overflow-tooltip />
        <el-table-column prop="deadline" label="截止日期" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.deadline) }}
          </template>
        </el-table-column>
        <el-table-column prop="maxScore" label="满分" width="80" align="center" />
        <el-table-column label="提交情况" width="120" align="center">
          <template #default="{ row }">
            {{ row.submittedCount }}/{{ row.totalCount }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleGrade(row)">批改</el-button>
            <el-button type="info" link @click="handleStatistics(row)">统计</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getHomeworkList, deleteHomework } from '@/api/homework'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const classId = route.params.id

const loading = ref(false)
const homeworkList = ref([])
const total = ref(0)
const queryParams = ref({
  current: 1,
  size: 10,
  courseClassId: classId
})

const getList = async () => {
  loading.value = true
  try {
    const { data } = await getHomeworkList(queryParams.value)
    homeworkList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取作业列表失败:', error)
    ElMessage.error('获取作业列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  router.push(`/homework/add?courseClassId=${classId}`)
}

const handleGrade = (row) => {
  router.push(`/homework/grade/${row.id}`)
}

const handleStatistics = (row) => {
  router.push(`/homework/statistics/${row.id}`)
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该作业吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteHomework(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除作业失败:', error)
      ElMessage.error('删除作业失败')
    }
  })
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
  getList()
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
</style>
