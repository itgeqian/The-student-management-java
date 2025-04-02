<template>
  <div class="app-container">
    <div class="filter-container">
      <el-select
        v-model="selectedClass"
        placeholder="选择课程班级"
        style="width: 200px"
        class="filter-item"
        @change="handleClassChange"
      >
        <el-option
          v-for="item in classList"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
    </div>

    <el-card v-if="selectedClass" class="box-card">
      <template #header>
        <div class="card-header">
          <span>成绩构成设置</span>
        </div>
      </template>
      
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="作业成绩占比" prop="homeworkPercent">
          <el-input-number
            v-model="form.homeworkPercent"
            :min="0"
            :max="100"
            @change="handlePercentChange"
          />
          <span class="percent-symbol">%</span>
        </el-form-item>
        <el-form-item label="实验成绩占比" prop="labPercent">
          <el-input-number
            v-model="form.labPercent"
            :min="0"
            :max="100"
            @change="handlePercentChange"
          />
          <span class="percent-symbol">%</span>
        </el-form-item>
        <el-form-item label="期末成绩占比" prop="examPercent">
          <el-input-number
            v-model="form.examPercent"
            :min="0"
            :max="100"
            @change="handlePercentChange"
          />
          <span class="percent-symbol">%</span>
        </el-form-item>
        <el-form-item>
          <div class="total-percent">
            总计：{{ totalPercent }}%
            <el-tag
              :type="totalPercent === 100 ? 'success' : 'danger'"
              class="ml-2"
            >
              {{ totalPercent === 100 ? '比例合理' : '总比例必须为100%' }}
            </el-tag>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getClassList } from '@/api/course'
import { getCourseGradeStructure, setCourseGradeStructure } from '@/api/grade'

const classList = ref([])
const selectedClass = ref('')
const formRef = ref()

const form = ref({
  homeworkPercent: 0,
  labPercent: 0,
  examPercent: 0
})

const rules = {
  homeworkPercent: [
    { required: true, message: '请输入作业成绩占比', trigger: 'blur' }
  ],
  labPercent: [
    { required: true, message: '请输入实验成绩占比', trigger: 'blur' }
  ],
  examPercent: [
    { required: true, message: '请输入期末成绩占比', trigger: 'blur' }
  ]
}

const totalPercent = computed(() => {
  return form.value.homeworkPercent + form.value.labPercent + form.value.examPercent
})

const handlePercentChange = () => {
  if (totalPercent.value > 100) {
    ElMessage.warning('总比例不能超过100%')
  }
}

const handleClassChange = async (classId) => {
  try {
    const response = await getCourseGradeStructure(classId)
    form.value = response.data
  } catch (error) {
    console.error('获取成绩构成失败:', error)
    ElMessage.error('获取成绩构成失败')
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (totalPercent.value !== 100) {
      ElMessage.error('成绩占比总和必须为100%')
      return
    }
    
    await setCourseGradeStructure(selectedClass.value, form.value)
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

const getClasses = async () => {
  try {
    const response = await getClassList()
    classList.value = response.data
  } catch (error) {
    console.error('获取课程班级列表失败:', error)
    ElMessage.error('获取课程班级列表失败')
  }
}

onMounted(() => {
  getClasses()
})
</script>

<style scoped>
.filter-container {
  margin-bottom: 20px;
}
.percent-symbol {
  margin-left: 5px;
}
.total-percent {
  font-size: 16px;
  font-weight: bold;
}
.ml-2 {
  margin-left: 10px;
}
</style>
