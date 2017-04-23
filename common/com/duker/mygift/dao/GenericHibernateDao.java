/*
 * GenericHibernateDao.java
 * 
 * Copyright @ 信息审核管理系统
 */
package com.duker.mygift.dao;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.IntegerType;
import org.hibernate.type.ObjectType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.duker.mygift.vo.CriteriaProperty;
import com.duker.mygift.vo.Page;
import com.duker.mygift.vo.PagedList;
import com.duker.mygift.vo.SortOrder;

/**
 * <pre>
 * 通用的、泛型的 HibernateDao类
 * 本 Dao 在对 My Eclispe 自动生成的 Dao方法进行提取的基础上, 做了增强, 以期简化数据访问层
 * 增强的功能包括:
 * 	1、多属性的联合查找(findByProperties), 免除在客户代码中构造hql可能造成的hql语法错误;
 * 	2、单个对象的查询(uniqueResultXXX), 简化在业务逻辑上只有唯一记录的对象查询;
 * 	3、hql的批量 插入/更新/删除, 支持hql语句(execute)和指定实体列表两种方式(deleteAll/saveOrUpdateAll);
 * 	4、数据分页, 对大数据量的数据集分页,要多少取多少;
 * 	5、原生态SQL的查找/执行, 支持SQL方式的操作;
 * 	6、支持命名查询,将hql语句配置到hbm.xml中,可以提高hql代码重用率;
 * 	7、支持使用二级缓存
 * 
 * @author wangzh
 * @version 1.6.9
 * 
 * 修改版本: 1.6.9
 * 修改日期：2014-12-05
 * 修改人 : wangzh
 * 修改说明：
 * 	1、增加缓存分页count总结果数的功能
 * 
 * 修改版本: 1.6.8
 * 修改日期：2010-08-19
 * 修改人 : wangzh
 * 修改说明：
 * 	1、修改findByCriterion/uniqueResultByCriterion方法,Projection投影和ResultTransformer结果转换的功能
 * 
 * 修改版本: 1.6.7
 * 修改日期：2010-05-24
 * 修改人 : wangzh
 * 修改说明：
 * 	1、增加findTopBy***方法,返回第N条数据
 * 
 * 修改版本: 1.6.6
 * 修改日期：2010-03-25
 * 修改人 : wangzh
 * 修改说明：
 * 	1、修改***ByCriterion方法,把原来的Map&lt;String, FetchMode&gt; fetchModes参数改为List&lt;CriteriaProperty&gt; properties
 * 	以同时支持关联属性别名和调整抓取策略
 *  
 * 修改版本: 1.6.5
 * 修改日期：2009-06-25
 * 修改人 : wangzh
 * 修改说明：
 * 	1、修改分页方法,，以pageNo,pageSize以分页参数,返回PagedList对象
 * 	2、使用hql和sql分页方法也支持排序
 * 
 * 修改版本: 1.6.4
 * 修改日期：2009-02-18
 * 修改人 : wangzh
 * 修改说明：
 * 	1、批量删除、修改、保存时每batchSize条清空一次数据库，防止大量数据提交时内存溢出
 * 
 * 修改版本: 1.6.3
 * 修改日期：2008-11-21
 * 修改人 : wangzh
 * 修改说明：
 * 	1、修改page,findByNamedQuery,uniqueResultByNamedQuery方法，以Object数组形式传值
 * 
 * 修改版本: 1.6.2
 * 修改日期：2008-11-13
 * 修改人 : wangzh
 * 修改说明：
 * 	1、增加使用Criterion取得唯一值系列方法uniqueResultByCriterion
 * 	2、修改page系列方法，增加resultCount <= page.getFirstResult()判断，在取不得数据的情况下直接返回
 * 	3、删除findByQueryExample一系列方法
 * 
 * 修改版本: 1.6.1
 * 修改日期：2008-11-13
 * 修改人 : wangzh
 * 修改说明：
 * 	1、增加pageBySQL分页方法，支持使用SQL语句的分页查询
 * 
 * 修改版本: 1.6.0
 * 修改日期：2008-10-21
 * 修改人 : wangzh
 * 修改说明：
 * 	1、修改二义性方法find(String, Object...values)与find(String, String, Object...values)
 * 	uniqueResultCached(String, Object...values)与uniqueResultCached(String, String,  Object...values)
 * 	这两个方法在传入两个String值时会产生二义性
 * 
 * 修改版本: 1.5.0
 * 修改日期：2008-10-16
 * 修改人 : wangzh
 * 修改说明：
 * 	1、增加使用二级缓存查询方法
 * 
 * 修改版本: 1.4.0
 * 修改日期：2008-8-19
 * 修改人 : wangzh
 * 修改说明：
 * 	1、增加支持NamedQuery的方式的查询,使hql语句可以配置到hbm.xml文件中,提高hql代码重用率
 * 
 * 修改版本: 1.3.0
 * 修改日期：2008-8-18
 * 修改人 : wangzh
 * 修改说明：
 * 	1、增加构造方法,使hibernateTemplate/sessionFactory可以使用构造方法注入
 * 	2、重载persist/update/save/delete等方法
 * 
 * 修改版本: 1.2.0
 * 修改日期：2008-8-8
 * 修改人 : wangzh
 * 修改说明：
 * 	1、修改按单个属性和多个属性返回唯一对象的方法名
 * 
 * 修改版本: 1.1.0
 * 修改日期：2008-8-5
 * 修改人 : wangzh
 * 修改说明：
 * 	1、增加多个 uniqueResult 方法,以方便单条数据查询
 * 	2、增加 SessionFactory 的注入 HibernateTemplate 的方法 
 * 
 * 修改版本: 1.0.0
 * 修改日期：2008-5-14
 * 修改人 : wangzh
 * 修改说明：形成初始版本
 * </pre>
 */
@SuppressWarnings("unchecked")
public class GenericHibernateDao {

	/** The PROXY class separator character "$$" */
	private static final String PROXY_CLASS_SEPARATOR = "$$";

	/**
	 * Hibernate SessionFactory
	 */
	private SessionFactory sessionFactory;

	/**
	 * 分页总数缓存,减少count次数
	 */
	private PageCache pageCache;

