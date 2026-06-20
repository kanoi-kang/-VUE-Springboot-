-- 添加coupon_id字段到orders表
USE `yijiangnan`;

ALTER TABLE `orders` 
ADD COLUMN IF NOT EXISTS `coupon_id` BIGINT NULL 
AFTER `address_id`;

-- 为coupon_id添加索引
CREATE INDEX IF NOT EXISTS `idx_coupon_id` ON `orders`(`coupon_id`);