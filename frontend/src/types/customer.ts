export interface CustomerDto {
  id: string;
  businessName: string;
  contactName: string;
  email: string;
  phone?: string;
  billingAddress: AddressDto;
  shippingAddress?: AddressDto;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AddressDto {
  street: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
}
