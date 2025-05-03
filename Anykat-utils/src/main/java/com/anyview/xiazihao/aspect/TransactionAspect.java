package com.anyview.xiazihao.aspect;

import com.anyview.xiazihao.aspectProcessor.ProceedingJoinPoint;
import com.anyview.xiazihao.aspectProcessor.annotation.KatAround;
import com.anyview.xiazihao.aspectProcessor.annotation.KatAspect;
import com.anyview.xiazihao.connectionPool.HakimiConnectionPool;
import com.anyview.xiazihao.connectionPool.ConnectionContext;

import java.sql.Connection;

@KatAspect
public class TransactionAspect {

    @KatAround("@annotation(com.anyview.xiazihao.annotation.KatTransactional)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Connection conn = null;
        try {
            // 1. 获取数据源连接
            conn = HakimiConnectionPool.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 2. 绑定连接到当前线程
            ConnectionContext.bindConnection(conn);

            // 3. 执行业务方法
            Object result = joinPoint.proceed();

            // 4. 提交事务
            conn.commit();
            return result;

        } catch (Throwable e) {
            // 5. 回滚事务
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            // 6. 清理资源
            ConnectionContext.unbindConnection();
            if (conn != null) conn.close();
        }
    }
}
