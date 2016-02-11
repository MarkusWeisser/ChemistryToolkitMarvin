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

import org.helm.chemtoolkit.AbstractMolecule.Flag;
import org.helm.chemtoolkit.IAtomBase;
import org.helm.chemtoolkit.IBondBase;

import chemaxon.struc.MolAtom;

/**
 * @author chistyakov
 *
 */
public class ChemAtom extends IAtomBase {

  private MolAtom atom;

  @Override
  public MolAtom getMolAtom() {

    return atom;
  }

  public ChemAtom(MolAtom atom) {
    this.atom = atom;
    this.flag = Flag.NONE;

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.helm.chemtoolkit.IAtom#getBoundCount()
   */
  @Override
  public int getIBondCount() {
    return atom.getBondCount();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.helm.chemtoolkit.IAtom#getIAtno()
   */
  @Override
  public int getIAtno() {

    return atom.getAtno();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.helm.chemtoolkit.IAtom#getRgroup()
   */
  @Override
  public int getRgroup() {

    return atom.getRgroup();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.helm.chemtoolkit.IAtom#getIBond(int)
   */
  @Override
  public IBondBase getIBond(int arg0) {
    return new ChemBond(atom.getBond(arg0));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.helm.chemtoolkit.IAtom#clone()
   */
  @Override
  public IAtomBase clone() {

    return new ChemAtom(((MolAtom) atom.clone()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean compare(Object obj) {
    if (!(obj instanceof ChemAtom)) {
      return false;
    }

    MolAtom toCompare = ((ChemAtom) obj).getMolAtom();
    return atom.equals(toCompare);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRgroup(int rGroup) {
    atom.setRgroup(rGroup);
    this.flag = Flag.PROCESSED;

  }

}
