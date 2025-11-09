'use client';

import React, { useState, useEffect } from 'react';
import { useForm, useFieldArray } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { invoiceSchema } from '@/lib/validation';
import { Input } from '@/components/ui/Input';
import { Select } from '@/components/ui/Select';
import { Button } from '@/components/ui/Button';
import { getAllCustomers } from '@/lib/api/customers';
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
  const [customers, setCustomers] = useState<CustomerDto[]>([]);
  const [subtotal, setSubtotal] = useState(0);
  const [taxRate] = useState(0.1); // 10% tax
  const [taxAmount, setTaxAmount] = useState(0);
  const [total, setTotal] = useState(0);

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
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      {/* Customer & Dates */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Select
          label="Customer"
          {...register('customerId')}
          error={errors.customerId?.message}
          options={customers.map((c) => ({ value: c.id, label: c.businessName }))}
          required
          disabled={!!invoice} // Can't change customer on existing invoice
        />

        <Input
          label="Issue Date"
          type="date"
          {...register('issueDate')}
          error={errors.issueDate?.message}
          required
        />

        <Input
          label="Due Date"
          type="date"
          {...register('dueDate')}
          error={errors.dueDate?.message}
          required
        />
      </div>

      {/* Line Items */}
      <div className="space-y-4">
        <div className="flex justify-between items-center">
          <h3 className="text-lg font-semibold">Line Items</h3>
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={() => append({ description: '', quantity: 1, unitPrice: 0 })}
          >
            Add Item
          </Button>
        </div>

        {/* Column Headers */}
        <div className="flex gap-3 items-center">
          <div className="flex-1">
            <label className="block text-sm font-medium text-gray-700">Item</label>
          </div>
          <div className="w-24">
            <label className="block text-sm font-medium text-gray-700">Qty</label>
          </div>
          <div className="w-32">
            <label className="block text-sm font-medium text-gray-700">Unit Price</label>
          </div>
          {fields.length > 1 && (
            <div className="w-20">
              {/* Spacer for remove button */}
            </div>
          )}
        </div>

        <div className="space-y-3">
          {fields.map((field, index) => (
            <div key={field.id} className="flex gap-3 items-start">
              <div className="flex-1">
                <Input
                  {...register(`lineItems.${index}.description`)}
                  error={errors.lineItems?.[index]?.description?.message}
                  placeholder="Description"
                />
              </div>
              <div className="w-24">
                <Input
                  type="number"
                  step="1"
                  {...register(`lineItems.${index}.quantity`, { valueAsNumber: true })}
                  error={errors.lineItems?.[index]?.quantity?.message}
                  placeholder="Qty"
                />
              </div>
              <div className="w-32">
                <div className="relative">
                  <span className="absolute left-3 top-2 text-gray-400 pointer-events-none">$</span>
                  <Input
                    type="text"
                    {...register(`lineItems.${index}.unitPrice`, {
                      valueAsNumber: true,
                      setValueAs: (v) => v === '' ? 0 : parseFloat(v) || 0
                    })}
                    error={errors.lineItems?.[index]?.unitPrice?.message}
                    placeholder="0.00"
                    className="pl-7"
                    onKeyPress={(e) => {
                      // Only allow numbers and decimal point
                      if (!/[0-9.]/.test(e.key)) {
                        e.preventDefault();
                      }
                    }}
                  />
                </div>
              </div>
              {fields.length > 1 && (
                <Button
                  type="button"
                  variant="danger"
                  size="sm"
                  onClick={() => remove(index)}
                >
                  Remove
                </Button>
              )}
            </div>
          ))}
        </div>

        {/* Totals */}
        <div className="border-t pt-4 space-y-2">
          <div className="flex justify-between text-sm">
            <span className="font-medium">Subtotal:</span>
            <span>{formatCurrency(subtotal)}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="font-medium">Tax (10%):</span>
            <span>{formatCurrency(taxAmount)}</span>
          </div>
          <div className="flex justify-between text-lg font-bold border-t pt-2">
            <span>Total:</span>
            <span>{formatCurrency(total)}</span>
          </div>
        </div>
      </div>

      {/* Additional Options */}
      <div className="space-y-4">
        <div className="flex items-center">
          <input
            type="checkbox"
            id="allowsPartialPayment"
            {...register('allowsPartialPayment')}
            className="h-4 w-4 rounded border-gray-300"
          />
          <label htmlFor="allowsPartialPayment" className="ml-2 text-sm text-gray-700">
            Allow partial payments
          </label>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Notes</label>
            <textarea
              {...register('notes')}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Internal notes..."
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Terms</label>
            <textarea
              {...register('terms')}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Payment terms..."
            />
          </div>
        </div>
      </div>

      {/* Form Actions */}
      <div className="flex justify-end space-x-4">
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" isLoading={isLoading}>
          {invoice ? 'Update Invoice' : 'Create Invoice'}
        </Button>
      </div>
    </form>
  );
};
