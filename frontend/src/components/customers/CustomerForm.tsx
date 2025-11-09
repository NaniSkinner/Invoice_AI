'use client';

import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { customerSchema } from '@/lib/validation';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { CustomerDto } from '@/types/customer';

interface CustomerFormData {
  businessName: string;
  contactName: string;
  email: string;
  phone?: string;
  billingAddress: {
    street: string;
    city: string;
    state: string;
    postalCode: string;
    country: string;
  };
  shippingAddress?: {
    street: string;
    city: string;
    state: string;
    postalCode: string;
    country: string;
  };
  active?: boolean;
}

interface CustomerFormProps {
  customer?: CustomerDto;
  onSubmit: (data: CustomerFormData) => void;
  onCancel: () => void;
  isLoading?: boolean;
}

export const CustomerForm: React.FC<CustomerFormProps> = ({
  customer,
  onSubmit,
  onCancel,
  isLoading = false,
}) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    watch,
  } = useForm<CustomerFormData>({
    resolver: zodResolver(customerSchema),
    defaultValues: customer || {
      active: true,
      billingAddress: {
        country: 'USA',
      },
    },
  });

  const [useSameAddress, setUseSameAddress] = React.useState(!customer?.shippingAddress);

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      {/* Basic Information */}
      <div className="space-y-4">
        <h3 className="text-lg font-semibold">Basic Information</h3>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Input
            label="Business Name"
            {...register('businessName')}
            error={errors.businessName?.message}
            required
          />

          <Input
            label="Contact Name"
            {...register('contactName')}
            error={errors.contactName?.message}
            required
          />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Input
            label="Email"
            type="email"
            {...register('email')}
            error={errors.email?.message}
            required
          />

          <Input
            label="Phone"
            type="tel"
            {...register('phone')}
            error={errors.phone?.message}
          />
        </div>
      </div>

      {/* Billing Address */}
      <div className="space-y-4">
        <h3 className="text-lg font-semibold">Billing Address</h3>

        <Input
          label="Street"
          {...register('billingAddress.street')}
          error={errors.billingAddress?.street?.message}
          required
        />

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Input
            label="City"
            {...register('billingAddress.city')}
            error={errors.billingAddress?.city?.message}
            required
          />

          <Input
            label="State"
            {...register('billingAddress.state')}
            error={errors.billingAddress?.state?.message}
            required
          />

          <Input
            label="Postal Code"
            {...register('billingAddress.postalCode')}
            error={errors.billingAddress?.postalCode?.message}
            required
          />
        </div>

        <Input
          label="Country"
          {...register('billingAddress.country')}
          error={errors.billingAddress?.country?.message}
          required
        />
      </div>

      {/* Shipping Address */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold">Shipping Address</h3>
          <label className="flex items-center space-x-2">
            <input
              type="checkbox"
              checked={useSameAddress}
              onChange={(e) => setUseSameAddress(e.target.checked)}
              className="rounded"
            />
            <span className="text-sm text-gray-600">Same as billing</span>
          </label>
        </div>

        {!useSameAddress && (
          <>
            <Input
              label="Street"
              {...register('shippingAddress.street')}
              error={errors.shippingAddress?.street?.message}
            />

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <Input
                label="City"
                {...register('shippingAddress.city')}
                error={errors.shippingAddress?.city?.message}
              />

              <Input
                label="State"
                {...register('shippingAddress.state')}
                error={errors.shippingAddress?.state?.message}
              />

              <Input
                label="Postal Code"
                {...register('shippingAddress.postalCode')}
                error={errors.shippingAddress?.postalCode?.message}
              />
            </div>

            <Input
              label="Country"
              {...register('shippingAddress.country')}
              error={errors.shippingAddress?.country?.message}
            />
          </>
        )}
      </div>

      {/* Form Actions */}
      <div className="flex justify-end space-x-4">
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" isLoading={isLoading}>
          {customer ? 'Update Customer' : 'Create Customer'}
        </Button>
      </div>
    </form>
  );
};
