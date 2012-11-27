package com.sap.core.odata.core.uri.expression;

import java.util.Iterator;
import java.util.Vector;

import com.sap.core.odata.api.uri.UriLiteral;

public class TokenList implements Iterator<Token>
{
  private Vector<Token> tokens = null;
  int currentToken = 0;

  public TokenList()
  {
    this.tokens = new Vector<Token>();
  } 
  
  /**
   * Append StringValue Token to tokens parameter
   * @param Position of parsed token 
   * @param Kind of parsed token
   * @param Stringvalue of parsed token
   */
  public void appendToken(int position, TokenKind kind, String uriLiteral)
  {
    Token token = new Token(kind, position, uriLiteral );
    this.tokens.add(token);
    return;
  }  
  
  
  /**
   * Append CharValue Token to tokens parameter
   * @param Token list to be extended
   * @param Position of parsed token 
   * @param Kind of parsed token
   * @param Charvalue of parsed token
   */
  public void appendToken(int position, TokenKind kind, char charValue)
  {
    Token token = new Token(kind, position, Character.toString(charValue));
    this.tokens.add(token);
    return;
  } 
  
  /**
   * Append UriLiteral Token to tokens parameter
   * @param Token list to be extended
   * @param Position of parsed token 
   * @param Kind of parsed token
   * @param UriLiteral of parsed token containing type and value of UriLiteral 
   */
  public void appendEdmTypedToken(int position, TokenKind kind, String uriLiteral, UriLiteral javaLiteral)
  {
    Token token = new Token(kind, position,  uriLiteral, javaLiteral);
    this.tokens.add(token);
    return;
  }


  public Token lookToken()
  {
    if (currentToken >= tokens.size())
      return null;

    return tokens.get(currentToken );
  }

  
  
  public boolean hasTokens()
  {
    return (tokens.size() > 0);
  }
  
  public int tokenCount()
  {
    return tokens.size(); 
  }
  
  public void expectToken(TokenKind comma) throws TokenizerMessage
  {
    // TODO Auto-generated method stub
  }
  
  
  public Token expectToken(String literal) throws TokenizerMessage
  {
    Token actual = next();
    if (literal.equals(actual.getUriLiteral()))
    {
      currentToken++;
      return actual;
    }
    throw TokenizerMessage.unexpectedToken(currentToken, literal, actual);
  }
  
  public void skip()
  {
    currentToken++;
  }

  @Override
  public boolean hasNext() {
    return (currentToken < tokens.size());
  }

  @Override
  public Token next() {
    if (currentToken >= tokens.size())
      return null;

    Token ret = tokens.get(currentToken );
    currentToken++;
    return ret;
  }

  @Override
  public void remove() {
    // TODO Auto-generated method stub
  }

  public Token elementAt(int index) {
    
    return tokens.elementAt(index);
  }

  public Token GetToken() {
    // TODO Auto-generated method stub
    return null;
  }

  
}