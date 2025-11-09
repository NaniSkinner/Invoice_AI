import { create } from 'zustand';
import { ChatMessage } from '@/types/chat';

interface ChatState {
  messages: ChatMessage[];
  isOpen: boolean;
  isLoading: boolean;
  conversationId: string | null;

  addMessage: (message: ChatMessage) => void;
  setMessages: (messages: ChatMessage[]) => void;
  setIsOpen: (isOpen: boolean) => void;
  setIsLoading: (isLoading: boolean) => void;
  setConversationId: (id: string | null) => void;
  clearMessages: () => void;
  toggleChat: () => void;
}

export const useChatStore = create<ChatState>((set) => ({
  messages: [],
  isOpen: false,
  isLoading: false,
  conversationId: null,

  addMessage: (message) =>
    set((state) => ({ messages: [...state.messages, message] })),

  setMessages: (messages) => set({ messages }),

  setIsOpen: (isOpen) => set({ isOpen }),

  setIsLoading: (isLoading) => set({ isLoading }),

  setConversationId: (id) => set({ conversationId: id }),

  clearMessages: () => set({ messages: [], conversationId: null }),

  toggleChat: () => set((state) => ({ isOpen: !state.isOpen })),
}));
