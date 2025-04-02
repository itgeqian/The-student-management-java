<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>班级学生列表</span>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="studentList"
        style="width: 100%"
      >
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 'MALE' ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="department" label="院系" />
        <el-table-column prop="major" label="专业" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="class" label="班级" width="100" />
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
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getClassStudents } from '@/api/course'

const route = useRoute()
const classId = route.params.id

const loading = ref(false)
const studentList = ref([])
const total = ref(0)
const queryParams = ref({
  current: 1,
  size: 10
})

const getList = async () => {
  loading.value = true
  try {
    const { data } = await getClassStudents(classId, queryParams.value)
    studentList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取班级学生列表失败:', error)
    ElMessage.error('获取班级学生列表失败')
  } finally {
    loading.value = false
  }
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
