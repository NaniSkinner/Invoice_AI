import { format, parseISO, formatDistanceToNow } from 'date-fns';

/**
 * Format currency value
 */
export const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(amount);
};

/**
 * Format date to readable string
 */
export const formatDate = (date: string | Date): string => {
  const dateObj = typeof date === 'string' ? parseISO(date) : date;
  return format(dateObj, 'MMM dd, yyyy');
};

/**
 * Format date with time
 */
export const formatDateTime = (date: string | Date): string => {
  const dateObj = typeof date === 'string' ? parseISO(date) : date;
  return format(dateObj, 'MMM dd, yyyy hh:mm a');
};

/**
 * Format date for input fields (YYYY-MM-DD)
 */
export const formatDateForInput = (date: string | Date): string => {
  const dateObj = typeof date === 'string' ? parseISO(date) : date;
  return format(dateObj, 'yyyy-MM-dd');
};

/**
 * Format relative time (e.g., "2 days ago")
 */
export const formatRelativeTime = (date: string | Date): string => {
  const dateObj = typeof date === 'string' ? parseISO(date) : date;
  return formatDistanceToNow(dateObj, { addSuffix: true });
};

/**
 * Calculate days until/overdue
 */
export const calculateDaysUntil = (dueDate: string): number => {
  const due = parseISO(dueDate);
  const today = new Date();
  const diffTime = due.getTime() - today.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  return diffDays;
};

/**
 * Format invoice status for display
 */
export const formatInvoiceStatus = (status: string): string => {
  switch (status) {
    case 'DRAFT':
      return 'Draft';
    case 'SENT':
      return 'Sent';
    case 'PAID':
      return 'Paid';
    case 'CANCELLED':
      return 'Cancelled';
    default:
      return status;
  }
};

/**
 * Format payment method for display
 */
export const formatPaymentMethod = (method: string): string => {
  switch (method) {
    case 'CREDIT_CARD':
      return 'Credit Card';
    case 'BANK_TRANSFER':
      return 'Bank Transfer';
    case 'CHECK':
      return 'Check';
    case 'CASH':
      return 'Cash';
    case 'OTHER':
      return 'Other';
    default:
      return method;
  }
};

/**
 * Format reminder type for display
 */
export const formatReminderType = (type: string): string => {
  switch (type) {
    case 'BEFORE_DUE':
      return 'Before Due Date';
    case 'ON_DUE_DATE':
      return 'On Due Date';
    case 'OVERDUE_7_DAYS':
      return '7 Days Overdue';
    case 'OVERDUE_14_DAYS':
      return '14 Days Overdue';
    case 'OVERDUE_30_DAYS':
      return '30 Days Overdue';
    default:
      return type;
  }
};

/**
 * Format percentage
 */
export const formatPercentage = (value: number): string => {
  return `${value.toFixed(2)}%`;
};
