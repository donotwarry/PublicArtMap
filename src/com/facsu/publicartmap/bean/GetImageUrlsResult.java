package com.facsu.publicartmap.bean;

public class GetImageUrlsResult implements Result {

	public ArtworkImage[] GetImageUrlsResult;

	@Override
	public ArtworkImage[] result() {
		return GetImageUrlsResult;
	}

}
