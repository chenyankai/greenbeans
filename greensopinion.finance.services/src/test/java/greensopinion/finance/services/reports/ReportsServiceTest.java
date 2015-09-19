package greensopinion.finance.services.reports;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Throwables;

import greensopinion.finance.services.data.ConfigurationService;
import greensopinion.finance.services.data.Transactions;
import greensopinion.finance.services.model.IncomeVersusExpensesReport;
import greensopinion.finance.services.model.IncomeVersusExpensesReport.Month;
import greensopinion.finance.services.transaction.Transaction;

public class ReportsServiceTest {

	private final ConfigurationService configurationService = mock(ConfigurationService.class);
	private final ReportsService service = new ReportsService(configurationService);
	private final Transactions transactions = createTransactions();

	@Before
	public void before() {
		doReturn(transactions).when(configurationService).getTransactions();
	}

	@Test
	public void incomeVersusExpenses() {
		IncomeVersusExpensesReport report = service.incomeVersusExpenses();
		List<Month> months = report.getMonths();
		assertNotNull(months);
		assertEquals(2, months.size());
		assertMonth("January 2015", 102300, 3004, months.get(0));
		assertMonth("February 2015", 0, 12345, months.get(1));
	}

	private void assertMonth(String monthName, long incomeTotal, long expensesTotal, Month month) {
		assertEquals(monthName, month.getName());
		assertEquals(incomeTotal, month.getIncomeTotal());
		assertEquals(expensesTotal, month.getExpensesTotal());
	}

	private Transactions createTransactions() {
		List<Transaction> transactions = new ArrayList<>();
		transactions.add(new Transaction(date("2015-01-03"), "test1", 102300));
		transactions.add(new Transaction(date("2015-01-03"), "test2", -1500));
		transactions.add(new Transaction(date("2015-01-05"), "test3", -1504));
		transactions.add(new Transaction(date("2015-02-15"), "test3", -12345));
		return new Transactions(transactions);
	}

	private Date date(String date) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return dateFormat.parse(date);
		} catch (ParseException e) {
			throw Throwables.propagate(e);
		}
	}
}
