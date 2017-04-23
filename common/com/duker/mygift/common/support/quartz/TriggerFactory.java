/*
 * @(#)TriggerFactory.java Dec 26, 2011
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.common.support.quartz;

import java.util.Date;
import java.util.Map;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
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
public class TriggerFactory implements FactoryBean<Trigger>, BeanNameAware,
		InitializingBean {

	private JobDetail jobDetail;

	private String beanName;

	private String group = Scheduler.DEFAULT_GROUP;

	private String description;

	private Date startTime = new Date();

	private Date endTime;

	private int priority = Trigger.DEFAULT_PRIORITY;

	private String calendarName;

	private Map<?, ?> jobDataMap;

	private ScheduleBuilder<Trigger> scheduleBuilder;

	private Trigger trigger;

	public void afterPropertiesSet() {
		TriggerBuilder<Trigger> builder = TriggerBuilder.newTrigger();
		builder.withIdentity(beanName, group).forJob(jobDetail)
				.withDescription(description).startAt(startTime).endAt(endTime)
				.withPriority(priority).modifiedByCalendar(calendarName)
				.withSchedule(scheduleBuilder);
		if (jobDataMap != null && !jobDataMap.isEmpty()) {
			builder.usingJobData(new JobDataMap(this.jobDataMap));
		}

		trigger = builder.build();
	}

	public Trigger getObject() {
		return this.trigger;
	}

	public Class<?> getObjectType() {
		return Trigger.class;
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

	public JobDetail getJobDetail() {
		return jobDetail;
	}

	public void setJobDetail(JobDetail jobDetail) {
		this.jobDetail = jobDetail;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getCalendarName() {
		return calendarName;
	}

	public void setCalendarName(String calendarName) {
		this.calendarName = calendarName;
	}

	public Map<?, ?> getJobDataMap() {
		return jobDataMap;
	}

	public void setJobDataMap(Map<?, ?> jobDataMap) {
		this.jobDataMap = jobDataMap;
	}

	public ScheduleBuilder<?> getScheduleBuilder() {
		return scheduleBuilder;
	}

	public void setScheduleBuilder(ScheduleBuilder<Trigger> scheduleBuilder) {
		this.scheduleBuilder = scheduleBuilder;
	}

}
