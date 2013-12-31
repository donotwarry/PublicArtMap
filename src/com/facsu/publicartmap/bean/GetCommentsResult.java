package com.facsu.publicartmap.bean;

public class GetCommentsResult implements Result {
	
	public UserComment[] GetCommentsResult;

	@Override
	public UserComment[] result() {
		return GetCommentsResult;
	}

}
