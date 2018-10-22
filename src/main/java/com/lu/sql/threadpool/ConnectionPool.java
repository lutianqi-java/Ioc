package com.lu.sql.threadpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

public class ConnectionPool implements IConnectionPool {

    private Logger log = Logger.getLogger(ConnectionPool.class.getName());

    private List<Connection> freeConnection = new Vector<Connection>();

    private List<Connection> activeConnection = new Vector<Connection>();

    private DbBean dbBean;

    /**
     * 总共的连接数
     */
    private int countConnection = 0;

    public ConnectionPool(DbBean dbBean) {
        this.dbBean = dbBean;
        init();
    }

    private void init() {
        if (dbBean == null) {
            return;
        }
        log.info("初始化连接数：" + dbBean.getInitConnections());
        for (int i = 0; i < dbBean.getInitConnections(); i++) {
            Connection connection = newConnection();
            if (connection != null) {
                freeConnection.add(connection);
            }

        }
        log.info("空闲连接数：" + freeConnection.size() + "总共的连接数：" + countConnection);

    }

    public Connection newConnection() {
        try {
            //创建连接
//            Class.forName(dbBean.getDriverName());
            Connection connection = DriverManager.getConnection(dbBean.getUrl(), dbBean.getUserName(), dbBean.getPassword());
            countConnection++;
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void releaseConnection() {

    }

    @Override
    public synchronized Connection getConnection() {
        Connection connection = null;
        if (countConnection < dbBean.getMaxActiveConnections()) {
            log.info("使用中的数量：" + countConnection);
            //当前的连接数没有超过设置的最大连接数
            if (freeConnection.size() > 0) {
                //有空闲的连接
                connection = freeConnection.remove(0);//从空闲连接的集合里面取出一个连接信息并在集合里面去除这个连接
//                log.info();
            } else {
                //没有空闲的连接
                connection = newConnection();
                countConnection++;
                log.info("可用的用光了，创建新的连接，总连接数：" + countConnection);
            }
            if (isAvailable(connection)) {
                activeConnection.add(connection);

                log.info("使用中的连接：" + activeConnection.size() + ";总共连接数：" + countConnection + ";空闲的连接：" + freeConnection.size());
            } else {
                log.info("不可用");
                countConnection--;
                connection = getConnection();
            }

        } else {
            try {
                wait(dbBean.getConnectionTimeOut());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getConnection();
        }
        return connection;
    }

    //判断是否可用
    private boolean isAvailable(Connection connection) {
        try {
            if (connection == null || connection.isClosed()) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }
}
