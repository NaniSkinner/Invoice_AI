'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { paymentSchema } from '@/lib/validation';
import axios from 'axios';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Select } from '@/components/ui/Select';
import { Loading } from '@/components/ui/Loading';
import { Badge, getInvoiceStatusBadgeVariant } from '@/components/ui/Badge';
import { Table } from '@/components/ui/Table';
import { InvoiceDto } from '@/types/invoice';
import { formatDate, formatCurrency, formatInvoiceStatus, formatDateForInput } from '@/lib/format';

interface PaymentFormData {
  paymentAmount: number;
  paymentDate: string;
  paymentMethod: 'CREDIT_CARD' | 'BANK_TRANSFER' | 'CHECK' | 'CASH' | 'OTHER';
  transactionReference?: string;
  notes?: string;
}

export default function PublicPaymentPage() {
  const params = useParams();
  const link = params.link as string;

  const [invoice, setInvoice] = useState<InvoiceDto | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [paymentSuccess, setPaymentSuccess] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<PaymentFormData>({
    resolver: zodResolver(paymentSchema.omit({ invoiceId: true })),
    defaultValues: {
      paymentDate: formatDateForInput(new Date()),
      paymentMethod: 'BANK_TRANSFER',
    },
  });

  useEffect(() => {
    const fetchInvoice = async () => {
      try {
        setIsLoading(true);
        // Use axios directly without auth for public endpoint
        const response = await axios.get(
          `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'}/invoices/payment-link/${link}`
        );
        setInvoice(response.data);

        // Set default payment amount to remaining balance
        reset({
          paymentAmount: response.data.balanceRemaining,
          paymentDate: formatDateForInput(new Date()),
          paymentMethod: 'BANK_TRANSFER',
        });
      } catch (error) {
        console.error('Error fetching invoice:', error);
        alert('Invalid or expired payment link.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchInvoice();
  }, [link]);

  const onSubmit = async (data: PaymentFormData) => {
    if (!invoice) return;

    try {
      setIsSubmitting(true);
      // Use axios directly without auth for public endpoint
      await axios.post(
        `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'}/payments`,
        {
          invoiceId: invoice.id,
          ...data,
        }
      );
      setPaymentSuccess(true);
    } catch (error) {
      console.error('Error recording payment:', error);
      alert('Failed to record payment. Please try again or contact support.');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <Loading text="Loading invoice..." />
      </div>
    );
  }

  if (!invoice) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <Card className="max-w-md">
          <div className="text-center">
            <h2 className="text-2xl font-bold text-red-600 mb-4">Invalid Payment Link</h2>
            <p className="text-gray-600">
              This payment link is invalid or has expired. Please contact the business for assistance.
            </p>
          </div>
        </Card>
      </div>
    );
  }

  if (paymentSuccess) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4">
        <div className="max-w-2xl w-full">
          <Card>
            <div className="text-center">
              <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-100 mb-4">
                <svg
                  className="h-6 w-6 text-green-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M5 13l4 4L19 7"
                  />
                </svg>
              </div>
              <h2 className="text-3xl font-bold text-gray-900 mb-4">Payment Received!</h2>
              <p className="text-lg text-gray-600 mb-6">
                Thank you for your payment. A confirmation has been sent to your email.
              </p>
              <div className="bg-gray-50 rounded-lg p-4">
                <p className="text-sm text-gray-600">Invoice Number</p>
                <p className="text-xl font-semibold text-gray-900">{invoice.invoiceNumber}</p>
              </div>
            </div>
          </Card>
        </div>
      </div>
    );
  }

  const paymentMethodOptions = [
    { value: 'CREDIT_CARD', label: 'Credit Card' },
    { value: 'BANK_TRANSFER', label: 'Bank Transfer' },
    { value: 'CHECK', label: 'Check' },
    { value: 'CASH', label: 'Cash' },
    { value: 'OTHER', label: 'Other' },
  ];

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-gray-900">InvoiceMe</h1>
          <p className="text-gray-600 mt-2">Invoice Payment Portal</p>
        </div>

        {/* Invoice Details */}
        <Card>
          <div className="flex justify-between items-start mb-6">
            <div>
              <h2 className="text-2xl font-bold text-gray-900">Invoice {invoice.invoiceNumber}</h2>
              <p className="text-gray-600 mt-1">{invoice.customerName}</p>
            </div>
            <Badge variant={getInvoiceStatusBadgeVariant(invoice.status)} className="text-base px-4 py-2">
              {formatInvoiceStatus(invoice.status)}
            </Badge>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
            <div>
              <span className="text-sm font-medium text-gray-600">Issue Date:</span>
              <p className="text-gray-900">{formatDate(invoice.issueDate)}</p>
            </div>
            <div>
              <span className="text-sm font-medium text-gray-600">Due Date:</span>
              <p className="text-gray-900">{formatDate(invoice.dueDate)}</p>
            </div>
            <div>
              <span className="text-sm font-medium text-gray-600">Total Amount:</span>
              <p className="text-xl font-bold text-gray-900">{formatCurrency(invoice.totalAmount)}</p>
            </div>
          </div>

          {/* Line Items */}
          <div className="mb-6">
            <h3 className="text-lg font-semibold mb-3">Items</h3>
            <Table
              data={invoice.lineItems}
              columns={[
                { header: 'Description', accessor: 'description' },
                { header: 'Quantity', accessor: (row) => row.quantity.toString() },
                { header: 'Unit Price', accessor: (row) => formatCurrency(row.unitPrice) },
                { header: 'Total', accessor: (row) => formatCurrency(row.lineTotal) },
              ]}
              emptyMessage="No items"
            />
          </div>

          {/* Totals */}
          <div className="border-t pt-4 space-y-2">
            <div className="flex justify-between text-sm">
              <span className="font-medium">Subtotal:</span>
              <span>{formatCurrency(invoice.subtotal)}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="font-medium">Tax:</span>
              <span>{formatCurrency(invoice.taxAmount)}</span>
            </div>
            <div className="flex justify-between text-lg font-bold border-t pt-2">
              <span>Total:</span>
              <span>{formatCurrency(invoice.totalAmount)}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="font-medium text-green-600">Amount Paid:</span>
              <span className="text-green-600">{formatCurrency(invoice.amountPaid)}</span>
            </div>
            <div className="flex justify-between text-lg font-bold text-red-600 border-t pt-2">
              <span>Balance Due:</span>
              <span>{formatCurrency(invoice.balanceRemaining)}</span>
            </div>
          </div>
        </Card>

        {/* Payment Form */}
        {invoice.balanceRemaining > 0 && invoice.status === 'SENT' && (
          <Card title="Make a Payment">
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              <Input
                label="Payment Amount"
                type="number"
                step="0.01"
                {...register('paymentAmount', { valueAsNumber: true })}
                error={errors.paymentAmount?.message}
                required
                helperText={`Maximum: ${formatCurrency(invoice.balanceRemaining)}`}
              />

              <Input
                label="Payment Date"
                type="date"
                {...register('paymentDate')}
                error={errors.paymentDate?.message}
                required
              />

              <Select
                label="Payment Method"
                {...register('paymentMethod')}
                error={errors.paymentMethod?.message}
                options={paymentMethodOptions}
                required
              />

              <Input
                label="Transaction Reference"
                {...register('transactionReference')}
                error={errors.transactionReference?.message}
                placeholder="Optional reference number"
              />

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Notes</label>
                <textarea
                  {...register('notes')}
                  rows={3}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Optional payment notes..."
                />
              </div>

              <Button type="submit" className="w-full" isLoading={isSubmitting}>
                Submit Payment
              </Button>
            </form>
          </Card>
        )}

        {invoice.balanceRemaining === 0 && (
          <Card>
            <div className="text-center py-8">
              <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-100 mb-4">
                <svg
                  className="h-6 w-6 text-green-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M5 13l4 4L19 7"
                  />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">Invoice Paid in Full</h3>
              <p className="text-gray-600">This invoice has been completely paid. Thank you!</p>
            </div>
          </Card>
        )}

        {invoice.terms && (
          <Card title="Payment Terms">
            <p className="text-gray-900 whitespace-pre-wrap">{invoice.terms}</p>
          </Card>
        )}
      </div>
    </div>
  );
}
