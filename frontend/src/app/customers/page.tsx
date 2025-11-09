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
import { Avatar } from '@/components/ui/Avatar';
import { getAllCustomers } from '@/lib/api/customers';
import { CustomerDto } from '@/types/customer';
import { formatDate } from '@/lib/format';

export default function CustomersPage() {
  const router = useRouter();
  const [customers, setCustomers] = useState<CustomerDto[]>([]);
  const [filteredCustomers, setFilteredCustomers] = useState<CustomerDto[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');

  useEffect(() => {
    const fetchCustomers = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const data = await getAllCustomers();
        setCustomers(data);
        setFilteredCustomers(data);
      } catch (error: any) {
        console.error('Error fetching customers:', error);
        setError(error.response?.data?.message || error.message || 'Failed to load customers');
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
          <div className="flex items-center space-x-3">
            {/* View Toggle */}
            <div className="flex items-center bg-white rounded-lg border border-gray-200 p-1">
              <button
                onClick={() => setViewMode('grid')}
                className={`p-2 rounded transition-colors ${
                  viewMode === 'grid' 
                    ? 'bg-primary-100 text-primary-700' 
                    : 'text-gray-500 hover:text-gray-700'
                }`}
                aria-label="Grid view"
              >
                <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
                </svg>
              </button>
              <button
                onClick={() => setViewMode('list')}
                className={`p-2 rounded transition-colors ${
                  viewMode === 'list' 
                    ? 'bg-primary-100 text-primary-700' 
                    : 'text-gray-500 hover:text-gray-700'
                }`}
                aria-label="List view"
              >
                <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                </svg>
              </button>
            </div>
            <Link href="/customers/new">
              <Button>Add Customer</Button>
            </Link>
          </div>
        </div>

        {error && (
          <Card className="border-l-4 border-red-500 bg-red-50">
            <div className="flex items-start">
              <div className="flex-1">
                <h3 className="text-lg font-semibold text-red-800">Error Loading Customers</h3>
                <p className="text-sm text-gray-600 mt-1">{error}</p>
              </div>
            </div>
          </Card>
        )}

        <Card>
          <div className="mb-6">
            <Input
              placeholder="Search customers by name, email, or company..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>

          {viewMode === 'grid' ? (
            /* Grid View */
            filteredCustomers.length === 0 ? (
              <div className="text-center py-12 text-gray-500">
                <p>No customers found. Add your first customer!</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {filteredCustomers.map((customer) => (
                  <div
                    key={customer.id}
                    onClick={() => router.push(`/customers/${customer.id}`)}
                    className="bg-white border border-gray-200 rounded-card p-5 hover:shadow-card-hover transition-all cursor-pointer group"
                  >
                    <div className="flex items-start space-x-4">
                      <Avatar 
                        name={customer.businessName} 
                        size="lg"
                      />
                      <div className="flex-1 min-w-0">
                        <h3 className="text-lg font-semibold text-gray-900 truncate group-hover:text-primary-600 transition-colors">
                          {customer.businessName}
                        </h3>
                        <p className="text-sm text-gray-600 truncate">{customer.contactName}</p>
                      </div>
                      <Badge variant={customer.active ? 'success' : 'danger'}>
                        {customer.active ? 'Active' : 'Inactive'}
                      </Badge>
                    </div>
                    
                    <div className="mt-4 space-y-2">
                      <div className="flex items-center text-sm text-gray-600">
                        <svg className="h-4 w-4 mr-2 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                        </svg>
                        <span className="truncate">{customer.email}</span>
                      </div>
                      {customer.phone && (
                        <div className="flex items-center text-sm text-gray-600">
                          <svg className="h-4 w-4 mr-2 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                          </svg>
                          <span>{customer.phone}</span>
                        </div>
                      )}
                      <div className="flex items-center text-sm text-gray-600">
                        <svg className="h-4 w-4 mr-2 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                        </svg>
                        <span>{customer.billingAddress.city}, {customer.billingAddress.state}</span>
                      </div>
                    </div>

                    <div className="mt-4 pt-4 border-t border-gray-100">
                      <p className="text-xs text-gray-500">
                        Customer since {formatDate(customer.createdAt)}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            )
          ) : (
            /* List View */
            <Table
              data={filteredCustomers}
              columns={[
                { 
                  header: 'Customer', 
                  accessor: (row) => (
                    <div className="flex items-center space-x-3">
                      <Avatar name={row.businessName} size="sm" />
                      <div>
                        <p className="font-medium text-gray-900">{row.businessName}</p>
                        <p className="text-sm text-gray-500">{row.contactName}</p>
                      </div>
                    </div>
                  )
                },
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
          )}
        </Card>
      </div>
    </AppLayout>
  );
}
