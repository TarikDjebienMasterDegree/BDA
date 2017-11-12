import java.io.*;

/**
 *  classe Lecture : permet de lire au clavier
 *
 *@author     caronc
 */
public class Lecture {
  private BufferedReader in
       = new BufferedReader(new InputStreamReader(System.in));


  /**
   *  lecture d'un caractere
   *
   *@return                          le caractere lu
   *@exception  java.io.IOException  si probleme de lecture
   */
  public char nextChar() throws java.io.IOException {
    return (char) in.read();
  }


  /**
   *  lecture d'une ligne
   *
   *@return                          la chaine de caracteres lue
   *@exception  java.io.IOException  si probleme de lecture
   */
  public String nextLine() throws java.io.IOException {
    String s = "";
    char c = this.nextChar();
    while (c != '\n') {
      s = s + c;
      c = this.nextChar();
    }
    return s;
  }


  /**
   *  lecture d'un entier
   *
   *@return                            l'entier lu
   *@exception  java.io.IOException    si pb de lecture
   *@exception  NumberFormatException  si la ligne lue n'est pas un entier
   */
  public int nextInt() throws java.io.IOException, NumberFormatException {
    Integer reponse = new Integer(this.nextLine());
    return reponse.intValue();
  }
  
    /**
     *  lecture d'un nombre a virgule
     *
     *@return                            l'entier lu
     *@exception  java.io.IOException    si pb de lecture
     *@exception  NumberFormatException  si la ligne lue n'est pas un double
     */
    public double nextDouble() throws java.io.IOException, NumberFormatException {
      Double reponse = new Double(this.nextLine());
      return reponse.doubleValue();
    }
}

