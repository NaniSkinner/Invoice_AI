# Phase 8: Customer Management UI

**Time Estimate:** 4-6 hours
**Status:** Not Started
**Prerequisites:** Phase 1 (Frontend Setup), Phase 3 (Customer Backend) completed

---

## What You'll Build

- Customer list page with search
- Create customer form
- Edit customer form
- Delete confirmation modal
- API client integration
- React Hook Form + Zod validation
- Tailwind CSS styling
- Customer detail view
- Success/error notifications

---

## Task 8.1: Setup Frontend API Client

### Step 8.1.1: Create API Client

```bash
cd ~/dev/Gauntlet/Invoice_AI/frontend

mkdir -p src/lib

cat > src/lib/api.ts << 'EOF'
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

const auth = {
  username: 'demo',
  password: 'password',
};

async function fetchWithAuth(url: string, options: RequestInit = {}) {
  const headers = new Headers(options.headers);
  headers.set('Authorization', 'Basic ' + btoa(`${auth.username}:${auth.password}`));
  headers.set('Content-Type', 'application/json');

  const response = await fetch(`${API_BASE_URL}${url}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const error = await response.text();
    throw new Error(error || `HTTP error! status: ${response.status}`);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export const api = {
  // Customer endpoints
  customers: {
    list: () => fetchWithAuth('/customers'),
    get: (id: string) => fetchWithAuth(`/customers/${id}`),
    create: (data: any) => fetchWithAuth('/customers', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
    update: (id: string, data: any) => fetchWithAuth(`/customers/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),
    delete: (id: string) => fetchWithAuth(`/customers/${id}`, {
      method: 'DELETE',
    }),
  },

  // Invoice endpoints
  invoices: {
    list: (params?: { customerId?: string; status?: string }) => {
      const query = params
        ? '?' + new URLSearchParams(params as any).toString()
        : '';
      return fetchWithAuth(`/invoices${query}`);
    },
    get: (id: string) => fetchWithAuth(`/invoices/${id}`),
    create: (data: any) => fetchWithAuth('/invoices', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
    send: (id: string) => fetchWithAuth(`/invoices/${id}/send`, {
      method: 'POST',
    }),
    cancel: (id: string, reason: string) => fetchWithAuth(`/invoices/${id}/cancel`, {
      method: 'POST',
      body: JSON.stringify({ cancellationReason: reason }),
    }),
    markPaid: (id: string) => fetchWithAuth(`/invoices/${id}/mark-paid`, {
      method: 'POST',
    }),
  },

  // Payment endpoints
  payments: {
    record: (data: any) => fetchWithAuth('/payments', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
    getByInvoice: (invoiceId: string) => fetchWithAuth(`/invoices/${invoiceId}/payments`),
  },

  // AI endpoints
  ai: {
    chat: (message: string, history?: any[]) => fetchWithAuth('/ai/chat', {
      method: 'POST',
      body: JSON.stringify({ message, history }),
    }),
    sendReminder: (invoiceId: string) => fetchWithAuth(`/ai/invoices/${invoiceId}/send-reminder`, {
      method: 'POST',
    }),
  },
};
EOF
```

### Step 8.1.2: Create Types

```bash
mkdir -p src/types

cat > src/types/customer.ts << 'EOF'
export interface Address {
  street: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
}

export interface Customer {
  id: string;
  businessName: string;
  contactName: string;
  email: string;
  phone: string;
  address: Address;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCustomerRequest {
  businessName: string;
  contactName: string;
  email: string;
  phone: string;
  address: Address;
}
EOF
```

---

## Task 8.2: Create Customer List Page

### Step 8.2.1: Create Customer List Component

```bash
mkdir -p src/app/customers

cat > src/app/customers/page.tsx << 'EOF'
'use client';

import { useEffect, useState } from 'react';
import { api } from '@/lib/api';
import { Customer } from '@/types/customer';
import Link from 'next/link';

export default function CustomersPage() {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    loadCustomers();
  }, []);

  const loadCustomers = async () => {
    try {
      setLoading(true);
      const data = await api.customers.list();
      setCustomers(data);
    } catch (error) {
      console.error('Failed to load customers:', error);
      alert('Failed to load customers');
    } finally {
      setLoading(false);
    }
  };

  const filteredCustomers = customers.filter((customer) =>
    customer.businessName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    customer.contactName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    customer.email.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-xl">Loading customers...</div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Customers</h1>
        <Link
          href="/customers/new"
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          Add Customer
        </Link>
      </div>

      <div className="mb-4">
        <input
          type="text"
          placeholder="Search customers..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full px-4 py-2 border rounded-lg"
        />
      </div>

      <div className="bg-white shadow rounded-lg overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                Business Name
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                Contact
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                Email
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                Phone
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {filteredCustomers.map((customer) => (
              <tr key={customer.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap">
                  <Link
                    href={`/customers/${customer.id}`}
                    className="text-blue-600 hover:underline"
                  >
                    {customer.businessName}
                  </Link>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {customer.contactName}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {customer.email}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {customer.phone}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <Link
                    href={`/customers/${customer.id}/edit`}
                    className="text-blue-600 hover:underline mr-4"
                  >
                    Edit
                  </Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {filteredCustomers.length === 0 && (
          <div className="text-center py-8 text-gray-500">
            No customers found
          </div>
        )}
      </div>
    </div>
  );
}
EOF
```

---

## Task 8.3: Create Customer Form Component

### Step 8.3.1: Install Form Dependencies

```bash
npm install react-hook-form zod @hookform/resolvers
```

### Step 8.3.2: Create Customer Form Component

```bash
mkdir -p src/components/customers

cat > src/components/customers/CustomerForm.tsx << 'EOF'
'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { CreateCustomerRequest } from '@/types/customer';

const addressSchema = z.object({
  street: z.string().min(1, 'Street is required'),
  city: z.string().min(1, 'City is required'),
  state: z.string().min(2, 'State is required').max(2, 'Use 2-letter state code'),
  zipCode: z.string().min(5, 'Zip code is required'),
  country: z.string().min(1, 'Country is required'),
});

const customerSchema = z.object({
  businessName: z.string().min(1, 'Business name is required'),
  contactName: z.string().min(1, 'Contact name is required'),
  email: z.string().email('Invalid email address'),
  phone: z.string().min(1, 'Phone is required'),
  address: addressSchema,
});

interface CustomerFormProps {
  initialData?: CreateCustomerRequest;
  onSubmit: (data: CreateCustomerRequest) => Promise<void>;
  onCancel: () => void;
  submitLabel?: string;
}

export default function CustomerForm({
  initialData,
  onSubmit,
  onCancel,
  submitLabel = 'Create Customer',
}: CustomerFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<CreateCustomerRequest>({
    resolver: zodResolver(customerSchema),
    defaultValues: initialData || {
      businessName: '',
      contactName: '',
      email: '',
      phone: '',
      address: {
        street: '',
        city: '',
        state: '',
        zipCode: '',
        country: 'USA',
      },
    },
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      <div className="bg-white shadow rounded-lg p-6">
        <h3 className="text-lg font-medium mb-4">Business Information</h3>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Business Name *
            </label>
            <input
              {...register('businessName')}
              className="w-full px-3 py-2 border rounded-md"
            />
            {errors.businessName && (
              <p className="text-red-500 text-sm mt-1">
                {errors.businessName.message}
              </p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Contact Name *
            </label>
            <input
              {...register('contactName')}
              className="w-full px-3 py-2 border rounded-md"
            />
            {errors.contactName && (
              <p className="text-red-500 text-sm mt-1">
                {errors.contactName.message}
              </p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Email *
            </label>
            <input
              type="email"
              {...register('email')}
              className="w-full px-3 py-2 border rounded-md"
            />
            {errors.email && (
              <p className="text-red-500 text-sm mt-1">
                {errors.email.message}
              </p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Phone *
            </label>
            <input
              {...register('phone')}
              className="w-full px-3 py-2 border rounded-md"
            />
            {errors.phone && (
              <p className="text-red-500 text-sm mt-1">
                {errors.phone.message}
              </p>
            )}
          </div>
        </div>
      </div>

      <div className="bg-white shadow rounded-lg p-6">
        <h3 className="text-lg font-medium mb-4">Address</h3>

        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Street *
            </label>
            <input
              {...register('address.street')}
              className="w-full px-3 py-2 border rounded-md"
            />
            {errors.address?.street && (
              <p className="text-red-500 text-sm mt-1">
                {errors.address.street.message}
              </p>
            )}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                City *
              </label>
              <input
                {...register('address.city')}
                className="w-full px-3 py-2 border rounded-md"
              />
              {errors.address?.city && (
                <p className="text-red-500 text-sm mt-1">
                  {errors.address.city.message}
                </p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                State *
              </label>
              <input
                {...register('address.state')}
                placeholder="CA"
                maxLength={2}
                className="w-full px-3 py-2 border rounded-md"
              />
              {errors.address?.state && (
                <p className="text-red-500 text-sm mt-1">
                  {errors.address.state.message}
                </p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Zip Code *
              </label>
              <input
                {...register('address.zipCode')}
                className="w-full px-3 py-2 border rounded-md"
              />
              {errors.address?.zipCode && (
                <p className="text-red-500 text-sm mt-1">
                  {errors.address.zipCode.message}
                </p>
              )}
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Country *
            </label>
            <input
              {...register('address.country')}
              className="w-full px-3 py-2 border rounded-md"
            />
            {errors.address?.country && (
              <p className="text-red-500 text-sm mt-1">
                {errors.address.country.message}
              </p>
            )}
          </div>
        </div>
      </div>

      <div className="flex justify-end gap-4">
        <button
          type="button"
          onClick={onCancel}
          className="px-4 py-2 border rounded-md hover:bg-gray-50"
          disabled={isSubmitting}
        >
          Cancel
        </button>
        <button
          type="submit"
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
          disabled={isSubmitting}
        >
          {isSubmitting ? 'Saving...' : submitLabel}
        </button>
      </div>
    </form>
  );
}
EOF
```

---

## Task 8.4: Create New Customer Page

### Step 8.4.1: Create New Customer Page

```bash
cat > src/app/customers/new/page.tsx << 'EOF'
'use client';

import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { CreateCustomerRequest } from '@/types/customer';
import CustomerForm from '@/components/customers/CustomerForm';

export default function NewCustomerPage() {
  const router = useRouter();

  const handleSubmit = async (data: CreateCustomerRequest) => {
    try {
      await api.customers.create(data);
      alert('Customer created successfully');
      router.push('/customers');
    } catch (error) {
      console.error('Failed to create customer:', error);
      alert('Failed to create customer: ' + (error as Error).message);
    }
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">Create New Customer</h1>
      <CustomerForm
        onSubmit={handleSubmit}
        onCancel={() => router.push('/customers')}
        submitLabel="Create Customer"
      />
    </div>
  );
}
EOF
```

---

## Task 8.5: Create Edit Customer Page

### Step 8.5.1: Create Edit Customer Page

```bash
mkdir -p src/app/customers/\[id\]/edit

cat > 'src/app/customers/[id]/edit/page.tsx' << 'EOF'
'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { api } from '@/lib/api';
import { Customer, CreateCustomerRequest } from '@/types/customer';
import CustomerForm from '@/components/customers/CustomerForm';

export default function EditCustomerPage() {
  const router = useRouter();
  const params = useParams();
  const customerId = params.id as string;

  const [customer, setCustomer] = useState<Customer | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadCustomer();
  }, [customerId]);

  const loadCustomer = async () => {
    try {
      const data = await api.customers.get(customerId);
      setCustomer(data);
    } catch (error) {
      console.error('Failed to load customer:', error);
      alert('Failed to load customer');
      router.push('/customers');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (data: CreateCustomerRequest) => {
    try {
      await api.customers.update(customerId, data);
      alert('Customer updated successfully');
      router.push('/customers');
    } catch (error) {
      console.error('Failed to update customer:', error);
      alert('Failed to update customer: ' + (error as Error).message);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-xl">Loading customer...</div>
      </div>
    );
  }

  if (!customer) {
    return <div>Customer not found</div>;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">Edit Customer</h1>
      <CustomerForm
        initialData={customer}
        onSubmit={handleSubmit}
        onCancel={() => router.push('/customers')}
        submitLabel="Update Customer"
      />
    </div>
  );
}
EOF
```

---

## Task 8.6: Create Customer Detail Page

### Step 8.6.1: Create Customer Detail Page

```bash
mkdir -p src/app/customers/\[id\]

cat > 'src/app/customers/[id]/page.tsx' << 'EOF'
'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import { api } from '@/lib/api';
import { Customer } from '@/types/customer';

export default function CustomerDetailPage() {
  const router = useRouter();
  const params = useParams();
  const customerId = params.id as string;

  const [customer, setCustomer] = useState<Customer | null>(null);
  const [loading, setLoading] = useState(true);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  useEffect(() => {
    loadCustomer();
  }, [customerId]);

  const loadCustomer = async () => {
    try {
      const data = await api.customers.get(customerId);
      setCustomer(data);
    } catch (error) {
      console.error('Failed to load customer:', error);
      alert('Failed to load customer');
      router.push('/customers');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    try {
      await api.customers.delete(customerId);
      alert('Customer deleted successfully');
      router.push('/customers');
    } catch (error) {
      console.error('Failed to delete customer:', error);
      alert('Failed to delete customer: ' + (error as Error).message);
      setShowDeleteModal(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-xl">Loading customer...</div>
      </div>
    );
  }

  if (!customer) {
    return <div>Customer not found</div>;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">{customer.businessName}</h1>
        <div className="flex gap-2">
          <Link
            href={`/customers/${customerId}/edit`}
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            Edit
          </Link>
          <button
            onClick={() => setShowDeleteModal(true)}
            className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
          >
            Delete
          </button>
        </div>
      </div>

      <div className="bg-white shadow rounded-lg p-6 mb-6">
        <h2 className="text-xl font-semibold mb-4">Contact Information</h2>
        <dl className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <dt className="text-sm font-medium text-gray-500">Contact Name</dt>
            <dd className="mt-1 text-sm text-gray-900">{customer.contactName}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Email</dt>
            <dd className="mt-1 text-sm text-gray-900">{customer.email}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Phone</dt>
            <dd className="mt-1 text-sm text-gray-900">{customer.phone}</dd>
          </div>
        </dl>
      </div>

      <div className="bg-white shadow rounded-lg p-6">
        <h2 className="text-xl font-semibold mb-4">Address</h2>
        <address className="not-italic text-sm text-gray-900">
          {customer.address.street}<br />
          {customer.address.city}, {customer.address.state} {customer.address.zipCode}<br />
          {customer.address.country}
        </address>
      </div>

      {/* Delete Confirmation Modal */}
      {showDeleteModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-md w-full">
            <h3 className="text-lg font-semibold mb-4">Delete Customer</h3>
            <p className="text-gray-700 mb-6">
              Are you sure you want to delete {customer.businessName}? This action cannot be undone.
            </p>
            <div className="flex justify-end gap-4">
              <button
                onClick={() => setShowDeleteModal(false)}
                className="px-4 py-2 border rounded hover:bg-gray-50"
              >
                Cancel
              </button>
              <button
                onClick={handleDelete}
                className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
EOF
```

---

## Task 8.7: Test Customer Management UI

### Step 8.7.1: Start Backend

```bash
cd ~/dev/Gauntlet/Invoice_AI/backend
./mvnw spring-boot:run
```

### Step 8.7.2: Start Frontend

```bash
cd ~/dev/Gauntlet/Invoice_AI/frontend
npm run dev
```

### Step 8.7.3: Test in Browser

Open [http://localhost:3000/customers](http://localhost:3000/customers)

**Test Scenarios:**
1. ✅ Click "Add Customer" and create a new customer
2. ✅ Verify customer appears in list
3. ✅ Click on customer name to view details
4. ✅ Click "Edit" and update customer information
5. ✅ Use search box to filter customers
6. ✅ Test form validation (try submitting empty fields)
7. ✅ Test email validation
8. ✅ Test state validation (2 letters only)
9. ✅ Delete a customer

---

## Task 8.8: Git Commit

```bash
git add .

git commit -m "$(cat <<'EOF'
Phase 8: Customer Management UI

Implemented customer management frontend:
- Customer list page with search functionality
- Create customer form with validation
- Edit customer form
- Customer detail view
- Delete confirmation modal
- API client with authentication
- React Hook Form + Zod validation
- Tailwind CSS styling
- TypeScript types for Customer and Address
- Success/error handling

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"
```

---

## Verification Checklist

After completing all tasks, verify:

- [ ] Customer list page loads
- [ ] Search functionality works
- [ ] Create customer form works
- [ ] Form validation works (required fields, email, state)
- [ ] Customer detail page displays correctly
- [ ] Edit customer form pre-fills data
- [ ] Update customer works
- [ ] Delete customer works with confirmation
- [ ] Delete modal shows and cancels properly
- [ ] Navigation works (links to detail, edit, list)
- [ ] Error messages display for validation failures
- [ ] Success messages display after operations

---

## Troubleshooting

### Issue: CORS errors
**Solution:** Ensure backend CORS configuration allows localhost:3000

### Issue: 401 Unauthorized
**Solution:** Check Basic Auth credentials in api.ts match backend

### Issue: Customer not found after creation
**Solution:** Reload the list page after creating

### Issue: Form validation not working
**Solution:** Verify zod schema matches field names exactly

---

## What's Next?

Continue to [Phase-09-Tasks.md](Phase-09-Tasks.md) for Invoice Management UI implementation.

---

**Phase 8 Complete!** ✅

You now have a fully functional customer management UI with create, read, update, and delete operations.
