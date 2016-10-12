package de.fh_dortmund.inf.cw.autofinanzierung.server.beans.interfaces;

public interface FinanzierungsRechner {
	
	public double getZinssatz();
	public double getNettoDarlehenssumme(double preis, double anzahlung);
	public double getDarlehenssumme(double preis, double anzahlung, int monate);
	public double getMonatlicheRaten(double preis, double anzahlung, int monate);
	 
}
