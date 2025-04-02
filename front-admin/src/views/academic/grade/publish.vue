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
        <el-form-item label="审核状态">
          <el-select v-model="queryParams.status" placeholder="请选择审核状态">
            <el-option label="待提交" value="pending" />
            <el-option label="待审核" value="submitted" />
            <el-option label="已通过" value="approved" />
            <el-option label="已驳回" value="rejected" />
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
          <span>成绩发布管理</span>
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
        <el-table-column label="审核状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最后更新时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'pending'"
              type="primary"
              link
              @click="handleSubmitAudit(row)"
            >
              提交审核
            </el-button>
            <el-button
              v-if="isAdmin && row.status === 'submitted'"
              type="success"
              link
              @click="handleApprove(row)"
            >
              通过
            </el-button>
            <el-button
              v-if="isAdmin && row.status === 'submitted'"
              type="danger"
              link
              @click="handleReject(row)"
            >
              驳回
            </el-button>
            <el-button
              v-if="row.status === 'approved'"
              type="warning"
              link
              @click="handleUnpublish(row)"
            >
              撤回发布
            </el-button>
            <el-button
              type="primary"
              link
              @click="handleViewDetails(row)"
            >
              查看详情
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

    <!-- 审核意见对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      :title="auditAction === 'approve' ? '通过审核' : '驳回原因'"
      width="500px"
    >
      <el-form ref="auditFormRef" :model="auditForm" :rules="auditRules">
        <el-form-item
          label="审核意见"
          prop="comment"
          :rules="[
            { required: true, message: '请输入审核意见', trigger: 'blur' },
            { min: 5, message: '审核意见不能少于5个字符', trigger: 'blur' }
          ]"
        >
          <el-input
            v-model="auditForm.comment"
            type="textarea"
            rows="4"
            placeholder="请输入审核意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="auditDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAudit">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  getClassList,
  getGradeList,
  submitGradeAudit,
  auditGrades,
  publishGrades,
  unpublishGrades
} from '@/api/grade'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const total = ref(0)
const gradeList = ref([])
const classList = ref([])
const auditDialogVisible = ref(false)
const auditAction = ref('')
const currentRow = ref(null)

// 是否为管理员
const isAdmin = computed(() => {
  return userStore.roles.includes('admin')
})

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  classId: undefined,
  status: undefined
})

// 审核表单
const auditForm = reactive({
  comment: ''
})

// 获取课程班级列表
const loadClassList = async () => {
  try {
    const { data } = await getClassList()
    classList.value = data.records;
  } catch (error) {
    console.error('Failed to load class list:', error)
    ElMessage.error('获取课程班级列表失败')
  }
}

// 获取成绩列表
const getGrades = async () => {
  loading.value = true
  try {
    const { data } = await getGradeList(queryParams)
    gradeList.value = data.list
    total.value = data.total
  } catch (error) {
    console.error('Failed to get grades:', error)
    ElMessage.error('获取成绩列表失败')
  } finally {
    loading.value = false
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    pending: 'info',
    submitted: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    pending: '待提交',
    submitted: '待审核',
    approved: '已通过',
    rejected: '已驳回'
  }
  return texts[status] || status
}

// 提交审核
const handleSubmitAudit = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确认提交该课程班级的成绩进行审核吗？',
      '提示',
      {
        type: 'warning'
      }
    )
    
    await submitGradeAudit(row.classId)
    ElMessage.success('提交审核成功')
    getGrades()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to submit audit:', error)
      ElMessage.error('提交审核失败')
    }
  }
}

// 通过审核
const handleApprove = (row) => {
  auditAction.value = 'approve'
  currentRow.value = row
  auditDialogVisible.value = true
}

// 驳回审核
const handleReject = (row) => {
  auditAction.value = 'reject'
  currentRow.value = row
  auditDialogVisible.value = true
}

// 提交审核结果
const submitAudit = async () => {
  if (!auditForm.comment) {
    ElMessage.warning('请输入审核意见')
    return
  }

  try {
    await auditGrades({
      classId: currentRow.value.classId,
      action: auditAction.value,
      comment: auditForm.comment
    })
    
    ElMessage.success('审核操作成功')
    auditDialogVisible.value = false
    auditForm.comment = ''
    getGrades()
  } catch (error) {
    console.error('Failed to submit audit result:', error)
    ElMessage.error('审核操作失败')
  }
}

// 撤回发布
const handleUnpublish = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确认撤回该课程班级的成绩发布吗？',
      '提示',
      {
        type: 'warning'
      }
    )
    
    await unpublishGrades(row.classId)
    ElMessage.success('撤回发布成功')
    getGrades()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to unpublish grades:', error)
      ElMessage.error('撤回发布失败')
    }
  }
}

// 查看详情
const handleViewDetails = (row) => {
  router.push({
    path: '/academic/grade/view',
    query: { classId: row.classId }
  })
}

// 查询和重置
const handleQuery = () => {
  queryParams.pageNum = 1
  getGrades()
}

const resetQuery = () => {
  queryParams.status = undefined
  handleQuery()
}

const handleClassChange = () => {
  handleQuery()
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

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

.filter-container {
  padding-bottom: 20px;
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
