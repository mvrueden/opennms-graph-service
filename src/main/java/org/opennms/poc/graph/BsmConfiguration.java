/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.poc.graph;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEdgeDao;
import org.opennms.netmgt.bsm.persistence.api.functions.map.MapFunctionDao;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.ReductionFunctionDao;
import org.opennms.netmgt.bsm.persistence.impl.BusinessServiceDaoImpl;
import org.opennms.netmgt.bsm.persistence.impl.BusinessServiceEdgeDaoImpl;
import org.opennms.netmgt.bsm.persistence.impl.functions.map.MapFunctionDaoImpl;
import org.opennms.netmgt.bsm.persistence.impl.functions.reduce.ReductionFunctionDaoImpl;
import org.opennms.netmgt.bsm.service.AlarmProvider;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.internal.AlarmProviderImpl;
import org.opennms.netmgt.bsm.service.internal.BusinessServiceManagerImpl;
import org.opennms.netmgt.bsm.service.internal.DefaultBusinessServiceStateMachine;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.hibernate.AlarmDaoHibernate;
import org.opennms.netmgt.dao.hibernate.GenericHibernateAccessor;
import org.opennms.netmgt.dao.hibernate.MonitoredServiceDaoHibernate;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.filter.JdbcFilterDao;
import org.opennms.netmgt.filter.api.FilterDao;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class BsmConfiguration {

    @Bean
    public BusinessServiceManager createServiceManager() {
        return new BusinessServiceManagerImpl();
    }

    @Bean
    public BusinessServiceStateMachine createStateMachine() {
        return new DefaultBusinessServiceStateMachine();
    }

    @Bean
    public AlarmProvider createAlarmProvider() {
        return new AlarmProviderImpl();
    }

    @Bean
    public AlarmDao createAlarmDao(SessionFactory sessionFactory) {
        final AlarmDaoHibernate daoHibernate =  new AlarmDaoHibernate();
        daoHibernate.setSessionFactory(sessionFactory);
        return daoHibernate;
    }

    @Bean
    public BusinessServiceDao createBusinessServiceDao(SessionFactory sessionFactory) {
        final BusinessServiceDaoImpl daoHibernate =  new BusinessServiceDaoImpl();
        daoHibernate.setSessionFactory(sessionFactory);
        return daoHibernate;
    }

    @Bean
    public BusinessServiceEdgeDao createBusinessServiceEdgeDao(SessionFactory sessionFactory) {
        final BusinessServiceEdgeDaoImpl daoHibernate =  new BusinessServiceEdgeDaoImpl();
        daoHibernate.setSessionFactory(sessionFactory);
        return daoHibernate;
    }

    @Bean
    public MonitoredServiceDao createMonitoredServiceDao(SessionFactory sessionFactory) {
        final MonitoredServiceDaoHibernate daoHibernate =  new MonitoredServiceDaoHibernate();
        daoHibernate.setSessionFactory(sessionFactory);
        return daoHibernate;
    }

    @Bean
    public MapFunctionDao createMapFunctionDao(SessionFactory sessionFactory) {
        final MapFunctionDaoImpl daoHibernate =  new MapFunctionDaoImpl();
        daoHibernate.setSessionFactory(sessionFactory);
        return daoHibernate;
    }

    @Bean
    public ReductionFunctionDao createReductionFunctionDao(SessionFactory sessionFactory) {
        final ReductionFunctionDaoImpl daoHibernate =  new ReductionFunctionDaoImpl();
        daoHibernate.setSessionFactory(sessionFactory);
        return daoHibernate;
    }

    @Bean
    public NodeDao createNodeDao(SessionFactory sessionFactory) {
        final NodeDaoHibernate daoHibernate =  new NodeDaoHibernate();
        daoHibernate.setSessionFactory(sessionFactory);
        return daoHibernate;
    }

    @Bean
    public FilterDao createFilterDao(DataSource dataSource) throws IOException {
        final JdbcFilterDao filterDao =  new JdbcFilterDao();
        filterDao.setDataSource(dataSource);
        filterDao.setDatabaseSchemaConfigFactory(new DatabaseSchemaConfigFactory());
        return filterDao;
    }

    @Bean
    public GenericPersistenceAccessor createPersistenceAccessor(SessionFactory sessionFactory) {
        final GenericHibernateAccessor accessor = new GenericHibernateAccessor();
        accessor.setSessionFactory(sessionFactory);
        return accessor;
    }

    @Bean
    public EventIpcManager createEventIpcManager() {
        return new MockEventIpcManager();
    }

    @Bean
    public FactoryBean<SessionFactory> createSessionFactory(DataSource dataSource) {
        final String[] packagesToScan = new String[] {
                "org.opennms.netmgt.model",
                "org.opennms.netmgt.dao.hibernate",
                "org.opennms.netmgt.dao.model",
                "org.opennms.netmgt.bsm.persistence.api",
                "org.opennms.poc.graph",
        };
        final AnnotationSessionFactoryBean sessionFactoryBean = new AnnotationSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setHibernateProperties(hibernateProperties());
        sessionFactoryBean.setAnnotatedPackages(packagesToScan);
        sessionFactoryBean.setPackagesToScan(packagesToScan);
        return sessionFactoryBean;
    }

    @Bean
    public PlatformTransactionManager hibernateTransactionManager(SessionFactory sessionFactory) {
        final HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory);
        return transactionManager;
    }

    private final Properties hibernateProperties() {
        Properties hibernateProperties = new Properties();
//        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "create");
        hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        hibernateProperties.setProperty("hibernate.jdbc.lob.non_contextual_creation", "true");
        hibernateProperties.setProperty("hibernate.show_sql", "true");
        hibernateProperties.setProperty("hibernate.format_sql", "true");
        return hibernateProperties;

//        hibernate.cache.use_query_cache=false
//        hibernate.cache.use_second_level_cache=false
//        hibernate.jdbc.batch_size=0
    }
}

