package zxc.classifiers.trees;

import weka.classifiers.Classifier;
import weka.classifiers.Sourcable;
import weka.core.Instances;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;

/**
 * ID3算法的决策树
 * 
 * @author zhouxiaocao, zhouxiaocao@iMiner.com
 * @date 2015年5月7日
 */
public class Id3 extends Classifier implements TechnicalInformationHandler, Sourcable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8357507244308292347L;

	@Override
	public String toSource(String className) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {
		// TODO Auto-generated method stub

	}

}
