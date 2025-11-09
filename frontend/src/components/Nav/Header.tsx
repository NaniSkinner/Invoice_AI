'use client';

import { useRouter } from 'next/navigation';
import { useAuth } from '@/contexts/AuthContext';
import { logout } from '@/lib/api/auth';
import { useAuthStore } from '@/store/authStore';
import { Button } from '@/components/ui/Button';

export const Header: React.FC = () => {
  const router = useRouter();
  const { user } = useAuth();
  const { clearAuth } = useAuthStore();

  const handleLogout = () => {
    logout();
    clearAuth();
    router.push('/login');
  };

  return (
    <header className="bg-white shadow-sm border-b border-gray-200 fixed top-0 left-64 right-0 z-10">
      <div className="px-6 py-4 flex justify-between items-center">
        <div>
          <h2 className="text-xl font-semibold text-gray-800">Welcome back!</h2>
          {user && <p className="text-sm text-gray-600">Logged in as: {user.username}</p>}
        </div>

        <div className="flex items-center space-x-4">
          <Button variant="outline" onClick={handleLogout}>
            Logout
          </Button>
        </div>
      </div>
    </header>
  );
};
