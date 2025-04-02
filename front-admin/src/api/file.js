import request from '@/utils/request'

// 上传文件
export function uploadFile(data) {
  return request({
    url: '/api/file/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除文件
export function deleteFile(fileUrl) {
  return request({
    url: '/api/file',
    method: 'delete',
    params: {
      fileUrl
    }
  })
}

// 下载文件
export function downloadFile(fileUrl) {
  return request({
    url: '/api/file/download',
    method: 'get',
    params: {
      fileUrl
    },
    responseType: 'blob'
  })
}
