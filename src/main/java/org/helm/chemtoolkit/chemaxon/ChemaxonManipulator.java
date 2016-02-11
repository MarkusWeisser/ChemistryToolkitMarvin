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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import org.helm.chemtoolkit.AbstractChemistryManipulator;
import org.helm.chemtoolkit.AbstractMolecule;
import org.helm.chemtoolkit.AttachmentList;
import org.helm.chemtoolkit.CTKException;
import org.helm.chemtoolkit.CTKSmilesException;
import org.helm.chemtoolkit.IAtomBase;
import org.helm.chemtoolkit.IBondBase;
import org.helm.chemtoolkit.IStereoElementBase;
import org.helm.chemtoolkit.MoleculeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chemaxon.formats.MolImporter;
import chemaxon.marvin.MolPrinter;
import chemaxon.marvin.calculations.ElementalAnalyserPlugin;
import chemaxon.marvin.io.MolExportException;
import chemaxon.marvin.paint.DispOptConsts;
import chemaxon.marvin.plugin.PluginException;
import chemaxon.struc.MolAtom;
import chemaxon.struc.MolBond;
import chemaxon.struc.Molecule;

/**
 * @author chistyakov
 *
 */
public class ChemaxonManipulator extends AbstractChemistryManipulator {

  private static final Logger LOG = LoggerFactory.getLogger(ChemaxonManipulator.class);

  public static final String UNIQUE_SMILES_FORMAT = "smiles:u";

  public static final String SMILES_FORMAT = "smiles";

  private static final String MOL_FORMAT = "mol";

  public static final String CHEMAXON_EXTENDEND_SMILES_FORMAT = "cxsmiles:u-e";

  /**
   * 
   * {@inheritDoc}
   */
  @Override
  public String convert(String data, StType type) throws CTKException {
    String result = null;

    switch (type) {
    case SMILES:
      result = convertSMILES2MolFile(data);
      break;
    case MOLFILE:
      result = convertMolFile2SMILES(data);
      break;
    case SEQUENCE:
      try {
        result = molecule2MolFile(getMolecule(data));
      } catch (MolExportException e) {
        new CTKException(e.getMessage(), e);
      } catch (IOException e) {
        new CTKException(e.getMessage(), e);
      }
      break;
    default:
      break;
    }

    return result;
  }

  /**
   * @param molecule
   * @return
   * @throws MolExportException
   */
  private String molecule2MolFile(Molecule molecule) throws MolExportException {
    molecule.clean(2, null);
    molecule.dearomatize();
    return molecule.exportToFormat(MOL_FORMAT);
  }

  /**
   * 
   * @param molecule
   * @return
   * @throws CTKException
   * @throws MolExportException
   */
  private String molecule2SMILES(Molecule molecule) throws MolExportException {

    molecule.dearomatize();

    return molecule.exportToFormat(SMILES_FORMAT);

  }

