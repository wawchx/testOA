/*
 * @(#)JobDetailFactory.java Dec 26, 2011
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.common.support.quartz;

import java.util.Map;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * <pre>
 * @author wangzh
 *
 * @version 0.9
 *
 * 修改版本: 0.9
 * 修改日期: Dec 26, 2011
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class JobDetailFactory implements FactoryBean<JobDetail>, BeanNameAware,
		InitializingBean {

	private String beanName;

	private String group = Scheduler.DEFAULT_GROUP;

	private String description;

	private Class<? extends Job> jobClass;

	private Map<?, ?> jobDataMap;

	private boolean durability = false;

	private boolean shouldRecover = false;

	private JobDetail jobDetail;

	public void afterPropertiesSet() {
		JobBuilder builder = JobBuilder.newJob(jobClass);
		builder.withIdentity(beanName, group).withDescription(description)
				.storeDurably(durability).requestRecovery(shouldRecover);
		if (jobDataMap != null && !jobDataMap.isEmpty()) {
			builder.usingJobData(new JobDataMap(this.jobDataMap));
		}

		jobDetail = builder.build();
	}

	public JobDetail getObject() {
		return jobDetail;
	}

	public Class<?> getObjectType() {
		return JobDetail.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Class<? extends Job> getJobClass() {
		return jobClass;
	}

	public void setJobClass(Class<? extends Job> jobClass) {
		this.jobClass = jobClass;
	}

	public Map<?, ?> getJobDataMap() {
		return jobDataMap;
	}

	public void setJobDataMap(Map<?, ?> jobDataMap) {
		this.jobDataMap = jobDataMap;
	}

	public boolean isDurability() {
		return durability;
	}

	public void setDurability(boolean durability) {
		this.durability = durability;
	}

	public boolean isShouldRecover() {
		return shouldRecover;
	}

	public void setShouldRecover(boolean shouldRecover) {
		this.shouldRecover = shouldRecover;
	}

}
