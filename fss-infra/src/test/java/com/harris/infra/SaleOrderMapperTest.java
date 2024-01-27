package com.harris.infra;

import com.harris.domain.model.PageQuery;
import com.harris.infra.mapper.SaleOrderMapper;
import com.harris.infra.model.SaleOrderDO;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SaleOrderMapperTest {
    private static SqlSessionFactory sqlSessionFactory;
    private SqlSession session;
    private SaleOrderMapper saleOrderMapper;

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
        saleOrderMapper = session.getMapper(SaleOrderMapper.class);
    }

    @Test
    void insertOrderTest() {
        SaleOrderDO saleOrderDO = new SaleOrderDO();
        saleOrderDO.setItemId(1L);
        saleOrderDO.setActivityId(3L);
        saleOrderDO.setItemTitle("test9");
        saleOrderDO.setSalePrice(88L);
        saleOrderDO.setQuantity(1);
        saleOrderDO.setTotalAmount(88L);
        saleOrderDO.setStatus(1);
        saleOrderDO.setUserId(1L);
        System.out.println(saleOrderDO);
        int affectedRows = saleOrderMapper.insertOrder(saleOrderDO);
        System.out.println(affectedRows);
        assertEquals(1, affectedRows);
        session.commit();

        SaleOrderDO saleOrderDO1 = new SaleOrderDO();
        saleOrderDO1.setItemId(2L);
        saleOrderDO1.setActivityId(3L);
        saleOrderDO1.setItemTitle("test9");
        saleOrderDO1.setSalePrice(99L);
        saleOrderDO1.setQuantity(1);
        saleOrderDO1.setTotalAmount(99L);
        saleOrderDO1.setStatus(0);
        saleOrderDO1.setUserId(2L);
        System.out.println(saleOrderDO1);
        int affectedRows1 = saleOrderMapper.insertOrder(saleOrderDO1);
        System.out.println(affectedRows1);
        assertEquals(1, affectedRows1);
        session.commit();
    }

    @Test
    void updateStatusTest() {
        SaleOrderDO saleOrderDO = new SaleOrderDO();
        saleOrderDO.setId(3L);
        saleOrderDO.setStatus(1);
        int affectedRows = saleOrderMapper.updateStatus(saleOrderDO);
        System.out.println(affectedRows);
        assertEquals(1, affectedRows);
        session.commit();
    }

    @Test
    void getOrderByIdTest() {
        SaleOrderDO saleOrderDO = saleOrderMapper.getOrderById(3L);
        System.out.println(saleOrderDO);
        assertEquals(3L, saleOrderDO.getId());
    }

    @Test
    void getOrdersByConditionTest() {
        PageQuery pageQuery = new PageQuery()
                .setKeyword("test")
                .setStatus(1)
                .validateParams();
        List<SaleOrderDO> orders = saleOrderMapper.getOrdersByCondition(pageQuery);
        orders.forEach(System.out::println);
        assertEquals(2, orders.size());
    }

    @Test
    void countOrdersByConditionTest() {
        PageQuery pageQuery = new PageQuery().setKeyword("test").setStatus(1);
        int count = saleOrderMapper.countOrdersByCondition(pageQuery);
        System.out.println(count);
        assertEquals(2, count);
    }
}
