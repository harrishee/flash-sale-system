CREATE DATABASE IF NOT EXISTS flash_sale
    default charset = utf8mb4;

CREATE TABLE IF NOT EXISTS flash_sale.sale_activity
(
    `id`            bigint(20)  NOT NULL AUTO_INCREMENT,
    `activity_name` varchar(50) NOT NULL,
    `activity_desc` text,
    `status`        int(11)     NOT NULL DEFAULT '0',
    `start_time`    datetime    NOT NULL,
    `end_time`      datetime    NOT NULL,
    `modified_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `flash_activity_end_time_idx` (`end_time`),
    KEY `flash_activity_start_time_idx` (`start_time`),
    KEY `flash_activity_status_idx` (`status`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = '活动表';

CREATE TABLE IF NOT EXISTS flash_sale.sale_item
(
    `id`              bigint(20)  NOT NULL AUTO_INCREMENT,
    `item_title`      varchar(50) NOT NULL,
    `item_sub_title`  varchar(50) NULL,
    `item_desc`       text,
    `initial_stock`   int(11)     NOT NULL DEFAULT '0',
    `available_stock` int(11)     NOT NULL DEFAULT '0',
    `stock_warm_up`   int(11)     NOT NULL DEFAULT '0',
    `original_price`  bigint(20)  NOT NULL,
    `sale_price`     bigint(20)  NOT NULL,
    `rules`           text COMMENT '抢购品可配规则，JSON格式',
    `status`          int(11)     NOT NULL DEFAULT '0',
    `activity_id`     bigint(20)  NOT NULL,
    `start_time`      datetime    NOT NULL,
    `end_time`        datetime    NOT NULL,
    `modified_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_time`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `flash_item_end_time_idx` (`end_time`),
    KEY `flash_item_start_time_idx` (`start_time`),
    KEY `flash_item_status_idx` (`status`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = '抢购品表';

CREATE TABLE IF NOT EXISTS flash_sale.sale_order
(
    `id`            bigint(20)  NOT NULL AUTO_INCREMENT,
    `item_id`       bigint(20)  NOT NULL,
    `activity_id`   bigint(20)  NOT NULL,
    `item_title`    varchar(50) NOT NULL,
    `sale_price`   bigint(20)  NOT NULL,
    `quantity`      int(11)     NOT NULL,
    `total_amount`  bigint(20)  NOT NULL,
    `status`        int(11)     NOT NULL DEFAULT '0',
    `user_id`       bigint(20)  NOT NULL,
    `modified_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `flash_order_id_uk` (`id`),
    KEY `flash_order_user_id_idx` (`user_id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = '订单表';

CREATE TABLE flash_sale.shedlock
(
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMP(3) NOT NULL,
    locked_at  TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    locked_by  VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = '分布式调度锁';


GRANT ALL PRIVILEGES ON `flash_sale`.* TO 'harris'@'%';
FLUSH PRIVILEGES;
