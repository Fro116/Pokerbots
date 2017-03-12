/**
 * Copyright (C) 2012-2013 Owen Derby (ocderby@gmail.com)
 *
 * This file is part of pbots_calc.
 *
 * pbots_calc is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * pbots_calc is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * pbots_calc in a file in teh toplevel directory called "GPLv3".  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package pokerbots.player;

import org.bridj.Pointer;

/**
 * Simple command line tool for equity calculations. Demonstrates how to use the
 * pbots_calc library java wrapper.
 */

public class Calculator {

	public static Results calc(String hands, String board, String dead,
			int iters) {
		Pointer<Pbots_calcResults> res = Pbots_calcLibrary.alloc_results();
		Results results = null;
		if (Pbots_calcLibrary.calc(Pointer.pointerToCString(hands),
				Pointer.pointerToCString(board),
				Pointer.pointerToCString(dead), iters, res) > 0) {
			results = new Results(res.get());
		}
		Pbots_calcLibrary.free_results(res);
		return results;
	}

}