	/**
	 * 是否排除不稳定排序，增加按主键排序
	 */
	private ThreadLocal<Boolean> orderById = new ThreadLocal<Boolean>() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.ThreadLocal#initialValue()
		 */
		protected Boolean initialValue() {
			return true;
		}
	};

	/**
	 * 默认的 GenericHibernateDao 构造方法
	 */
	public GenericHibernateDao() {
	}

	/**
	 * 使用 SessionFactory 来实例化一个 GenericHibernateDao
	 * 
	 * @param sessionFactory
	 *            Hibernate会话工厂类
	 */
	public GenericHibernateDao(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * 设置二级缓存
	 * 
	 * @param cacheMode
	 *            二级缓存模式
	 */
	public void setCacheMode(CacheMode cacheMode) {
		Session session = sessionFactory.getCurrentSession();
		session.setCacheMode(cacheMode);
	}

	/**
	 * 清空一级缓存,重新打开一个新的会话
	 * 
	 */
	public void clear() {
		Session session = sessionFactory.getCurrentSession();
		session.clear();
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager
				.unbindResource(sessionFactory);
		SessionFactoryUtils.closeSession(sessionHolder.getSession());
		try {
			session = sessionFactory.openSession();
			session.setFlushMode(FlushMode.MANUAL);
			TransactionSynchronizationManager.bindResource(sessionFactory,
					new SessionHolder(session));
		}
		catch (HibernateException ex) {
			throw new DataAccessResourceFailureException(
					"Could not open Hibernate Session", ex);
		}
	}

	/**
	 * 查询session cache(一级缓存)中是否包含此对象
	 * 
	 * @param entity
	 *            实体
	 * @return true存在,false不存在
	 */
	public boolean contains(Object entity) {
		return sessionFactory.getCurrentSession().contains(entity);
	}

	/**
	 * 删除一个处于persistent状态的对象, 对应数据库的delete. 处于persistent状态的对象对应数据库的一条记录,
	 * 与某个session关联,并处于session的有效期内
	 * 
	 * @param persistentEntity
	 *            处于persistent状态的对象.
	 */
	public void delete(Object persistentEntity) {
		sessionFactory.getCurrentSession().delete(persistentEntity);
	}

	/**
	 * 删除一个持久化状态的对象
	 * 
	 * @param persistentEntity
	 *            持久态对象
	 * @param lockOptions
	 *            锁模式
	 */
	public void delete(Object persistentEntity, LockOptions lockOptions) {
		Session session = sessionFactory.getCurrentSession();
		if (lockOptions != null) {
			session.buildLockRequest(lockOptions).lock(persistentEntity);
		}
		session.delete(persistentEntity);
	}

	/**
	 * 删除一个持久化状态的对象
	 * 
	 * @param entityName
	 *            实体名
	 * @param persistentEntity
	 *            持久态对象
	 */
	public void delete(String entityName, Object persistentEntity) {
		sessionFactory.getCurrentSession().delete(entityName, persistentEntity);
	}

	/**
	 * 删除一个持久化对象
	 * 
	 * @param entityName
	 *            实体名
	 * @param persistentEntity
	 *            持久态对象
	 * @param lockOptions
	 *            锁模式
	 */
	public void delete(String entityName, Object persistentEntity,
			LockOptions lockOptions) {
		Session session = sessionFactory.getCurrentSession();
		if (lockOptions != null) {
			session.buildLockRequest(lockOptions).lock(entityName,
					persistentEntity);
		}
		session.delete(entityName, persistentEntity);
	}

	/**
	 * 删除集合中的所有对象
	 * 
	 * @param entities
	 *            持久化对象集合
	 */
	public void deleteAll(Collection<?> entities) {
		Session session = sessionFactory.getCurrentSession();
		// 禁用二级缓存
		session.setCacheMode(CacheMode.IGNORE);
		for (Object entity : entities) {
			session.delete(entity);
		}
	}

	/**
	 * 把对象从session cache(一级缓存)中移除
	 * 
	 * @param entity
	 *            实体
	 */
	public void evict(Object entity) {
		sessionFactory.getCurrentSession().evict(entity);
	}

	/**
	 * <pre>
	 * 根据指定的hql语句和参数列表，执行delete/update操作的hql语句，返回受影响的行数
	 * <b>注意:</b>  Hibernate没有解决批量delete/update的缓存问题, 无法保证缓存数据的一致有效性
	 * 
	 * 	示例：
	 * 		String hql = "update Cust as cust set cust.logonPassword = ? where cust.id = ?";
	 * 		dao.execute(hql, "123456", 1L);
	 * </pre>
	 * 
	 * @param hql
	 *            动态hql.
	 * @param values
	 *            参数列表
	 * @return int 受影响行数
	 */
	public int execute(String hql, Object... values) {
		Session session = sessionFactory.getCurrentSession();
		Query queryObject = session.createQuery(getNamedHql(hql, values));
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(Integer.toString(i), values[i]);
			}
		}

		return queryObject.executeUpdate();
	}

	/**
	 * 获取JPA-style hql
	 * 
	 * @param hql
	 *            position based hql
	 * @param values
	 *            参数
	 * @return name based hql
	 */
	private String getNamedHql(String hql, Object... values) {
		if (values == null || hql.contains("?0")) {
			return hql;
		}

		StringBuilder sb = new StringBuilder();
		int start = 0;
		for (int i = 0; i < values.length; i++) {
			int end = hql.indexOf('?', start);
			sb.append(hql.substring(start, end));
			sb.append('?');
			sb.append(i);

			start = end + 1;
		}
		sb.append(hql.substring(start, hql.length()));

		return sb.toString();
	}

	/**
	 * <pre>
	 * 根据指定的sql语句和sql参数列表，执行insert/delete/update操作的sql语句，返回受影响的行数
	 * 	示例：
	 * 		String sql = "update cust set name = ?, password = ? where id = ?"
	 * 		dao.executeSQL(sql, "007", "123456", 1);
	 * </pre>
	 * 
	 * @param sql
	 *            sql语句
	 * @param values
	 *            sql参数列表
	 * @return int 受影响的行数
	 */
	public int executeSQL(String sql, Object... values) {
		sql = getNamedHql(sql, values);
		Session session = sessionFactory.getCurrentSession();
		SQLQuery queryObject = session.createSQLQuery(sql);

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(Integer.toString(i), values[i]);
			}
		}

		return queryObject.executeUpdate();
	}

	/**
	 * <pre>
	 * 根据指定的queryName和参数列表，执行无返回值存储过程，返回受影响的行数
	 * 	示例：
	 * 		dao.executeNamedQuery("KEY_BUILD_VIDEO_FULL_TEXT");
	 * </pre>
	 * 
	 * @param queryName
	 *            查询名
	 * @param values
	 *            命名查询参数列表
	 * @return int 受影响的行数
	 */
	public int executeNamedQuery(String queryName, Object... values) {
		Session session = sessionFactory.getCurrentSession();
		Query namedQuery = session.getNamedQuery(queryName);
		String sql = namedQuery.getQueryString();
		String[] ss = sql.split("\\s*;\\s*");
		int ret = 0;
		for (String s : ss) {
			s = getNamedHql(s.trim(), values);
			SQLQuery query = session.createSQLQuery(s);
			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					query.setParameter(Integer.toString(i), values[i]);
				}
			}
			ret += query.executeUpdate();
		}

		return ret;
	}

	/**
	 * 生成新的主键id，但并不产生数据库记录
	 * 
	 * @param clz
	 *            实体类
	 * @return 主键id
	 */
	public Serializable createIdentifier(Class<?> clz) {
		try {
			Object obj = clz.newInstance();
			Session session = sessionFactory.getCurrentSession();
			session.save(obj);
			Serializable id = session.getIdentifier(obj);
			session.delete(obj);

			return id;
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * 获取对象的id
	 * 
	 * @param object
	 *            对象
	 * @return 对象id
	 */
	public Serializable getIdentifier(Object object) {
		Session session = sessionFactory.getCurrentSession();
		return session.getIdentifier(object);
	}

	/**
	 * 获取对象的id属性名
	 * 
	 * @param object
	 *            对象
	 * @return 对象id属性名
	 */
	public String getIdentifierPropertyName(Object object) {
		Class<?> clazz = getUserClass(object);
		ClassMetadata meta = sessionFactory.getClassMetadata(clazz);
		return meta.getIdentifierPropertyName();
	}

	/**
	 * 获取代理对象的非代理类
	 * 
	 * @param object
	 *            代理对象
	 * @return 非代理类
	 */
	public static Class<?> getUserClass(Object object) {
		return getUserClass(object.getClass());
	}

	/**
	 * 获取代理对象的非代理类
	 * 
	 * @param clazz
	 *            代理对象的类
	 * @return 非代理类
	 */
	public static Class<?> getUserClass(Class<?> clazz) {
		if (clazz != null
				&& (clazz.getName().contains(PROXY_CLASS_SEPARATOR) || clazz
						.getName()
						.startsWith("org.hibernate"))) {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass != null && !Object.class.equals(superClass)) {
				return superClass;
			}
		}
		return clazz;
	}

	/**
	 * <pre>
	 * 根据指定的<b>hql</b>和参数列表，返回查找的结果
	 * 示例：
	 * 		String hql = "from Cust as cust where cust.name like ? ";
	 * 		List&lt;Cust&gt; custs = dao.find(hql, "%007%");
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param hql
	 *            hql语句
	 * @param values
	 *            参数列表
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> find(String hql, Object... values) {
		Session session = sessionFactory.getCurrentSession();
		Query queryObject = session.createQuery(getNamedHql(hql, values));
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(Integer.toString(i), values[i]);
			}
		}

		return queryObject.list();
	}

	/**
	 * <pre>
	 * 返回实体类的所有对象
	 * 由于一次性将返回表的全部记录，因此对于大数据量的库表不能使用此方法
	 * 	示例：
	 * 		List&lt;Cust&gt; custs = dao.findAll(Cust.class);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类，如 Cust.class
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findAll(Class<T> entityClass) {
		return this.<T> find("from " + entityClass.getName());
	}

	/**
	 * <pre>
	 * 返回实体类的所有对象
	 * 由于一次性将返回表的全部记录，因此对于大数据量的库表不能使用此方法
	 * 	示例：
	 * 		List&lt;Cust&gt; custs = dao.findAll("Cust");
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名，如"Cust"
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findAll(String entityName) {
		return this.<T> find("from " + entityName);
	}

	/**
	 * 使用二级缓存查找所有实例
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityClass
	 *            实体类
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findAll(String cacheRegion, Class<T> entityClass) {
		return this
				.<T> findCached(cacheRegion, "from " + entityClass.getName());
	}

	/**
	 * 使用二级缓存查找所有实例
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityName
	 *            实体名
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findAll(String cacheRegion, String entityName) {
		return this.<T> findCached(cacheRegion, "from " + entityName);
	}

	/**
	 * 根据指定的实体类、Criterion、Order进行查询
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            hibernate查询标准
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByCriterion(Class<T> entityClass, Criterion criterion) {
		return this.<T> findByCriterion(entityClass.getName(), criterion);
	}

	/**
	 * 根据指定的实体类、Criterion、Order进行查询
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            hibernate查询标准
	 * @param orders
	 *            排序对象,可为空
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByCriterion(Class<T> entityClass,
			Criterion criterion, List<SortOrder> orders) {
		return this.<T> findByCriterion(entityClass.getName(), criterion,
				orders);
	}

	/**
	 * <pre>
	 * 根据指定的实体类、Criterion、Order进行查询
	 * 	示例：
	 * 		Criterion c1 = null;
	 *		Criterion c2 = null;
	 *		Criterion c3 = null;
	 *		Conjunction conjunction = null;
	 *
	 *		c1 = Restrictions.eq("name", "007");
	 *		c2 = Restrictions.eq("age", "20");
	 *		c3 = Restrictions.between("birthday", beginDate, endDate);
	 *		conjunction = Restrictions.conjunction();
	 *		conjunction.add(c1).add(c2).add(c3);
	 *
	 *		List&lt;Order&gt; orders = new ArrayList&lt;Order&gt;();
	 *		order.add(SortOrder.asc("id"));
	 *		List&lt;CriteriaProperty&gt; properties = new ArrayList&lt;CriteriaProperty&gt;();
	 *		properties.add(new CriteriaProperty("user", FetchMode.JOIN));
	 *
	 * 		List&lt;Cust&gt; custs = dao.findByCriterion(Cust.class, conjunction, orders, properties);
	 * 		//List&lt;Cust&gt; custs = dao.findByCriterion(Cust.class, conjunction, null, null);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            hibernate查询标准
	 * @param orders
	 *            排序对象,可为空
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByCriterion(Class<T> entityClass,
			Criterion criterion, List<SortOrder> orders,
			List<CriteriaProperty> properties) {
		return this.<T> findByCriterion(entityClass.getName(), criterion,
				orders, properties);
	}

	/**
	 * 根据指定的实体类、Criterion、Order进行查询
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            hibernate查询标准
	 * @param orders
	 *            排序对象,可为空
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @param projection
	 *            查询结果投影,可为空
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByCriterion(Class<T> entityClass,
			Criterion criterion, List<SortOrder> orders,
			List<CriteriaProperty> properties, Projection projection,
			ResultTransformer transformer) {
		return this.<T> findByCriterion(entityClass.getName(), criterion,
				orders, properties, projection, transformer);
	}

	/**
	 * 使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            　hibernate查询标准
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByCriterion(String cacheRegion,
			Class<T> entityClass, Criterion criterion) {
		return this.<T> findByCriterion(cacheRegion, entityClass.getName(),
				criterion);
	}

	/**
	 * 使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            　hibernate查询标准
	 * @param orders
	 *            排序对象,可为空
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByCriterion(String cacheRegion,
			Class<T> entityClass, Criterion criterion, List<SortOrder> orders) {
		return this.<T> findByCriterion(cacheRegion, entityClass.getName(),
				criterion, orders);
	}

	/**
	 * 使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            　hibernate查询标准
	 * @param orders
	 *            排序对象,可为空
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByCriterion(String cacheRegion,
			Class<T> entityClass, Criterion criterion, List<SortOrder> orders,
			List<CriteriaProperty> properties) {
		return this.<T> findByCriterion(cacheRegion, entityClass.getName(),
				criterion, orders, properties);
	}

	/**
	 * 使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            　hibernate查询标准
	 * @param orders
	 *            排序对象,可为空
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @param projection
	 *            查询结果投影,可为空
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByCriterion(String cacheRegion,
			Class<T> entityClass, Criterion criterion, List<SortOrder> orders,
			List<CriteriaProperty> properties, Projection projection,
			ResultTransformer transformer) {
		return this.<T> findByCriterion(cacheRegion, entityClass.getName(),
				criterion, orders, properties, projection, transformer);
	}

	/**
	 * 根据指定的实体名、Criterion、Order对象，进行查询
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByCriterion(String entityName, Criterion criterion) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		return criteria.list();
	}

	/**
	 * 根据指定的实体名、Criterion、Order对象，进行查询
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByCriterion(String entityName, Criterion criterion,
			List<SortOrder> orders) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		if (orders != null) {
			for (Order order : orders) {
				criteria.addOrder(order);
			}
		}

		return criteria.list();
	}

	/**
	 * 根据指定的实体名、Criterion、Order对象，进行查询
	 * 
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByCriterion(String entityName, Criterion criterion,
			List<SortOrder> orders, List<CriteriaProperty> properties) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		if (properties != null) {
			for (CriteriaProperty property : properties) {
				String prop = property.getProp();
				String alias = property.getAlias();
				JoinType joinType = property.getJoinType();
				FetchMode fetchMode = property.getFetchMode();

				if (StringUtils.isNotBlank(alias)) {
					if (joinType == null) {
						criteria.createAlias(prop, alias);
					}
					else {
						criteria.createAlias(prop, alias, joinType);
					}
				}
				if (fetchMode != null) {
					criteria.setFetchMode(prop, fetchMode);
				}
			}
		}

		if (orders != null) {
			for (Order order : orders) {
				criteria.addOrder(order);
			}
		}

		return criteria.list();
	}

	/**
	 * 根据指定的实体名、Criterion、Order对象，进行查询
	 * 
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @param projection
	 *            查询结果投影,可为空
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByCriterion(String entityName, Criterion criterion,
			List<SortOrder> orders, List<CriteriaProperty> properties,
			Projection projection, ResultTransformer transformer) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		if (properties != null) {
			for (CriteriaProperty property : properties) {
				String prop = property.getProp();
				String alias = property.getAlias();
				JoinType joinType = property.getJoinType();
				FetchMode fetchMode = property.getFetchMode();

				if (StringUtils.isNotBlank(alias)) {
					if (joinType == null) {
						criteria.createAlias(prop, alias);
					}
					else {
						criteria.createAlias(prop, alias, joinType);
					}
				}
				if (fetchMode != null) {
					criteria.setFetchMode(prop, fetchMode);
				}
			}
		}

		if (projection != null) {
			criteria.setProjection(projection);
		}

		if (transformer != null) {
			criteria.setResultTransformer(transformer);
		}

		if (orders != null) {
			for (Order order : orders) {
				criteria.addOrder(order);
			}
		}

		return criteria.list();
	}

	/**
	 * 使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByCriterion(String cacheRegion, String entityName,
			Criterion criterion) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		criteria.setCacheable(true);
		criteria.setCacheRegion(cacheRegion);

		return criteria.list();
	}

	/**
	 * 使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByCriterion(String cacheRegion, String entityName,
			Criterion criterion, List<SortOrder> orders) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		criteria.setCacheable(true);
		criteria.setCacheRegion(cacheRegion);

		if (orders != null) {
			for (Order order : orders) {
				criteria.addOrder(order);
			}
		}

		return criteria.list();
	}

	/**
	 * 使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByCriterion(String cacheRegion, String entityName,
			Criterion criterion, List<SortOrder> orders,
			List<CriteriaProperty> properties) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		if (properties != null) {
			for (CriteriaProperty property : properties) {
				String prop = property.getProp();
				String alias = property.getAlias();
				JoinType joinType = property.getJoinType();
				FetchMode fetchMode = property.getFetchMode();

				if (StringUtils.isNotBlank(alias)) {
					if (joinType == null) {
						criteria.createAlias(prop, alias);
					}
					else {
						criteria.createAlias(prop, alias, joinType);
					}
				}
				if (fetchMode != null) {
					criteria.setFetchMode(prop, fetchMode);
				}
			}
		}

		criteria.setCacheable(true);
		criteria.setCacheRegion(cacheRegion);

		if (orders != null) {
			for (Order order : orders) {
				criteria.addOrder(order);
			}
		}

		return criteria.list();
	}

	/**
	 * 使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @param projection
	 *            查询结果投影,可为空
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByCriterion(String cacheRegion, String entityName,
			Criterion criterion, List<SortOrder> orders,
			List<CriteriaProperty> properties, Projection projection,
			ResultTransformer transformer) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		if (properties != null) {
			for (CriteriaProperty property : properties) {
				String prop = property.getProp();
				String alias = property.getAlias();
				JoinType joinType = property.getJoinType();
				FetchMode fetchMode = property.getFetchMode();

				if (StringUtils.isNotBlank(alias)) {
					if (joinType == null) {
						criteria.createAlias(prop, alias);
					}
					else {
						criteria.createAlias(prop, alias, joinType);
					}
				}
				if (fetchMode != null) {
					criteria.setFetchMode(prop, fetchMode);
				}
			}
		}

		if (projection != null) {
			criteria.setProjection(projection);
		}

		if (transformer != null) {
			criteria.setResultTransformer(transformer);
		}

		criteria.setCacheable(true);
		criteria.setCacheRegion(cacheRegion);

		if (orders != null) {
			for (Order order : orders) {
				criteria.addOrder(order);
			}
		}

		return criteria.list();
	}

	/**
	 * 使用Hibernate的org.hibernate.criterion.Example来进行多条件的复合查询
	 * 
	 * @param <T>
	 *            实体类型
	 * @param exampleEntity
	 *            　实体对象
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByExample(Object exampleEntity) {
		String entityName = exampleEntity.getClass().getName();
		Criterion criterion = Example.create(exampleEntity);

		return this.<T> findByCriterion(entityName, criterion);
	}

	/**
	 * 使用Hibernate的org.hibernate.criterion.Example来进行多条件的复合查询,对结果按org.hibernate.
	 * criterion.Order进行排序
	 * 
	 * @param <T>
	 *            实体类型
	 * @param exampleEntity
	 *            　实体对象
	 * @param orders
	 *            排序对象,可为空
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByExample(Object exampleEntity,
			List<SortOrder> orders) {
		String entityName = exampleEntity.getClass().getName();
		Criterion criterion = Example.create(exampleEntity);

		return this.<T> findByCriterion(entityName, criterion, orders);
	}

	/**
	 * <pre>
	 * 使用Hibernate的org.hibernate.criterion.Example来进行多条件的复合查询,并对结果进行排序,可以指定抓取策略
	 * 	示例：
	 * 		Cust cust = new Cust();
	 * 		cust.setName("007");
	 * 		cust.setAge(20);
	 *		List&lt;CriteriaProperty&gt; properties = new ArrayList&lt;CriteriaProperty&gt;();
	 *		properties.add(new CriteriaProperty("user", FetchMode.JOIN));
	 * 		List&lt;Cust&gt; custs = dao.findByExample(cust, properties);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param exampleEntity
	 *            　实体对象
	 * @param orders
	 *            排序对象,可为空
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByExample(Object exampleEntity,
			List<SortOrder> orders, List<CriteriaProperty> properties) {
		String entityName = exampleEntity.getClass().getName();
		Criterion criterion = Example.create(exampleEntity);

		return this.<T> findByCriterion(entityName, criterion, orders,
				properties);
	}

	/**
	 * 使用Hibernate的org.hibernate.criterion.Example来进行多条件的复合查询,并对结果进行排序,可以指定抓取策略
	 * 
	 * @param <T>
	 *            实体类型
	 * @param exampleEntity
	 *            　实体对象
	 * @param orders
	 *            排序对象,可为空
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @param projection
	 *            查询结果投影,可为空
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByExample(Object exampleEntity,
			List<SortOrder> orders, List<CriteriaProperty> properties,
			Projection projection, ResultTransformer transformer) {
		String entityName = exampleEntity.getClass().getName();
		Criterion criterion = Example.create(exampleEntity);

		return this.<T> findByCriterion(entityName, criterion, orders,
				properties, projection, transformer);
	}

	/**
	 * 使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param exampleEntity
	 *            　实体对象
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByExample(String cacheRegion, Object exampleEntity) {
		String entityName = exampleEntity.getClass().getName();
		Criterion criterion = Example.create(exampleEntity);

		return this.<T> findByCriterion(cacheRegion, entityName, criterion);
	}

	/**
	 * 使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param exampleEntity
	 *            　实体对象
	 * @param orders
	 *            排序对象,可为空
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByExample(String cacheRegion, Object exampleEntity,
			List<SortOrder> orders) {
		String entityName = exampleEntity.getClass().getName();
		Criterion criterion = Example.create(exampleEntity);

		return this.<T> findByCriterion(cacheRegion, entityName, criterion,
				orders);
	}

	/**
	 * 使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param exampleEntity
	 *            　实体对象
	 * @param orders
	 *            排序对象,可为空
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByExample(String cacheRegion, Object exampleEntity,
			List<SortOrder> orders, List<CriteriaProperty> properties) {
		String entityName = exampleEntity.getClass().getName();
		Criterion criterion = Example.create(exampleEntity);

		return this.<T> findByCriterion(cacheRegion, entityName, criterion,
				orders, properties);
	}

	/**
	 * 使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param exampleEntity
	 *            　实体对象
	 * @param orders
	 *            排序对象,可为空
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @param projection
	 *            查询结果投影,可为空
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByExample(String cacheRegion, Object exampleEntity,
			List<SortOrder> orders, List<CriteriaProperty> properties,
			Projection projection, ResultTransformer transformer) {
		String entityName = exampleEntity.getClass().getName();
		Criterion criterion = Example.create(exampleEntity);

		return this.<T> findByCriterion(cacheRegion, entityName, criterion,
				orders, properties, projection, transformer);
	}

	/**
	 * <pre>
	 * 按主键来查找对象
	 * 如果指定id的对象不存在,则返回null
	 * 	示例：
	 * 		Cust cust = dao.findById(Cust.class, 1L);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类, 如 Cust.class
	 * @param id
	 *            主键值
	 * @return T 实体
	 */
	public <T> T findById(Class<T> entityClass, Serializable id) {
		Session session = sessionFactory.getCurrentSession();

		return (T) session.get(entityClass, id);
	}

	/**
	 * <pre>
	 * 按主键来查找对象,同时加锁
	 * 如果指定id的对象不存在,则返回null
	 * 	示例：
	 * 		Cust cust = dao.findById(Cust.class,1L, LockOptions.READ);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类, 如 Cust.class
	 * @param id
	 *            主键值
	 * @param lockOptions
	 *            锁模式
	 * @return T 实体
	 */
	public <T> T findById(Class<T> entityClass, Serializable id,
			LockOptions lockOptions) {
		Session session = sessionFactory.getCurrentSession();

		if (lockOptions != null) {
			return (T) session.get(entityClass, id, lockOptions);
		}
		return (T) session.get(entityClass, id);
	}

	/**
	 * <pre>
	 * 按主键来查询对象
	 * 如果指定id的对象不存在,则返回null
	 * 	示例：
	 * 		Cust cust = dao.findById("Cust", 1L);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名，如 "Cust"
	 * @param id
	 *            主键值
	 * @return T 实体
	 */
	public <T> T findById(String entityName, Serializable id) {
		Session session = sessionFactory.getCurrentSession();

		return (T) session.get(entityName, id);
	}

	/**
	 * <pre>
	 * 按主键来查询对象,同时加锁
	 * 如果指定id的对象不存在,则返回null
	 * 	示例：
	 * 		Cust cust = dao.findById("Cust", 1L, LockOptions.READ);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名，如 "Cust"
	 * @param id
	 *            主键值
	 * @param lockOptions
	 *            锁模式
	 * @return T 实体
	 */
	public <T> T findById(String entityName, Serializable id,
			LockOptions lockOptions) {
		Session session = sessionFactory.getCurrentSession();

		if (lockOptions != null) {
			return (T) session.get(entityName, id, lockOptions);
		}
		return (T) session.get(entityName, id);
	}

	/**
	 * 按主键来查找对象，使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类, 如 Cust.class
	 * @param id
	 *            主键值
	 * @return T 实体
	 */
	public <T> T findByIdCached(Class<T> entityClass, Serializable id) {
		return this.<T> findByIdCached(entityClass, id, null);
	}

	/**
	 * 按主键来查找对象,使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类, 如 Cust.class
	 * @param id
	 *            主键值
	 * @param lockOptions
	 *            锁模式h
	 * @return T 实体
	 */
	public <T> T findByIdCached(Class<T> entityClass, Serializable id,
			LockOptions lockOptions) {
		Session session = sessionFactory.getCurrentSession();

		if (lockOptions != null) {
			try {
				Object obj = session.load(entityClass, id, lockOptions);

				obj.toString();

				return (T) obj;
			}
			catch (Exception ex) {
				return null;
			}
		}
		try {
			Object obj = session.load(entityClass, id);

			obj.toString();

			return (T) obj;
		}
		catch (Exception ex) {
			return null;
		}
	}

	/**
	 * 按主键来查询对象,使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名，如 "Cust"
	 * @param id
	 *            主键值
	 * @return T 实体
	 */
	public <T> T findByIdCached(String entityName, Serializable id) {
		return this.<T> findByIdCached(entityName, id, null);
	}

	/**
	 * 按主键来查询对象,使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名，如 "Cust"
	 * @param id
	 *            主键值
	 * @param lockOptions
	 *            锁模式
	 * @return T 实体
	 */
	public <T> T findByIdCached(String entityName, Serializable id,
			LockOptions lockOptions) {
		Session session = sessionFactory.getCurrentSession();

		if (lockOptions != null) {
			try {
				Object obj = session.load(entityName, id, lockOptions);

				obj.toString();

				return (T) obj;
			}
			catch (Exception ex) {
				return null;
			}
		}
		try {
			Object obj = session.load(entityName, id);

			obj.toString();

			return (T) obj;
		}
		catch (Exception ex) {
			return null;
		}
	}

	/**
	 * <pre>
	 * 使用命名查询
	 * 	示例:
	 * 		List&lt;Cust&gt; custs = dao.findByNamedQuery("queryCustByNameAndAge", "007", 21);
	 * 	其中,queryCustByNameAndAge 在 hbm.xml 文件定义,示例:
	 * 		&lt;hibernate-mapping>
	 * 			&lt;class name="com.duker.mygift.model.Cust" table="CUST">
	 * 				&lt;id name="id" type="java.lang.Long">
	 * 					&lt;column name="ID" precision="10" scale="0" />
	 * 					&lt;generator class="assigned" />
	 * 				&lt;/id>
	 * 				&lt;property name="name" type="java.lang.String">
	 * 					&lt;column name="NAME" length="50" />
	 *				&lt;/property>
	 *				&lt;property name="age" type="java.lang.Integer">
	 *					&lt;column name="AGE"/>
	 *				&lt;/property>
	 *				&lt;query name="queryCustByNameAndAge">
	 *					&lt;![CDATA[
	 *						from Cust cust where cust.name = ? and cust.age = ?
	 *					]]>
	 *				&lt;/query>
	 *			&lt;/class>
	 *		&lt;/hibernate-mapping>
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param queryName
	 *            查询名
	 * @param values
	 *            参数值
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByNamedQuery(String queryName, Object... values) {
		Session session = sessionFactory.getCurrentSession();
		Query namedQuery = session.getNamedQuery(queryName);

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				namedQuery.setParameter(Integer.toString(i), values[i]);
			}
		}

		return namedQuery.list();
	}

	/**
	 * 使用命名查询，支持二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param queryName
	 *            查询名
	 * @param values
	 *            参数值
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByNamedQueryCached(String cacheRegion,
			String queryName, Object... values) {
		Session session = sessionFactory.getCurrentSession();
		Query namedQuery = session.getNamedQuery(queryName);

		namedQuery.setCacheable(true);
		namedQuery.setCacheRegion(cacheRegion);

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				namedQuery.setParameter(Integer.toString(i), values[i]);
			}
		}

		return namedQuery.list();
	}

	/**
	 * <pre>
	 * 根据指定的实体类、以属性名(key)属性值(value)的Map, 以多个属性为条件来查询 
	 * 	示例：
	 * 		Map<String, Object> propertities = new LinkedHashMap<String, Object>(3);
	 * 		propertities.put("name", "007");
	 * 		propertities.put("sex", 1);
	 * 		propertities.put("age", 30);
	 * 		List&lt;Cust&gt; custs = dao.findByProperties(Cust.class, propertities);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param propertities
	 *            名值对
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByProperties(Class<T> entityClass,
			Map<String, Object> propertities) {
		return this.<T> findByProperties(entityClass.getName(), propertities);
	}

	/**
	 * <pre>
	 * 根据指定的实体类、属性名列表、属性值列表, 以多个属性为条件来查询
	 * 注意：必须保证属性名列表、属性值列表中的属性名-值的对应关系
	 * 	示例：
	 * 		String[] propertyNames = {"name", "sex", "age"};
	 * 		Object[] propertyValues = {"007", 1, 30};
	 * 		List&lt;Cust&gt; custs = dao.findByProperties(Cust.class, propertyNames, propertyValues);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param propertyNames
	 *            属性数组
	 * @param propertyValues
	 *            值数组
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByProperties(Class<T> entityClass,
			String[] propertyNames, Object[] propertyValues) {
		return this.<T> findByProperties(entityClass.getName(), propertyNames,
				propertyValues);
	}

	/**
	 * 根据指定的实体类、以属性名(key)属性值(value)的Map, 以多个属性为条件来查询，使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityClass
	 *            实体类
	 * @param propertities
	 *            名值对
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByProperties(String cacheRegion,
			Class<T> entityClass, Map<String, Object> propertities) {
		return this.<T> findByProperties(cacheRegion, entityClass.getName(),
				propertities);
	}

	/**
	 * 根据指定的实体类、属性名列表、属性值列表, 以多个属性为条件来查询，使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityClass
	 *            实体类
	 * @param propertyNames
	 *            属性数组
	 * @param propertyValues
	 *            值数组
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByProperties(String cacheRegion,
			Class<T> entityClass, String[] propertyNames,
			Object[] propertyValues) {
		return this.<T> findByProperties(cacheRegion, entityClass.getName(),
				propertyNames, propertyValues);
	}

	/**
	 * <pre>
	 * 根据指定的实体名、以属性名(key)属性值(value)的Map, 以多个属性为条件来查询 
	 * 	示例：
	 * 		Map<String, Object> propertities = new HashMap<String, Object>(3);
	 * 		propertities.put("name", "007");
	 * 		propertities.put("sex", 1);
	 * 		propertities.put("age", 1);
	 * 		List&lt;Cust&gt; custs = dao.findByProperties("Cust", propertities);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名
	 * @param propertities
	 *            名值对
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByProperties(String entityName,
			Map<String, Object> propertities) {
		if (propertities == null || propertities.size() == 0) {
			return findAll(entityName);
		}

		Object[] propertyValues = new Object[propertities.size()];
		StringBuilder hql = new StringBuilder();

		hql.append("from ").append(entityName).append(" as model where");
		Iterator<Entry<String, Object>> it = propertities.entrySet().iterator();

		for (int i = 0; it.hasNext(); i++) {
			Entry<String, Object> entry = it.next();
			hql.append(" model.")
					.append(entry.getKey())
					.append(" = ?")
					.append(i);
			propertyValues[i] = entry.getValue();

			if (it.hasNext()) {
				hql.append(" and");
			}
		}

		return this.<T> find(hql.toString(), propertyValues);
	}

	/**
	 * 根据指定的实体名、以属性名(key)属性值(value)的Map, 以多个属性为条件来查询，使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityName
	 *            实体名
	 * @param propertities
	 *            名值对
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByProperties(String cacheRegion, String entityName,
			Map<String, Object> propertities) {
		if (propertities == null || propertities.size() == 0) {
			return findAll(cacheRegion, entityName);
		}

		Object[] propertyValues = new Object[propertities.size()];
		StringBuilder hql = new StringBuilder();

		hql.append("from ").append(entityName).append(" as model where ");
		Iterator<Entry<String, Object>> it = propertities.entrySet().iterator();

		for (int i = 0; it.hasNext(); i++) {
			Entry<String, Object> entry = it.next();
			hql.append(" model.")
					.append(entry.getKey())
					.append(" = ?")
					.append(i);
			propertyValues[i] = entry.getValue();

			if (it.hasNext()) {
				hql.append(" and");
			}
		}

		return this.<T> findCached(cacheRegion, hql.toString(), propertyValues);
	}

	/**
	 * 根据指定的实体名、属性名列表、属性值列表, 以多个属性为条件来查询，使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityName
	 *            实体名
	 * @param propertyNames
	 *            属性数组
	 * @param propertyValues
	 *            值数组
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByProperties(String cacheRegion, String entityName,
			String[] propertyNames, Object[] propertyValues) {
		boolean arrayLenEquals = (propertyNames == null) ? (propertyValues == null)
				: (propertyNames.length == propertyValues.length);

		if (!arrayLenEquals) {
			throw new IllegalArgumentException(
					"propertyNames.length not equals propertyValues.length");
		}

		if (propertyNames == null) {
			return findAll(entityName);
		}

		StringBuilder hql = new StringBuilder();
		hql.append("from ").append(entityName).append(" as model where ");

		for (int i = 0, len = propertyNames.length; i < len; i++) {
			if (i != 0) {
				hql.append(" and");
			}

			hql.append(" model.")
					.append(propertyNames[i])
					.append(" = ?")
					.append(i);
		}

		return this.<T> findCached(cacheRegion, hql.toString(), propertyValues);
	}

	/**
	 * <pre>
	 * 根据指定的实体名、属性名列表、属性值列表, 以多个属性为条件来查询
	 * 注意：必须保证属性名列表、属性值列表中的属性名-值的对应关系
	 * 	示例：
	 * 		String[] propertyNames = {"name", "sex", "age"};
	 * 		Object[] propertyValues = {"007", 1, 30};
	 * 		List&lt;Cust&gt; custs = dao.findByProperties("Cust", propertyNames, propertyValues);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名
	 * @param propertyNames
	 *            属性数组
	 * @param propertyValues
	 *            值数组
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByProperties(String entityName,
			String[] propertyNames, Object[] propertyValues) {
		boolean arrayLenEquals = (propertyNames == null) ? (propertyValues == null)
				: (propertyNames.length == propertyValues.length);

		if (!arrayLenEquals) {
			throw new IllegalArgumentException(
					"propertyNames.length not equals propertyValues.length");
		}

		if (propertyNames == null) {
			return findAll(entityName);
		}

		StringBuilder hql = new StringBuilder();

		hql.append("from ").append(entityName).append(" as model where ");

		for (int i = 0, len = propertyNames.length; i < len; i++) {
			if (i != 0) {
				hql.append(" and");
			}

			hql.append(" model.")
					.append(propertyNames[i])
					.append(" = ?")
					.append(i);
		}

		return this.<T> find(hql.toString(), propertyValues);
	}

	/**
	 * <pre>
	 * 根据指定的实体类、属性名、属性值, 以单个属性为条件来查询
	 * 	示例：
	 * 		List&lt;Cust&gt; custs = dao.findByProperty(Cust.class, "name", "007");
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param propertyName
	 *            属性名
	 * @param propertyValue
	 *            属性值
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByProperty(Class<T> entityClass,
			String propertyName, Object propertyValue) {
		return this.<T> findByProperty(entityClass.getName(), propertyName,
				propertyValue);
	}

	/**
	 * 根据指定的实体类、属性名、属性值, 以单个属性为条件来查询，使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityClass
	 *            实体类
	 * @param propertyName
	 *            属性名
	 * @param propertyValue
	 *            属性值
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByProperty(String cacheRegion, Class<T> entityClass,
			String propertyName, Object propertyValue) {
		return this.<T> findByProperty(cacheRegion, entityClass.getName(),
				propertyName, propertyValue);
	}

	/**
	 * <pre>
	 * 根据指定的实体名、属性名、属性值, 以单个属性为条件来查询
	 * 	示例：
	 * 		List&lt;Cust&gt; custs = dao.findByProperty("Cust", "name", "007");
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名, 如 "Cust"
	 * @param propertyName
	 *            属性名
	 * @param propertyValue
	 *            属性值
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByProperty(String entityName, String propertyName,
			Object propertyValue) {
		StringBuilder hql = new StringBuilder();

		hql.append("from ");
		hql.append(entityName);
		hql.append(" as model where model.");
		hql.append(propertyName);
		hql.append(" = ?0");

		return this.<T> find(hql.toString(), propertyValue);
	}

	/**
	 * 根据指定的实体名、属性名、属性值, 以单个属性为条件来查询，使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityName
	 *            实体名, 如 "Cust"
	 * @param propertyName
	 *            属性名
	 * @param propertyValue
	 *            属性值
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByProperty(String cacheRegion, String entityName,
			String propertyName, Object propertyValue) {
		StringBuilder hql = new StringBuilder();

		hql.append("from ");
		hql.append(entityName);
		hql.append(" as model where model.");
		hql.append(propertyName);
		hql.append(" = ?0");

		return this.<T> findCached(cacheRegion, hql.toString(), propertyValue);
	}

	/**
	 * 根据指定的实体类、sql语句和sql参数列表，查询对象列表
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类, 如结果集不对应实体，填null即可
	 * @param sql
	 *            指定的sql语句
	 * @param scalars
	 *            scalar
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @param values
	 *            参数列表
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findBySQL(Class<T> entityClass, String sql,
			Map<String, Type> scalars, ResultTransformer transformer,
			Object... values) {
		return this.<T> findBySQL(entityClass, null, sql, scalars, transformer,
				values);
	}

	/**
	 * <pre>
	 * 根据指定的实体类、sql语句和sql参数列表，查询对象列表
	 * 	示例:
	 * 		String sql = "select * from cust where id = ? and sex = ? ";
	 * 		List&lt;Cust&gt; custs = dao.findBySQL(Cust.class, sql, null, null, 100, 1);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类, 如结果集不对应实体，填null即可
	 * @param tableAlias
	 *            根对象别名
	 * @param sql
	 *            指定的sql语句
	 * @param scalars
	 *            scalar
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @param values
	 *            参数列表
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findBySQL(Class<T> entityClass, String tableAlias,
			String sql, Map<String, Type> scalars,
			ResultTransformer transformer, Object... values) {
		sql = getNamedHql(sql, values);
		Session session = sessionFactory.getCurrentSession();
		SQLQuery queryObject = session.createSQLQuery(sql);

		if (entityClass != null) {
			if (tableAlias != null) {
				queryObject.addEntity(tableAlias, entityClass);
			}
			else {
				queryObject.addEntity(entityClass);
			}
		}

		if (transformer != null) {
			queryObject.setResultTransformer(transformer);
		}

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(Integer.toString(i), values[i]);
			}
		}

		if (scalars != null) {
			for (Entry<String, Type> entry : scalars.entrySet()) {
				queryObject.addScalar(entry.getKey(), entry.getValue());
			}
		}

		return queryObject.list();
	}

	/**
	 * 根据指定的实体类、sql语句和sql参数列表，查询对象列表，使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityClass
	 *            实体类, 如结果集不对应实体，填null即可
	 * @param sql
	 *            指定的sql语句
	 * @param scalars
	 *            scalar
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @param values
	 *            参数列表
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findBySQL(String cacheRegion, Class<T> entityClass,
			String sql, Map<String, Type> scalars,
			ResultTransformer transformer, Object... values) {
		return this.<T> findBySQL(cacheRegion, entityClass, null, sql, scalars,
				transformer, values);
	}

	/**
	 * 根据指定的实体类、sql语句和sql参数列表，查询对象列表，使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityClass
	 *            实体类, 如结果集不对应实体，填null即可
	 * @param tableAlias
	 *            根结点别名
	 * @param sql
	 *            指定的sql语句
	 * @param scalars
	 *            scalar
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @param values
	 *            参数列表
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findBySQL(String cacheRegion, Class<T> entityClass,
			String tableAlias, String sql, Map<String, Type> scalars,
			ResultTransformer transformer, Object... values) {
		sql = getNamedHql(sql, values);
		Session session = sessionFactory.getCurrentSession();
		SQLQuery queryObject = session.createSQLQuery(sql);

		if (entityClass != null) {
			if (tableAlias != null) {
				queryObject.addEntity(tableAlias, entityClass);
			}
			else {
				queryObject.addEntity(entityClass);
			}
		}

		if (transformer != null) {
			queryObject.setResultTransformer(transformer);
		}

		queryObject.setCacheable(true);
		queryObject.setCacheRegion(cacheRegion);

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(Integer.toString(i), values[i]);
			}
		}
		if (scalars != null) {
			for (Entry<String, Type> entry : scalars.entrySet()) {
				queryObject.addScalar(entry.getKey(), entry.getValue());
			}
		}

		return queryObject.list();
	}

	/**
	 * <pre>
	 * 根据指定的实体类、sql语句和sql参数列表，查询对象列表，返回的实体为非受管实体
	 * 	示例:
	 * 		String sql = "select cust_id as custId, cust_name as custName from cust where id = ? and sex = ? ";
	 * 		List&lt;Cust&gt; custs = dao.findBySQL(Cust.class, sql, 100, 1);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类, 如结果集不对应实体，填null即可
	 * @param sql
	 *            指定的sql语句
	 * @param scalars
	 *            scalar
	 * @param values
	 *            参数列表
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findBySQLDetached(Class<T> entityClass, String sql,
			Map<String, Type> scalars, Object... values) {
		sql = getNamedHql(sql, values);
		Session session = sessionFactory.getCurrentSession();
		SQLQuery queryObject = session.createSQLQuery(sql);
		String[] aliaes = getAliaes(sql);
		Type[] types = getTypes(entityClass, aliaes);

		if (types != null) {
			for (int i = 0; i < types.length; i++) {
				queryObject.addScalar(aliaes[i], types[i]);
			}
		}

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(Integer.toString(i), values[i]);
			}
		}

		if (scalars != null) {
			for (Entry<String, Type> entry : scalars.entrySet()) {
				queryObject.addScalar(entry.getKey(), entry.getValue());
			}
		}

		return getObjectList(entityClass, queryObject.list(), aliaes);
	}

	/**
	 * 根据指定的实体类、sql语句和sql参数列表，查询对象列表，返回的实体为非受管实体，使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityClass
	 *            实体类, 如结果集不对应实体，填null即可
	 * @param sql
	 *            指定的sql语句
	 * @param scalars
	 *            scalar
	 * @param values
	 *            参数列表
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findBySQLDetached(String cacheRegion,
			Class<T> entityClass, String sql, Map<String, Type> scalars,
			Object... values) {
		sql = getNamedHql(sql, values);
		Session session = sessionFactory.getCurrentSession();
		SQLQuery queryObject = session.createSQLQuery(sql);

		queryObject.setCacheable(true);
		queryObject.setCacheRegion(cacheRegion);

		String[] aliaes = getAliaes(sql);
		Type[] types = getTypes(entityClass, aliaes);

		if (types != null) {
			for (int i = 0; i < types.length; i++) {
				queryObject.addScalar(aliaes[i], types[i]);
			}
		}

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(Integer.toString(i), values[i]);
			}
		}

		if (scalars != null) {
			for (Entry<String, Type> entry : scalars.entrySet()) {
				queryObject.addScalar(entry.getKey(), entry.getValue());
			}
		}

		return getObjectList(entityClass, queryObject.list(), aliaes);
	}

	/**
	 * <pre>
	 * 根据指定的<b>hql</b>和参数列表，返回查找的结果，使用二级缓存
	 * 示例：
	 * 		String hql = "from Cust as cust where cust.name like ? ";
	 * 		List&lt;Cust&gt; custs = dao.find("Cust", hql, "%007%");
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param hql
	 *            hql语句
	 * @param values
	 *            参数列表
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findCached(String cacheRegion, String hql,
			Object... values) {
		Session session = sessionFactory.getCurrentSession();
		Query queryObject = session.createQuery(getNamedHql(hql, values));

		queryObject.setCacheable(true);
		queryObject.setCacheRegion(cacheRegion);

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(Integer.toString(i), values[i]);
			}
		}

		return queryObject.list();
	}

	/**
	 * 刷新持久化对象
	 */
	public void flush() {
		sessionFactory.getCurrentSession().flush();
	}

	/**
	 * 获得指定的sequence的nextval
	 * 
	 * @param sequenceName
	 *            序列号名
	 * @return Long 序列值
	 */
	public Long getSeqNextVal(String sequenceName) {
		StringBuilder sql = new StringBuilder();
		sql.append("select ")
				.append(sequenceName)
				.append(".nextval next_val from dual");
		Session session = sessionFactory.getCurrentSession();
		SQLQuery queryObject = session
				.createSQLQuery(sql.toString())
				.addScalar("next_val", StandardBasicTypes.LONG);

		queryObject.setCacheable(false);

		return (Long) queryObject.uniqueResult();
	}

	/**
	 * 锁定对象
	 * 
	 * @param entity
	 *            持久化对象
	 */
	public void lock(Object entity) {
		sessionFactory
				.getCurrentSession()
				.buildLockRequest(LockOptions.NONE)
				.lock(entity);
	}

	/**
	 * 锁定对象
	 * 
	 * @param entity
	 *            实体
	 * @param lockOptions
	 *            锁模式
	 */
	public void lock(Object entity, LockOptions lockOptions) {
		sessionFactory
				.getCurrentSession()
				.buildLockRequest(lockOptions)
				.lock(entity);
	}

	/**
	 * 锁定对象
	 * 
	 * @param entityName
	 *            实体名
	 * @param entity
	 *            实体
	 * @param lockOptions
	 *            锁模式
	 */
	public void lock(String entityName, Object entity, LockOptions lockOptions) {
		sessionFactory
				.getCurrentSession()
				.buildLockRequest(lockOptions)
				.lock(entityName, entity);
	}

	/**
	 * 将一个处于detached状态的对象与session重新关联，使之处于persistent状态
	 * 处于detached状态的对象对应数据库的一条记录(包含主键值), 但与Hibernate实体容器无关,亦与session无关联
	 * 
	 * @param <T>
	 *            实体类型
	 * @param detachedInstance
	 *            处于detached状态的对象.
	 * @return T 实体
	 */
	public <T> T merge(Object detachedInstance) {
		return (T) sessionFactory.getCurrentSession().merge(detachedInstance);
	}

	/**
	 * 将一个处于detached状态的对象与session重新关联，使之处于persistent状态
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名
	 * @param detachedInstance
	 *            处于detached状态的对象.
	 * @return T 实体
	 * @throws DataAccessException
	 */
	public <T> T merge(String entityName, Object detachedInstance)
			throws DataAccessException {
		return (T) sessionFactory.getCurrentSession().merge(entityName,
				detachedInstance);
	}

	/**
	 * <pre>
	 * 根据指定的hql、参数、pageNo、pageSize 来进行查询分页
	 * 示例:
	 *		String hql = "from Cust as cust where cust.age > ?";
	 * 		PagedList&lt;Cust&gt; custs = dao.page(hql, 0, 10, null, 0);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param hql
	 *            hql语句
	 * @param pageNo
	 *            当前页码,从0开始
	 * @param pageSize
	 *            页面大小
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param values
	 *            参数值
	 * @return PagedList&lt;T&gt; 分页查询的结果列表
	 */
	public <T> PagedList<T> page(String hql, int pageNo, int pageSize,
			List<SortOrder> orders, Object... values) {
		hql = getNamedHql(hql, values);
		Session session = sessionFactory.getCurrentSession();
		String countHql = hql.toLowerCase();
		int selectIndex = countHql.indexOf("select");
		int fromIndex = countHql.indexOf("from");
		String prefix = null;
		if (selectIndex != -1 && selectIndex < fromIndex) {
			prefix = hql.substring(selectIndex + 6, fromIndex).trim();
		}

		Integer resultCount = pageCache.get(hql, values);
		if (pageNo == 0 || resultCount == null) {
			// count
			countHql = "select count(*) " + hql.substring(fromIndex);
			Query queryObject = session.createQuery(countHql);
			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					queryObject.setParameter(Integer.toString(i), values[i]);
				}
			}

			resultCount = ((Number) queryObject.uniqueResult()).intValue();
			pageCache.put(resultCount, hql, values);
		}

		Page page = new Page(pageNo, pageSize);
		page.setResultCount(resultCount);
		if (resultCount <= page.getFirstResult()) {
			return null;
		}

		Query queryObject;
		if (orders != null && !orders.isEmpty()) {
			StringBuilder pageHql = new StringBuilder(hql);
			pageHql.append(" order by ");
			for (Order order : orders) {
				pageHql.append(order.toString());
				pageHql.append(", ");
			}

			if (this.orderById.get()) {
				if (prefix != null) {
					String[] ss = prefix.split("\\s*,\\s*");
					for (String s : ss) {
						String[] sss = s.split("\\s+");
						pageHql.append(sss[sss.length - 1].trim());
						pageHql.append(".id, ");
					}
					int len = pageHql.length();
					pageHql.delete(len - 2, len);
				}
				else {
					pageHql.append("id");
				}
			}
			else {
				int len = pageHql.length();
				pageHql.delete(len - 2, len);
			}

			queryObject = session.createQuery(pageHql.toString());
		}
		else {
			queryObject = session.createQuery(hql);
		}

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(Integer.toString(i), values[i]);
			}
		}

		queryObject.setFirstResult(page.getFirstResult());
		queryObject.setMaxResults(page.getCurrentPageSize());

		PagedList<T> pagedList = new PagedList<T>(queryObject.list(),
				resultCount);

		return pagedList;
	}

	/**
	 * 根据指定的实体类、Criterion、Order、pageNo、pageSize来进行复合查询、分页
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param pageNo
	 *            当前页码,从0开始
	 * @param pageSize
	 *            页面大小
	 * @return PagedList&lt;T&gt; 分页查询的结果列表
	 */
	public <T> PagedList<T> pageByCriterion(Class<T> entityClass,
			Criterion criterion, int pageNo, int pageSize) {
		return this.<T> pageByCriterion(entityClass.getName(), criterion,
				pageNo, pageSize);
	}

	/**
	 * 根据指定的实体类、Criterion、Order、pageNo、pageSize 来进行复合查询、分页
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param pageNo
	 *            当前页码,从0开始
	 * @param pageSize
	 *            页面大小
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @return PagedList&lt;T&gt; 分页查询的结果列表
	 */
	public <T> PagedList<T> pageByCriterion(Class<T> entityClass,
			Criterion criterion, int pageNo, int pageSize,
			List<SortOrder> orders) {
		return this.<T> pageByCriterion(entityClass.getName(), criterion,
				pageNo, pageSize, orders);
	}

	/**
	 * <pre>
	 * 根据指定的实体类、Criterion、Order、pageNo、pageSize 来进行复合查询、分页
	 * 示例1:
	 * 		Criterion c1 = null;
	 *		Criterion c2 = null;
	 *		Criterion c3 = null;
	 *		Conjunction conjunction = null;
	 *
	 *		c1 = Restrictions.eq("name", "007");
	 *		c2 = Restrictions.eq("age", "20");
	 *		c3 = Restrictions.between("birthday", beginDate, endDate);
	 *		conjunction = Restrictions.conjunction();
	 *		conjunction.add(c1).add(c2).add(c3);
	 *
	 *		List&lt;Order&gt; orders = new ArrayList&lt;Order&gt;();
	 *		orders.add(SortOrder.asc("id"));
	 *		List&lt;CriteriaProperty&gt; properties = new ArrayList&lt;CriteriaProperty&gt;();
	 *		properties.add(new CriteriaProperty("user", FetchMode.JOIN));
	 *
	 * 		PagedList&lt;Cust&gt; custs = dao.pageByCriterion(Cust.class, conjunction, 2, 10, orders, properties);
	 * 		//PagedList&lt;Cust&gt; custs = dao.pageByCriterion(Cust.class, conjunction, 2, 10, null, null);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param pageNo
	 *            当前页码,从0开始
	 * @param pageSize
	 *            页面大小
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @return PagedList&lt;T&gt; 分页查询的结果列表
	 */
	public <T> PagedList<T> pageByCriterion(Class<T> entityClass,
			Criterion criterion, int pageNo, int pageSize,
			List<SortOrder> orders, List<CriteriaProperty> properties) {
		return this.<T> pageByCriterion(entityClass.getName(), criterion,
				pageNo, pageSize, orders, properties);
	}

	/**
	 * 根据指定的实体名、criterion、pageNo、pageSize，进行分页查询
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param pageNo
	 *            当前页码,从0开始
	 * @param pageSize
	 *            页面大小
	 * @return PagedList&lt;T&gt; 分页查询的结果列表
	 */
	public <T> PagedList<T> pageByCriterion(String entityName,
			Criterion criterion, int pageNo, int pageSize) {
		Session session = sessionFactory.getCurrentSession();
		Integer resultCount = pageCache.get(null, entityName, criterion);
		if (pageNo == 0 || resultCount == null) {
			// count
			Criteria criteria = session.createCriteria(entityName);
			if (criterion != null) {
				criteria.add(criterion);
			}
			criteria.setProjection(Projections.rowCount());
			resultCount = ((Number) criteria.uniqueResult()).intValue();
			pageCache.put(resultCount, null, entityName, criterion);
		}

		Page page = new Page(pageNo, pageSize);
		page.setResultCount(resultCount);

		if (resultCount <= page.getFirstResult()) {
			return null;
		}

		Criteria criteria = session.createCriteria(entityName);
		if (criterion != null) {
			criteria.add(criterion);
		}

		// list
		criteria.setFirstResult(page.getFirstResult());
		criteria.setMaxResults(page.getCurrentPageSize());

		PagedList<T> pagedList = new PagedList<T>(criteria.list(), resultCount);

		return pagedList;
	}

	/**
	 * 根据指定的实体名、criterion、pageNo、pageSize、orders，进行分页查询
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param pageNo
	 *            当前页码,从0开始
	 * @param pageSize
	 *            页面大小
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @return PagedList&lt;T&gt; 分页查询的结果列表
	 */
	public <T> PagedList<T> pageByCriterion(String entityName,
			Criterion criterion, int pageNo, int pageSize,
			List<SortOrder> orders) {
		Session session = sessionFactory.getCurrentSession();
		Integer resultCount = pageCache.get(null, entityName, criterion);
		if (pageNo == 0 || resultCount == null) {
			// count
			Criteria criteria = session.createCriteria(entityName);
			if (criterion != null) {
				criteria.add(criterion);
			}
			criteria.setProjection(Projections.rowCount());
			resultCount = ((Number) criteria.uniqueResult()).intValue();
			pageCache.put(resultCount, null, entityName, criterion);
		}

		Page page = new Page(pageNo, pageSize);
		page.setResultCount(resultCount);

		if (resultCount <= page.getFirstResult()) {
			return null;
		}

		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		// list
		criteria.setFirstResult(page.getFirstResult());
		criteria.setMaxResults(page.getCurrentPageSize());

		if (orders != null) {
			for (Order order : orders) {
				criteria.addOrder(order);
			}
			criteria.addOrder(SortOrder.asc("id"));
		}

		PagedList<T> pagedList = new PagedList<T>(criteria.list(), resultCount);

		return pagedList;
	}

	/**
	 * 根据指定的实体名、criterion、pageNo、pageSize、orders，进行分页查询
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param pageNo
	 *            当前页码,从0开始
	 * @param pageSize
	 *            页面大小
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @return PagedList&lt;T&gt; 分页查询的结果列表
	 */
	public <T> PagedList<T> pageByCriterion(String entityName,
			Criterion criterion, int pageNo, int pageSize,
			List<SortOrder> orders, List<CriteriaProperty> properties) {
		Session session = sessionFactory.getCurrentSession();
		Integer resultCount = pageCache.get(null, entityName, criterion);
		if (pageNo == 0 || resultCount == null) {
			// count
			Criteria criteria = session.createCriteria(entityName);
			if (criterion != null) {
				criteria.add(criterion);
			}

			if (properties != null) {
				for (CriteriaProperty property : properties) {
					String prop = property.getProp();
					String alias = property.getAlias();
					JoinType joinType = property.getJoinType();
					FetchMode fetchMode = property.getFetchMode();

					if (StringUtils.isNotBlank(alias)) {
						if (joinType == null) {
							criteria.createAlias(prop, alias);
						}
						else {
							criteria.createAlias(prop, alias, joinType);
						}
					}
					if (fetchMode != null) {
						criteria.setFetchMode(prop, fetchMode);
					}
				}
			}
			criteria.setProjection(Projections.rowCount());
			resultCount = ((Number) criteria.uniqueResult()).intValue();
			pageCache.put(resultCount, null, entityName, criterion);
		}

		Page page = new Page(pageNo, pageSize);
		page.setResultCount(resultCount);

		if (resultCount <= page.getFirstResult()) {
			return null;
		}

		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		if (properties != null) {
			for (CriteriaProperty property : properties) {
				String prop = property.getProp();
				String alias = property.getAlias();
				JoinType joinType = property.getJoinType();
				FetchMode fetchMode = property.getFetchMode();

				if (StringUtils.isNotBlank(alias)) {
					if (joinType == null) {
						criteria.createAlias(prop, alias);
					}
					else {
						criteria.createAlias(prop, alias, joinType);
					}
				}
				if (fetchMode != null) {
					criteria.setFetchMode(prop, fetchMode);
				}
			}
		}

		// list
		criteria.setFirstResult(page.getFirstResult());
		criteria.setMaxResults(page.getCurrentPageSize());

		if (orders != null) {
			for (Order order : orders) {
				criteria.addOrder(order);
			}
			criteria.addOrder(SortOrder.asc("id"));
		}

		PagedList<T> pagedList = new PagedList<T>(criteria.list(), resultCount);

		return pagedList;
	}

	/**
	 * 使用Hibernate的org.hibernate.criterion.Example来进行多条件的复合查询分页
	 * 
	 * @param <T>
	 *            实体类型
	 * @param exampleEntity
	 *            实体对象
	 * @param pageNo
	 *            当前页码,从0开始
	 * @param pageSize
	 *            页面大小
	 * @return PagedList&lt;T&gt; 分页查询的结果列表
	 */
	public <T> PagedList<T> pageByExample(T exampleEntity, int pageNo,
			int pageSize) {
		Example example = Example.create(exampleEntity);
		Class<T> clz = (Class<T>) exampleEntity.getClass();

		return this.<T> pageByCriterion(clz, example, pageNo, pageSize);
	}

	/**
	 * 使用Hibernate的org.hibernate.criterion.Example来进行多条件的复合查询分页,并对结果进行排序
	 * 
	 * @param <T>
	 *            实体类型
	 * @param exampleEntity
	 *            实体对象
	 * @param pageNo
	 *            当前页码,从0开始
	 * @param pageSize
	 *            页面大小
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @return PagedList&lt;T&gt; 分页查询的结果列表
	 */
	public <T> PagedList<T> pageByExample(T exampleEntity, int pageNo,
			int pageSize, List<SortOrder> orders) {
		Example example = Example.create(exampleEntity);
		Class<T> clz = (Class<T>) exampleEntity.getClass();

		return this.<T> pageByCriterion(clz, example, pageNo, pageSize, orders);
	}

	/**
	 * <pre>
	 * 使用Hibernate的org.hibernate.criterion.Example来进行多条件的复合查询分页
	 * 	示例：
	 * 		Cust cust = new Cust();
	 * 		cust.setName("007");
	 * 		cust.setAge(20);
	 * 		
	 * 		List&lt;Order&gt; orders = new ArrayList&lt;Order&gt;();
	 * 		orders.add(SortOrder.asc("id"));
	 *		List&lt;CriteriaProperty&gt; properties = new ArrayList&lt;CriteriaProperty&gt;();
	 *		properties.add(new CriteriaProperty("user", FetchMode.JOIN));
	 * 
	 * 		PagedList&lt;Cust&gt; custs = dao.pageByExample(cust, 2, 10 orders, properties);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param exampleEntity
	 *            实体对象
	 * @param pageNo
	 *            当前页码,从0开始
	 * @param pageSize
	 *            页面大小
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @return PagedList&lt;T&gt; 分页查询的结果列表
	 */
	public <T> PagedList<T> pageByExample(T exampleEntity, int pageNo,
			int pageSize, List<SortOrder> orders,
			List<CriteriaProperty> properties) {
		Example example = Example.create(exampleEntity);
		Class<T> clz = (Class<T>) exampleEntity.getClass();

		return this.<T> pageByCriterion(clz, example, pageNo, pageSize, orders,
				properties);
	}

	/**
	 * 根据指定的entityClass、sql、Page、参数值 来进行查询分页
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类型
	 * @param sql
	 *            sql语句
	 * @param pageNo
	 *            当前页码,从0开始
	 * @param pageSize
	 *            页面大小
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param scalars
	 *            scalar
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @param values
	 *            参数值
	 * @return PagedList&lt;T&gt; 分页查询的结果列表
	 */
	public <T> PagedList<T> pageBySQL(Class<T> entityClass, String sql,
			int pageNo, int pageSize, List<SortOrder> orders,
			Map<String, Type> scalars, ResultTransformer transformer,
			Object... values) {
		return this.<T> pageBySQL(entityClass, null, sql, pageNo, pageSize,
				orders, scalars, transformer, values);
	}

	/**
	 * <pre>
	 * 根据指定的entityClass、sql、Page、参数值 来进行查询分页
	 * 示例1:
	 * 		String sql = "select * from cust where age > ?";
	 * 		PagedList&lt;Cust&gt; custs = dao.pageBySQL(Cust.class, sql, null, 2, 10, null, 1);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类型
	 * @param tableAlias
	 *            根结点别名
	 * @param sql
	 *            sql语句
	 * @param pageNo
	 *            当前页码,从0开始
	 * @param pageSize
	 *            页面大小
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param scalars
	 *            scalar
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @param values
	 *            参数值
	 * @return PagedList&lt;T&gt; 分页查询的结果列表
	 */
	public <T> PagedList<T> pageBySQL(Class<T> entityClass, String tableAlias,
			String sql, int pageNo, int pageSize, List<SortOrder> orders,
			Map<String, Type> scalars, ResultTransformer transformer,
			Object... values) {
		sql = getNamedHql(sql, values);
		Session session = sessionFactory.getCurrentSession();
		Integer resultCount = pageCache.get(sql, values);
		if (pageNo == 0 || resultCount == null) {
			// count
			String countSql = sql.toLowerCase();
			int fromIndex = countSql.indexOf("from");
			countSql = "select count(1) as count " + sql.substring(fromIndex);
			SQLQuery queryObject = session.createSQLQuery(countSql);

			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					queryObject.setParameter(Integer.toString(i), values[i]);
				}
			}
			queryObject.addScalar("count", IntegerType.INSTANCE);
			resultCount = (Integer) queryObject.uniqueResult();
			pageCache.put(resultCount, sql, values);
		}

		Page page = new Page(pageNo, pageSize);
		page.setResultCount(resultCount);

		if (resultCount <= page.getFirstResult()) {
			return null;
		}

		SQLQuery queryObject;
		if (orders != null && !orders.isEmpty()) {
			StringBuilder pageSql = new StringBuilder(sql);

			pageSql.append(" order by ");
			for (Order order : orders) {
				pageSql.append(order.toString());
				pageSql.append(", ");
			}
			if (this.orderById.get()) {
				ClassMetadata meta = sessionFactory
						.getClassMetadata(entityClass);
				if (meta instanceof AbstractEntityPersister) {
					AbstractEntityPersister entityPersister = (AbstractEntityPersister) meta;
					String[] ids = entityPersister.getIdentifierColumnNames();
					if (ids != null && ids.length > 0) {
						for (String id : ids) {
							pageSql.append(id);
							pageSql.append(", ");
						}
					}
				}
			}
			int len = pageSql.length();
			pageSql.delete(len - 2, len);

			queryObject = session.createSQLQuery(pageSql.toString());
		}
		else {
			queryObject = session.createSQLQuery(sql);
		}

		if (entityClass != null) {
			if (tableAlias != null) {
				queryObject.addEntity(tableAlias, entityClass);
			}
			else {
				queryObject.addEntity(entityClass);
			}
		}

		if (transformer != null) {
			queryObject.setResultTransformer(transformer);
		}

		if (scalars != null) {
			for (Entry<String, Type> entry : scalars.entrySet()) {
				queryObject.addScalar(entry.getKey(), entry.getValue());
			}
		}

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(Integer.toString(i), values[i]);
			}
		}

		queryObject.setFirstResult(page.getFirstResult());
		queryObject.setMaxResults(page.getCurrentPageSize());

		PagedList<T> pagedList = new PagedList<T>(queryObject.list(),
				resultCount);

		return pagedList;
	}

	/**
	 * <pre>
	 * 根据指定的实体类、sql语句和sql参数列表，查询分页列表，返回的实体为非受管实体
	 * 	示例:
	 * 		String sql = "select cust_id as custId, cust_name as custName from cust where id = ? and sex = ? ";
	 * 		PagedList&lt;Cust&gt; custs = dao.pageBySQLDetached(Cust.class, sql, 1, 10, null, 100, 1);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类, 如结果集不对应实体，填null即可
	 * @param sql
	 *            指定的sql语句
	 * @param pageNo
	 *            当前页码,从0开始
	 * @param pageSize
	 *            页面大小
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param scalars
	 *            scalar
	 * @param values
	 *            参数列表
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> PagedList<T> pageBySQLDetached(Class<T> entityClass, String sql,
			int pageNo, int pageSize, List<SortOrder> orders,
			Map<String, Type> scalars, Object... values) {
		sql = getNamedHql(sql, values);
		Session session = sessionFactory.getCurrentSession();
		Integer resultCount = pageCache.get(sql, values);
		if (pageNo == 0 || resultCount == null) {
			// count
			String countSql = sql.toLowerCase();
			int fromIndex = countSql.indexOf("from");
			countSql = "select count(1) as count " + sql.substring(fromIndex);
			SQLQuery queryObject = session.createSQLQuery(countSql);

			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					queryObject.setParameter(Integer.toString(i), values[i]);
				}
			}
			queryObject.addScalar("count", IntegerType.INSTANCE);
			resultCount = (Integer) queryObject.uniqueResult();
			pageCache.put(resultCount, sql, values);
		}

		Page page = new Page(pageNo, pageSize);
		page.setResultCount(resultCount);

		if (resultCount <= page.getFirstResult()) {
			return null;
		}

		SQLQuery queryObject;
		if (orders != null && !orders.isEmpty()) {
			StringBuilder pageSql = new StringBuilder(sql);

			pageSql.append(" order by ");
			for (Order order : orders) {
				pageSql.append(order.toString());
				pageSql.append(", ");
			}

			if (this.orderById.get()) {
				ClassMetadata meta = sessionFactory
						.getClassMetadata(entityClass);
				if (meta instanceof AbstractEntityPersister) {
					AbstractEntityPersister entityPersister = (AbstractEntityPersister) meta;
					String[] ids = entityPersister.getIdentifierColumnNames();
					if (ids != null && ids.length > 0) {
						for (String id : ids) {
							pageSql.append(id);
							pageSql.append(", ");
						}
					}
				}
			}
			int len = pageSql.length();
			pageSql.delete(len - 2, len);

			queryObject = session.createSQLQuery(pageSql.toString());
		}
		else {
			queryObject = session.createSQLQuery(sql);
		}

		String[] aliaes = getAliaes(sql);
		Type[] types = getTypes(entityClass, aliaes);

		if (types != null) {
			for (int i = 0; i < types.length; i++) {
				queryObject.addScalar(aliaes[i], types[i]);
			}
		}

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(Integer.toString(i), values[i]);
			}
		}

		if (scalars != null) {
			for (Entry<String, Type> entry : scalars.entrySet()) {
				queryObject.addScalar(entry.getKey(), entry.getValue());
			}
		}

		queryObject.setFirstResult(page.getFirstResult());
		queryObject.setMaxResults(page.getCurrentPageSize());

		List<T> list = getObjectList(entityClass, queryObject.list(), aliaes);
		PagedList<T> pagedList = new PagedList<T>(list, resultCount);

		return pagedList;
	}

	/**
	 * <pre>
	 * 根据指定的hql、参数、startIndex、endIndex 来取部分数据
	 * 示例:
	 *		String hql = "from Cust as cust where cust.age > ?";
	 * 		List&lt;Cust&gt; custs = dao.findPartial(hql, 0, 10, null, 0);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param hql
	 *            hql语句
	 * @param startIndex
	 *            起始位置,从0开始
	 * @param endIndex
	 *            结束位置
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param values
	 *            参数值
	 * @return List&lt;T&gt; 分页查询的结果列表
	 */
	public <T> List<T> findPartial(String hql, int startIndex, int endIndex,
			List<SortOrder> orders, Object... values) {
		if (startIndex < 0 || endIndex <= startIndex) {
			return null;
		}

		Session session = sessionFactory.getCurrentSession();
		Query queryObject = null;
		hql = getNamedHql(hql, values);

		if (orders != null && !orders.isEmpty()) {
			StringBuilder pageHql = new StringBuilder(hql);

			pageHql.append(" order by ");
			for (Order order : orders) {
				pageHql.append(order.toString());
				pageHql.append(", ");
			}

			if (this.orderById.get()) {
				String hql1 = hql.toLowerCase();
				int fromIndex = hql1.indexOf("from");
				int selectIndex = hql1.indexOf("select");
				if (fromIndex != -1 && selectIndex != -1
						&& selectIndex < fromIndex) {
					String prefix = hql
							.substring(selectIndex + 6, fromIndex)
							.trim();
					String[] ss = prefix.split("\\s*,\\s*");
					for (String s : ss) {
						String[] sss = s.split("\\s+");
						pageHql.append(sss[sss.length - 1].trim());
						pageHql.append(".id, ");
					}
					int len = pageHql.length();
					pageHql.delete(len - 2, len);
				}
				else {
					pageHql.append("id");
				}
			}
			else {
				int len = pageHql.length();
				pageHql.delete(len - 2, len);
			}

			queryObject = session.createQuery(pageHql.toString());
		}
		else {
			queryObject = session.createQuery(hql);
		}

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(Integer.toString(i), values[i]);
			}
		}

		queryObject.setFirstResult(startIndex);
		queryObject.setMaxResults(endIndex - startIndex);

		return queryObject.list();
	}

	/**
	 * 根据指定的实体类、Criterion、Order、startIndex、endIndex 取部分数据
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param startIndex
	 *            起始位置,从0开始
	 * @param endIndex
	 *            结束位置
	 * @return List&lt;T&gt; 分页查询的结果列表
	 */
	public <T> List<T> findPartialByCriterion(Class<T> entityClass,
			Criterion criterion, int startIndex, int endIndex) {
		return this.<T> findPartialByCriterion(entityClass.getName(),
				criterion, startIndex, endIndex);
	}

	/**
	 * 根据指定的实体类、Criterion、Order、startIndex、endIndex 取部分数据
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param startIndex
	 *            起始位置,从0开始
	 * @param endIndex
	 *            结束位置
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @return List&lt;T&gt; 分页查询的结果列表
	 */
	public <T> List<T> findPartialByCriterion(Class<T> entityClass,
			Criterion criterion, int startIndex, int endIndex,
			List<SortOrder> orders) {
		return this.<T> findPartialByCriterion(entityClass.getName(),
				criterion, startIndex, endIndex, orders);
	}

	/**
	 * <pre>
	 * 根据指定的实体类、Criterion、Order、startIndex、endIndex 取部分数据
	 * 示例1:
	 * 		Criterion c1 = null;
	 *		Criterion c2 = null;
	 *		Criterion c3 = null;
	 *		Conjunction conjunction = null;
	 *
	 *		c1 = Restrictions.eq("name", "007");
	 *		c2 = Restrictions.eq("age", "20");
	 *		c3 = Restrictions.between("birthday", beginDate, endDate);
	 *		conjunction = Restrictions.conjunction();
	 *		conjunction.add(c1).add(c2).add(c3);
	 *
	 *		List&lt;Order&gt; orders = new ArrayList&lt;Order&gt;();
	 *		orders.add(SortOrder.asc("id"));
	 *		List&lt;CriteriaProperty&gt; properties = new ArrayList&lt;CriteriaProperty&gt;(1);
	 *		properties.add(new CriteriaProperty("user", FetchMode.JOIN));
	 *
	 * 		List&lt;Cust&gt; custs = dao.findPartialByCriterion(Cust.class, conjunction, 0, 10, orders, properties);
	 * 		//List&lt;Cust&gt; custs = dao.findPartialByCriterion(Cust.class, conjunction, 0, 10, null, null);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param startIndex
	 *            起始位置,从0开始
	 * @param endIndex
	 *            结束位置
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @return List&lt;T&gt; 分页查询的结果列表
	 */
	public <T> List<T> findPartialByCriterion(Class<T> entityClass,
			Criterion criterion, int startIndex, int endIndex,
			List<SortOrder> orders, List<CriteriaProperty> properties) {
		return this.<T> findPartialByCriterion(entityClass.getName(),
				criterion, startIndex, endIndex, orders, properties);
	}

	/**
	 * 根据指定的实体名、criterion、startIndex、endIndex 取部分数据
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param startIndex
	 *            起始位置,从0开始
	 * @param endIndex
	 *            结束位置
	 * @return List&lt;T&gt; 分页查询的结果列表
	 */
	public <T> List<T> findPartialByCriterion(String entityName,
			Criterion criterion, int startIndex, int endIndex) {
		if (startIndex < 0 || endIndex <= startIndex) {
			return null;
		}

		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		// list
		criteria.setFirstResult(startIndex);
		criteria.setMaxResults(endIndex - startIndex);

		return criteria.list();
	}

	/**
	 * 根据指定的实体名、criterion、startIndex、endIndex、orders 取部分数据
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param startIndex
	 *            起始位置,从0开始
	 * @param endIndex
	 *            结束位置
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @return List&lt;T&gt; 分页查询的结果列表
	 */
	public <T> List<T> findPartialByCriterion(String entityName,
			Criterion criterion, int startIndex, int endIndex,
			List<SortOrder> orders) {
		if (startIndex < 0 || endIndex <= startIndex) {
			return null;
		}

		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		// list
		criteria.setFirstResult(startIndex);
		criteria.setMaxResults(endIndex - startIndex);

		if (orders != null) {
			for (Order order : orders) {
				criteria.addOrder(order);
			}
			criteria.addOrder(SortOrder.asc("id"));
		}

		return criteria.list();
	}

	/**
	 * 根据指定的实体名、criterion、startIndex、endIndex、orders 取部分数据
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param startIndex
	 *            起始位置,从0开始
	 * @param endIndex
	 *            结束位置
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @return List&lt;T&gt; 分页查询的结果列表
	 */
	public <T> List<T> findPartialByCriterion(String entityName,
			Criterion criterion, int startIndex, int endIndex,
			List<SortOrder> orders, List<CriteriaProperty> properties) {
		if (startIndex < 0 || endIndex <= startIndex) {
			return null;
		}

		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		if (properties != null) {
			for (CriteriaProperty property : properties) {
				String prop = property.getProp();
				String alias = property.getAlias();
				JoinType joinType = property.getJoinType();
				FetchMode fetchMode = property.getFetchMode();

				if (StringUtils.isNotBlank(alias)) {
					if (joinType == null) {
						criteria.createAlias(prop, alias);
					}
					else {
						criteria.createAlias(prop, alias, joinType);
					}
				}
				if (fetchMode != null) {
					criteria.setFetchMode(prop, fetchMode);
				}
			}
		}

		// list
		criteria.setFirstResult(startIndex);
		criteria.setMaxResults(endIndex - startIndex);

		if (orders != null) {
			for (Order order : orders) {
				criteria.addOrder(order);
			}
			criteria.addOrder(SortOrder.asc("id"));
		}

		return criteria.list();
	}

	/**
	 * 使用Hibernate的org.hibernate.criterion.Example取部分数据
	 * 
	 * @param <T>
	 *            实体类型
	 * @param exampleEntity
	 *            实体对象
	 * @param startIndex
	 *            起始位置,从0开始
	 * @param endIndex
	 *            结束位置
	 * @return List&lt;T&gt; 分页查询的结果列表
	 */
	public <T> List<T> findPartialByExample(T exampleEntity, int startIndex,
			int endIndex) {
		Example example = Example.create(exampleEntity);
		Class<T> clz = (Class<T>) exampleEntity.getClass();

		return this.<T> findPartialByCriterion(clz, example, startIndex,
				endIndex);
	}

	/**
	 * 使用Hibernate的org.hibernate.criterion.Example取部分数据,并对结果进行排序
	 * 
	 * @param <T>
	 *            实体类型
	 * @param exampleEntity
	 *            实体对象
	 * @param startIndex
	 *            起始位置,从0开始
	 * @param endIndex
	 *            结束位置
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @return List&lt;T&gt; 分页查询的结果列表
	 */
	public <T> List<T> findPartialByExample(T exampleEntity, int startIndex,
			int endIndex, List<SortOrder> orders) {
		Example example = Example.create(exampleEntity);
		Class<T> clz = (Class<T>) exampleEntity.getClass();

		return this.<T> findPartialByCriterion(clz, example, startIndex,
				endIndex, orders);
	}

	/**
	 * <pre>
	 * 使用Hibernate的org.hibernate.criterion.Example取部分数据
	 * 	示例：
	 * 		Cust cust = new Cust();
	 * 		cust.setName("007");
	 * 		cust.setAge(20);
	 * 		
	 * 		List&lt;Order&gt; orders = new ArrayList&lt;Order&gt;();
	 * 		orders.add(SortOrder.asc("id"));
	 *		List&lt;CriteriaProperty&gt; properties = new ArrayList&lt;CriteriaProperty&gt;();
	 *		properties.add(new CriteriaProperty("user", FetchMode.JOIN));
	 * 
	 * 		List&lt;Cust&gt; custs = dao.findPartialByExample(cust, 0, 10, orders, properties);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param exampleEntity
	 *            实体对象
	 * @param startIndex
	 *            起始位置,从0开始
	 * @param endIndex
	 *            结束位置
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @return List&lt;T&gt; 分页查询的结果列表
	 */
	public <T> List<T> findPartialByExample(T exampleEntity, int startIndex,
			int endIndex, List<SortOrder> orders,
			List<CriteriaProperty> properties) {
		Example example = Example.create(exampleEntity);
		Class<T> clz = (Class<T>) exampleEntity.getClass();

		return this.<T> findPartialByCriterion(clz, example, startIndex,
				endIndex, orders, properties);
	}

	/**
	 * <pre>
	 * 使用命名查询取部分数据
	 * 	示例:
	 * 		List&lt;Cust&gt; custs = dao.findPartialByNamedQuery("queryCustByNameAndAge", 0, 10 "007", 21);
	 * 	其中,queryCustByNameAndAge 在 hbm.xml 文件定义,示例:
	 * 		&lt;hibernate-mapping>
	 * 			&lt;class name="com.duker.mygift.model.Cust" table="CUST">
	 * 				&lt;id name="id" type="java.lang.Long">
	 * 					&lt;column name="ID" precision="10" scale="0" />
	 * 					&lt;generator class="assigned" />
	 * 				&lt;/id>
	 * 				&lt;property name="name" type="java.lang.String">
	 * 					&lt;column name="NAME" length="50" />
	 *				&lt;/property>
	 *				&lt;property name="age" type="java.lang.Integer">
	 *					&lt;column name="AGE"/>
	 *				&lt;/property>
	 *				&lt;query name="queryCustByNameAndAge">
	 *					&lt;![CDATA[
	 *						from Cust cust where cust.name = ? and cust.age = ?
	 *					]]>
	 *				&lt;/query>
	 *			&lt;/class>
	 *		&lt;/hibernate-mapping>
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param queryName
	 *            查询名
	 * @param startIndex
	 *            起始位置,从0开始
	 * @param endIndex
	 *            结束位置
	 * @param values
	 *            参数值
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findPartialByNamedQuery(String queryName,
			int startIndex, int endIndex, Object... values) {
		if (startIndex < 0 || endIndex <= startIndex) {
			return null;
		}

		Session session = sessionFactory.getCurrentSession();
		Query namedQuery = session.getNamedQuery(queryName);

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				namedQuery.setParameter(Integer.toString(i), values[i]);
			}
		}

		namedQuery.setFirstResult(startIndex);
		namedQuery.setMaxResults(endIndex - startIndex);

		return namedQuery.list();
	}

	/**
	 * <pre>
	 * 根据指定的entityClass、sql、startIndex、endIndex、参数值 取部分数据
	 * 示例1:
	 * 		String sql = "select * from cust where age > ?";
	 * 		List&lt;Cust&gt; custs = dao.findPartialBySQL(Cust.class, sql, 0, 10, null, 1);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类型
	 * @param sql
	 *            sql语句
	 * @param startIndex
	 *            起始位置,从0开始
	 * @param endIndex
	 *            结束位置
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param scalars
	 *            scalar
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @param values
	 *            参数值
	 * @return List&lt;T&gt; 分页查询的结果列表
	 */
	public <T> List<T> findPartialBySQL(Class<T> entityClass, String sql,
			int startIndex, int endIndex, List<SortOrder> orders,
			Map<String, Type> scalars, ResultTransformer transformer,
			Object... values) {
		return this.<T> findPartialBySQL(entityClass, null, sql, startIndex,
				endIndex, orders, scalars, transformer, values);
	}

	/**
	 * <pre>
	 * 根据指定的entityClass、sql、startIndex、endIndex、参数值 取部分数据
	 * 示例1:
	 * 		String sql = "select * from cust where age > ?";
	 * 		List&lt;Cust&gt; custs = dao.findPartialBySQL(Cust.class, sql, null, 0, 10, null, 1);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类型
	 * @param tableAlias
	 *            根结点别名
	 * @param sql
	 *            sql语句
	 * @param startIndex
	 *            起始位置,从0开始
	 * @param endIndex
	 *            结束位置
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param scalars
	 *            scalar
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @param values
	 *            参数值
	 * @return List&lt;T&gt; 分页查询的结果列表
	 */
	public <T> List<T> findPartialBySQL(Class<T> entityClass,
			String tableAlias, String sql, int startIndex, int endIndex,
			List<SortOrder> orders, Map<String, Type> scalars,
			ResultTransformer transformer, Object... values) {
		if (startIndex < 0 || endIndex <= startIndex) {
			return null;
		}

		sql = getNamedHql(sql, values);
		Session session = sessionFactory.getCurrentSession();
		SQLQuery queryObject = null;

		if (orders != null && !orders.isEmpty()) {
			StringBuilder pageSql = new StringBuilder(sql);

			pageSql.append(" order by ");
			for (Order order : orders) {
				pageSql.append(order.toString());
				pageSql.append(", ");
			}
			if (this.orderById.get()) {
				ClassMetadata meta = sessionFactory
						.getClassMetadata(entityClass);
				if (meta instanceof AbstractEntityPersister) {
					AbstractEntityPersister entityPersister = (AbstractEntityPersister) meta;
					String[] ids = entityPersister.getIdentifierColumnNames();
					if (ids != null && ids.length > 0) {
						for (String id : ids) {
							pageSql.append(id);
							pageSql.append(", ");
						}
					}
				}
			}
			int len = pageSql.length();
			pageSql.delete(len - 2, len);

			queryObject = session.createSQLQuery(pageSql.toString());
		}
		else {
			queryObject = session.createSQLQuery(sql);
		}

		if (entityClass != null) {
			if (tableAlias != null) {
				queryObject.addEntity(tableAlias, entityClass);
			}
			else {
				queryObject.addEntity(entityClass);
			}
		}

		if (transformer != null) {
			queryObject.setResultTransformer(transformer);
		}

		if (scalars != null) {
			for (Entry<String, Type> entry : scalars.entrySet()) {
				queryObject.addScalar(entry.getKey(), entry.getValue());
			}
		}

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(Integer.toString(i), values[i]);
			}
		}

		queryObject.setFirstResult(startIndex);
		queryObject.setMaxResults(endIndex - startIndex);

		return queryObject.list();
	}

	/**
	 * <pre>
	 * 根据指定的实体类、sql语句和sql参数列表，查询部分数据对象列表，返回的实体为非受管实体
	 * 	示例:
	 * 		String sql = "select cust_id as custId, cust_name as custName from cust where id = ? and sex = ? ";
	 * 		List&lt;Cust&gt; custs = dao.findPartialBySQLDetached(Cust.class, sql, 1, 10, null, 100, 1);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类, 如结果集不对应实体，填null即可
	 * @param sql
	 *            指定的sql语句
	 * @param startIndex
	 *            起始位置,从0开始
	 * @param endIndex
	 *            结束位置
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param scalars
	 *            scalar
	 * @param values
	 *            参数列表
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findPartialBySQLDetached(Class<T> entityClass,
			String sql, int startIndex, int endIndex, List<SortOrder> orders,
			Map<String, Type> scalars, Object... values) {
		if (startIndex < 0 || endIndex <= startIndex) {
			return null;
		}

		sql = getNamedHql(sql, values);
		Session session = sessionFactory.getCurrentSession();
		SQLQuery queryObject = null;

		if (orders != null && !orders.isEmpty()) {
			StringBuilder pageSql = new StringBuilder(sql);

			pageSql.append(" order by ");
			for (Order order : orders) {
				pageSql.append(order.toString());
				pageSql.append(", ");
			}
			if (this.orderById.get()) {
				ClassMetadata meta = sessionFactory
						.getClassMetadata(entityClass);
				if (meta instanceof AbstractEntityPersister) {
					AbstractEntityPersister entityPersister = (AbstractEntityPersister) meta;
					String[] ids = entityPersister.getIdentifierColumnNames();
					if (ids != null && ids.length > 0) {
						for (String id : ids) {
							pageSql.append(id);
							pageSql.append(", ");
						}
					}
				}
			}
			int len = pageSql.length();
			pageSql.delete(len - 2, len);

			queryObject = session.createSQLQuery(pageSql.toString());
		}
		else {
			queryObject = session.createSQLQuery(sql);
		}

		String[] aliaes = getAliaes(sql);
		Type[] types = getTypes(entityClass, aliaes);

		if (types != null) {
			for (int i = 0; i < types.length; i++) {
				queryObject.addScalar(aliaes[i], types[i]);
			}
		}

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(Integer.toString(i), values[i]);
			}
		}

		if (scalars != null) {
			for (Entry<String, Type> entry : scalars.entrySet()) {
				queryObject.addScalar(entry.getKey(), entry.getValue());
			}
		}

		queryObject.setFirstResult(startIndex);
		queryObject.setMaxResults(endIndex - startIndex);

		List<T> list = getObjectList(entityClass, queryObject.list(), aliaes);

		return list;
	}

	/**
	 * <pre>
	 * 根据指定的hql、参数来取第top条数据
	 * 示例:
	 *		String hql = "from Cust as cust where cust.age > ?";
	 * 		Cust cust = dao.findTop(hql, 1, null, 0);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param hql
	 *            hql语句
	 * @param top
	 *            第几个结果,从1开始
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param values
	 *            参数值
	 * @return 第top条结果
	 */
	public <T> T findTop(String hql, int top, List<SortOrder> orders,
			Object... values) {
		if (top < 1) {
			return null;
		}

		Session session = sessionFactory.getCurrentSession();
		Query queryObject = null;
		hql = getNamedHql(hql, values);

		if (orders != null && !orders.isEmpty()) {
			StringBuilder pageHql = new StringBuilder(hql);

			pageHql.append(" order by ");
			for (Order order : orders) {
				pageHql.append(order.toString());
				pageHql.append(", ");
			}
			if (this.orderById.get()) {
				String hql1 = hql.toLowerCase();
				int fromIndex = hql1.indexOf("from");
				int selectIndex = hql1.indexOf("select");
				if (fromIndex != -1 && selectIndex != -1
						&& selectIndex < fromIndex) {
					String prefix = hql
							.substring(selectIndex + 6, fromIndex)
							.trim();
					String[] ss = prefix.split("\\s*,\\s*");
					for (String s : ss) {
						String[] sss = s.split("\\s+");
						pageHql.append(sss[sss.length - 1].trim());
						pageHql.append(".id, ");
					}
					int len = pageHql.length();
					pageHql.delete(len - 2, len);
				}
				else {
					pageHql.append("id");
				}
			}
			else {
				int len = pageHql.length();
				pageHql.delete(len - 2, len);
			}

			queryObject = session.createQuery(pageHql.toString());
		}
		else {
			queryObject = session.createQuery(hql);
		}

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(Integer.toString(i), values[i]);
			}
		}

		queryObject.setFirstResult(top - 1);
		queryObject.setMaxResults(1);
		List<T> res = queryObject.list();
		if (res != null && !res.isEmpty()) {
			return res.get(0);
		}

		return null;
	}

	/**
	 * 根据指定的实体类、Criterion、Order取第top条数据
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param top
	 *            第几个结果,从1开始
	 * @return 第top条结果
	 */
	public <T> T findTopByCriterion(Class<T> entityClass, Criterion criterion,
			int top) {
		return this.<T> findTopByCriterion(entityClass.getName(), criterion,
				top);
	}

	/**
	 * 根据指定的实体类、Criterion、Order取第top条数据
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param top
	 *            第几个结果,从1开始
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @return 第top条结果
	 */
	public <T> T findTopByCriterion(Class<T> entityClass, Criterion criterion,
			int top, List<SortOrder> orders) {
		return this.<T> findTopByCriterion(entityClass.getName(), criterion,
				top, orders);
	}

	/**
	 * <pre>
	 * 根据指定的实体类、Criterion、Order取第top条数据
	 * 示例1:
	 * 		Criterion c1 = null;
	 *		Criterion c2 = null;
	 *		Criterion c3 = null;
	 *		Conjunction conjunction = null;
	 *
	 *		c1 = Restrictions.eq("name", "007");
	 *		c2 = Restrictions.eq("age", "20");
	 *		c3 = Restrictions.between("birthday", beginDate, endDate);
	 *		conjunction = Restrictions.conjunction();
	 *		conjunction.add(c1).add(c2).add(c3);
	 *
	 *		List&lt;Order&gt; orders = new ArrayList&lt;Order&gt;();
	 *		orders.add(SortOrder.asc("id"));
	 *		List&lt;CriteriaProperty&gt; properties = new ArrayList&lt;CriteriaProperty&gt;(1);
	 *		properties.add(new CriteriaProperty("user", FetchMode.JOIN));
	 *
	 * 		Cust cust = dao.findTopByCriterion(Cust.class, conjunction, 1, orders, properties);
	 * 		//Cust cust = dao.findTopByCriterion(Cust.class, conjunction, 1, null, null);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param top
	 *            第几个结果,从1开始
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @return 第top条结果
	 */
	public <T> T findTopByCriterion(Class<T> entityClass, Criterion criterion,
			int top, List<SortOrder> orders, List<CriteriaProperty> properties) {
		return this.<T> findTopByCriterion(entityClass.getName(), criterion,
				top, orders, properties);
	}

	/**
	 * 根据指定的实体名、criterion取第top条数据
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param top
	 *            第几个结果,从1开始
	 * @return 第top条结果
	 */
	public <T> T findTopByCriterion(String entityName, Criterion criterion,
			int top) {
		if (top < 1) {
			return null;
		}

		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		// list
		criteria.setFirstResult(top - 1);
		criteria.setMaxResults(1);
		List<T> res = criteria.list();
		if (res != null && !res.isEmpty()) {
			return res.get(0);
		}

		return null;
	}

	/**
	 * 根据指定的实体名、criterion、orders取第top条数据
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param top
	 *            第几个结果,从1开始
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @return 第top条结果
	 */
	public <T> T findTopByCriterion(String entityName, Criterion criterion,
			int top, List<SortOrder> orders) {
		if (top < 1) {
			return null;
		}

		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		// list
		criteria.setFirstResult(top - 1);
		criteria.setMaxResults(1);

		if (orders != null) {
			for (Order order : orders) {
				criteria.addOrder(order);
			}
			criteria.addOrder(SortOrder.asc("id"));
		}

		List<T> res = criteria.list();
		if (res != null && !res.isEmpty()) {
			return res.get(0);
		}

		return null;
	}

	/**
	 * 根据指定的实体名、criterion、orders取第top条数据
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param top
	 *            第几个结果,从1开始
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @return 第top条结果
	 */
	public <T> T findTopByCriterion(String entityName, Criterion criterion,
			int top, List<SortOrder> orders, List<CriteriaProperty> properties) {
		if (top < 1) {
			return null;
		}

		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		if (properties != null) {
			for (CriteriaProperty property : properties) {
				String prop = property.getProp();
				String alias = property.getAlias();
				JoinType joinType = property.getJoinType();
				FetchMode fetchMode = property.getFetchMode();

				if (StringUtils.isNotBlank(alias)) {
					if (joinType == null) {
						criteria.createAlias(prop, alias);
					}
					else {
						criteria.createAlias(prop, alias, joinType);
					}
				}
				if (fetchMode != null) {
					criteria.setFetchMode(prop, fetchMode);
				}
			}
		}

		// list
		criteria.setFirstResult(top - 1);
		criteria.setMaxResults(1);

		if (orders != null) {
			for (Order order : orders) {
				criteria.addOrder(order);
			}
			criteria.addOrder(SortOrder.asc("id"));
		}

		List<T> res = criteria.list();
		if (res != null && !res.isEmpty()) {
			return res.get(0);
		}

		return null;
	}

	/**
	 * 使用Hibernate的org.hibernate.criterion.Example取第top条数据
	 * 
	 * @param <T>
	 *            实体类型
	 * @param exampleEntity
	 *            实体对象
	 * @param top
	 *            第几个结果,从1开始
	 * @return 第top条结果
	 */
	public <T> T findTopByExample(T exampleEntity, int top) {
		Example example = Example.create(exampleEntity);
		Class<T> clz = (Class<T>) exampleEntity.getClass();

		return this.<T> findTopByCriterion(clz, example, top);
	}

	/**
	 * 使用Hibernate的org.hibernate.criterion.Example取第top条数据
	 * 
	 * @param <T>
	 *            实体类型
	 * @param exampleEntity
	 *            实体对象
	 * @param top
	 *            第几个结果,从1开始
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @return 第top条结果
	 */
	public <T> T findTopByExample(T exampleEntity, int top,
			List<SortOrder> orders) {
		Example example = Example.create(exampleEntity);
		Class<T> clz = (Class<T>) exampleEntity.getClass();

		return this.<T> findTopByCriterion(clz, example, top, orders);
	}

	/**
	 * <pre>
	 * 使用Hibernate的org.hibernate.criterion.Example取第top条数据
	 * 	示例：
	 * 		Cust cust = new Cust();
	 * 		cust.setName("007");
	 * 		cust.setAge(20);
	 * 		
	 * 		List&lt;Order&gt; orders = new ArrayList&lt;Order&gt;();
	 * 		orders.add(SortOrder.asc("id"));
	 *		List&lt;CriteriaProperty&gt; properties = new ArrayList&lt;CriteriaProperty&gt;();
	 *		properties.add(new CriteriaProperty("user", FetchMode.JOIN));
	 * 
	 * 		Cust cust = dao.findTopByExample(cust, 1, orders, properties);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param exampleEntity
	 *            实体对象
	 * @param top
	 *            第几个结果,从1开始
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @return 第top条结果
	 */
	public <T> T findTopByExample(T exampleEntity, int top,
			List<SortOrder> orders, List<CriteriaProperty> properties) {
		Example example = Example.create(exampleEntity);
		Class<T> clz = (Class<T>) exampleEntity.getClass();

		return this.<T> findTopByCriterion(clz, example, top, orders,
				properties);
	}

	/**
	 * <pre>
	 * 使用命名查询取第top条数据
	 * 	示例:
	 * 		Cust cust = dao.findTopByNamedQuery("queryCustByNameAndAge", 1, "007", 21);
	 * 	其中,queryCustByNameAndAge 在 hbm.xml 文件定义,示例:
	 * 		&lt;hibernate-mapping>
	 * 			&lt;class name="com.duker.mygift.model.Cust" table="CUST">
	 * 				&lt;id name="id" type="java.lang.Long">
	 * 					&lt;column name="ID" precision="10" scale="0" />
	 * 					&lt;generator class="assigned" />
	 * 				&lt;/id>
	 * 				&lt;property name="name" type="java.lang.String">
	 * 					&lt;column name="NAME" length="50" />
	 *				&lt;/property>
	 *				&lt;property name="age" type="java.lang.Integer">
	 *					&lt;column name="AGE"/>
	 *				&lt;/property>
	 *				&lt;query name="queryCustByNameAndAge">
	 *					&lt;![CDATA[
	 *						from Cust cust where cust.name = ? and cust.age = ?
	 *					]]>
	 *				&lt;/query>
	 *			&lt;/class>
	 *		&lt;/hibernate-mapping>
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param queryName
	 *            查询名
	 * @param top
	 *            第几个结果,从1开始
	 * @param values
	 *            参数值
	 * @return &lt;T&gt; 实体列表
	 */
	public <T> T findTopByNamedQuery(String queryName, int top,
			Object... values) {
		if (top < 1) {
			return null;
		}

		Session session = sessionFactory.getCurrentSession();
		Query namedQuery = session.getNamedQuery(queryName);

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				namedQuery.setParameter(Integer.toString(i), values[i]);
			}
		}

		namedQuery.setFirstResult(top - 1);
		namedQuery.setMaxResults(1);

		List<T> res = namedQuery.list();
		if (res != null && !res.isEmpty()) {
			return res.get(0);
		}

		return null;
	}

	/**
	 * 根据指定的entityClass、sql参数值取第top条数据
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类型
	 * @param sql
	 *            sql语句
	 * @param top
	 *            第几个结果,从1开始
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param scalars
	 *            scalar
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @param values
	 *            参数值
	 * @return 第top条结果
	 */
	public <T> T findTopBySQL(Class<T> entityClass, String sql, int top,
			List<SortOrder> orders, Map<String, Type> scalars,
			ResultTransformer transformer, Object... values) {
		return this.<T> findTopBySQL(entityClass, null, sql, top, orders,
				scalars, transformer, values);
	}

	/**
	 * <pre>
	 * 根据指定的entityClass、sql参数值取第top条数据
	 * 示例1:
	 * 		String sql = "select * from cust where age > ?";
	 * 		Cust cust = dao.findTopBySQL(Cust.class, sql, null, 1, null, null, 1);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类型
	 * @param tableAlias
	 *            根结点别名
	 * @param sql
	 *            sql语句
	 * @param top
	 *            第几个结果,从1开始
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param scalars
	 *            scalar
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @param values
	 *            参数值
	 * @return 第top条结果
	 */
	public <T> T findTopBySQL(Class<T> entityClass, String tableAlias,
			String sql, int top, List<SortOrder> orders,
			Map<String, Type> scalars, ResultTransformer transformer,
			Object... values) {
		if (top < 1) {
			return null;
		}

		sql = getNamedHql(sql, values);
		Session session = sessionFactory.getCurrentSession();
		SQLQuery queryObject = null;

		if (orders != null && !orders.isEmpty()) {
			StringBuilder pageSql = new StringBuilder(sql);

			pageSql.append(" order by ");
			for (Order order : orders) {
				pageSql.append(order.toString());
				pageSql.append(", ");
			}
			if (this.orderById.get()) {
				ClassMetadata meta = sessionFactory
						.getClassMetadata(entityClass);
				if (meta instanceof AbstractEntityPersister) {
					AbstractEntityPersister entityPersister = (AbstractEntityPersister) meta;
					String[] ids = entityPersister.getIdentifierColumnNames();
					if (ids != null && ids.length > 0) {
						for (String id : ids) {
							pageSql.append(id);
							pageSql.append(", ");
						}
					}
				}
			}
			int len = pageSql.length();
			pageSql.delete(len - 2, len);

			queryObject = session.createSQLQuery(pageSql.toString());
		}
		else {
			queryObject = session.createSQLQuery(sql);
		}

		if (entityClass != null) {
			if (tableAlias != null) {
				queryObject.addEntity(tableAlias, entityClass);
			}
			else {
				queryObject.addEntity(entityClass);
			}
		}

		if (transformer != null) {
			queryObject.setResultTransformer(transformer);
		}

		if (scalars != null) {
			for (Entry<String, Type> entry : scalars.entrySet()) {
				queryObject.addScalar(entry.getKey(), entry.getValue());
			}
		}

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(Integer.toString(i), values[i]);
			}
		}

		queryObject.setFirstResult(top - 1);
		queryObject.setMaxResults(1);
		List<T> res = queryObject.list();
		if (res != null && !res.isEmpty()) {
			return res.get(0);
		}

		return null;
	}

	/**
	 * <pre>
	 * 根据指定的实体类、sql语句和sql参数列表，查询第top条数据对象，返回的实体为非受管实体
	 * 	示例:
	 * 		String sql = "select cust_id as custId, cust_name as custName from cust where id = ? and sex = ? ";
	 * 		Cust cust = dao.findTopBySQLDetached(Cust.class, sql, 1, null, 100, 1);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类, 如结果集不对应实体，填null即可
	 * @param sql
	 *            指定的sql语句
	 * @param top
	 *            第几个结果,从1开始
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param scalars
	 *            scalar
	 * @param values
	 *            参数列表
	 * @return 第top条结果
	 */
	public <T> T findTopBySQLDetached(Class<T> entityClass, String sql,
			int top, List<SortOrder> orders, Map<String, Type> scalars,
			Object... values) {
		if (top < 1) {
			return null;
		}

		sql = getNamedHql(sql, values);
		Session session = sessionFactory.getCurrentSession();
		SQLQuery queryObject = null;

		if (orders != null && !orders.isEmpty()) {
			StringBuilder pageSql = new StringBuilder(sql);

			pageSql.append(" order by ");
			for (Order order : orders) {
				pageSql.append(order.toString());
				pageSql.append(", ");
			}
			if (this.orderById.get()) {
				ClassMetadata meta = sessionFactory
						.getClassMetadata(entityClass);
				if (meta instanceof AbstractEntityPersister) {
					AbstractEntityPersister entityPersister = (AbstractEntityPersister) meta;
					String[] ids = entityPersister.getIdentifierColumnNames();
					if (ids != null && ids.length > 0) {
						for (String id : ids) {
							pageSql.append(id);
							pageSql.append(", ");
						}
					}
				}
			}
			int len = pageSql.length();
			pageSql.delete(len - 2, len);

			queryObject = session.createSQLQuery(pageSql.toString());
		}
		else {
			queryObject = session.createSQLQuery(sql);
		}

		String[] aliaes = getAliaes(sql);
		Type[] types = getTypes(entityClass, aliaes);

		if (types != null) {
			for (int i = 0; i < types.length; i++) {
				queryObject.addScalar(aliaes[i], types[i]);
			}
		}

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(Integer.toString(i), values[i]);
			}
		}

		if (scalars != null) {
			for (Entry<String, Type> entry : scalars.entrySet()) {
				queryObject.addScalar(entry.getKey(), entry.getValue());
			}
		}

		queryObject.setFirstResult(top - 1);
		queryObject.setMaxResults(1);

		List<T> res = getObjectList(entityClass, queryObject.list(), aliaes);
		if (res != null && !res.isEmpty()) {
			return res.get(0);
		}

		return null;
	}

	/**
	 * 持久化对象
	 * 
	 * @param entity
	 *            实体对象
	 */
	public void persist(Object entity) {
		sessionFactory.getCurrentSession().persist(entity);
	}

	/**
	 * 持久化对象
	 * 
	 * @param entityName
	 *            实体名
	 * @param entity
	 *            实体对象
	 */
	public void persist(String entityName, Object entity) {
		sessionFactory.getCurrentSession().persist(entityName, entity);
	}

	/**
	 * 更新对象状态
	 * 
	 * @param entity
	 *            实体
	 */
	public void refresh(Object entity) {
		sessionFactory.getCurrentSession().refresh(entity);
	}

	/**
	 * 更新对象状态
	 * 
	 * @param entity
	 *            实体
	 * @param lockOptions
	 *            锁模式
	 */
	public void refresh(Object entity, LockOptions lockOptions) {
		Session session = sessionFactory.getCurrentSession();
		if (lockOptions != null) {
			session.refresh(entity, lockOptions);
		}
		else {
			session.refresh(entity);
		}
	}

	/**
	 * 保存一个处于transient状态的对象, 对应数据库的insert. 处于transient状态的对象没有主键信息,与数据库记录没有任何关联
	 * 
	 * @param transientInstance
	 *            处于transient状态的对象
	 * @return Serializable 主键值
	 */
	public Serializable save(Object transientInstance) {
		return sessionFactory.getCurrentSession().save(transientInstance);
	}

	/**
	 * 保存多个处于transient状态的对象, 对应数据库的insert. 处于transient状态的对象没有主键信息,与数据库记录没有任何关联
	 * 
	 * @param entities
	 *            处于transient状态的对象集合
	 * @return Serializable 主键值
	 */
	public void saveAll(Collection<?> entities) {
		Session session = sessionFactory.getCurrentSession();
		// 禁用二级缓存
		session.setCacheMode(CacheMode.IGNORE);

		for (Object entity : entities) {
			session.save(entity);
		}
	}

	/**
	 * 保存一个处于transient状态的对象, 对应数据库的insert. 处于transient状态的对象没有主键信息,与数据库记录没有任何关联
	 * 
	 * @param entityName
	 *            实体名
	 * @param entity
	 *            实体对象
	 * @return Serializable 主键值
	 */
	public Serializable save(String entityName, Object entity) {
		return sessionFactory.getCurrentSession().save(entityName, entity);
	}

	/**
	 * 保存一个处于transient状态的对象, 对应数据库的insert. 处于transient状态的对象没有主键信息,与数据库记录没有任何关联
	 * 
	 * @param transientInstance
	 *            处于transient状态的对象
	 * @param replicationMode
	 *            覆盖行为
	 */
	public void replicate(Object transientInstance,
			ReplicationMode replicationMode) {
		sessionFactory.getCurrentSession().replicate(transientInstance,
				replicationMode);
	}

	/**
	 * 保存一个处于transient状态的对象, 对应数据库的insert. 处于transient状态的对象没有主键信息,与数据库记录没有任何关联
	 * 
	 * @param entityName
	 *            实体名
	 * @param entity
	 *            实体对象
	 * @param replicationMode
	 *            覆盖行为
	 */
	public void replicate(String entityName, Object entity,
			ReplicationMode replicationMode) {
		sessionFactory.getCurrentSession().replicate(entityName, entity,
				replicationMode);
	}

	/**
	 * 保存对象. 如果对象处于transient状态,则insert进数据库, 否则update
	 * 
	 * @param entity
	 *            实体对象
	 */
	public void saveOrUpdate(Object entity) {
		sessionFactory.getCurrentSession().saveOrUpdate(entity);
	}

	/**
	 * 保存对象. 如果对象处于transient状态,则insert进数据库, 否则update
	 * 
	 * @param entityName
	 *            实体名
	 * @param entity
	 *            实体对象
	 */
	public void saveOrUpdate(String entityName, Object entity) {
		sessionFactory.getCurrentSession().saveOrUpdate(entityName, entity);
	}

	/**
	 * 保存指定的实体对象列表
	 * 
	 * @param entities
	 *            实体对象列表
	 */
	public void saveOrUpdateAll(Collection<?> entities) {
		Session session = sessionFactory.getCurrentSession();
		// 禁用二级缓存
		session.setCacheMode(CacheMode.IGNORE);

		for (Object entity : entities) {
			session.saveOrUpdate(entity);
		}
	}

	/**
	 * <pre>
	 * 根据指定的hql和参数列表，查询唯一对象
	 * 如果查询的结果多于1条,则抛出 NonUniqueResultException
	 * 	示例：
	 * 		String hql = "from Cust as cust where cust.id = ? ";
	 * 		Cust cust = dao.uniqueResult(hql, 1L);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param hql
	 *            指定的hql语句
	 * @param values
	 *            参数列表
	 * @return T 唯一对象
	 */
	public <T> T uniqueResult(String hql, Object... values) {
		Session session = sessionFactory.getCurrentSession();
		Query queryObject = session.createQuery(getNamedHql(hql, values));

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(Integer.toString(i), values[i]);
			}
		}

		return (T) queryObject.uniqueResult();
	}

	/**
	 * <pre>
	 * 根据指定的实体类、Criterion取得唯一实体
	 * 	示例：
	 *		Cust cust = dao.uniqueResultByCriterion(Cust.class, Restrictions.eq("id", 1L));
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            hibernate查询标准
	 * @return T 实体
	 */
	public <T> T uniqueResultByCriterion(Class<T> entityClass,
			Criterion criterion) {
		return this.<T> uniqueResultByCriterion(entityClass.getName(),
				criterion);
	}

	/**
	 * <pre>
	 * 根据指定的实体类、Criterion取得唯一实体
	 * 	示例：
	 *		List&lt;CriteriaProperty&gt; properties = new ArrayList&lt;CriteriaProperty&gt;();
	 *		properties.add(new CriteriaProperty("user", FetchMode.JOIN));
	 *		Cust cust = dao.uniqueResultByCriterion(Cust.class, Restrictions.eq("id", 1L, properties));
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            hibernate查询标准
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @return T 实体
	 */
	public <T> T uniqueResultByCriterion(Class<T> entityClass,
			Criterion criterion, List<CriteriaProperty> properties) {
		return this.<T> uniqueResultByCriterion(entityClass.getName(),
				criterion, properties);
	}

	/**
	 * 根据指定的实体类、Criterion取得唯一实体
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            hibernate查询标准
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @param projection
	 *            查询结果投影,可为空
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @return T 实体
	 */
	public <T> T uniqueResultByCriterion(Class<T> entityClass,
			Criterion criterion, List<CriteriaProperty> properties,
			Projection projection, ResultTransformer transformer) {
		return this.<T> uniqueResultByCriterion(entityClass.getName(),
				criterion, properties, projection, transformer);
	}

	/**
	 * 使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            　hibernate查询标准
	 * @return T 实体
	 */
	public <T> T uniqueResultByCriterion(String cacheRegion,
			Class<T> entityClass, Criterion criterion) {
		return this.<T> uniqueResultByCriterion(cacheRegion,
				entityClass.getName(), criterion);
	}

	/**
	 * 使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            　hibernate查询标准
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @return T 实体
	 */
	public <T> T uniqueResultByCriterion(String cacheRegion,
			Class<T> entityClass, Criterion criterion,
			List<CriteriaProperty> properties) {
		return this.<T> uniqueResultByCriterion(cacheRegion,
				entityClass.getName(), criterion, properties);
	}

	/**
	 * 使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityClass
	 *            实体类
	 * @param criterion
	 *            　hibernate查询标准
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @param projection
	 *            查询结果投影,可为空
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @return T 实体
	 */
	public <T> T uniqueResultByCriterion(String cacheRegion,
			Class<T> entityClass, Criterion criterion,
			List<CriteriaProperty> properties, Projection projection,
			ResultTransformer transformer) {
		return this.<T> uniqueResultByCriterion(cacheRegion,
				entityClass.getName(), criterion, properties, projection,
				transformer);
	}

	/**
	 * 根据指定的实体名、Criterion返回唯一实体
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @return T 实体
	 */
	public <T> T uniqueResultByCriterion(String entityName, Criterion criterion) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		return (T) criteria.uniqueResult();
	}

	/**
	 * 根据指定的实体名、Criterion返回唯一实体
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @return T 实体
	 */
	public <T> T uniqueResultByCriterion(String entityName,
			Criterion criterion, List<CriteriaProperty> properties) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		if (properties != null) {
			for (CriteriaProperty property : properties) {
				String prop = property.getProp();
				String alias = property.getAlias();
				JoinType joinType = property.getJoinType();
				FetchMode fetchMode = property.getFetchMode();

				if (StringUtils.isNotBlank(alias)) {
					if (joinType == null) {
						criteria.createAlias(prop, alias);
					}
					else {
						criteria.createAlias(prop, alias, joinType);
					}
				}
				if (fetchMode != null) {
					criteria.setFetchMode(prop, fetchMode);
				}
			}
		}

		return (T) criteria.uniqueResult();
	}

	/**
	 * 根据指定的实体名、Criterion返回唯一实体
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @param projection
	 *            查询结果投影,可为空
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @return T 实体
	 */
	public <T> T uniqueResultByCriterion(String entityName,
			Criterion criterion, List<CriteriaProperty> properties,
			Projection projection, ResultTransformer transformer) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		if (properties != null) {
			for (CriteriaProperty property : properties) {
				String prop = property.getProp();
				String alias = property.getAlias();
				JoinType joinType = property.getJoinType();
				FetchMode fetchMode = property.getFetchMode();

				if (StringUtils.isNotBlank(alias)) {
					if (joinType == null) {
						criteria.createAlias(prop, alias);
					}
					else {
						criteria.createAlias(prop, alias, joinType);
					}
				}
				if (fetchMode != null) {
					criteria.setFetchMode(prop, fetchMode);
				}
			}
		}

		if (projection != null) {
			criteria.setProjection(projection);
		}

		if (transformer != null) {
			criteria.setResultTransformer(transformer);
		}

		return (T) criteria.uniqueResult();
	}

	/**
	 * 使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @return T 实体
	 */
	public <T> T uniqueResultByCriterion(String cacheRegion, String entityName,
			Criterion criterion) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		criteria.setCacheable(true);
		criteria.setCacheRegion(cacheRegion);

		return (T) criteria.uniqueResult();
	}

	/**
	 * 使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @return T 实体
	 */
	public <T> T uniqueResultByCriterion(String cacheRegion, String entityName,
			Criterion criterion, List<CriteriaProperty> properties) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		if (properties != null) {
			for (CriteriaProperty property : properties) {
				String prop = property.getProp();
				String alias = property.getAlias();
				JoinType joinType = property.getJoinType();
				FetchMode fetchMode = property.getFetchMode();

				if (StringUtils.isNotBlank(alias)) {
					if (joinType == null) {
						criteria.createAlias(prop, alias);
					}
					else {
						criteria.createAlias(prop, alias, joinType);
					}
				}
				if (fetchMode != null) {
					criteria.setFetchMode(prop, fetchMode);
				}
			}
		}

		criteria.setCacheable(true);
		criteria.setCacheRegion(cacheRegion);

		return (T) criteria.uniqueResult();
	}

	/**
	 * 使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityName
	 *            实体名
	 * @param criterion
	 *            Criterion对象, 作为查询条件
	 * @param properties
	 *            调整抓取策略与关联属性别名,可为空
	 * @param projection
	 *            查询结果投影,可为空
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @return T 实体
	 */
	public <T> T uniqueResultByCriterion(String cacheRegion, String entityName,
			Criterion criterion, List<CriteriaProperty> properties,
			Projection projection, ResultTransformer transformer) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(entityName);

		if (criterion != null) {
			criteria.add(criterion);
		}

		if (properties != null) {
			for (CriteriaProperty property : properties) {
				String prop = property.getProp();
				String alias = property.getAlias();
				JoinType joinType = property.getJoinType();
				FetchMode fetchMode = property.getFetchMode();

				if (StringUtils.isNotBlank(alias)) {
					if (joinType == null) {
						criteria.createAlias(prop, alias);
					}
					else {
						criteria.createAlias(prop, alias, joinType);
					}
				}
				if (fetchMode != null) {
					criteria.setFetchMode(prop, fetchMode);
				}
			}
		}

		if (projection != null) {
			criteria.setProjection(projection);
		}

		if (transformer != null) {
			criteria.setResultTransformer(transformer);
		}

		criteria.setCacheable(true);
		criteria.setCacheRegion(cacheRegion);

		return (T) criteria.uniqueResult();
	}

	/**
	 * <pre>
	 * 根据指定的引用查询名称、bean来查询唯一对象
	 * 如果查询的结果多于1条,则抛出 NonUniqueResultException
	 * 	示例:
	 * 		Cust cust = dao.uniqueResultByNamedQuery("queryCustByNameAndAge", "007", 21);
	 * 	其中,queryCustByNameAndAge 在 hbm.xml 文件定义,示例:
	 * 		&lt;hibernate-mapping>
	 * 			&lt;class name="com.duker.mygift.model.Cust" table="CUST">
	 * 				&lt;id name="id" type="java.lang.Long">
	 * 					&lt;column name="ID" precision="10" scale="0" />
	 * 					&lt;generator class="assigned" />
	 * 				&lt;/id>
	 * 				&lt;property name="name" type="java.lang.String">
	 * 					&lt;column name="NAME" length="50" />
	 *				&lt;/property>
	 *				&lt;property name="age" type="java.lang.Integer">
	 *					&lt;column name="AGE"/>
	 *				&lt;/property>
	 *				&lt;query name="queryCustByNameAndAge">
	 *					&lt;![CDATA[
	 *						from Cust cust where cust.name = ? and cust.age = ?
	 *					]]>
	 *				&lt;/query>
	 *			&lt;/class>
	 *		&lt;/hibernate-mapping>
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param queryName
	 *            查询名
	 * @param values
	 *            　参数值
	 * @return T 唯一对象
	 */
	public <T> T uniqueResultByNamedQuery(String queryName, Object... values) {
		Session session = sessionFactory.getCurrentSession();
		Query namedQuery = session.getNamedQuery(queryName);

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				namedQuery.setParameter(Integer.toString(i), values[i]);
			}
		}

		return (T) namedQuery.uniqueResult();
	}

	/**
	 * 根据指定的引用查询名称、bean来查询唯一对象，使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param queryName
	 *            查询名
	 * @param values
	 *            　实体
	 * @return T 唯一对象
	 */
	public <T> T uniqueResultByNamedQuery(String cacheRegion, String queryName,
			Object... values) {
		Session session = sessionFactory.getCurrentSession();
		Query namedQuery = session.getNamedQuery(queryName);

		namedQuery.setCacheable(true);
		namedQuery.setCacheRegion(cacheRegion);

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				namedQuery.setParameter(Integer.toString(i), values[i]);
			}
		}

		return (T) namedQuery.uniqueResult();
	}

	/**
	 * <pre>
	 * 根据指定的实体类、以属性名(key)属性值(value)的Map, 以多个属性为条件来查询唯一对象
	 * 如果查询的结果多于1条,则抛出 NonUniqueResultException
	 * 	示例：
	 * 		Map<String, Object> propertities = new HashMap<String, Object>(3);
	 * 		propertities.put("name", "007");
	 * 		propertities.put("sex", 1);
	 * 		propertities.put("age", 30);
	 * 		Cust cust = dao.uniqueResult(Cust.class, propertities);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param propertities
	 *            　属性名值对
	 * @return T 唯一对象
	 */
	public <T> T uniqueResultByProperties(Class<T> entityClass,
			Map<String, Object> propertities) {
		return this.<T> uniqueResultByProperties(entityClass.getName(),
				propertities);
	}

	/**
	 * <pre>
	 * 根据指定的实体类、属性名列表、属性值列表, 以多个属性为条件来查询唯一对象
	 * 如果查询的结果多于1条,则抛出 NonUniqueResultException
	 * 注意：必须保证属性名列表、属性值列表中的属性名-值的对应关系
	 * 	示例：
	 * 		String propertyNames = {"name", "sex", "age"};
	 * 		Object propertyValues = {"007", 1, 30};
	 * 		Cust cust = dao.uniqueResult(Cust.class, propertyNames, propertyValues);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param propertyNames
	 *            　属性值数组
	 * @param propertyValues
	 *            　值数组
	 * @return T 唯一对象
	 */
	public <T> T uniqueResultByProperties(Class<T> entityClass,
			String[] propertyNames, Object[] propertyValues) {
		return this.<T> uniqueResultByProperties(entityClass.getName(),
				propertyNames, propertyValues);
	}

	/**
	 * 根据指定的实体类、以属性名(key)属性值(value)的Map, 以多个属性为条件来查询唯一对象，使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityClass
	 *            实体类
	 * @param propertities
	 *            　属性名值对
	 * @return T 唯一对象
	 */
	public <T> T uniqueResultByProperties(String cacheRegion,
			Class<T> entityClass, Map<String, Object> propertities) {
		return this.<T> uniqueResultByProperties(cacheRegion,
				entityClass.getName(), propertities);
	}

	/**
	 * 根据指定的实体类、属性名列表、属性值列表, 以多个属性为条件来查询唯一对象，使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityClass
	 *            实体类
	 * @param propertyNames
	 *            　属性值数组
	 * @param propertyValues
	 *            　值数组
	 * @return 唯一对象
	 */
	public <T> T uniqueResultByProperties(String cacheRegion,
			Class<T> entityClass, String[] propertyNames,
			Object[] propertyValues) {
		return this.<T> uniqueResultByProperties(cacheRegion,
				entityClass.getName(), propertyNames, propertyValues);
	}

	/**
	 * <pre>
	 * 根据指定的实体名、以属性名(key)属性值(value)的Map, 以多个属性为条件来查询唯一对象
	 * 如果查询的结果多于1条,则抛出 NonUniqueResultException
	 * 	示例：
	 * 		Map<String, Object> propertities = new LinkedHashMap<String, Object>(3);
	 * 		propertities.put("name", "007");
	 * 		propertities.put("sex", 1);
	 * 		propertities.put("age", 30);
	 * 		Cust cust = dao.uniqueResult("Cust", propertities);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名
	 * @param propertities
	 *            　属性名值对
	 * @return T 唯一对象
	 */
	public <T> T uniqueResultByProperties(String entityName,
			Map<String, Object> propertities) {
		if (propertities == null || propertities.size() == 0) {
			throw new NonUniqueResultException(Integer.MAX_VALUE);
		}

		Object[] propertyValues = new Object[propertities.size()];
		StringBuilder hql = new StringBuilder();

		hql.append("from ").append(entityName).append(" as model where ");
		Iterator<Entry<String, Object>> it = propertities.entrySet().iterator();

		for (int i = 0; it.hasNext(); i++) {
			Entry<String, Object> entry = it.next();
			hql.append(" model.")
					.append(entry.getKey())
					.append(" = ?")
					.append(i);
			propertyValues[i] = entry.getValue();

			if (it.hasNext()) {
				hql.append(" and");
			}
		}

		return this.<T> uniqueResult(hql.toString(), propertyValues);
	}

	/**
	 * 根据指定的实体名、以属性名(key)属性值(value)的Map, 以多个属性为条件来查询唯一对象，使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityName
	 *            实体名
	 * @param propertities
	 *            　属性名值对
	 * @return T 唯一对象
	 */
	public <T> T uniqueResultByProperties(String cacheRegion,
			String entityName, Map<String, Object> propertities) {
		if (propertities == null || propertities.size() == 0) {
			throw new NonUniqueResultException(Integer.MAX_VALUE);
		}

		Object[] propertyValues = new Object[propertities.size()];
		StringBuilder hql = new StringBuilder();

		hql.append("from ").append(entityName).append(" as model where ");
		Iterator<Entry<String, Object>> it = propertities.entrySet().iterator();

		for (int i = 0; it.hasNext(); i++) {
			Entry<String, Object> entry = it.next();
			hql.append(" model.")
					.append(entry.getKey())
					.append(" = ?")
					.append(i);
			propertyValues[i] = entry.getValue();

			if (it.hasNext()) {
				hql.append(" and");
			}
		}

		return this.<T> uniqueResultCached(cacheRegion, hql.toString(),
				propertyValues);
	}

	/**
	 * 根据指定的实体名、属性名列表、属性值列表, 以多个属性为条件来查询唯一对象，使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityName
	 *            实体名
	 * @param propertyNames
	 *            　属性名值数组
	 * @param propertyValues
	 *            　值数组
	 * @return T 唯一对象
	 */
	public <T> T uniqueResultByProperties(String cacheRegion,
			String entityName, String[] propertyNames, Object[] propertyValues) {
		boolean arrayLenEquals = (propertyNames == null) ? (propertyValues == null)
				: (propertyNames.length == propertyValues.length);

		if (!arrayLenEquals) {
			throw new IllegalArgumentException(
					"propertyNames.length not equals propertyValues.length");
		}

		if (propertyNames == null) {
			throw new NonUniqueResultException(Integer.MAX_VALUE);
		}

		StringBuilder hql = new StringBuilder();

		hql.append("from ").append(entityName).append(" as model where ");

		for (int i = 0, len = propertyNames.length; i < len; i++) {
			if (i != 0) {
				hql.append(" and");
			}

			hql.append(" model.")
					.append(propertyNames[i])
					.append(" = ?")
					.append(i);
		}

		return this.<T> uniqueResultCached(cacheRegion, hql.toString(),
				propertyValues);
	}

	/**
	 * <pre>
	 * 根据指定的实体名、属性名列表、属性值列表, 以多个属性为条件来查询唯一对象
	 * 如果查询的结果多于1条,则抛出 NonUniqueResultException
	 * <b>注意：</b>必须保证属性名列表、属性值列表中的属性名-值的对应关系
	 * 	示例：
	 * 		String propertyNames = {"name", "sex", "age"};
	 * 		Object propertyValues = {"007", 1, 30};
	 * 		Cust cust = dao.uniqueResult("Cust", propertyNames, propertyValues);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名
	 * @param propertyNames
	 *            　属性名值数组
	 * @param propertyValues
	 *            　值数组
	 * @return T 唯一对象
	 */
	public <T> T uniqueResultByProperties(String entityName,
			String[] propertyNames, Object[] propertyValues) {
		boolean arrayLenEquals = (propertyNames == null) ? (propertyValues == null)
				: (propertyNames.length == propertyValues.length);

		if (!arrayLenEquals) {
			throw new IllegalArgumentException(
					"propertyNames.length not equals propertyValues.length");
		}

		if (propertyNames == null) {
			throw new NonUniqueResultException(Integer.MAX_VALUE);
		}

		StringBuilder hql = new StringBuilder();

		hql.append("from ").append(entityName).append(" as model where ");

		for (int i = 0, len = propertyNames.length; i < len; i++) {
			if (i != 0) {
				hql.append(" and");
			}

			hql.append(" model.")
					.append(propertyNames[i])
					.append(" = ?")
					.append(i);
		}

		return this.<T> uniqueResult(hql.toString(), propertyValues);
	}

	/**
	 * <pre>
	 * 根据指定的实体类、属性名、属性值, 以单个属性为条件来查询唯一对象
	 * 如果查询的结果多于1条,则抛出 NonUniqueResultException
	 * 	示例：
	 * 		Cust cust = dao.uniqueResult(Cust.class, "name", "007");
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类
	 * @param propertyName
	 *            属性名
	 * @param propertyValue
	 *            属性值
	 * @return T 唯一对象
	 */
	public <T> T uniqueResultByProperty(Class<T> entityClass,
			String propertyName, Object propertyValue) {
		return this.<T> uniqueResultByProperty(entityClass.getName(),
				propertyName, propertyValue);
	}

	/**
	 * 根据指定的实体类、属性名、属性值, 以单个属性为条件来查询唯一对象，使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityClass
	 *            实体类
	 * @param propertyName
	 *            属性名
	 * @param propertyValue
	 *            属性值
	 * @return T 唯一对象
	 */
	public <T> T uniqueResultByProperty(String cacheRegion,
			Class<T> entityClass, String propertyName, Object propertyValue) {
		return this.<T> uniqueResultByProperty(cacheRegion,
				entityClass.getName(), propertyName, propertyValue);
	}

	/**
	 * <pre>
	 * 根据指定的实体名、属性名、属性值, 以单个属性为条件来查询唯一对象
	 * 如果查询的结果多于1条,则抛出 NonUniqueResultException
	 * 	示例：
	 * 		Cust cust = dao.uniqueResult("Cust", "name", "007"); 
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名
	 * @param propertyName
	 *            属性名
	 * @param propertyValue
	 *            属性值
	 * @return T 唯一对象
	 */
	public <T> T uniqueResultByProperty(String entityName, String propertyName,
			Object propertyValue) {
		StringBuilder hql = new StringBuilder();

		hql.append("from ").append(entityName);
		hql.append(" as model where model.")
				.append(propertyName)
				.append(" = ?0");

		return this.<T> uniqueResult(hql.toString(), propertyValue);
	}

	/**
	 * 根据指定的实体名、属性名、属性值, 以单个属性为条件来查询唯一对象，使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityName
	 *            实体名
	 * @param propertyName
	 *            属性名
	 * @param propertyValue
	 *            属性值
	 * @return T 唯一对象
	 */
	public <T> T uniqueResultByProperty(String cacheRegion, String entityName,
			String propertyName, Object propertyValue) {
		StringBuilder hql = new StringBuilder();

		hql.append("from ").append(entityName);
		hql.append(" as model where model.")
				.append(propertyName)
				.append(" = ?0");

		return this.<T> uniqueResultCached(cacheRegion, hql.toString(),
				propertyValue);
	}

	/**
	 * 根据指定的实体类、sql语句和sql参数列表，查询唯一记录
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类, 如结果集不对应实体，填null即可
	 * @param sql
	 *            sql语句
	 * @param scalars
	 *            scalar
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @param values
	 *            sql参数列表
	 * @return T 实体
	 */
	public <T> T uniqueResultBySQL(Class<T> entityClass, String sql,
			Map<String, Type> scalars, ResultTransformer transformer,
			Object... values) {
		return this.<T> uniqueResultBySQL(entityClass, null, sql, scalars,
				transformer, values);
	}

	/**
	 * <pre>
	 * 根据指定的实体类、sql语句和sql参数列表，查询唯一记录
	 * 示例1：
	 * 		String sql = "select * from cust where id = ? "
	 * 		Cust cust = dao.uniqueResult(Cust.class, sql, null, null, 100);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类, 如结果集不对应实体，填null即可
	 * @param tableAlias
	 *            根结点别名
	 * @param sql
	 *            sql语句
	 * @param scalars
	 *            scalar
	 * @param transformer
	 *            增加查询结果转换器,可为空
	 * @param values
	 *            sql参数列表
	 * @return T 实体
	 */
	public <T> T uniqueResultBySQL(Class<T> entityClass, String tableAlias,
			String sql, Map<String, Type> scalars,
			ResultTransformer transformer, Object... values) {
		sql = getNamedHql(sql, values);
		Session session = sessionFactory.getCurrentSession();
		SQLQuery queryObject = session.createSQLQuery(sql);

		if (entityClass != null) {
			if (tableAlias != null) {
				queryObject.addEntity(tableAlias, entityClass);
			}
			else {
				queryObject.addEntity(entityClass);
			}
		}

		if (transformer != null) {
			queryObject.setResultTransformer(transformer);
		}

		if (scalars != null) {
			for (Entry<String, Type> entry : scalars.entrySet()) {
				queryObject.addScalar(entry.getKey(), entry.getValue());
			}
		}

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(Integer.toString(i), values[i]);
			}
		}

		return (T) queryObject.uniqueResult();
	}

	/**
	 * 根据指定的实体类、sql语句和sql参数列表，查询唯一记录，使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityClass
	 *            实体类, 如结果集不对应实体，填null即可
	 * @param sql
	 *            sql语句
	 * @param scalars
	 *            scalar
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @param values
	 *            sql参数列表
	 * @return T 实体
	 */
	public <T> T uniqueResultBySQL(String cacheRegion, Class<T> entityClass,
			String sql, Map<String, Type> scalars,
			ResultTransformer transformer, Object... values) {
		return this.<T> uniqueResultBySQL(cacheRegion, entityClass, null, sql,
				scalars, transformer, values);
	}

	/**
	 * 根据指定的实体类、sql语句和sql参数列表，查询唯一记录，使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param entityClass
	 *            实体类, 如结果集不对应实体，填null即可
	 * @param tableAlias
	 *            根结点别名
	 * @param sql
	 *            sql语句
	 * @param scalars
	 *            scalar
	 * @param transformer
	 *            查询结果转换器,可为空
	 * @param values
	 *            sql参数列表
	 * @return T 实体
	 */
	public <T> T uniqueResultBySQL(String cacheRegion, Class<T> entityClass,
			String tableAlias, String sql, Map<String, Type> scalars,
			ResultTransformer transformer, Object... values) {
		sql = getNamedHql(sql, values);
		Session session = sessionFactory.getCurrentSession();
		SQLQuery queryObject = session.createSQLQuery(sql);

		if (entityClass != null) {
			if (tableAlias != null) {
				queryObject.addEntity(tableAlias, entityClass);
			}
			else {
				queryObject.addEntity(entityClass);
			}
		}

		if (transformer != null) {
			queryObject.setResultTransformer(transformer);
		}

		if (scalars != null) {
			for (Entry<String, Type> entry : scalars.entrySet()) {
				queryObject.addScalar(entry.getKey(), entry.getValue());
			}
		}

		queryObject.setCacheable(true);
		queryObject.setCacheRegion(cacheRegion);

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(Integer.toString(i), values[i]);
			}
		}

		return (T) queryObject.uniqueResult();
	}

	/**
	 * 根据指定的hql和参数列表，查询唯一对象，使用二级缓存
	 * 
	 * @param <T>
	 *            实体类型
	 * @param cacheRegion
	 *            缓存名
	 * @param hql
	 *            指定的hql语句
	 * @param values
	 *            参数列表
	 * @return T 唯一对象
	 */
	public <T> T uniqueResultCached(String cacheRegion, String hql,
			Object... values) {
		Session session = sessionFactory.getCurrentSession();
		Query queryObject = session.createQuery(getNamedHql(hql, values));

		queryObject.setCacheable(true);
		queryObject.setCacheRegion(cacheRegion);

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(Integer.toString(i), values[i]);
			}
		}

		return (T) queryObject.uniqueResult();
	}

	/**
	 * 更新一个处于persistent状态的对象, 对应数据库的update. 处于persistent状态的对象对应数据库的一条记录,
	 * 与某个session关联,并处于session的有效期内
	 * 
	 * @param persistentEntity
	 *            处于persistent状态的对象.
	 */
	public void update(Object persistentEntity) {
		sessionFactory.getCurrentSession().update(persistentEntity);
	}

	/**
	 * 更新多个处于persistent状态的对象, 对应数据库的update. 处于persistent状态的对象对应数据库的一条记录,
	 * 与某个session关联,并处于session的有效期内
	 * 
	 * @param entities
	 *            处于persistent状态的对象集合.
	 */
	public void updateAll(Collection<?> entities) {
		Session session = sessionFactory.getCurrentSession();
		// 禁用二级缓存
		session.setCacheMode(CacheMode.IGNORE);

		for (Object entity : entities) {
			session.update(entity);
		}
	}

	/**
	 * 指定更新一个处于persistent状态的对象
	 * 
	 * @param persistentEntity
	 *            处于persistent状态的对象.
	 * @param lockOptions
	 *            锁模式
	 */
	public void update(Object persistentEntity, LockOptions lockOptions) {
		Session session = sessionFactory.getCurrentSession();
		session.update(persistentEntity);
		if (lockOptions != null) {
			session.buildLockRequest(lockOptions).lock(persistentEntity);
		}
	}

	/**
	 * 指定更新一个处于persistent状态的对象
	 * 
	 * @param entityName
	 *            实体名
	 * @param persistentEntity
	 *            处于persistent状态的对象.
	 */
	public void update(String entityName, Object persistentEntity) {
		sessionFactory.getCurrentSession().update(entityName, persistentEntity);
	}

	/**
	 * 指定更新一个处于persistent状态的对象
	 * 
	 * @param entityName
	 *            实体名
	 * @param persistentEntity
	 *            处于persistent状态的对象.
	 * @param lockOptions
	 *            锁模式
	 */
	public void update(String entityName, Object persistentEntity,
			LockOptions lockOptions) {
		Session session = sessionFactory.getCurrentSession();
		session.update(entityName, persistentEntity);
		if (lockOptions != null) {
			session.buildLockRequest(lockOptions).lock(persistentEntity);
		}
	}

	/**
	 * 去掉SQL语句中括号之间的部分
	 * 
	 * @param result
	 *            去掉括号后的串
	 * @return 去掉括号前的串
	 */
	protected String dropBracket(String result) {
		while (true) {
			int start = result.indexOf("(");

			if (start == -1) {
				return result;
			}

			int length = result.length();
			int i = start + 1;
			int end;
			char c;
			int count = 1;

			while (i < length && count != 0) {
				c = result.charAt(i++);

				if (c == '(') {
					count++;
				}
				else if (c == ')') {
					count--;
				}
			}

			end = i;

			if (count == 0) {
				StringBuilder str = new StringBuilder();

				str.append(result.substring(0, start));
				str.append(result.substring(end));

				return dropBracket(str.toString());
			}
			return result;
		}
	}

	/**
	 * 取得SQL中的别名
	 * 
	 * @param sql
	 *            SQL语句
	 * @return 别名列表
	 */
	protected String[] getAliaes(String sql) {
		if (!StringUtils.isBlank(sql)) {
			sql = dropBracket(sql);

			String selectRegex = "[sS][eE][lL][eE][cC][tT] *";
			String fromRegex = " *[fF][rR][oO][mM]";
			Pattern p = Pattern.compile(selectRegex);
			Matcher m = p.matcher(sql);

			if (m.find()) {
				int left = m.end();

				p = Pattern.compile(fromRegex);
				m = p.matcher(sql);

				if (m.find()) {
					int right = m.start();

					if (right > left) {
						String str = sql.substring(left, right);
						String[] split = str.split("\\s*,\\s");
						String[] aliaes = new String[split.length];

						for (int i = 0; i < split.length; i++) {
							String[] ss = split[i].split(" +(as +)*");

							if (ss.length == 2) {
								aliaes[i] = ss[1];
							}
						}

						return aliaes;
					}

				}
			}
		}

		return null;
	}

	/**
	 * 根据结果集、属性名生成实体集合
	 * 
	 * @param entityClass
	 *            实体
	 * @param rs
	 *            结果集
	 * @param properties
	 *            属性
	 * @return List 实体集合
	 */
	protected <T> List<T> getObjectList(Class<T> entityClass, List<T> rs,
			String[] properties) {
		if (entityClass != null && properties != null && rs != null) {
			List<T> list = new ArrayList<T>(rs.size());
			Iterator<T> it = rs.iterator();

			while (it.hasNext()) {
				Object res = it.next();

				if (res.getClass().isArray()) {
					int length = Array.getLength(res);

					if (length == properties.length) {
						try {
							T obj = entityClass.newInstance();

							for (int i = 0; i < length; i++) {
								if (properties[i] != null) {
									try {
										Object v = Array.get(res, i);

										PropertyUtils.setProperty(obj,
												properties[i], v);
									}
									catch (Exception ex) {
									}
								}
							}

							list.add(obj);
						}
						catch (Exception ex) {
						}

					}
				}
			}

			if (list.size() > 0) {
				return list;
			}
		}

		return rs;
	}

	/**
	 * 取得Hibernate支持的类型
	 * 
	 * @param entityClass
	 *            实体类
	 * @param properties
	 *            属性列表
	 * @return 类型列表
	 */
	protected Type[] getTypes(Class<?> entityClass, String[] properties) {
		Type[] types = new Type[properties.length];

		for (int i = 0; i < properties.length; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append("get");
			sb.append(properties[i].substring(0, 1).toUpperCase());
			sb.append(properties[i].substring(1));

			try {
				Method method = entityClass.getMethod(sb.toString());
				Class<?> clz = method.getReturnType();

				if (clz == Long.class || clz == long.class) {
					types[i] = StandardBasicTypes.LONG;
				}
				else if (clz == Short.class || clz == short.class) {
					types[i] = StandardBasicTypes.SHORT;
				}
				else if (clz == Integer.class || clz == int.class) {
					types[i] = StandardBasicTypes.INTEGER;
				}
				else if (clz == Byte.class || clz == byte.class) {
					types[i] = StandardBasicTypes.BYTE;
				}
				else if (clz == Float.class || clz == float.class) {
					types[i] = StandardBasicTypes.FLOAT;
				}
				else if (clz == Double.class || clz == double.class) {
					types[i] = StandardBasicTypes.DOUBLE;
				}
				else if (clz == Character.class || clz == char.class) {
					types[i] = StandardBasicTypes.CHARACTER;
				}
				else if (clz == String.class) {
					types[i] = StandardBasicTypes.STRING;
				}
				else if (Date.class.isAssignableFrom(clz)) {
					types[i] = StandardBasicTypes.DATE;
				}
				else if (clz == Boolean.class || clz == boolean.class) {
					types[i] = StandardBasicTypes.BOOLEAN;
				}
				else if (BigDecimal.class.isAssignableFrom(clz)) {
					types[i] = StandardBasicTypes.BIG_DECIMAL;
				}
				else if (BigInteger.class.isAssignableFrom(clz)) {
					types[i] = StandardBasicTypes.BIG_INTEGER;
				}
				else if (clz == byte[].class) {
					types[i] = StandardBasicTypes.BINARY;
				}
				else if (clz == Byte[].class) {
					types[i] = StandardBasicTypes.WRAPPER_BINARY;
				}
				else if (clz == char[].class) {
					types[i] = StandardBasicTypes.CHAR_ARRAY;
				}
				else if (clz == Character[].class) {
					types[i] = StandardBasicTypes.CHARACTER_ARRAY;
				}
				else if (clz == Blob.class) {
					types[i] = StandardBasicTypes.BLOB;
				}
				else if (clz == Clob.class) {
					types[i] = StandardBasicTypes.CLOB;
				}
				else if (clz == Calendar.class) {
					types[i] = StandardBasicTypes.CALENDAR;
				}
				else if (clz == Locale.class) {
					types[i] = StandardBasicTypes.LOCALE;
				}
				else if (clz == Currency.class) {
					types[i] = StandardBasicTypes.CURRENCY;
				}
				else if (clz == TimeZone.class) {
					types[i] = StandardBasicTypes.TIMEZONE;
				}
				else if (clz == Class.class) {
					types[i] = StandardBasicTypes.CLASS;
				}
				else {
					types[i] = ObjectType.INSTANCE;
				}
			}
			catch (Exception ex) {
			}
		}

		return types;
	}

	public void setOrderById(boolean orderById) {
		this.orderById.set(orderById);
	}

	public PageCache getPageCache() {
		return pageCache;
	}

	public void setPageCache(PageCache pageCache) {
		this.pageCache = pageCache;
	}

}
