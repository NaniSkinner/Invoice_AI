'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { AppLayout } from '@/components/AppLayout';
import { Card } from '@/components/ui/Card';
import { Badge, getInvoiceStatusBadgeVariant } from '@/components/ui/Badge';
import { Table } from '@/components/ui/Table';
import { Loading } from '@/components/ui/Loading';
import { Button } from '@/components/ui/Button';
import { getAllInvoices } from '@/lib/api/invoices';
import { getAllPayments } from '@/lib/api/payments';
import { getOverdueInvoices } from '@/lib/api/reminders';
import { InvoiceDto } from '@/types/invoice';
import { PaymentDto } from '@/types/payment';
import { OverdueInvoiceDto } from '@/types/reminder';
import { formatCurrency, formatDate, formatInvoiceStatus } from '@/lib/format';

export default function DashboardPage() {
  const router = useRouter();
  const [invoices, setInvoices] = useState<InvoiceDto[]>([]);
  const [payments, setPayments] = useState<PaymentDto[]>([]);
  const [overdueInvoices, setOverdueInvoices] = useState<OverdueInvoiceDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true);
        const [invoicesData, paymentsData, overdueData] = await Promise.all([
          getAllInvoices(),
          getAllPayments(),
          getOverdueInvoices(),
        ]);
        setInvoices(invoicesData);
        setPayments(paymentsData);
        setOverdueInvoices(overdueData);
      } catch (error) {
        console.error('Error fetching dashboard data:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, []);

  if (isLoading) {
    return (
      <AppLayout>
        <Loading text="Loading dashboard..." />
      </AppLayout>
    );
  }

  // Calculate metrics
  const totalRevenue = invoices
    .filter((inv) => inv.status === 'PAID')
    .reduce((sum, inv) => sum + inv.totalAmount, 0);

  const draftCount = invoices.filter((inv) => inv.status === 'DRAFT').length;
  const sentCount = invoices.filter((inv) => inv.status === 'SENT').length;
  const paidCount = invoices.filter((inv) => inv.status === 'PAID').length;
  const cancelledCount = invoices.filter((inv) => inv.status === 'CANCELLED').length;

  const recentInvoices = [...invoices]
    .sort((a, b) => new Date(b.issueDate).getTime() - new Date(a.issueDate).getTime())
    .slice(0, 5);

  const recentPayments = [...payments]
    .sort((a, b) => new Date(b.paymentDate).getTime() - new Date(a.paymentDate).getTime())
    .slice(0, 5);

  return (
    <AppLayout>
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
          <Link href="/invoices/new">
            <Button>Create Invoice</Button>
          </Link>
        </div>

        {/* Metrics Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <Card>
            <div className="flex flex-col">
              <span className="text-sm font-medium text-gray-600">Total Revenue</span>
              <span className="text-3xl font-bold text-green-600 mt-2">
                {formatCurrency(totalRevenue)}
              </span>
            </div>
          </Card>

          <Card>
            <div className="flex flex-col">
              <span className="text-sm font-medium text-gray-600">Draft</span>
              <span className="text-3xl font-bold text-gray-600 mt-2">{draftCount}</span>
            </div>
          </Card>

          <Card>
            <div className="flex flex-col">
              <span className="text-sm font-medium text-gray-600">Sent</span>
              <span className="text-3xl font-bold text-blue-600 mt-2">{sentCount}</span>
            </div>
          </Card>

          <Card>
            <div className="flex flex-col">
              <span className="text-sm font-medium text-gray-600">Paid</span>
              <span className="text-3xl font-bold text-green-600 mt-2">{paidCount}</span>
            </div>
          </Card>
        </div>

        {/* Overdue Invoices Alert */}
        {overdueInvoices.length > 0 && (
          <Card className="border-l-4 border-red-500">
            <div className="flex justify-between items-start">
              <div>
                <h3 className="text-lg font-semibold text-red-800">
                  Overdue Invoices ({overdueInvoices.length})
                </h3>
                <p className="text-sm text-gray-600 mt-1">
                  You have {overdueInvoices.length} invoice(s) that are past due.
                </p>
              </div>
              <Link href="/reminders">
                <Button variant="danger" size="sm">
                  View Overdue
                </Button>
              </Link>
            </div>
          </Card>
        )}

        {/* Recent Invoices */}
        <Card title="Recent Invoices" action={
          <Link href="/invoices">
            <Button variant="outline" size="sm">
              View All
            </Button>
          </Link>
        }>
          <Table
            data={recentInvoices}
            columns={[
              { header: 'Invoice #', accessor: 'invoiceNumber' },
              { header: 'Customer', accessor: 'customerName' },
              { header: 'Issue Date', accessor: (row) => formatDate(row.issueDate) },
              { header: 'Due Date', accessor: (row) => formatDate(row.dueDate) },
              { header: 'Amount', accessor: (row) => formatCurrency(row.totalAmount) },
              {
                header: 'Status',
                accessor: (row) => (
                  <Badge variant={getInvoiceStatusBadgeVariant(row.status)}>
                    {formatInvoiceStatus(row.status)}
                  </Badge>
                ),
              },
            ]}
            onRowClick={(invoice) => router.push(`/invoices/${invoice.id}`)}
            emptyMessage="No invoices yet. Create your first invoice!"
          />
        </Card>

        {/* Recent Payments */}
        <Card title="Recent Payments">
          <Table
            data={recentPayments}
            columns={[
              { header: 'Invoice #', accessor: 'invoiceNumber' },
              { header: 'Date', accessor: (row) => formatDate(row.paymentDate) },
              { header: 'Amount', accessor: (row) => formatCurrency(row.paymentAmount) },
              { header: 'Method', accessor: (row) => row.paymentMethod.replace('_', ' ') },
            ]}
            emptyMessage="No payments recorded yet."
          />
        </Card>
      </div>
    </AppLayout>
  );
}
