'use client';

import React, { useState } from 'react';
import { Modal } from '@/components/ui/Modal';
import { Select } from '@/components/ui/Select';
import { Button } from '@/components/ui/Button';
import { ReminderType } from '@/types/reminder';

interface SendReminderModalProps {
  isOpen: boolean;
  onClose: () => void;
  onPreview: (reminderType: ReminderType) => void;
  isLoading?: boolean;
}

export const SendReminderModal: React.FC<SendReminderModalProps> = ({
  isOpen,
  onClose,
  onPreview,
  isLoading = false,
}) => {
  const [reminderType, setReminderType] = useState<ReminderType>('ON_DUE_DATE');

  const handlePreview = () => {
    onPreview(reminderType);
  };

  const reminderTypeOptions = [
    { value: 'BEFORE_DUE', label: 'Before Due Date' },
    { value: 'ON_DUE_DATE', label: 'On Due Date' },
    { value: 'OVERDUE_7_DAYS', label: '7 Days Overdue' },
    { value: 'OVERDUE_14_DAYS', label: '14 Days Overdue' },
    { value: 'OVERDUE_30_DAYS', label: '30 Days Overdue' },
  ];

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Send Payment Reminder"
      size="md"
      footer={
        <div className="flex justify-end space-x-3">
          <Button variant="outline" onClick={onClose} disabled={isLoading}>
            Cancel
          </Button>
          <Button onClick={handlePreview} isLoading={isLoading}>
            Preview Email
          </Button>
        </div>
      }
    >
      <div className="space-y-4">
        <p className="text-gray-700">
          Select the type of reminder you want to send to the customer. You'll be able to preview the email before sending.
        </p>
        <Select
          label="Reminder Type"
          value={reminderType}
          onChange={(e) => setReminderType(e.target.value as ReminderType)}
          options={reminderTypeOptions}
        />
      </div>
    </Modal>
  );
};
