/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * SimpleStringTokenizer.java
 * Copyright (C) 2007-2012 University of Waikato, Hamilton, New Zealand
 * 
 * created by Peiyang Liu, 18/07/2019
 */

package weka.core.tokenizers;

import java.util.StringTokenizer;

import weka.core.RevisionUtils;
import java.util.ArrayList;
import org.ansj.*;

/**
 * <!-- globalinfo-start --> A simple tokenizer that is using the
 * java.util.StringTokenizer class to tokenize the strings.
 * <p/>
 * <!-- globalinfo-end -->
 * 
 * <!-- options-start --> Valid options are:
 * <p/>
 * 
 * <pre>
 * -delimiters &lt;value&gt;
 *  The delimiters to use
 *  (default ' \r\n\t.,;:'"()?!').
 * </pre>
 * 
 * <!-- options-end -->
 * 
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 10203 $
 */
public class ChineseWordTokenizer extends CharacterDelimitedTokenizer {

  /** for serialization */
  private static final long serialVersionUID = -930893034037880773L;

  /** the actual tokenizer */
  protected transient StringTokenizer m_Tokenizer;

  /**
   * Returns a string describing the stemmer
   * 
   * @return a description suitable for displaying in the explorer/experimenter
   *         gui
   */
  @Override
  public String globalInfo() {
    return "A simple tokenizer that is using the java.util.StringTokenizer "
      + "class to tokenize the strings.";
  }
  
  private static final boolean isChinese(char c) {   
	    Character.UnicodeBlock ub = Character.UnicodeBlock.of(c); 
	    if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS  
	            || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS  
	            || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A  
	            || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION  
	            || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION  
	            || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {  
	        return true;  
	    }  
	    return false;  
	} 
  
  //create a new Array to contain the string received from m_Tokenizer.nextToken() and save as the single
  //characters. Abstract each single char until Array is empty and extend array again.
  
  //change 18/07/2019
  private ArrayList<String> m_buffer = new ArrayList<String>();
  private int m_used = 0;
  
  /**
   * Tests if this enumeration contains more elements.
   * 
   * @return true if and only if this enumeration object contains at least one
   *         more element to provide; false otherwise.
   */
  @Override
  public boolean hasMoreElements() {
	  return(m_Tokenizer.hasMoreElements() || m_used != m_buffer.size());
  }
  
  /**
   * Returns the next element of this enumeration if this enumeration object has
   * at least one more element to provide.
   * 
   * @return the next element of this enumeration.
   */
  @Override
  public String nextElement() {
	  String m_eng_buffer = "";
	  
	  if (m_used == m_buffer.size()) {
		  String next = m_Tokenizer.nextToken();
		  //System.out.println(next);
		  char[] str = next.toCharArray();
		  int start = 0;		  
		  if (next == null || next.length() < 1)
			  return next;		  
		  while (start < next.length()) {		  
			  if (isChinese(str[start])) {
				  if(!m_eng_buffer.equals("")) 
					  //some English string had been pushed. move it to the m_buffer first
				  {
					  m_buffer.add(m_eng_buffer);
				  	  m_eng_buffer = ""; //set it null after pushing string to m_buffer
				  }
				  //push the element to the m_buffer when it is Chinese character
				  m_buffer.add(next.substring(start, start+1));				  
				  start++;
			  } // if
			  else {
				  //if it's not Chinese character, push it to the m_eng_buffer
				  m_eng_buffer += next.substring(start, start+1);
				  start++;  
			  }				  
		  } // while
	  //after finish all scanning job for string, 
	  //push the element to m_buffer from m_eng_buffer at last
	  if (!m_eng_buffer.equals(""))
		  m_buffer.add(m_eng_buffer);
	  
	  }// if
	  return m_buffer.get(m_used++);
    // return m_Tokenizer.nextToken();
  }

  /**
   * Sets the string to tokenize. Tokenization happens immediately.
   * 
   * @param s the string to tokenize
   */
  @Override
  public void tokenize(String s) {
    m_Tokenizer = new StringTokenizer(s, getDelimiters());
  }

  /**
   * Returns the revision string.
   * 
   * @return the revision
   */
  @Override
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 10203 $");
  }

  /**
   * Runs the tokenizer with the given options and strings to tokenize. The
   * tokens are printed to stdout.
   * 
   * @param args the commandline options and strings to tokenize
   */
  public static void main(String[] args) {
    runTokenizer(new WordTokenizer(), args);
  }
}
