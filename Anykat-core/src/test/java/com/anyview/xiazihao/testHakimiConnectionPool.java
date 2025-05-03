package com.anyview.xiazihao;

import com.anyview.xiazihao.connectionPool.HakimiConnectionPool;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.sql.SQLException;

public class testHakimiConnectionPool {
    @Test
    public void testConnect() throws FileNotFoundException, SQLException {
        HakimiConnectionPool.getInstance().getConnection();
    }
}
