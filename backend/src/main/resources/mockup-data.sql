-- InvoiceMe Mockup Data
-- This script creates realistic demo data for testing
-- Run after initial Flyway migrations

-- Clear existing data (optional - comment out if you want to keep existing data)
-- DELETE FROM payments;
-- DELETE FROM invoice_line_items;
-- DELETE FROM reminder_emails;
-- DELETE FROM invoices;
-- DELETE FROM customers;

-- ========================================
-- CUSTOMERS (10 diverse business customers)
-- ========================================

INSERT INTO customers (id, business_name, contact_name, email, phone, billing_street, billing_city, billing_state, billing_postal_code, billing_country, shipping_street, shipping_city, shipping_state, shipping_postal_code, shipping_country, active, created_at, updated_at) VALUES

-- Tech Companies
('11111111-1111-1111-1111-111111111111', 'Acme Corporation', 'John Smith', 'john.smith@acmecorp.com', '+1-555-0101', '123 Innovation Drive', 'San Francisco', 'CA', '94105', 'USA', '123 Innovation Drive', 'San Francisco', 'CA', '94105', 'USA', true, NOW() - INTERVAL '90 days', NOW() - INTERVAL '90 days'),

('22222222-2222-2222-2222-222222222222', 'TechStart Solutions', 'Jane Doe', 'jane.doe@techstart.io', '+1-555-0102', '456 Startup Lane', 'Austin', 'TX', '78701', 'USA', NULL, NULL, NULL, NULL, NULL, true, NOW() - INTERVAL '75 days', NOW() - INTERVAL '75 days'),

('33333333-3333-3333-3333-333333333333', 'Global Dynamics Inc', 'Robert Johnson', 'r.johnson@globaldynamics.com', '+1-555-0103', '789 Enterprise Blvd', 'New York', 'NY', '10001', 'USA', '789 Enterprise Blvd', 'New York', 'NY', '10001', 'USA', true, NOW() - INTERVAL '60 days', NOW() - INTERVAL '60 days'),

-- Consulting & Services
('44444444-4444-4444-4444-444444444444', 'Blue Ocean Consulting', 'Sarah Williams', 'sarah@blueocean.com', '+1-555-0104', '321 Strategy Court', 'Chicago', 'IL', '60601', 'USA', NULL, NULL, NULL, NULL, NULL, true, NOW() - INTERVAL '50 days', NOW() - INTERVAL '50 days'),

('55555555-5555-5555-5555-555555555555', 'Peak Performance LLC', 'Michael Brown', 'mbrown@peakperf.com', '+1-555-0105', '654 Excellence Way', 'Seattle', 'WA', '98101', 'USA', '654 Excellence Way', 'Seattle', 'WA', '98101', 'USA', true, NOW() - INTERVAL '45 days', NOW() - INTERVAL '45 days'),

