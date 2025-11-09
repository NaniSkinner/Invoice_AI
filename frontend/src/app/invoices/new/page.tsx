'use client';

import { useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { AppLayout } from '@/components/AppLayout';
import { Card } from '@/components/ui/Card';
import { InvoiceForm } from '@/components/invoices/InvoiceForm';
import { createInvoice } from '@/lib/api/invoices';

export default function NewInvoicePage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const customerId = searchParams.get('customerId') || undefined;
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (data: any) => {
    try {
      setIsLoading(true);
      const invoice = await createInvoice(data);
      router.push(`/invoices/${invoice.id}`);
    } catch (error) {
      console.error('Error creating invoice:', error);
      alert('Failed to create invoice. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleCancel = () => {
    router.push('/invoices');
  };

  return (
    <AppLayout>
      <div className="max-w-5xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-900 mb-6">New Invoice</h1>

        <Card>
          <InvoiceForm
            onSubmit={handleSubmit}
            onCancel={handleCancel}
            isLoading={isLoading}
            preselectedCustomerId={customerId}
          />
        </Card>
      </div>
    </AppLayout>
  );
}
