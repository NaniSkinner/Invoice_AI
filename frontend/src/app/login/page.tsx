'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { loginSchema } from '@/lib/validation';
import { login } from '@/lib/api/auth';
import { useAuthStore } from '@/store/authStore';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Card } from '@/components/ui/Card';

interface LoginFormData {
  username: string;
  password: string;
}

export default function LoginPage() {
  const router = useRouter();
  const { setUser } = useAuthStore();
  const [error, setError] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormData) => {
    try {
      setIsLoading(true);
      setError('');

      const user = login(data);
      setUser(user);

      // Redirect to dashboard
      router.push('/dashboard');
    } catch (err) {
      setError('Invalid credentials. Please try again.');
      console.error('Login error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-sage-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full">
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-gray-900">
            <span className="text-primary-600">Invoice</span>Me
          </h1>
          <p className="mt-2 text-gray-600">Sign in to your account</p>
        </div>

        <Card>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
                {error}
              </div>
            )}

            <Input
              label="Username"
              type="text"
              {...register('username')}
              error={errors.username?.message}
              placeholder="demo"
              autoComplete="username"
            />

            <Input
              label="Password"
              type="password"
              {...register('password')}
              error={errors.password?.message}
              placeholder="password"
              autoComplete="current-password"
            />

            <Button type="submit" className="w-full" isLoading={isLoading}>
              Sign in
            </Button>

            <div className="text-center text-sm text-gray-600">
              <p>Demo credentials:</p>
              <p className="font-mono mt-1">username: demo</p>
              <p className="font-mono">password: password</p>
            </div>
          </form>
        </Card>
      </div>
    </div>
  );
}
