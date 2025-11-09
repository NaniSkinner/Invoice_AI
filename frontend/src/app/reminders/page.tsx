'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { AppLayout } from '@/components/AppLayout';
import { Card } from '@/components/ui/Card';
import { Table } from '@/components/ui/Table';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Loading } from '@/components/ui/Loading';
import { ReminderEmailPreviewModal } from '@/components/invoices/ReminderEmailPreviewModal';
import { SuccessModal } from '@/components/ui/SuccessModal';
import { getOverdueInvoices, sendReminder } from '@/lib/api/reminders';
import { getInvoiceById } from '@/lib/api/invoices';
import { OverdueInvoiceDto, ReminderType } from '@/types/reminder';
import { InvoiceDto } from '@/types/invoice';
import { formatDate, formatCurrency, calculateDaysUntil } from '@/lib/format';

export default function RemindersPage() {
  const router = useRouter();
  const [overdueInvoices, setOverdueInvoices] = useState<OverdueInvoiceDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedInvoiceId, setSelectedInvoiceId] = useState<string | null>(null);
  const [selectedInvoice, setSelectedInvoice] = useState<InvoiceDto | null>(null);
  const [isReminderEmailPreviewOpen, setIsReminderEmailPreviewOpen] = useState(false);
  const [selectedReminderType, setSelectedReminderType] = useState<ReminderType>('ON_DUE_DATE');
  const [isSuccessModalOpen, setIsSuccessModalOpen] = useState(false);
  const [successMessage, setSuccessMessage] = useState({ title: '', message: '', details: '' });
  const [isSending, setIsSending] = useState(false);

  const fetchOverdueInvoices = async () => {
    try {
      setIsLoading(true);
      const data = await getOverdueInvoices();
      setOverdueInvoices(data);
    } catch (error) {
      console.error('Error fetching overdue invoices:', error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchOverdueInvoices();
  }, []);

  // Determine appropriate reminder type based on days overdue
  const getReminderTypeForDaysOverdue = (daysOverdue: number): ReminderType => {
    if (daysOverdue >= 30) return 'OVERDUE_30_DAYS';
    if (daysOverdue >= 14) return 'OVERDUE_14_DAYS';
    if (daysOverdue >= 7) return 'OVERDUE_7_DAYS';
    return 'ON_DUE_DATE';
  };

  const handleSendReminder = async (invoiceId: string, daysOverdue: number) => {
    try {
      setSelectedInvoiceId(invoiceId);
      const invoiceData = await getInvoiceById(invoiceId);
      setSelectedInvoice(invoiceData);

      // Automatically determine reminder type based on days overdue
      const reminderType = getReminderTypeForDaysOverdue(daysOverdue);
      setSelectedReminderType(reminderType);

      // Go directly to email preview
      setIsReminderEmailPreviewOpen(true);
    } catch (error) {
      console.error('Error fetching invoice:', error);
      alert('Failed to load invoice details.');
    }
  };

  const handleConfirmSendReminder = async () => {
    if (!selectedInvoiceId) return;

    try {
      setIsSending(true);
      await sendReminder({ invoiceId: selectedInvoiceId, reminderType: selectedReminderType });
      await fetchOverdueInvoices();
      setIsReminderEmailPreviewOpen(false);
      setSuccessMessage({
        title: 'Reminder Sent Successfully!',
        message: `Payment reminder has been sent to ${selectedInvoice?.customerName}.`,
        details: `The reminder email has been delivered to ${selectedInvoice?.customerEmail}. The customer has been notified about the payment status.`
      });
      setIsSuccessModalOpen(true);
    } catch (error) {
      console.error('Error sending reminder:', error);
      alert('Failed to send reminder.');
    } finally {
      setIsSending(false);
    }
  };

  const handleEditFromReminderPreview = () => {
    if (selectedInvoiceId) {
      router.push(`/invoices/${selectedInvoiceId}/edit`);
    }
  };

  if (isLoading) {
    return (
      <AppLayout>
        <Loading text="Loading overdue invoices..." />
      </AppLayout>
    );
  }

  const getSeverityBadge = (daysOverdue: number) => {
    if (daysOverdue >= 30) return <Badge variant="danger">Critical</Badge>;
    if (daysOverdue >= 14) return <Badge variant="warning">High</Badge>;
    if (daysOverdue >= 7) return <Badge variant="warning">Medium</Badge>;
    return <Badge variant="info">Low</Badge>;
  };

  return (
    <AppLayout>
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Reminders</h1>
            <p className="text-gray-600 mt-1">Manage overdue invoices and send payment reminders</p>
          </div>
        </div>

        {overdueInvoices.length === 0 ? (
          <Card>
            <div className="text-center py-12">
              <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-100 mb-4">
                <svg
                  className="h-6 w-6 text-green-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M5 13l4 4L19 7"
                  />
                </svg>
              </div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2">No Overdue Invoices</h3>
              <p className="text-gray-600">All invoices are up to date. Great job!</p>
            </div>
          </Card>
        ) : (
          <Card
            title={`Overdue Invoices (${overdueInvoices.length})`}
            className="border-l-4 border-red-500"
          >
            <Table
              data={overdueInvoices}
              columns={[
                { header: 'Invoice #', accessor: 'invoiceNumber' },
                { header: 'Customer', accessor: 'customerName' },
                { header: 'Due Date', accessor: (row) => formatDate(row.dueDate) },
                {
                  header: 'Days Overdue',
                  accessor: (row) => (
                    <span className="font-semibold text-red-600">{row.daysOverdue} days</span>
                  ),
                },
                { header: 'Total', accessor: (row) => formatCurrency(row.totalAmount) },
                { header: 'Balance Due', accessor: (row) => formatCurrency(row.balanceRemaining) },
                {
                  header: 'Severity',
                  accessor: (row) => getSeverityBadge(row.daysOverdue),
                },
                {
                  header: 'Actions',
                  accessor: (row) => (
                    <div className="flex space-x-2">
                      <Button
                        size="sm"
                        variant="secondary"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleSendReminder(row.invoiceId, row.daysOverdue);
                        }}
                      >
                        Send Reminder
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={(e) => {
                          e.stopPropagation();
                          router.push(`/invoices/${row.invoiceId}`);
                        }}
                      >
                        View
                      </Button>
                    </div>
                  ),
                },
              ]}
              emptyMessage="No overdue invoices"
            />
          </Card>
        )}

        {/* Statistics */}
        {overdueInvoices.length > 0 && (
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
            <Card>
              <div className="flex flex-col">
                <span className="text-sm font-medium text-gray-600">Total Overdue</span>
                <span className="text-3xl font-bold text-red-600 mt-2">{overdueInvoices.length}</span>
              </div>
            </Card>

            <Card>
              <div className="flex flex-col">
                <span className="text-sm font-medium text-gray-600">Total Amount</span>
                <span className="text-3xl font-bold text-red-600 mt-2">
                  {formatCurrency(
                    overdueInvoices.reduce((sum, inv) => sum + inv.balanceRemaining, 0)
                  )}
                </span>
              </div>
            </Card>

            <Card>
              <div className="flex flex-col">
                <span className="text-sm font-medium text-gray-600">7+ Days Overdue</span>
                <span className="text-3xl font-bold text-orange-600 mt-2">
                  {overdueInvoices.filter((inv) => inv.daysOverdue >= 7).length}
                </span>
              </div>
            </Card>

            <Card>
              <div className="flex flex-col">
                <span className="text-sm font-medium text-gray-600">30+ Days Overdue</span>
                <span className="text-3xl font-bold text-red-700 mt-2">
                  {overdueInvoices.filter((inv) => inv.daysOverdue >= 30).length}
                </span>
              </div>
            </Card>
          </div>
        )}
      </div>

      {selectedInvoice && (
        <ReminderEmailPreviewModal
          isOpen={isReminderEmailPreviewOpen}
          onClose={() => setIsReminderEmailPreviewOpen(false)}
          onConfirmSend={handleConfirmSendReminder}
          onEdit={handleEditFromReminderPreview}
          invoice={selectedInvoice}
          reminderType={selectedReminderType}
          isLoading={isSending}
        />
      )}

      <SuccessModal
        isOpen={isSuccessModalOpen}
        onClose={() => {
          setIsSuccessModalOpen(false);
          setSelectedInvoiceId(null);
          setSelectedInvoice(null);
        }}
        title={successMessage.title}
        message={successMessage.message}
        details={successMessage.details}
      />
    </AppLayout>
  );
}
