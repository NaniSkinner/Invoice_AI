'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { AppLayout } from '@/components/AppLayout';
import { Card } from '@/components/ui/Card';
import { Loading } from '@/components/ui/Loading';
import { InvoiceForm } from '@/components/invoices/InvoiceForm';
import { getInvoiceById, updateInvoice } from '@/lib/api/invoices';
import { InvoiceDto } from '@/types/invoice';

export default function EditInvoicePage() {
  const router = useRouter();
  const params = useParams();
  const id = params.id as string;

  const [invoice, setInvoice] = useState<InvoiceDto | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    const fetchInvoice = async () => {
      try {
        setIsLoading(true);
        const data = await getInvoiceById(id);

        if (data.status !== 'DRAFT') {
          alert('Only draft invoices can be edited.');
          router.push(`/invoices/${id}`);
          return;
        }

        setInvoice(data);
      } catch (error) {
        console.error('Error fetching invoice:', error);
        alert('Failed to load invoice details.');
        router.push('/invoices');
      } finally {
        setIsLoading(false);
      }
    };

    fetchInvoice();
  }, [id, router]);

  const handleSubmit = async (data: any) => {
    try {
      setIsSaving(true);
      await updateInvoice(id, { ...data, status: 'DRAFT' });
      router.push(`/invoices/${id}`);
    } catch (error) {
      console.error('Error updating invoice:', error);
      alert('Failed to update invoice. Please try again.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleCancel = () => {
    router.push(`/invoices/${id}`);
  };

  if (isLoading) {
    return (
      <AppLayout>
        <Loading text="Loading invoice..." />
      </AppLayout>
    );
  }

  if (!invoice) {
    return null;
  }

  return (
    <AppLayout>
      <div className="max-w-5xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-900 mb-6">Edit Invoice {invoice.invoiceNumber}</h1>

        <Card>
          <InvoiceForm
            invoice={invoice}
            onSubmit={handleSubmit}
            onCancel={handleCancel}
            isLoading={isSaving}
          />
        </Card>
      </div>
    </AppLayout>
  );
}
