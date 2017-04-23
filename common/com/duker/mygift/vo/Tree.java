/*
 * @(#)Tree.java 2009-11-25
 * 
 * 信息审核管理系统
 */

package com.duker.mygift.vo;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * <pre>
 * 无限级树
 * 
 * @author wangzh
 * 
 * @version 0.9
 * 
 * 修改版本: 0.9
 * 修改日期: 2009-11-25
 * 修改人 :  wangzh
 * 修改说明: 初步完成
 * 复审人 ：
 * </pre>
 */
public class Tree<T extends Serializable> implements Serializable {

	/**
	 * 序列化版本号
	 */
	private static final long serialVersionUID = 3218158303532485977L;

	/**
	 * 根元素
	 */
	private T element;

	/**
	 * 父节点
	 */
	private Tree<T> parent;

	/**
	 * 子节点
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
