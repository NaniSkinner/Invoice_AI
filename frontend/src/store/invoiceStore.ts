import { create } from 'zustand';
import { InvoiceDto } from '@/types/invoice';

interface InvoiceState {
  invoices: InvoiceDto[];
  selectedInvoice: InvoiceDto | null;
  setInvoices: (invoices: InvoiceDto[]) => void;
  setSelectedInvoice: (invoice: InvoiceDto | null) => void;
  addInvoice: (invoice: InvoiceDto) => void;
  updateInvoice: (id: string, invoice: InvoiceDto) => void;
  removeInvoice: (id: string) => void;
}

export const useInvoiceStore = create<InvoiceState>((set) => ({
  invoices: [],
  selectedInvoice: null,

  setInvoices: (invoices) => set({ invoices }),

  setSelectedInvoice: (invoice) => set({ selectedInvoice: invoice }),

  addInvoice: (invoice) =>
    set((state) => ({ invoices: [...state.invoices, invoice] })),

  updateInvoice: (id, invoice) =>
    set((state) => ({
      invoices: state.invoices.map((i) => (i.id === id ? invoice : i)),
      selectedInvoice: state.selectedInvoice?.id === id ? invoice : state.selectedInvoice,
    })),

  removeInvoice: (id) =>
    set((state) => ({
      invoices: state.invoices.filter((i) => i.id !== id),
      selectedInvoice: state.selectedInvoice?.id === id ? null : state.selectedInvoice,
    })),
}));