  /**
   * 
   * {@inheritDoc}
   */
  @Override
  public boolean validateSMILES(String smiles) {
    Molecule mol;
    try {
      mol = getMolecule(smiles);
      for (int i = 0; i < mol.getAtomCount(); i++) {
        MolAtom a = mol.getAtom(i);
        a.valenceCheck();
        if (a.hasValenceError()) {
          return false;
        }
      }
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  /**
   * 
   * {@inheritDoc}
   */
  @Override
  public MoleculeInfo getMoleculeInfo(AbstractMolecule aMolecule) throws CTKException {

    Molecule molecule = ((ChemMolecule) aMolecule).getMolecule();
    MoleculeInfo moleculeInfo;
    try {
      // molecule = getMolecule(smiles);
      ElementalAnalyserPlugin plugin = new ElementalAnalyserPlugin();
      plugin.setMolecule(molecule);
      plugin.run();
      moleculeInfo = new MoleculeInfo();
      moleculeInfo.setMolecularFormula(plugin.getFormula());
      moleculeInfo.setMolecularWeight(plugin.getMass());
      moleculeInfo.setExactMass(plugin.getExactMass());

    } catch (PluginException e) {
      throw new CTKException("unable to analyse molecule", e);
    }

    return moleculeInfo;
  }

  private String convertSMILES2MolFile(String smiles) throws CTKException {
    String result = null;
    try {
      Molecule molecule = getMolecule(smiles);
      molecule.clean(2, null);
      molecule.dearomatize();
      result = molecule.exportToFormat(MOL_FORMAT);

    } catch (IOException e) {
      throw new CTKSmilesException("invalid SMILES!", e);
    }
    return result;
  }

  private String convertMolFile2SMILES(String molfile) throws CTKException {
    String result = null;
    try {
      Molecule molecule = getMolecule(molfile);
      result = molecule.exportToFormat(SMILES_FORMAT);

    } catch (IOException e) {
      throw new CTKSmilesException("invalid molfile!", e);
    }
    return result;
  }

  /**
   * 
   * {@inheritDoc}
   */
  @Override
  public String canonicalize(String smiles) throws CTKException, CTKSmilesException {
    String result = null;
    try {
      Molecule molecule = getMolecule(smiles);
      molecule.implicitizeHydrogens(MolAtom.ALL_H);
      result = molecule.toFormat(UNIQUE_SMILES_FORMAT);
    } catch (IOException e) {
      throw new CTKSmilesException("invalid SMILES!", e);
    }
    return result;
  }

  /**
   * convert SMILES and MOLFiles to Molecule
   * 
   * @param smiles input data string
   * @return Molecule object
   * @throws java.io.IOException
   */

  private Molecule getMolecule(String data) throws IOException {
    Molecule molecule = null;
    if (data != null) {
      // molecule = MolImporter.importMol(data);
      InputStream is = new ByteArrayInputStream(data.getBytes());
      MolImporter importer = new MolImporter(is);
      molecule = importer.read();
      molecule.clean(2, null);
// for (MolBond bond : molecule.getBondArray()) {
// bond.calcStereo2();
// }
// molecule.clean(2, null);

    }
    return molecule;
  }

  /**
   * 
   * {@inheritDoc}
   */
  @Override
  public byte[] renderMol(String molFile, OutputType outputType, int width, int height, int rgb) throws CTKException {
    byte[] result;

    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {

      int scaledw = width / 2;
      int scaledh = (scaledw * 3) / 6;
      BufferedImage image = new BufferedImage(scaledw, scaledh, BufferedImage.TYPE_INT_ARGB);

      Graphics2D g = image.createGraphics();
      Rectangle drawArea = new Rectangle(-1, -1, scaledw + 1, scaledh + 1);

      g.draw(drawArea);

      Molecule mol = getMolecule(molFile);
      mol.hydrogenize(false);

      MolPrinter printer = new MolPrinter(mol);
      // printer.setImplicitH(DispOptConsts.IMPLICITH_OFF_S);
      printer.setImplicitH(DispOptConsts.IMPLICITH_ALL_S);

      System.out.println(printer.getImplicitH());
      printer.setScale(printer.maxScale(drawArea));
      printer.setBackgroundColor(new Color(rgb));
      g.setBackground(new Color(rgb));
      printer.paint(g, drawArea);

      ImageIO.write(image, outputType.toString(), ios);

      result = baos.toByteArray();
    } catch (IOException e) {
      throw new CTKException("unable to invoke the outputstream");
    }

    return result;

  }

  /**
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public byte[] renderSequence(String sequence, OutputType outputType, int width, int height, int rgb)
      throws CTKException {
    // TODO
    return null;
  }

  /**
   * 
   * {@inheritDoc}
   */
  @Override
  public AbstractMolecule getMolecule(String smiles, AttachmentList attachments) throws IOException {

    ChemMolecule molecule = new ChemMolecule(getMolecule(smiles), attachments);
    return molecule;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IBondBase bindAtoms(IAtomBase atom1, IAtomBase atom2) throws CTKException {
    IBondBase bond = null;
    if ((atom1 instanceof ChemAtom) && (atom1 instanceof ChemAtom)) {
      bond = new ChemBond(new MolBond(((ChemAtom) atom1).getMolAtom(), ((ChemAtom) atom2).getMolAtom()));
    } else {
      throw new CTKException("invalid atoms");
    }

    return bond;
  }

  /**
   * {@inheritDoc}
   * 
   * @throws CTKException
   */
  @Override
  public IStereoElementBase getStereoInformation(AbstractMolecule molecule, IAtomBase rGroup, IAtomBase atom1,
      IAtomBase atom2)
          throws CTKException {
    if (molecule instanceof ChemMolecule && rGroup instanceof ChemAtom && atom1 instanceof ChemAtom
        && atom2 instanceof ChemAtom) {
      MolAtom rAtom = (MolAtom) rGroup.getMolAtom();
      MolBond chiralBond = rAtom.getBond(0);
      MolBond mergedBond = chiralBond.cloneBond((MolAtom) atom2.getMolAtom(), (MolAtom) atom1.getMolAtom());

      return new ChemStereoElement(mergedBond);

    } else
      throw new CTKException("invalid input data");
  }

  /**
   * {@inheritDoc}
   * 
   * @throws CTKException
   * 
   * @throws MolExportException
   */
  @Override
  public String convertMolecule(AbstractMolecule container, StType type) throws CTKException {
    String result = null;
    Molecule molecule = (Molecule) container.getMolecule();
    switch (type) {
    case SMILES:
      try {
        result = molecule2SMILES(molecule);
      } catch (MolExportException e) {
        throw new CTKException("unable to export molecule to SMILES!", e);
      }
      break;
    case MOLFILE:
      try {
        result = molecule2MolFile(molecule);
      } catch (MolExportException e) {
        throw new CTKException("unable to export molecule to molfile!", e);
      }
      break;
    default:
      break;

    }

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean setStereoInformation(AbstractMolecule firstContainer, IAtomBase firstRgroup,
      AbstractMolecule secondContainer, IAtomBase secondRgroup, IAtomBase atom1, IAtomBase atom2) throws CTKException {

    boolean isStereo =
        super.setStereoInformation(firstContainer, firstRgroup, secondContainer, secondRgroup, atom1, atom2);
    if (!isStereo) {
      firstContainer.addIBase(bindAtoms(atom1, atom2));

    }
    return isStereo;
  }

}
