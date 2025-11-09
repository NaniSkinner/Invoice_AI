'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { AppLayout } from '@/components/AppLayout';
import { Card } from '@/components/ui/Card';
import { Table } from '@/components/ui/Table';
import { Button } from '@/components/ui/Button';
import { Select } from '@/components/ui/Select';
import { Loading } from '@/components/ui/Loading';
import { Badge, getInvoiceStatusBadgeVariant } from '@/components/ui/Badge';
import { getAllInvoices } from '@/lib/api/invoices';
import { InvoiceDto } from '@/types/invoice';
import { formatDate, formatCurrency, formatInvoiceStatus } from '@/lib/format';

export default function InvoicesPage() {
  const router = useRouter();
  const [invoices, setInvoices] = useState<InvoiceDto[]>([]);
  const [filteredInvoices, setFilteredInvoices] = useState<InvoiceDto[]>([]);
  const [statusFilter, setStatusFilter] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchInvoices = async () => {
      try {
        setIsLoading(true);
        const data = await getAllInvoices();
        setInvoices(data);
        setFilteredInvoices(data);
      } catch (error) {
        console.error('Error fetching invoices:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchInvoices();
  }, []);

  useEffect(() => {
    if (statusFilter) {
      const filtered = invoices.filter((invoice) => invoice.status === statusFilter);
      setFilteredInvoices(filtered);
    } else {
      setFilteredInvoices(invoices);
    }
  }, [statusFilter, invoices]);

  if (isLoading) {
    return (
      <AppLayout>
        <Loading text="Loading invoices..." />
      </AppLayout>
    );
  }

  const statusOptions = [
    { value: '', label: 'All Statuses' },
    { value: 'DRAFT', label: 'Draft' },
    { value: 'SENT', label: 'Sent' },
    { value: 'PAID', label: 'Paid' },
    { value: 'CANCELLED', label: 'Cancelled' },
  ];

  return (
    <AppLayout>
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <h1 className="text-3xl font-bold text-gray-900">Invoices</h1>
          <Link href="/invoices/new">
            <Button>Create Invoice</Button>
          </Link>
        </div>

        <Card>
          <div className="mb-4 flex gap-4">
            <div className="w-64">
              <Select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                options={statusOptions}
              />
            </div>
          </div>

          <Table
            data={filteredInvoices}
            columns={[
              { header: 'Invoice #', accessor: 'invoiceNumber' },
              { header: 'Customer', accessor: 'customerName' },
              { header: 'Issue Date', accessor: (row) => formatDate(row.issueDate) },
              { header: 'Due Date', accessor: (row) => formatDate(row.dueDate) },
              { header: 'Total', accessor: (row) => formatCurrency(row.totalAmount) },
              { header: 'Balance', accessor: (row) => formatCurrency(row.balanceRemaining) },
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
            emptyMessage="No invoices found. Create your first invoice!"
          />
        </Card>
      </div>
    </AppLayout>
  );
}
