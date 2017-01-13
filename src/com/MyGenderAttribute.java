package com;

import org.apache.lucene.util.Attribute;

public interface MyGenderAttribute extends Attribute {

	public static enum Gender { MALE, FEMALE, UNSPECIFIED };

	public void setGender(Gender gender);
	
	public Gender getGender();

}
