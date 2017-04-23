/*
 * @(#)StatelessHibernateDao.java Dec 4, 2012
 * 
 * ��Ϣ��˹���ϵͳ
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
 * ��״̬���ٲ�ѯdao������hibernateһ�������棬�����������������飬�������������������Լ������ԣ�����hibernate�¼���������
 * 
 * @author wangzh
 *
 * @version 0.9
 *
 * �޸İ汾: 0.9
 * �޸�����: Dec 4, 2012
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
@SuppressWarnings("unchecked")
public class StatelessHibernateDao {

	/**
	 * ��ǰ�Ự
	 */
	private static final ThreadLocal<StatelessSession> SESSION = new ThreadLocal<StatelessSession>();

	/**
	 * Hibernate SessionFactory
	 */
	private SessionFactory sessionFactory;

	/**
	 * ��ҳ��������,����count����
	 */
	private PageCache pageCache;

	/**
	 * Ĭ�ϵ� StatelessHibernateDao ���췽��
	 */
	public StatelessHibernateDao() {
	}

	/**
	 * ʹ�� SessionFactory ��ʵ����һ�� StatelessHibernateDao
	 * 
	 * @param sessionFactory
	 *            Hibernate�Ự������
	 */
	public StatelessHibernateDao(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * �ڵ�ǰ�߳��ϴ�һ���Ự
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
	 * �ڹرյ�ǰ�̰߳󶨵ĻỰ
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
	 * ����һ������
	 * 
	 * @return ��ǰ����������
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
	 * ����һ�����񣬲����ó�ʱʱ��
	 * 
	 * @param timeout
	 *            ����ʱʱ��
	 * @return ��ǰ����������
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
	 * �ύ����
	 */
	public void commit() {
		StatelessSession session = SESSION.get();
		if (session != null) {
			session.getTransaction().commit();
		}
	}

	/**
	 * �ع�����
	 */
	public void rollback() {
		StatelessSession session = SESSION.get();
		if (session != null) {
			session.getTransaction().rollback();
		}
	}

	/**
	 * ɾ��һ������persistent״̬�Ķ���, ��Ӧ���ݿ��delete. ����persistent״̬�Ķ����Ӧ���ݿ��һ����¼,
	 * ��ĳ��session����,������session����Ч����
	 * 
	 * @param persistentEntity
	 *            ����persistent״̬�Ķ���.
	 */
	public void delete(Object persistentEntity) {
		StatelessSession session = openSession();
		session.delete(persistentEntity);
	}

	/**
	 * ɾ�������е����ж���
	 * 
	 * @param entities
	 *            �־û����󼯺�
	 */
	public void deleteAll(Collection<?> entities) {
		StatelessSession session = openSession();
		for (Object entity : entities) {
			session.delete(entity);
		}
	}

	/**
	 * <pre>
	 * ����ָ����hql���Ͳ����б�ִ��delete/update������hql��䣬������Ӱ�������
	 * <b>ע��:</b>  Hibernateû�н������delete/update�Ļ�������, �޷���֤�������ݵ�һ����Ч��
	 * 
	 * 	ʾ����
	 * 		String hql = "update Cust as cust set cust.logonPassword = ? where cust.id = ?";
	 * 		dao.execute(hql, "123456", 1L);
	 * </pre>
	 * 
	 * @param hql
	 *            ��̬hql.
	 * @param values
	 *            �����б�
	 * @return int ��Ӱ������
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
	 * ��ȡJPA-style hql
	 * 
	 * @param hql
	 *            position based hql
	 * @param values
	 *            ����
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
	 * ����ָ����sql����sql�����б�ִ��insert/delete/update������sql��䣬������Ӱ�������
	 * 	ʾ����
	 * 		String sql = "update cust set name = ?, password = ? where id = ?"
	 * 		dao.executeSQL(sql, "007", "123456", 1);
	 * </pre>
	 * 
	 * @param sql
	 *            sql���
	 * @param values
	 *            sql�����б�
	 * @return int ��Ӱ�������
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
	 * ����ָ����queryName�Ͳ����б�ִ���޷���ֵ�洢���̣�������Ӱ�������
	 * 	ʾ����
	 * 		dao.executeNamedQuery("KEY_BUILD_VIDEO_FULL_TEXT");
	 * </pre>
	 * 
	 * @param queryName
	 *            ��ѯ��
	 * @param values
	 *            ������ѯ�����б�
	 * @return int ��Ӱ�������
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
	 * ����ָ����<b>hql</b>�Ͳ����б����ز��ҵĽ��
	 * ʾ����
	 * 		String hql = "from Cust as cust where cust.name like ? ";
	 * 		List&lt;Cust&gt; custs = dao.find(hql, "%007%");
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param hql
	 *            hql���
	 * @param values
	 *            �����б�
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ����ʵ��������ж���
	 * ����һ���Խ����ر��ȫ����¼����˶��ڴ��������Ŀ����ʹ�ô˷���
	 * 	ʾ����
	 * 		List&lt;Cust&gt; custs = dao.findAll(Cust.class);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ���࣬�� Cust.class
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findAll(Class<T> entityClass) {
		return this.<T> find("from " + entityClass.getName());
	}

	/**
	 * <pre>
	 * ����ʵ��������ж���
	 * ����һ���Խ����ر��ȫ����¼����˶��ڴ��������Ŀ����ʹ�ô˷���
	 * 	ʾ����
	 * 		List&lt;Cust&gt; custs = dao.findAll("Cust");
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ��������"Cust"
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findAll(String entityName) {
		return this.<T> find("from " + entityName);
	}

	/**
	 * ����ָ����ʵ���ࡢCriterion��Order���в�ѯ
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            hibernate��ѯ��׼
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findByCriterion(Class<T> entityClass, Criterion criterion) {
		return this.<T> findByCriterion(entityClass.getName(), criterion);
	}

	/**
	 * ����ָ����ʵ���ࡢCriterion��Order���в�ѯ
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            hibernate��ѯ��׼
	 * @param orders
	 *            �������,��Ϊ��
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findByCriterion(Class<T> entityClass,
			Criterion criterion, List<? extends Order> orders) {
		return this.<T> findByCriterion(entityClass.getName(), criterion,
				orders);
	}

	/**
	 * <pre>
	 * ����ָ����ʵ���ࡢCriterion��Order���в�ѯ
	 * 	ʾ����
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
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            hibernate��ѯ��׼
	 * @param orders
	 *            �������,��Ϊ��
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findByCriterion(Class<T> entityClass,
			Criterion criterion, List<? extends Order> orders,
			List<CriteriaProperty> properties) {
		return this.<T> findByCriterion(entityClass.getName(), criterion,
				orders, properties);
	}

	/**
	 * ����ָ����ʵ���ࡢCriterion��Order���в�ѯ
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            hibernate��ѯ��׼
	 * @param orders
	 *            �������,��Ϊ��
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @param projection
	 *            ����ͶӰ,��Ϊ��
	 * @param transformer
	 *            ���Ӳ�ѯ���ת����,��Ϊ��
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findByCriterion(Class<T> entityClass,
			Criterion criterion, List<? extends Order> orders,
			List<CriteriaProperty> properties, Projection projection,
			ResultTransformer transformer) {
		return this.<T> findByCriterion(entityClass.getName(), criterion,
				orders, properties, projection, transformer);
	}

	/**
	 * ����ָ����ʵ������Criterion��Order���󣬽��в�ѯ
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ����ָ����ʵ������Criterion��Order���󣬽��в�ѯ
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ����ָ����ʵ������Criterion��Order���󣬽��в�ѯ
	 * 
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ����ָ����ʵ������Criterion��Order���󣬽��в�ѯ
	 * 
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @param projection
	 *            ����ͶӰ,��Ϊ��
	 * @param transformer
	 *            ���Ӳ�ѯ���ת����,��Ϊ��
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ʹ��Hibernate��org.hibernate.criterion.Example�����ж������ĸ��ϲ�ѯ
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param exampleEntity
	 *            ��ʵ�����
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findByExample(Object exampleEntity) {
		String entityName = exampleEntity.getClass().getName();
		Criterion criterion = Example.create(exampleEntity);

		return this.<T> findByCriterion(entityName, criterion);
	}

	/**
	 * ʹ��Hibernate��org.hibernate.criterion.Example�����ж������ĸ��ϲ�ѯ,�Խ����org.hibernate.
	 * criterion.Order��������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param exampleEntity
	 *            ��ʵ�����
	 * @param orders
	 *            �������,��Ϊ��
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findByExample(Object exampleEntity,
			List<? extends Order> orders) {
		String entityName = exampleEntity.getClass().getName();
		Criterion criterion = Example.create(exampleEntity);

		return this.<T> findByCriterion(entityName, criterion, orders);
	}

	/**
	 * <pre>
	 * ʹ��Hibernate��org.hibernate.criterion.Example�����ж������ĸ��ϲ�ѯ,���Խ����������,����ָ��ץȡ����
	 * 	ʾ����
	 * 		Cust cust = new Cust();
	 * 		cust.setName("007");
	 * 		cust.setAge(20);
	 *		List&lt;CriteriaProperty&gt; properties = new ArrayList&lt;CriteriaProperty&gt;();
	 *		properties.add(new CriteriaProperty("user", FetchMode.JOIN));
	 * 		List&lt;Cust&gt; custs = dao.findByExample(cust, properties);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param exampleEntity
	 *            ��ʵ�����
	 * @param orders
	 *            �������,��Ϊ��
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findByExample(Object exampleEntity,
			List<? extends Order> orders, List<CriteriaProperty> properties) {
		String entityName = exampleEntity.getClass().getName();
		Criterion criterion = Example.create(exampleEntity);

		return this.<T> findByCriterion(entityName, criterion, orders,
				properties);
	}

	/**
	 * ʹ��Hibernate��org.hibernate.criterion.Example�����ж������ĸ��ϲ�ѯ,���Խ����������,����ָ��ץȡ����
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param exampleEntity
	 *            ��ʵ�����
	 * @param orders
	 *            �������,��Ϊ��
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @param projection
	 *            ����ͶӰ,��Ϊ��
	 * @param transformer
	 *            ���Ӳ�ѯ���ת����,��Ϊ��
	 * @return List&lt;T&gt; ʵ���б�
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
	 * �����������Ҷ���
	 * ���ָ��id�Ķ��󲻴���,�򷵻�null
	 * 	ʾ����
	 * 		Cust cust = dao.findById(Cust.class, 1L);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����, �� Cust.class
	 * @param id
	 *            ����ֵ
	 * @return T ʵ��
	 */
	public <T> T findById(Class<T> entityClass, Serializable id) {
		StatelessSession session = openSession();
		return (T) session.get(entityClass, id);
	}

	/**
	 * <pre>
	 * �����������Ҷ���,ͬʱ����
	 * ���ָ��id�Ķ��󲻴���,�򷵻�null
	 * 	ʾ����
	 * 		Cust cust = dao.findById(Cust.class,1L, LockMode.READ);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����, �� Cust.class
	 * @param id
	 *            ����ֵ
	 * @param lockMode
	 *            ��ģʽ
	 * @return T ʵ��
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
	 * ����������ѯ����
	 * ���ָ��id�Ķ��󲻴���,�򷵻�null
	 * 	ʾ����
	 * 		Cust cust = dao.findById("Cust", 1L);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ�������� "Cust"
	 * @param id
	 *            ����ֵ
	 * @return T ʵ��
	 */
	public <T> T findById(String entityName, Serializable id) {
		StatelessSession session = openSession();
		return (T) session.get(entityName, id);
	}

	/**
	 * <pre>
	 * ����������ѯ����,ͬʱ����
	 * ���ָ��id�Ķ��󲻴���,�򷵻�null
	 * 	ʾ����
	 * 		Cust cust = dao.findById("Cust", 1L, lockMode.READ);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ�������� "Cust"
	 * @param id
	 *            ����ֵ
	 * @param lockMode
	 *            ��ģʽ
	 * @return T ʵ��
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
	 * ʹ��������ѯ
	 * 	ʾ��:
	 * 		List&lt;Cust&gt; custs = dao.findByNamedQuery("queryCustByNameAndAge", "007", 21);
	 * 	����,queryCustByNameAndAge �� hbm.xml �ļ�����,ʾ��:
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
	 *            ʵ������
	 * @param queryName
	 *            ��ѯ��
	 * @param values
	 *            ����ֵ
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ����ָ����ʵ���ࡢ��������(key)����ֵ(value)��Map, �Զ������Ϊ��������ѯ 
	 * 	ʾ����
	 * 		Map<String, Object> propertities = new LinkedHashMap<String, Object>(3);
	 * 		propertities.put("name", "007");
	 * 		propertities.put("sex", 1);
	 * 		propertities.put("age", 30);
	 * 		List&lt;Cust&gt; custs = dao.findByProperties(Cust.class, propertities);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param propertities
	 *            ��ֵ��
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findByProperties(Class<T> entityClass,
			Map<String, Object> propertities) {
		return this.<T> findByProperties(entityClass.getName(), propertities);
	}

	/**
	 * <pre>
	 * ����ָ����ʵ���ࡢ�������б�����ֵ�б�, �Զ������Ϊ��������ѯ
	 * ע�⣺���뱣֤�������б�����ֵ�б��е�������-ֵ�Ķ�Ӧ��ϵ
	 * 	ʾ����
	 * 		String[] propertyNames = {"name", "sex", "age"};
	 * 		Object[] propertyValues = {"007", 1, 30};
	 * 		List&lt;Cust&gt; custs = dao.findByProperties(Cust.class, propertyNames, propertyValues);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param propertyNames
	 *            ��������
	 * @param propertyValues
	 *            ֵ����
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findByProperties(Class<T> entityClass,
			String[] propertyNames, Object[] propertyValues) {
		return this.<T> findByProperties(entityClass.getName(), propertyNames,
				propertyValues);
	}

	/**
	 * <pre>
	 * ����ָ����ʵ��������������(key)����ֵ(value)��Map, �Զ������Ϊ��������ѯ 
	 * 	ʾ����
	 * 		Map<String, Object> propertities = new HashMap<String, Object>(3);
	 * 		propertities.put("name", "007");
	 * 		propertities.put("sex", 1);
	 * 		propertities.put("age", 1);
	 * 		List&lt;Cust&gt; custs = dao.findByProperties("Cust", propertities);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ����
	 * @param propertities
	 *            ��ֵ��
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ����ָ����ʵ�������������б�����ֵ�б�, �Զ������Ϊ��������ѯ
	 * ע�⣺���뱣֤�������б�����ֵ�б��е�������-ֵ�Ķ�Ӧ��ϵ
	 * 	ʾ����
	 * 		String[] propertyNames = {"name", "sex", "age"};
	 * 		Object[] propertyValues = {"007", 1, 30};
	 * 		List&lt;Cust&gt; custs = dao.findByProperties("Cust", propertyNames, propertyValues);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ����
	 * @param propertyNames
	 *            ��������
	 * @param propertyValues
	 *            ֵ����
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ����ָ����ʵ���ࡢ������������ֵ, �Ե�������Ϊ��������ѯ
	 * 	ʾ����
	 * 		List&lt;Cust&gt; custs = dao.findByProperty(Cust.class, "name", "007");
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param propertyName
	 *            ������
	 * @param propertyValue
	 *            ����ֵ
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findByProperty(Class<T> entityClass,
			String propertyName, Object propertyValue) {
		return this.<T> findByProperty(entityClass.getName(), propertyName,
				propertyValue);
	}

	/**
	 * <pre>
	 * ����ָ����ʵ������������������ֵ, �Ե�������Ϊ��������ѯ
	 * 	ʾ����
	 * 		List&lt;Cust&gt; custs = dao.findByProperty("Cust", "name", "007");
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ����, �� "Cust"
	 * @param propertyName
	 *            ������
	 * @param propertyValue
	 *            ����ֵ
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ����ָ����ʵ���ࡢsql����sql�����б���ѯ�����б�
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����, ����������Ӧʵ�壬��null����
	 * @param sql
	 *            ָ����sql���
	 * @param scalars
	 *            scalar
	 * @param values
	 *            �����б�
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findBySQL(Class<T> entityClass, String sql,
			Map<String, Type> scalars, Object... values) {
		return this.<T> findBySQL(entityClass, sql, scalars, null, values);
	}

	/**
	 * <pre>
	 * ����ָ����ʵ���ࡢsql����sql�����б���ѯ�����б�
	 * 	ʾ��:
	 * 		String sql = "select * from cust where id = ? and sex = ? ";
	 * 		List&lt;Cust&gt; custs = dao.findBySQL(Cust.class, sql, null, null, 100, 1);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����, ����������Ӧʵ�壬��null����
	 * @param sql
	 *            ָ����sql���
	 * @param scalars
	 *            scalar
	 * @param tableAlias
	 *            ���������
	 * @param values
	 *            �����б�
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ���ָ����sequence��nextval
	 * 
	 * @param sequenceName
	 *            ���к���
	 * @return Long ����ֵ
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
	 * ����ָ����hql��������pageNo��pageSize �����в�ѯ��ҳ
	 * ʾ��:
	 *		String hql = "from Cust as cust where cust.age > ?";
	 * 		PagedList&lt;Cust&gt; custs = dao.page(hql, 0, 10, null, 0);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param hql
	 *            hql���
	 * @param pageNo
	 *            ��ǰҳ��,��0��ʼ
	 * @param pageSize
	 *            ҳ���С
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param values
	 *            ����ֵ
	 * @return PagedList&lt;T&gt; ��ҳ��ѯ�Ľ���б�
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
	 * ����ָ����ʵ���ࡢCriterion��Order��pageNo��pageSize�����и��ϲ�ѯ����ҳ
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param pageNo
	 *            ��ǰҳ��,��0��ʼ
	 * @param pageSize
	 *            ҳ���С
	 * @return PagedList&lt;T&gt; ��ҳ��ѯ�Ľ���б�
	 */
	public <T> PagedList<T> pageByCriterion(Class<T> entityClass,
			Criterion criterion, int pageNo, int pageSize) {
		return this.<T> pageByCriterion(entityClass.getName(), criterion,
				pageNo, pageSize);
	}

	/**
	 * ����ָ����ʵ���ࡢCriterion��Order��pageNo��pageSize �����и��ϲ�ѯ����ҳ
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param pageNo
	 *            ��ǰҳ��,��0��ʼ
	 * @param pageSize
	 *            ҳ���С
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @return PagedList&lt;T&gt; ��ҳ��ѯ�Ľ���б�
	 */
	public <T> PagedList<T> pageByCriterion(Class<T> entityClass,
			Criterion criterion, int pageNo, int pageSize,
			List<? extends Order> orders) {
		return this.<T> pageByCriterion(entityClass.getName(), criterion,
				pageNo, pageSize, orders);
	}

	/**
	 * <pre>
	 * ����ָ����ʵ���ࡢCriterion��Order��pageNo��pageSize �����и��ϲ�ѯ����ҳ
	 * ʾ��1:
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
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param pageNo
	 *            ��ǰҳ��,��0��ʼ
	 * @param pageSize
	 *            ҳ���С
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @return PagedList&lt;T&gt; ��ҳ��ѯ�Ľ���б�
	 */
	public <T> PagedList<T> pageByCriterion(Class<T> entityClass,
			Criterion criterion, int pageNo, int pageSize,
			List<? extends Order> orders, List<CriteriaProperty> properties) {
		return this.<T> pageByCriterion(entityClass.getName(), criterion,
				pageNo, pageSize, orders, properties);
	}

	/**
	 * ����ָ����ʵ������criterion��pageNo��pageSize�����з�ҳ��ѯ
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param pageNo
	 *            ��ǰҳ��,��0��ʼ
	 * @param pageSize
	 *            ҳ���С
	 * @return PagedList&lt;T&gt; ��ҳ��ѯ�Ľ���б�
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
	 * ����ָ����ʵ������criterion��pageNo��pageSize��orders�����з�ҳ��ѯ
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param pageNo
	 *            ��ǰҳ��,��0��ʼ
	 * @param pageSize
	 *            ҳ���С
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @return PagedList&lt;T&gt; ��ҳ��ѯ�Ľ���б�
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
	 * ����ָ����ʵ������criterion��pageNo��pageSize��orders�����з�ҳ��ѯ
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param pageNo
	 *            ��ǰҳ��,��0��ʼ
	 * @param pageSize
	 *            ҳ���С
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @return PagedList&lt;T&gt; ��ҳ��ѯ�Ľ���б�
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
	 * ʹ��Hibernate��org.hibernate.criterion.Example�����ж������ĸ��ϲ�ѯ��ҳ
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param exampleEntity
	 *            ʵ�����
	 * @param pageNo
	 *            ��ǰҳ��,��0��ʼ
	 * @param pageSize
	 *            ҳ���С
	 * @return PagedList&lt;T&gt; ��ҳ��ѯ�Ľ���б�
	 */
	public <T> PagedList<T> pageByExample(T exampleEntity, int pageNo,
			int pageSize) {
		Example example = Example.create(exampleEntity);
		Class<T> clz = (Class<T>) exampleEntity.getClass();

		return this.<T> pageByCriterion(clz, example, pageNo, pageSize);
	}

	/**
	 * ʹ��Hibernate��org.hibernate.criterion.Example�����ж������ĸ��ϲ�ѯ��ҳ,���Խ����������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param exampleEntity
	 *            ʵ�����
	 * @param pageNo
	 *            ��ǰҳ��,��0��ʼ
	 * @param pageSize
	 *            ҳ���С
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @return PagedList&lt;T&gt; ��ҳ��ѯ�Ľ���б�
	 */
	public <T> PagedList<T> pageByExample(T exampleEntity, int pageNo,
			int pageSize, List<? extends Order> orders) {
		Example example = Example.create(exampleEntity);
		Class<T> clz = (Class<T>) exampleEntity.getClass();

		return this.<T> pageByCriterion(clz, example, pageNo, pageSize, orders);
	}

	/**
	 * <pre>
	 * ʹ��Hibernate��org.hibernate.criterion.Example�����ж������ĸ��ϲ�ѯ��ҳ
	 * 	ʾ����
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
	 *            ʵ������
	 * @param exampleEntity
	 *            ʵ�����
	 * @param pageNo
	 *            ��ǰҳ��,��0��ʼ
	 * @param pageSize
	 *            ҳ���С
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @return PagedList&lt;T&gt; ��ҳ��ѯ�Ľ���б�
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
	 * ����ָ����entityClass��sql��Page������ֵ �����в�ѯ��ҳ
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ������
	 * @param sql
	 *            sql���
	 * @param pageNo
	 *            ��ǰҳ��,��0��ʼ
	 * @param pageSize
	 *            ҳ���С
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param scalars
	 *            scalar
	 * @param values
	 *            ����ֵ
	 * @return PagedList&lt;T&gt; ��ҳ��ѯ�Ľ���б�
	 */
	public <T> PagedList<T> pageBySQL(Class<T> entityClass, String sql,
			int pageNo, int pageSize, List<? extends Order> orders,
			Map<String, Type> scalars, Object... values) {
		return this.<T> pageBySQL(entityClass, sql, pageNo, pageSize, orders,
				scalars, null, values);
	}

	/**
	 * <pre>
	 * ����ָ����entityClass��sql��Page������ֵ �����в�ѯ��ҳ
	 * ʾ��1:
	 * 		String sql = "select * from cust where age > ?";
	 * 		PagedList&lt;Cust&gt; custs = dao.pageBySQL(Cust.class, sql, null, 2, 10, null, 1);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ������
	 * @param sql
	 *            sql���
	 * @param tableAlias
	 *            ��������
	 * @param pageNo
	 *            ��ǰҳ��,��0��ʼ
	 * @param pageSize
	 *            ҳ���С
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param scalars
	 *            scalar
	 * @param values
	 *            ����ֵ
	 * @return PagedList&lt;T&gt; ��ҳ��ѯ�Ľ���б�
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
	 * ����ָ����hql��������startIndex��endIndex ��ȡ��������
	 * ʾ��:
	 *		String hql = "from Cust as cust where cust.age > ?";
	 * 		List&lt;Cust&gt; custs = dao.findPartial(hql, 0, 10, null, 0);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param hql
	 *            hql���
	 * @param startIndex
	 *            ��ʼλ��,��0��ʼ
	 * @param endIndex
	 *            ����λ��
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param values
	 *            ����ֵ
	 * @return List&lt;T&gt; ��ҳ��ѯ�Ľ���б�
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
	 * ����ָ����ʵ���ࡢCriterion��Order��startIndex��endIndex ȡ��������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param startIndex
	 *            ��ʼλ��,��0��ʼ
	 * @param endIndex
	 *            ����λ��
	 * @return List&lt;T&gt; ��ҳ��ѯ�Ľ���б�
	 */
	public <T> List<T> findPartialByCriterion(Class<T> entityClass,
			Criterion criterion, int startIndex, int endIndex) {
		return this.<T> findPartialByCriterion(entityClass.getName(),
				criterion, startIndex, endIndex);
	}

	/**
	 * ����ָ����ʵ���ࡢCriterion��Order��startIndex��endIndex ȡ��������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param startIndex
	 *            ��ʼλ��,��0��ʼ
	 * @param endIndex
	 *            ����λ��
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @return List&lt;T&gt; ��ҳ��ѯ�Ľ���б�
	 */
	public <T> List<T> findPartialByCriterion(Class<T> entityClass,
			Criterion criterion, int startIndex, int endIndex,
			List<? extends Order> orders) {
		return this.<T> findPartialByCriterion(entityClass.getName(),
				criterion, startIndex, endIndex, orders);
	}

	/**
	 * <pre>
	 * ����ָ����ʵ���ࡢCriterion��Order��startIndex��endIndex ȡ��������
	 * ʾ��1:
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
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param startIndex
	 *            ��ʼλ��,��0��ʼ
	 * @param endIndex
	 *            ����λ��
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @return List&lt;T&gt; ��ҳ��ѯ�Ľ���б�
	 */
	public <T> List<T> findPartialByCriterion(Class<T> entityClass,
			Criterion criterion, int startIndex, int endIndex,
			List<? extends Order> orders, List<CriteriaProperty> properties) {
		return this.<T> findPartialByCriterion(entityClass.getName(),
				criterion, startIndex, endIndex, orders, properties);
	}

	/**
	 * ����ָ����ʵ������criterion��startIndex��endIndex ȡ��������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param startIndex
	 *            ��ʼλ��,��0��ʼ
	 * @param endIndex
	 *            ����λ��
	 * @return List&lt;T&gt; ��ҳ��ѯ�Ľ���б�
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
	 * ����ָ����ʵ������criterion��startIndex��endIndex��orders ȡ��������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param startIndex
	 *            ��ʼλ��,��0��ʼ
	 * @param endIndex
	 *            ����λ��
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @return List&lt;T&gt; ��ҳ��ѯ�Ľ���б�
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
	 * ����ָ����ʵ������criterion��startIndex��endIndex��orders ȡ��������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param startIndex
	 *            ��ʼλ��,��0��ʼ
	 * @param endIndex
	 *            ����λ��
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @return List&lt;T&gt; ��ҳ��ѯ�Ľ���б�
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
	 * ʹ��Hibernate��org.hibernate.criterion.Exampleȡ��������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param exampleEntity
	 *            ʵ�����
	 * @param startIndex
	 *            ��ʼλ��,��0��ʼ
	 * @param endIndex
	 *            ����λ��
	 * @return List&lt;T&gt; ��ҳ��ѯ�Ľ���б�
	 */
	public <T> List<T> findPartialByExample(T exampleEntity, int startIndex,
			int endIndex) {
		Example example = Example.create(exampleEntity);
		Class<T> clz = (Class<T>) exampleEntity.getClass();

		return this.<T> findPartialByCriterion(clz, example, startIndex,
				endIndex);
	}

	/**
	 * ʹ��Hibernate��org.hibernate.criterion.Exampleȡ��������,���Խ����������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param exampleEntity
	 *            ʵ�����
	 * @param startIndex
	 *            ��ʼλ��,��0��ʼ
	 * @param endIndex
	 *            ����λ��
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @return List&lt;T&gt; ��ҳ��ѯ�Ľ���б�
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
	 * ʹ��Hibernate��org.hibernate.criterion.Exampleȡ��������
	 * 	ʾ����
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
	 *            ʵ������
	 * @param exampleEntity
	 *            ʵ�����
	 * @param startIndex
	 *            ��ʼλ��,��0��ʼ
	 * @param endIndex
	 *            ����λ��
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @return List&lt;T&gt; ��ҳ��ѯ�Ľ���б�
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
	 * ʹ��������ѯȡ��������
	 * 	ʾ��:
	 * 		List&lt;Cust&gt; custs = dao.findPartialByNamedQuery("queryCustByNameAndAge", 0, 10 "007", 21);
	 * 	����,queryCustByNameAndAge �� hbm.xml �ļ�����,ʾ��:
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
	 *            ʵ������
	 * @param queryName
	 *            ��ѯ��
	 * @param startIndex
	 *            ��ʼλ��,��0��ʼ
	 * @param endIndex
	 *            ����λ��
	 * @param values
	 *            ����ֵ
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ����ָ����entityClass��sql��startIndex��endIndex������ֵ ȡ��������
	 * ʾ��1:
	 * 		String sql = "select * from cust where age > ?";
	 * 		List&lt;Cust&gt; custs = dao.findPartialBySQL(Cust.class, sql, 0, 10, null, 1);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ������
	 * @param sql
	 *            sql���
	 * @param startIndex
	 *            ��ʼλ��,��0��ʼ
	 * @param endIndex
	 *            ����λ��
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param scalars
	 *            scalar
	 * @param values
	 *            ����ֵ
	 * @return List&lt;T&gt; ��ҳ��ѯ�Ľ���б�
	 */
	public <T> List<T> findPartialBySQL(Class<T> entityClass, String sql,
			int startIndex, int endIndex, List<? extends Order> orders,
			Map<String, Type> scalars, Object... values) {
		return this.<T> findPartialBySQL(entityClass, sql, null, startIndex,
				endIndex, orders, scalars, values);
	}

	/**
	 * <pre>
	 * ����ָ����entityClass��sql��startIndex��endIndex������ֵ ȡ��������
	 * ʾ��1:
	 * 		String sql = "select * from cust where age > ?";
	 * 		List&lt;Cust&gt; custs = dao.findPartialBySQL(Cust.class, sql, null, 0, 10, null, 1);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ������
	 * @param sql
	 *            sql���
	 * @param tableAlias
	 *            ��������
	 * @param startIndex
	 *            ��ʼλ��,��0��ʼ
	 * @param endIndex
	 *            ����λ��
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param scalars
	 *            scalar
	 * @param values
	 *            ����ֵ
	 * @return List&lt;T&gt; ��ҳ��ѯ�Ľ���б�
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
	 * ����ָ����hql��������ȡ��top������
	 * ʾ��:
	 *		String hql = "from Cust as cust where cust.age > ?";
	 * 		Cust cust = dao.findTop(hql, 1, null, 0);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param hql
	 *            hql���
	 * @param top
	 *            �ڼ������,��1��ʼ
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param values
	 *            ����ֵ
	 * @return ��top�����
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
	 * ����ָ����ʵ���ࡢCriterion��Orderȡ��top������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param top
	 *            �ڼ������,��1��ʼ
	 * @return ��top�����
	 */
	public <T> T findTopByCriterion(Class<T> entityClass, Criterion criterion,
			int top) {
		return this.<T> findTopByCriterion(entityClass.getName(), criterion,
				top);
	}

	/**
	 * ����ָ����ʵ���ࡢCriterion��Orderȡ��top������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param top
	 *            �ڼ������,��1��ʼ
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @return ��top�����
	 */
	public <T> T findTopByCriterion(Class<T> entityClass, Criterion criterion,
			int top, List<? extends Order> orders) {
		return this.<T> findTopByCriterion(entityClass.getName(), criterion,
				top, orders);
	}

	/**
	 * <pre>
	 * ����ָ����ʵ���ࡢCriterion��Orderȡ��top������
	 * ʾ��1:
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
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param top
	 *            �ڼ������,��1��ʼ
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @return ��top�����
	 */
	public <T> T findTopByCriterion(Class<T> entityClass, Criterion criterion,
			int top, List<? extends Order> orders,
			List<CriteriaProperty> properties) {
		return this.<T> findTopByCriterion(entityClass.getName(), criterion,
				top, orders, properties);
	}

	/**
	 * ����ָ����ʵ������criterionȡ��top������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param top
	 *            �ڼ������,��1��ʼ
	 * @return ��top�����
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
	 * ����ָ����ʵ������criterion��ordersȡ��top������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param top
	 *            �ڼ������,��1��ʼ
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @return ��top�����
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
	 * ����ָ����ʵ������criterion��ordersȡ��top������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param top
	 *            �ڼ������,��1��ʼ
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @return ��top�����
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
	 * ʹ��Hibernate��org.hibernate.criterion.Exampleȡ��top������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param exampleEntity
	 *            ʵ�����
	 * @param top
	 *            �ڼ������,��1��ʼ
	 * @return ��top�����
	 */
	public <T> T findTopByExample(T exampleEntity, int top) {
		Example example = Example.create(exampleEntity);
		Class<T> clz = (Class<T>) exampleEntity.getClass();

		return this.<T> findTopByCriterion(clz, example, top);
	}

	/**
	 * ʹ��Hibernate��org.hibernate.criterion.Exampleȡ��top������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param exampleEntity
	 *            ʵ�����
	 * @param top
	 *            �ڼ������,��1��ʼ
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @return ��top�����
	 */
	public <T> T findTopByExample(T exampleEntity, int top,
			List<? extends Order> orders) {
		Example example = Example.create(exampleEntity);
		Class<T> clz = (Class<T>) exampleEntity.getClass();

		return this.<T> findTopByCriterion(clz, example, top, orders);
	}

	/**
	 * <pre>
	 * ʹ��Hibernate��org.hibernate.criterion.Exampleȡ��top������
	 * 	ʾ����
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
	 *            ʵ������
	 * @param exampleEntity
	 *            ʵ�����
	 * @param top
	 *            �ڼ������,��1��ʼ
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @return ��top�����
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
	 * ʹ��������ѯȡ��top������
	 * 	ʾ��:
	 * 		Cust cust = dao.findTopByNamedQuery("queryCustByNameAndAge", 1, "007", 21);
	 * 	����,queryCustByNameAndAge �� hbm.xml �ļ�����,ʾ��:
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
	 *            ʵ������
	 * @param queryName
	 *            ��ѯ��
	 * @param top
	 *            �ڼ������,��1��ʼ
	 * @param values
	 *            ����ֵ
	 * @return &lt;T&gt; ʵ���б�
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
	 * ����ָ����entityClass��sql����ֵȡ��top������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ������
	 * @param sql
	 *            sql���
	 * @param top
	 *            �ڼ������,��1��ʼ
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param scalars
	 *            scalar
	 * @param values
	 *            ����ֵ
	 * @return ��top�����
	 */
	public <T> T findTopBySQL(Class<T> entityClass, String sql, int top,
			List<? extends Order> orders, Map<String, Type> scalars,
			Object... values) {
		return this.<T> findTopBySQL(entityClass, sql, null, top, orders,
				scalars, values);
	}

	/**
	 * <pre>
	 * ����ָ����entityClass��sql����ֵȡ��top������
	 * ʾ��1:
	 * 		String sql = "select * from cust where age > ?";
	 * 		Cust cust = dao.findTopBySQL(Cust.class, sql, null, 1, null, null, 1);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ������
	 * @param sql
	 *            sql���
	 * @param tableAlias
	 *            ��������
	 * @param top
	 *            �ڼ������,��1��ʼ
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param scalars
	 *            scalar
	 * @param values
	 *            ����ֵ
	 * @return ��top�����
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
	 * ����һ������transient״̬�Ķ���, ��Ӧ���ݿ��insert. ����transient״̬�Ķ���û��������Ϣ,�����ݿ��¼û���κι���
	 * 
	 * @param transientInstance
	 *            ����transient״̬�Ķ���
	 * @return Serializable ����ֵ
	 */
	public Serializable save(Object transientInstance) {
		StatelessSession session = openSession();
		return session.insert(transientInstance);
	}

	/**
	 * ����һ������transient״̬�Ķ���, ��Ӧ���ݿ��insert. ����transient״̬�Ķ���û��������Ϣ,�����ݿ��¼û���κι���
	 * 
	 * @param entityName
	 *            ʵ����
	 * @param entity
	 *            ʵ�����
	 * @return Serializable ����ֵ
	 */
	public Serializable save(String entityName, Object entity) {
		StatelessSession session = openSession();
		return session.insert(entityName, entity);
	}

	/**
	 * ����ָ����ʵ������б�
	 * 
	 * @param entities
	 *            ʵ������б�
	 */
	public void saveAll(Collection<?> entities) {
		StatelessSession session = openSession();
		for (Object entity : entities) {
			session.insert(entity);
		}
	}

	/**
	 * <pre>
	 * ����ָ����hql�Ͳ����б���ѯΨһ����
	 * �����ѯ�Ľ������1��,���׳� NonUniqueResultException
	 * 	ʾ����
	 * 		String hql = "from Cust as cust where cust.id = ? ";
	 * 		Cust cust = dao.uniqueResult(hql, 1L);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param hql
	 *            ָ����hql���
	 * @param values
	 *            �����б�
	 * @return T Ψһ����
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
	 * ����ָ����ʵ���ࡢCriterionȡ��Ψһʵ��
	 * 	ʾ����
	 *		Cust cust = dao.uniqueResultByCriterion(Cust.class, Restrictions.eq("id", 1L));
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            hibernate��ѯ��׼
	 * @return T ʵ��
	 */
	public <T> T uniqueResultByCriterion(Class<T> entityClass,
			Criterion criterion) {
		return this.<T> uniqueResultByCriterion(entityClass.getName(),
				criterion);
	}

	/**
	 * <pre>
	 * ����ָ����ʵ���ࡢCriterionȡ��Ψһʵ��
	 * 	ʾ����
	 *		List&lt;CriteriaProperty&gt; properties = new ArrayList&lt;CriteriaProperty&gt;();
	 *		properties.add(new CriteriaProperty("user", FetchMode.JOIN));
	 *		Cust cust = dao.uniqueResultByCriterion(Cust.class, Restrictions.eq("id", 1L, properties));
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            hibernate��ѯ��׼
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @return T ʵ��
	 */
	public <T> T uniqueResultByCriterion(Class<T> entityClass,
			Criterion criterion, List<CriteriaProperty> properties) {
		return this.<T> uniqueResultByCriterion(entityClass.getName(),
				criterion, properties);
	}

	/**
	 * ����ָ����ʵ���ࡢCriterionȡ��Ψһʵ��
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            hibernate��ѯ��׼
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @param projection
	 *            ����ͶӰ,��Ϊ��
	 * @param transformer
	 *            ���Ӳ�ѯ���ת����,��Ϊ��
	 * @return T ʵ��
	 */
	public <T> T uniqueResultByCriterion(Class<T> entityClass,
			Criterion criterion, List<CriteriaProperty> properties,
			Projection projection, ResultTransformer transformer) {
		return this.<T> uniqueResultByCriterion(entityClass.getName(),
				criterion, properties, projection, transformer);
	}

	/**
	 * ����ָ����ʵ������Criterion����Ψһʵ��
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @return T ʵ��
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
	 * ����ָ����ʵ������Criterion����Ψһʵ��
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @return T ʵ��
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
	 * ����ָ����ʵ������Criterion����Ψһʵ��
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @param projection
	 *            ����ͶӰ,��Ϊ��
	 * @param transformer
	 *            ���Ӳ�ѯ���ת����,��Ϊ��
	 * @return T ʵ��
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
	 * ����ָ�������ò�ѯ���ơ�bean����ѯΨһ����
	 * �����ѯ�Ľ������1��,���׳� NonUniqueResultException
	 * 	ʾ��:
	 * 		Cust cust = dao.uniqueResultByNamedQuery("queryCustByNameAndAge", "007", 21);
	 * 	����,queryCustByNameAndAge �� hbm.xml �ļ�����,ʾ��:
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
	 *            ʵ������
	 * @param queryName
	 *            ��ѯ��
	 * @param values
	 *            ������ֵ
	 * @return T Ψһ����
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
	 * ����ָ����ʵ���ࡢ��������(key)����ֵ(value)��Map, �Զ������Ϊ��������ѯΨһ����
	 * �����ѯ�Ľ������1��,���׳� NonUniqueResultException
	 * 	ʾ����
	 * 		Map<String, Object> propertities = new HashMap<String, Object>(3);
	 * 		propertities.put("name", "007");
	 * 		propertities.put("sex", 1);
	 * 		propertities.put("age", 30);
	 * 		Cust cust = dao.uniqueResult(Cust.class, propertities);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param propertities
	 *            ��������ֵ��
	 * @return T Ψһ����
	 */
	public <T> T uniqueResultByProperties(Class<T> entityClass,
			Map<String, Object> propertities) {
		return this.<T> uniqueResultByProperties(entityClass.getName(),
				propertities);
	}

	/**
	 * <pre>
	 * ����ָ����ʵ���ࡢ�������б�����ֵ�б�, �Զ������Ϊ��������ѯΨһ����
	 * �����ѯ�Ľ������1��,���׳� NonUniqueResultException
	 * ע�⣺���뱣֤�������б�����ֵ�б��е�������-ֵ�Ķ�Ӧ��ϵ
	 * 	ʾ����
	 * 		String propertyNames = {"name", "sex", "age"};
	 * 		Object propertyValues = {"007", 1, 30};
	 * 		Cust cust = dao.uniqueResult(Cust.class, propertyNames, propertyValues);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param propertyNames
	 *            ������ֵ����
	 * @param propertyValues
	 *            ��ֵ����
	 * @return T Ψһ����
	 */
	public <T> T uniqueResultByProperties(Class<T> entityClass,
			String[] propertyNames, Object[] propertyValues) {
		return this.<T> uniqueResultByProperties(entityClass.getName(),
				propertyNames, propertyValues);
	}

	/**
	 * <pre>
	 * ����ָ����ʵ��������������(key)����ֵ(value)��Map, �Զ������Ϊ��������ѯΨһ����
	 * �����ѯ�Ľ������1��,���׳� NonUniqueResultException
	 * 	ʾ����
	 * 		Map<String, Object> propertities = new LinkedHashMap<String, Object>(3);
	 * 		propertities.put("name", "007");
	 * 		propertities.put("sex", 1);
	 * 		propertities.put("age", 30);
	 * 		Cust cust = dao.uniqueResult("Cust", propertities);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ����
	 * @param propertities
	 *            ��������ֵ��
	 * @return T Ψһ����
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
	 * ����ָ����ʵ�������������б�����ֵ�б�, �Զ������Ϊ��������ѯΨһ����
	 * �����ѯ�Ľ������1��,���׳� NonUniqueResultException
	 * <b>ע�⣺</b>���뱣֤�������б�����ֵ�б��е�������-ֵ�Ķ�Ӧ��ϵ
	 * 	ʾ����
	 * 		String propertyNames = {"name", "sex", "age"};
	 * 		Object propertyValues = {"007", 1, 30};
	 * 		Cust cust = dao.uniqueResult("Cust", propertyNames, propertyValues);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ����
	 * @param propertyNames
	 *            ��������ֵ����
	 * @param propertyValues
	 *            ��ֵ����
	 * @return T Ψһ����
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
	 * ����ָ����ʵ���ࡢ������������ֵ, �Ե�������Ϊ��������ѯΨһ����
	 * �����ѯ�Ľ������1��,���׳� NonUniqueResultException
	 * 	ʾ����
	 * 		Cust cust = dao.uniqueResult(Cust.class, "name", "007");
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����
	 * @param propertyName
	 *            ������
	 * @param propertyValue
	 *            ����ֵ
	 * @return T Ψһ����
	 */
	public <T> T uniqueResultByProperty(Class<T> entityClass,
			String propertyName, Object propertyValue) {
		return this.<T> uniqueResultByProperty(entityClass.getName(),
				propertyName, propertyValue);
	}

	/**
	 * <pre>
	 * ����ָ����ʵ������������������ֵ, �Ե�������Ϊ��������ѯΨһ����
	 * �����ѯ�Ľ������1��,���׳� NonUniqueResultException
	 * 	ʾ����
	 * 		Cust cust = dao.uniqueResult("Cust", "name", "007"); 
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ����
	 * @param propertyName
	 *            ������
	 * @param propertyValue
	 *            ����ֵ
	 * @return T Ψһ����
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
	 * ����ָ����ʵ���ࡢsql����sql�����б���ѯΨһ��¼
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����, ����������Ӧʵ�壬��null����
	 * @param sql
	 *            sql���
	 * @param scalars
	 *            scalar
	 * @param values
	 *            sql�����б�
	 * @return T ʵ��
	 */
	public <T> T uniqueResultBySQL(Class<T> entityClass, String sql,
			Map<String, Type> scalars, Object... values) {
		return this.<T> uniqueResultBySQL(entityClass, sql, null, scalars,
				values);
	}

	/**
	 * <pre>
	 * ����ָ����ʵ���ࡢsql����sql�����б���ѯΨһ��¼
	 * ʾ��1��
	 * 		String sql = "select * from cust where id = ? "
	 * 		Cust cust = dao.uniqueResult(Cust.class, sql, null, null, 100);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����, ����������Ӧʵ�壬��null����
	 * @param sql
	 *            sql���
	 * @param tableAlias
	 *            ��������
	 * @param scalars
	 *            scalar
	 * @param values
	 *            sql�����б�
	 * @return T ʵ��
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
	 * ����һ������persistent״̬�Ķ���, ��Ӧ���ݿ��update. ����persistent״̬�Ķ����Ӧ���ݿ��һ����¼,
	 * ��ĳ��session����,������session����Ч����
	 * 
	 * @param persistentEntity
	 *            ����persistent״̬�Ķ���.
	 */
	public void update(Object persistentEntity) {
		StatelessSession session = openSession();
		session.update(persistentEntity);
	}

	/**
	 * ָ������һ������persistent״̬�Ķ���
	 * 
	 * @param entityName
	 *            ʵ����
	 * @param persistentEntity
	 *            ����persistent״̬�Ķ���.
	 */
	public void update(String entityName, Object persistentEntity) {
		StatelessSession session = openSession();
		session.update(entityName, persistentEntity);
	}

	/**
	 * ����ָ����ʵ������б�
	 * 
	 * @param entities
	 *            ʵ������б�
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
