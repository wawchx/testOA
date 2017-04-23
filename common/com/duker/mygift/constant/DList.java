/*
 * @(#)DList.java Aug 26, 2009
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.constant;

/**
 * <pre>
 * 数据库中各个字段常量的取值
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: Aug 26, 2009
 * 修改人 :  lijingrui
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public interface DList {

	/**
	 * 用户信息表
	 */
	interface UserInfo {

		/**
		 * 用户状态
		 */
		interface State {

			/**
			 * 有效
			 */
			Integer VALID = 1;

			/**
			 * 无效
			 */
			Integer INVALID = 2;

			/**
			 * 锁定
			 */
			Integer LOCKED = 3;
		}
	}

	/**
	 * 分类显示顺序操作
	 */
	interface OperationType {

		/**
		 * 置顶
		 */
		Integer TOP = 0;

		/**
		 * 上移
		 */
		Integer UP = 1;

		/**
		 * 下移
		 */
		Integer DOWN = 2;

		/**
		 * 置底
		 */
		Integer BOTTOM = 3;
	}

	interface AuditInfo {

		/**
		 * 审核状态
		 */
		interface status {

			/**
			 * 已上报
			 */
			Integer going = 1;

			/**
			 * 不合格
			 */
			Integer notthrough = 2;

			/**
			 * 继续上报
			 */
			Integer goingthrough = 3;

			/**
			 * 已通过
			 */
			Integer through = 4;

			/**
			 * 直接发布
			 */
			Integer success = 5;
		}
	}
}
