export interface InvoiceDto {
  id: string;
  invoiceNumber: string;
  customerId: string;
  customerName: string;
  customerEmail: string;
  issueDate: string;
  dueDate: string;
  status: 'DRAFT' | 'SENT' | 'PAID' | 'CANCELLED';
  subtotal: number;
  taxAmount: number;
  totalAmount: number;
  amountPaid: number;
  balanceRemaining: number;
  allowsPartialPayment: boolean;
  paymentLink?: string;
  notes?: string;
  terms?: string;
  lineItems: LineItemDto[];
}

export interface LineItemDto {
  id: string;
  description: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}
