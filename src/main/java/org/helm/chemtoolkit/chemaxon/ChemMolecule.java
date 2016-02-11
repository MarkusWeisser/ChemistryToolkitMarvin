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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.helm.chemtoolkit.AbstractMolecule;
import org.helm.chemtoolkit.AttachmentList;
import org.helm.chemtoolkit.CTKException;
import org.helm.chemtoolkit.IAtomBase;
import org.helm.chemtoolkit.IBondBase;
import org.helm.chemtoolkit.IChemObjectBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chemaxon.struc.MolAtom;
import chemaxon.struc.MolBond;
import chemaxon.struc.Molecule;

/**
 * @author chistyakov
 *
 */
public class ChemMolecule extends AbstractMolecule {
  private static final Logger LOG = LoggerFactory.getLogger(ChemMolecule.class);

  private Molecule molecule;

  @Override
  public Molecule getMolecule() {
    return this.molecule;
  }

  public ChemMolecule(Molecule molecule) {
    this.molecule = molecule;

  }

  public ChemMolecule(Molecule molecule, AttachmentList attachments) {
    this(molecule);
    atoms = new ArrayList<>();
    for (MolAtom a : molecule.getAtomArray())
      atoms.add(new ChemAtom(a));
    if (attachments != null) {
      this.attachments =
          attachments.cloneList();
    } else
      this.attachments = new AttachmentList();
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @throws CTKException
   */
  @Override
  public void removeINode(IAtomBase node) throws CTKException {
    if (node instanceof ChemAtom) {
      if (atoms.contains(node)) {
        molecule.removeNode(((ChemAtom) node).getMolAtom());
        atoms.remove(node);
      } else
        throw new CTKException("the atom not found in the molecule");
    } else
      throw new CTKException("invalid atom");
  }

  /**
   * 
   * {@inheritDoc}
   */
  @Override
  public void addIBase(IChemObjectBase node) {
    if (node instanceof ChemMolecule) {
      ChemMolecule nodeMolecule = (ChemMolecule) node;
      for (IAtomBase atom : nodeMolecule.getIAtomArray()) {
        molecule.add(((ChemAtom) atom).getMolAtom());
        atoms.add(atom);
      }
      for (MolBond bond : nodeMolecule.getMolecule().getBondArray()) {
        molecule.add(bond);
      }
    } else if (node instanceof ChemAtom) {
      molecule.add(((ChemAtom) node).getMolAtom());
      atoms.add((ChemAtom) node);
    } else if (node instanceof ChemBond) {
      molecule.add(((ChemBond) node).getMolBond());
    }
    if (node instanceof ChemStereoElement) {
      molecule.add(((ChemStereoElement) node).getStereoElement());
    }

  }

  /**
   * 
   * {@inheritDoc}
   */
  @Override
  public void dearomatize() throws CTKException {
    try {
      molecule.dearomatize();
    } catch (IllegalArgumentException e) {
      throw new CTKException(e.getMessage(), e);
    }

  }

  /**
   * 
   * {@inheritDoc}
   */
  @Override
  public Map<String, IAtomBase> getRgroups() throws CTKException {
    molecule.dearomatize();
    return super.getRgroups();
  }

  /**
   * 
   * {@inheritDoc}
   */
  @Override
  public List<IBondBase> getIBondArray() {
    MolBond[] parent = molecule.getBondArray();
    List<IBondBase> target = new ArrayList<>();
    for (int i = 0; i < parent.length; i++) {
      target.add(new ChemBond(parent[i]));
    }
    return target;

  }

  /**
   * 
   * {@inheritDoc}
   */
  @Override
  public AbstractMolecule cloneMolecule() {
    ChemMolecule cloned = new ChemMolecule(molecule.cloneMolecule(), attachments.cloneList());

    return cloned;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void generateCoordinates(int dem) throws CTKException {
    molecule.clean(dem, null);

  }

  /**
   * {@inheritDoc}
   * 
   * @throws CTKException
   */
  @Override
  public void changeAtomLabel(int index, int toIndex) throws CTKException {
    for (IAtomBase atom : getIAtomArray()) {
      if (atom.getFlag() != Flag.PROCESSED && atom.getRgroup() == index) {
        atom.setRgroup(toIndex);
      }
    }

  }

  /**
   * {@inheritDoc}
   * 
   * @throws CTKException
   */
  @Override
  public boolean isSingleStereo(IAtomBase atom) throws CTKException {
    if (atom instanceof ChemAtom) {
      MolAtom rAtom = (MolAtom) atom.getMolAtom();
      int bondCount = rAtom.getBondCount();
      if (bondCount != 1) {
        throw new CTKException("RGroup is allowed to have single connection to other atom");
      }

      MolBond bond = rAtom.getBond(0);

      int bondType = bond.getFlags() & MolBond.STEREO1_MASK;

      return bondType == MolBond.UP || bondType == MolBond.DOWN || bondType == MolBond.WAVY;
    } else
      throw new CTKException("invalid atom!");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeIBase(IChemObjectBase node) {
    if (node instanceof ChemBond) {
      molecule.removeEdge(((ChemBond) node).getMolBond());
    }
    if (node instanceof ChemStereoElement) {
      molecule.removeEdge(((ChemStereoElement) node).getStereoElement());
    }

  }

}
