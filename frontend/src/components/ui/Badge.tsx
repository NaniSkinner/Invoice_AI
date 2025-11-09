import React from 'react';

interface BadgeProps {
  children: React.ReactNode;
  variant?: 'default' | 'success' | 'warning' | 'danger' | 'info';
  className?: string;
}

export const Badge: React.FC<BadgeProps> = ({ children, variant = 'default', className = '' }) => {
  const variantStyles = {
    default: 'bg-slate-100 text-slate-700',
    success: 'bg-primary-50 text-primary-700 border border-primary-200',
    warning: 'bg-amber-50 text-amber-700 border border-amber-200',
    danger: 'bg-red-50 text-red-700 border border-red-200',
    info: 'bg-slate-100 text-slate-700 border border-slate-200',
  };

  return (
    <span
      className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold ${variantStyles[variant]} ${className}`}
    >
      {children}
    </span>
  );
};

export const getInvoiceStatusBadgeVariant = (status: string): 'default' | 'success' | 'warning' | 'danger' | 'info' => {
  switch (status) {
    case 'PAID':
      return 'success';
    case 'SENT':
      return 'info';
    case 'DRAFT':
      return 'default';
    case 'CANCELLED':
      return 'danger';
    default:
      return 'default';
  }
};
