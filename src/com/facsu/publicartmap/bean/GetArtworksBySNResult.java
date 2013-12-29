package com.facsu.publicartmap.bean;

public class GetArtworksBySNResult implements Result{

	public Artwork[] GetArtworksBySNResult;

	@Override
	public Artwork[] result() {
		return GetArtworksBySNResult;
	}
	
}
