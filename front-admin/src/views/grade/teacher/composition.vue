<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>成绩构成设置</span>
          <el-button type="primary" @click="handleSave">保存设置</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="作业成绩占比" prop="homeworkWeight">
          <el-input-number
            v-model="form.homeworkWeight"
            :min="0"
            :max="100"
            :precision="0"
            :step="5"
          />
          <span class="unit">%</span>
        </el-form-item>
        <el-form-item label="实验成绩占比" prop="experimentWeight">
          <el-input-number
            v-model="form.experimentWeight"
            :min="0"
            :max="100"
            :precision="0"
            :step="5"
          />
          <span class="unit">%</span>
        </el-form-item>
        <el-form-item label="期末成绩占比" prop="finalExamWeight">
          <el-input-number
            v-model="form.finalExamWeight"
            :min="0"
            :max="100"
            :precision="0"
            :step="5"
          />
          <span class="unit">%</span>
        </el-form-item>
      </el-form>

      <div class="tips">
        <p>注意：所有占比之和必须等于100%</p>
        <el-divider />
        <h4>帮助说明：</h4>
        <ul>
          <li>如果所有权重都为0，表示各个学科的值不一致</li>
          <li>如果所有权重都为-1，表示没有数据</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getTeacherGradeComposition, updateTeacherGradeComposition } from '@/api/homework'

const userStore = useUserStore()
const formRef = ref()
const form = ref({
  homeworkWeight: 30,
  experimentWeight: 30,
  finalExamWeight: 40
})

const rules = {
  homeworkWeight: [
    { required: true, message: '请输入作业成绩占比', trigger: 'blur' },
    { type: 'number', message: '必须为数字' }
  ],
  experimentWeight: [
    { required: true, message: '请输入实验成绩占比', trigger: 'blur' },
    { type: 'number', message: '必须为数字' }
  ],
  finalExamWeight: [
    { required: true, message: '请输入期末成绩占比', trigger: 'blur' },
    { type: 'number', message: '必须为数字' }
  ]
}

const getComposition = async () => {
  try {
    const { data } = await getTeacherGradeComposition(userStore.userInfo?.teacherInfo?.id)
    if (data) {
      form.value = {
        homeworkWeight: data.homeworkWeight,
        experimentWeight: data.experimentWeight,
        finalExamWeight: data.finalExamWeight
      }
    }
  } catch (error) {
    console.error('获取成绩构成失败:', error)
    ElMessage.error('获取成绩构成失败')
  }
}

const handleSave = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      const total = form.value.homeworkWeight + form.value.experimentWeight + form.value.finalExamWeight
      if (total !== 100) {
        ElMessage.error('所有占比之和必须等于100%')
        return
      }

      try {
        await updateTeacherGradeComposition(userStore.userInfo?.teacherInfo?.id, {
          homeworkWeight: form.value.homeworkWeight,
          experimentWeight: form.value.experimentWeight,
          finalExamWeight: form.value.finalExamWeight
        })
        ElMessage.success('保存成功')
      } catch (error) {
        console.error('保存成绩构成失败:', error)
        ElMessage.error('保存成绩构成失败')
      }
    }
  })
}

onMounted(() => {
  getComposition()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.unit {
  margin-left: 8px;
  color: #666;
}

.tips {
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.tips p {
  margin: 0;
  line-height: 1.5;
}

.tips h4 {
  margin: 10px 0;
  color: #333;
}

.tips ul {
  margin: 0;
  padding-left: 20px;
}

.tips li {
  line-height: 1.8;
}
</style>
