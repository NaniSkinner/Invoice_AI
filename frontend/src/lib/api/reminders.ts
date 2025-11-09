import axiosInstance from './axios-instance';

export type ReminderType = 'BEFORE_DUE' | 'ON_DUE_DATE' | 'OVERDUE_7_DAYS' | 'OVERDUE_14_DAYS' | 'OVERDUE_30_DAYS';

export interface ReminderHistoryDto {
  id: string;
  invoiceId: string;
  invoiceNumber: string;
  reminderType: ReminderType;
  sentDate: string;
  recipientEmail: string;
  subject: string;
  message: string;
}

export interface SendReminderRequest {
  invoiceId: string;
  reminderType: ReminderType;
}

export interface ReminderPreviewDto {
  subject: string;
  message: string;
  recipientEmail: string;
  invoiceNumber: string;
}

export interface OverdueInvoiceDto {
  invoiceId: string;
  invoiceNumber: string;
  customerName: string;
  dueDate: string;
  totalAmount: number;
  balanceRemaining: number;
  daysOverdue: number;
}

/**
 * Send reminder for an invoice
 */
export const sendReminder = async (data: SendReminderRequest): Promise<ReminderHistoryDto> => {
  const response = await axiosInstance.post('/reminders/send', data);
  return response.data;
};

/**
 * Get reminder history for an invoice
 */
export const getReminderHistory = async (invoiceId: string): Promise<ReminderHistoryDto[]> => {
  const response = await axiosInstance.get(`/reminders/history/${invoiceId}`);
  return response.data;
};

/**
 * Get all overdue invoices
 */
export const getOverdueInvoices = async (): Promise<OverdueInvoiceDto[]> => {
  const response = await axiosInstance.get('/reminders/overdue');
  return response.data;
};

/**
 * Preview reminder before sending
 */
export const previewReminder = async (invoiceId: string, reminderType: ReminderType): Promise<ReminderPreviewDto> => {
  const response = await axiosInstance.get(`/reminders/preview/${invoiceId}?type=${reminderType}`);
  return response.data;
};
