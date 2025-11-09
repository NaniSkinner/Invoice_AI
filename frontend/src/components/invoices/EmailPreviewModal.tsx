'use client';

import React from 'react';
import { InvoiceDto } from '@/types/invoice';
import { formatCurrency, formatDate } from '@/lib/format';

interface EmailPreviewModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirmSend: () => void;
  onEdit: () => void;
  invoice: InvoiceDto;
  isLoading?: boolean;
}

export const EmailPreviewModal: React.FC<EmailPreviewModalProps> = ({
  isOpen,
  onClose,
  onConfirmSend,
  onEdit,
  invoice,
  isLoading = false,
}) => {
  if (!isOpen) return null;

  const companyEmail = 'billing@invoiceme.com';
  const paymentUrl = invoice.paymentLink
    ? `${window.location.origin}/public/payment/${invoice.paymentLink}`
    : null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-hidden flex flex-col">
        {/* Modal Header */}
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900">Email Preview</h2>
          <p className="text-sm text-gray-600 mt-1">Review the email before sending to customer</p>
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
              <span className="text-gray-900">Invoice #{invoice.invoiceNumber} from InvoiceMe</span>
            </div>
          </div>

          {/* Email Body (HTML Preview) */}
          <div className="border-2 border-gray-300 rounded-lg overflow-hidden">
            <div className="bg-white p-8">
              {/* Email Header */}
              <div className="border-b-4 border-blue-600 pb-6 mb-6">
                <h1 className="text-3xl font-bold text-gray-900">InvoiceMe</h1>
                <p className="text-sm text-gray-600 mt-1">Professional Invoicing System</p>
              </div>

              {/* Greeting */}
              <div className="mb-6">
                <p className="text-gray-800">Dear {invoice.customerName},</p>
                <p className="text-gray-800 mt-4">
                  Thank you for your business. Please find your invoice details below.
                </p>
              </div>

              {/* Invoice Details Box */}
              <div className="bg-gray-50 rounded-lg p-6 mb-6">
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
                    <p className="text-lg font-semibold text-red-600">{formatDate(invoice.dueDate)}</p>
                  </div>
                  <div>
                    <p className="text-sm font-semibold text-gray-600">Amount Due</p>
                    <p className="text-lg font-bold text-green-600">{formatCurrency(invoice.balanceRemaining)}</p>
                  </div>
                </div>
              </div>

              {/* Line Items Table */}
              <div className="mb-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-3">Invoice Items</h3>
                <table className="w-full border-collapse">
                  <thead>
                    <tr className="bg-gray-100 border-b-2 border-gray-300">
                      <th className="text-left py-3 px-4 font-semibold text-gray-700">Description</th>
                      <th className="text-center py-3 px-4 font-semibold text-gray-700">Qty</th>
                      <th className="text-right py-3 px-4 font-semibold text-gray-700">Unit Price</th>
                      <th className="text-right py-3 px-4 font-semibold text-gray-700">Total</th>
                    </tr>
                  </thead>
                  <tbody>
                    {invoice.lineItems.map((item, index) => (
                      <tr key={index} className="border-b border-gray-200">
                        <td className="py-3 px-4 text-gray-800">{item.description}</td>
                        <td className="py-3 px-4 text-center text-gray-800">{item.quantity}</td>
                        <td className="py-3 px-4 text-right text-gray-800">{formatCurrency(item.unitPrice)}</td>
                        <td className="py-3 px-4 text-right font-semibold text-gray-900">{formatCurrency(item.lineTotal)}</td>
                      </tr>
                    ))}
                  </tbody>
                  <tfoot>
                    <tr className="border-t-2 border-gray-300">
                      <td colSpan={3} className="py-2 px-4 text-right font-semibold text-gray-700">Subtotal:</td>
                      <td className="py-2 px-4 text-right text-gray-900">{formatCurrency(invoice.subtotal)}</td>
                    </tr>
                    <tr>
                      <td colSpan={3} className="py-2 px-4 text-right font-semibold text-gray-700">Tax:</td>
                      <td className="py-2 px-4 text-right text-gray-900">{formatCurrency(invoice.taxAmount)}</td>
                    </tr>
                    <tr className="bg-blue-50 border-t-2 border-gray-300">
                      <td colSpan={3} className="py-3 px-4 text-right font-bold text-gray-900 text-lg">Total Amount:</td>
                      <td className="py-3 px-4 text-right font-bold text-green-600 text-xl">{formatCurrency(invoice.totalAmount)}</td>
                    </tr>
                  </tfoot>
                </table>
              </div>

              {/* Payment Link */}
              {paymentUrl && (
                <div className="bg-blue-50 border-2 border-blue-200 rounded-lg p-6 mb-6">
                  <h3 className="text-lg font-semibold text-blue-900 mb-2">Pay Online</h3>
                  <p className="text-gray-700 mb-4">Click the button below to pay this invoice securely online:</p>
                  <a
                    href={paymentUrl}
                    className="inline-block bg-blue-600 text-white font-semibold py-3 px-6 rounded-lg hover:bg-blue-700 transition-colors"
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    Pay Invoice Now
                  </a>
                  <p className="text-sm text-gray-600 mt-3">
                    Payment Link: <span className="font-mono text-blue-600">{paymentUrl}</span>
                  </p>
                </div>
              )}

              {/* Terms */}
              {invoice.terms && (
                <div className="mb-6">
                  <h3 className="text-sm font-semibold text-gray-700 mb-2">Payment Terms</h3>
                  <p className="text-gray-700 whitespace-pre-wrap">{invoice.terms}</p>
                </div>
              )}

              {/* Footer */}
              <div className="border-t-2 border-gray-200 pt-6 mt-6">
                <p className="text-gray-600 text-sm">
                  If you have any questions about this invoice, please contact us at {companyEmail}.
                </p>
                <p className="text-gray-600 text-sm mt-2">
                  Thank you for your business!
                </p>
                <p className="text-gray-500 text-xs mt-4">
                  This is an automated email from InvoiceMe. Please do not reply directly to this email.
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Modal Footer */}
        <div className="px-6 py-4 border-t border-gray-200 flex justify-between items-center bg-gray-50">
          <button
            onClick={onEdit}
            className="px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 font-medium"
            disabled={isLoading}
          >
            Edit Invoice
          </button>
          <div className="flex space-x-3">
            <button
              onClick={onClose}
              className="px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 font-medium"
              disabled={isLoading}
            >
              Cancel
            </button>
            <button
              onClick={onConfirmSend}
              disabled={isLoading}
              className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-semibold disabled:bg-blue-400 disabled:cursor-not-allowed flex items-center space-x-2"
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
                <span>Send Invoice</span>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
