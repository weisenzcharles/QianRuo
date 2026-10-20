import request from '@/utils/request'
import { ElLoading, ElMessage } from 'element-plus'
import { saveAs } from 'file-saver'
import errorCode from '@/utils/errorCode'
import { blobValidate } from '@/utils/ruoyi'

let downloadLoadingInstance

export default {
  zip(url, name) {
    downloadLoadingInstance = ElLoading.service({ text: "正在下载数据，请稍候", background: "rgba(0, 0, 0, 0.7)", })
    request({
      method: 'get',
      url: url,
      responseType: 'blob'
    }).then((data) => {
      const isBlob = blobValidate(data)
      if (isBlob) {
        const blob = new Blob([data], { type: 'application/zip' })
        this.saveAs(blob, name)
      } else {
        this.printErrMsg(data)
      }
      downloadLoadingInstance.close()
    }).catch((r) => {
      console.error(r)
      ElMessage.error('下载文件出现错误，请联系管理员！')
      downloadLoadingInstance.close()
    })
  },
  saveAs(text, name, opts) {
    saveAs(text, name, opts)
  },
  async printErrMsg(data) {
    const resText = await data.text()
    const rspObj = JSON.parse(resText)
    const errMsg = errorCode[rspObj.code] || rspObj.msg || errorCode['default']
    ElMessage.error(errMsg)
  }
}

