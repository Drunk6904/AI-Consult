import http from './axios'

export interface UserInfo {
  userId: string
  phone: string
  isRegistered: boolean
}

export interface AuthResponse {
  token: string
  user: UserInfo
}

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

function unwrap<T>(resp: ApiResponse<T>): T {
  if (resp.code !== 0) {
    throw new Error(resp.message || '请求失败')
  }
  return resp.data
}

/**
 * 注册接口调用。
 */
export async function register(phone: string, password: string): Promise<AuthResponse> {
  const { data } = await http.post<ApiResponse<AuthResponse>>('/auth/register', { phone, password })
  return unwrap(data)
}

/**
 * 登录接口调用。
 */
export async function login(phone: string, password: string): Promise<AuthResponse> {
  const { data } = await http.post<ApiResponse<AuthResponse>>('/auth/login', { phone, password })
  return unwrap(data)
}

/**
 * 获取当前用户信息。
 */
export async function me(): Promise<UserInfo> {
  const { data } = await http.get<ApiResponse<UserInfo>>('/me')
  return unwrap(data)
}
