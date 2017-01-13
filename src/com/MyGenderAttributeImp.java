package com;

import org.apache.lucene.util.AttributeImpl;

public class MyGenderAttributeImp extends AttributeImpl implements MyGenderAttribute {

	private Gender gender = Gender.UNSPECIFIED;
	
	@Override
	public void setGender(Gender gender) {
		this.gender = gender;
	}

	@Override
	public Gender getGender() {
		return this.gender;
	}

	@Override
	public void clear() {
		this.gender = Gender.UNSPECIFIED;
	}

	@Override
	public void copyTo(AttributeImpl target) {
		((MyGenderAttribute)target).setGender(this.gender);
	}

}
