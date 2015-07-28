/* Copyright (c) 2003, Carl Burch. License information is located in the
 * edu.csbsju.socs.grammar.Main source code and at
 * www.cburch.com/proj/grammar/. */

package edu.csbsju.socs.grammar;

class Tree {
	private static final Tree[] null_children = { };

	private Object data;
	private Tree[] children;
	private boolean is_leaf = false;

	protected Tree(Object data) {
		this(data, null_children);
		this.is_leaf = true;
	}
	protected Tree(Object data, Tree[] children) {
		this.data = data;
		this.children = children;
	}
	public Object getData() { return data; }
	public Tree getChild(int which) { return children[which]; }
	public Tree[] getChildren() { return children; }
	public boolean isLeaf() { return is_leaf; }

	public static Tree createNode(Object data, Tree[] children) {
		return new Tree(data, children);
	}
	public static Tree createLeaf(Object data) {
		return new Tree(data);
	}
}
