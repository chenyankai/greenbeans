package greensopinion.finance.services.transaction;

import static java.text.MessageFormat.format;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;

import greensopinion.finance.services.domain.Transaction;
import greensopinion.finance.services.transaction.SgmlReader.Token;
import greensopinion.finance.services.transaction.SgmlReader.TokenType;

public class OfxTransactionReader implements Closeable {

	private static final String NAME = "NAME";
	private static final String TRNAMT = "TRNAMT";
	private static final String DTPOSTED = "DTPOSTED";
	private static final String STMTTRN = "STMTTRN";
	private static final String ACCTID = "ACCTID";
	private static final String TAG_BANKACCTFROM = "BANKACCTFROM";
	private static final Object TAG_CCACCTFROM = "CCACCTFROM";

	private static final String CHARSET_NAME_CP1252 = "Cp1252";
	private final BufferedReader reader;

	public OfxTransactionReader(InputStream inputStream) {
		this.reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName(CHARSET_NAME_CP1252)));
	}

	public List<Transaction> transactions() {
		List<Transaction> transactions = new ArrayList<>();
		try {
			readHeader();

			try (SgmlReader sgmlReader = new SgmlReader(reader)) {
				String accountNumber = null;
				Token token;
				while ((token = sgmlReader.readToken()) != null) {
					if (token.getType() == TokenType.OPEN_TAG) {
						if (TAG_BANKACCTFROM.equals(token.getValue()) || TAG_CCACCTFROM.equals(token.getValue())) {
							accountNumber = readAccountNumber(sgmlReader, token.getValue());
						} else if (STMTTRN.equals(token.getValue())) {
							transactions.add(readTransaction(sgmlReader, token.getValue(), accountNumber));
						}
					}
				}
			}
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
		return FluentIterable.from(transactions).filter(Predicates.notNull()).toList();
	}

	private Transaction readTransaction(SgmlReader sgmlReader, String tagName, String accountNumber)
			throws IOException {
		// <TRNTYPE>DEBIT
		// <DTPOSTED>20150914020000[-5:EST]
		// <TRNAMT>-42.54
		// <FITID>02015091400000000000003000
		// <NAME>HOMESENSE 084 SURREY
		Date date = null;
		Long amount = null;
		String name = null;

		Token previousToken = null;
		Token token;
		while ((token = sgmlReader.readToken()) != null) {
			if (isCloseTag(token, tagName)) {
				if (date != null && amount != null && name != null) {
					return new Transaction(date, name, amount, null, accountNumber);
				}
				return null;
			}
			if (token.getType() == TokenType.DATA) {
				if (previousToken != null && previousToken.getType() == TokenType.OPEN_TAG) {
					String previousTagName = previousToken.getValue();
					if (DTPOSTED.equals(previousTagName)) {
						date = readTransactionDate(token.getValue());
					} else if (TRNAMT.equals(previousTagName)) {
						amount = readTransactionAmount(token.getValue());
					} else if (NAME.equals(previousTagName)) {
						name = token.getValue();
					}
				}
			}
			previousToken = token;
		}
		return null;
	}

	private Long readTransactionAmount(String value) {
		try {
			float floatValue = Float.parseFloat(value);
			return (long) (floatValue * 100f);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Date readTransactionDate(String value) {
		if (value.length() >= 8) {
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
				return dateFormat.parse(value.substring(0, 8));
			} catch (ParseException e) {
				return null;
			}
		}
		return null;
	}

	private String readAccountNumber(SgmlReader sgmlReader, String tagName) throws IOException {
		String acctId = null;
		String bankId = null;
		Token previousToken = null;
		Token token;
		while ((token = sgmlReader.readToken()) != null) {
			if (isCloseTag(token, tagName)) {
				if (acctId != null && bankId != null) {
					return bankId + acctId;
				}
				return acctId;
			}
			if (token.getType() == TokenType.DATA) {
				if (previousToken != null && previousToken.getType() == TokenType.OPEN_TAG) {
					String previousTagName = previousToken.getValue();
					if (ACCTID.equals(previousTagName)) {
						acctId = token.getValue();
					} else if ("BANKID".equals(previousTagName)) {
						bankId = token.getValue();
					}
				}
			}
			previousToken = token;
		}
		return null;
	}

	private boolean isCloseTag(Token token, String tagName) {
		return token.getType() == TokenType.CLOSE_TAG && token.getValue().equals(tagName);
	}

	private void readHeader() throws IOException {
		// OFXHEADER:100
		// DATA:OFXSGML
		// VERSION:102
		// SECURITY:TYPE1
		// ENCODING:USASCII
		// CHARSET:1252
		// COMPRESSION:NONE
		// OLDFILEUID:NONE
		// NEWFILEUID:NONE
		checkValidFormat("OFXHEADER:100", reader.readLine());
		checkValidFormat("DATA:OFXSGML", reader.readLine());
		String line;
		do {
			line = reader.readLine();
		} while (line != null && !line.trim().isEmpty());
	}

	private void checkValidFormat(String expected, String line) {
		if (!line.equals(expected)) {
			throw new InvalidFileFormatException(
					format("Invalid file format: expected {0} but got {1}", expected, line));
		}
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}
}
