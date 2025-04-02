<template>
  <div class="app-container">
    <div class="filter-container">
      <el-button type="primary" class="filter-item" @click="handleAdd">新增班级</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="classList"
      style="width: 100%"
      border
    >
      <el-table-column prop="className" label="班级名称" width="180" />
      <el-table-column label="所属课程" width="180">
        <template #default="{ row }">
          {{ row.course?.name || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="任课教师" width="120">
        <template #default="{ row }">
          {{ row.teacher?.name || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="maxStudent" label="班级容量" width="100" />
      <el-table-column prop="classTime" label="上课时间" width="120" />
      <el-table-column prop="classroom" label="上课地点" width="120" />
      <el-table-column prop="semester" label="学期" width="150" />
      <el-table-column prop="isActive" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isActive ? 'success' : 'info'">
            {{ row.isActive ? '开放选课' : '已关闭' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="280">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="success" link @click="handleManageHomework(row)">作业管理</el-button>
          <el-button type="warning" link @click="handleManageGrade(row)">成绩管理</el-button>
          <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 班级信息表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增班级' : '编辑班级'"
      width="600px"
    >
      <el-form
        ref="classFormRef"
        :model="classForm"
        :rules="classRules"
        label-width="100px"
      >
        <el-form-item label="班级编号" prop="classId">
          <el-input v-model="classForm.classId" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="班级名称" prop="name">
          <el-input v-model="classForm.name" />
        </el-form-item>
        <el-form-item label="任课教师" prop="teacherId">
          <el-select v-model="classForm.teacherId" placeholder="请选择教师">
            <el-option
              v-for="item in teacherList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班级容量" prop="capacity">
          <el-input-number v-model="classForm.capacity" :min="1" :max="200" />
        </el-form-item>
        <el-form-item label="上课时间" prop="time">
          <el-input v-model="classForm.time" placeholder="如：周一 1-2节" />
        </el-form-item>
        <el-form-item label="上课地点" prop="location">
          <el-input v-model="classForm.location" />
        </el-form-item>
        <el-form-item label="状态" prop="isActive">
          <el-radio-group v-model="classForm.isActive">
            <el-radio :label="true">开放选课</el-radio>
            <el-radio :label="false">关闭选课</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCourseClassList,
  addCourseClass,
  updateCourseClass,
  deleteCourseClass
} from '@/api/course'
import { getTeacherList } from '@/api/teacher'

const route = useRoute()
const router = useRouter()
const courseId = route.params.courseId

const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')
const classList = ref([])
const teacherList = ref([])
const classFormRef = ref(null)

const classForm = reactive({
  classId: '',
  name: '',
  teacherId: '',
  capacity: 50,
  time: '',
  location: '',
  status: 1
})

const classRules = {
  classId: [
    { required: true, message: '请输入班级编号', trigger: 'blur' },
    { pattern: /^[A-Z0-9]{4,6}$/, message: '班级编号必须为4-6位大写字母和数字', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入班级名称', trigger: 'blur' }
  ],
  teacherId: [
    { required: true, message: '请选择任课教师', trigger: 'change' }
  ],
  capacity: [
    { required: true, message: '请输入班级容量', trigger: 'blur' }
  ],
  time: [
    { required: true, message: '请输入上课时间', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入上课地点', trigger: 'blur' }
  ]
}

// 获取班级列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await getCourseClassList(courseId)
    classList.value = data.records;
  } catch (error) {
    console.error('Failed to get class list:', error)
    ElMessage.error('获取班级列表失败')
  } finally {
    loading.value = false
  }
}

// 获取教师列表
const getTeachers = async () => {
  try {
    const { data } = await getTeacherList()
    teacherList.value = data.records;
  } catch (error) {
    console.error('Failed to get teacher list:', error)
    ElMessage.error('获取教师列表失败')
  }
}

const handleAdd = () => {
  dialogType.value = 'add'
  Object.assign(classForm, {
    classId: '',
    name: '',
    teacherId: '',
    capacity: 50,
    time: '',
    location: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.assign(classForm, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确认删除该班级吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteCourseClass(courseId, row.classId)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('Failed to delete class:', error)
      ElMessage.error('删除失败')
    }
  })
}

const handleManageHomework = (row) => {
  router.push({
    name: 'CourseHomework',
    params: { courseId, classId: row.classId }
  })
}

const handleManageGrade = (row) => {
  router.push({
    name: 'CourseGrade',
    params: { courseId, classId: row.classId }
  })
}

const handleSubmit = async () => {
  try {
    await classFormRef.value.validate()
    if (dialogType.value === 'add') {
      await addCourseClass(courseId, classForm)
      ElMessage.success('添加成功')
    } else {
      await updateCourseClass(courseId, classForm.classId, classForm)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('Failed to submit class form:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

onMounted(() => {
  if (!courseId) {
    ElMessage.error('课程ID不能为空')
    router.push('/academic/course')
    return
  }
  getList()
  getTeachers()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

.filter-container {
  padding-bottom: 10px;
  .filter-item {
    margin-right: 10px;
  }
}
</style>
