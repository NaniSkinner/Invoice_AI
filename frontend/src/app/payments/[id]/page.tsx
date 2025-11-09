'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import { AppLayout } from '@/components/AppLayout';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Loading } from '@/components/ui/Loading';
import { getPaymentById } from '@/lib/api/payments';
import { getInvoiceById } from '@/lib/api/invoices';
import { PaymentDto } from '@/types/payment';
import { InvoiceDto } from '@/types/invoice';
import { formatDate, formatCurrency, formatPaymentMethod, formatDateTime } from '@/lib/format';

export default function PaymentDetailPage() {
  const router = useRouter();
  const params = useParams();
  const id = params.id as string;

  const [payment, setPayment] = useState<PaymentDto | null>(null);
  const [invoice, setInvoice] = useState<InvoiceDto | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true);
        const paymentData = await getPaymentById(id);
        setPayment(paymentData);

        const invoiceData = await getInvoiceById(paymentData.invoiceId);
        setInvoice(invoiceData);
      } catch (error) {
        console.error('Error fetching payment:', error);
        alert('Failed to load payment details.');
        router.push('/payments');
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, [id, router]);

  if (isLoading) {
    return (
      <AppLayout>
        <Loading text="Loading payment details..." />
      </AppLayout>
    );
  }

  if (!payment) {
    return null;
  }

  return (
    <AppLayout>
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Payment Details</h1>
            <p className="text-gray-600 mt-1">Payment ID: {payment.id}</p>
          </div>
          <Link href="/payments">
            <Button variant="outline">Back to Payments</Button>
          </Link>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Card title="Payment Information">
            <div className="space-y-4">
              <div>
                <span className="text-sm font-medium text-gray-600">Payment Amount:</span>
                <p className="text-2xl font-bold text-green-600">{formatCurrency(payment.paymentAmount)}</p>
              </div>
              <div>
                <span className="text-sm font-medium text-gray-600">Payment Date:</span>
                <p className="text-gray-900">{formatDate(payment.paymentDate)}</p>
              </div>
              <div>
                <span className="text-sm font-medium text-gray-600">Payment Method:</span>
                <p className="text-gray-900">{formatPaymentMethod(payment.paymentMethod)}</p>
              </div>
              {payment.transactionReference && (
                <div>
                  <span className="text-sm font-medium text-gray-600">Transaction Reference:</span>
                  <p className="text-gray-900 font-mono">{payment.transactionReference}</p>
                </div>
              )}
              <div>
                <span className="text-sm font-medium text-gray-600">Recorded:</span>
                <p className="text-gray-900">{formatDateTime(payment.createdAt)}</p>
              </div>
            </div>
          </Card>

          <Card title="Invoice Information">
            <div className="space-y-4">
              <div>
                <span className="text-sm font-medium text-gray-600">Invoice Number:</span>
                <p className="text-gray-900">{payment.invoiceNumber}</p>
              </div>
              {invoice && (
                <>
                  <div>
                    <span className="text-sm font-medium text-gray-600">Customer:</span>
                    <p className="text-gray-900">{invoice.customerName}</p>
                  </div>
                  <div>
                    <span className="text-sm font-medium text-gray-600">Invoice Total:</span>
                    <p className="text-gray-900">{formatCurrency(invoice.totalAmount)}</p>
                  </div>
                  <div>
                    <span className="text-sm font-medium text-gray-600">Amount Paid:</span>
                    <p className="text-green-600 font-semibold">{formatCurrency(invoice.amountPaid)}</p>
                  </div>
                  <div>
                    <span className="text-sm font-medium text-gray-600">Balance Remaining:</span>
                    <p className="text-red-600 font-semibold">{formatCurrency(invoice.balanceRemaining)}</p>
                  </div>
                  <div className="pt-4">
                    <Link href={`/invoices/${invoice.id}`}>
                      <Button variant="secondary" className="w-full">
                        View Invoice
                      </Button>
                    </Link>
                  </div>
                </>
              )}
            </div>
          </Card>
        </div>

        {payment.notes && (
          <Card title="Notes">
            <p className="text-gray-900 whitespace-pre-wrap">{payment.notes}</p>
          </Card>
        )}
      </div>
    </AppLayout>
  );
}
