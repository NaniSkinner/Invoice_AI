'use client';

import React, { useState } from 'react';
import { Select } from '@/components/ui/Select';

interface CancelInvoiceModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (reason: string, customReason?: string) => void;
  invoiceNumber: string;
  isLoading?: boolean;
}

const CANCELLATION_REASONS = [
  { value: 'customer_request', label: 'Customer Request' },
  { value: 'billing_error', label: 'Billing Error' },
  { value: 'duplicate_invoice', label: 'Duplicate Invoice' },
  { value: 'service_not_provided', label: 'Service Not Provided' },
  { value: 'pricing_error', label: 'Pricing Error' },
  { value: 'other', label: 'Other' },
];

export const CancelInvoiceModal: React.FC<CancelInvoiceModalProps> = ({
  isOpen,
  onClose,
  onConfirm,
  invoiceNumber,
  isLoading = false,
}) => {
  const [selectedReason, setSelectedReason] = useState('');
  const [customReason, setCustomReason] = useState('');

  if (!isOpen) return null;

  const handleConfirm = () => {
    if (!selectedReason) {
      alert('Please select a cancellation reason');
      return;
    }

    if (selectedReason === 'other' && !customReason.trim()) {
      alert('Please specify the cancellation reason');
      return;
    }

    // Get the label for the selected reason
    const reasonLabel = CANCELLATION_REASONS.find(r => r.value === selectedReason)?.label || selectedReason;

    // Pass the label as first param, and customReason as second param only if 'other' is selected
    if (selectedReason === 'other' && customReason.trim()) {
      onConfirm(reasonLabel, customReason.trim());
    } else {
      onConfirm(reasonLabel, undefined);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg max-w-lg w-full overflow-hidden shadow-2xl">
        {/* Header */}
        <div className="bg-red-600 p-6 text-center">
          <div className="mx-auto w-16 h-16 bg-white rounded-full flex items-center justify-center mb-4">
            <svg
              className="w-10 h-10 text-red-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
              />
            </svg>
          </div>
          <h2 className="text-2xl font-bold text-white">Cancel Invoice</h2>
        </div>

        {/* Content */}
        <div className="p-6">
          <p className="text-gray-800 text-center mb-6">
            You are about to cancel invoice <span className="font-bold">#{invoiceNumber}</span>.
            This action cannot be undone.
          </p>

          {/* Reason Selection */}
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Cancellation Reason *
              </label>
              <select
                value={selectedReason}
                onChange={(e) => {
                  setSelectedReason(e.target.value);
                  if (e.target.value !== 'other') {
                    setCustomReason('');
                  }
                }}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                disabled={isLoading}
              >
                <option value="">Select a reason...</option>
                {CANCELLATION_REASONS.map((reason) => (
                  <option key={reason.value} value={reason.value}>
                    {reason.label}
                  </option>
                ))}
              </select>
            </div>

            {/* Custom Reason Input */}
            {selectedReason === 'other' && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Please Specify *
                </label>
                <textarea
                  value={customReason}
                  onChange={(e) => setCustomReason(e.target.value)}
                  rows={3}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                  placeholder="Enter the reason for cancellation..."
                  disabled={isLoading}
                />
              </div>
            )}

            {/* Warning Box */}
            <div className="bg-red-50 border border-red-200 rounded-lg p-4">
              <div className="flex">
                <svg
                  className="w-5 h-5 text-red-600 mt-0.5 mr-3 flex-shrink-0"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path
                    fillRule="evenodd"
                    d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
                    clipRule="evenodd"
                  />
                </svg>
                <div className="flex-1">
                  <p className="text-sm font-semibold text-red-800">Warning</p>
                  <p className="text-sm text-red-700 mt-1">
                    Cancelling this invoice will permanently change its status. If the invoice was sent to the customer, they will receive a cancellation notification email.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="px-6 pb-6 flex justify-end space-x-3">
          <button
            onClick={onClose}
            className="px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 font-medium"
            disabled={isLoading}
          >
            Keep Invoice
          </button>
          <button
            onClick={handleConfirm}
            disabled={isLoading || !selectedReason || (selectedReason === 'other' && !customReason.trim())}
            className="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 font-semibold disabled:bg-red-400 disabled:cursor-not-allowed flex items-center space-x-2"
          >
            {isLoading ? (
              <>
                <svg className="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                <span>Processing...</span>
              </>
            ) : (
              <span>Confirm Cancellation</span>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};
