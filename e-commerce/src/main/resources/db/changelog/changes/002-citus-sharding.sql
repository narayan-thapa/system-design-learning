--liquibase formatted sql

--changeset narayan:002-enable-citus-extension dbms:postgresql
CREATE EXTENSION IF NOT EXISTS citus;

--changeset narayan:002-distribute-tables-citus dbms:postgresql splitStatements:false
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'citus') THEN
        -- 1. Reference tables: replicated to all Citus worker nodes for efficient local joins with products
        IF NOT EXISTS (SELECT 1 FROM citus_tables WHERE table_name::text = 'categories') THEN
            PERFORM create_reference_table('categories');
        END IF;

        IF NOT EXISTS (SELECT 1 FROM citus_tables WHERE table_name::text = 'brands') THEN
            PERFORM create_reference_table('brands');
        END IF;

        -- 2. Distributed catalog and user tables
        IF NOT EXISTS (SELECT 1 FROM citus_tables WHERE table_name::text = 'users') THEN
            PERFORM create_distributed_table('users', 'id');
        END IF;

        IF NOT EXISTS (SELECT 1 FROM citus_tables WHERE table_name::text = 'products') THEN
            PERFORM create_distributed_table('products', 'id');
        END IF;

        -- 3. Customer domain tables co-located by user_id
        -- Co-locating by user_id ensures all cart, order, and payment operations for any given user
        -- execute on a single shard node without cross-shard network hops.
        IF NOT EXISTS (SELECT 1 FROM citus_tables WHERE table_name::text = 'carts') THEN
            PERFORM create_distributed_table('carts', 'user_id');
        END IF;

        IF NOT EXISTS (SELECT 1 FROM citus_tables WHERE table_name::text = 'cart_items') THEN
            PERFORM create_distributed_table('cart_items', 'user_id', colocate_with => 'carts');
        END IF;

        IF NOT EXISTS (SELECT 1 FROM citus_tables WHERE table_name::text = 'orders') THEN
            PERFORM create_distributed_table('orders', 'user_id', colocate_with => 'carts');
        END IF;

        IF NOT EXISTS (SELECT 1 FROM citus_tables WHERE table_name::text = 'order_items') THEN
            PERFORM create_distributed_table('order_items', 'user_id', colocate_with => 'carts');
        END IF;

        IF NOT EXISTS (SELECT 1 FROM citus_tables WHERE table_name::text = 'payments') THEN
            PERFORM create_distributed_table('payments', 'user_id', colocate_with => 'carts');
        END IF;
    END IF;
END $$;
