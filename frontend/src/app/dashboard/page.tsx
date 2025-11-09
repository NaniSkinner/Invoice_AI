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
import { CircularProgress } from '@/components/ui/CircularProgress';
import { RevenueTrendChart } from '@/components/charts/RevenueTrendChart';
import { InvoiceStatusDonut } from '@/components/charts/InvoiceStatusDonut';
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

  // Calculate percentages for circular progress
  const totalInvoices = invoices.length;
  const paidPercentage = totalInvoices > 0 ? (paidCount / totalInvoices) * 100 : 0;
  const sentPercentage = totalInvoices > 0 ? (sentCount / totalInvoices) * 100 : 0;
  const draftPercentage = totalInvoices > 0 ? (draftCount / totalInvoices) * 100 : 0;

  // Generate revenue trend data (last 7 days)
  const revenueTrendData = Array.from({ length: 7 }, (_, i) => {
    const date = new Date();
    date.setDate(date.getDate() - (6 - i));
    const dateStr = date.toISOString().split('T')[0];
    
    // Calculate revenue for this day
    const dayRevenue = payments
      .filter(p => p.paymentDate.startsWith(dateStr))
      .reduce((sum, p) => sum + p.paymentAmount, 0);
    
    return {
      date: dateStr,
      revenue: dayRevenue,
    };
  });

  // Prepare donut chart data
  const statusChartData = [
    { name: 'Paid', value: paidCount, color: '#5A8F7B' },
    { name: 'Sent', value: sentCount, color: '#64748B' },
    { name: 'Draft', value: draftCount, color: '#94A3B8' },
    { name: 'Cancelled', value: cancelledCount, color: '#EF4444' },
  ].filter(item => item.value > 0);

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
            <div className="flex items-center justify-between">
              <div className="flex flex-col">
                <span className="text-sm font-medium text-gray-600">Total Revenue</span>
                <span className="text-3xl font-bold text-primary-600 mt-2">
                  {formatCurrency(totalRevenue)}
                </span>
                <span className="text-xs text-gray-500 mt-1">
                  {paidCount} paid invoices
                </span>
              </div>
              <div className="ml-4">
                <CircularProgress 
                  percentage={paidPercentage} 
                  size={70}
                  strokeWidth={6}
                  color="#5A8F7B"
                />
              </div>
            </div>
          </Card>

          <Card>
            <div className="flex items-center justify-between">
              <div className="flex flex-col">
                <span className="text-sm font-medium text-gray-600">Draft</span>
                <span className="text-3xl font-bold text-slate-600 mt-2">{draftCount}</span>
                <span className="text-xs text-gray-500 mt-1">
                  {draftPercentage.toFixed(0)}% of total
                </span>
              </div>
              <div className="ml-4">
                <CircularProgress 
                  percentage={draftPercentage} 
                  size={70}
                  strokeWidth={6}
                  color="#94A3B8"
                />
              </div>
            </div>
          </Card>

          <Card>
            <div className="flex items-center justify-between">
              <div className="flex flex-col">
                <span className="text-sm font-medium text-gray-600">Sent</span>
                <span className="text-3xl font-bold text-slate-700 mt-2">{sentCount}</span>
                <span className="text-xs text-gray-500 mt-1">
                  {sentPercentage.toFixed(0)}% of total
                </span>
              </div>
              <div className="ml-4">
                <CircularProgress 
                  percentage={sentPercentage} 
                  size={70}
                  strokeWidth={6}
                  color="#64748B"
                />
              </div>
            </div>
          </Card>

          <Card>
            <div className="flex items-center justify-between">
              <div className="flex flex-col">
                <span className="text-sm font-medium text-gray-600">Paid</span>
                <span className="text-3xl font-bold text-primary-600 mt-2">{paidCount}</span>
                <span className="text-xs text-gray-500 mt-1">
                  {paidPercentage.toFixed(0)}% success rate
                </span>
              </div>
              <div className="ml-4">
                <CircularProgress 
                  percentage={paidPercentage} 
                  size={70}
                  strokeWidth={6}
                  color="#5A8F7B"
                />
              </div>
            </div>
          </Card>
        </div>

        {/* Overdue Invoices Alert */}
        {overdueInvoices.length > 0 && (
          <Card className="border-l-4 border-red-500 bg-red-50">
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

        {/* Charts Section */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Revenue Trend Chart */}
          <Card title="Revenue Overview">
            <div className="mt-4">
              <div className="flex items-center justify-between mb-4">
                <div>
                  <p className="text-sm text-gray-600">Last 7 Days</p>
                  <p className="text-2xl font-bold text-primary-600">
                    {formatCurrency(revenueTrendData.reduce((sum, d) => sum + d.revenue, 0))}
                  </p>
                </div>
              </div>
              <RevenueTrendChart data={revenueTrendData} />
            </div>
          </Card>

          {/* Invoice Status Distribution */}
          <Card title="Invoice Status Distribution">
            <div className="mt-4">
              <div className="mb-4">
                <p className="text-sm text-gray-600">Total Invoices</p>
                <p className="text-2xl font-bold text-gray-800">{totalInvoices}</p>
              </div>
              {statusChartData.length > 0 ? (
                <InvoiceStatusDonut data={statusChartData} />
              ) : (
                <div className="text-center py-12 text-gray-500">
                  <p>No invoice data available</p>
                </div>
              )}
            </div>
          </Card>
        </div>

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
