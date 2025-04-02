// 学期选项
export const semesterOptions = [
  { value: '2023-2024-1', label: '2023-2024学年第一学期' },
  { value: '2023-2024-2', label: '2023-2024学年第二学期' },
  { value: '2024-2025-1', label: '2024-2025学年第一学期' },
  { value: '2024-2025-2', label: '2024-2025学年第二学期' }
]

// 上课时间选项
export const classTimeOptions = [
  { label: '周一 1-2节', value: 'MON_1_2' },
  { label: '周一 3-4节', value: 'MON_3_4' },
  { label: '周一 5-6节', value: 'MON_5_6' },
  { label: '周一 7-8节', value: 'MON_7_8' },
  { label: '周二 1-2节', value: 'TUE_1_2' },
  { label: '周二 3-4节', value: 'TUE_3_4' },
  { label: '周二 5-6节', value: 'TUE_5_6' },
  { label: '周二 7-8节', value: 'TUE_7_8' },
  { label: '周三 1-2节', value: 'WED_1_2' },
  { label: '周三 3-4节', value: 'WED_3_4' },
  { label: '周三 5-6节', value: 'WED_5_6' },
  { label: '周三 7-8节', value: 'WED_7_8' },
  { label: '周四 1-2节', value: 'THU_1_2' },
  { label: '周四 3-4节', value: 'THU_3_4' },
  { label: '周四 5-6节', value: 'THU_5_6' },
  { label: '周四 7-8节', value: 'THU_7_8' },
  { label: '周五 1-2节', value: 'FRI_1_2' },
  { label: '周五 3-4节', value: 'FRI_3_4' },
  { label: '周五 5-6节', value: 'FRI_5_6' },
  { label: '周五 7-8节', value: 'FRI_7_8' }
]

// 作业类型选项
export const homeworkTypeOptions = [
  { value: 'HOMEWORK', label: '平时作业' },
  { value: 'EXPERIMENT', label: '实验作业' },
  { value: 'EXAM', label: '期末考试' }
]

// 获取上课时间显示文本
export const getClassTimeText = (value) => {
  const option = classTimeOptions.find(item => item.value === value)
  return option ? option.label : '-'
}

// 获取学期显示文本
export const getSemesterText = (value) => {
  const option = semesterOptions.find(item => item.value === value)
  return option ? option.label : '-'
}

// 获取作业类型显示文本
export const getHomeworkTypeText = (value) => {
  const option = homeworkTypeOptions.find(item => item.value === value)
  return option ? option.label : '-'
}
