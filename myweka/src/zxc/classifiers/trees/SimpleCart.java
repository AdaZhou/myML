/*
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version. This program is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU General Public License along with this program; if not, write to the
 * Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 * SimpleCart.java Copyright (C) 2007 Haijian Shi
 */

package zxc.classifiers.trees;

import java.util.Enumeration;

import weka.classifiers.RandomizableClassifier;
import weka.core.AdditionalMeasureProducer;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instances;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;

/**
 * <!-- globalinfo-start --> Class implementing minimal cost-complexity pruning.<br/>
 * Note when dealing with missing values, use "fractional instances" method instead of surrogate split method.<br/>
 * <br/>
 * For more information, see:<br/>
 * <br/>
 * Leo Breiman, Jerome H. Friedman, Richard A. Olshen, Charles J. Stone (1984). Classification and Regression Trees. Wadsworth International Group,
 * Belmont, California.
 * <p/>
 * <!-- globalinfo-end --> <!-- technical-bibtex-start --> BibTeX:
 * 
 * <pre>
 * &#64;book{Breiman1984,
 *    address = {Belmont, California},
 *    author = {Leo Breiman and Jerome H. Friedman and Richard A. Olshen and Charles J. Stone},
 *    publisher = {Wadsworth International Group},
 *    title = {Classification and Regression Trees},
 *    year = {1984}
 * }
 * </pre>
 * <p/>
 * <!-- technical-bibtex-end --> <!-- options-start --> Valid options are:
 * <p/>
 * 
 * <pre> -S &lt;num&gt;
 *  Random number seed.
 *  (default 1)</pre>
 * 
 * <pre> -D
 *  If set, classifier is run in debug mode and
 *  may output additional info to the console</pre>
 * 
 * <pre> -M &lt;min no&gt;
 *  The minimal number of instances at the terminal nodes.
 *  (default 2)</pre>
 * 
 * <pre> -N &lt;num folds&gt;
 *  The number of folds used in the minimal cost-complexity pruning.
 *  (default 5)</pre>
 * 
 * <pre> -U
 *  Don't use the minimal cost-complexity pruning.
 *  (default yes).</pre>
 * 
 * <pre> -H
 *  Don't use the heuristic method for binary split.
 *  (default true).</pre>
 * 
 * <pre> -A
 *  Use 1 SE rule to make pruning decision.
 *  (default no).</pre>
 * 
 * <pre> -C
 *  Percentage of training data size (0-1].
 *  (default 1).</pre>
 * 
 <!-- options-end -->
 * 
 * @author Haijian Shi (hs69@cs.waikato.ac.nz)
 * @version $Revision: 10491 $
 */
public class SimpleCart extends RandomizableClassifier implements AdditionalMeasureProducer, TechnicalInformationHandler {

	/** For serialization. */
	private static final long serialVersionUID = 4154189200352566053L;

	/** Training data. */
	protected Instances m_train;

	/** Successor nodes. */
	protected SimpleCart[] m_Successors;

	/** Attribute used to split data. */
	protected Attribute m_Attribute;

	/** Split point for a numeric attribute. */
	protected double m_SplitValue;

	/** Split subset used to split data for nominal attributes. */
	protected String m_SplitString;

	/** Class value if the node is leaf. */
	protected double m_ClassValue;

	/** Class attriubte of data. */
	protected Attribute m_ClassAttribute;

	/** Minimum number of instances in at the terminal nodes. */
	protected double m_minNumObj = 2;

	/** Number of folds for minimal cost-complexity pruning. */
	protected int m_numFoldsPruning = 5;

	/** Alpha-value (for pruning) at the node. */
	protected double m_Alpha;

	/** Number of training examples misclassified by the model (subtree rooted). */
	protected double m_numIncorrectModel;

	/** Number of training examples misclassified by the model (subtree not rooted). */
	protected double m_numIncorrectTree;

	/** Indicate if the node is a leaf node. */
	protected boolean m_isLeaf;

	/** If use minimal cost-compexity pruning. */
	protected boolean m_Prune = true;

	/** Total number of instances used to build the classifier. */
	protected int m_totalTrainInstances;

	/** Proportion for each branch. */
	protected double[] m_Props;

	/** Class probabilities. */
	protected double[] m_ClassProbs = null;

	/** Distributions of leaf node (or temporary leaf node in minimal cost-complexity pruning) */
	protected double[] m_Distribution;

	/** If use huristic search for nominal attributes in multi-class problems (default true). */
	protected boolean m_Heuristic = true;

	/** If use the 1SE rule to make final decision tree. */
	protected boolean m_UseOneSE = false;

	/** Training data size. */
	protected double m_SizePer = 1;

	/**
	 * Return a description suitable for displaying in the explorer/experimenter.
	 * 
	 * @return a description suitable for displaying in the explorer/experimenter
	 */
	public String globalInfo() {
		return "Class implementing minimal cost-complexity pruning.\n" + "Note when dealing with missing values, use \"fractional " +
				"instances\" method instead of surrogate split method.\n\n" + "For more information, see:\n\n" + getTechnicalInformation().toString();
	}

	/**
	 * Returns an instance of a TechnicalInformation object, containing detailed information about the technical background of this class, e.g., paper
	 * reference or book this class is based on.
	 * 
	 * @return the technical information about this class
	 */
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result;

		result = new TechnicalInformation(Type.BOOK);
		result.setValue(Field.AUTHOR, "Leo Breiman and Jerome H. Friedman and Richard A. Olshen and Charles J. Stone");
		result.setValue(Field.YEAR, "1984");
		result.setValue(Field.TITLE, "Classification and Regression Trees");
		result.setValue(Field.PUBLISHER, "Wadsworth International Group");
		result.setValue(Field.ADDRESS, "Belmont, California");

		return result;
	}

	/**
	 * Returns default capabilities of the classifier.
	 * 
	 * @return the capabilities of this classifier
	 */
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();
		result.disableAll();

		// attributes
		result.enable(Capability.NOMINAL_ATTRIBUTES);
		result.enable(Capability.NUMERIC_ATTRIBUTES);
		result.enable(Capability.MISSING_VALUES);

		// class
		result.enable(Capability.NOMINAL_CLASS);

		return result;
	}

	@Override
	public Enumeration enumerateMeasures() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getMeasure(String measureName) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {
		// TODO Auto-generated method stub
		getCapabilities().testWithFail(data);
		data = new Instances(data);
		data.deleteWithMissingClass();
		// 如果不剪枝
		if(!m_Prune) {
			// 每个特征值最佳分裂点的Gini指数
			double[] eachAttriBestGiniGain = new double[data.numAttributes()];
			for(int i = 0; i < data.numAttributes(); i++) {

			}
		}
	}

	private double calcuAttriGiniGainNominal() {

	}

	private double calcuAttriGiniGainNumeric() {

	}

	private double calcuAttriGiniGain(Instances data, int attriIndex) {
		Attribute att = data.attribute(attriIndex);
		if(att.isNominal()) {

		}
		if(att.isNumeric()) {

		}
	}

}
