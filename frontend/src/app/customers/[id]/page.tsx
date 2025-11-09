'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import { AppLayout } from '@/components/AppLayout';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Loading } from '@/components/ui/Loading';
import { Table } from '@/components/ui/Table';
import { getCustomerById, deleteCustomer } from '@/lib/api/customers';
import { getInvoicesByCustomer } from '@/lib/api/invoices';
import { CustomerDto } from '@/types/customer';
import { InvoiceDto } from '@/types/invoice';
import { formatDate, formatCurrency, formatInvoiceStatus } from '@/lib/format';
import { getInvoiceStatusBadgeVariant } from '@/components/ui/Badge';

export default function CustomerDetailPage() {
  const router = useRouter();
  const params = useParams();
  const id = params.id as string;

  const [customer, setCustomer] = useState<CustomerDto | null>(null);
  const [invoices, setInvoices] = useState<InvoiceDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true);
        const customerData = await getCustomerById(id);
        setCustomer(customerData);
        // TODO: Implement backend endpoint GET /api/invoices/customer/{id} to fetch customer invoices
        setInvoices([]);
      } catch (error) {
        console.error('Error fetching customer:', error);
        alert('Failed to load customer details.');
        router.push('/customers');
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, [id, router]);

  const handleDelete = async () => {
    if (!confirm('Are you sure you want to delete this customer? This action cannot be undone.')) {
      return;
    }

    try {
      await deleteCustomer(id);
      router.push('/customers');
    } catch (error) {
      console.error('Error deleting customer:', error);
      alert('Failed to delete customer. They may have associated invoices.');
    }
  };

  if (isLoading) {
    return (
      <AppLayout>
        <Loading text="Loading customer details..." />
      </AppLayout>
    );
  }

  if (!customer) {
    return null;
  }

  return (
    <AppLayout>
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">{customer.businessName}</h1>
            <p className="text-gray-600 mt-1">{customer.contactName}</p>
          </div>
          <div className="flex space-x-3">
            <Link href={`/customers/${id}/edit`}>
              <Button variant="secondary">Edit</Button>
            </Link>
            <Button variant="danger" onClick={handleDelete}>
              Delete
            </Button>
          </div>
        </div>

        {/* Customer Information */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Card title="Contact Information">
            <div className="space-y-3">
              <div>
                <span className="text-sm font-medium text-gray-600">Email:</span>
                <p className="text-gray-900">{customer.email}</p>
              </div>
              <div>
                <span className="text-sm font-medium text-gray-600">Phone:</span>
                <p className="text-gray-900">{customer.phone || 'N/A'}</p>
              </div>
              <div>
                <span className="text-sm font-medium text-gray-600">Status:</span>
                <div className="mt-1">
                  <Badge variant={customer.active ? 'success' : 'danger'}>
                    {customer.active ? 'Active' : 'Inactive'}
                  </Badge>
                </div>
              </div>
              <div>
                <span className="text-sm font-medium text-gray-600">Created:</span>
                <p className="text-gray-900">{formatDate(customer.createdAt)}</p>
              </div>
            </div>
          </Card>

          <Card title="Billing Address">
            <div className="text-gray-900">
              <p>{customer.billingAddress.street}</p>
              <p>
                {customer.billingAddress.city}, {customer.billingAddress.state}{' '}
                {customer.billingAddress.postalCode}
              </p>
              <p>{customer.billingAddress.country}</p>
            </div>

            {customer.shippingAddress && (
              <div className="mt-6">
                <h4 className="text-sm font-medium text-gray-600 mb-2">Shipping Address:</h4>
                <div className="text-gray-900">
                  <p>{customer.shippingAddress.street}</p>
                  <p>
                    {customer.shippingAddress.city}, {customer.shippingAddress.state}{' '}
                    {customer.shippingAddress.postalCode}
                  </p>
                  <p>{customer.shippingAddress.country}</p>
                </div>
              </div>
            )}
          </Card>
        </div>

        {/* Invoices */}
        <Card
          title={`Invoices (${invoices.length})`}
          action={
            <Link href={`/invoices/new?customerId=${id}`}>
              <Button size="sm">Create Invoice</Button>
            </Link>
          }
        >
          <Table
            data={invoices}
            columns={[
              { header: 'Invoice #', accessor: 'invoiceNumber' },
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
            emptyMessage="No invoices for this customer yet."
          />
        </Card>
      </div>
    </AppLayout>
  );
}
