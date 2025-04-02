<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>班级成绩列表</span>
          <el-button type="primary" @click="handlePublish">发布成绩</el-button>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="gradeList"
        style="width: 100%"
      >
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column prop="homeworkScore" label="作业成绩" width="100" align="center">
          <template #default="{ row }">
            {{ formatScore(row.homeworkScore) }}
          </template>
        </el-table-column>
        <el-table-column prop="midtermScore" label="期中成绩" width="100" align="center">
          <template #default="{ row }">
            {{ formatScore(row.midtermScore) }}
          </template>
        </el-table-column>
        <el-table-column prop="finalScore" label="期末成绩" width="100" align="center">
          <template #default="{ row }">
            {{ formatScore(row.finalScore) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总评成绩" width="100" align="center">
          <template #default="{ row }">
            {{ formatScore(row.totalScore) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="info" link @click="handleDetail(row)">详情</el-button>
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
import { ElMessage } from 'element-plus'
import { getClassGrades } from '@/api/grade'

const route = useRoute()
const router = useRouter()
const classId = route.params.id

const loading = ref(false)
const gradeList = ref([])
const total = ref(0)
const queryParams = ref({
  current: 1,
  size: 10
})

const getList = async () => {
  loading.value = true
  try {
    const { data } = await getClassGrades(classId, queryParams.value)
    gradeList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取班级成绩列表失败:', error)
    ElMessage.error('获取班级成绩列表失败')
  } finally {
    loading.value = false
  }
}

const formatScore = (score) => {
  return score !== null && score !== undefined ? score.toFixed(1) : '-'
}

const handlePublish = () => {
  router.push(`/grade/publish?courseClassId=${classId}`)
}

const handleEdit = (row) => {
  router.push(`/grade/edit/${classId}/${row.studentId}`)
}

const handleDetail = (row) => {
  router.push(`/grade/detail/${classId}/${row.studentId}`)
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
