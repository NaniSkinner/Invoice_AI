'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useForm, useFieldArray } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { invoiceSchema } from '@/lib/validation';
import { Input } from '@/components/ui/Input';
import { Select } from '@/components/ui/Select';
import { Button } from '@/components/ui/Button';
import { EmailPreviewModal } from './EmailPreviewModal';
import { getAllCustomers } from '@/lib/api/customers';
import { sendInvoice } from '@/lib/api/invoices';
import { CustomerDto } from '@/types/customer';
import { InvoiceDto } from '@/types/invoice';
import { formatCurrency, formatDateForInput } from '@/lib/format';

interface LineItem {
  description: string;
  quantity: number;
  unitPrice: number;
}

interface InvoiceFormData {
  customerId: string;
  issueDate: string;
  dueDate: string;
  allowsPartialPayment: boolean;
  notes?: string;
  terms?: string;
  lineItems: LineItem[];
}

interface InvoiceFormProps {
  invoice?: InvoiceDto;
  onSubmit: (data: InvoiceFormData) => void;
  onCancel: () => void;
  isLoading?: boolean;
  preselectedCustomerId?: string;
}

export const InvoiceForm: React.FC<InvoiceFormProps> = ({
  invoice,
  onSubmit,
  onCancel,
  isLoading = false,
  preselectedCustomerId,
}) => {
  const router = useRouter();
  const [customers, setCustomers] = useState<CustomerDto[]>([]);
  const [subtotal, setSubtotal] = useState(0);
  const [taxRate] = useState(0.1); // 10% tax
  const [taxAmount, setTaxAmount] = useState(0);
  const [total, setTotal] = useState(0);
  const [showEmailModal, setShowEmailModal] = useState(false);
  const [isSending, setIsSending] = useState(false);

  const {
    register,
    handleSubmit,
    control,
    watch,
    formState: { errors },
  } = useForm<InvoiceFormData>({
    resolver: zodResolver(invoiceSchema),
    defaultValues: invoice
      ? {
          customerId: invoice.customerId,
          issueDate: formatDateForInput(invoice.issueDate),
          dueDate: formatDateForInput(invoice.dueDate),
          allowsPartialPayment: invoice.allowsPartialPayment,
          notes: invoice.notes,
          terms: invoice.terms,
          lineItems: invoice.lineItems.map((item) => ({
            description: item.description,
            quantity: item.quantity,
            unitPrice: item.unitPrice,
          })),
        }
      : {
          customerId: preselectedCustomerId || '',
          issueDate: formatDateForInput(new Date()),
          dueDate: formatDateForInput(new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)), // 30 days from now
          allowsPartialPayment: false,
          lineItems: [{ description: '', quantity: 1, unitPrice: 0 }],
        },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'lineItems',
  });

  const lineItems = watch('lineItems');

  useEffect(() => {
    const fetchCustomers = async () => {
      try {
        const data = await getAllCustomers();
        setCustomers(data.filter((c) => c.active));
      } catch (error) {
        console.error('Error fetching customers:', error);
      }
    };

    fetchCustomers();
  }, []);

  useEffect(() => {
    const calculatedSubtotal = lineItems.reduce(
      (sum, item) => sum + (item.quantity || 0) * (item.unitPrice || 0),
      0
    );
    const calculatedTax = calculatedSubtotal * taxRate;
    const calculatedTotal = calculatedSubtotal + calculatedTax;

    setSubtotal(calculatedSubtotal);
    setTaxAmount(calculatedTax);
    setTotal(calculatedTotal);
  }, [lineItems, taxRate]);

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex gap-6">
      {/* Left Side - Form Fields */}
      <div className="flex-1 bg-white rounded-card shadow-card p-6 space-y-6">
        {/* Invoice Number & Date */}
        <div className="grid grid-cols-2 gap-4">
          <Input
            label="Invoice Number"
            value={invoice?.invoiceNumber || "Auto-generated"}
            disabled
            className="bg-gray-50"
          />
          <Input
            label="Date"
            type="date"
            {...register('issueDate')}
            error={errors.issueDate?.message}
            required
          />
        </div>

        {/* Client */}
        <Select
          label="Client"
          {...register('customerId')}
          error={errors.customerId?.message}
          options={customers.map((c) => ({ value: c.id, label: c.businessName }))}
          required
          disabled={!!invoice}
        />

        {/* Company Name - Read only from selected customer */}
        <Input
          label="Company Name"
          value={customers.find(c => c.id === watch('customerId'))?.businessName || ''}
          disabled
          className="bg-gray-50"
        />

        {/* Company Address - Read only from selected customer */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Company Address</label>
          <textarea
            value={(() => {
              const customer = customers.find(c => c.id === watch('customerId'));
              if (!customer) return '';
              const addr = customer.billingAddress;
              return `${addr.street}\n${addr.city}, ${addr.state} ${addr.postalCode}\n${addr.country}`;
            })()}
            disabled
            rows={3}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-50 text-gray-600 focus:outline-none"
          />
        </div>

        {/* Company Email - Read only from selected customer */}
        <Input
          label="Company Email"
          value={customers.find(c => c.id === watch('customerId'))?.email || ''}
          disabled
          className="bg-gray-50"
        />

        {/* Service Name & Details (similar to line items but simplified) */}
        <div className="grid grid-cols-2 gap-4">
          <Input
            label="Service Name"
            {...register('lineItems.0.description')}
            error={errors.lineItems?.[0]?.description?.message}
            placeholder="Web Design and Development"
          />
          <Select
            label="Service Details"
            options={[
              { value: 'web-development', label: 'Web Development' },
              { value: 'design', label: 'Design' },
              { value: 'consulting', label: 'Consulting' },
              { value: 'maintenance', label: 'Maintenance' },
            ]}
          />
        </div>

        {/* Due Date & Subtotal */}
        <div className="grid grid-cols-2 gap-4">
          <Input
            label="Due Date"
            type="date"
            {...register('dueDate')}
            error={errors.dueDate?.message}
            required
          />
          <Input
            label="Subtotal"
            type="number"
            step="0.01"
            {...register('lineItems.0.unitPrice', { valueAsNumber: true })}
            error={errors.lineItems?.[0]?.unitPrice?.message}
            placeholder="0.0"
          />
        </div>

        {/* Hidden quantity field (default to 1) */}
        <input type="hidden" {...register('lineItems.0.quantity')} value={1} />

        {/* Additional Options */}
        <div className="space-y-4 pt-4 border-t border-gray-100">
          <div className="flex items-center">
            <input
              type="checkbox"
              id="allowsPartialPayment"
              {...register('allowsPartialPayment')}
              className="h-4 w-4 rounded border-gray-300 text-primary-600 focus:ring-primary-500"
            />
            <label htmlFor="allowsPartialPayment" className="ml-2 text-sm text-gray-700">
              Allow partial payments
            </label>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Notes</label>
            <textarea
              {...register('notes')}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors"
              placeholder="Internal notes..."
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Terms</label>
            <textarea
              {...register('terms')}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors"
              placeholder="Payment terms..."
            />
          </div>
        </div>
      </div>

      {/* Right Side - Action Buttons */}
      <div className="w-64 space-y-3">
        <Button
          type="submit"
          variant="primary"
          className="w-full justify-center"
          isLoading={isLoading}
        >
          <svg className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4" />
          </svg>
          Save as Draft
        </Button>

        {invoice && invoice.status === 'DRAFT' && (
          <Button
            type="button"
            variant="success"
            className="w-full justify-center"
            onClick={() => setShowEmailModal(true)}
          >
            <svg className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
            </svg>
            Send Invoice
          </Button>
        )}

        <div className="pt-3 border-t border-gray-200">
          <Button
            type="button"
            variant="outline"
            className="w-full justify-center text-gray-600"
            onClick={onCancel}
          >
            Cancel
          </Button>
        </div>
      </div>

      {/* Email Preview Modal */}
      {invoice && showEmailModal && (
        <EmailPreviewModal
          isOpen={showEmailModal}
          onClose={() => setShowEmailModal(false)}
          onConfirmSend={async () => {
            try {
              setIsSending(true);
              await sendInvoice(invoice.id);
              setShowEmailModal(false);
              router.push('/invoices');
            } catch (error) {
              console.error('Error sending invoice:', error);
              alert('Failed to send invoice. Please try again.');
            } finally {
              setIsSending(false);
            }
          }}
          onEdit={() => {
            setShowEmailModal(false);
          }}
          invoice={invoice}
          isLoading={isSending}
        />
      )}
    </form>
  );
};
