package com.angelstone.android.phonetools.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PhoneNumberMatcher {
	private Set<String> mCountryCodes = new HashSet<String>();
	private Set<String> mAreaCodes = new HashSet<String>();
	private HashMap<String, Object> mNumbers = new HashMap<String, Object>();
	private MatchNode mRootNode = null;

	public PhoneNumberMatcher() {

	}

	public Set<String> getCountryCodes() {
		return mCountryCodes;
	}

	public void setCountryCodes(Set<String> countryCodes) {
		mCountryCodes = countryCodes;
	}

	public Set<String> getAreaCodes() {
		return mAreaCodes;
	}

	public void setAreaCodes(Set<String> areaCodes) {
		mAreaCodes = areaCodes;
	}

	public HashMap<String, Object> getNumbers() {
		return mNumbers;
	}

	public void setNumbers(HashMap<String, Object> numbers) {
		mNumbers = numbers;
	}

	public void addNumber(String number, Object value) {
		if (mNumbers.containsKey(number))
			return;

		if (internalAddNumber(number, value)) {
			mNumbers.put(number, value);
		}
	}

	private boolean internalAddNumber(String number, Object value) {
		int length = number.length();
		
		if (length == 0)
			return false;
		
		MatchNode lastNode = findOrCreateNodeAtPos(number, length - 1);

		if (lastNode == null)
			return false;

		MatchNode childNode = new ValueNode();
		childNode.setValue(value);
		lastNode.setChildNode(number.charAt(length - 1), childNode);
		
		return true;
	}

	public void removeNumber(String number) {
		if (!mNumbers.containsKey(number))
			return;

		int length = number.length();
		
		if (length == 0)
			return;
		
		MatchNode lastNode = findOrCreateNodeAtPos(number, length - 1);

		if (lastNode == null)
			return;

		mNumbers.remove(number);

		MatchNode childNode = new ValueNode();
		childNode.setValue(null);
		lastNode.setChildNode(number.charAt(length - 1), childNode);
	}

	public void updateNumber(String number, Object value) {
		if (!mNumbers.containsKey(number)) {
			addNumber(number, value);
			return;
		}

		int length = number.length();
		
		if (length == 0)
			return;
		
		MatchNode lastNode = findOrCreateNodeAtPos(number, length - 1);

		if (lastNode == null)
			return;

		MatchNode childNode = new ValueNode();
		childNode.setValue(value);
		lastNode.setChildNode(number.charAt(length - 1), childNode);
	}

	public void build() {
		mRootNode = new MatchNode();
		buildCountryCode();
		buildAreaCode();
		buildNumbers();
	}

	private void buildNumbers() {
		for (String cc : mNumbers.keySet()) {
			internalAddNumber(cc, mNumbers.get(cc));
		}
	}

	private void buildAreaCode() {
		for (String cc : mAreaCodes) {
			buildTopLevelNumber(cc);
		}
	}

	private void buildCountryCode() {
		for (String cc : mCountryCodes) {
			buildTopLevelNumber(cc);
		}
	}

	/**
	 * return null when no match found
	 */
	public Object match(String number) {
		return mRootNode.match(number, 0);
	}

	private MatchNode findOrCreateNodeAtPos(String number,int length) {
		MatchNode node = mRootNode;

		if (length == 0)
			return node;

		boolean skip = false;

		for (int i = 0; i < length; i++) {
			MatchNode childNode = node.getChildNode(number.charAt(i));

			if (childNode == null) {
				childNode = new MatchNode();
				node.setChildNode(number.charAt(i), childNode);
			} else if (childNode instanceof ValueNode) {
				if (childNode.getValue() == null) {
					childNode = new MatchNode();
					node.setChildNode(number.charAt(i), childNode);
				} else {
					skip = true;
					break;
				}
			}

			node = childNode;
		}

		if (skip)
			return null;

		return node;
	}
	
	private void buildTopLevelNumber(String number) {
		int length = number.length();

		if (length == 0)
			return;

		MatchNode node = findOrCreateNodeAtPos(number, length);

		if (node == null)
			return;
		
		MatchNode[] nodes = node.getChildrenNodes();
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = mRootNode;
		}
	}
}
