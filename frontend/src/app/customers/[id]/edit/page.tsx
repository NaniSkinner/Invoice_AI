'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { AppLayout } from '@/components/AppLayout';
import { Card } from '@/components/ui/Card';
import { Loading } from '@/components/ui/Loading';
import { CustomerForm } from '@/components/customers/CustomerForm';
import { getCustomerById, updateCustomer } from '@/lib/api/customers';
import { CustomerDto } from '@/types/customer';

export default function EditCustomerPage() {
  const router = useRouter();
  const params = useParams();
  const id = params.id as string;

  const [customer, setCustomer] = useState<CustomerDto | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    const fetchCustomer = async () => {
      try {
        setIsLoading(true);
        const data = await getCustomerById(id);
        setCustomer(data);
      } catch (error) {
        console.error('Error fetching customer:', error);
        alert('Failed to load customer details.');
        router.push('/customers');
      } finally {
        setIsLoading(false);
      }
    };

    fetchCustomer();
  }, [id, router]);

  const handleSubmit = async (data: any) => {
    try {
      setIsSaving(true);
      await updateCustomer(id, data);
      router.push(`/customers/${id}`);
    } catch (error) {
      console.error('Error updating customer:', error);
      alert('Failed to update customer. Please try again.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleCancel = () => {
    router.push(`/customers/${id}`);
  };

  if (isLoading) {
    return (
      <AppLayout>
        <Loading text="Loading customer details..." />
      </AppLayout>
    );
  }

  if (!customer) {
    return null;
  }

  return (
    <AppLayout>
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-900 mb-6">Edit Customer</h1>

        <Card>
          <CustomerForm
            customer={customer}
            onSubmit={handleSubmit}
            onCancel={handleCancel}
            isLoading={isSaving}
          />
        </Card>
      </div>
    </AppLayout>
  );
}
