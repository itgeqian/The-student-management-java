<template>
  <div class="app-container">
    <div class="content-container">
      <div class="filter-container">
        <el-button
          v-if="hasPermission(['ADMIN', 'teacher'])"
          type="primary"
          class="filter-item"
          @click="handleAdd"
        >
          新增班级
        </el-button>
      </div>

      <div class="table-container">
        <el-table v-loading="loading" :data="classList" border>
          <el-table-column prop="className" label="班级名称" />
          <el-table-column label="所属课程">
            <template #default="{ row }">
              {{ row.course?.name || "-" }}
            </template>
          </el-table-column>
          <el-table-column label="任课教师" width="120">
            <template #default="{ row }">
              {{ row.teacher?.name || "-" }}
            </template>
          </el-table-column>
          <el-table-column prop="maxStudent" label="班级容量" width="100" />
          <el-table-column prop="semester" label="学期">
            <template #default="{ row }">
              {{ getSemesterText(row.semester) }}
            </template>
          </el-table-column>
          <el-table-column prop="classTime" label="上课时间" width="150">
            <template #default="{ row }">
              {{ getClassTimeText(row.classTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="classroom" label="上课地点" width="120" />
          <el-table-column prop="isActive" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.isActive ? 'success' : 'info'">
                {{ row.isActive ? "开放选课" : "已关闭" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleEdit(row)"
                >编辑</el-button
              >
              <!-- <el-button type="success" link @click="handleManageHomework(row)"
                >作业管理</el-button
              >
              <el-button type="warning" link @click="handleManageGrade(row)"
                >成绩管理</el-button
              > -->
              <el-button type="danger" link @click="handleDelete(row)"
                >删除</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 新增/编辑班级对话框 -->
      <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
        <el-form
          ref="classFormRef"
          :model="classForm"
          :rules="classRules"
          label-width="100px"
        >
          <el-form-item label="课程" prop="courseId">
            <el-select v-model="classForm.courseId" placeholder="请选择课程">
              <el-option
                v-for="item in courseOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="班级名称" prop="className">
            <el-input v-model="classForm.className" />
          </el-form-item>
          <el-form-item label="任课教师" prop="teacherId">
            <el-select v-model="classForm.teacherId" placeholder="请选择">
              <el-option
                v-for="item in teacherOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="学期" prop="semester">
            <el-select v-model="classForm.semester" placeholder="请选择">
              <el-option
                v-for="item in semesterOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="容量" prop="capacity">
            <el-input-number v-model="classForm.capacity" :min="1" :max="200" />
          </el-form-item>
          <el-form-item label="上课时间" prop="classTime">
            <el-select
              v-model="classForm.classTime"
              placeholder="请选择上课时间"
              style="width: 100%"
            >
              <el-option
                v-for="item in classTimeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="上课地点" prop="classroom">
            <el-input
              v-model="classForm.classroom"
              placeholder="请输入上课地点"
            />
          </el-form-item>
          <el-form-item label="状态" prop="isActive">
            <el-select v-model="classForm.isActive" placeholder="请选择">
              <el-option label="开放选课" :value="true" />
              <el-option label="已关闭" :value="false" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="dialogVisible = false">取 消</el-button>
            <el-button type="primary" @click="handleSubmit">确 定</el-button>
          </div>
        </template>
      </el-dialog>

      <!-- 成绩构成对话框 -->
      <el-dialog
        title="成绩构成设置"
        v-model="gradeStructureVisible"
        width="500px"
      >
        <el-form
          ref="gradeStructureFormRef"
          :model="gradeStructureForm"
          :rules="gradeStructureRules"
          label-width="100px"
        >
          <el-form-item label="作业占比" prop="homeworkWeight">
            <el-input-number
              v-model="gradeStructureForm.homeworkWeight"
              :min="0"
              :max="100"
              style="width: 160px"
            />
            <span class="weight-unit">%</span>
          </el-form-item>
          <el-form-item label="实验占比" prop="experimentWeight">
            <el-input-number
              v-model="gradeStructureForm.experimentWeight"
              :min="0"
              :max="100"
              style="width: 160px"
            />
            <span class="weight-unit">%</span>
          </el-form-item>
          <el-form-item label="期末占比" prop="finalExamWeight">
            <el-input-number
              v-model="gradeStructureForm.finalExamWeight"
              :min="0"
              :max="100"
              style="width: 160px"
            />
            <span class="weight-unit">%</span>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="gradeStructureVisible = false">取消</el-button>
            <el-button type="primary" @click="handleGradeStructureSubmit"
              >确 定</el-button
            >
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  getCourseList,
  getCourseClassList,
  updateCourseClass,
  createCourseClass,
  deleteCourseClass,
} from "@/api/course";
import { getTeacherListNoPage } from "@/api/teacher";
import { hasPermission } from "@/utils/permission";
import {
  semesterOptions,
  classTimeOptions,
  getSemesterText,
  getClassTimeText,
} from "@/constants/options";

// 列表数据
const loading = ref(false);
const classList = ref([]);

// 查询参数
const queryParams = reactive({
  semester: "",
  current: 1,
  size: 10,
});

// 教师选项
const teacherOptions = ref([]);

// 课程选项
const courseOptions = ref([]);

// 班级表单
const dialogVisible = ref(false);
const dialogTitle = ref("新增班级");
const classFormRef = ref(null);
const classForm = reactive({
  id: "",
  courseId: "",
  className: "",
  teacherId: "",
  semester: "",
  capacity: 50,
  classTime: "",
  classroom: "",
  status: 1,
});
const classRules = {
  courseId: [{ required: true, message: "请选择课程", trigger: "change" }],
  className: [{ required: true, message: "请输入班级名称", trigger: "blur" }],
  teacherId: [{ required: true, message: "请选择任课教师", trigger: "change" }],
  semester: [{ required: true, message: "请选择学期", trigger: "change" }],
  capacity: [
    { required: true, message: "请输入容量", trigger: "blur" },
    {
      type: "number",
      min: 1,
      max: 200,
      message: "容量必须在1-200之间",
      trigger: "blur",
    },
  ],
  classTime: [{ required: true, message: "请选择上课时间", trigger: "change" }],
  classroom: [{ required: true, message: "请输入上课地点", trigger: "blur" }],
};

// 成绩构成表单
const gradeStructureVisible = ref(false);
const gradeStructureFormRef = ref(null);
const gradeStructureForm = reactive({
  classId: "",
  homeworkWeight: 30,
  experimentWeight: 30,
  finalExamWeight: 40,
});
const gradeStructureRules = {
  homeworkWeight: [
    { required: true, message: "请输入作业占比", trigger: "blur" },
    {
      type: "number",
      min: 0,
      max: 100,
      message: "占比必须在0-100之间",
      trigger: "blur",
    },
  ],
  experimentWeight: [
    { required: true, message: "请输入实验占比", trigger: "blur" },
    {
      type: "number",
      min: 0,
      max: 100,
      message: "占比必须在0-100之间",
      trigger: "blur",
    },
  ],
  finalExamWeight: [
    { required: true, message: "请输入期末占比", trigger: "blur" },
    {
      type: "number",
      min: 0,
      max: 100,
      message: "占比必须在0-100之间",
      trigger: "blur",
    },
  ],
};

// 获取班级列表
const getList = async () => {
  loading.value = true;
  try {
    const { data } = await getCourseClassList(queryParams);
    classList.value = data.records;
  } catch (error) {
    console.error("获取班级列表失败:", error);
    ElMessage.error("获取班级列表失败");
  } finally {
    loading.value = false;
  }
};

// 获取教师列表
const getTeachers = async () => {
  try {
    const { data } = await getTeacherListNoPage();
    teacherOptions.value = data.records.map((teacher) => ({
      id: teacher.id,
      name: teacher.name,
    }));
  } catch (error) {
    console.error("获取教师列表失败:", error);
    ElMessage.error("获取教师列表失败");
  }
};

// 获取课程列表
const getCourses = async () => {
  try {
    const { data } = await getCourseList();
    courseOptions.value = data.records.map((course) => ({
      value: course.id,
      label: course.name,
    }));
  } catch (error) {
    console.error("获取课程列表失败:", error);
    ElMessage.error("获取课程列表失败");
  }
};

// 新增班级
const handleAdd = () => {
  dialogTitle.value = "新增班级";
  Object.keys(classForm).forEach((key) => {
    classForm[key] = key === "capacity" ? 50 : key === "status" ? 1 : "";
  });
  dialogVisible.value = true;
};

// 编辑班级
const handleEdit = (row) => {
  dialogTitle.value = "编辑班级";
  Object.assign(classForm, row);
  dialogVisible.value = true;
};

// 删除班级
const handleDelete = (row) => {
  ElMessageBox.confirm("确认删除该班级吗？", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    try {
      await deleteCourseClass(row.id);
      ElMessage.success("删除成功");
      getList();
    } catch (error) {
      console.error("删除班级失败:", error);
      ElMessage.error("删除班级失败");
    }
  });
};

// 查看学生名单
const handleViewStudents = (row) => {
  router.push({
    name: "ClassStudents",
    params: { id: row.id },
  });
};

// 成绩构成设置
const handleGradeStructure = async (row) => {
  try {
    const { data } = await getGradeStructure(row.id);
    gradeStructureForm.classId = row.id;
    Object.assign(gradeStructureForm, {
      homeworkWeight: data.homeworkWeight,
      experimentWeight: data.experimentWeight,
      finalExamWeight: data.finalExamWeight,
    });
    gradeStructureVisible.value = true;
  } catch (error) {
    console.error("获取成绩构成失败:", error);
    ElMessage.error("获取成绩构成失败");
  }
};

// 作业管理
const handleHomework = (row) => {
  router.push({
    name: "ClassHomework",
    params: { id: row.id },
  });
};

// 成绩管理
const handleGrade = (row) => {
  router.push({
    name: "ClassGrade",
    params: { id: row.id },
  });
};

// 提交班级表单
const handleSubmit = async () => {
  if (!classFormRef.value) return;

  try {
    await classFormRef.value.validate();
    if (classForm.id) {
      await updateCourseClass(classForm);
      ElMessage.success("更新成功");
    } else {
      await createCourseClass(classForm);
      ElMessage.success("创建成功");
    }
    dialogVisible.value = false;
    getList();
  } catch (error) {
    console.error("提交班级失败:", error);
    ElMessage.error(error.message || "提交失败");
  }
};

// 提交成绩构成
const handleGradeStructureSubmit = async () => {
  if (!gradeStructureFormRef.value) return;

  try {
    await gradeStructureFormRef.value.validate();
    // 检查总和是否为100
    const total =
      gradeStructureForm.homeworkWeight +
      gradeStructureForm.experimentWeight +
      gradeStructureForm.finalExamWeight;
    if (total !== 100) {
      ElMessage.error("各项占比之和必须为100%");
      return;
    }
    await updateGradeStructure(gradeStructureForm.classId, {
      homeworkWeight: gradeStructureForm.homeworkWeight,
      experimentWeight: gradeStructureForm.experimentWeight,
      finalExamWeight: gradeStructureForm.finalExamWeight,
    });
    ElMessage.success("更新成功");
    gradeStructureVisible.value = false;
  } catch (error) {
    console.error("提交成绩构成失败:", error);
    ElMessage.error(error.message || "提交失败");
  }
};

onMounted(() => {
  getList();
  getTeachers();
  getCourses();
});
</script>

<style scoped>
/* 移除所有局部样式，使用全局统一样式 */
</style>
