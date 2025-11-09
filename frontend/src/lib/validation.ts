import { z } from 'zod';

/**
 * Address validation schema
 */
export const addressSchema = z.object({
  street: z.string().min(1, 'Street is required'),
  city: z.string().min(1, 'City is required'),
  state: z.string().min(1, 'State is required'),
  postalCode: z.string().min(1, 'Postal code is required'),
  country: z.string().min(1, 'Country is required'),
});

/**
 * Customer validation schema
 */
export const customerSchema = z.object({
  businessName: z.string().min(1, 'Business name is required'),
  contactName: z.string().min(1, 'Contact name is required'),
  email: z.string().email('Invalid email address'),
  phone: z.string().optional(),
  billingAddress: addressSchema,
  shippingAddress: addressSchema.optional(),
  active: z.boolean().optional().default(true),
});

/**
 * Line item validation schema
 */
export const lineItemSchema = z.object({
  description: z.string().min(1, 'Description is required'),
  quantity: z.number().min(1, 'Quantity must be at least 1'),
  unitPrice: z.number().min(0, 'Unit price must be positive'),
});

/**
 * Invoice validation schema
 */
export const invoiceSchema = z.object({
  customerId: z.string().min(1, 'Customer is required'),
  issueDate: z.string().min(1, 'Issue date is required'),
  dueDate: z.string().min(1, 'Due date is required'),
  allowsPartialPayment: z.boolean().default(false),
  notes: z.string().optional(),
  terms: z.string().optional(),
  lineItems: z.array(lineItemSchema).min(1, 'At least one line item is required'),
});

/**
 * Payment validation schema
 */
export const paymentSchema = z.object({
  invoiceId: z.string().min(1, 'Invoice is required'),
  paymentAmount: z.number().min(0.01, 'Payment amount must be greater than 0'),
  paymentDate: z.string().min(1, 'Payment date is required'),
  paymentMethod: z.enum(['CREDIT_CARD', 'BANK_TRANSFER', 'CHECK', 'CASH', 'OTHER']),
  transactionReference: z.string().optional(),
  notes: z.string().optional(),
});

/**
 * Login validation schema
 */
export const loginSchema = z.object({
  username: z.string().min(1, 'Username is required'),
  password: z.string().min(1, 'Password is required'),
});

/**
 * Reminder validation schema
 */
export const reminderSchema = z.object({
  invoiceId: z.string().min(1, 'Invoice is required'),
  reminderType: z.enum(['BEFORE_DUE', 'ON_DUE_DATE', 'OVERDUE_7_DAYS', 'OVERDUE_14_DAYS', 'OVERDUE_30_DAYS']),
});
