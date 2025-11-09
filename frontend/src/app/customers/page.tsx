'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { AppLayout } from '@/components/AppLayout';
import { Card } from '@/components/ui/Card';
import { Table } from '@/components/ui/Table';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Loading } from '@/components/ui/Loading';
import { Badge } from '@/components/ui/Badge';
import { getAllCustomers } from '@/lib/api/customers';
import { CustomerDto } from '@/types/customer';
import { formatDate } from '@/lib/format';

export default function CustomersPage() {
  const router = useRouter();
  const [customers, setCustomers] = useState<CustomerDto[]>([]);
  const [filteredCustomers, setFilteredCustomers] = useState<CustomerDto[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchCustomers = async () => {
      try {
        setIsLoading(true);
        const data = await getAllCustomers();
        setCustomers(data);
        setFilteredCustomers(data);
      } catch (error) {
        console.error('Error fetching customers:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchCustomers();
  }, []);

  useEffect(() => {
    if (searchQuery) {
      const filtered = customers.filter(
        (customer) =>
          customer.businessName.toLowerCase().includes(searchQuery.toLowerCase()) ||
          customer.contactName.toLowerCase().includes(searchQuery.toLowerCase()) ||
          customer.email.toLowerCase().includes(searchQuery.toLowerCase())
      );
      setFilteredCustomers(filtered);
    } else {
      setFilteredCustomers(customers);
    }
  }, [searchQuery, customers]);

  if (isLoading) {
    return (
      <AppLayout>
        <Loading text="Loading customers..." />
      </AppLayout>
    );
  }

  return (
    <AppLayout>
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <h1 className="text-3xl font-bold text-gray-900">Customers</h1>
          <Link href="/customers/new">
            <Button>Add Customer</Button>
          </Link>
        </div>

        <Card>
          <div className="mb-4">
            <Input
              placeholder="Search customers..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>

          <Table
            data={filteredCustomers}
            columns={[
              { header: 'Business Name', accessor: 'businessName' },
              { header: 'Contact', accessor: 'contactName' },
              { header: 'Email', accessor: 'email' },
              { header: 'Phone', accessor: (row) => row.phone || '-' },
              { header: 'City', accessor: (row) => row.billingAddress.city },
              {
                header: 'Status',
                accessor: (row) => (
                  <Badge variant={row.active ? 'success' : 'danger'}>
                    {row.active ? 'Active' : 'Inactive'}
                  </Badge>
                ),
              },
              { header: 'Created', accessor: (row) => formatDate(row.createdAt) },
            ]}
            onRowClick={(customer) => router.push(`/customers/${customer.id}`)}
            emptyMessage="No customers found. Add your first customer!"
          />
        </Card>
      </div>
    </AppLayout>
  );
}
