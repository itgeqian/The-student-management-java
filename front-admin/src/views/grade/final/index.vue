<template>
  <div class="final-grade-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>期末成绩管理</span>
          <div class="header-operations">
            <el-button type="primary" @click="handleImport">导入成绩</el-button>
            <el-button type="success" @click="handleExport">导出成绩</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="课程">
          <el-select
            v-model="searchForm.courseId"
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
        <el-form-item label="学号">
          <el-input
            v-model="searchForm.studentId"
            placeholder="输入学号"
            clearable
          />
        </el-form-item>
        <el-form-item label="成绩范围">
          <el-input-number
            v-model="searchForm.minScore"
            :min="0"
            :max="100"
            placeholder="最低分"
            style="width: 120px"
          />
          <span class="range-separator">-</span>
          <el-input-number
            v-model="searchForm.maxScore"
            :min="0"
            :max="100"
            placeholder="最高分"
            style="width: 120px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        v-loading="tableLoading"
        :data="tableData"
        style="width: 100%"
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column label="平时成绩" width="100">
          <template #default="scope">
            {{ scope.row.usualScore }}
            <el-tooltip
              v-if="scope.row.usualDetail"
              effect="dark"
              placement="top"
            >
              <template #content>
                <div v-for="item in scope.row.usualDetail" :key="item.name">
                  {{ item.name }}: {{ item.score }}
                </div>
              </template>
              <el-icon class="detail-icon"><InfoFilled /></el-icon>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="midtermScore" label="期中成绩" width="100" />
        <el-table-column prop="finalScore" label="期末成绩" width="100" />
        <el-table-column prop="totalScore" label="总评成绩" width="100">
          <template #default="scope">
            <span :class="getScoreClass(scope.row.totalScore)">
              {{ scope.row.totalScore }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="rank" label="排名" width="100" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              link
              size="small"
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              type="primary"
              link
              size="small"
              @click="handleDetail(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 编辑成绩对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑成绩"
      width="500px"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="学生信息">
          {{ editForm.studentName }} ({{ editForm.studentId }})
        </el-form-item>
        <el-form-item label="平时成绩" prop="usualScore">
          <el-input-number
            v-model="editForm.usualScore"
            :min="0"
            :max="100"
            :precision="1"
          />
        </el-form-item>
        <el-form-item label="期中成绩" prop="midtermScore">
          <el-input-number
            v-model="editForm.midtermScore"
            :min="0"
            :max="100"
            :precision="1"
          />
        </el-form-item>
        <el-form-item label="期末成绩" prop="finalScore">
          <el-input-number
            v-model="editForm.finalScore"
            :min="0"
            :max="100"
            :precision="1"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="editForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入成绩"
      width="500px"
    >
      <el-upload
        class="upload-demo"
        drag
        action="/api/grade/import"
        :headers="headers"
        :on-success="handleImportSuccess"
        :on-error="handleImportError"
        :before-upload="beforeImport"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            请上传 Excel 文件（.xlsx, .xls），文件大小不超过 10MB
          </div>
        </template>
      </el-upload>
      <div class="import-template">
        <span>没有模板？</span>
        <el-button type="primary" link @click="downloadTemplate">
          下载模板
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled, UploadFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 搜索表单
const searchForm = reactive({
  courseId: '',
  studentId: '',
  minScore: null,
  maxScore: null
})

// 课程选项
const courseOptions = ref([])
const courseLoading = ref(false)

// 表格数据
const tableData = ref([])
const tableLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 编辑对话框
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  studentId: '',
  studentName: '',
  usualScore: 0,
  midtermScore: 0,
  finalScore: 0,
  remark: ''
})

// 编辑表单校验规则
const editRules = {
  usualScore: [
    { required: true, message: '请输入平时成绩', trigger: 'blur' }
  ],
  midtermScore: [
    { required: true, message: '请输入期中成绩', trigger: 'blur' }
  ],
  finalScore: [
    { required: true, message: '请输入期末成绩', trigger: 'blur' }
  ]
}

// 导入对话框
const importDialogVisible = ref(false)
const headers = {
  Authorization: userStore.token
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

// 获取成绩样式
const getScoreClass = (score) => {
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 70) return 'score-fair'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

// 搜索
const handleSearch = () => {
  fetchTableData()
}

// 重置搜索
const resetSearch = () => {
  searchForm.courseId = ''
  searchForm.studentId = ''
  searchForm.minScore = null
  searchForm.maxScore = null
  fetchTableData()
}

// 获取表格数据
const fetchTableData = () => {
  tableLoading.value = true
  // 模拟API调用
  setTimeout(() => {
    tableData.value = [
      {
        studentId: '2021001',
        studentName: '张三',
        className: '计科2101',
        usualScore: 85,
        midtermScore: 88,
        finalScore: 92,
        totalScore: 89,
        rank: 1,
        usualDetail: [
          { name: '作业1', score: 85 },
          { name: '作业2', score: 88 },
          { name: '实验', score: 90 }
        ]
      },
      {
        studentId: '2021002',
        studentName: '李四',
        className: '计科2101',
        usualScore: 78,
        midtermScore: 82,
        finalScore: 85,
        totalScore: 82,
        rank: 2
      }
    ]
    total.value = 2
    tableLoading.value = false
  }, 500)
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchTableData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchTableData()
}

// 编辑成绩
const handleEdit = (row) => {
  Object.assign(editForm, row)
  editDialogVisible.value = true
}

// 查看详情
const handleDetail = (row) => {
  console.log('查看详情', row)
}

// 提交编辑
const submitEdit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate((valid) => {
    if (valid) {
      // 这里应该调用API保存数据
      ElMessage.success('更新成功')
      editDialogVisible.value = false
      fetchTableData()
    }
  })
}

// 导入成绩
const handleImport = () => {
  importDialogVisible.value = true
}

// 导出成绩
const handleExport = () => {
  // 这里应该调用API导出成绩
  ElMessage.success('导出成功')
}

// 导入成功
const handleImportSuccess = () => {
  ElMessage.success('导入成功')
  importDialogVisible.value = false
  fetchTableData()
}

// 导入失败
const handleImportError = () => {
  ElMessage.error('导入失败')
}

// 导入前检查
const beforeImport = (file) => {
  const isExcel = file.type === 'application/vnd.ms-excel' || 
                  file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isExcel) {
    ElMessage.error('只能上传 Excel 文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  return true
}

// 下载模板
const downloadTemplate = () => {
  // 这里应该调用API下载模板
  console.log('下载模板')
}

// 初始化
fetchTableData()
</script>

<style scoped>
.final-grade-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.range-separator {
  margin: 0 10px;
  color: #909399;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.detail-icon {
  margin-left: 5px;
  color: #409eff;
  cursor: pointer;
}

.score-excellent {
  color: #67c23a;
  font-weight: bold;
}

.score-good {
  color: #409eff;
  font-weight: bold;
}

.score-fair {
  color: #e6a23c;
}

.score-pass {
  color: #909399;
}

.score-fail {
  color: #f56c6c;
  font-weight: bold;
}

.import-template {
  margin-top: 20px;
  text-align: center;
  color: #909399;
}

.el-upload {
  width: 100%;
}
</style>
