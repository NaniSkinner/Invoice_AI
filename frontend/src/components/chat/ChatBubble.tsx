'use client';

import React from 'react';
import { useChatStore } from '@/store/chatStore';

/**
 * Floating chat bubble button that toggles the chat window
 */
export default function ChatBubble() {
  const { isOpen, toggleChat, messages } = useChatStore();

  // Count unread messages (simple implementation - could be enhanced)
  const unreadCount = 0; // We could track this if needed

  return (
    <button
      onClick={toggleChat}
      className={`fixed bottom-6 right-6 z-50 rounded-full bg-blue-600 p-4 text-white shadow-lg transition-all hover:bg-blue-700 hover:shadow-xl focus:outline-none focus:ring-4 focus:ring-blue-300 ${
        isOpen ? 'scale-90' : 'scale-100'
      }`}
      aria-label="Toggle chat"
    >
      {/* Chat Icon */}
      <svg
        xmlns="http://www.w3.org/2000/svg"
        className="h-6 w-6"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        strokeWidth={2}
      >
        {isOpen ? (
          // X icon when open
          <>
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
          </>
        ) : (
          // Chat bubble icon when closed
          <>
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
            />
          </>
        )}
      </svg>

      {/* Unread badge (if needed in future) */}
      {unreadCount > 0 && (
        <span className="absolute -top-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-red-500 text-xs font-bold">
          {unreadCount > 9 ? '9+' : unreadCount}
        </span>
      )}
    </button>
  );
}
