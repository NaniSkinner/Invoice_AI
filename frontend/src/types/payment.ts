export interface PaymentDto {
  id: string;
  invoiceId: string;
  invoiceNumber: string;
  paymentAmount: number;
  paymentDate: string;
  paymentMethod: 'CREDIT_CARD' | 'BANK_TRANSFER' | 'CHECK' | 'CASH' | 'OTHER';
  transactionReference?: string;
  notes?: string;
  createdAt: string;
}
