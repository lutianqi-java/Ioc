package com.lu.sql.threadpool;

import java.sql.Connection;

public interface IConnectionPool {


    /**
     * 获取连接
     *
     * @return
     */
    Connection getConnection();

    /**
     * 释放连接
     */
    void releaseConnection();

}
