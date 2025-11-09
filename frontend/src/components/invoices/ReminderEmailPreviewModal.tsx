'use client';

import React from 'react';
import { InvoiceDto } from '@/types/invoice';
import { ReminderType } from '@/types/reminder';
import { formatCurrency, formatDate } from '@/lib/format';

interface ReminderEmailPreviewModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirmSend: () => void;
  onEdit: () => void;
  invoice: InvoiceDto;
  reminderType: ReminderType;
  isLoading?: boolean;
}

const getReminderTypeLabel = (type: ReminderType): string => {
  const labels: Record<ReminderType, string> = {
    'BEFORE_DUE': 'Upcoming Payment Due',
    'ON_DUE_DATE': 'Payment Due Today',
    'OVERDUE_7_DAYS': 'Payment Overdue - 7 Days',
    'OVERDUE_14_DAYS': 'Payment Overdue - 14 Days',
    'OVERDUE_30_DAYS': 'Final Payment Notice - 30 Days Overdue',
  };
  return labels[type];
};

const getReminderSeverity = (type: ReminderType): 'gentle' | 'firm' | 'final' => {
  if (type === 'BEFORE_DUE' || type === 'ON_DUE_DATE') return 'gentle';
  if (type === 'OVERDUE_7_DAYS' || type === 'OVERDUE_14_DAYS') return 'firm';
  return 'final';
};

