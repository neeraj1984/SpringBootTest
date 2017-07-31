package com.lh.dandi.coe.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.lh.dandi.coe.bean.MemberDataFileBean;

public class MemberDataFieldSetMapper implements FieldSetMapper<MemberDataFileBean>{
	
	public final static Logger logger = LoggerFactory.getLogger(MemberDataFieldSetMapper.class.getName());	

	public MemberDataFileBean mapFieldSet(FieldSet fieldSet) throws BindException {
		
		if(fieldSet == null){
			logger.info("Got null value into field set ");
			return null;
		}
		
		MemberDataFileBean memberData = new MemberDataFileBean();
		
		//int memberId = new Integer(fieldSet.readString(0).replace("\"", ""));		
		memberData.setMemberId(fieldSet.readString(0).replace("\"", ""));
		memberData.setMemberFirstName(fieldSet.readString(1).replace("\"", ""));
		memberData.setMemberLastName(fieldSet.readString(2).replace("\"", ""));
		memberData.setDob(fieldSet.readString(3).replace("\"", ""));
		memberData.setGender(fieldSet.readString(4).replace("\"", ""));
		memberData.setAddress(fieldSet.readString(5).replace("\"", ""));
		memberData.setCity(fieldSet.readString(6).replace("\"", ""));
		memberData.setState(fieldSet.readString(7).replace("\"", ""));
		memberData.setPostalCode(fieldSet.readString(8).replace("\"", ""));
		memberData.setPhoneNumber(fieldSet.readString(9).replace("\"", ""));
		
		memberData.setField1(fieldSet.readString(10).replace("\"", ""));
		memberData.setField2(fieldSet.readString(11).replace("\"", ""));
		memberData.setField3(fieldSet.readString(12).replace("\"", ""));
		//memberData.setField4(fieldSet.readString(13).replace("\"", ""));
		return memberData;
		
	}

}
