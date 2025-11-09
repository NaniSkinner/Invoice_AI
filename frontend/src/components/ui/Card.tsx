import React from 'react';

interface CardProps {
  children: React.ReactNode;
  className?: string;
  title?: string;
  action?: React.ReactNode;
}

export const Card: React.FC<CardProps> = ({ children, className = '', title, action }) => {
  return (
    <div className={`bg-white rounded-card shadow-card hover:shadow-card-hover transition-shadow duration-200 ${className}`}>
      {(title || action) && (
        <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
          {title && <h2 className="text-xl font-semibold text-gray-800">{title}</h2>}
          {action && <div>{action}</div>}
        </div>
      )}
      <div className="p-6">{children}</div>
    </div>
  );
};