export const ReminderEmailPreviewModal: React.FC<ReminderEmailPreviewModalProps> = ({
  isOpen,
  onClose,
  onConfirmSend,
  onEdit,
  invoice,
  reminderType,
  isLoading = false,
}) => {
  if (!isOpen) return null;

  const companyEmail = 'billing@invoiceme.com';
  const severity = getReminderSeverity(reminderType);
  const reminderLabel = getReminderTypeLabel(reminderType);

  // Different color schemes based on severity
  const colorScheme = {
    gentle: {
      border: 'border-blue-600',
      bg: 'bg-blue-50',
      text: 'text-blue-800',
      header: 'text-blue-600',
      button: 'bg-blue-600 hover:bg-blue-700',
    },
    firm: {
      border: 'border-orange-600',
      bg: 'bg-orange-50',
      text: 'text-orange-800',
      header: 'text-orange-600',
      button: 'bg-orange-600 hover:bg-orange-700',
    },
    final: {
      border: 'border-red-600',
      bg: 'bg-red-50',
      text: 'text-red-800',
      header: 'text-red-600',
      button: 'bg-red-600 hover:bg-red-700',
    },
  };

  const colors = colorScheme[severity];

  // Different message content based on severity
  const getMessage = () => {
    if (severity === 'gentle') {
      return (
        <>
          <p className="text-gray-800">Dear {invoice.customerName},</p>
          <p className="text-gray-800 mt-4">
            This is a friendly reminder that invoice #{invoice.invoiceNumber} is{' '}
            {reminderType === 'BEFORE_DUE' ? 'coming due soon' : 'due today'}.
          </p>
          <p className="text-gray-800 mt-4">
            We kindly request that you process the payment at your earliest convenience to avoid any late fees or service interruptions.
          </p>
        </>
      );
    } else if (severity === 'firm') {
      return (
        <>
          <p className="text-gray-800">Dear {invoice.customerName},</p>
          <p className="text-gray-800 mt-4">
            Our records indicate that invoice #{invoice.invoiceNumber} is now overdue. The payment was due on {formatDate(invoice.dueDate)}.
          </p>
          <p className="text-gray-800 mt-4">
            Please remit payment immediately to avoid additional late fees and potential service disruption. If you have already sent payment, please disregard this notice.
          </p>
        </>
      );
    } else {
      return (
        <>
          <p className="text-gray-800">Dear {invoice.customerName},</p>
          <p className="text-gray-800 mt-4 font-semibold">
            This is a FINAL NOTICE regarding invoice #{invoice.invoiceNumber}, which is now 30 days overdue.
          </p>
          <p className="text-gray-800 mt-4">
            Immediate payment is required. Failure to remit payment within 7 business days may result in:
          </p>
          <ul className="list-disc list-inside space-y-1 text-gray-800 mt-2 ml-4">
            <li>Suspension of services</li>
            <li>Referral to a collections agency</li>
            <li>Additional late fees and interest charges</li>
            <li>Legal action to recover the outstanding balance</li>
          </ul>
          <p className="text-gray-800 mt-4">
            If you are experiencing financial difficulties, please contact us immediately to discuss payment arrangements.
          </p>
        </>
      );
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-hidden flex flex-col">
        {/* Modal Header */}
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900">Payment Reminder Email Preview</h2>
          <p className="text-sm text-gray-600 mt-1">Review the reminder email before sending to customer</p>
        </div>

        {/* Email Preview Content */}
        <div className="flex-1 overflow-y-auto p-6">
          {/* Email Headers */}
          <div className="bg-gray-50 rounded-lg p-4 mb-6 space-y-2">
            <div className="flex">
              <span className="font-semibold text-gray-700 w-20">From:</span>
              <span className="text-gray-900">{companyEmail}</span>
            </div>
            <div className="flex">
              <span className="font-semibold text-gray-700 w-20">To:</span>
              <span className="text-gray-900">{invoice.customerEmail}</span>
            </div>
            <div className="flex">
              <span className="font-semibold text-gray-700 w-20">Subject:</span>
              <span className="text-gray-900">{reminderLabel} - Invoice #{invoice.invoiceNumber}</span>
            </div>
          </div>

          {/* Email Body (HTML Preview) */}
          <div className="border-2 border-gray-300 rounded-lg overflow-hidden">
            <div className="bg-white p-8">
              {/* Email Header */}
              <div className={`border-b-4 ${colors.border} pb-6 mb-6`}>
                <h1 className="text-3xl font-bold text-gray-900">InvoiceMe</h1>
                <p className="text-sm text-gray-600 mt-1">Professional Invoicing System</p>
              </div>

              {/* Reminder Notice */}
              <div className={`${colors.bg} border-l-4 ${colors.border} p-6 mb-6`}>
                <h2 className={`text-2xl font-bold ${colors.text} mb-2`}>{reminderLabel}</h2>
                <p className="text-gray-700">
                  {severity === 'gentle' && 'This is a friendly payment reminder.'}
                  {severity === 'firm' && 'This invoice is now past due and requires immediate attention.'}
                  {severity === 'final' && 'URGENT: This is a final notice. Immediate action required.'}
                </p>
              </div>

              {/* Greeting and Message */}
              <div className="mb-6">
                {getMessage()}
              </div>

              {/* Invoice Details Box */}
              <div className="bg-gray-50 rounded-lg p-6 mb-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Invoice Details</h3>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm font-semibold text-gray-600">Invoice Number</p>
                    <p className="text-lg font-bold text-gray-900">{invoice.invoiceNumber}</p>
                  </div>
                  <div>
                    <p className="text-sm font-semibold text-gray-600">Issue Date</p>
                    <p className="text-lg text-gray-900">{formatDate(invoice.issueDate)}</p>
                  </div>
                  <div>
                    <p className="text-sm font-semibold text-gray-600">Due Date</p>
                    <p className={`text-lg font-semibold ${severity === 'gentle' ? 'text-gray-900' : 'text-red-600'}`}>
                      {formatDate(invoice.dueDate)}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm font-semibold text-gray-600">Amount Due</p>
                    <p className="text-lg font-bold text-red-600">{formatCurrency(invoice.balanceRemaining)}</p>
                  </div>
                </div>
              </div>

              {/* Payment Instructions */}
              <div className="mb-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-3">How to Pay</h3>
                <p className="text-gray-700 mb-4">
                  Click the button below to view your invoice and make a secure online payment:
                </p>
                <div className="text-center">
                  <button className={`${colors.button} text-white px-8 py-3 rounded-lg font-semibold text-lg shadow-md transition-colors`}>
                    Pay Invoice Now
                  </button>
                </div>
                <p className="text-gray-600 text-sm text-center mt-4">
                  Or visit: {window.location.origin}/public/payment/{invoice.paymentLink}
                </p>
              </div>

              {/* Contact Information */}
              <div className="mb-6 bg-gray-50 rounded-lg p-4">
                <h3 className="text-sm font-semibold text-gray-700 mb-2">Questions or Concerns?</h3>
                <p className="text-sm text-gray-700">
                  If you have any questions about this invoice or need to discuss payment arrangements,
                  please contact us at {companyEmail} or reply to this email.
                </p>
              </div>

              {/* Footer */}
              <div className="border-t-2 border-gray-200 pt-6 mt-6">
                <p className="text-gray-600 text-sm">
                  Thank you for your prompt attention to this matter.
                </p>
                <p className="text-gray-600 text-sm mt-2">
                  Best regards,<br />
                  InvoiceMe Billing Team
                </p>
                <p className="text-gray-500 text-xs mt-4">
                  This is an automated email from InvoiceMe. Please do not reply directly to this email.
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Modal Footer */}
        <div className="px-6 py-4 border-t border-gray-200 flex justify-end space-x-3 bg-gray-50">
          <button
            onClick={onClose}
            className="px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 font-medium"
            disabled={isLoading}
          >
            Cancel
          </button>
          <button
            onClick={onEdit}
            className="px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 font-medium"
            disabled={isLoading}
          >
            Edit Invoice
          </button>
          <button
            onClick={onConfirmSend}
            disabled={isLoading}
            className={`px-6 py-2 ${colors.button} text-white rounded-lg font-semibold disabled:opacity-50 disabled:cursor-not-allowed flex items-center space-x-2`}
          >
            {isLoading ? (
              <>
                <svg className="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                <span>Sending...</span>
              </>
            ) : (
              <span>Confirm & Send Reminder</span>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};
