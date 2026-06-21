// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /mail/getCode */
export async function sendMail(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.sendMailParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseString>('/mail/getCode', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
