INSERT INTO restaurant.restaurants(id, name, active)
VALUES ('fdfe3ba1-da91-45f9-8b8a-4f3c00b7caef', 'restaurant_1', TRUE),
       ('33c900b6-e473-4028-b63e-7eca6bc4f2ea', 'restaurant_2', FALSE);

INSERT INTO restaurant.products(id, name, price, available)
VALUES ('f761546f-b7f3-4363-8ce4-50ec9f9425c9', 'product_1', 25.00, FALSE),
       ('dd73287d-ba1c-457f-a57e-dab7842069fb', 'product_2', 50.00, TRUE),
       ('906a98b0-f84f-4218-89a5-c6802e14abb2', 'product_3', 20.00, FALSE),
       ('bc96d4f2-ef05-4531-93bd-f1f543a8ab3b', 'product_4', 40.00, TRUE);

INSERT INTO restaurant.restaurants_products(id, restaurant_id, product_id)
VALUES ('0a55e6b1-9a27-470e-864f-cae85ee9f6be', 'fdfe3ba1-da91-45f9-8b8a-4f3c00b7caef',
        'f761546f-b7f3-4363-8ce4-50ec9f9425c9'),
       ('ed27d89c-c67d-4984-8a31-eedef8f313ab', 'fdfe3ba1-da91-45f9-8b8a-4f3c00b7caef',
        'dd73287d-ba1c-457f-a57e-dab7842069fb'),
       ('77766ebb-22b7-4f98-858d-2a1ed59b75c2', '33c900b6-e473-4028-b63e-7eca6bc4f2ea',
        '906a98b0-f84f-4218-89a5-c6802e14abb2'),
       ('4a1ad124-671c-4527-87e6-fd9466524e12', '33c900b6-e473-4028-b63e-7eca6bc4f2ea',
        'bc96d4f2-ef05-4531-93bd-f1f543a8ab3b');