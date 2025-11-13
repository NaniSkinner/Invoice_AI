'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import { AppLayout } from '@/components/AppLayout';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge, getInvoiceStatusBadgeVariant } from '@/components/ui/Badge';
import { Loading } from '@/components/ui/Loading';
import { Table } from '@/components/ui/Table';
import { RecordPaymentModal } from '@/components/invoices/RecordPaymentModal';
import { ReminderEmailPreviewModal } from '@/components/invoices/ReminderEmailPreviewModal';
import { MarkAsPaidModal } from '@/components/invoices/MarkAsPaidModal';
import { EmailPreviewModal } from '@/components/invoices/EmailPreviewModal';
import { CancelInvoiceModal } from '@/components/invoices/CancelInvoiceModal';
import { CancellationEmailPreviewModal } from '@/components/invoices/CancellationEmailPreviewModal';
import { PaymentLinkModal } from '@/components/invoices/PaymentLinkModal';
import { SuccessModal } from '@/components/ui/SuccessModal';
import {
  getInvoiceById,
  sendInvoice,
  markInvoiceAsPaid,
  cancelInvoice,
} from '@/lib/api/invoices';
import { recordPayment, getPaymentsByInvoice } from '@/lib/api/payments';
import { sendReminder, getReminderHistory } from '@/lib/api/reminders';
import { InvoiceDto } from '@/types/invoice';
import { PaymentDto } from '@/types/payment';
import { ReminderHistoryDto, ReminderType } from '@/types/reminder';
import { formatDate, formatCurrency, formatInvoiceStatus, formatPaymentMethod, formatReminderType, formatDateTime } from '@/lib/format';

