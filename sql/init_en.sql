CREATE TABLE IF NOT EXISTS fs_activity
(
    activity_id     BIGINT AUTO_INCREMENT COMMENT 'Sale Activity ID' PRIMARY KEY,
    activity_name   VARCHAR(30)       NOT NULL COMMENT 'Sale Activity Name',
    commodity_id    BIGINT            NOT NULL COMMENT 'Commodity ID',
    total_stock     BIGINT            NOT NULL COMMENT 'Total Stock Quantity',
    available_stock BIGINT            NOT NULL COMMENT 'Available Stock Quantity',
    lock_stock      BIGINT  DEFAULT 0 NOT NULL COMMENT 'Locked Stock Quantity',
    activity_status TINYINT DEFAULT 0 NOT NULL COMMENT 'Activity Status: 0: Off, 1: active',
    old_price       DECIMAL(10, 2)    NOT NULL COMMENT 'Original Price of Commodity',
    sale_price      DECIMAL(10, 2)    NOT NULL COMMENT 'Sale Price during Activity',
    start_time      DATETIME          NULL COMMENT 'Start Time',
    end_time        DATETIME          NULL COMMENT 'End Time'
);

CREATE TABLE IF NOT EXISTS fs_commodity
(
    commodity_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    commodity_name   VARCHAR(50)    NOT NULL COMMENT 'Commodity Name',
    commodity_price  DECIMAL(10, 2) NOT NULL COMMENT 'Commodity Price',
    commodity_detail LONGTEXT       NOT NULL COMMENT 'Commodity Details',
    commodity_img    VARCHAR(64)    NULL COMMENT 'Commodity Image'
);

CREATE TABLE IF NOT EXISTS fs_order
(
    order_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no     VARCHAR(50)    NOT NULL COMMENT 'Order ID',
    order_status TINYINT        NOT NULL COMMENT 'Order Status: 0: No Stock, invalid Order, 1: Created, Waiting for Payment, 2: Paid, Purchase Successful, -1: Unpaid, Closed',
    order_amount DECIMAL(10, 2) NOT NULL COMMENT 'Order Amount',
    user_id      BIGINT         NOT NULL COMMENT 'User ID',
    activity_id  BIGINT         NOT NULL COMMENT 'Activity ID',
    commodity_id BIGINT         NOT NULL COMMENT 'Commodity ID',
    create_time  DATETIME       NOT NULL COMMENT 'Order Creation Time',
    pay_time     DATETIME       NULL COMMENT 'Payment Time'
);

CREATE TABLE IF NOT EXISTS fs_user
(
    user_id  BIGINT AUTO_INCREMENT COMMENT 'User ID, Phone Number' PRIMARY KEY,
    username VARCHAR(20)  NOT NULL COMMENT 'Username',
    password VARCHAR(32)  NULL COMMENT 'MD5(MD5(pass+fixed salt)+salt)',
    salt     VARCHAR(10)  NULL,
    address  VARCHAR(255) NULL COMMENT 'Shipping Address'
);
