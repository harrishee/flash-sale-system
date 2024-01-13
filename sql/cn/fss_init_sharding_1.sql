CREATE DATABASE IF NOT EXISTS flash_sale_1
    default charset = utf8mb4;

CREATE TABLE IF NOT EXISTS flash_sale_1.flash_order_0
(
    `id`            bigint(20)  NOT NULL AUTO_INCREMENT
        COMMENT '主键',
    `item_id`       bigint(20)  NOT NULL
        COMMENT '商品ID',
    `activity_id`   bigint(20)  NOT NULL
        COMMENT '活动ID',
    `item_title`    varchar(50) NOT NULL
        COMMENT '商品标题',
    `flash_price`   bigint(20)  NOT NULL
        COMMENT '商品活动价',
    `quantity`      int(11)     NOT NULL
        COMMENT '数量',
    `total_amount`  bigint(20)  NOT NULL
        COMMENT '总价格',
    `status`        int(11)     NOT NULL DEFAULT '0'
        COMMENT '订单状态',
    `user_id`       bigint(20)  NOT NULL
        COMMENT '用户ID',
    `modified_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '更新时间',
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `flash_order_user_id_idx` (`user_id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = '订单表';

CREATE TABLE IF NOT EXISTS flash_sale_1.flash_order_1
(
    `id`            bigint(20)  NOT NULL AUTO_INCREMENT
        COMMENT '主键',
    `item_id`       bigint(20)  NOT NULL
        COMMENT '商品ID',
    `activity_id`   bigint(20)  NOT NULL
        COMMENT '活动ID',
    `item_title`    varchar(50) NOT NULL
        COMMENT '商品标题',
    `flash_price`   bigint(20)  NOT NULL
        COMMENT '商品活动价',
    `quantity`      int(11)     NOT NULL
        COMMENT '数量',
    `total_amount`  bigint(20)  NOT NULL
        COMMENT '总价格',
    `status`        int(11)     NOT NULL DEFAULT '0'
        COMMENT '订单状态',
    `user_id`       bigint(20)  NOT NULL
        COMMENT '用户ID',
    `modified_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '更新时间',
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `flash_order_user_id_idx` (`user_id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = '订单表';

CREATE TABLE IF NOT EXISTS flash_sale_1.flash_order_2
(
    `id`            bigint(20)  NOT NULL AUTO_INCREMENT
        COMMENT '主键',
    `item_id`       bigint(20)  NOT NULL
        COMMENT '商品ID',
    `activity_id`   bigint(20)  NOT NULL
        COMMENT '活动ID',
    `item_title`    varchar(50) NOT NULL
        COMMENT '商品标题',
    `flash_price`   bigint(20)  NOT NULL
        COMMENT '商品活动价',
    `quantity`      int(11)     NOT NULL
        COMMENT '数量',
    `total_amount`  bigint(20)  NOT NULL
        COMMENT '总价格',
    `status`        int(11)     NOT NULL DEFAULT '0'
        COMMENT '订单状态',
    `user_id`       bigint(20)  NOT NULL
        COMMENT '用户ID',
    `modified_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '更新时间',
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `flash_order_user_id_idx` (`user_id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = '订单表';


CREATE TABLE IF NOT EXISTS flash_sale_1.flash_bucket_0
(
    `id`                      bigint(20) NOT NULL AUTO_INCREMENT
        COMMENT '主键',
    `item_id`                 bigint(20) NOT NULL
        COMMENT '商品ID',
    `total_stocks_amount`     int(11)    NOT NULL
        COMMENT '库存总量',
    `available_stocks_amount` int(11)    NOT NULL
        COMMENT '可用库存总量',
    `status`                  int(11)    NOT NULL DEFAULT '0'
        COMMENT '库存状态',
    `serial_no`               int(11)    NOT NULL
        COMMENT '库存分桶编号',
    `modified_time`           datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '更新时间',
    `create_time`             datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `flash_bucket_item_id_serial_no_uk` (`item_id`, `serial_no`),
    KEY `flash_bucket_item_id_idx` (`item_id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = '商品库存分桶表';

CREATE TABLE IF NOT EXISTS flash_sale_1.flash_bucket_1
(
    `id`                      bigint(20) NOT NULL AUTO_INCREMENT
        COMMENT '主键',
    `item_id`                 bigint(20) NOT NULL
        COMMENT '商品ID',
    `total_stocks_amount`     int(11)    NOT NULL
        COMMENT '库存总量',
    `available_stocks_amount` int(11)    NOT NULL
        COMMENT '可用库存总量',
    `status`                  int(11)    NOT NULL DEFAULT '0'
        COMMENT '库存状态',
    `serial_no`               int(11)    NOT NULL
        COMMENT '库存分桶编号',
    `modified_time`           datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '更新时间',
    `create_time`             datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `flash_bucket_item_id_serial_no_uk` (`item_id`, `serial_no`),
    KEY `flash_bucket_item_id_idx` (`item_id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = '商品库存分桶表';

CREATE TABLE IF NOT EXISTS flash_sale_1.flash_bucket_2
(
    `id`                      bigint(20) NOT NULL AUTO_INCREMENT
        COMMENT '主键',
    `item_id`                 bigint(20) NOT NULL
        COMMENT '商品ID',
    `total_stocks_amount`     int(11)    NOT NULL
        COMMENT '库存总量',
    `available_stocks_amount` int(11)    NOT NULL
        COMMENT '可用库存总量',
    `status`                  int(11)    NOT NULL DEFAULT '0'
        COMMENT '库存状态',
    `serial_no`               int(11)    NOT NULL
        COMMENT '库存分桶编号',
    `modified_time`           datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '更新时间',
    `create_time`             datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `flash_bucket_item_id_serial_no_uk` (`item_id`, `serial_no`),
    KEY `flash_bucket_item_id_idx` (`item_id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = '商品库存分桶表';

GRANT ALL PRIVILEGES ON `flash_sale_1`.* TO 'harris'@'%';
FLUSH PRIVILEGES;