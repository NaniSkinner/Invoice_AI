'use client';

import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Area, AreaChart } from 'recharts';

interface RevenueTrendChartProps {
  data: Array<{ date: string; revenue: number; expenses?: number }>;
}

export const RevenueTrendChart: React.FC<RevenueTrendChartProps> = ({ data }) => {
  const formatCurrency = (value: number) => {
    return `$${value.toLocaleString()}`;
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  };

  return (
    <ResponsiveContainer width="100%" height={300}>
      <AreaChart data={data} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
        <defs>
          <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
            <stop offset="5%" stopColor="#5A8F7B" stopOpacity={0.3}/>
            <stop offset="95%" stopColor="#5A8F7B" stopOpacity={0}/>
          </linearGradient>
          <linearGradient id="colorExpenses" x1="0" y1="0" x2="0" y2="1">
            <stop offset="5%" stopColor="#EF4444" stopOpacity={0.3}/>
            <stop offset="95%" stopColor="#EF4444" stopOpacity={0}/>
          </linearGradient>
        </defs>
        <CartesianGrid strokeDasharray="3 3" stroke="#E8EEEB" vertical={false} />
        <XAxis 
          dataKey="date" 
          tickFormatter={formatDate}
          stroke="#9CA3AF"
          style={{ fontSize: '12px' }}
          tickLine={false}
        />
        <YAxis 
          tickFormatter={formatCurrency}
          stroke="#9CA3AF"
          style={{ fontSize: '12px' }}
          tickLine={false}
          axisLine={false}
        />
        <Tooltip 
          formatter={(value: number) => formatCurrency(value)}
          contentStyle={{
            backgroundColor: 'white',
            border: '1px solid #E8EEEB',
            borderRadius: '8px',
            padding: '8px 12px',
          }}
          labelFormatter={formatDate}
        />
        <Area
          type="monotone"
          dataKey="revenue"
          stroke="#5A8F7B"
          strokeWidth={2}
          fill="url(#colorRevenue)"
          name="Income"
        />
        {data.some(d => d.expenses !== undefined) && (
          <Area
            type="monotone"
            dataKey="expenses"
            stroke="#EF4444"
            strokeWidth={2}
            fill="url(#colorExpenses)"
            name="Outcome"
          />
        )}
      </AreaChart>
    </ResponsiveContainer>
  );
};

