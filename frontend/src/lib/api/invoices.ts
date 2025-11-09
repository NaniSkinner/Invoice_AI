import axiosInstance from './axios-instance';
import { InvoiceDto, LineItemDto } from '@/types/invoice';

export interface CreateInvoiceRequest {
  customerId: string;
  issueDate: string;
  dueDate: string;
  allowsPartialPayment: boolean;
  notes?: string;
  terms?: string;
  lineItems: Omit<LineItemDto, 'id' | 'lineTotal'>[];
}

export interface UpdateInvoiceRequest extends CreateInvoiceRequest {
  status: 'DRAFT' | 'SENT' | 'PAID' | 'CANCELLED';
}

export interface InvoiceFilters {
  status?: 'DRAFT' | 'SENT' | 'PAID' | 'CANCELLED';
  customerId?: string;
  fromDate?: string;
  toDate?: string;
}

/**
 * Get all invoices
 */
export const getAllInvoices = async (): Promise<InvoiceDto[]> => {
  const response = await axiosInstance.get('/invoices');
  return response.data;
};

/**
 * Get invoices with filters
 */
export const getInvoices = async (filters?: InvoiceFilters): Promise<InvoiceDto[]> => {
  const params = new URLSearchParams();
  if (filters?.status) params.append('status', filters.status);
  if (filters?.customerId) params.append('customerId', filters.customerId);
  if (filters?.fromDate) params.append('fromDate', filters.fromDate);
  if (filters?.toDate) params.append('toDate', filters.toDate);

  const queryString = params.toString();
  const response = await axiosInstance.get(`/invoices${queryString ? `?${queryString}` : ''}`);
  return response.data;
};

/**
 * Get invoice by ID
 */
export const getInvoiceById = async (id: string): Promise<InvoiceDto> => {
  const response = await axiosInstance.get(`/invoices/${id}`);
  return response.data;
};

/**
 * Create new invoice
 */
export const createInvoice = async (data: CreateInvoiceRequest): Promise<InvoiceDto> => {
  const response = await axiosInstance.post('/invoices', data);
  return response.data;
};

/**
 * Update invoice (only for DRAFT status)
 */
export const updateInvoice = async (id: string, data: UpdateInvoiceRequest): Promise<InvoiceDto> => {
  const response = await axiosInstance.put(`/invoices/${id}`, data);
  return response.data;
};

/**
 * Delete invoice
 */
export const deleteInvoice = async (id: string): Promise<void> => {
  await axiosInstance.delete(`/invoices/${id}`);
};

/**
 * Send invoice (changes status to SENT)
 */
export const sendInvoice = async (id: string): Promise<InvoiceDto> => {
  const response = await axiosInstance.post(`/invoices/${id}/send`);
  return response.data;
};

/**
 * Mark invoice as paid
 */
export const markInvoiceAsPaid = async (id: string): Promise<InvoiceDto> => {
  const response = await axiosInstance.post(`/invoices/${id}/mark-paid`);
  return response.data;
};

/**
 * Cancel invoice
 */
export const cancelInvoice = async (id: string, cancellationReason: string): Promise<InvoiceDto> => {
  const response = await axiosInstance.post(`/invoices/${id}/cancel`, {
    cancellationReason
  });
  return response.data;
};

/**
 * Get invoices by customer
 */
export const getInvoicesByCustomer = async (customerId: string): Promise<InvoiceDto[]> => {
  const response = await axiosInstance.get(`/invoices/customer/${customerId}`);
  return response.data;
};

/**
 * Get invoice by payment link
 */
export const getInvoiceByPaymentLink = async (link: string): Promise<InvoiceDto> => {
  const response = await axiosInstance.get(`/invoices/payment-link/${link}`);
  return response.data;
};
