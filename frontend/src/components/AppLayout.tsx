'use client';

import React from 'react';
import { Sidebar } from './Nav/Sidebar';
import { Header } from './Nav/Header';
import { ProtectedRoute } from './ProtectedRoute';

interface AppLayoutProps {
  children: React.ReactNode;
}

export const AppLayout: React.FC<AppLayoutProps> = ({ children }) => {
  return (
    <ProtectedRoute>
      <div className="flex min-h-screen">
        <Sidebar />
        <div className="flex-1 ml-64">
          <Header />
          <main className="mt-20 p-6">{children}</main>
        </div>
      </div>
    </ProtectedRoute>
  );
};
