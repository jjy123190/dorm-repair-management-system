package com.scau.dormrepair.common;

import java.util.function.Function;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * 对 MyBatis 的会话打开、提交、回滚做一层统一封装。
 * 后续同学写 service 时只需要关心业务逻辑，不用重复写模板代码。
 */
public final class MyBatisExecutor {

    private final SqlSessionFactory sqlSessionFactory;

    public MyBatisExecutor(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     * 只读查询统一走这个入口。
     */
    public <T> T executeRead(Function<SqlSession, T> action) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return action.apply(session);
        }
    }

    /**
     * 写操作统一走事务入口，异常时自动回滚。
     */
    public <T> T executeWrite(Function<SqlSession, T> action) {
        try (SqlSession session = sqlSessionFactory.openSession(false)) {
            try {
                T result = action.apply(session);
                session.commit();
                return result;
            } catch (RuntimeException exception) {
                session.rollback();
                throw exception;
            }
        }
    }
}
