import axiosInstance from './axios-instance';
import { CustomerDto, AddressDto } from '@/types/customer';

export interface CreateCustomerRequest {
  businessName: string;
  contactName: string;
  email: string;
  phone?: string;
  billingAddress: AddressDto;
  shippingAddress?: AddressDto;
}

export interface UpdateCustomerRequest extends CreateCustomerRequest {
  active: boolean;
}

/**
 * Get all customers
 */
export const getAllCustomers = async (): Promise<CustomerDto[]> => {
  const response = await axiosInstance.get('/customers');
  return response.data;
};

/**
 * Get customer by ID
 */
export const getCustomerById = async (id: string): Promise<CustomerDto> => {
  const response = await axiosInstance.get(`/customers/${id}`);
  return response.data;
};

/**
 * Create new customer
 */
export const createCustomer = async (data: CreateCustomerRequest): Promise<CustomerDto> => {
  const response = await axiosInstance.post('/customers', data);
  return response.data;
};

/**
 * Update customer
 */
export const updateCustomer = async (id: string, data: UpdateCustomerRequest): Promise<CustomerDto> => {
  const response = await axiosInstance.put(`/customers/${id}`, data);
  return response.data;
};

/**
 * Delete customer
 */
export const deleteCustomer = async (id: string): Promise<void> => {
  await axiosInstance.delete(`/customers/${id}`);
};

/**
 * Search customers by query
 */
export const searchCustomers = async (query: string): Promise<CustomerDto[]> => {
  const response = await axiosInstance.get(`/customers/search?q=${encodeURIComponent(query)}`);
  return response.data;
};
