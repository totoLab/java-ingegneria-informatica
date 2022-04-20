package centrosportivo;
import java.util.*;

public class Tesserato {

	private String cf;
	private String nome, cognome;
	private int dataNascita;
	private int codiceTessera;
	private int dataScadenza;
	private ArrayList<Abbonamento> listaAbbonamenti;

	public Tesserato(String cf, String nome, String cognome, int dataNascita, int codiceTessera, int dataScadenza, ArrayList<Abbonamento> listaAbbonamenti) {
		this.cf = cf;
		this.nome = nome;
		this.cognome = cognome;
		this.dataNascita = dataNascita;
		this.codiceTessera = codiceTessera;
		this.dataScadenza = dataScadenza;
		for (Abbonamento a : listaAbbonamenti) {
			this.listaAbbonamenti.add(new Abbonamento(a.getCodiceServizio(), a.getMesiSettimane()));
		}
	}
	
	public String getCf() {
		return this.cf;
	} 
	
	public String getNome() {
		return this.nome;
	}
	
	public String getCognome() {
		return this.cognome;
	}
	
	public int getDataNascita() {
		return this.dataNascita;
	}
	
	public int getCodiceTessera() {
		return this.codiceTessera;
	}
	
	public int getDataScadenza() {
		return this.dataScadenza;
	}
	
	public ArrayList<Abbonamento> getListaAbbonamenti() {
		return this.listaAbbonamenti;
	}
}
