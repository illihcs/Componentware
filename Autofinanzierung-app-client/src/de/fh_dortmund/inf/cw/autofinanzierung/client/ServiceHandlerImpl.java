package de.fh_dortmund.inf.cw.autofinanzierung.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import de.fh_dortmund.inf.cw.autofinanzierung.server.beans.interfaces.FinanzierungsRechnerRemote;
import de.fh_dortmund.inf.cw.car_financing.client.shared.*;

public class ServiceHandlerImpl extends ServiceHandler implements CarFinancingHandler {

	private static ServiceHandlerImpl instance;
	private Context ctx;
	private FinanzierungsRechnerRemote finanzierungsRechner;
	
	
	private ServiceHandlerImpl(){
		try {
			ctx = new InitialContext();
			finanzierungsRechner = (FinanzierungsRechnerRemote) ctx.lookup("java:global/Autofinanzierung-ear/Autofinanzierung-ejb/FinanzierungsRechnerBean!de.fh_dortmund.inf.cw.autofinanzierung.server.beans.interfaces.FinanzierungsRechnerRemote");
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
	
	public static ServiceHandlerImpl getInstance()
	{
		if(instance == null)
			instance = new ServiceHandlerImpl();
		return instance;
	}
	
	
	@Override
	public double computeGrossLoanAmount(double preis, double anzahlung, int monate) {
		return finanzierungsRechner.getDarlehenssumme(preis, anzahlung, monate);
	}

	@Override
	public double computeMonthlyPayment(double preis, double anzahlung, int monate) {
		return finanzierungsRechner.getMonatlicheRaten(preis, anzahlung, monate);
	}

	@Override
	public double computeNetLoanAmount(double preis, double anzahlung) {
		return finanzierungsRechner.getNettoDarlehenssumme(preis, anzahlung);
	}

	@Override
	public double getInterestRate() {
		return finanzierungsRechner.getZinssatz(); 
	}

}
