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
import java.util.Vector;

import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

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
public class NGramChineseTokenizer extends CharacterDelimitedTokenizer {

  /** for serialization */
  private static final long serialVersionUID = -930893034037880773L;

  /** the actual tokenizer */
  protected transient StringTokenizer m_Tokenizer;
  
  /** the maximum number of N */
  protected int m_NMax = 3;

  /** the minimum number of N */
  protected int m_NMin = 1;

  /** the current length of the N-grams */
  protected int m_N;

  /** the number of strings available */
  protected int m_MaxPosition;

  /** the current position for returning elements */
  protected int m_CurrentPosition;

  /** all the available grams */
  protected String[] m_SplitString;

  /**
   * Returns a string describing the stemmer
   * 
   * @return a description suitable for displaying in the explorer/experimenter
   *         gui
   */
  
  @Override
  public String globalInfo() {
    return "Splits a Chinese string into an n-gram with min and max grams.";
  }

  /**
   * Returns an enumeration of all the available options..
   * 
   * @return an enumeration of all available options.
   */
  @Override
  public Enumeration<Option> listOptions() {
    Vector<Option> result = new Vector<Option>();

    result.addElement(new Option("\tThe max size of the Ngram (default = 3).",
      "max", 1, "-max <int>"));

    result.addElement(new Option("\tThe min size of the Ngram (default = 1).",
      "min", 1, "-min <int>"));

    result.addAll(Collections.list(super.listOptions()));

    return result.elements();
  }
  
  /**
   * Gets the max N of the NGram.
   * 
   * @return the size (N) of the NGram.
   */
  public int getNGramMaxSize() {
    return m_NMax;
  }

  /**
   * Sets the max size of the Ngram.
   * 
   * @param value the size of the NGram.
   */
  public void setNGramMaxSize(int value) {
    if (value < 1) {
      m_NMax = 1;
    } else {
      m_NMax = value;
    }
  }
  
  /**
   * Returns the tip text for this property.
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String NGramMaxSizeTipText() {
    return "The max N of the NGram.";
  }

  /**
   * Sets the min size of the Ngram.
   * 
   * @param value the size of the NGram.
   */
  public void setNGramMinSize(int value) {
    if (value < 1) {
      m_NMin = 1;
    } else {
      m_NMin = value;
    }
  }

  /**
   * Gets the min N of the NGram.
   * 
   * @return the size (N) of the NGram.
   */
  public int getNGramMinSize() {
    return m_NMin;
  }

  public String NGramMinSizeTipText() {
	    return "The min N of the NGram.";
	  }

  /**
   * Gets the current option settings for the OptionHandler.
   * 
   * @return the list of current option settings as an array of strings
   */
  @Override
  public String[] getOptions() {
    Vector<String> result = new Vector<String>();

    result.add("-max");
    result.add("" + getNGramMaxSize());

    result.add("-min");
    result.add("" + getNGramMinSize());

    Collections.addAll(result, super.getOptions());

    return result.toArray(new String[result.size()]);
  }
  
  @Override
  public void setOptions(String[] options) throws Exception {
    String value;

    value = Utils.getOption("max", options);
    if (value.length() != 0) {
      setNGramMaxSize(Integer.parseInt(value));
    } else {
      setNGramMaxSize(3);
    }

    value = Utils.getOption("min", options);
    if (value.length() != 0) {
      setNGramMinSize(Integer.parseInt(value));
    } else {
      setNGramMinSize(1);
    }

    super.setOptions(options);
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
	  m_N = 0;
	  if (m_used == m_buffer.size()) {
		  String next = m_Tokenizer.nextToken();
		  //System.out.println(next);
		  char[] str = next.toCharArray();
		  int start = 0;
		  int max_length = next.length();
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
				  if(m_N < m_NMax)
					m_N++;
				  else
				  {
					  if(start + m_N <= next.length())
					  {
					   m_buffer.add(next.substring(start, start + m_N));
					   start += m_N;
					  }
					  else
					  {
					   m_buffer.add(next.substring(start, max_length));
					   start = max_length;
					  }
					  m_N = 0;
				  }
				  
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
