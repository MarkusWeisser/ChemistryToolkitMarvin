/*******************************************************************************
 * Copyright C 2015, The Pistoia Alliance
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package org.helm.chemtoolkit.chemaxon;

import org.helm.chemtoolkit.IBondBase;
import org.helm.chemtoolkit.IStereoElementBase;

import chemaxon.struc.MolBond;

/**
 * @author chistyakov
 *
 */
public class ChemBond implements IBondBase {

  private MolBond bond;

  private ChemStereoElement stereoElement;

  /**
   * @param bond
   */
  protected ChemBond(MolBond bond) {
    this.bond = bond;
  }

  public ChemBond(MolBond bond, IStereoElementBase stereoElement) {
    new ChemBond(bond);
    if (stereoElement != null && stereoElement instanceof ChemStereoElement)
      this.stereoElement = (ChemStereoElement) stereoElement;
  }

  /**
   * 
   * {@inheritDoc}
   */
  @Override
  public ChemAtom getIAtom1() {
    return new ChemAtom(bond.getAtom1());
  }

  /**
   * 
   * {@inheritDoc}
   */
  @Override
  public ChemAtom getIAtom2() {
    return new ChemAtom(bond.getAtom2());
  }

  /**
   * @return
   */
  public MolBond getMolBond() {

    return bond;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IStereoElementBase getStereoElement() {

    return stereoElement;
  }

  /**
   * {@inheritDoc}
   */
  public int getType() {
    return bond.getType();
  }

}
