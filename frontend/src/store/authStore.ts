import { create } from 'zustand';
import { getCurrentUser, UserInfo } from '@/lib/api/auth';

interface AuthState {
  user: UserInfo | null;
  isAuthenticated: boolean;
  setUser: (user: UserInfo | null) => void;
  initAuth: () => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,

  setUser: (user) => set({ user, isAuthenticated: !!user }),

  initAuth: () => {
    const user = getCurrentUser();
    set({ user, isAuthenticated: !!user });
  },

  clearAuth: () => set({ user: null, isAuthenticated: false }),
}));
