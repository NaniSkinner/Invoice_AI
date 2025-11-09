'use client';

import React from 'react';
import { useAuth } from '@/contexts/AuthContext';
import ChatBubble from './ChatBubble';
import ChatWindow from './ChatWindow';

/**
 * Chat Assistant wrapper that only shows when user is authenticated
 */
export default function ChatAssistant() {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return null;
  }

  return (
    <>
      <ChatWindow />
      <ChatBubble />
    </>
  );
}
