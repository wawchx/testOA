/*
 * @(#)StatelessHibernateDao.java Dec 4, 2012
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.LockMode;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
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
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import com.duker.mygift.vo.CriteriaProperty;
import com.duker.mygift.vo.Page;
import com.duker.mygift.vo.PagedList;
import com.duker.mygift.vo.SortOrder;

/**
 * <pre>
 * 无状态快速查询dao，忽略hibernate一二级缓存，不进行脏数据事务检查，不触发级联操作，忽略集合属性，跳过hibernate事件和拦截器
 * 
 * @author wangzh
 *
 * @version 0.9
 *
 * 修改版本: 0.9
 * 修改日期: Dec 4, 2012
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
@SuppressWarnings("unchecked")
public class StatelessHibernateDao {

	/**
	 * 当前会话
	 */
	private static final ThreadLocal<StatelessSession> SESSION = new ThreadLocal<StatelessSession>();

	/**
	 * Hibernate SessionFactory
	 */
	private SessionFactory sessionFactory;

	/**
	 * 分页总数缓存,减少count次数
	 */
	private PageCache pageCache;

	/**
	 * 默认的 StatelessHibernateDao 构造方法
	 */
	public StatelessHibernateDao() {
	}

	/**
	 * 使用 SessionFactory 来实例化一个 StatelessHibernateDao
	 * 
	 * @param sessionFactory
	 *            Hibernate会话工厂类
	 */
	public StatelessHibernateDao(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * 在当前线程上打开一个会话
	 */
	public StatelessSession openSession() {
		StatelessSession session = SESSION.get();
		if (session == null) {
			session = sessionFactory.openStatelessSession();
			SESSION.set(session);
		}

		return session;
	}

	/**
	 * 在关闭当前线程绑定的会话
	 */
	public void closeSession() {
		StatelessSession session = SESSION.get();
		SESSION.remove();
		if (session != null) {
			try {
				session.close();
			}
			catch (Exception e) {
			}
		}
	}

	/**
	 * 开启一个事务
	 * 
	 * @return 当前开启的事务
	 */
	public Transaction beginTransaction() {
		StatelessSession session = SESSION.get();
		if (session == null) {
			session = sessionFactory.openStatelessSession();
			SESSION.set(session);
		}

		Transaction transaction = session.beginTransaction();

		return transaction;
	}

	/**
	 * 开启一个事务，并设置超时时间
	 * 
	 * @param timeout
	 *            事务超时时间
	 * @return 当前开启的事务
	 */
	public Transaction beginTransaction(int timeout) {
		StatelessSession session = SESSION.get();
		if (session == null) {
			session = sessionFactory.openStatelessSession();
			SESSION.set(session);
		}

		Transaction transaction = session.beginTransaction();
		transaction.setTimeout(timeout);

		return transaction;
	}

	/**
	 * 提交事务
	 */
	public void commit() {
		StatelessSession session = SESSION.get();
		if (session != null) {
			session.getTransaction().commit();
		}
	}

	/**
	 * 回滚事务
	 */
	public void rollback() {
		StatelessSession session = SESSION.get();
		if (session != null) {
			session.getTransaction().rollback();
		}
	}

	/**
	 * 删除一个处于persistent状态的对象, 对应数据库的delete. 处于persistent状态的对象对应数据库的一条记录,
	 * 与某个session关联,并处于session的有效期内
	 * 
	 * @param persistentEntity
	 *            处于persistent状态的对象.
	 */
	public void delete(Object persistentEntity) {
		StatelessSession session = openSession();
		session.delete(persistentEntity);
	}

	/**
	 * 删除集合中的所有对象
	 * 
	 * @param entities
	 *            持久化对象集合
	 */
	public void deleteAll(Collection<?> entities) {
		StatelessSession session = openSession();
		for (Object entity : entities) {
			session.delete(entity);
		}
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
		StatelessSession session = openSession();
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
		StatelessSession session = openSession();
		SQLQuery queryObject = session.createSQLQuery(sql);
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(i, values[i]);
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
		StatelessSession session = openSession();
		Query namedQuery = session.getNamedQuery(queryName);
		String sql = namedQuery.getQueryString();
		String[] ss = sql.split("\\s*;\\s*");
		int ret = 0;
		for (String s : ss) {
			SQLQuery query = session.createSQLQuery(s.trim());
			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					query.setParameter(i, values[i]);
				}
			}
			ret += query.executeUpdate();
		}

		return ret;
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
		StatelessSession session = openSession();
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
			Criterion criterion, List<? extends Order> orders) {
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
			Criterion criterion, List<? extends Order> orders,
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
	 *            增加投影,可为空
	 * @param transformer
	 *            增加查询结果转换器,可为空
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByCriterion(Class<T> entityClass,
			Criterion criterion, List<? extends Order> orders,
			List<CriteriaProperty> properties, Projection projection,
			ResultTransformer transformer) {
		return this.<T> findByCriterion(entityClass.getName(), criterion,
				orders, properties, projection, transformer);
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
		StatelessSession session = openSession();
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
			List<? extends Order> orders) {
		StatelessSession session = openSession();
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
			List<? extends Order> orders, List<CriteriaProperty> properties) {
		StatelessSession session = openSession();
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
	 *            增加投影,可为空
	 * @param transformer
	 *            增加查询结果转换器,可为空
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByCriterion(String entityName, Criterion criterion,
			List<? extends Order> orders, List<CriteriaProperty> properties,
			Projection projection, ResultTransformer transformer) {
		StatelessSession session = openSession();
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
			List<? extends Order> orders) {
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
			List<? extends Order> orders, List<CriteriaProperty> properties) {
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
	 *            增加投影,可为空
	 * @param transformer
	 *            增加查询结果转换器,可为空
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findByExample(Object exampleEntity,
			List<? extends Order> orders, List<CriteriaProperty> properties,
			Projection projection, ResultTransformer transformer) {
		String entityName = exampleEntity.getClass().getName();
		Criterion criterion = Example.create(exampleEntity);

		return this.<T> findByCriterion(entityName, criterion, orders,
				properties, projection, transformer);
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
		StatelessSession session = openSession();
		return (T) session.get(entityClass, id);
	}

	/**
	 * <pre>
	 * 按主键来查找对象,同时加锁
	 * 如果指定id的对象不存在,则返回null
	 * 	示例：
	 * 		Cust cust = dao.findById(Cust.class,1L, LockMode.READ);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityClass
	 *            实体类, 如 Cust.class
	 * @param id
	 *            主键值
	 * @param lockMode
	 *            锁模式
	 * @return T 实体
	 */
	public <T> T findById(Class<T> entityClass, Serializable id,
			LockMode lockMode) {
		StatelessSession session = openSession();
		if (lockMode != null) {
			return (T) session.get(entityClass, id, lockMode);
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
		StatelessSession session = openSession();
		return (T) session.get(entityName, id);
	}

	/**
	 * <pre>
	 * 按主键来查询对象,同时加锁
	 * 如果指定id的对象不存在,则返回null
	 * 	示例：
	 * 		Cust cust = dao.findById("Cust", 1L, lockMode.READ);
	 * </pre>
	 * 
	 * @param <T>
	 *            实体类型
	 * @param entityName
	 *            实体名，如 "Cust"
	 * @param id
	 *            主键值
	 * @param lockMode
	 *            锁模式
	 * @return T 实体
	 */
	public <T> T findById(String entityName, Serializable id, LockMode lockMode) {
		StatelessSession session = openSession();
		if (lockMode != null) {
			return (T) session.get(entityName, id, lockMode);
		}
		return (T) session.get(entityName, id);
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
		StatelessSession session = openSession();
		Query namedQuery = session.getNamedQuery(queryName);

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
	 * @param values
	 *            参数列表
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findBySQL(Class<T> entityClass, String sql,
			Map<String, Type> scalars, Object... values) {
		return this.<T> findBySQL(entityClass, sql, scalars, null, values);
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
	 * @param sql
	 *            指定的sql语句
	 * @param scalars
	 *            scalar
	 * @param tableAlias
	 *            根对象别名
	 * @param values
	 *            参数列表
	 * @return List&lt;T&gt; 实体列表
	 */
	public <T> List<T> findBySQL(Class<T> entityClass, String sql,
			Map<String, Type> scalars, String tableAlias, Object... values) {
		StatelessSession session = openSession();
		SQLQuery queryObject = session.createSQLQuery(sql);

		if (entityClass != null) {
			if (tableAlias != null) {
				queryObject.addEntity(tableAlias, entityClass);
			}
			else {
				queryObject.addEntity(entityClass);
			}
		}

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(i, values[i]);
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
	 * 获得指定的sequence的nextval
	 * 
	 * @param sequenceName
	 *            序列号名
	 * @return Long 序列值
	 */
	public Long getSeqNextVal(String sequenceName) {
		StatelessSession session = openSession();
		StringBuilder sql = new StringBuilder();
		sql.append("select ")
				.append(sequenceName)
				.append(".nextval next_val from dual");
		SQLQuery queryObject = session
				.createSQLQuery(sql.toString())
				.addScalar("next_val", StandardBasicTypes.LONG);

		return (Long) queryObject.uniqueResult();
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
			List<? extends Order> orders, Object... values) {
		StatelessSession session = openSession();
		hql = getNamedHql(hql, values);
		String countHql = hql.toLowerCase();
		String prefix = null;
		int selectIndex = countHql.indexOf("select");
		int fromIndex = countHql.indexOf("from");
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
			List<? extends Order> orders) {
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
			List<? extends Order> orders, List<CriteriaProperty> properties) {
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
		StatelessSession session = openSession();
		Integer resultCount = pageCache.get(null, criterion);
		if (pageNo == 0 || resultCount == null) {
			// count
			Criteria criteria = session.createCriteria(entityName);
			if (criterion != null) {
				criteria.add(criterion);
			}
			criteria.setProjection(Projections.rowCount());
			resultCount = ((Number) criteria.uniqueResult()).intValue();
			pageCache.put(resultCount, null, criterion);
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
			List<? extends Order> orders) {
		StatelessSession session = openSession();
		Integer resultCount = pageCache.get(null, criterion);
		if (pageNo == 0 || resultCount == null) {
			// count
			Criteria criteria = session.createCriteria(entityName);
			if (criterion != null) {
				criteria.add(criterion);
			}
			criteria.setProjection(Projections.rowCount());
			resultCount = ((Number) criteria.uniqueResult()).intValue();
			pageCache.put(resultCount, null, criterion);
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
			List<? extends Order> orders, List<CriteriaProperty> properties) {
		StatelessSession session = openSession();
		Integer resultCount = pageCache.get(null, criterion);
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
			pageCache.put(resultCount, null, criterion);
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
			int pageSize, List<? extends Order> orders) {
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
			int pageSize, List<? extends Order> orders,
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
	 * @param values
	 *            参数值
	 * @return PagedList&lt;T&gt; 分页查询的结果列表
	 */
	public <T> PagedList<T> pageBySQL(Class<T> entityClass, String sql,
			int pageNo, int pageSize, List<? extends Order> orders,
			Map<String, Type> scalars, Object... values) {
		return this.<T> pageBySQL(entityClass, sql, pageNo, pageSize, orders,
				scalars, null, values);
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
	 * @param sql
	 *            sql语句
	 * @param tableAlias
	 *            根结点别名
	 * @param pageNo
	 *            当前页码,从0开始
	 * @param pageSize
	 *            页面大小
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param scalars
	 *            scalar
	 * @param values
	 *            参数值
	 * @return PagedList&lt;T&gt; 分页查询的结果列表
	 */
	public <T> PagedList<T> pageBySQL(Class<T> entityClass, String sql,
			String tableAlias, int pageNo, int pageSize,
			List<? extends Order> orders, Map<String, Type> scalars,
			Object... values) {
		StatelessSession session = openSession();
		Integer resultCount = pageCache.get(sql, values);
		if (pageNo == 0 || resultCount == null) {
			// count
			String countSql = sql.toLowerCase();
			int fromIndex = countSql.indexOf("from");
			countSql = "select count(1) as count " + sql.substring(fromIndex);
			SQLQuery queryObject = session.createSQLQuery(countSql);

			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					queryObject.setParameter(i, values[i]);
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
			ClassMetadata meta = sessionFactory.getClassMetadata(entityClass);
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

		if (scalars != null) {
			for (Entry<String, Type> entry : scalars.entrySet()) {
				queryObject.addScalar(entry.getKey(), entry.getValue());
			}
		}

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(i, values[i]);
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
			List<? extends Order> orders, Object... values) {
		if (startIndex < 0 || endIndex <= startIndex) {
			return null;
		}

		StatelessSession session = openSession();
		Query queryObject = null;
		hql = getNamedHql(hql, values);

		if (orders != null && !orders.isEmpty()) {
			StringBuilder pageHql = new StringBuilder(hql);

			pageHql.append(" order by ");
			for (Order order : orders) {
				pageHql.append(order.toString());
				pageHql.append(", ");
			}
			String hql1 = hql.toLowerCase();
			int fromIndex = hql1.indexOf("from");
			int selectIndex = hql1.indexOf("select");
			if (fromIndex != -1 && selectIndex != -1 && selectIndex < fromIndex) {
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
			List<? extends Order> orders) {
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
			List<? extends Order> orders, List<CriteriaProperty> properties) {
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

		StatelessSession session = openSession();
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
			List<? extends Order> orders) {
		if (startIndex < 0 || endIndex <= startIndex) {
			return null;
		}

		StatelessSession session = openSession();
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
			List<? extends Order> orders, List<CriteriaProperty> properties) {
		if (startIndex < 0 || endIndex <= startIndex) {
			return null;
		}

		StatelessSession session = openSession();
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
			int endIndex, List<? extends Order> orders) {
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
			int endIndex, List<? extends Order> orders,
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
		StatelessSession session = openSession();
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
	 * @param values
	 *            参数值
	 * @return List&lt;T&gt; 分页查询的结果列表
	 */
	public <T> List<T> findPartialBySQL(Class<T> entityClass, String sql,
			int startIndex, int endIndex, List<? extends Order> orders,
			Map<String, Type> scalars, Object... values) {
		return this.<T> findPartialBySQL(entityClass, sql, null, startIndex,
				endIndex, orders, scalars, values);
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
	 * @param sql
	 *            sql语句
	 * @param tableAlias
	 *            根结点别名
	 * @param startIndex
	 *            起始位置,从0开始
	 * @param endIndex
	 *            结束位置
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param scalars
	 *            scalar
	 * @param values
	 *            参数值
	 * @return List&lt;T&gt; 分页查询的结果列表
	 */
	public <T> List<T> findPartialBySQL(Class<T> entityClass, String sql,
			String tableAlias, int startIndex, int endIndex,
			List<? extends Order> orders, Map<String, Type> scalars,
			Object... values) {
		if (startIndex < 0 || endIndex <= startIndex) {
			return null;
		}
		StatelessSession session = openSession();
		SQLQuery queryObject = null;

		if (orders != null && !orders.isEmpty()) {
			StringBuilder pageSql = new StringBuilder(sql);

			pageSql.append(" order by ");
			for (Order order : orders) {
				pageSql.append(order.toString());
				pageSql.append(", ");
			}
			ClassMetadata meta = sessionFactory.getClassMetadata(entityClass);
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

		if (scalars != null) {
			for (Entry<String, Type> entry : scalars.entrySet()) {
				queryObject.addScalar(entry.getKey(), entry.getValue());
			}
		}

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(i, values[i]);
			}
		}

		queryObject.setFirstResult(startIndex);
		queryObject.setMaxResults(endIndex - startIndex);

		return queryObject.list();
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
	public <T> T findTop(String hql, int top, List<? extends Order> orders,
			Object... values) {
		if (top < 1) {
			return null;
		}
		StatelessSession session = openSession();
		Query queryObject = null;
		hql = getNamedHql(hql, values);

		if (orders != null && !orders.isEmpty()) {
			StringBuilder pageHql = new StringBuilder(hql);

			pageHql.append(" order by ");
			for (Order order : orders) {
				pageHql.append(order.toString());
				pageHql.append(", ");
			}
			String hql1 = hql.toLowerCase();
			int fromIndex = hql1.indexOf("from");
			int selectIndex = hql1.indexOf("select");
			if (fromIndex != -1 && selectIndex != -1 && selectIndex < fromIndex) {
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
			int top, List<? extends Order> orders) {
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
			int top, List<? extends Order> orders,
			List<CriteriaProperty> properties) {
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
		StatelessSession session = openSession();
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
			int top, List<? extends Order> orders) {
		if (top < 1) {
			return null;
		}
		StatelessSession session = openSession();
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
			int top, List<? extends Order> orders,
			List<CriteriaProperty> properties) {
		if (top < 1) {
			return null;
		}
		StatelessSession session = openSession();
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
			List<? extends Order> orders) {
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
			List<? extends Order> orders, List<CriteriaProperty> properties) {
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
		StatelessSession session = openSession();
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
	 * @param values
	 *            参数值
	 * @return 第top条结果
	 */
	public <T> T findTopBySQL(Class<T> entityClass, String sql, int top,
			List<? extends Order> orders, Map<String, Type> scalars,
			Object... values) {
		return this.<T> findTopBySQL(entityClass, sql, null, top, orders,
				scalars, values);
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
	 * @param sql
	 *            sql语句
	 * @param tableAlias
	 *            根结点别名
	 * @param top
	 *            第几个结果,从1开始
	 * @param orders
	 *            Order对象，作为排序依据，如果为null，则不进行排序
	 * @param scalars
	 *            scalar
	 * @param values
	 *            参数值
	 * @return 第top条结果
	 */
	public <T> T findTopBySQL(Class<T> entityClass, String sql,
			String tableAlias, int top, List<? extends Order> orders,
			Map<String, Type> scalars, Object... values) {
		if (top < 1) {
			return null;
		}
		StatelessSession session = openSession();
		SQLQuery queryObject = null;

		if (orders != null && !orders.isEmpty()) {
			StringBuilder pageSql = new StringBuilder(sql);

			pageSql.append(" order by ");
			for (Order order : orders) {
				pageSql.append(order.toString());
				pageSql.append(", ");
			}
			ClassMetadata meta = sessionFactory.getClassMetadata(entityClass);
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

		if (scalars != null) {
			for (Entry<String, Type> entry : scalars.entrySet()) {
				queryObject.addScalar(entry.getKey(), entry.getValue());
			}
		}

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(i, values[i]);
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
	 * 保存一个处于transient状态的对象, 对应数据库的insert. 处于transient状态的对象没有主键信息,与数据库记录没有任何关联
	 * 
	 * @param transientInstance
	 *            处于transient状态的对象
	 * @return Serializable 主键值
	 */
	public Serializable save(Object transientInstance) {
		StatelessSession session = openSession();
		return session.insert(transientInstance);
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
		StatelessSession session = openSession();
		return session.insert(entityName, entity);
	}

	/**
	 * 保存指定的实体对象列表
	 * 
	 * @param entities
	 *            实体对象列表
	 */
	public void saveAll(Collection<?> entities) {
		StatelessSession session = openSession();
		for (Object entity : entities) {
			session.insert(entity);
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
		StatelessSession session = openSession();
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
	 *            增加投影,可为空
	 * @param transformer
	 *            增加查询结果转换器,可为空
	 * @return T 实体
	 */
	public <T> T uniqueResultByCriterion(Class<T> entityClass,
			Criterion criterion, List<CriteriaProperty> properties,
			Projection projection, ResultTransformer transformer) {
		return this.<T> uniqueResultByCriterion(entityClass.getName(),
				criterion, properties, projection, transformer);
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
		StatelessSession session = openSession();
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
		StatelessSession session = openSession();
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
	 *            增加投影,可为空
	 * @param transformer
	 *            增加查询结果转换器,可为空
	 * @return T 实体
	 */
	public <T> T uniqueResultByCriterion(String entityName,
			Criterion criterion, List<CriteriaProperty> properties,
			Projection projection, ResultTransformer transformer) {
		StatelessSession session = openSession();
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
		StatelessSession session = openSession();
		Query namedQuery = session.getNamedQuery(queryName);

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
	 * @param values
	 *            sql参数列表
	 * @return T 实体
	 */
	public <T> T uniqueResultBySQL(Class<T> entityClass, String sql,
			Map<String, Type> scalars, Object... values) {
		return this.<T> uniqueResultBySQL(entityClass, sql, null, scalars,
				values);
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
	 * @param sql
	 *            sql语句
	 * @param tableAlias
	 *            根结点别名
	 * @param scalars
	 *            scalar
	 * @param values
	 *            sql参数列表
	 * @return T 实体
	 */
	public <T> T uniqueResultBySQL(Class<T> entityClass, String sql,
			String tableAlias, Map<String, Type> scalars, Object... values) {
		StatelessSession session = openSession();
		SQLQuery queryObject = session.createSQLQuery(sql);

		if (entityClass != null) {
			if (tableAlias != null) {
				queryObject.addEntity(tableAlias, entityClass);
			}
			else {
				queryObject.addEntity(entityClass);
			}
		}
		if (scalars != null) {
			for (Entry<String, Type> entry : scalars.entrySet()) {
				queryObject.addScalar(entry.getKey(), entry.getValue());
			}
		}

		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter(i, values[i]);
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
		StatelessSession session = openSession();
		session.update(persistentEntity);
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
		StatelessSession session = openSession();
		session.update(entityName, persistentEntity);
	}

	/**
	 * 更新指定的实体对象列表
	 * 
	 * @param entities
	 *            实体对象列表
	 */
	public void updateAll(Collection<?> entities) {
		StatelessSession session = openSession();
		for (Object entity : entities) {
			session.update(entity);
		}
	}

	public PageCache getPageCache() {
		return pageCache;
	}

	public void setPageCache(PageCache pageCache) {
		this.pageCache = pageCache;
	}

}
