package com.fluidapi.csv.reader;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import com.fluidapi.csv.bean.Quote;
import com.fluidapi.csv.exception.CsvFormatException;
import com.fluidapi.csv.reader.deserializer.CsvColumnMapper;
import com.fluidapi.csv.reader.provider.columndecorator.StripColumns;
import com.fluidapi.csv.reader.provider.columndecorator.TrimColumns;
import com.fluidapi.csv.reader.provider.deserializer.PickBigDecimal;
import com.fluidapi.csv.reader.provider.deserializer.PickInteger;
import com.fluidapi.csv.reader.provider.deserializer.PickMapped;
import com.fluidapi.csv.reader.provider.deserializer.PickString;
import com.fluidapi.csv.reader.provider.linesplitter.NoSplit;
import com.fluidapi.csv.reader.provider.linesplitter.SplitByDelimiter;
import com.fluidapi.csv.reader.provider.linesplitter.SplitFixedLengths;
import com.fluidapi.csv.reader.provider.linesplitter.SplitQuoted;

/**
 * Provides shorthand methods to all sorts of functionalities that this utility
 * provides related to reading a csv.
 * 
 * @author Arindam Biswas
 * @since 1.0
 */
public class CsvReader {
	
	// LINE TO COLUMNS MAPPER //

	/**
	 * splits the string using the given regular expression
	 * 
	 * @param regex the regular expression to split the string by
	 * @return {@link CsvLineSplitter} as specified
	 */
	public static CsvLineSplitter delimiter(String regex) {
		return new SplitByDelimiter(regex);
	}

	/**
	 * splits the string using the given parsed regular expression
	 * 
	 * @param pattern compiled regular expression to split the string by
	 * @return {@link CsvLineSplitter} as specified
	 */
	public static CsvLineSplitter delimiter(Pattern pattern) {
		return new SplitByDelimiter(pattern);
	}

	/**
	 * reads through the line to consider text enclosed by the quotes/enclosing pair
	 * of characters as each column. there may or may not be any delimiter between
	 * quoted parts, but they're entirely ignored. Also, any text before the first
	 * quote or after the last quote are also ignored. However, missing quote would
	 * result in {@link CsvFormatException}
	 * 
	 * @param quote character that defines the start as well as end of a quotation,
	 *              cannot be \
	 * @return {@link CsvLineSplitter} as specified
	 * @see #dequote(char, char)
	 * @see #dequote(char, char, char)
	 * @see #dequote(Quote)
	 */
	public static CsvLineSplitter dequote(char quote) {
		return dequote(quote, quote);
	}

	/**
	 * reads through the line to consider text enclosed by the quotes/enclosing pair
	 * of characters as each column. there may or may not be any delimiter between
	 * quoted parts, but they're entirely ignored. Also, any text before the first
	 * quote or after the last quote are also ignored. However, missing quote would
	 * result in {@link CsvFormatException}
	 * 
	 * @param quoteStart character that defines the start of a quotation, cannot be \
	 * @param quoteEnd   character that defines the end of a quotation, cannot be \
	 * @return {@link CsvLineSplitter} as specified
	 * @see #dequote(char)
	 * @see #dequote(char, char, char)
	 * @see #dequote(Quote)
	 */
	public static CsvLineSplitter dequote(char quoteStart, char quoteEnd) {
		return dequote(quoteStart, quoteEnd, '\\');
	}

	/**
	 * reads through the line to consider text enclosed by the quotes/enclosing pair
	 * of characters as each column. there may or may not be any delimiter between
	 * quoted parts, but they're entirely ignored. Also, any text before the first
	 * quote or after the last quote are also ignored. However, missing quote would
	 * result in {@link CsvFormatException}
	 * 
	 * @param quoteStart      character that defines the start of a quotation, must
	 *                        not be same as {@code escapeIndicator}
	 * @param quoteEnd        character that defines the end of a quotation, must
	 *                        not be same as {@code escapeIndicator}
	 * @param escapeIndicator character that defines which character would mark a
	 *                        quote as a simple content character, must not be same
	 *                        as {@code quoteStart} or {@code quoteEnd}, e.g. \
	 * @return {@link CsvLineSplitter} as specified
	 * @see #dequote(char)
	 * @see #dequote(char, char)
	 * @see #dequote(Quote)
	 */
	public static CsvLineSplitter dequote(char quoteStart, char quoteEnd, char escapeIndicator) {
		return dequote(new Quote(quoteStart, quoteEnd, escapeIndicator));
	}

	/**
	 * reads through the line to consider text enclosed by the quotes/enclosing pair
	 * of characters as each column. there may or may not be any delimiter between
	 * quoted parts, but they're entirely ignored. Also, any text before the first
	 * quote or after the last quote are also ignored. However, missing quote would
	 * result in {@link CsvFormatException}
	 * 
	 * @param quote information regarding quotation
	 * @return {@link CsvLineSplitter} as specified
	 * @see #dequote(char)
	 * @see #dequote(char, char)
	 * @see #dequote(char, char, char)
	 */
	public static CsvLineSplitter dequote(Quote quote) {
		return new SplitQuoted(quote);
	}
	
	/**
	 * Useful to read fixed length Csv files, where each column has a fixed width.
	 * <p>
	 * This, however, does not deal with the spaces used to form the fixed width for
	 * variable contents since it assumes the spaces maybe relevant piece of
	 * information.<br/>
	 * In order to remove the spaces, use {@link #strip()} or {@link #trim()}
	 * </p>
	 * 
	 * @param lengths zero or positive valued character lengths in ltr sequence to
	 *                specify per column width. zero lengths can be utilized to
	 *                adjust column index in configuration, although discouraged
	 *                from using unless bean index configuration is really rigid
	 * @return {@link CsvLineSplitter} as specified
	 */
	public static CsvLineSplitter fixed(int...lengths) {
		return new SplitFixedLengths(lengths);
	}
	
