package com.anyview.xiazihao.connectionPool;

import java.sql.Connection;
import java.sql.SQLException;

public interface PooledConnection extends Connection {
    /**
     * 获取原始连接（危险操作，需谨慎使用）
     */
    Connection getRawConnection();

    /**
     * 获取连接最后使用时间
     */
    long getLastUsedTime();

    /**
     * 强制物理关闭连接
     */
    void realClose() throws SQLException;
}
