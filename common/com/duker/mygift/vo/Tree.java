/*
 * @(#)Tree.java 2009-11-25
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.vo;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * <pre>
 * ���޼���
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: 2009-11-25
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class Tree<T extends Serializable> implements Serializable {

	/**
	 * ���л��汾��
	 */
	private static final long serialVersionUID = 3218158303532485977L;

	/**
	 * ��Ԫ��
	 */
	private T element;

	/**
	 * ���ڵ�
	 */
	private Tree<T> parent;

	/**
	 * �ӽڵ�
	 */
	private List<Tree<T>> children;

	public Tree() {
	}

	public Tree(T element) {
		this.element = element;
	}

	public Tree(T element, Tree<T> parent) {
		this.element = element;
		this.setParent(parent);
	}

	public Tree(T element, Tree<T> parent, List<Tree<T>> children) {
		this.element = element;
		this.setParent(parent);
		this.setChildren(children);
	}

	public T getElement() {
		return element;
	}

	public void setElement(T element) {
		this.element = element;
	}

	public List<Tree<T>> getChildren() {
		return children;
	}

	public void setChildren(List<Tree<T>> children) {
		this.children = children;
		if (children != null) {
			for (Tree<T> child : children) {
				child.parent = this;
			}
		}
	}

	public Tree<T> getParent() {
		return parent;
	}

	public void setParent(Tree<T> parent) {
		this.parent = parent;
		if (parent != null) {
			parent.addChild(this);
		}
	}

	public void addChild(Tree<T> subTree) {
		if (subTree == null) {
			return;
		}

		if (children == null) {
			children = new LinkedList<Tree<T>>();
		}

		children.add(subTree);
		subTree.parent = this;
	}

	public boolean hasChild() {
		return children != null && !children.isEmpty();
	}

}
