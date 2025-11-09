'use client';

import React from 'react';
import { InvoiceDto } from '@/types/invoice';
import { formatCurrency, formatDate } from '@/lib/format';

interface MarkAsPaidModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  invoice: InvoiceDto;
  isLoading?: boolean;
}

export const MarkAsPaidModal: React.FC<MarkAsPaidModalProps> = ({
  isOpen,
  onClose,
  onConfirm,
  invoice,
  isLoading = false,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-hidden flex flex-col">
        {/* Modal Header */}
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900">Mark Invoice as Paid</h2>
          <p className="text-sm text-gray-600 mt-1">Confirm marking this invoice as fully paid</p>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6">
          {/* Warning Notice */}
          <div className="bg-amber-50 border-l-4 border-amber-500 p-4 mb-6">
            <div className="flex">
              <div className="flex-shrink-0">
                <svg className="h-5 w-5 text-amber-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                </svg>
              </div>
              <div className="ml-3">
                <p className="text-sm text-amber-800">
                  <strong>Important:</strong> This action will mark the invoice as fully paid and update the payment status. This is typically used when receiving full payment outside the system.
                </p>
              </div>
            </div>
          </div>

          {/* Invoice Summary */}
          <div className="border-2 border-gray-300 rounded-lg overflow-hidden mb-6">
            <div className="bg-white p-6">
              {/* Header */}
              <div className="border-b-4 border-green-600 pb-6 mb-6">
                <h1 className="text-3xl font-bold text-gray-900">Payment Confirmation</h1>
                <p className="text-sm text-gray-600 mt-1">Invoice #{invoice.invoiceNumber}</p>
              </div>

              {/* Customer Information */}
              <div className="mb-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-3">Customer Details</h3>
                <div className="bg-gray-50 rounded-lg p-4">
                  <p className="text-gray-900 font-semibold">{invoice.customerName}</p>
                  <p className="text-gray-600 text-sm mt-1">{invoice.customerEmail}</p>
                </div>
              </div>

              {/* Payment Summary Box */}
              <div className="bg-green-50 border-2 border-green-500 rounded-lg p-6 mb-6">
                <h3 className="text-lg font-semibold text-green-900 mb-4">Payment Summary</h3>
                <div className="space-y-3">
                  <div className="flex justify-between">
                    <span className="text-gray-700 font-medium">Invoice Date:</span>
                    <span className="text-gray-900">{formatDate(invoice.issueDate)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-700 font-medium">Due Date:</span>
                    <span className="text-gray-900">{formatDate(invoice.dueDate)}</span>
                  </div>
                  <div className="flex justify-between pt-2 border-t-2 border-green-200">
                    <span className="text-gray-700 font-medium">Total Amount:</span>
                    <span className="text-gray-900 font-semibold">{formatCurrency(invoice.totalAmount)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-700 font-medium">Amount Already Paid:</span>
                    <span className="text-green-600 font-semibold">{formatCurrency(invoice.amountPaid)}</span>
                  </div>
                  <div className="flex justify-between pt-3 border-t-2 border-green-300 bg-green-100 -mx-6 px-6 py-3 -mb-6">
                    <span className="text-green-900 font-bold text-lg">Outstanding Balance:</span>
                    <span className="text-green-600 font-bold text-xl">{formatCurrency(invoice.balanceRemaining)}</span>
                  </div>
                </div>
              </div>

              {/* Line Items Preview */}
              <div className="mb-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-3">Invoice Items</h3>
                <div className="bg-gray-50 rounded-lg overflow-hidden">
                  <table className="w-full">
                    <thead className="bg-gray-200">
                      <tr>
                        <th className="text-left py-2 px-4 font-semibold text-gray-700 text-sm">Description</th>
                        <th className="text-center py-2 px-4 font-semibold text-gray-700 text-sm">Qty</th>
                        <th className="text-right py-2 px-4 font-semibold text-gray-700 text-sm">Price</th>
                        <th className="text-right py-2 px-4 font-semibold text-gray-700 text-sm">Total</th>
                      </tr>
                    </thead>
                    <tbody>
                      {invoice.lineItems.slice(0, 3).map((item, index) => (
                        <tr key={index} className="border-b border-gray-200 last:border-0">
                          <td className="py-2 px-4 text-gray-800 text-sm">{item.description}</td>
                          <td className="py-2 px-4 text-center text-gray-800 text-sm">{item.quantity}</td>
                          <td className="py-2 px-4 text-right text-gray-800 text-sm">{formatCurrency(item.unitPrice)}</td>
                          <td className="py-2 px-4 text-right font-semibold text-gray-900 text-sm">{formatCurrency(item.lineTotal)}</td>
                        </tr>
                      ))}
                      {invoice.lineItems.length > 3 && (
                        <tr>
                          <td colSpan={4} className="py-2 px-4 text-center text-gray-500 text-sm italic">
                            + {invoice.lineItems.length - 3} more item(s)
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </div>

              {/* Action Summary */}
              <div className="bg-blue-50 border-2 border-blue-500 rounded-lg p-5 mb-6">
                <h3 className="text-blue-900 font-semibold mb-2 flex items-center">
                  <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  What will happen?
                </h3>
                <ul className="text-blue-800 text-sm space-y-2 ml-7">
                  <li>• Invoice status will be updated to <strong>PAID</strong></li>
                  <li>• Balance remaining will be set to <strong>$0.00</strong></li>
                  <li>• Amount paid will be set to <strong>{formatCurrency(invoice.totalAmount)}</strong></li>
                  <li>• This action cannot be easily undone</li>
                </ul>
              </div>

              {/* Note */}
              <div className="bg-gray-100 rounded-lg p-4 border-l-4 border-gray-400">
                <p className="text-gray-700 text-sm">
                  <strong>Note:</strong> If you received a partial payment or need to track payment details (payment method, reference number, etc.), please use the <strong>"Record Payment"</strong> feature instead. This allows you to maintain a detailed payment history.
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Modal Footer */}
        <div className="px-6 py-4 border-t border-gray-200 flex justify-between items-center bg-gray-50">
          <button
            onClick={onClose}
            className="px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 font-medium"
            disabled={isLoading}
          >
            Cancel
          </button>
          <div className="flex space-x-3">
            <button
              onClick={onConfirm}
              disabled={isLoading}
              className="px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 font-semibold disabled:bg-green-400 disabled:cursor-not-allowed flex items-center space-x-2"
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
                <>
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                  <span>Mark as Paid</span>
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
