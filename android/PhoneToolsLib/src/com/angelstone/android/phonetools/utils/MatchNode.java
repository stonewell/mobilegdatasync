package com.angelstone.android.phonetools.utils;

public class MatchNode {
	private Object mValue = null;
	private MatchNode[] mChildrenNodes = new MatchNode[15];

	private MatchNode() {
	}
	
	protected MatchNode(Object value) {
		mValue = value;
	}
	
	public static MatchNode createMatchNode() {
		MatchNode node = new MatchNode();
		
		for(int i=0;i<node.mChildrenNodes.length;i++)
			node.mChildrenNodes[i] = ValueNode.createValueNode();
		
		return node;
	}
	
	public Object getValue() {
		return mValue;
	}

	public void setValue(Object value) {
		mValue = value;
	}

	public MatchNode[] getChildrenNodes() {
		return mChildrenNodes;
	}

	public void setChildrenNodes(MatchNode[] childrenNodes) {
		mChildrenNodes = childrenNodes;
	}

	public MatchNode getChildNode(char ch) {
		return mChildrenNodes[ch - '+'];
	}
	
	public void setChildNode(char ch, MatchNode node) {
		mChildrenNodes[ch - '+'] = node;
	}
	
	public Object match(String number, int offset) {
		return mChildrenNodes[number.charAt(offset) - '+'].match(number, offset + 1);
	}
}
