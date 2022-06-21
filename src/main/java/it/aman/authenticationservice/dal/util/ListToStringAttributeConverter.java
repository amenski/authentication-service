package it.aman.authenticationservice.dal.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.AttributeConverter;

import org.apache.commons.lang3.StringUtils;

public class ListToStringAttributeConverter implements AttributeConverter<List<String>, String> {

    private static final String ATTRIBUTE_SEPARATOR = "#";
    
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if(attribute == null || attribute.isEmpty()) return StringUtils.EMPTY;
        
        StringBuilder sb = new StringBuilder();
        attribute.stream().forEach(val -> sb.append(val).append(ATTRIBUTE_SEPARATOR));
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if(StringUtils.isBlank(dbData)) return new ArrayList<>(0);
        
        return Arrays.asList(dbData.split(ATTRIBUTE_SEPARATOR));
    }

}
