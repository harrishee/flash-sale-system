package com.harris.infra;

import com.harris.domain.model.PageQueryCondition;
import com.harris.infra.mapper.SaleItemMapper;
import com.harris.infra.model.SaleItemDO;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SaleItemMapperTest {
    private static SqlSessionFactory sqlSessionFactory;
    private SqlSession session;
    private SaleItemMapper saleItemMapper;

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
        saleItemMapper = session.getMapper(SaleItemMapper.class);
    }

    @Test
    void insertItemTest() {
        SaleItemDO saleItemDO = new SaleItemDO();
        saleItemDO.setItemTitle("test2");
        saleItemDO.setItemDesc("test2 desc");
        saleItemDO.setOriginalPrice(999L);
        saleItemDO.setSalePrice(99L);
        saleItemDO.setActivityId(3L);
        saleItemDO.setInitialStock(5);
        saleItemDO.setAvailableStock(5);
        saleItemDO.setStockWarmUp(0);
        saleItemDO.setStatus(0);
        saleItemDO.setStartTime(new Date());
        saleItemDO.setEndTime(new Date(System.currentTimeMillis() + 1000000));
        System.out.println(saleItemDO);

        int affectedRows = saleItemMapper.insertItem(saleItemDO);
        System.out.println(affectedRows);
        assertEquals(1, affectedRows);
        session.commit();
    }

    @Test
    void updateItemTest() {
        SaleItemDO saleItemDO = new SaleItemDO();
        saleItemDO.setId(1L);
        saleItemDO.setItemTitle("test9");
        saleItemDO.setItemDesc("test9 desc");
        saleItemDO.setOriginalPrice(888L);
        saleItemDO.setSalePrice(88L);
        saleItemDO.setActivityId(3L);
        saleItemDO.setInitialStock(5);
        saleItemDO.setAvailableStock(5);
        saleItemDO.setStockWarmUp(0);
        saleItemDO.setStatus(1);
        saleItemDO.setStartTime(new Date());
        saleItemDO.setEndTime(new Date(System.currentTimeMillis() + 1000000));
        System.out.println(saleItemDO);

        int affectedRows = saleItemMapper.updateItem(saleItemDO);
        System.out.println(affectedRows);
        assertEquals(1, affectedRows);
        session.commit();
    }

    @Test
    void getItemByIdTest() {
        SaleItemDO saleItemDO = saleItemMapper.getItemById(1L);
        System.out.println(saleItemDO);
        assertEquals("test9", saleItemDO.getItemTitle());
    }

    @Test
    void getItemsByConditionTest() {
        PageQueryCondition pageQueryCondition = new PageQueryCondition()
                .setKeyword("test")
                .setStatus(1)
                .validateParams();
        List<SaleItemDO> items = saleItemMapper.getItemsByCondition(pageQueryCondition);
        items.forEach(System.out::println);
        assertEquals(1, items.size());
    }

    @Test
    void countItemsByConditionTest() {
        PageQueryCondition pageQueryCondition = new PageQueryCondition().setKeyword("test");
        Integer count = saleItemMapper.countItemsByCondition(pageQueryCondition);
        System.out.println(count);
        assertEquals(2, count);
    }

    @Test
    void reduceStockByIdTest() {
        int affectedRows = saleItemMapper.reduceStockById(1L, 1);
        System.out.println(affectedRows);
        assertEquals(1, affectedRows);
        session.commit();
    }

    @Test
    void addStockByIdTest() {
        int affectedRows = saleItemMapper.addStockById(1L, 1);
        System.out.println(affectedRows);
        assertEquals(1, affectedRows);
        session.commit();
    }
}
