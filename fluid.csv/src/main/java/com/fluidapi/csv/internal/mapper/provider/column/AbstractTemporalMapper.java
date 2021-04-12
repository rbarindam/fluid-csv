package com.fluidapi.csv.internal.mapper.provider.column;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.fluidapi.csv.config.CsvFormat;
import com.fluidapi.csv.internal.mapper.provider.beans.MemberInfo;

public abstract class AbstractTemporalMapper extends AbstractColumnMapper {
	
	protected final DateTimeFormatter formatter;
	
	protected AbstractTemporalMapper(MemberInfo<?> memberInfo) {
		super(memberInfo);
		
		formatter = findFormat()
				.map(DateTimeFormatter::ofPattern)
				.orElse(defaultFormatter());
	}

	private Optional<String> findFormat() {
		return Optional.of(memberInfo)
				.filter(info -> info.hasAnnotation(CsvFormat.class))
				.map(info -> info.findAnnotation(CsvFormat.class))
				.map(CsvFormat::value);
	}

	protected abstract DateTimeFormatter defaultFormatter();
	
}