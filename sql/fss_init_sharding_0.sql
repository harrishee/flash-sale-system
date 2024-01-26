CREATE DATABASE IF NOT EXISTS flash_sale_0
    default charset = utf8mb4;

CREATE TABLE IF NOT EXISTS flash_sale_0.sale_order_0
(
    `id`            bigint(20)  NOT NULL AUTO_INCREMENT,
    `item_id`       bigint(20)  NOT NULL,
    `activity_id`   bigint(20)  NOT NULL,
    `item_title`    varchar(50) NOT NULL,
    `sale_price`    bigint(20)  NOT NULL,
    `quantity`      int(11)     NOT NULL,
    `total_amount`  bigint(20)  NOT NULL,
    `status`        int(11)     NOT NULL DEFAULT '0',
    `user_id`       bigint(20)  NOT NULL,
    `modified_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `flash_order_user_id_idx` (`user_id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = '订单表';

CREATE TABLE IF NOT EXISTS flash_sale_0.sale_order_1
(
    `id`            bigint(20)  NOT NULL AUTO_INCREMENT,
    `item_id`       bigint(20)  NOT NULL,
    `activity_id`   bigint(20)  NOT NULL,
    `item_title`    varchar(50) NOT NULL,
    `sale_price`    bigint(20)  NOT NULL,
    `quantity`      int(11)     NOT NULL,
    `total_amount`  bigint(20)  NOT NULL,
    `status`        int(11)     NOT NULL DEFAULT '0',
    `user_id`       bigint(20)  NOT NULL,
    `modified_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `flash_order_user_id_idx` (`user_id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = '订单表';

CREATE TABLE IF NOT EXISTS flash_sale_0.sale_order_2
(
    `id`            bigint(20)  NOT NULL AUTO_INCREMENT,
    `item_id`       bigint(20)  NOT NULL,
    `activity_id`   bigint(20)  NOT NULL,
    `item_title`    varchar(50) NOT NULL,
    `sale_price`    bigint(20)  NOT NULL,
    `quantity`      int(11)     NOT NULL,
    `total_amount`  bigint(20)  NOT NULL,
    `status`        int(11)     NOT NULL DEFAULT '0',
    `user_id`       bigint(20)  NOT NULL,
    `modified_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `flash_order_user_id_idx` (`user_id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = '订单表';


CREATE TABLE IF NOT EXISTS flash_sale_0.sale_bucket_0
(
    `id`                      bigint(20) NOT NULL AUTO_INCREMENT,
    `item_id`                 bigint(20) NOT NULL,
    `total_stocks_amount`     int(11)    NOT NULL,
    `available_stocks_amount` int(11)    NOT NULL,
    `status`                  int(11)    NOT NULL DEFAULT '0',
    `serial_no`               int(11)    NOT NULL,
    `modified_time`           datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_time`             datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `flash_bucket_item_id_serial_no_uk` (`item_id`, `serial_no`),
    KEY `flash_bucket_item_id_idx` (`item_id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = '抢购品库存分桶表';

CREATE TABLE IF NOT EXISTS flash_sale_0.sale_bucket_1
(
    `id`                      bigint(20) NOT NULL AUTO_INCREMENT,
    `item_id`                 bigint(20) NOT NULL,
    `total_stocks_amount`     int(11)    NOT NULL,
    `available_stocks_amount` int(11)    NOT NULL,
    `status`                  int(11)    NOT NULL DEFAULT '0',
    `serial_no`               int(11)    NOT NULL,
    `modified_time`           datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_time`             datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `flash_bucket_item_id_serial_no_uk` (`item_id`, `serial_no`),
    KEY `flash_bucket_item_id_idx` (`item_id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = '抢购品库存分桶表';

CREATE TABLE IF NOT EXISTS flash_sale_0.sale_bucket_2
(
    `id`                      bigint(20) NOT NULL AUTO_INCREMENT,
    `item_id`                 bigint(20) NOT NULL,
    `total_stocks_amount`     int(11)    NOT NULL,
    `available_stocks_amount` int(11)    NOT NULL,
    `status`                  int(11)    NOT NULL DEFAULT '0',
    `serial_no`               int(11)    NOT NULL,
    `modified_time`           datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_time`             datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `flash_bucket_item_id_serial_no_uk` (`item_id`, `serial_no`),
    KEY `flash_bucket_item_id_idx` (`item_id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = '抢购品库存分桶表';

GRANT ALL PRIVILEGES ON `flash_sale_0`.* TO 'harris'@'%';
FLUSH PRIVILEGES;