-- Manufacturing & Retail
('66666666-6666-6666-6666-666666666666', 'Summit Manufacturing', 'Emily Davis', 'e.davis@summitmfg.com', '+1-555-0106', '987 Industrial Pkwy', 'Detroit', 'MI', '48201', 'USA', '100 Warehouse Rd', 'Detroit', 'MI', '48202', 'USA', true, NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days'),

('77777777-7777-7777-7777-777777777777', 'Metro Retailers Group', 'David Martinez', 'david.m@metroretail.com', '+1-555-0107', '147 Commerce St', 'Los Angeles', 'CA', '90001', 'USA', NULL, NULL, NULL, NULL, NULL, true, NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days'),

-- Healthcare & Education
('88888888-8888-8888-8888-888888888888', 'HealthFirst Medical', 'Dr. Lisa Anderson', 'l.anderson@healthfirst.com', '+1-555-0108', '258 Wellness Ave', 'Boston', 'MA', '02101', 'USA', '258 Wellness Ave', 'Boston', 'MA', '02101', 'USA', true, NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days'),

('99999999-9999-9999-9999-999999999999', 'Bright Future Academy', 'Thomas Wilson', 't.wilson@brightfuture.edu', '+1-555-0109', '369 Learning Lane', 'Portland', 'OR', '97201', 'USA', NULL, NULL, NULL, NULL, NULL, true, NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days'),

-- Small Business
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Sunset Cafe & Bakery', 'Maria Garcia', 'maria@sunsetcafe.com', '+1-555-0110', '741 Foodie Street', 'Miami', 'FL', '33101', 'USA', '741 Foodie Street', 'Miami', 'FL', '33101', 'USA', true, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days');

-- ========================================
-- INVOICES (25 invoices across all statuses)
-- ========================================

-- DRAFT Invoices (5) - Recently created, not sent yet
INSERT INTO invoices (id, invoice_number, customer_id, issue_date, due_date, status, subtotal, tax_amount, total_amount, amount_paid, balance_remaining, notes, payment_terms, allows_partial_payment, payment_link, created_at, updated_at) VALUES

('d0000001-0000-0000-0000-000000000001', 'INV-202501-0001', '11111111-1111-1111-1111-111111111111', CURRENT_DATE, CURRENT_DATE + INTERVAL '30 days', 'DRAFT', 5000.00, 500.00, 5500.00, 0.00, 5500.00, 'Q1 consulting services', 'Net 30', true, NULL, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),

('d0000002-0000-0000-0000-000000000002', '202501-0002', '22222222-2222-2222-2222-222222222222', CURRENT_DATE, CURRENT_DATE + INTERVAL '45 days', 'DRAFT', 3200.00, 320.00, 3520.00, 0.00, 3520.00, 'Website development - Phase 1', 'Net 45', false, NULL, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),

('d0000003-0000-0000-0000-000000000003', 'INV-202501-0003', '33333333-3333-3333-3333-333333333333', CURRENT_DATE, CURRENT_DATE + INTERVAL '15 days', 'DRAFT', 12500.00, 1250.00, 13750.00, 0.00, 13750.00, 'Enterprise software license', 'Net 15', false, NULL, NOW(), NOW()),

('d0000004-0000-0000-0000-000000000004', 'INV-202501-0004', '44444444-4444-4444-4444-444444444444', CURRENT_DATE, CURRENT_DATE + INTERVAL '30 days', 'DRAFT', 8000.00, 800.00, 8800.00, 0.00, 8800.00, 'Strategy consulting services', 'Net 30', true, NULL, NOW(), NOW()),

('d0000005-0000-0000-0000-000000000005', 'INV-202501-0005', '55555555-5555-5555-5555-555555555555', CURRENT_DATE, CURRENT_DATE + INTERVAL '30 days', 'DRAFT', 2500.00, 250.00, 2750.00, 0.00, 2750.00, 'Training workshop materials', 'Net 30', true, NULL, NOW(), NOW());

-- SENT Invoices (8) - Active invoices, some near due date
INSERT INTO invoices (id, invoice_number, customer_id, issue_date, due_date, status, subtotal, tax_amount, total_amount, amount_paid, balance_remaining, notes, payment_terms, allows_partial_payment, payment_link, created_at, updated_at) VALUES

('s0000001-0000-0000-0000-000000000001', 'INV-202501-0006', '66666666-6666-6666-6666-666666666666', CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE + INTERVAL '20 days', 'SENT', 15000.00, 1500.00, 16500.00, 0.00, 16500.00, 'Manufacturing equipment', 'Net 30', false, 'payment-link-sent-001', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),

('s0000002-0000-0000-0000-000000000002', 'INV-202501-0007', '77777777-7777-7777-7777-777777777777', CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE + INTERVAL '25 days', 'SENT', 4500.00, 450.00, 4950.00, 0.00, 4950.00, 'Retail inventory management system', 'Net 30', true, 'payment-link-sent-002', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),

('s0000003-0000-0000-0000-000000000003', 'INV-202501-0008', '88888888-8888-8888-8888-888888888888', CURRENT_DATE - INTERVAL '15 days', CURRENT_DATE + INTERVAL '15 days', 'SENT', 7800.00, 780.00, 8580.00, 0.00, 8580.00, 'Medical software subscription', 'Net 30', false, 'payment-link-sent-003', NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days'),

('s0000004-0000-0000-0000-000000000004', 'INV-202501-0009', '99999999-9999-9999-9999-999999999999', CURRENT_DATE - INTERVAL '7 days', CURRENT_DATE + INTERVAL '23 days', 'SENT', 3600.00, 360.00, 3960.00, 0.00, 3960.00, 'Educational platform license', 'Net 30', true, 'payment-link-sent-004', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),

('s0000005-0000-0000-0000-000000000005', 'INV-202501-0010', '11111111-1111-1111-1111-111111111111', CURRENT_DATE - INTERVAL '20 days', CURRENT_DATE + INTERVAL '10 days', 'SENT', 9500.00, 950.00, 10450.00, 5000.00, 5450.00, 'Cloud infrastructure services - Partial payment received', 'Net 30', true, 'payment-link-sent-005', NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days'),

('s0000006-0000-0000-0000-000000000006', 'INV-202501-0011', '22222222-2222-2222-2222-222222222222', CURRENT_DATE - INTERVAL '3 days', CURRENT_DATE + INTERVAL '27 days', 'SENT', 2100.00, 210.00, 2310.00, 0.00, 2310.00, 'SEO optimization services', 'Net 30', false, 'payment-link-sent-006', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),

('s0000007-0000-0000-0000-000000000007', 'INV-202501-0012', '33333333-3333-3333-3333-333333333333', CURRENT_DATE - INTERVAL '12 days', CURRENT_DATE + INTERVAL '18 days', 'SENT', 18000.00, 1800.00, 19800.00, 0.00, 19800.00, 'Annual maintenance contract', 'Net 30', false, 'payment-link-sent-007', NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days'),

('s0000008-0000-0000-0000-000000000008', 'INV-202501-0013', '44444444-4444-4444-4444-444444444444', CURRENT_DATE - INTERVAL '25 days', CURRENT_DATE + INTERVAL '5 days', 'SENT', 6700.00, 670.00, 7370.00, 0.00, 7370.00, 'Market research report', 'Net 30', true, 'payment-link-sent-008', NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days');

-- OVERDUE Invoices (5) - Past due date, needs reminders
INSERT INTO invoices (id, invoice_number, customer_id, issue_date, due_date, status, subtotal, tax_amount, total_amount, amount_paid, balance_remaining, notes, payment_terms, allows_partial_payment, payment_link, created_at, updated_at) VALUES

('o0000001-0000-0000-0000-000000000001', 'INV-202412-0025', '55555555-5555-5555-5555-555555555555', CURRENT_DATE - INTERVAL '40 days', CURRENT_DATE - INTERVAL '5 days', 'SENT', 5200.00, 520.00, 5720.00, 0.00, 5720.00, 'Leadership training program - 5 days overdue', 'Net 30', true, 'payment-link-overdue-001', NOW() - INTERVAL '40 days', NOW() - INTERVAL '40 days'),

('o0000002-0000-0000-0000-000000000002', 'INV-202412-0026', '66666666-6666-6666-6666-666666666666', CURRENT_DATE - INTERVAL '50 days', CURRENT_DATE - INTERVAL '10 days', 'SENT', 11000.00, 1100.00, 12100.00, 0.00, 12100.00, 'Equipment parts - 10 days overdue', 'Net 30', false, 'payment-link-overdue-002', NOW() - INTERVAL '50 days', NOW() - INTERVAL '50 days'),

('o0000003-0000-0000-0000-000000000003', 'INV-202412-0027', '77777777-7777-7777-7777-777777777777', CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE - INTERVAL '30 days', 'SENT', 8900.00, 890.00, 9790.00, 0.00, 9790.00, 'POS system upgrade - 30 days overdue', 'Net 30', true, 'payment-link-overdue-003', NOW() - INTERVAL '60 days', NOW() - INTERVAL '60 days'),

('o0000004-0000-0000-0000-000000000004', 'INV-202411-0028', '88888888-8888-8888-8888-888888888888', CURRENT_DATE - INTERVAL '75 days', CURRENT_DATE - INTERVAL '15 days', 'SENT', 4200.00, 420.00, 4620.00, 0.00, 4620.00, 'Telehealth integration - 15 days overdue', 'Net 30', false, 'payment-link-overdue-004', NOW() - INTERVAL '75 days', NOW() - INTERVAL '75 days'),

('o0000005-0000-0000-0000-000000000005', 'INV-202411-0029', '99999999-9999-9999-9999-999999999999', CURRENT_DATE - INTERVAL '37 days', CURRENT_DATE - INTERVAL '7 days', 'SENT', 2900.00, 290.00, 3190.00, 0.00, 3190.00, 'Student management system - 7 days overdue', 'Net 30', true, 'payment-link-overdue-005', NOW() - INTERVAL '37 days', NOW() - INTERVAL '37 days');

-- PAID Invoices (5) - Fully paid with payment history
INSERT INTO invoices (id, invoice_number, customer_id, issue_date, due_date, status, subtotal, tax_amount, total_amount, amount_paid, balance_remaining, notes, payment_terms, allows_partial_payment, payment_link, created_at, updated_at) VALUES

('p0000001-0000-0000-0000-000000000001', 'INV-202412-0014', '11111111-1111-1111-1111-111111111111', CURRENT_DATE - INTERVAL '45 days', CURRENT_DATE - INTERVAL '15 days', 'PAID', 7500.00, 750.00, 8250.00, 8250.00, 0.00, 'December consulting - Paid in full', 'Net 30', false, 'payment-link-paid-001', NOW() - INTERVAL '45 days', NOW() - INTERVAL '16 days'),

('p0000002-0000-0000-0000-000000000002', 'INV-202412-0015', '22222222-2222-2222-2222-222222222222', CURRENT_DATE - INTERVAL '35 days', CURRENT_DATE - INTERVAL '5 days', 'PAID', 4100.00, 410.00, 4510.00, 4510.00, 0.00, 'Mobile app development - Paid', 'Net 30', true, 'payment-link-paid-002', NOW() - INTERVAL '35 days', NOW() - INTERVAL '6 days'),

('p0000003-0000-0000-0000-000000000003', 'INV-202412-0016', '33333333-3333-3333-3333-333333333333', CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE - INTERVAL '30 days', 'PAID', 22000.00, 2200.00, 24200.00, 24200.00, 0.00, 'Q4 enterprise contract - Paid', 'Net 30', false, 'payment-link-paid-003', NOW() - INTERVAL '60 days', NOW() - INTERVAL '32 days'),

('p0000004-0000-0000-0000-000000000004', 'INV-202412-0017', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', CURRENT_DATE - INTERVAL '25 days', CURRENT_DATE + INTERVAL '5 days', 'PAID', 1200.00, 120.00, 1320.00, 1320.00, 0.00, 'POS software - Early payment', 'Net 30', false, 'payment-link-paid-004', NOW() - INTERVAL '25 days', NOW() - INTERVAL '10 days'),

('p0000005-0000-0000-0000-000000000005', 'INV-202412-0018', '44444444-4444-4444-4444-444444444444', CURRENT_DATE - INTERVAL '50 days', CURRENT_DATE - INTERVAL '20 days', 'PAID', 5600.00, 560.00, 6160.00, 6160.00, 0.00, 'Business analysis - Paid via installments', 'Net 30', true, 'payment-link-paid-005', NOW() - INTERVAL '50 days', NOW() - INTERVAL '22 days');

-- CANCELLED Invoices (2)
INSERT INTO invoices (id, invoice_number, customer_id, issue_date, due_date, status, subtotal, tax_amount, total_amount, amount_paid, balance_remaining, notes, payment_terms, allows_partial_payment, payment_link, cancellation_reason, created_at, updated_at) VALUES

('c0000001-0000-0000-0000-000000000001', 'INV-202412-0019', '55555555-5555-5555-5555-555555555555', CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE, 'CANCELLED', 3300.00, 330.00, 3630.00, 0.00, 3630.00, 'Cancelled - Duplicate invoice', 'Net 30', false, NULL, 'Duplicate invoice created by mistake', NOW() - INTERVAL '30 days', NOW() - INTERVAL '28 days'),

('c0000002-0000-0000-0000-000000000002', 'INV-202412-0020', '66666666-6666-6666-6666-666666666666', CURRENT_DATE - INTERVAL '20 days', CURRENT_DATE + INTERVAL '10 days', 'CANCELLED', 7200.00, 720.00, 7920.00, 0.00, 7920.00, 'Cancelled - Customer requested project cancellation', 'Net 30', true, NULL, 'Project cancelled by customer before delivery', NOW() - INTERVAL '20 days', NOW() - INTERVAL '18 days');

-- ========================================
-- LINE ITEMS for all invoices
-- ========================================

-- Line items for DRAFT invoices
INSERT INTO invoice_line_items (id, invoice_id, description, quantity, unit_price, line_total, line_order) VALUES
('l1000001-0000-0000-0000-000000000001', 'd0000001-0000-0000-0000-000000000001', 'Senior Consultant Hours', 40, 125.00, 5000.00, 1),
('l1000002-0000-0000-0000-000000000002', 'd0000002-0000-0000-0000-000000000002', 'Website Design & Development', 1, 2500.00, 2500.00, 1),
('l1000003-0000-0000-0000-000000000003', 'd0000002-0000-0000-0000-000000000002', 'Hosting Setup (1 year)', 1, 700.00, 700.00, 2),
('l1000004-0000-0000-0000-000000000004', 'd0000003-0000-0000-0000-000000000003', 'Enterprise Software License (Annual)', 1, 12500.00, 12500.00, 1),
('l1000005-0000-0000-0000-000000000005', 'd0000004-0000-0000-0000-000000000004', 'Strategy Consulting', 1, 8000.00, 8000.00, 1),
('l1000006-0000-0000-0000-000000000006', 'd0000005-0000-0000-0000-000000000005', 'Training Materials', 50, 50.00, 2500.00, 1);

-- Line items for SENT invoices (continuing pattern)
INSERT INTO invoice_line_items (id, invoice_id, description, quantity, unit_price, line_total, line_order) VALUES
('l2000001-0000-0000-0000-000000000001', 's0000001-0000-0000-0000-000000000001', 'Industrial Machine Parts', 10, 1500.00, 15000.00, 1),
('l2000002-0000-0000-0000-000000000002', 's0000002-0000-0000-0000-000000000002', 'POS System Software', 3, 1500.00, 4500.00, 1),
('l2000003-0000-0000-0000-000000000003', 's0000003-0000-0000-0000-000000000003', 'Medical Software (6-month subscription)', 1, 7800.00, 7800.00, 1),
('l2000004-0000-0000-0000-000000000004', 's0000004-0000-0000-0000-000000000004', 'Educational Platform (Annual)', 1, 3600.00, 3600.00, 1),
('l2000005-0000-0000-0000-000000000005', 's0000005-0000-0000-0000-000000000005', 'Cloud Hosting (3 months)', 1, 9500.00, 9500.00, 1),
('l2000006-0000-0000-0000-000000000006', 's0000006-0000-0000-0000-000000000006', 'SEO Services (Monthly)', 1, 2100.00, 2100.00, 1),
('l2000007-0000-0000-0000-000000000007', 's0000007-0000-0000-0000-000000000007', 'Annual Maintenance', 1, 18000.00, 18000.00, 1),
('l2000008-0000-0000-0000-000000000008', 's0000008-0000-0000-0000-000000000008', 'Market Research Report', 1, 6700.00, 6700.00, 1);

-- Line items for OVERDUE invoices
INSERT INTO invoice_line_items (id, invoice_id, description, quantity, unit_price, line_total, line_order) VALUES
('l3000001-0000-0000-0000-000000000001', 'o0000001-0000-0000-0000-000000000001', 'Leadership Training Workshop', 4, 1300.00, 5200.00, 1),
('l3000002-0000-0000-0000-000000000002', 'o0000002-0000-0000-0000-000000000002', 'Equipment Parts & Labor', 1, 11000.00, 11000.00, 1),
('l3000003-0000-0000-0000-000000000003', 'o0000003-0000-0000-0000-000000000003', 'POS System Upgrade', 1, 8900.00, 8900.00, 1),
('l3000004-0000-0000-0000-000000000004', 'o0000004-0000-0000-0000-000000000004', 'Telehealth Integration', 1, 4200.00, 4200.00, 1),
('l3000005-0000-0000-0000-000000000005', 'o0000005-0000-0000-0000-000000000005', 'Student Management System', 1, 2900.00, 2900.00, 1);

-- Line items for PAID invoices
INSERT INTO invoice_line_items (id, invoice_id, description, quantity, unit_price, line_total, line_order) VALUES
('l4000001-0000-0000-0000-000000000001', 'p0000001-0000-0000-0000-000000000001', 'December Consulting Services', 60, 125.00, 7500.00, 1),
('l4000002-0000-0000-0000-000000000002', 'p0000002-0000-0000-0000-000000000002', 'Mobile App Development', 1, 4100.00, 4100.00, 1),
('l4000003-0000-0000-0000-000000000003', 'p0000003-0000-0000-0000-000000000003', 'Q4 Enterprise Contract', 1, 22000.00, 22000.00, 1),
('l4000004-0000-0000-0000-000000000004', 'p0000004-0000-0000-0000-000000000004', 'POS Software License', 1, 1200.00, 1200.00, 1),
('l4000005-0000-0000-0000-000000000005', 'p0000005-0000-0000-0000-000000000005', 'Business Analysis Services', 28, 200.00, 5600.00, 1);

-- Line items for CANCELLED invoices
INSERT INTO invoice_line_items (id, invoice_id, description, quantity, unit_price, line_total, line_order) VALUES
('l5000001-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 'Training Materials (Cancelled)', 30, 110.00, 3300.00, 1),
('l5000002-0000-0000-0000-000000000002', 'c0000002-0000-0000-0000-000000000002', 'Equipment Order (Cancelled)', 1, 7200.00, 7200.00, 1);

-- ========================================
-- PAYMENTS for PAID and PARTIALLY PAID invoices
-- ========================================

-- Payments for fully paid invoices
INSERT INTO payments (id, invoice_id, payment_amount, payment_date, payment_method, transaction_reference, notes, created_at) VALUES
('pay00001-0000-0000-0000-000000000001', 'p0000001-0000-0000-0000-000000000001', 8250.00, CURRENT_DATE - INTERVAL '16 days', 'BANK_TRANSFER', 'WIRE-20241216-001', 'Full payment received', NOW() - INTERVAL '16 days'),
('pay00002-0000-0000-0000-000000000002', 'p0000002-0000-0000-0000-000000000002', 4510.00, CURRENT_DATE - INTERVAL '6 days', 'CREDIT_CARD', 'CC-VISA-4532', 'Paid via company card', NOW() - INTERVAL '6 days'),
('pay00003-0000-0000-0000-000000000003', 'p0000003-0000-0000-0000-000000000003', 24200.00, CURRENT_DATE - INTERVAL '32 days', 'BANK_TRANSFER', 'WIRE-20241201-005', 'Enterprise contract payment', NOW() - INTERVAL '32 days'),
('pay00004-0000-0000-0000-000000000004', 'p0000004-0000-0000-0000-000000000004', 1320.00, CURRENT_DATE - INTERVAL '10 days', 'CHECK', 'CHK-8947', 'Early payment - check received', NOW() - INTERVAL '10 days'),

-- Multiple payments for installment invoice
('pay00005-0000-0000-0000-000000000005', 'p0000005-0000-0000-0000-000000000005', 3000.00, CURRENT_DATE - INTERVAL '25 days', 'BANK_TRANSFER', 'WIRE-20241208-002', 'First installment', NOW() - INTERVAL '25 days'),
('pay00006-0000-0000-0000-000000000006', 'p0000005-0000-0000-0000-000000000005', 3160.00, CURRENT_DATE - INTERVAL '22 days', 'BANK_TRANSFER', 'WIRE-20241211-003', 'Final installment', NOW() - INTERVAL '22 days'),

-- Partial payment for one SENT invoice
('pay00007-0000-0000-0000-000000000007', 's0000005-0000-0000-0000-000000000005', 5000.00, CURRENT_DATE - INTERVAL '15 days', 'BANK_TRANSFER', 'WIRE-20241218-004', 'Partial payment - 50% down', NOW() - INTERVAL '15 days');

-- ========================================
-- REMINDER EMAILS for OVERDUE invoices
-- ========================================

INSERT INTO reminder_emails (id, invoice_id, recipient_email, subject, email_body, reminder_type, status, scheduled_for, sent_at, created_at, updated_at) VALUES

-- Reminders sent for first overdue invoice (5 days overdue)
('rem00001-0000-0000-0000-000000000001', 'o0000001-0000-0000-0000-000000000001', 'mbrown@peakperf.com', 'Payment Reminder: Invoice INV-202412-0025', 'Dear Michael, This is a friendly reminder that invoice INV-202412-0025 for $5,720.00 was due on ' || TO_CHAR(CURRENT_DATE - INTERVAL '5 days', 'Mon DD, YYYY') || '. Please submit payment at your earliest convenience. Payment link: http://localhost:3000/public/payment/payment-link-overdue-001', 'OVERDUE_7_DAYS', 'SENT', CURRENT_DATE - INTERVAL '2 days', CURRENT_DATE - INTERVAL '2 days', CURRENT_DATE - INTERVAL '2 days', CURRENT_DATE - INTERVAL '2 days'),

-- Reminders sent for second overdue invoice (10 days overdue)
('rem00002-0000-0000-0000-000000000002', 'o0000002-0000-0000-0000-000000000002', 'e.davis@summitmfg.com', 'Urgent: Overdue Invoice INV-202412-0026', 'Dear Emily, Invoice INV-202412-0026 for $12,100.00 is now 10 days past due. We kindly request immediate payment. If payment has been sent, please disregard this message. Payment link: http://localhost:3000/public/payment/payment-link-overdue-002', 'OVERDUE_7_DAYS', 'SENT', CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE - INTERVAL '5 days'),

-- Multiple reminders for severely overdue invoice (30 days overdue)
('rem00003-0000-0000-0000-000000000003', 'o0000003-0000-0000-0000-000000000003', 'david.m@metroretail.com', 'Important: Invoice Due', 'Dear David, Your invoice INV-202412-0027 for $9,790.00 is now due. Please arrange payment soon. Payment link: http://localhost:3000/public/payment/payment-link-overdue-003', 'OVERDUE_7_DAYS', 'SENT', CURRENT_DATE - INTERVAL '23 days', CURRENT_DATE - INTERVAL '23 days', CURRENT_DATE - INTERVAL '23 days', CURRENT_DATE - INTERVAL '23 days'),

('rem00004-0000-0000-0000-000000000004', 'o0000003-0000-0000-0000-000000000003', 'david.m@metroretail.com', 'Second Notice: Overdue Invoice', 'Dear David, This is our second notice regarding invoice INV-202412-0027 for $9,790.00, now 30 days past due. Please contact us immediately to arrange payment. Payment link: http://localhost:3000/public/payment/payment-link-overdue-003', 'OVERDUE_30_DAYS', 'SENT', CURRENT_DATE - INTERVAL '1 day', CURRENT_DATE - INTERVAL '1 day', CURRENT_DATE - INTERVAL '1 day', CURRENT_DATE - INTERVAL '1 day');

-- ========================================
-- SUMMARY STATISTICS (for verification)
-- ========================================

-- SELECT 'Data Load Summary' as report;
-- SELECT 'Total Customers:', COUNT(*) FROM customers;
-- SELECT 'Total Invoices:', COUNT(*) FROM invoices;
-- SELECT '  - DRAFT:', COUNT(*) FROM invoices WHERE status = 'DRAFT';
-- SELECT '  - SENT:', COUNT(*) FROM invoices WHERE status = 'SENT';
-- SELECT '  - PAID:', COUNT(*) FROM invoices WHERE status = 'PAID';
-- SELECT '  - CANCELLED:', COUNT(*) FROM invoices WHERE status = 'CANCELLED';
-- SELECT '  - OVERDUE (estimated):', COUNT(*) FROM invoices WHERE status = 'SENT' AND due_date < CURRENT_DATE;
-- SELECT 'Total Payments:', COUNT(*) FROM payments;
-- SELECT 'Total Reminder Emails:', COUNT(*) FROM reminder_emails;
-- SELECT 'Total Line Items:', COUNT(*) FROM invoice_line_items;

-- ========================================
-- MOCKUP DATA LOAD COMPLETE
-- ========================================
