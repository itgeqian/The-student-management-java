<template>
  <div class="assignment-create-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑作业' : '新建作业' }}</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="assignment-form"
      >
        <el-form-item label="作业标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入作业标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="所属课程" prop="courseId">
          <el-select
            v-model="form.courseId"
            placeholder="选择课程"
            filterable
            remote
            :remote-method="handleCourseSearch"
            :loading="courseLoading"
            style="width: 100%"
          >
            <el-option
              v-for="item in courseOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="作业说明" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入作业说明"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="时间设置" required>
          <el-col :span="11">
            <el-form-item prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="开始时间"
                style="width: 100%"
                :disabled-date="disabledStartDate"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="2" class="text-center">
            <span class="text-gray-500">-</span>
          </el-col>
          <el-col :span="11">
            <el-form-item prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="截止时间"
                style="width: 100%"
                :disabled-date="disabledEndDate"
                :disabled-time="disabledEndTime"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-form-item>

        <el-form-item label="附件" prop="attachments">
          <el-upload
            class="upload-demo"
            action="/api/assignment/upload"
            :headers="headers"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            multiple
          >
            <el-button type="primary">上传附件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持 doc, docx, pdf, zip 等格式文件，单个文件不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="评分标准" prop="scoreStandard">
          <el-input
            v-model="form.scoreStandard"
            type="textarea"
            :rows="3"
            placeholder="请输入评分标准说明"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="总分" prop="totalScore">
          <el-input-number
            v-model="form.totalScore"
            :min="0"
            :max="100"
            :step="5"
            step-strictly
          />
        </el-form-item>

        <el-form-item label="提交要求">
          <el-checkbox-group v-model="form.submitRequirements">
            <el-checkbox label="requireAttachment">必须上传附件</el-checkbox>
            <el-checkbox label="requireComment">必须填写说明</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit">保存</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isEdit = computed(() => route.query.id !== undefined)

// 表单ref
const formRef = ref(null)

// 表单数据
const form = ref({
  title: '',
  courseId: undefined,
  description: '',
  startTime: '',
  endTime: '',
  attachments: [],
  scoreStandard: '',
  totalScore: 100,
  submitRequirements: ['requireAttachment']
})

// 表单校验规则
const rules = {
  title: [
    { required: true, message: '请输入作业标题', trigger: 'blur' },
    { min: 3, max: 100, message: '长度在 3 到 100 个字符', trigger: 'blur' }
  ],
  courseId: [
    { required: true, message: '请选择课程', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入作业说明', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择截止时间', trigger: 'change' }
  ],
  scoreStandard: [
    { required: true, message: '请输入评分标准', trigger: 'blur' }
  ],
  totalScore: [
    { required: true, message: '请设置总分', trigger: 'change' }
  ]
}

// 课程选项
const courseOptions = ref([])
const courseLoading = ref(false)

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

// 日期禁用
const disabledStartDate = (time) => {
  if (form.value.endTime) {
    return time.getTime() > new Date(form.value.endTime).getTime()
  }
}

const disabledEndDate = (time) => {
  if (form.value.startTime) {
    return time.getTime() < new Date(form.value.startTime).getTime()
  }
}

const disabledEndTime = (time) => {
  if (form.value.startTime) {
    return time.getTime() < new Date(form.value.startTime).getTime()
  }
}

// 上传相关
const headers = {
  Authorization: userStore.token
}

const handlePreview = (file) => {
  console.log('预览文件', file)
}

const handleRemove = (file, fileList) => {
  console.log('移除文件', file, fileList)
}

const handleUploadSuccess = (response, file, fileList) => {
  form.value.attachments.push({
    name: file.name,
    url: response.url
  })
  ElMessage.success('上传成功')
}

const handleUploadError = () => {
  ElMessage.error('上传失败')
}

const beforeUpload = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  return true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (valid) {
      // 这里应该调用API保存数据
      ElMessage({
        type: 'success',
        message: isEdit.value ? '更新成功' : '创建成功'
      })
      router.push('/assignment/list')
    }
  })
}

// 取消
const handleCancel = () => {
  router.back()
}

// 获取作业详情
const getAssignmentDetail = () => {
  if (isEdit.value) {
    // 这里应该调用API获取作业详情
    console.log('获取作业详情', route.query.id)
  }
}

onMounted(() => {
  getAssignmentDetail()
})
</script>

<style scoped>
.assignment-create-container {
  padding: 20px;
}

.assignment-form {
  max-width: 800px;
  margin: 0 auto;
}

.text-center {
  text-align: center;
  line-height: 32px;
}

.upload-demo {
  width: 360px;
}

.el-upload__tip {
  line-height: 1.2;
  margin-top: 5px;
  color: #909399;
}
</style>
