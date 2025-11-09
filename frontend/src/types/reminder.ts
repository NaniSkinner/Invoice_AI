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

export interface ReminderPreviewDto {
  subject: string;
  message: string;
  recipientEmail: string;
  invoiceNumber: string;
}

export interface OverdueInvoiceDto {
  id?: string; // For Table component compatibility
  invoiceId: string;
  invoiceNumber: string;
  customerName: string;
  dueDate: string;
  totalAmount: number;
  balanceRemaining: number;
  daysOverdue: number;
}
