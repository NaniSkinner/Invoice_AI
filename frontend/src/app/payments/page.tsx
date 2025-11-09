'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { AppLayout } from '@/components/AppLayout';
import { Card } from '@/components/ui/Card';
import { Table } from '@/components/ui/Table';
import { Loading } from '@/components/ui/Loading';
import { getAllPayments } from '@/lib/api/payments';
import { PaymentDto } from '@/types/payment';
import { formatDate, formatCurrency, formatPaymentMethod } from '@/lib/format';

export default function PaymentsPage() {
  const router = useRouter();
  const [payments, setPayments] = useState<PaymentDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchPayments = async () => {
      try {
        setIsLoading(true);
        const data = await getAllPayments();
        setPayments(data);
      } catch (error) {
        console.error('Error fetching payments:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchPayments();
  }, []);

  if (isLoading) {
    return (
      <AppLayout>
        <Loading text="Loading payments..." />
      </AppLayout>
    );
  }

  const totalPayments = payments.reduce((sum, payment) => sum + payment.paymentAmount, 0);

  return (
    <AppLayout>
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <h1 className="text-3xl font-bold text-gray-900">Payments</h1>
        </div>

        <Card>
          <div className="mb-4 p-4 bg-primary-50 border border-primary-200 rounded-lg">
            <p className="text-sm text-gray-600">Total Payments Received</p>
            <p className="text-3xl font-bold text-primary-600">{formatCurrency(totalPayments)}</p>
          </div>

          <Table
            data={payments}
            columns={[
              { header: 'Invoice #', accessor: 'invoiceNumber' },
              { header: 'Payment Date', accessor: (row) => formatDate(row.paymentDate) },
              { header: 'Amount', accessor: (row) => formatCurrency(row.paymentAmount) },
              { header: 'Method', accessor: (row) => formatPaymentMethod(row.paymentMethod) },
              { header: 'Reference', accessor: (row) => row.transactionReference || '-' },
              { header: 'Notes', accessor: (row) => row.notes || '-' },
            ]}
            onRowClick={(payment) => router.push(`/payments/${payment.id}`)}
            emptyMessage="No payments recorded yet."
          />
        </Card>
      </div>
    </AppLayout>
  );
}
