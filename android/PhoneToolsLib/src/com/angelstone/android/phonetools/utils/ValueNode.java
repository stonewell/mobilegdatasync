package com.angelstone.android.phonetools.utils;

public class ValueNode extends MatchNode{
	
	public ValueNode() {
		MatchNode[] nodes = super.getChildrenNodes();
		
		for(int i=0;i<nodes.length;i++){
			nodes[i] = this;
		}
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
}
