/**
 * Authentication helper functions
 */

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface UserInfo {
  username: string;
  isAuthenticated: boolean;
}

/**
 * Login user with Basic Auth
 */
export const login = (credentials: LoginCredentials): UserInfo => {
  const encodedCredentials = btoa(`${credentials.username}:${credentials.password}`);

  if (typeof window !== 'undefined') {
    localStorage.setItem('auth_credentials', encodedCredentials);
    localStorage.setItem('user_info', JSON.stringify({
      username: credentials.username,
      isAuthenticated: true,
    }));
  }

  return {
    username: credentials.username,
    isAuthenticated: true,
  };
};

/**
 * Logout user
 */
export const logout = (): void => {
  if (typeof window !== 'undefined') {
    localStorage.removeItem('auth_credentials');
    localStorage.removeItem('user_info');
  }
};

/**
 * Get current user info
 */
export const getCurrentUser = (): UserInfo | null => {
  if (typeof window !== 'undefined') {
    const userInfo = localStorage.getItem('user_info');
    if (userInfo) {
      return JSON.parse(userInfo);
    }
  }
  return null;
};

/**
 * Check if user is authenticated
 */
export const isAuthenticated = (): boolean => {
  if (typeof window !== 'undefined') {
    return !!localStorage.getItem('auth_credentials');
  }
  return false;
};
