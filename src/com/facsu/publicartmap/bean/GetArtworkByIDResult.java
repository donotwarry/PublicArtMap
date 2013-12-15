package com.facsu.publicartmap.bean;

public class GetArtworkByIDResult implements Result {

	public Artwork GetArtworkByIDResult;

	@Override
	public Artwork result() {
		return GetArtworkByIDResult;
	}

}
