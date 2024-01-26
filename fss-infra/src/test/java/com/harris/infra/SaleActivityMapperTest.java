package com.harris.infra;

import com.harris.domain.model.PageQueryCondition;
import com.harris.infra.mapper.SaleActivityMapper;
import com.harris.infra.model.SaleActivityDO;
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

class SaleActivityMapperTest {
    private static SqlSessionFactory sqlSessionFactory;
    private SqlSession session;
    private SaleActivityMapper saleActivityMapper;

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
        saleActivityMapper = session.getMapper(SaleActivityMapper.class);
    }

    @Test
    void insertActivityTest() {
        SaleActivityDO saleActivityDO = new SaleActivityDO();
        saleActivityDO.setActivityName("test1");
        saleActivityDO.setActivityDesc("test1 desc");
        saleActivityDO.setStatus(1);
        saleActivityDO.setStartTime(new Date());
        saleActivityDO.setEndTime(new Date());
        System.out.println(saleActivityDO);

        int affectedRows = saleActivityMapper.insertActivity(saleActivityDO);
        System.out.println(affectedRows);
        assertEquals(1, affectedRows);
        session.commit();
    }

    @Test
    void updateActivityTest() {
        SaleActivityDO saleActivityDO = new SaleActivityDO();
        saleActivityDO.setId(3L);
        saleActivityDO.setActivityName("test2");
        saleActivityDO.setActivityDesc("test2 desc");
        saleActivityDO.setStatus(0);
        saleActivityDO.setStartTime(new Date());
        saleActivityDO.setEndTime(new Date());
        System.out.println(saleActivityDO);

        int affectedRows = saleActivityMapper.updateActivity(saleActivityDO);
        System.out.println(affectedRows);
        assertEquals(1, affectedRows);
        session.commit();
    }

    @Test
    void getActivityByIdTest() {
        SaleActivityDO saleActivityDO = saleActivityMapper.getActivityById(3L);
        System.out.println(saleActivityDO);
        assertEquals("test2", saleActivityDO.getActivityName());
    }

    @Test
    void getActivitiesByConditionTest() {
        PageQueryCondition pageQueryCondition = new PageQueryCondition()
                .setKeyword("test")
                .setStatus(0)
                .validateParams();
        List<SaleActivityDO> activities = saleActivityMapper.getActivitiesByCondition(pageQueryCondition);
        activities.forEach(System.out::println);
        assertEquals(1, activities.size());
    }

    @Test
    void countActivitiesByConditionTest() {
        PageQueryCondition pageQueryCondition = new PageQueryCondition().setKeyword("test");
        Integer count = saleActivityMapper.countActivitiesByCondition(pageQueryCondition);
        System.out.println(count);
        assertEquals(2, count);
    }
}
