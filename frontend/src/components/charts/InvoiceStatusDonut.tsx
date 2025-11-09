'use client';

import React from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';

interface InvoiceStatusDonutProps {
  data: Array<{ name: string; value: number; color: string }>;
}

export const InvoiceStatusDonut: React.FC<InvoiceStatusDonutProps> = ({ data }) => {
  const total = data.reduce((sum, item) => sum + item.value, 0);

  const renderCustomLabel = (entry: any) => {
    const percent = ((entry.value / total) * 100).toFixed(0);
    return `${percent}%`;
  };

  return (
    <ResponsiveContainer width="100%" height={300}>
      <PieChart>
        <Pie
          data={data}
          cx="50%"
          cy="50%"
          labelLine={false}
          label={renderCustomLabel}
          outerRadius={90}
          innerRadius={60}
          fill="#8884d8"
          dataKey="value"
          paddingAngle={2}
        >
          {data.map((entry, index) => (
            <Cell key={`cell-${index}`} fill={entry.color} />
          ))}
        </Pie>
        <Tooltip 
          formatter={(value: number) => `${value} invoices`}
          contentStyle={{
            backgroundColor: 'white',
            border: '1px solid #E8EEEB',
            borderRadius: '8px',
            padding: '8px 12px',
          }}
        />
        <Legend 
          verticalAlign="bottom" 
          height={36}
          iconType="circle"
          formatter={(value, entry: any) => {
            return `${value}: ${entry.payload.value}`;
          }}
        />
      </PieChart>
    </ResponsiveContainer>
  );
};

