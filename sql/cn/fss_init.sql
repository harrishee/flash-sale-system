CREATE DATABASE IF NOT EXISTS flash_sale
    default charset = utf8mb4;

CREATE TABLE IF NOT EXISTS flash_sale.flash_activity
(
    `id`            bigint(20)  NOT NULL AUTO_INCREMENT
        COMMENT '主键',
    `activity_name` varchar(50) NOT NULL
        COMMENT '活动名称',
    `activity_desc` text COMMENT '活动描述',
    `status`        int(11)     NOT NULL DEFAULT '0'
        COMMENT '活动状态',
    `start_time`    datetime    NOT NULL
        COMMENT '活动开始时间',
    `end_time`      datetime    NOT NULL
        COMMENT '活动结束时间',
    `modified_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '更新时间',
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `flash_activity_end_time_idx` (`end_time`),
    KEY `flash_activity_start_time_idx` (`start_time`),
    KEY `flash_activity_status_idx` (`status`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = '活动表';

CREATE TABLE IF NOT EXISTS flash_sale.flash_item
(
    `id`              bigint(20)  NOT NULL AUTO_INCREMENT
        COMMENT '主键',
    `item_title`      varchar(50) NOT NULL
        COMMENT '商品标题',
    `item_sub_title`  varchar(50) NULL
        COMMENT '商品副标题',
    `item_desc`       text COMMENT '商品介绍',
    `initial_stock`   int(11)     NOT NULL DEFAULT '0'
        COMMENT '商品初始库存',
    `available_stock` int(11)     NOT NULL DEFAULT '0'
        COMMENT '商品可用库存',
    `stock_warm_up`   int(11)     NOT NULL DEFAULT '0'
        COMMENT '商品是否已经预热',
    `original_price`  bigint(20)  NOT NULL
        COMMENT '商品原价',
    `flash_price`     bigint(20)  NOT NULL
        COMMENT '商品活动价',
    `rules`           text COMMENT '商品可配规则，JSON格式',
    `status`          int(11)     NOT NULL DEFAULT '0'
        COMMENT '商品状态',
    `activity_id`     bigint(20)  NOT NULL
        COMMENT '所属活动id',
    `start_time`      datetime    NOT NULL
        COMMENT '商品开始时间',
    `end_time`        datetime    NOT NULL
        COMMENT '商品结束时间',
    `modified_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '更新时间',
    `create_time`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `flash_item_end_time_idx` (`end_time`),
    KEY `flash_item_start_time_idx` (`start_time`),
    KEY `flash_item_status_idx` (`status`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COMMENT = '商品表';

CREATE TABLE IF NOT EXISTS flash_sale.flash_order
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
