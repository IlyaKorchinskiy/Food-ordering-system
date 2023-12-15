INSERT INTO payment.credit_entries(id, customer_id, total_credit_amount)
VALUES ('6209ceaa-09c5-4b52-9ea9-608cfadc6bc9', 'f9d2c787-0885-4189-a2d9-5e98f3d6b16d', 500.00);
INSERT INTO payment.credit_history(id, customer_id, amount, type)
VALUES ('3accf7e1-8a50-403e-8a4a-0ff89cb84b83', 'f9d2c787-0885-4189-a2d9-5e98f3d6b16d', 100.00, 'CREDIT'),
       ('6b237b40-000d-415c-988d-b74cd359ce79', 'f9d2c787-0885-4189-a2d9-5e98f3d6b16d', 600.00, 'CREDIT'),
       ('44a767de-a80a-4545-ba76-f74a6b00cae8', 'f9d2c787-0885-4189-a2d9-5e98f3d6b16d', 200.00, 'DEBIT');

INSERT INTO payment.credit_entries(id, customer_id, total_credit_amount)
VALUES ('843b3f35-49ca-4ea6-8222-e58ea7f7d2b5', '9ce63b16-dea0-46a6-b3c8-4659d864fac6', 100.00);
INSERT INTO payment.credit_history(id, customer_id, amount, type)
VALUES ('4d776b29-9e6a-4cba-8124-90ae96aadf21', '9ce63b16-dea0-46a6-b3c8-4659d864fac6', 100.00, 'CREDIT');
