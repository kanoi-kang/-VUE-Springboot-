import request from '@/utils/request'
import type { Address } from '@/types'

export const getAddresses = (): Promise<Address[]> => {
  return request.get('/user/addresses')
}

export const getAddress = (id: number): Promise<Address> => {
  return request.get(`/user/addresses/${id}`)
}

export const createAddress = (data: { consignee: string; phone: string; province: string; city: string; district: string; detailAddress: string; isDefault?: number; postalCode?: string; tag?: string }) => {
  return request.post('/user/addresses', data)
}

export const updateAddress = (id: number, data: { consignee?: string; phone?: string; province?: string; city?: string; district?: string; detailAddress?: string; isDefault?: number; postalCode?: string; tag?: string }) => {
  return request.put(`/user/addresses/${id}`, data)
}

export const deleteAddress = (id: number) => {
  return request.delete(`/user/addresses/${id}`)
}

export const setDefaultAddress = (id: number) => {
  return request.put(`/user/addresses/${id}/default`)
}
