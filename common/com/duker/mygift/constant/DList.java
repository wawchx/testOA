/*
 * @(#)DList.java Aug 26, 2009
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.constant;

/**
 * <pre>
 * ���ݿ��и����ֶγ�����ȡֵ
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: Aug 26, 2009
 * �޸��� :  lijingrui
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public interface DList {

	/**
	 * �û���Ϣ��
	 */
	interface UserInfo {

		/**
		 * �û�״̬
		 */
		interface State {

			/**
			 * ��Ч
			 */
			Integer VALID = 1;

			/**
			 * ��Ч
			 */
			Integer INVALID = 2;

			/**
			 * ����
			 */
			Integer LOCKED = 3;
		}
	}

	/**
	 * ������ʾ˳�����
	 */
	interface OperationType {

		/**
		 * �ö�
		 */
		Integer TOP = 0;

		/**
		 * ����
		 */
		Integer UP = 1;

		/**
		 * ����
		 */
		Integer DOWN = 2;

		/**
		 * �õ�
		 */
		Integer BOTTOM = 3;
	}

	interface AuditInfo {

		/**
		 * ���״̬
		 */
		interface status {

			/**
			 * ���ϱ�
			 */
			Integer going = 1;

			/**
			 * ���ϸ�
			 */
			Integer notthrough = 2;

			/**
			 * �����ϱ�
			 */
			Integer goingthrough = 3;

			/**
			 * ��ͨ��
			 */
			Integer through = 4;

			/**
			 * ֱ�ӷ���
			 */
			Integer success = 5;
		}
	}
}
