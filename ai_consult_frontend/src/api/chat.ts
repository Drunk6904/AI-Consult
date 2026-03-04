import http from './axios'

export interface SourceItem {
  docName: string
  snippet: string
  locator?: string
  score?: number
}

export interface ChatMessage {
  senderType: 'user' | 'ai'
  content: string
  createdAt: string
  sources: SourceItem[]
}

export interface ChatHistory {
  conversationId: string
  messages: ChatMessage[]
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
 * 新建会话。
 */
export async function createConversation(): Promise<{ conversationId: string }> {
  const { data } = await http.post<ApiResponse<{ conversationId: string }>>('/chat/new')
  return unwrap(data)
}

/**
 * 发送消息。
 */
export async function sendMessage(
  conversationId: string,
  content: string
): Promise<{ conversationId: string; aiReply: string; sources: SourceItem[] }> {
  const { data } = await http.post<
    ApiResponse<{ conversationId: string; aiReply: string; sources: SourceItem[] }>
  >('/chat/send', {
    conversationId,
    content
  })
  return unwrap(data)
}

/**
 * 查询历史消息。
 */
export async function getHistory(conversationId: string): Promise<ChatHistory> {
  const { data } = await http.get<ApiResponse<ChatHistory>>('/chat/history', {
    params: { conversationId }
  })
  return unwrap(data)
}
