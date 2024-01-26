package com.harris.infra;

import com.harris.infra.mapper.BucketMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

class BucketMapperTest {
    private static SqlSessionFactory sqlSessionFactory;
    private SqlSession session;
    private BucketMapper bucketMapper;

    static {
        try {
            String resource = "mybatis-config-test.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SqlSessionFactory", e);
        }
    }

    @BeforeEach
    void setUp() {
        session = sqlSessionFactory.openSession();
        bucketMapper = session.getMapper(BucketMapper.class);
    }

    @Test
    void insertBucketsTest() {

    }

    @Test
    void updateBucketStatusByItemIdTest() {

    }

    @Test
    void getBucketsByItemIdTest() {

    }

    @Test
    void reduceStockByItemIdTest() {

    }

    @Test
    void addStockByItemIdTest() {

    }

    @Test
    void deleteBucketByItemIdTest() {

    }
}
