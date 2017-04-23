/*
 * GenericHibernateDao.java
 * 
 * Copyright @ ��Ϣ��˹���ϵͳ
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
 * ͨ�õġ����͵� HibernateDao��
 * �� Dao �ڶ� My Eclispe �Զ����ɵ� Dao����������ȡ�Ļ�����, ������ǿ, ���ڼ����ݷ��ʲ�
 * ��ǿ�Ĺ��ܰ���:
 * 	1�������Ե����ϲ���(findByProperties), ����ڿͻ������й���hql������ɵ�hql�﷨����;
 * 	2����������Ĳ�ѯ(uniqueResultXXX), ����ҵ���߼���ֻ��Ψһ��¼�Ķ����ѯ;
 * 	3��hql������ ����/����/ɾ��, ֧��hql���(execute)��ָ��ʵ���б����ַ�ʽ(deleteAll/saveOrUpdateAll);
 * 	4�����ݷ�ҳ, �Դ������������ݼ���ҳ,Ҫ����ȡ����;
 * 	5��ԭ��̬SQL�Ĳ���/ִ��, ֧��SQL��ʽ�Ĳ���;
 * 	6��֧��������ѯ,��hql������õ�hbm.xml��,�������hql����������;
 * 	7��֧��ʹ�ö�������
 * 
 * @author wangzh
 * @version 1.6.9
 * 
 * �޸İ汾: 1.6.9
 * �޸����ڣ�2014-12-05
 * �޸��� : wangzh
 * �޸�˵����
 * 	1�����ӻ����ҳcount�ܽ�����Ĺ���
 * 
 * �޸İ汾: 1.6.8
 * �޸����ڣ�2010-08-19
 * �޸��� : wangzh
 * �޸�˵����
 * 	1���޸�findByCriterion/uniqueResultByCriterion����,ProjectionͶӰ��ResultTransformer���ת���Ĺ���
 * 
 * �޸İ汾: 1.6.7
 * �޸����ڣ�2010-05-24
 * �޸��� : wangzh
 * �޸�˵����
 * 	1������findTopBy***����,���ص�N������
 * 
 * �޸İ汾: 1.6.6
 * �޸����ڣ�2010-03-25
 * �޸��� : wangzh
 * �޸�˵����
 * 	1���޸�***ByCriterion����,��ԭ����Map&lt;String, FetchMode&gt; fetchModes������ΪList&lt;CriteriaProperty&gt; properties
 * 	��ͬʱ֧�ֹ������Ա����͵���ץȡ����
 *  
 * �޸İ汾: 1.6.5
 * �޸����ڣ�2009-06-25
 * �޸��� : wangzh
 * �޸�˵����
 * 	1���޸ķ�ҳ����,����pageNo,pageSize�Է�ҳ����,����PagedList����
 * 	2��ʹ��hql��sql��ҳ����Ҳ֧������
 * 
 * �޸İ汾: 1.6.4
 * �޸����ڣ�2009-02-18
 * �޸��� : wangzh
 * �޸�˵����
 * 	1������ɾ�����޸ġ�����ʱÿbatchSize�����һ�����ݿ⣬��ֹ���������ύʱ�ڴ����
 * 
 * �޸İ汾: 1.6.3
 * �޸����ڣ�2008-11-21
 * �޸��� : wangzh
 * �޸�˵����
 * 	1���޸�page,findByNamedQuery,uniqueResultByNamedQuery��������Object������ʽ��ֵ
 * 
 * �޸İ汾: 1.6.2
 * �޸����ڣ�2008-11-13
 * �޸��� : wangzh
 * �޸�˵����
 * 	1������ʹ��Criterionȡ��Ψһֵϵ�з���uniqueResultByCriterion
 * 	2���޸�pageϵ�з���������resultCount <= page.getFirstResult()�жϣ���ȡ�������ݵ������ֱ�ӷ���
 * 	3��ɾ��findByQueryExampleһϵ�з���
 * 
 * �޸İ汾: 1.6.1
 * �޸����ڣ�2008-11-13
 * �޸��� : wangzh
 * �޸�˵����
 * 	1������pageBySQL��ҳ������֧��ʹ��SQL���ķ�ҳ��ѯ
 * 
 * �޸İ汾: 1.6.0
 * �޸����ڣ�2008-10-21
 * �޸��� : wangzh
 * �޸�˵����
 * 	1���޸Ķ����Է���find(String, Object...values)��find(String, String, Object...values)
 * 	uniqueResultCached(String, Object...values)��uniqueResultCached(String, String,  Object...values)
 * 	�����������ڴ�������Stringֵʱ�����������
 * 
 * �޸İ汾: 1.5.0
 * �޸����ڣ�2008-10-16
 * �޸��� : wangzh
 * �޸�˵����
 * 	1������ʹ�ö��������ѯ����
 * 
 * �޸İ汾: 1.4.0
 * �޸����ڣ�2008-8-19
 * �޸��� : wangzh
 * �޸�˵����
 * 	1������֧��NamedQuery�ķ�ʽ�Ĳ�ѯ,ʹhql���������õ�hbm.xml�ļ���,���hql����������
 * 
 * �޸İ汾: 1.3.0
 * �޸����ڣ�2008-8-18
 * �޸��� : wangzh
 * �޸�˵����
 * 	1�����ӹ��췽��,ʹhibernateTemplate/sessionFactory����ʹ�ù��췽��ע��
 * 	2������persist/update/save/delete�ȷ���
 * 
 * �޸İ汾: 1.2.0
 * �޸����ڣ�2008-8-8
 * �޸��� : wangzh
 * �޸�˵����
 * 	1���޸İ��������ԺͶ�����Է���Ψһ����ķ�����
 * 
 * �޸İ汾: 1.1.0
 * �޸����ڣ�2008-8-5
 * �޸��� : wangzh
 * �޸�˵����
 * 	1�����Ӷ�� uniqueResult ����,�Է��㵥�����ݲ�ѯ
 * 	2������ SessionFactory ��ע�� HibernateTemplate �ķ��� 
 * 
 * �޸İ汾: 1.0.0
 * �޸����ڣ�2008-5-14
 * �޸��� : wangzh
 * �޸�˵�����γɳ�ʼ�汾
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
	 * ��ҳ��������,����count����
	 */
	private PageCache pageCache;

	/**
	 * �Ƿ��ų����ȶ��������Ӱ���������
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
	 * Ĭ�ϵ� GenericHibernateDao ���췽��
	 */
	public GenericHibernateDao() {
	}

	/**
	 * ʹ�� SessionFactory ��ʵ����һ�� GenericHibernateDao
	 * 
	 * @param sessionFactory
	 *            Hibernate�Ự������
	 */
	public GenericHibernateDao(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * ���ö�������
	 * 
	 * @param cacheMode
	 *            ��������ģʽ
	 */
	public void setCacheMode(CacheMode cacheMode) {
		Session session = sessionFactory.getCurrentSession();
		session.setCacheMode(cacheMode);
	}

	/**
	 * ���һ������,���´�һ���µĻỰ
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
	 * ��ѯsession cache(һ������)���Ƿ�����˶���
	 * 
	 * @param entity
	 *            ʵ��
	 * @return true����,false������
	 */
	public boolean contains(Object entity) {
		return sessionFactory.getCurrentSession().contains(entity);
	}

	/**
	 * ɾ��һ������persistent״̬�Ķ���, ��Ӧ���ݿ��delete. ����persistent״̬�Ķ����Ӧ���ݿ��һ����¼,
	 * ��ĳ��session����,������session����Ч����
	 * 
	 * @param persistentEntity
	 *            ����persistent״̬�Ķ���.
	 */
	public void delete(Object persistentEntity) {
		sessionFactory.getCurrentSession().delete(persistentEntity);
	}

	/**
	 * ɾ��һ���־û�״̬�Ķ���
	 * 
	 * @param persistentEntity
	 *            �־�̬����
	 * @param lockOptions
	 *            ��ģʽ
	 */
	public void delete(Object persistentEntity, LockOptions lockOptions) {
		Session session = sessionFactory.getCurrentSession();
		if (lockOptions != null) {
			session.buildLockRequest(lockOptions).lock(persistentEntity);
		}
		session.delete(persistentEntity);
	}

	/**
	 * ɾ��һ���־û�״̬�Ķ���
	 * 
	 * @param entityName
	 *            ʵ����
	 * @param persistentEntity
	 *            �־�̬����
	 */
	public void delete(String entityName, Object persistentEntity) {
		sessionFactory.getCurrentSession().delete(entityName, persistentEntity);
	}

	/**
	 * ɾ��һ���־û�����
	 * 
	 * @param entityName
	 *            ʵ����
	 * @param persistentEntity
	 *            �־�̬����
	 * @param lockOptions
	 *            ��ģʽ
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
	 * ɾ�������е����ж���
	 * 
	 * @param entities
	 *            �־û����󼯺�
	 */
	public void deleteAll(Collection<?> entities) {
		Session session = sessionFactory.getCurrentSession();
		// ���ö�������
		session.setCacheMode(CacheMode.IGNORE);
		for (Object entity : entities) {
			session.delete(entity);
		}
	}

	/**
	 * �Ѷ����session cache(һ������)���Ƴ�
	 * 
	 * @param entity
	 *            ʵ��
	 */
	public void evict(Object entity) {
		sessionFactory.getCurrentSession().evict(entity);
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
	 * �����µ�����id���������������ݿ��¼
	 * 
	 * @param clz
	 *            ʵ����
	 * @return ����id
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
	 * ��ȡ�����id
	 * 
	 * @param object
	 *            ����
	 * @return ����id
	 */
	public Serializable getIdentifier(Object object) {
		Session session = sessionFactory.getCurrentSession();
		return session.getIdentifier(object);
	}

	/**
	 * ��ȡ�����id������
	 * 
	 * @param object
	 *            ����
	 * @return ����id������
	 */
	public String getIdentifierPropertyName(Object object) {
		Class<?> clazz = getUserClass(object);
		ClassMetadata meta = sessionFactory.getClassMetadata(clazz);
		return meta.getIdentifierPropertyName();
	}

	/**
	 * ��ȡ�������ķǴ�����
	 * 
	 * @param object
	 *            �������
	 * @return �Ǵ�����
	 */
	public static Class<?> getUserClass(Object object) {
		return getUserClass(object.getClass());
	}

	/**
	 * ��ȡ�������ķǴ�����
	 * 
	 * @param clazz
	 *            ����������
	 * @return �Ǵ�����
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
	 * ʹ�ö��������������ʵ��
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityClass
	 *            ʵ����
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findAll(String cacheRegion, Class<T> entityClass) {
		return this
				.<T> findCached(cacheRegion, "from " + entityClass.getName());
	}

	/**
	 * ʹ�ö��������������ʵ��
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityName
	 *            ʵ����
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findAll(String cacheRegion, String entityName) {
		return this.<T> findCached(cacheRegion, "from " + entityName);
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
			Criterion criterion, List<SortOrder> orders) {
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
			Criterion criterion, List<SortOrder> orders,
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
	 *            ��ѯ���ͶӰ,��Ϊ��
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findByCriterion(Class<T> entityClass,
			Criterion criterion, List<SortOrder> orders,
			List<CriteriaProperty> properties, Projection projection,
			ResultTransformer transformer) {
		return this.<T> findByCriterion(entityClass.getName(), criterion,
				orders, properties, projection, transformer);
	}

	/**
	 * ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            ��hibernate��ѯ��׼
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findByCriterion(String cacheRegion,
			Class<T> entityClass, Criterion criterion) {
		return this.<T> findByCriterion(cacheRegion, entityClass.getName(),
				criterion);
	}

	/**
	 * ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            ��hibernate��ѯ��׼
	 * @param orders
	 *            �������,��Ϊ��
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findByCriterion(String cacheRegion,
			Class<T> entityClass, Criterion criterion, List<SortOrder> orders) {
		return this.<T> findByCriterion(cacheRegion, entityClass.getName(),
				criterion, orders);
	}

	/**
	 * ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            ��hibernate��ѯ��׼
	 * @param orders
	 *            �������,��Ϊ��
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findByCriterion(String cacheRegion,
			Class<T> entityClass, Criterion criterion, List<SortOrder> orders,
			List<CriteriaProperty> properties) {
		return this.<T> findByCriterion(cacheRegion, entityClass.getName(),
				criterion, orders, properties);
	}

	/**
	 * ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            ��hibernate��ѯ��׼
	 * @param orders
	 *            �������,��Ϊ��
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @param projection
	 *            ��ѯ���ͶӰ,��Ϊ��
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findByCriterion(String cacheRegion,
			Class<T> entityClass, Criterion criterion, List<SortOrder> orders,
			List<CriteriaProperty> properties, Projection projection,
			ResultTransformer transformer) {
		return this.<T> findByCriterion(cacheRegion, entityClass.getName(),
				criterion, orders, properties, projection, transformer);
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
		Session session = sessionFactory.getCurrentSession();
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
	 *            ��ѯ���ͶӰ,��Ϊ��
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
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
	 * ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @param projection
	 *            ��ѯ���ͶӰ,��Ϊ��
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @return List&lt;T&gt; ʵ���б�
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
			List<SortOrder> orders) {
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
			List<SortOrder> orders, List<CriteriaProperty> properties) {
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
	 *            ��ѯ���ͶӰ,��Ϊ��
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param exampleEntity
	 *            ��ʵ�����
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findByExample(String cacheRegion, Object exampleEntity) {
		String entityName = exampleEntity.getClass().getName();
		Criterion criterion = Example.create(exampleEntity);

		return this.<T> findByCriterion(cacheRegion, entityName, criterion);
	}

	/**
	 * ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param exampleEntity
	 *            ��ʵ�����
	 * @param orders
	 *            �������,��Ϊ��
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findByExample(String cacheRegion, Object exampleEntity,
			List<SortOrder> orders) {
		String entityName = exampleEntity.getClass().getName();
		Criterion criterion = Example.create(exampleEntity);

		return this.<T> findByCriterion(cacheRegion, entityName, criterion,
				orders);
	}

	/**
	 * ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param exampleEntity
	 *            ��ʵ�����
	 * @param orders
	 *            �������,��Ϊ��
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findByExample(String cacheRegion, Object exampleEntity,
			List<SortOrder> orders, List<CriteriaProperty> properties) {
		String entityName = exampleEntity.getClass().getName();
		Criterion criterion = Example.create(exampleEntity);

		return this.<T> findByCriterion(cacheRegion, entityName, criterion,
				orders, properties);
	}

	/**
	 * ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param exampleEntity
	 *            ��ʵ�����
	 * @param orders
	 *            �������,��Ϊ��
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @param projection
	 *            ��ѯ���ͶӰ,��Ϊ��
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @return List&lt;T&gt; ʵ���б�
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
		Session session = sessionFactory.getCurrentSession();

		return (T) session.get(entityClass, id);
	}

	/**
	 * <pre>
	 * �����������Ҷ���,ͬʱ����
	 * ���ָ��id�Ķ��󲻴���,�򷵻�null
	 * 	ʾ����
	 * 		Cust cust = dao.findById(Cust.class,1L, LockOptions.READ);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����, �� Cust.class
	 * @param id
	 *            ����ֵ
	 * @param lockOptions
	 *            ��ģʽ
	 * @return T ʵ��
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
		Session session = sessionFactory.getCurrentSession();

		return (T) session.get(entityName, id);
	}

	/**
	 * <pre>
	 * ����������ѯ����,ͬʱ����
	 * ���ָ��id�Ķ��󲻴���,�򷵻�null
	 * 	ʾ����
	 * 		Cust cust = dao.findById("Cust", 1L, LockOptions.READ);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ�������� "Cust"
	 * @param id
	 *            ����ֵ
	 * @param lockOptions
	 *            ��ģʽ
	 * @return T ʵ��
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
	 * �����������Ҷ���ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����, �� Cust.class
	 * @param id
	 *            ����ֵ
	 * @return T ʵ��
	 */
	public <T> T findByIdCached(Class<T> entityClass, Serializable id) {
		return this.<T> findByIdCached(entityClass, id, null);
	}

	/**
	 * �����������Ҷ���,ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����, �� Cust.class
	 * @param id
	 *            ����ֵ
	 * @param lockOptions
	 *            ��ģʽh
	 * @return T ʵ��
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
	 * ����������ѯ����,ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ�������� "Cust"
	 * @param id
	 *            ����ֵ
	 * @return T ʵ��
	 */
	public <T> T findByIdCached(String entityName, Serializable id) {
		return this.<T> findByIdCached(entityName, id, null);
	}

	/**
	 * ����������ѯ����,ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ�������� "Cust"
	 * @param id
	 *            ����ֵ
	 * @param lockOptions
	 *            ��ģʽ
	 * @return T ʵ��
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
	 * ʹ��������ѯ��֧�ֶ�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param queryName
	 *            ��ѯ��
	 * @param values
	 *            ����ֵ
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ����ָ����ʵ���ࡢ��������(key)����ֵ(value)��Map, �Զ������Ϊ��������ѯ��ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityClass
	 *            ʵ����
	 * @param propertities
	 *            ��ֵ��
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findByProperties(String cacheRegion,
			Class<T> entityClass, Map<String, Object> propertities) {
		return this.<T> findByProperties(cacheRegion, entityClass.getName(),
				propertities);
	}

	/**
	 * ����ָ����ʵ���ࡢ�������б�����ֵ�б�, �Զ������Ϊ��������ѯ��ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityClass
	 *            ʵ����
	 * @param propertyNames
	 *            ��������
	 * @param propertyValues
	 *            ֵ����
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findByProperties(String cacheRegion,
			Class<T> entityClass, String[] propertyNames,
			Object[] propertyValues) {
		return this.<T> findByProperties(cacheRegion, entityClass.getName(),
				propertyNames, propertyValues);
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
	 * ����ָ����ʵ��������������(key)����ֵ(value)��Map, �Զ������Ϊ��������ѯ��ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityName
	 *            ʵ����
	 * @param propertities
	 *            ��ֵ��
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ����ָ����ʵ�������������б�����ֵ�б�, �Զ������Ϊ��������ѯ��ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityName
	 *            ʵ����
	 * @param propertyNames
	 *            ��������
	 * @param propertyValues
	 *            ֵ����
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ����ָ����ʵ���ࡢ������������ֵ, �Ե�������Ϊ��������ѯ��ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityClass
	 *            ʵ����
	 * @param propertyName
	 *            ������
	 * @param propertyValue
	 *            ����ֵ
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findByProperty(String cacheRegion, Class<T> entityClass,
			String propertyName, Object propertyValue) {
		return this.<T> findByProperty(cacheRegion, entityClass.getName(),
				propertyName, propertyValue);
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
	 * ����ָ����ʵ������������������ֵ, �Ե�������Ϊ��������ѯ��ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityName
	 *            ʵ����, �� "Cust"
	 * @param propertyName
	 *            ������
	 * @param propertyValue
	 *            ����ֵ
	 * @return List&lt;T&gt; ʵ���б�
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
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @param values
	 *            �����б�
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findBySQL(Class<T> entityClass, String sql,
			Map<String, Type> scalars, ResultTransformer transformer,
			Object... values) {
		return this.<T> findBySQL(entityClass, null, sql, scalars, transformer,
				values);
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
	 * @param tableAlias
	 *            ���������
	 * @param sql
	 *            ָ����sql���
	 * @param scalars
	 *            scalar
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @param values
	 *            �����б�
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ����ָ����ʵ���ࡢsql����sql�����б���ѯ�����б�ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityClass
	 *            ʵ����, ����������Ӧʵ�壬��null����
	 * @param sql
	 *            ָ����sql���
	 * @param scalars
	 *            scalar
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @param values
	 *            �����б�
	 * @return List&lt;T&gt; ʵ���б�
	 */
	public <T> List<T> findBySQL(String cacheRegion, Class<T> entityClass,
			String sql, Map<String, Type> scalars,
			ResultTransformer transformer, Object... values) {
		return this.<T> findBySQL(cacheRegion, entityClass, null, sql, scalars,
				transformer, values);
	}

	/**
	 * ����ָ����ʵ���ࡢsql����sql�����б���ѯ�����б�ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityClass
	 *            ʵ����, ����������Ӧʵ�壬��null����
	 * @param tableAlias
	 *            ��������
	 * @param sql
	 *            ָ����sql���
	 * @param scalars
	 *            scalar
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @param values
	 *            �����б�
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ����ָ����ʵ���ࡢsql����sql�����б���ѯ�����б����ص�ʵ��Ϊ���ܹ�ʵ��
	 * 	ʾ��:
	 * 		String sql = "select cust_id as custId, cust_name as custName from cust where id = ? and sex = ? ";
	 * 		List&lt;Cust&gt; custs = dao.findBySQL(Cust.class, sql, 100, 1);
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
	 * @param values
	 *            �����б�
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ����ָ����ʵ���ࡢsql����sql�����б���ѯ�����б����ص�ʵ��Ϊ���ܹ�ʵ�壬ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
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
	 * ����ָ����<b>hql</b>�Ͳ����б����ز��ҵĽ����ʹ�ö�������
	 * ʾ����
	 * 		String hql = "from Cust as cust where cust.name like ? ";
	 * 		List&lt;Cust&gt; custs = dao.find("Cust", hql, "%007%");
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param hql
	 *            hql���
	 * @param values
	 *            �����б�
	 * @return List&lt;T&gt; ʵ���б�
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
	 * ˢ�³־û�����
	 */
	public void flush() {
		sessionFactory.getCurrentSession().flush();
	}

	/**
	 * ���ָ����sequence��nextval
	 * 
	 * @param sequenceName
	 *            ���к���
	 * @return Long ����ֵ
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
	 * ��������
	 * 
	 * @param entity
	 *            �־û�����
	 */
	public void lock(Object entity) {
		sessionFactory
				.getCurrentSession()
				.buildLockRequest(LockOptions.NONE)
				.lock(entity);
	}

	/**
	 * ��������
	 * 
	 * @param entity
	 *            ʵ��
	 * @param lockOptions
	 *            ��ģʽ
	 */
	public void lock(Object entity, LockOptions lockOptions) {
		sessionFactory
				.getCurrentSession()
				.buildLockRequest(lockOptions)
				.lock(entity);
	}

	/**
	 * ��������
	 * 
	 * @param entityName
	 *            ʵ����
	 * @param entity
	 *            ʵ��
	 * @param lockOptions
	 *            ��ģʽ
	 */
	public void lock(String entityName, Object entity, LockOptions lockOptions) {
		sessionFactory
				.getCurrentSession()
				.buildLockRequest(lockOptions)
				.lock(entityName, entity);
	}

	/**
	 * ��һ������detached״̬�Ķ�����session���¹�����ʹ֮����persistent״̬
	 * ����detached״̬�Ķ����Ӧ���ݿ��һ����¼(��������ֵ), ����Hibernateʵ�������޹�,����session�޹���
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param detachedInstance
	 *            ����detached״̬�Ķ���.
	 * @return T ʵ��
	 */
	public <T> T merge(Object detachedInstance) {
		return (T) sessionFactory.getCurrentSession().merge(detachedInstance);
	}

	/**
	 * ��һ������detached״̬�Ķ�����session���¹�����ʹ֮����persistent״̬
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityName
	 *            ʵ����
	 * @param detachedInstance
	 *            ����detached״̬�Ķ���.
	 * @return T ʵ��
	 * @throws DataAccessException
	 */
	public <T> T merge(String entityName, Object detachedInstance)
			throws DataAccessException {
		return (T) sessionFactory.getCurrentSession().merge(entityName,
				detachedInstance);
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
			List<SortOrder> orders) {
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
			List<SortOrder> orders, List<CriteriaProperty> properties) {
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
			int pageSize, List<SortOrder> orders) {
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
			int pageSize, List<SortOrder> orders,
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
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @param values
	 *            ����ֵ
	 * @return PagedList&lt;T&gt; ��ҳ��ѯ�Ľ���б�
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
	 * @param tableAlias
	 *            ��������
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
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @param values
	 *            ����ֵ
	 * @return PagedList&lt;T&gt; ��ҳ��ѯ�Ľ���б�
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
	 * ����ָ����ʵ���ࡢsql����sql�����б���ѯ��ҳ�б����ص�ʵ��Ϊ���ܹ�ʵ��
	 * 	ʾ��:
	 * 		String sql = "select cust_id as custId, cust_name as custName from cust where id = ? and sex = ? ";
	 * 		PagedList&lt;Cust&gt; custs = dao.pageBySQLDetached(Cust.class, sql, 1, 10, null, 100, 1);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����, ����������Ӧʵ�壬��null����
	 * @param sql
	 *            ָ����sql���
	 * @param pageNo
	 *            ��ǰҳ��,��0��ʼ
	 * @param pageSize
	 *            ҳ���С
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param scalars
	 *            scalar
	 * @param values
	 *            �����б�
	 * @return List&lt;T&gt; ʵ���б�
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
			List<SortOrder> orders) {
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
			List<SortOrder> orders, List<CriteriaProperty> properties) {
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
			int endIndex, List<SortOrder> orders) {
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
			int endIndex, List<SortOrder> orders,
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
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @param values
	 *            ����ֵ
	 * @return List&lt;T&gt; ��ҳ��ѯ�Ľ���б�
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
	 * @param tableAlias
	 *            ��������
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
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @param values
	 *            ����ֵ
	 * @return List&lt;T&gt; ��ҳ��ѯ�Ľ���б�
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
	 * ����ָ����ʵ���ࡢsql����sql�����б���ѯ�������ݶ����б����ص�ʵ��Ϊ���ܹ�ʵ��
	 * 	ʾ��:
	 * 		String sql = "select cust_id as custId, cust_name as custName from cust where id = ? and sex = ? ";
	 * 		List&lt;Cust&gt; custs = dao.findPartialBySQLDetached(Cust.class, sql, 1, 10, null, 100, 1);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����, ����������Ӧʵ�壬��null����
	 * @param sql
	 *            ָ����sql���
	 * @param startIndex
	 *            ��ʼλ��,��0��ʼ
	 * @param endIndex
	 *            ����λ��
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param scalars
	 *            scalar
	 * @param values
	 *            �����б�
	 * @return List&lt;T&gt; ʵ���б�
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
			int top, List<SortOrder> orders) {
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
			int top, List<SortOrder> orders, List<CriteriaProperty> properties) {
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
			List<SortOrder> orders) {
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
			List<SortOrder> orders, List<CriteriaProperty> properties) {
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
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @param values
	 *            ����ֵ
	 * @return ��top�����
	 */
	public <T> T findTopBySQL(Class<T> entityClass, String sql, int top,
			List<SortOrder> orders, Map<String, Type> scalars,
			ResultTransformer transformer, Object... values) {
		return this.<T> findTopBySQL(entityClass, null, sql, top, orders,
				scalars, transformer, values);
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
	 * @param tableAlias
	 *            ��������
	 * @param sql
	 *            sql���
	 * @param top
	 *            �ڼ������,��1��ʼ
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param scalars
	 *            scalar
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @param values
	 *            ����ֵ
	 * @return ��top�����
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
	 * ����ָ����ʵ���ࡢsql����sql�����б���ѯ��top�����ݶ��󣬷��ص�ʵ��Ϊ���ܹ�ʵ��
	 * 	ʾ��:
	 * 		String sql = "select cust_id as custId, cust_name as custName from cust where id = ? and sex = ? ";
	 * 		Cust cust = dao.findTopBySQLDetached(Cust.class, sql, 1, null, 100, 1);
	 * </pre>
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param entityClass
	 *            ʵ����, ����������Ӧʵ�壬��null����
	 * @param sql
	 *            ָ����sql���
	 * @param top
	 *            �ڼ������,��1��ʼ
	 * @param orders
	 *            Order������Ϊ�������ݣ����Ϊnull���򲻽�������
	 * @param scalars
	 *            scalar
	 * @param values
	 *            �����б�
	 * @return ��top�����
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
	 * �־û�����
	 * 
	 * @param entity
	 *            ʵ�����
	 */
	public void persist(Object entity) {
		sessionFactory.getCurrentSession().persist(entity);
	}

	/**
	 * �־û�����
	 * 
	 * @param entityName
	 *            ʵ����
	 * @param entity
	 *            ʵ�����
	 */
	public void persist(String entityName, Object entity) {
		sessionFactory.getCurrentSession().persist(entityName, entity);
	}

	/**
	 * ���¶���״̬
	 * 
	 * @param entity
	 *            ʵ��
	 */
	public void refresh(Object entity) {
		sessionFactory.getCurrentSession().refresh(entity);
	}

	/**
	 * ���¶���״̬
	 * 
	 * @param entity
	 *            ʵ��
	 * @param lockOptions
	 *            ��ģʽ
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
	 * ����һ������transient״̬�Ķ���, ��Ӧ���ݿ��insert. ����transient״̬�Ķ���û��������Ϣ,�����ݿ��¼û���κι���
	 * 
	 * @param transientInstance
	 *            ����transient״̬�Ķ���
	 * @return Serializable ����ֵ
	 */
	public Serializable save(Object transientInstance) {
		return sessionFactory.getCurrentSession().save(transientInstance);
	}

	/**
	 * ����������transient״̬�Ķ���, ��Ӧ���ݿ��insert. ����transient״̬�Ķ���û��������Ϣ,�����ݿ��¼û���κι���
	 * 
	 * @param entities
	 *            ����transient״̬�Ķ��󼯺�
	 * @return Serializable ����ֵ
	 */
	public void saveAll(Collection<?> entities) {
		Session session = sessionFactory.getCurrentSession();
		// ���ö�������
		session.setCacheMode(CacheMode.IGNORE);

		for (Object entity : entities) {
			session.save(entity);
		}
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
		return sessionFactory.getCurrentSession().save(entityName, entity);
	}

	/**
	 * ����һ������transient״̬�Ķ���, ��Ӧ���ݿ��insert. ����transient״̬�Ķ���û��������Ϣ,�����ݿ��¼û���κι���
	 * 
	 * @param transientInstance
	 *            ����transient״̬�Ķ���
	 * @param replicationMode
	 *            ������Ϊ
	 */
	public void replicate(Object transientInstance,
			ReplicationMode replicationMode) {
		sessionFactory.getCurrentSession().replicate(transientInstance,
				replicationMode);
	}

	/**
	 * ����һ������transient״̬�Ķ���, ��Ӧ���ݿ��insert. ����transient״̬�Ķ���û��������Ϣ,�����ݿ��¼û���κι���
	 * 
	 * @param entityName
	 *            ʵ����
	 * @param entity
	 *            ʵ�����
	 * @param replicationMode
	 *            ������Ϊ
	 */
	public void replicate(String entityName, Object entity,
			ReplicationMode replicationMode) {
		sessionFactory.getCurrentSession().replicate(entityName, entity,
				replicationMode);
	}

	/**
	 * �������. ���������transient״̬,��insert�����ݿ�, ����update
	 * 
	 * @param entity
	 *            ʵ�����
	 */
	public void saveOrUpdate(Object entity) {
		sessionFactory.getCurrentSession().saveOrUpdate(entity);
	}

	/**
	 * �������. ���������transient״̬,��insert�����ݿ�, ����update
	 * 
	 * @param entityName
	 *            ʵ����
	 * @param entity
	 *            ʵ�����
	 */
	public void saveOrUpdate(String entityName, Object entity) {
		sessionFactory.getCurrentSession().saveOrUpdate(entityName, entity);
	}

	/**
	 * ����ָ����ʵ������б�
	 * 
	 * @param entities
	 *            ʵ������б�
	 */
	public void saveOrUpdateAll(Collection<?> entities) {
		Session session = sessionFactory.getCurrentSession();
		// ���ö�������
		session.setCacheMode(CacheMode.IGNORE);

		for (Object entity : entities) {
			session.saveOrUpdate(entity);
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
	 *            ��ѯ���ͶӰ,��Ϊ��
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @return T ʵ��
	 */
	public <T> T uniqueResultByCriterion(Class<T> entityClass,
			Criterion criterion, List<CriteriaProperty> properties,
			Projection projection, ResultTransformer transformer) {
		return this.<T> uniqueResultByCriterion(entityClass.getName(),
				criterion, properties, projection, transformer);
	}

	/**
	 * ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            ��hibernate��ѯ��׼
	 * @return T ʵ��
	 */
	public <T> T uniqueResultByCriterion(String cacheRegion,
			Class<T> entityClass, Criterion criterion) {
		return this.<T> uniqueResultByCriterion(cacheRegion,
				entityClass.getName(), criterion);
	}

	/**
	 * ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            ��hibernate��ѯ��׼
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @return T ʵ��
	 */
	public <T> T uniqueResultByCriterion(String cacheRegion,
			Class<T> entityClass, Criterion criterion,
			List<CriteriaProperty> properties) {
		return this.<T> uniqueResultByCriterion(cacheRegion,
				entityClass.getName(), criterion, properties);
	}

	/**
	 * ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityClass
	 *            ʵ����
	 * @param criterion
	 *            ��hibernate��ѯ��׼
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @param projection
	 *            ��ѯ���ͶӰ,��Ϊ��
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @return T ʵ��
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
		Session session = sessionFactory.getCurrentSession();
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
	 *            ��ѯ���ͶӰ,��Ϊ��
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @return T ʵ��
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
	 * ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @return T ʵ��
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
	 * ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @return T ʵ��
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
	 * ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityName
	 *            ʵ����
	 * @param criterion
	 *            Criterion����, ��Ϊ��ѯ����
	 * @param properties
	 *            ����ץȡ������������Ա���,��Ϊ��
	 * @param projection
	 *            ��ѯ���ͶӰ,��Ϊ��
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @return T ʵ��
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
	 * ����ָ�������ò�ѯ���ơ�bean����ѯΨһ����ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param queryName
	 *            ��ѯ��
	 * @param values
	 *            ��ʵ��
	 * @return T Ψһ����
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
	 * ����ָ����ʵ���ࡢ��������(key)����ֵ(value)��Map, �Զ������Ϊ��������ѯΨһ����ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityClass
	 *            ʵ����
	 * @param propertities
	 *            ��������ֵ��
	 * @return T Ψһ����
	 */
	public <T> T uniqueResultByProperties(String cacheRegion,
			Class<T> entityClass, Map<String, Object> propertities) {
		return this.<T> uniqueResultByProperties(cacheRegion,
				entityClass.getName(), propertities);
	}

	/**
	 * ����ָ����ʵ���ࡢ�������б�����ֵ�б�, �Զ������Ϊ��������ѯΨһ����ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityClass
	 *            ʵ����
	 * @param propertyNames
	 *            ������ֵ����
	 * @param propertyValues
	 *            ��ֵ����
	 * @return Ψһ����
	 */
	public <T> T uniqueResultByProperties(String cacheRegion,
			Class<T> entityClass, String[] propertyNames,
			Object[] propertyValues) {
		return this.<T> uniqueResultByProperties(cacheRegion,
				entityClass.getName(), propertyNames, propertyValues);
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
	 * ����ָ����ʵ��������������(key)����ֵ(value)��Map, �Զ������Ϊ��������ѯΨһ����ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityName
	 *            ʵ����
	 * @param propertities
	 *            ��������ֵ��
	 * @return T Ψһ����
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
	 * ����ָ����ʵ�������������б�����ֵ�б�, �Զ������Ϊ��������ѯΨһ����ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityName
	 *            ʵ����
	 * @param propertyNames
	 *            ��������ֵ����
	 * @param propertyValues
	 *            ��ֵ����
	 * @return T Ψһ����
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
	 * ����ָ����ʵ���ࡢ������������ֵ, �Ե�������Ϊ��������ѯΨһ����ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityClass
	 *            ʵ����
	 * @param propertyName
	 *            ������
	 * @param propertyValue
	 *            ����ֵ
	 * @return T Ψһ����
	 */
	public <T> T uniqueResultByProperty(String cacheRegion,
			Class<T> entityClass, String propertyName, Object propertyValue) {
		return this.<T> uniqueResultByProperty(cacheRegion,
				entityClass.getName(), propertyName, propertyValue);
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
	 * ����ָ����ʵ������������������ֵ, �Ե�������Ϊ��������ѯΨһ����ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityName
	 *            ʵ����
	 * @param propertyName
	 *            ������
	 * @param propertyValue
	 *            ����ֵ
	 * @return T Ψһ����
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
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @param values
	 *            sql�����б�
	 * @return T ʵ��
	 */
	public <T> T uniqueResultBySQL(Class<T> entityClass, String sql,
			Map<String, Type> scalars, ResultTransformer transformer,
			Object... values) {
		return this.<T> uniqueResultBySQL(entityClass, null, sql, scalars,
				transformer, values);
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
	 * @param tableAlias
	 *            ��������
	 * @param sql
	 *            sql���
	 * @param scalars
	 *            scalar
	 * @param transformer
	 *            ���Ӳ�ѯ���ת����,��Ϊ��
	 * @param values
	 *            sql�����б�
	 * @return T ʵ��
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
	 * ����ָ����ʵ���ࡢsql����sql�����б���ѯΨһ��¼��ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityClass
	 *            ʵ����, ����������Ӧʵ�壬��null����
	 * @param sql
	 *            sql���
	 * @param scalars
	 *            scalar
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @param values
	 *            sql�����б�
	 * @return T ʵ��
	 */
	public <T> T uniqueResultBySQL(String cacheRegion, Class<T> entityClass,
			String sql, Map<String, Type> scalars,
			ResultTransformer transformer, Object... values) {
		return this.<T> uniqueResultBySQL(cacheRegion, entityClass, null, sql,
				scalars, transformer, values);
	}

	/**
	 * ����ָ����ʵ���ࡢsql����sql�����б���ѯΨһ��¼��ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param entityClass
	 *            ʵ����, ����������Ӧʵ�壬��null����
	 * @param tableAlias
	 *            ��������
	 * @param sql
	 *            sql���
	 * @param scalars
	 *            scalar
	 * @param transformer
	 *            ��ѯ���ת����,��Ϊ��
	 * @param values
	 *            sql�����б�
	 * @return T ʵ��
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
	 * ����ָ����hql�Ͳ����б���ѯΨһ����ʹ�ö�������
	 * 
	 * @param <T>
	 *            ʵ������
	 * @param cacheRegion
	 *            ������
	 * @param hql
	 *            ָ����hql���
	 * @param values
	 *            �����б�
	 * @return T Ψһ����
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
	 * ����һ������persistent״̬�Ķ���, ��Ӧ���ݿ��update. ����persistent״̬�Ķ����Ӧ���ݿ��һ����¼,
	 * ��ĳ��session����,������session����Ч����
	 * 
	 * @param persistentEntity
	 *            ����persistent״̬�Ķ���.
	 */
	public void update(Object persistentEntity) {
		sessionFactory.getCurrentSession().update(persistentEntity);
	}

	/**
	 * ���¶������persistent״̬�Ķ���, ��Ӧ���ݿ��update. ����persistent״̬�Ķ����Ӧ���ݿ��һ����¼,
	 * ��ĳ��session����,������session����Ч����
	 * 
	 * @param entities
	 *            ����persistent״̬�Ķ��󼯺�.
	 */
	public void updateAll(Collection<?> entities) {
		Session session = sessionFactory.getCurrentSession();
		// ���ö�������
		session.setCacheMode(CacheMode.IGNORE);

		for (Object entity : entities) {
			session.update(entity);
		}
	}

	/**
	 * ָ������һ������persistent״̬�Ķ���
	 * 
	 * @param persistentEntity
	 *            ����persistent״̬�Ķ���.
	 * @param lockOptions
	 *            ��ģʽ
	 */
	public void update(Object persistentEntity, LockOptions lockOptions) {
		Session session = sessionFactory.getCurrentSession();
		session.update(persistentEntity);
		if (lockOptions != null) {
			session.buildLockRequest(lockOptions).lock(persistentEntity);
		}
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
		sessionFactory.getCurrentSession().update(entityName, persistentEntity);
	}

	/**
	 * ָ������һ������persistent״̬�Ķ���
	 * 
	 * @param entityName
	 *            ʵ����
	 * @param persistentEntity
	 *            ����persistent״̬�Ķ���.
	 * @param lockOptions
	 *            ��ģʽ
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
	 * ȥ��SQL���������֮��Ĳ���
	 * 
	 * @param result
	 *            ȥ�����ź�Ĵ�
	 * @return ȥ������ǰ�Ĵ�
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
	 * ȡ��SQL�еı���
	 * 
	 * @param sql
	 *            SQL���
	 * @return �����б�
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
	 * ���ݽ����������������ʵ�弯��
	 * 
	 * @param entityClass
	 *            ʵ��
	 * @param rs
	 *            �����
	 * @param properties
	 *            ����
	 * @return List ʵ�弯��
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
	 * ȡ��Hibernate֧�ֵ�����
	 * 
	 * @param entityClass
	 *            ʵ����
	 * @param properties
	 *            �����б�
	 * @return �����б�
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
