'use client';

import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { paymentSchema } from '@/lib/validation';
import { Modal } from '@/components/ui/Modal';
import { Input } from '@/components/ui/Input';
import { Select } from '@/components/ui/Select';
import { Button } from '@/components/ui/Button';
import { formatDateForInput } from '@/lib/format';

interface RecordPaymentFormData {
  paymentAmount: number;
  paymentDate: string;
  paymentMethod: 'CREDIT_CARD' | 'BANK_TRANSFER' | 'CHECK' | 'CASH' | 'OTHER';
  transactionReference?: string;
  notes?: string;
}

interface RecordPaymentModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: RecordPaymentFormData) => void;
  maxAmount: number;
  isLoading?: boolean;
}

export const RecordPaymentModal: React.FC<RecordPaymentModalProps> = ({
  isOpen,
  onClose,
  onSubmit,
  maxAmount,
  isLoading = false,
}) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<RecordPaymentFormData>({
    resolver: zodResolver(paymentSchema.omit({ invoiceId: true })),
    defaultValues: {
      paymentAmount: maxAmount,
      paymentDate: formatDateForInput(new Date()),
      paymentMethod: 'BANK_TRANSFER',
    },
  });

  const handleFormSubmit = (data: RecordPaymentFormData) => {
    onSubmit(data);
    reset();
  };

  const handleClose = () => {
    reset();
    onClose();
  };

  const paymentMethodOptions = [
    { value: 'CREDIT_CARD', label: 'Credit Card' },
    { value: 'BANK_TRANSFER', label: 'Bank Transfer' },
    { value: 'CHECK', label: 'Check' },
    { value: 'CASH', label: 'Cash' },
    { value: 'OTHER', label: 'Other' },
  ];

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      title="Record Payment"
      footer={
        <div className="flex justify-end space-x-3">
          <Button variant="outline" onClick={handleClose} disabled={isLoading}>
            Cancel
          </Button>
          <Button onClick={handleSubmit(handleFormSubmit)} isLoading={isLoading}>
            Record Payment
          </Button>
        </div>
      }
    >
      <form className="space-y-4">
        <Input
          label="Payment Amount"
          type="number"
          step="0.01"
          {...register('paymentAmount', { valueAsNumber: true })}
          error={errors.paymentAmount?.message}
          required
          helperText={`Maximum: $${maxAmount.toFixed(2)}`}
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
      </form>
    </Modal>
  );
};
