import axiosInstance from './axios-instance';
import { PaymentDto } from '@/types/payment';

export interface RecordPaymentRequest {
  invoiceId: string;
  paymentAmount: number;
  paymentDate: string;
  paymentMethod: 'CREDIT_CARD' | 'BANK_TRANSFER' | 'CHECK' | 'CASH' | 'OTHER';
  transactionReference?: string;
  notes?: string;
}

/**
 * Get all payments
 */
export const getAllPayments = async (): Promise<PaymentDto[]> => {
  const response = await axiosInstance.get('/payments');
  return response.data;
};

/**
 * Get payment by ID
 */
export const getPaymentById = async (id: string): Promise<PaymentDto> => {
  const response = await axiosInstance.get(`/payments/${id}`);
  return response.data;
};

/**
 * Record a new payment
 */
export const recordPayment = async (data: RecordPaymentRequest): Promise<PaymentDto> => {
  const response = await axiosInstance.post('/payments', data);
  return response.data;
};

/**
 * Get payments for a specific invoice
 */
export const getPaymentsByInvoice = async (invoiceId: string): Promise<PaymentDto[]> => {
  const response = await axiosInstance.get(`/payments/invoice/${invoiceId}`);
  return response.data;
};

/**
 * Delete payment
 */
export const deletePayment = async (id: string): Promise<void> => {
  await axiosInstance.delete(`/payments/${id}`);
};
