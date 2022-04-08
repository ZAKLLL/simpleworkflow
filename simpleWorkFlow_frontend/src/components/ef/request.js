import axios from 'axios'

axios.defaults.baseURL = process.env.VUE_APP_BASE_API

// 创建axios实例
const service = axios.create({
  // baseURL: process.env.NODE_ENV === 'production' ? process.env.VUE_APP_BASE_API : '/', // api 的 base_url
  baseURL: "//127.0.0.1:8092"
})


export function apiSaveModel(modelInfo) {
  return service({
    url: 'model/insertOrUpdateModel',
    data: modelInfo,
    method: 'POST'
  })
}

export function apiGetModels() {
  return service({
    url: 'model/getModelConfigs',
    params: {
      page: 0,
      size: 100,
    },
    method: 'GET'
  })
}
export function apiDeploy(modelId) {
  return service({
    url: 'model/deploy',
    params: {
      modelId: modelId
    },
    method: 'GET'
  })
}

export function apiGetModelInstances(modelId) {
  return service({
    url: 'process/getModelProcessInstances',
    params: {
      modelId: modelId
    },
    method: 'GET'
  })
}

export function apiGetInstanceHistory(processInstanceId) {
  return service({
    url: 'process/getProcessHistory',
    params: {
      processInstanceId: processInstanceId
    },
    method: 'GET'
  })
}
export function apiGetInstanceNodeHistory(processInstanceId) {
  return service({
    url: 'process/getProcessNodeHistory',
    params: {
      processInstanceId: processInstanceId
    },
    method: 'GET'
  })
}


