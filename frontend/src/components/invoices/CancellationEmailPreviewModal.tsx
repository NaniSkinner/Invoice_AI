'use client';

import React from 'react';
import { InvoiceDto } from '@/types/invoice';
import { formatCurrency, formatDate } from '@/lib/format';

interface CancellationEmailPreviewModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirmSend: () => void;
  invoice: InvoiceDto;
  cancellationReason: string;
  isLoading?: boolean;
}

export const CancellationEmailPreviewModal: React.FC<CancellationEmailPreviewModalProps> = ({
  isOpen,
  onClose,
  onConfirmSend,
  invoice,
  cancellationReason,
  isLoading = false,
}) => {
  if (!isOpen) return null;

  const companyEmail = 'billing@invoiceme.com';

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-hidden flex flex-col">
        {/* Modal Header */}
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900">Cancellation Email Preview</h2>
          <p className="text-sm text-gray-600 mt-1">Review the cancellation notice before sending to customer</p>
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
              <span className="text-gray-900">Invoice #{invoice.invoiceNumber} Cancelled</span>
            </div>
          </div>

          {/* Email Body (HTML Preview) */}
          <div className="border-2 border-gray-300 rounded-lg overflow-hidden">
            <div className="bg-white p-8">
              {/* Email Header */}
              <div className="border-b-4 border-red-600 pb-6 mb-6">
                <h1 className="text-3xl font-bold text-gray-900">InvoiceMe</h1>
                <p className="text-sm text-gray-600 mt-1">Professional Invoicing System</p>
              </div>

              {/* Cancellation Notice */}
              <div className="bg-red-50 border-l-4 border-red-600 p-6 mb-6">
                <h2 className="text-2xl font-bold text-red-800 mb-2">Invoice Cancelled</h2>
                <p className="text-gray-700">
                  This invoice has been cancelled and is no longer payable.
                </p>
              </div>

              {/* Greeting */}
              <div className="mb-6">
                <p className="text-gray-800">Dear {invoice.customerName},</p>
                <p className="text-gray-800 mt-4">
                  We are writing to inform you that Invoice #{invoice.invoiceNumber} has been cancelled.
                </p>
              </div>

              {/* Invoice Details Box */}
              <div className="bg-gray-50 rounded-lg p-6 mb-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Cancelled Invoice Details</h3>
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
                    <p className="text-sm font-semibold text-gray-600">Original Amount</p>
                    <p className="text-lg font-semibold text-gray-900">{formatCurrency(invoice.totalAmount)}</p>
                  </div>
                  <div>
                    <p className="text-sm font-semibold text-gray-600">Status</p>
                    <p className="text-lg font-bold text-red-600">CANCELLED</p>
                  </div>
                </div>
              </div>

              {/* Cancellation Reason */}
              <div className="mb-6 bg-gray-50 rounded-lg p-4">
                <h3 className="text-sm font-semibold text-gray-700 mb-2">Cancellation Reason</h3>
                <p className="text-gray-800">{cancellationReason}</p>
              </div>

              {/* Next Steps */}
              <div className="mb-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-3">What This Means</h3>
                <ul className="list-disc list-inside space-y-2 text-gray-700">
                  <li>You are no longer required to pay this invoice</li>
                  <li>Any payment links associated with this invoice have been deactivated</li>
                  <li>This cancellation is final and cannot be reversed</li>
                </ul>
              </div>

              {/* Footer */}
              <div className="border-t-2 border-gray-200 pt-6 mt-6">
                <p className="text-gray-600 text-sm">
                  If you have any questions about this cancellation, please contact us at {companyEmail}.
                </p>
                <p className="text-gray-600 text-sm mt-2">
                  Thank you for your understanding.
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
            Go Back
          </button>
          <button
            onClick={onConfirmSend}
            disabled={isLoading}
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
              <span>Confirm & Send Cancellation</span>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};
