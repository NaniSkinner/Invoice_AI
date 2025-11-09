'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { AppLayout } from '@/components/AppLayout';
import { Card } from '@/components/ui/Card';
import { CustomerForm } from '@/components/customers/CustomerForm';
import { createCustomer } from '@/lib/api/customers';

export default function NewCustomerPage() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (data: any) => {
    try {
      setIsLoading(true);
      await createCustomer(data);
      router.push('/customers');
    } catch (error) {
      console.error('Error creating customer:', error);
      alert('Failed to create customer. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleCancel = () => {
    router.push('/customers');
  };

  return (
    <AppLayout>
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-900 mb-6">New Customer</h1>

        <Card>
          <CustomerForm onSubmit={handleSubmit} onCancel={handleCancel} isLoading={isLoading} />
        </Card>
      </div>
    </AppLayout>
  );
}
