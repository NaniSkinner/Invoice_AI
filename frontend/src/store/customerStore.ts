import { create } from 'zustand';
import { CustomerDto } from '@/types/customer';

interface CustomerState {
  customers: CustomerDto[];
  selectedCustomer: CustomerDto | null;
  setCustomers: (customers: CustomerDto[]) => void;
  setSelectedCustomer: (customer: CustomerDto | null) => void;
  addCustomer: (customer: CustomerDto) => void;
  updateCustomer: (id: string, customer: CustomerDto) => void;
  removeCustomer: (id: string) => void;
}

export const useCustomerStore = create<CustomerState>((set) => ({
  customers: [],
  selectedCustomer: null,

  setCustomers: (customers) => set({ customers }),

  setSelectedCustomer: (customer) => set({ selectedCustomer: customer }),

  addCustomer: (customer) =>
    set((state) => ({ customers: [...state.customers, customer] })),

  updateCustomer: (id, customer) =>
    set((state) => ({
      customers: state.customers.map((c) => (c.id === id ? customer : c)),
      selectedCustomer: state.selectedCustomer?.id === id ? customer : state.selectedCustomer,
    })),

  removeCustomer: (id) =>
    set((state) => ({
      customers: state.customers.filter((c) => c.id !== id),
      selectedCustomer: state.selectedCustomer?.id === id ? null : state.selectedCustomer,
    })),
}));
