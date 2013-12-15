package com.facsu.publicartmap.bean;

public class GetArtworksByGPSResult implements Result {

	public Artwork[] GetArtworksByGPSResult;

	@Override
	public Artwork[] result() {
		return GetArtworksByGPSResult;
	}
}