	/**
	 * Considers as in the Csv doesn't really have any split specifiers since it
	 * contains only one column. Hence, the whole line is read as a single column.
	 * Useful when you still need some transformation regarding that column, and
	 * that the provided mapping providers would come in handy.
	 * 
	 * @return {@link CsvLineSplitter} as specified
	 */
	public static CsvLineSplitter line() {
		return new NoSplit();
	}
	
	// COLUMN DECORATOR //

	/**
	 * Removes all blank spaces from all of the columns individually using
	 * {@link String#strip()}.
	 * 
	 * @return {@link CsvColumnDecorator} as specified
	 */
	public static CsvColumnDecorator strip() {
		return new StripColumns();
	}
	
	/**
	 * Removes all blank spaces from all of the columns individually using
	 * {@link String#trim()}.
	 * 
	 * @return {@link CsvColumnDecorator} as specified
	 */
	public static CsvColumnDecorator trim() {
		return new TrimColumns();
	}

	/**
	 * from each column, remove the escape character.
	 * <p>
	 * Useful when you have {@code "Using \" As Quote"}, which would translate to as
	 * {@code Using \" As Quote}, but what you really need is
	 * {@code Using " As Quote}
	 * </p>
	 * 
	 * @return {@link CsvColumnDecorator} as specified
	 */
	public static CsvColumnDecorator unescape() {
		return null;
	}
	
	/**
	 * from each column, remove the escape character.
	 * <p>
	 * Useful when you have {@code "Using \" As Quote"}, which would translate to as
	 * {@code Using \" As Quote}, but what you really need is
	 * {@code Using " As Quote}
	 * </p>
	 * 
	 * <style>
	 * code {
	 * 	background: rgba(100, 100, 0, .3);
	 * 	padding: 0 3px;
	 * 	border-radius: 3px;
	 * }
	 * </style>
	 * 
	 * @param escapeIndicator usually {@code backslash \} but you get to choose if
	 *                        it's anything else
	 * @return {@link CsvColumnDecorator} as specified
	 */
	public static CsvColumnDecorator unescape(char escapeIndicator) {
		return null;
	}
	
	// COLUMN TO BEAN MAPPER //

	/**
	 * Pick first column and convert it using given {@link CsvColumnMapper}
	 * 
	 * @param <T>         any bean type
	 * @param withMapping map string column to desired bean, should be capable of
	 *                    handling {@code null}
	 * @return {@link CsvBeanDeserializer} as specified
	 */
	public static <T> CsvBeanDeserializer<T> pick(CsvColumnMapper<T> withMapping) {
		return pick(0, withMapping);
	}
	
	/**
	 * Pick first column at said index and convert it using given
	 * {@link CsvColumnMapper}
	 * 
	 * @param <T>         any bean type
	 * @param index       0-based index of the column, would supply {@code null} if
	 *                    index exceeds length of column
	 * @param withMapping map string column to desired bean, should be capable of
	 *                    handling {@code null}
	 * @return {@link CsvBeanDeserializer} as specified
	 */
	public static <T> CsvBeanDeserializer<T> pick(int index, CsvColumnMapper<T> withMapping) {
		return new PickMapped<>(index, withMapping);
	}

	/**
	 * Picks first column as {@link String}
	 * 
	 * @return {@link CsvBeanDeserializer} as specified
	 */
	public static CsvBeanDeserializer<String> string() {
		return string(0);
	}

	/**
	 * Picks first column as {@link String}
	 * 
	 * @param index 0-based index of the column, would supply {@code null} if index
	 *              exceeds length of column
	 * @return {@link CsvBeanDeserializer} as specified
	 */
	public static CsvBeanDeserializer<String> string(int index) {
		return new PickString(index);
	}

	/**
	 * Picks first column as {@link Integer}
	 * 
	 * @return {@link CsvBeanDeserializer} as specified
	 */
	public static CsvBeanDeserializer<Integer> integer() {
		return integer(0);
	}

	/**
	 * Picks first column as {@link Integer}
	 * 
	 * @param index 0-based index of the column, would supply {@code null} if index
	 *              exceeds length of column
	 * @return {@link CsvBeanDeserializer} as specified
	 */
	public static CsvBeanDeserializer<Integer> integer(int index) {
		return new PickInteger(index);
	}

	/**
	 * Picks first column as {@link BigDecimal}
	 * 
	 * @return {@link CsvBeanDeserializer} as specified
	 */
	public static CsvBeanDeserializer<BigDecimal> bigDecimal() {
		return bigDecimal(0);
	}

	/**
	 * Picks first column as {@link BigDecimal}
	 * 
	 * @param index 0-based index of the column, would supply {@code null} if index
	 *              exceeds length of column
	 * @return {@link CsvBeanDeserializer} as specified
	 */
	public static CsvBeanDeserializer<BigDecimal> bigDecimal(int index) {
		return new PickBigDecimal(index);
	}
	
	/**
	 * Translates the provided columns to said bean type using specified
	 * configuration in the bean
	 * 
	 * @param <T>  any bean type
	 * @param type {@link Class} type of the given bean
	 * @return {@link CsvBeanDeserializer} as specified
	 */
	public static <T> CsvBeanDeserializer<T> auto(Class<T> type) {
		return null;
	}
	
}