export default function InvoiceDetailPage() {
  const router = useRouter();
  const params = useParams();
  const id = params.id as string;

  const [invoice, setInvoice] = useState<InvoiceDto | null>(null);
  const [payments, setPayments] = useState<PaymentDto[]>([]);
  const [reminders, setReminders] = useState<ReminderHistoryDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isPaymentModalOpen, setIsPaymentModalOpen] = useState(false);
  const [isReminderEmailPreviewOpen, setIsReminderEmailPreviewOpen] = useState(false);
  const [selectedReminderType, setSelectedReminderType] = useState<ReminderType>('ON_DUE_DATE');
  const [isEmailPreviewOpen, setIsEmailPreviewOpen] = useState(false);
  const [isCancelModalOpen, setIsCancelModalOpen] = useState(false);
  const [isCancellationEmailPreviewOpen, setIsCancellationEmailPreviewOpen] = useState(false);
  const [cancellationReason, setCancellationReason] = useState('');
  const [isPaymentLinkModalOpen, setIsPaymentLinkModalOpen] = useState(false);
  const [isMarkAsPaidModalOpen, setIsMarkAsPaidModalOpen] = useState(false);
  const [isSuccessModalOpen, setIsSuccessModalOpen] = useState(false);
  const [successMessage, setSuccessMessage] = useState({ title: '', message: '', details: '' });
  const [isProcessing, setIsProcessing] = useState(false);

  const fetchData = async () => {
    try {
      setIsLoading(true);
      const [invoiceData, paymentsData, remindersData] = await Promise.all([
        getInvoiceById(id),
        getPaymentsByInvoice(id),
        getReminderHistory(id).catch(() => []), // Reminders might not exist
      ]);
      setInvoice(invoiceData);
      setPayments(paymentsData);
      setReminders(remindersData);
    } catch (error) {
      console.error('Error fetching invoice:', error);
      alert('Failed to load invoice details.');
      router.push('/invoices');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [id]);

  const handleShowEmailPreview = () => {
    setIsEmailPreviewOpen(true);
  };

  const handleConfirmSendInvoice = async () => {
    try {
      setIsProcessing(true);
      const updated = await sendInvoice(id);
      setInvoice(updated);
      setIsEmailPreviewOpen(false);
      setSuccessMessage({
        title: 'Invoice Sent Successfully!',
        message: `Invoice #${updated.invoiceNumber} has been sent to ${updated.customerName}.`,
        details: `The invoice email has been delivered to ${updated.customerEmail}. The customer can now view and pay the invoice online.`
      });
      setIsSuccessModalOpen(true);
    } catch (error) {
      console.error('Error sending invoice:', error);
      alert('Failed to send invoice.');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleEditFromPreview = () => {
    setIsEmailPreviewOpen(false);
    router.push(`/invoices/${id}/edit`);
  };

  const handleRecordPayment = async (data: any) => {
    try {
      setIsProcessing(true);
      await recordPayment({
        invoiceId: id,
        ...data,
      });
      await fetchData();
      setIsPaymentModalOpen(false);
      alert('Payment recorded successfully!');
    } catch (error) {
      console.error('Error recording payment:', error);
      alert('Failed to record payment.');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleShowReminderPreview = () => {
    // Default to ON_DUE_DATE for general reminders
    setSelectedReminderType('ON_DUE_DATE');
    setIsReminderEmailPreviewOpen(true);
  };

  const handleConfirmSendReminder = async () => {
    try {
      setIsProcessing(true);
      await sendReminder({ invoiceId: id, reminderType: selectedReminderType });
      await fetchData();
      setIsReminderEmailPreviewOpen(false);
      setSuccessMessage({
        title: 'Reminder Sent Successfully!',
        message: `Payment reminder has been sent to ${invoice?.customerName}.`,
        details: `The reminder email has been delivered to ${invoice?.customerEmail}. The customer has been notified about the payment status.`
      });
      setIsSuccessModalOpen(true);
    } catch (error) {
      console.error('Error sending reminder:', error);
      alert('Failed to send reminder.');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleEditFromReminderPreview = () => {
    setIsReminderEmailPreviewOpen(false);
    router.push(`/invoices/${id}/edit`);
  };

  const handleShowMarkAsPaidModal = () => {
    setIsMarkAsPaidModalOpen(true);
  };

  const handleConfirmMarkAsPaid = async () => {
    try {
      setIsProcessing(true);
      const updated = await markInvoiceAsPaid(id);
      setInvoice(updated);
      setIsMarkAsPaidModalOpen(false);
      setSuccessMessage({
        title: 'Invoice Marked as Paid!',
        message: `Invoice #${updated.invoiceNumber} has been successfully marked as paid.`,
        details: `The invoice status has been updated to PAID and the balance is now $0.00. The payment has been recorded in the system.`
      });
      setIsSuccessModalOpen(true);
    } catch (error) {
      console.error('Error marking as paid:', error);
      alert('Failed to mark invoice as paid.');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleShowCancelModal = () => {
    setIsCancelModalOpen(true);
  };

  const handleCancelReasonSubmit = (reason: string, customReason?: string): void => {
    const reasonText = customReason ?? reason;
    setCancellationReason(reasonText);
    setIsCancelModalOpen(false);

    // If invoice was SENT, show email preview
    if (invoice?.status === 'SENT') {
      setIsCancellationEmailPreviewOpen(true);
    } else {
      // If DRAFT, just cancel directly without email
      handleConfirmCancellation();
    }
  };

  const handleConfirmCancellation = async () => {
    try {
      setIsProcessing(true);
      const updated = await cancelInvoice(id, cancellationReason);
      setInvoice(updated);
      setIsCancellationEmailPreviewOpen(false);

      // Show success modal with different messages based on whether email was sent
      const wasEmailSent = updated.status === 'CANCELLED' && invoice?.status === 'SENT';
      setSuccessMessage({
        title: 'Invoice Cancelled',
        message: `Invoice #${updated.invoiceNumber} has been cancelled successfully.`,
        details: wasEmailSent
          ? `A cancellation notification has been sent to ${updated.customerEmail}. The customer has been informed that this invoice is no longer payable.`
          : 'The invoice status has been updated to CANCELLED.'
      });
      setIsSuccessModalOpen(true);
    } catch (error) {
      console.error('Error cancelling invoice:', error);
      alert('Failed to cancel invoice.');
    } finally {
      setIsProcessing(false);
    }
  };

  if (isLoading) {
    return (
      <AppLayout>
        <Loading text="Loading invoice..." />
      </AppLayout>
    );
  }

  if (!invoice) {
    return null;
  }

  const canEdit = invoice.status === 'DRAFT';
  const canSend = invoice.status === 'DRAFT';
  const canRecordPayment = invoice.status === 'SENT' && invoice.balanceRemaining > 0;
  const canSendReminder = invoice.status === 'SENT';
  const canMarkPaid = invoice.status === 'SENT' && invoice.balanceRemaining > 0;
  const canCancel = invoice.status !== 'PAID' && invoice.status !== 'CANCELLED';

  return (
    <AppLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Invoice {invoice.invoiceNumber}</h1>
            <p className="text-gray-600 mt-1">{invoice.customerName}</p>
          </div>
          <div className="flex items-center space-x-3">
            <Badge variant={getInvoiceStatusBadgeVariant(invoice.status)} className="text-base px-4 py-2">
              {formatInvoiceStatus(invoice.status)}
            </Badge>
            {canEdit && (
              <Link href={`/invoices/${id}/edit`}>
                <Button variant="secondary">Edit</Button>
              </Link>
            )}
          </div>
        </div>

        {/* Actions */}
        <Card title="Actions">
          <div className="flex flex-wrap gap-3">
            {canSend && (
              <Button onClick={handleShowEmailPreview}>
                Send Invoice
              </Button>
            )}
            {canRecordPayment && (
              <Button variant="success" onClick={() => setIsPaymentModalOpen(true)}>
                Record Payment
              </Button>
            )}
            {canSendReminder && (
              <Button variant="secondary" onClick={handleShowReminderPreview}>
                Send Reminder
              </Button>
            )}
            {canMarkPaid && (
              <Button variant="success" onClick={handleShowMarkAsPaidModal}>
                Mark as Paid
              </Button>
            )}
            {canCancel && (
              <Button variant="danger" onClick={handleShowCancelModal}>
                Cancel Invoice
              </Button>
            )}
            {invoice.paymentLink && (
              <Button
                variant="outline"
                onClick={() => setIsPaymentLinkModalOpen(true)}
              >
                Copy Payment Link
              </Button>
            )}
          </div>
        </Card>

        {/* Invoice Details */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Card title="Invoice Information">
            <div className="space-y-3">
              <div>
                <span className="text-sm font-medium text-gray-600">Issue Date:</span>
                <p className="text-gray-900">{formatDate(invoice.issueDate)}</p>
              </div>
              <div>
                <span className="text-sm font-medium text-gray-600">Due Date:</span>
                <p className="text-gray-900">{formatDate(invoice.dueDate)}</p>
              </div>
              <div>
                <span className="text-sm font-medium text-gray-600">Partial Payments:</span>
                <p className="text-gray-900">{invoice.allowsPartialPayment ? 'Allowed' : 'Not Allowed'}</p>
              </div>
            </div>
          </Card>

          <Card title="Payment Summary">
            <div className="space-y-3">
              <div>
                <span className="text-sm font-medium text-gray-600">Subtotal:</span>
                <p className="text-gray-900">{formatCurrency(invoice.subtotal)}</p>
              </div>
              <div>
                <span className="text-sm font-medium text-gray-600">Tax:</span>
                <p className="text-gray-900">{formatCurrency(invoice.taxAmount)}</p>
              </div>
              <div className="border-t pt-2">
                <span className="text-sm font-medium text-gray-600">Total:</span>
                <p className="text-xl font-bold text-gray-900">{formatCurrency(invoice.totalAmount)}</p>
              </div>
            </div>
          </Card>

          <Card title="Balance">
            <div className="space-y-3">
              <div>
                <span className="text-sm font-medium text-gray-600">Amount Paid:</span>
                <p className="text-green-600 font-semibold">{formatCurrency(invoice.amountPaid)}</p>
              </div>
              <div className="border-t pt-2">
                <span className="text-sm font-medium text-gray-600">Balance Remaining:</span>
                <p className="text-xl font-bold text-red-600">{formatCurrency(invoice.balanceRemaining)}</p>
              </div>
            </div>
          </Card>
        </div>

        {/* Line Items */}
        <Card title="Line Items">
          <Table
            data={invoice.lineItems}
            columns={[
              { header: 'Description', accessor: 'description' },
              { header: 'Quantity', accessor: (row) => row.quantity.toString() },
              { header: 'Unit Price', accessor: (row) => formatCurrency(row.unitPrice) },
              { header: 'Total', accessor: (row) => formatCurrency(row.lineTotal) },
            ]}
            emptyMessage="No line items"
          />
        </Card>

        {/* Additional Information */}
        {(invoice.notes || invoice.terms) && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {invoice.notes && (
              <Card title="Notes">
                <p className="text-gray-900 whitespace-pre-wrap">{invoice.notes}</p>
              </Card>
            )}
            {invoice.terms && (
              <Card title="Terms">
                <p className="text-gray-900 whitespace-pre-wrap">{invoice.terms}</p>
              </Card>
            )}
          </div>
        )}

        {/* Payment History */}
        <Card title={`Payment History (${payments.length})`}>
          <Table
            data={payments}
            columns={[
              { header: 'Date', accessor: (row) => formatDate(row.paymentDate) },
              { header: 'Amount', accessor: (row) => formatCurrency(row.paymentAmount) },
              { header: 'Method', accessor: (row) => formatPaymentMethod(row.paymentMethod) },
              { header: 'Reference', accessor: (row) => row.transactionReference || '-' },
              { header: 'Notes', accessor: (row) => row.notes || '-' },
            ]}
            emptyMessage="No payments recorded yet"
          />
        </Card>

        {/* Reminder History */}
        {reminders.length > 0 && (
          <Card title={`Reminder History (${reminders.length})`}>
            <Table
              data={reminders}
              columns={[
                { header: 'Date', accessor: (row) => formatDateTime(row.sentDate) },
                { header: 'Type', accessor: (row) => formatReminderType(row.reminderType) },
                { header: 'Recipient', accessor: 'recipientEmail' },
                { header: 'Subject', accessor: 'subject' },
              ]}
              emptyMessage="No reminders sent"
            />
          </Card>
        )}
      </div>

      {/* Modals */}
      {invoice && (
        <EmailPreviewModal
          isOpen={isEmailPreviewOpen}
          onClose={() => setIsEmailPreviewOpen(false)}
          onConfirmSend={handleConfirmSendInvoice}
          onEdit={handleEditFromPreview}
          invoice={invoice}
          isLoading={isProcessing}
        />
      )}

      <CancelInvoiceModal
        isOpen={isCancelModalOpen}
        onClose={() => setIsCancelModalOpen(false)}
        onConfirm={handleCancelReasonSubmit}
        invoiceNumber={invoice?.invoiceNumber || ''}
        isLoading={isProcessing}
      />

      {invoice && (
        <CancellationEmailPreviewModal
          isOpen={isCancellationEmailPreviewOpen}
          onClose={() => setIsCancellationEmailPreviewOpen(false)}
          onConfirmSend={handleConfirmCancellation}
          invoice={invoice}
          cancellationReason={cancellationReason}
          isLoading={isProcessing}
        />
      )}

      <SuccessModal
        isOpen={isSuccessModalOpen}
        onClose={() => setIsSuccessModalOpen(false)}
        title={successMessage.title}
        message={successMessage.message}
        details={successMessage.details}
      />

      {invoice && (
        <RecordPaymentModal
          isOpen={isPaymentModalOpen}
          onClose={() => setIsPaymentModalOpen(false)}
          onSubmit={handleRecordPayment}
          maxAmount={invoice.balanceRemaining}
          isLoading={isProcessing}
        />
      )}

      {invoice && (
        <ReminderEmailPreviewModal
          isOpen={isReminderEmailPreviewOpen}
          onClose={() => setIsReminderEmailPreviewOpen(false)}
          onConfirmSend={handleConfirmSendReminder}
          onEdit={handleEditFromReminderPreview}
          invoice={invoice}
          reminderType={selectedReminderType}
          isLoading={isProcessing}
        />
      )}

      {invoice && (
        <PaymentLinkModal
          isOpen={isPaymentLinkModalOpen}
          onClose={() => setIsPaymentLinkModalOpen(false)}
          invoice={invoice}
        />
      )}

      {invoice && (
        <MarkAsPaidModal
          isOpen={isMarkAsPaidModalOpen}
          onClose={() => setIsMarkAsPaidModalOpen(false)}
          onConfirm={handleConfirmMarkAsPaid}
          invoice={invoice}
          isLoading={isProcessing}
        />
      )}
    </AppLayout>
  );
}
