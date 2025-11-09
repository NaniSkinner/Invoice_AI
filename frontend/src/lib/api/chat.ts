import axiosInstance from './axios-instance';
import { ChatMessageRequest, ChatMessageResponse } from '@/types/chat';

/**
 * Send a chat message to the AI assistant
 */
export async function sendChatMessage(
  request: ChatMessageRequest
): Promise<ChatMessageResponse> {
  const response = await axiosInstance.post<ChatMessageResponse>('/chat/message', request);
  return response.data;
}
