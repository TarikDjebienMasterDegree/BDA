package maison;	
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * 
 * @author tarik
 * programme java qui, à partir du fichier maision.XML, affiche,
 * pour chaque maison du fichier, les pieces qui la compose sur la sortie standard.
 */

public class Pieces extends DefaultHandler {


	private static int profondeurCourante;
	private Set<String> lesPieces;
	/**
	 * Evenement envoye au demarrage du parse du flux xml.
	 * @throws SAXException en cas de probleme quelquonque ne permettant pas de
	 * se lancer dans l'analyse du document.
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	public void startDocument() throws SAXException {
		System.out.println("Affichage des pièces de chaques maisons\n Chargement du document . . .\n");
		Pieces.profondeurCourante=0;
		this.lesPieces = new TreeSet<String>();
	}

	/**
	 * Evenement envoye a la fin de l'analyse du flux xml.
	 * @throws SAXException en cas de probleme quelquonque ne permettant pas de
	 * considerer l'analyse du document comme etant complete.
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	public void endDocument() throws SAXException {
		List<String> l = new ArrayList<String>();
		
		for(String s : this.lesPieces)
			l.add(s);
		Collections.sort(l);
		for(String s : l) System.out.println(s);
	}

	/**
	 * Evenement recu a chaque fois que l'analyseur rencontre une balise xml ouvrante.
	 * @param nameSpaceURI l'url de l'espace de nommage.
	 * @param localName le nom local de la balise.
	 * @param rawName nom de la balise en version 1.0 <code>nameSpaceURI + ":" + localName</code>
	 * @throws SAXException si la balise ne correspond pas a ce qui est attendu,
	 * comme par exemple non respect d'une dtd.
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String nameSpaceURI, String localName, String rawName, Attributes attributs) throws SAXException {
		Pieces.profondeurCourante++;
		if (Pieces.profondeurCourante > 3) 
			this.lesPieces.add(localName);		
	}

	/**
	 * Evenement recu a chaque fermeture de balise.
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String nameSpaceURI, String localName, String rawName) throws SAXException {
		Pieces.profondeurCourante--;
	}

	/**
	 * Evenement recu a chaque fois que l'analyseur rencontre des caracteres (entre
	 * deux balises).
	 * @param ch les caracteres proprement dits.
	 * @param start le rang du premier caractere a traiter effectivement.
	 * @param end le nombre de caracteres a traiter effectivement
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int end) throws SAXException {

	}

	/**
	 * Recu chaque fois que des caracteres d'espacement peuvent etre ignores au sens de
	 * XML. C'est a dire que cet evenement est envoye pour plusieurs espaces se succedant,
	 * les tabulations, et les retours chariot se succedants ainsi que toute combinaison de ces
	 * trois types d'occurrence.
	 * @param ch les caracteres proprement dits.
	 * @param start le rang du premier caractere a traiter effectivement.
	 * @param end le rang du dernier caractere a traiter effectivement
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
	 */
	public void ignorableWhitespace(char[] ch, int start, int end) throws SAXException {
		System.out.println("espaces inutiles rencontres : ..." + new String(ch, start, end) +  "...");
	}

	/**
	 * Rencontre une instruction de traitement.
	 * @param target la cible de l'instruction de traitement.
	 * @param data les valeurs associees a cette cible. En general, elle se presente sous la forme 
	 * d'une serie de paires nom/valeur.
	 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
	 */
	public void processingInstruction(String target, String data) throws SAXException {
		System.out.println("Instruction de traitement : " + target);
		System.out.println("  dont les arguments sont : " + data);
	}


	public static void main(String[] args) {
		try {
			XMLReader saxReader = XMLReaderFactory.createXMLReader();
			saxReader.setContentHandler(new Pieces());
			saxReader.parse(args[0]);
		} catch (Exception t) {
			t.printStackTrace();
		}
	}

}



