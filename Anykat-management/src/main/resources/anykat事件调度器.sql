-- 开启事件调度器
SET GLOBAL event_scheduler = ON;

############# 更新已到截止时间但状态未更新的练习 ##############
-- 储存过程
DROP PROCEDURE IF EXISTS `update_exercise_status_at_endtime`;
DROP EVENT IF EXISTS `event_check_exercise_endtime`;

DELIMITER //
CREATE PROCEDURE `update_exercise_status_at_endtime`()
BEGIN
    -- 更新"未开始"状态
    UPDATE `exercise` SET `status` = 0
    WHERE `start_time` > NOW()
    AND `status` != 0;

    -- 更新"进行中"状态
    UPDATE `exercise` SET `status` = 1
    WHERE `start_time` <= NOW() AND `end_time` >= NOW()
    AND `status` != 1;

    -- 更新"已结束"状态
    UPDATE `exercise` SET `status` = 2
    WHERE `end_time` < NOW()
    AND `status` != 2;
END //
DELIMITER ;

-- 定时任务调度器
DELIMITER //
CREATE EVENT `event_check_exercise_endtime`
    ON SCHEDULE EVERY 1 MINUTE  -- 每1分钟执行一次
        STARTS CURRENT_TIMESTAMP    -- 立即生效
    COMMENT '定期检查并更新已截止的练习状态'
    DO
    BEGIN
        CALL `update_exercise_status_at_endtime`();
    END //
DELIMITER ;



##########################################################

-- 查看所有事件
SHOW EVENTS FROM `anykat`;