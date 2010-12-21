package com.angelstone.android.phonetools.utils;

public class ValueNode extends MatchNode{
	
	private ValueNode() {
		super(null);
	}

	@Override
	public Object match(String number, int offset) {
		return getValue();
	}

	@Override
	public void setChildrenNodes(MatchNode[] childrenNodes) {
	}

	@Override
	public void setChildNode(char ch, MatchNode node) {
	}

	public static ValueNode createValueNode() {
		ValueNode node = new ValueNode();
		
		MatchNode[] nodes = node.getChildrenNodes();
		
		for(int i=0;i<nodes.length;i++){
			nodes[i] = node;
		}
		
		return node;
	}
}
