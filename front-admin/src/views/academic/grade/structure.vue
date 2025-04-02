<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>成绩构成设置</span>
          <el-button type="primary" @click="handleSave">保存设置</el-button>
        </div>
      </template>
      
      <el-form
        ref="gradeFormRef"
        :model="gradeForm"
        :rules="gradeRules"
        label-width="120px"
      >
        <el-form-item label="作业成绩占比" prop="homeworkWeight">
          <el-input-number
            v-model="gradeForm.homeworkWeight"
            :min="0"
            :max="100"
            :step="5"
          >
            <template #suffix>%</template>
          </el-input-number>
        </el-form-item>
        <el-form-item label="实验成绩占比" prop="practiceWeight">
          <el-input-number
            v-model="gradeForm.practiceWeight"
            :min="0"
            :max="100"
            :step="5"
          >
            <template #suffix>%</template>
          </el-input-number>
        </el-form-item>
        <el-form-item label="期末成绩占比" prop="finalWeight">
          <el-input-number
            v-model="gradeForm.finalWeight"
            :min="0"
            :max="100"
            :step="5"
          >
            <template #suffix>%</template>
          </el-input-number>
        </el-form-item>
      </el-form>

      <div class="weight-chart" ref="chartRef"></div>

      <div class="tips">
        <el-alert
          title="提示"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <p>1. 各项成绩占比之和必须为100%</p>
            <p>2. 设置成绩占比后，系统将按照设置的比例自动计算学生的总成绩</p>
            <p>3. 成绩占比一经设置，建议不要随意更改，以免影响学生成绩的计算</p>
          </template>
        </el-alert>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getCourseGradeStructure, setCourseGradeStructure } from '@/api/course'

const route = useRoute()
const router = useRouter()
const courseId = route.params.courseId
const classId = route.params.classId

const gradeFormRef = ref(null)
const chartRef = ref(null)
let chart = null

const gradeForm = reactive({
  homeworkWeight: 30,
  practiceWeight: 30,
  finalWeight: 40
})

const gradeRules = {
  homeworkWeight: [
    { required: true, message: '请输入作业成绩占比', trigger: 'blur' }
  ],
  practiceWeight: [
    { required: true, message: '请输入实验成绩占比', trigger: 'blur' }
  ],
  finalWeight: [
    { required: true, message: '请输入期末成绩占比', trigger: 'blur' }
  ]
}

// 获取成绩构成
const getGradeStructure = async () => {
  try {
    const { data } = await getCourseGradeStructure(courseId)
    Object.assign(gradeForm, {
      homeworkWeight: data.homeworkWeight,
      practiceWeight: data.practiceWeight,
      finalWeight: data.finalWeight
    })
  } catch (error) {
    console.error('Failed to get grade structure:', error)
    ElMessage.error('获取成绩构成失败')
  }
}

// 更新饼图
const updateChart = () => {
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const option = {
    title: {
      text: '成绩构成比例',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}%'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        type: 'pie',
        radius: '50%',
        data: [
          { value: gradeForm.homeworkWeight, name: '作业成绩' },
          { value: gradeForm.practiceWeight, name: '实验成绩' },
          { value: gradeForm.finalWeight, name: '期末成绩' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }

  chart.setOption(option)
}

// 监听窗口大小变化
window.addEventListener('resize', () => {
  chart?.resize()
})

// 监听表单数据变化
watch(
  () => [gradeForm.homeworkWeight, gradeForm.practiceWeight, gradeForm.finalWeight],
  () => {
    updateChart()
  }
)

const validateWeights = () => {
  const total = gradeForm.homeworkWeight + gradeForm.practiceWeight + gradeForm.finalWeight
  if (total !== 100) {
    ElMessage.error('各项成绩占比之和必须为100%')
    return false
  }
  return true
}

const handleSave = async () => {
  if (!validateWeights()) return

  try {
    await gradeFormRef.value.validate()
    await setCourseGradeStructure(courseId, gradeForm)
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('Failed to save grade structure:', error)
    ElMessage.error(error.message || '保存失败')
  }
}

onMounted(() => {
  if (!courseId || !classId) {
    ElMessage.error('参数错误')
    router.push('/academic/course')
    return
  }
  getGradeStructure()
  // 等待DOM更新后初始化图表
  nextTick(() => {
    updateChart()
  })
})

onBeforeUnmount(() => {
  chart?.dispose()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.weight-chart {
  height: 400px;
  margin: 20px 0;
}

.tips {
  margin-top: 20px;

  p {
    margin: 5px 0;
    line-height: 1.5;
  }
}

:deep(.el-input-number) {
  width: 180px;
}
</style>
