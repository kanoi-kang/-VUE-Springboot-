-- 创建退款表
CREATE TABLE IF NOT EXISTS `refunds` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
  `refund_no` VARCHAR(50) NOT NULL COMMENT '退款单号',
  `reason` VARCHAR(500) NOT NULL COMMENT '退款原因',
  `description` VARCHAR(1000) DEFAULT NULL COMMENT '退款描述',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '退款金额',
  `status` INT NOT NULL DEFAULT 0 COMMENT '状态: 0-待处理, 1-已通过, 2-已拒绝',
  `admin_remark` VARCHAR(500) DEFAULT NULL COMMENT '管理员备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `process_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款表';