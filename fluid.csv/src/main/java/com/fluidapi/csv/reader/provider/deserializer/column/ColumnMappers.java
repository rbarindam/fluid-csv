package com.fluidapi.csv.reader.provider.deserializer.column;

import static com.fluidapi.csv.validaton.FailCheck.failIf;

import java.util.stream.Stream;

import com.fluidapi.csv.annotations.CsvStrip;
import com.fluidapi.csv.annotations.CsvTrim;
import com.fluidapi.csv.reader.deserializer.CsvColumnMapper;
import com.fluidapi.csv.reader.provider.bean.AnnotatedInfo;
import com.fluidapi.csv.reader.provider.bean.TypeInfo;
import com.fluidapi.csv.reader.provider.deserializer.column.number.MapNumber;
import com.fluidapi.csv.reader.provider.deserializer.column.primitive.MapPrimitive;
import com.fluidapi.csv.reader.provider.deserializer.column.temporal.MapTemporal;

public class ColumnMappers {
	
	public static CsvColumnMapper<?> of(TypeInfo<?> typeInfo, AnnotatedInfo<?> origin) {
		
		CsvColumnMapper<String> trimming = findTrimming(origin);
		CsvColumnMapper<?> mapper = findSupportOf(typeInfo, origin);
		
		// check if anything default could be found
		failIf(mapper == null,
				"no mapper supported for %s, try using @CsvDeserialzier"
				.formatted(typeInfo.getType()),
				UnsupportedOperationException::new);

		// join prefix mapper with field mapper
		return join(trimming, mapper);
	}

	private static CsvColumnMapper<?> findSupportOf(TypeInfo<?> typeInfo, AnnotatedInfo<?> origin) {
		
		// because we know MapSupport uses Class<?> info as key,
		// let's keep it handy to reduce at least 1 set of redundant call stack
		Class<?> type = typeInfo.getType();
		
		return Stream.of(
			//	support maps in preference order
			//	i.e. first one supporting the type is taken
				MapString.support,
				MapPrimitive.support,
				MapNumber.support,
				MapTemporal.support
		)
		
		// find the supported, use it to get the mapper and return
		.filter( support -> support.supports(type) )
		.map( support -> support.of(typeInfo, origin) )
		.findFirst()
		.orElse(null);
		
	}

	private static CsvColumnMapper<String> findTrimming(AnnotatedInfo<?> origin) {
		if( origin.hasAnnotation(CsvStrip.class) ) return new MapStringStripped();
		if( origin.hasAnnotation(CsvTrim.class) ) return new MapStringTrimmed();
		return null;
	}
	
	private static CsvColumnMapper<?> join(CsvColumnMapper<String> before, CsvColumnMapper<?> after) {
		if( before == null ) return after;
		if( after == null ) return before;
		return (String column) -> after.apply(before.apply(column));
	}
}