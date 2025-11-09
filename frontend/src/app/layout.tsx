import type { Metadata } from 'next';
import './globals.css';
import { AuthProvider } from '@/contexts/AuthContext';
import ChatAssistant from '@/components/chat/ChatAssistant';

export const metadata: Metadata = {
  title: 'InvoiceMe - AI-Assisted Invoicing',
  description: 'Modern ERP invoicing system with AI features',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className="bg-gray-50">
        <AuthProvider>
          {children}
          <ChatAssistant />
        </AuthProvider>
      </body>
    </html>
  );
}
