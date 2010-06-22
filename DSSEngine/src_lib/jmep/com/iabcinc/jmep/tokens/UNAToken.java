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

package com.iabcinc.jmep.tokens;

import com.iabcinc.jmep.XIllegalOperation;
import com.iabcinc.jmep.XIllegalStatus;


//TODO: Complex support:
//import com.neemsoft.util;

public class UNAToken extends Token {
    public static final int PLS = 0;
    public static final int MIN = 1;
    public static final int NOT = 2;
    public static final int INV = 3;

  private int m_kUNAToken;

  public UNAToken(int kUNAToken,int iPosition) {
    super(Token.UNA,iPosition);
    m_kUNAToken = kUNAToken;
  }

  public int getKindOfUNA() {
    return m_kUNAToken;
  }

  public Object evaluate(Object oValue, int[][] intArrays, float[][] floatArrays, Object[][] objectArrays, int i)
  throws XIllegalOperation,XIllegalStatus {
    if (oValue instanceof int[]) {    	int value = ((int[])oValue)[0];
      switch (m_kUNAToken) {
        case PLS: return oValue;
        case MIN: intArrays[i][0] = -value; return intArrays[i];        case NOT: intArrays[i][0] = value == 0 ? 1:0; return intArrays[i];        case INV: intArrays[i][0] = ~value; return intArrays[i];
        default: throw new XIllegalStatus(getPosition());
      }
    }
    else if (oValue instanceof float[]) {    	float value = ((float[])oValue)[0];
     switch (m_kUNAToken) {
        case PLS: return oValue;        case MIN: floatArrays[i][0] = -value; return floatArrays[i];
        case NOT: throw new XIllegalOperation(this,oValue);
        case INV: throw new XIllegalOperation(this,oValue);
        default: throw new XIllegalStatus(getPosition());
      }
    }
    else if (oValue instanceof String) {
      throw new XIllegalOperation(this,oValue);
    }
/*
 * TODO: Complex support
 *
 *   else if (oValue instanceof Complex) {
 *     switch (m_kUNAToken) {
 *       case PLS:
 *       case MIN:
 *       case NOT:
 *       case INV:
 *     }
 *   }
 */
    throw new XIllegalStatus(getPosition());
  }
  
}


