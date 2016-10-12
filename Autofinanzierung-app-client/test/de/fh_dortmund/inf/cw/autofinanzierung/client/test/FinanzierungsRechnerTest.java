package de.fh_dortmund.inf.cw.autofinanzierung.client.test;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fh_dortmund.inf.cw.autofinanzierung.client.ServiceHandlerImpl;

public class FinanzierungsRechnerTest {

	private static ServiceHandlerImpl serviceHandler;
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		serviceHandler = ServiceHandlerImpl.getInstance();
	}

	@Test
	public void test_InterestRate(){
		assertEquals(1 , serviceHandler.getInterestRate(), 0.01);
	}
	
	@Test
	public void testNettoDarlehenssumme(){
		assertEquals(12000, serviceHandler.computeNetLoanAmount(14500, 2500),0.01);
	}
	
	@Test
	public void testComputeGrossLoanAmount(){
		assertEquals(12065, serviceHandler.computeGrossLoanAmount(12000, 0, 12), 0.01);		
	}
	
	@Test
	public void testComputeMonthlyPayment(){
		assertEquals(1005.42, serviceHandler.computeMonthlyPayment(12000, 0, 12), 0.01);
	}	
}
