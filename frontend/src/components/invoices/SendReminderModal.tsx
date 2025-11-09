'use client';

import React, { useState, useEffect } from 'react';
import { Modal } from '@/components/ui/Modal';
import { Select } from '@/components/ui/Select';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { previewReminder } from '@/lib/api/reminders';
import { ReminderType, ReminderPreviewDto } from '@/types/reminder';

interface SendReminderModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (reminderType: ReminderType) => void;
  invoiceId: string;
  isLoading?: boolean;
}

export const SendReminderModal: React.FC<SendReminderModalProps> = ({
  isOpen,
  onClose,
  onSubmit,
  invoiceId,
  isLoading = false,
}) => {
  const [reminderType, setReminderType] = useState<ReminderType>('ON_DUE_DATE');
  const [preview, setPreview] = useState<ReminderPreviewDto | null>(null);
  const [isLoadingPreview, setIsLoadingPreview] = useState(false);

  useEffect(() => {
    if (isOpen) {
      loadPreview();
    }
  }, [isOpen, reminderType]);

  const loadPreview = async () => {
    try {
      setIsLoadingPreview(true);
      const data = await previewReminder(invoiceId, reminderType);
      setPreview(data);
    } catch (error) {
      console.error('Error loading preview:', error);
    } finally {
      setIsLoadingPreview(false);
    }
  };

  const handleSubmit = () => {
    onSubmit(reminderType);
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
      title="Send Reminder"
      size="lg"
      footer={
        <div className="flex justify-end space-x-3">
          <Button variant="outline" onClick={onClose} disabled={isLoading}>
            Cancel
          </Button>
          <Button onClick={handleSubmit} isLoading={isLoading}>
            Send Reminder
          </Button>
        </div>
      }
    >
      <div className="space-y-4">
        <Select
          label="Reminder Type"
          value={reminderType}
          onChange={(e) => setReminderType(e.target.value as ReminderType)}
          options={reminderTypeOptions}
        />

        {isLoadingPreview ? (
          <div className="text-center py-8 text-gray-500">Loading preview...</div>
        ) : preview ? (
          <Card title="Email Preview">
            <div className="space-y-3">
              <div>
                <span className="text-sm font-medium text-gray-600">To:</span>
                <p className="text-gray-900">{preview.recipientEmail}</p>
              </div>
              <div>
                <span className="text-sm font-medium text-gray-600">Subject:</span>
                <p className="text-gray-900">{preview.subject}</p>
              </div>
              <div>
                <span className="text-sm font-medium text-gray-600">Message:</span>
                <div className="mt-2 p-4 bg-gray-50 rounded border border-gray-200 whitespace-pre-wrap">
                  {preview.message}
                </div>
              </div>
            </div>
          </Card>
        ) : null}
      </div>
    </Modal>
  );
};
