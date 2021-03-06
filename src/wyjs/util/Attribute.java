// This file is part of the Whiley-to-Java Compiler (wyjc).
//
// The Whiley-to-Java Compiler is free software; you can redistribute 
// it and/or modify it under the terms of the GNU General Public 
// License as published by the Free Software Foundation; either 
// version 3 of the License, or (at your option) any later version.
//
// The Whiley-to-Java Compiler is distributed in the hope that it 
// will be useful, but WITHOUT ANY WARRANTY; without even the 
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
// PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public 
// License along with the Whiley-to-Java Compiler. If not, see 
// <http://www.gnu.org/licenses/>
//
// Copyright 2010, David James Pearce. 

package wyjs.util;

import wyjs.lang.*;

public interface Attribute {

  public static class Source implements Attribute {

    public final int start;
    public final int end;

    public Source(int start, int end) {
      this.start = start;
      this.end = end;
    }

    public String toString() {
      return "@" + start + ":" + end;
    }
  }

  public static final class Module implements Attribute {
		public final ModuleID module;
		
		public Module(ModuleID module) {
			this.module = module;
		}
	}
	
  
  public static final class FunType implements Attribute {
	  public final Type.Fun type;
	  
	  public FunType(Type.Fun type) {
		  this.type = type;
	  }
  }
}
