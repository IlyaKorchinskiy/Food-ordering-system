DROP SCHEMA IF EXISTS payment CASCADE;
CREATE SCHEMA payment;

CREATE
EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TYPE IF EXISTS payment_status;
CREATE TYPE payment_status AS ENUM ('COMPLETED', 'CANCELLED', 'FAILED');

DROP TABLE IF EXISTS payment.payments CASCADE;
CREATE TABLE payment.payments
(
    id             uuid                     NOT NULL,
    customer_id    uuid                     NOT NULL,
    order_id       uuid                     NOT NULL,
    price          numeric(10, 2)           NOT NULL,
    payment_status payment_status           NOT NULL,
    created_at     timestamp with time zone NOT NULL,
    CONSTRAINT payments_pkey PRIMARY KEY (id)
);

DROP TABLE IF EXISTS payment.credit_entries CASCADE;
CREATE TABLE payment.credit_entries
(
    id                  uuid           NOT NULL,
    customer_id         uuid           NOT NULL,
    total_credit_amount numeric(10, 2) NOT NULL,
    CONSTRAINT credit_entries_pkey PRIMARY KEY (id)
);

DROP TYPE IF EXISTS transaction_type;
CREATE TYPE transaction_type AS ENUM ('DEBIT', 'CREDIT');

DROP TABLE IF EXISTS payment.credit_history CASCADE;
CREATE TABLE payment.credit_history
(
    id               uuid             NOT NULL,
    customer_id      uuid             NOT NULL,
    amount           numeric(10, 2)   NOT NULL,
    transaction_type transaction_type NOT NULL,
    CONSTRAINT credit_history_pkey PRIMARY KEY (id)
);

DROP TYPE IF EXISTS outbox_status;
CREATE TYPE outbox_status AS ENUM ('STARTED', 'FAILED', 'COMPLETED');

DROP TABLE IF EXISTS payment.order_outbox CASCADE;
CREATE TABLE payment.order_outbox
(
    id             uuid                                           NOT NULL,
    saga_id        uuid                                           NOT NULL,
    created_at     timestamp with time zone                       NOT NULL,
    processed_at   timestamp with time zone                       NOT NULL,
    type           character varying COLLATE pg_catalog."default" NOT NULL,
    payload        jsonb                                          NOT NULL,
    outbox_status  outbox_status                                  NOT NULL,
    payment_status payment_status                                 NOT NULL,
    version        integer                                        NOT NULL,
    CONSTRAINT order_outbox_pkey PRIMARY KEY (id)
);

CREATE INDEX "payment_order_outbox_payment_status"
    ON payment.order_outbox (type, payment_status);

CREATE UNIQUE INDEX "payment_order_outbox_saga_id_payment_status_outbox_status"
    ON "order".payment_outbox (type, saga_id, payment_status, outbox_status);