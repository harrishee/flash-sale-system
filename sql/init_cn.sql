CREATE TABLE IF NOT EXISTS fs_activity
(
    activity_id     BIGINT AUTO_INCREMENT COMMENT '抢购活动ID' PRIMARY KEY,
    activity_name   VARCHAR(30)       NOT NULL COMMENT '抢购活动名称',
    commodity_id    BIGINT            NOT NULL COMMENT '商品ID',
    total_stock     BIGINT            NOT NULL COMMENT '库存数量',
    available_stock BIGINT            NOT NULL COMMENT '可用库存数量',
    lock_stock      BIGINT  DEFAULT 0 NOT NULL COMMENT '锁定库存数量',
    activity_status TINYINT DEFAULT 0 NOT NULL COMMENT '活动状态，0:下架 1:正常',
    old_price       DECIMAL(10, 2)    NOT NULL COMMENT '商品原价',
    sale_price      DECIMAL(10, 2)    NOT NULL COMMENT '活动价格',
    start_time      DATETIME          NULL COMMENT '开始时间',
    end_time        DATETIME          NULL COMMENT '结束时间'
);

CREATE TABLE IF NOT EXISTS fs_commodity
(
    commodity_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    commodity_name   VARCHAR(50)    NOT NULL COMMENT '商品名称',
    commodity_price  DECIMAL(10, 2) NOT NULL COMMENT '商品价格',
    commodity_detail LONGTEXT       NOT NULL COMMENT '商品详情',
    commodity_img    VARCHAR(64)    NULL COMMENT '商品图片'
);

CREATE TABLE IF NOT EXISTS fs_order
(
    order_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no     VARCHAR(50)    NOT NULL COMMENT '订单ID',
    order_status TINYINT        NOT NULL COMMENT '订单状态：0:没有库存，无效订单，1:已创建等待支付，2: 已支付购买成功，-1: 未支付已关闭',
    order_amount DECIMAL(10, 2) NOT NULL COMMENT '订单金额',
    user_id      BIGINT         NOT NULL COMMENT '用户ID',
    activity_id  BIGINT         NOT NULL COMMENT '活动ID',
    commodity_id BIGINT         NOT NULL COMMENT '商品ID',
    create_time  DATETIME       NOT NULL COMMENT '订单创建时间',
    pay_time     DATETIME       NULL COMMENT '支付时间'
);

CREATE TABLE IF NOT EXISTS fs_user
(
    user_id  BIGINT AUTO_INCREMENT COMMENT '用户ID，手机号码' PRIMARY KEY,
    username VARCHAR(20)  NOT NULL COMMENT '用户名',
    password VARCHAR(32)  NULL COMMENT 'MD5(MD5(pass+固定salt)+salt)',
    salt     VARCHAR(10)  NULL,
    address  VARCHAR(255) NULL COMMENT '收货地址'
);
