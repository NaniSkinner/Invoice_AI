'use client';

import React, { useState } from 'react';
import { InvoiceDto } from '@/types/invoice';
import { formatCurrency, formatDate } from '@/lib/format';

interface PaymentLinkModalProps {
  isOpen: boolean;
  onClose: () => void;
  invoice: InvoiceDto;
}

export const PaymentLinkModal: React.FC<PaymentLinkModalProps> = ({
  isOpen,
  onClose,
  invoice,
}) => {
  const [copied, setCopied] = useState(false);

  if (!isOpen || !invoice.paymentLink) return null;

  const paymentUrl = `${window.location.origin}/public/payment/${invoice.paymentLink}`;

  const handleCopyLink = () => {
    navigator.clipboard.writeText(paymentUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleCopyHtml = () => {
    const htmlContent = generateEmailHtml();
    navigator.clipboard.writeText(htmlContent);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const generateEmailHtml = () => {
    return `
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Payment Link - Invoice #${invoice.invoiceNumber}</title>
</head>
<body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f3f4f6;">
  <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff;">
    <!-- Header -->
    <div style="background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); padding: 40px 30px; text-align: center;">
      <h1 style="margin: 0; color: #ffffff; font-size: 32px; font-weight: bold;">Payment Request</h1>
      <p style="margin: 10px 0 0 0; color: #dbeafe; font-size: 16px;">Invoice #${invoice.invoiceNumber}</p>
    </div>

    <!-- Content -->
    <div style="padding: 40px 30px;">
      <!-- Greeting -->
      <p style="margin: 0 0 20px 0; color: #374151; font-size: 16px; line-height: 1.6;">
        Dear ${invoice.customerName},
      </p>
      <p style="margin: 0 0 30px 0; color: #374151; font-size: 16px; line-height: 1.6;">
        You can conveniently pay your invoice online using the secure payment link below.
      </p>

      <!-- Invoice Summary -->
      <div style="background-color: #f9fafb; border-radius: 8px; padding: 24px; margin-bottom: 30px;">
        <h2 style="margin: 0 0 20px 0; color: #111827; font-size: 18px; font-weight: 600;">Invoice Summary</h2>
        <table style="width: 100%; border-collapse: collapse;">
          <tr>
            <td style="padding: 8px 0; color: #6b7280; font-size: 14px; font-weight: 500;">Invoice Number:</td>
            <td style="padding: 8px 0; color: #111827; font-size: 14px; text-align: right; font-weight: 600;">${invoice.invoiceNumber}</td>
          </tr>
          <tr>
            <td style="padding: 8px 0; color: #6b7280; font-size: 14px; font-weight: 500;">Issue Date:</td>
            <td style="padding: 8px 0; color: #111827; font-size: 14px; text-align: right;">${formatDate(invoice.issueDate)}</td>
          </tr>
          <tr>
            <td style="padding: 8px 0; color: #6b7280; font-size: 14px; font-weight: 500;">Due Date:</td>
            <td style="padding: 8px 0; color: #dc2626; font-size: 14px; text-align: right; font-weight: 600;">${formatDate(invoice.dueDate)}</td>
          </tr>
          <tr style="border-top: 2px solid #e5e7eb;">
            <td style="padding: 12px 0 0 0; color: #111827; font-size: 16px; font-weight: 700;">Amount Due:</td>
            <td style="padding: 12px 0 0 0; color: #059669; font-size: 20px; text-align: right; font-weight: 700;">${formatCurrency(invoice.balanceRemaining)}</td>
          </tr>
        </table>
      </div>

      <!-- Payment Button -->
      <div style="text-align: center; margin-bottom: 30px;">
        <a href="${paymentUrl}"
           style="display: inline-block; background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); color: #ffffff; text-decoration: none; padding: 16px 48px; border-radius: 8px; font-size: 18px; font-weight: 600; box-shadow: 0 4px 6px rgba(37, 99, 235, 0.3);">
          Pay Invoice Now
        </a>
      </div>

      <!-- Payment Link -->
      <div style="background-color: #eff6ff; border: 2px solid #3b82f6; border-radius: 8px; padding: 20px; margin-bottom: 30px;">
        <h3 style="margin: 0 0 12px 0; color: #1e40af; font-size: 16px; font-weight: 600;">Secure Payment Link</h3>
        <p style="margin: 0 0 10px 0; color: #1f2937; font-size: 14px;">
          If the button above doesn't work, copy and paste this link into your browser:
        </p>
        <p style="margin: 0; background-color: #ffffff; padding: 12px; border-radius: 4px; font-family: 'Courier New', monospace; font-size: 12px; color: #2563eb; word-break: break-all;">
          ${paymentUrl}
        </p>
      </div>

      <!-- Additional Info -->
      <div style="background-color: #fef3c7; border-left: 4px solid #f59e0b; padding: 16px; margin-bottom: 30px;">
        <p style="margin: 0; color: #92400e; font-size: 14px; line-height: 1.6;">
          <strong>Important:</strong> This payment link is unique to your invoice and can be used to make secure online payments. Keep this link confidential.
        </p>
      </div>

      <!-- Footer -->
      <div style="border-top: 2px solid #e5e7eb; padding-top: 24px;">
        <p style="margin: 0 0 12px 0; color: #6b7280; font-size: 14px;">
          If you have any questions about this invoice or need assistance with payment, please contact us at <a href="mailto:billing@invoiceme.com" style="color: #2563eb; text-decoration: none;">billing@invoiceme.com</a>.
        </p>
        <p style="margin: 0; color: #6b7280; font-size: 14px;">
          Thank you for your business!
        </p>
      </div>
    </div>

    <!-- Footer -->
    <div style="background-color: #f9fafb; padding: 20px 30px; text-align: center; border-top: 1px solid #e5e7eb;">
      <p style="margin: 0; color: #9ca3af; font-size: 12px;">
        This is an automated email from InvoiceMe. Please do not reply directly to this email.
      </p>
      <p style="margin: 8px 0 0 0; color: #9ca3af; font-size: 12px;">
        © ${new Date().getFullYear()} InvoiceMe. All rights reserved.
      </p>
    </div>
  </div>
</body>
</html>
    `.trim();
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-hidden flex flex-col">
        {/* Modal Header */}
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900">Payment Link Preview</h2>
          <p className="text-sm text-gray-600 mt-1">Share this payment link with your customer</p>
        </div>

        {/* Email Preview Content */}
        <div className="flex-1 overflow-y-auto p-6">
          {/* Quick Actions */}
          <div className="bg-blue-50 rounded-lg p-4 mb-6">
            <div className="flex items-center justify-between">
              <div className="flex-1">
                <h3 className="text-sm font-semibold text-gray-900 mb-2">Payment Link</h3>
                <p className="text-sm font-mono text-blue-600 break-all">{paymentUrl}</p>
              </div>
              <button
                onClick={handleCopyLink}
                className="ml-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium transition-colors flex items-center space-x-2"
              >
                {copied ? (
                  <>
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                    <span>Copied!</span>
                  </>
                ) : (
                  <>
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                    </svg>
                    <span>Copy Link</span>
                  </>
                )}
              </button>
            </div>
          </div>

          {/* Email HTML Preview */}
          <div className="border-2 border-gray-300 rounded-lg overflow-hidden mb-4">
            <div className="bg-white">
              {/* Email Header */}
              <div className="bg-gradient-to-br from-blue-500 to-blue-600 p-10 text-center">
                <h1 className="text-3xl font-bold text-white">Payment Request</h1>
                <p className="text-blue-100 mt-2">Invoice #{invoice.invoiceNumber}</p>
              </div>

              {/* Email Body */}
              <div className="p-8">
                {/* Greeting */}
                <div className="mb-6">
                  <p className="text-gray-800 mb-4">Dear {invoice.customerName},</p>
                  <p className="text-gray-800">
                    You can conveniently pay your invoice online using the secure payment link below.
                  </p>
                </div>

                {/* Invoice Summary Box */}
                <div className="bg-gray-50 rounded-lg p-6 mb-6">
                  <h2 className="text-lg font-semibold text-gray-900 mb-4">Invoice Summary</h2>
                  <div className="space-y-2">
                    <div className="flex justify-between">
                      <span className="text-gray-600 font-medium">Invoice Number:</span>
                      <span className="text-gray-900 font-semibold">{invoice.invoiceNumber}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600 font-medium">Issue Date:</span>
                      <span className="text-gray-900">{formatDate(invoice.issueDate)}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600 font-medium">Due Date:</span>
                      <span className="text-red-600 font-semibold">{formatDate(invoice.dueDate)}</span>
                    </div>
                    <div className="flex justify-between pt-3 border-t-2 border-gray-200">
                      <span className="text-gray-900 font-bold text-lg">Amount Due:</span>
                      <span className="text-green-600 font-bold text-xl">{formatCurrency(invoice.balanceRemaining)}</span>
                    </div>
                  </div>
                </div>

                {/* Payment Button */}
                <div className="text-center mb-6">
                  <a
                    href={paymentUrl}
                    className="inline-block bg-gradient-to-r from-blue-500 to-blue-600 text-white font-semibold py-4 px-12 rounded-lg hover:from-blue-600 hover:to-blue-700 transition-all shadow-lg"
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    Pay Invoice Now
                  </a>
                </div>

                {/* Payment Link Box */}
                <div className="bg-blue-50 border-2 border-blue-500 rounded-lg p-5 mb-6">
                  <h3 className="text-blue-900 font-semibold mb-2">Secure Payment Link</h3>
                  <p className="text-gray-700 text-sm mb-2">
                    If the button above doesn't work, copy and paste this link into your browser:
                  </p>
                  <p className="bg-white p-3 rounded font-mono text-xs text-blue-600 break-all">
                    {paymentUrl}
                  </p>
                </div>

                {/* Important Notice */}
                <div className="bg-yellow-50 border-l-4 border-yellow-500 p-4 mb-6">
                  <p className="text-yellow-900 text-sm">
                    <strong>Important:</strong> This payment link is unique to your invoice and can be used to make secure online payments. Keep this link confidential.
                  </p>
                </div>

                {/* Footer */}
                <div className="border-t-2 border-gray-200 pt-6">
                  <p className="text-gray-600 text-sm mb-3">
                    If you have any questions about this invoice or need assistance with payment, please contact us at billing@invoiceme.com.
                  </p>
                  <p className="text-gray-600 text-sm">
                    Thank you for your business!
                  </p>
                </div>
              </div>

              {/* Email Footer */}
              <div className="bg-gray-50 p-5 border-t border-gray-200 text-center">
                <p className="text-gray-500 text-xs">
                  This is an automated email from InvoiceMe. Please do not reply directly to this email.
                </p>
                <p className="text-gray-500 text-xs mt-2">
                  © {new Date().getFullYear()} InvoiceMe. All rights reserved.
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Modal Footer */}
        <div className="px-6 py-4 border-t border-gray-200 flex justify-between items-center bg-gray-50">
          <button
            onClick={handleCopyHtml}
            className="px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 font-medium flex items-center space-x-2"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
            </svg>
            <span>Copy HTML Email</span>
          </button>
          <div className="flex space-x-3">
            <button
              onClick={onClose}
              className="px-6 py-2 text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 font-medium"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
