package com.grainger.openshift.isocode;

import static org.junit.Assert.*;

import org.junit.Test;

public class IsoCodeTest {

	@Test
	public void testNormalizeWithAnd() {
		assertEquals(IsoCode.ANTIGUA_AND_BARBUDA.name(),  IsoCode.normalize("Antigua & Barbuda"));
	}
	
	
	@Test
	public void testNormalizeWithSpace() {
		assertEquals(IsoCode.UNITED_KINGDOM.name(),  IsoCode.normalize("United Kingdom"));
	}
	
	
	@Test
	public void testNormalizeWithDot() {
		assertEquals(IsoCode.ST_HELENA.name(),  IsoCode.normalize("St. Helena"));
	}

}
