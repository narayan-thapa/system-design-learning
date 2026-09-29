--liquibase formatted sql

--changeset narayan:003-add-colocated-foreign-keys
ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_items_cart
    FOREIGN KEY (cart_id, user_id)
    REFERENCES carts(id, user_id)
    ON DELETE CASCADE;

ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_order
    FOREIGN KEY (order_id, user_id)
    REFERENCES orders(id, user_id)
    ON DELETE CASCADE;

ALTER TABLE payments
    ADD CONSTRAINT fk_payments_order
    FOREIGN KEY (order_id, user_id)
    REFERENCES orders(id, user_id)
    ON DELETE CASCADE;
