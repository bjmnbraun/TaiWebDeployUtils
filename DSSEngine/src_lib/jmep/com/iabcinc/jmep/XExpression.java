/*
 * JMEP - Java Mathematical Expression Parser.
 * Copyright (C) 1999  Jo Desmet
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * You can contact the Original submitter of this library by
 * email at: Jo_Desmet@yahoo.com.
 * 
 */


package com.iabcinc.jmep;

/**
 * This is the base class for all exceptions that can occure using the
 * Expression Class.
 * @author Jo Desmet
 * @see com.iabcinc.jmep.Expression
 * @see com.iabcinc.jmep.XIllegalOperation
 * @see com.iabcinc.jmep.XIllegalStatus
 * @see com.iabcinc.jmep.XUndefinedVariable
 * @see com.iabcinc.jmep.XUndefinedFunction
 * @see com.iabcinc.jmep.XUndefinedUnit
 */
public class XExpression extends Exception {
  private int m_iPosition;

  /*
   * NOTE: The constructor should not defined public as it should only
   * be used within the package.
   */
  public XExpression(int iPosition,String sError,Throwable source) {
    super("ERROR(@"+iPosition+"): "+sError, source);
    m_iPosition = iPosition;
  }  /*   * NOTE: The constructor should not defined public as it should only   * be used within the package.   */  public XExpression(int iPosition,String sError) {	  this(iPosition,sError,null);  }

  /**
   * Gets the position where the error occurred.
   * @return the position of the problem.
   */
  public int getPosition() {
    return m_iPosition;
  }
} 
