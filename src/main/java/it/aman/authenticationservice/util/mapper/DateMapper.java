package it.aman.authenticationservice.util.mapper;

import org.springframework.stereotype.Component;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetDateTime;

@Component
public class DateMapper {

    public java.time.LocalDate toJavaTimeLocalDate(LocalDate value) {
        return java.time.LocalDate.parse(value.toString());
    }
    
    public java.time.OffsetDateTime toJavaTimeOffsetDateTime(OffsetDateTime value) {
        return java.time.OffsetDateTime.parse(value.toString());
    }
    
    public LocalDate toThreetenLocalDate(java.time.LocalDate value) {
        return LocalDate.parse(value.toString());
    }
    
    public OffsetDateTime toThreetenOffsetDateTime(java.time.OffsetDateTime value) {
        return OffsetDateTime.parse(value.toString());
    }
}
