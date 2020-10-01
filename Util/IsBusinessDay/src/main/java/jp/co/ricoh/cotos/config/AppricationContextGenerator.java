package jp.co.ricoh.cotos.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

/**
 *
 * AppricationContextを生成する
 *
 */
public class AppricationContextGenerator extends AbstractXmlApplicationContextEx {

	private Resource[] configResources;

	/**
	 * AppricationContextの生成
	 * @param configLocation resource location
	 * @throws BeansException if context creation failed
	 */
	public AppricationContextGenerator(String configLocation) throws BeansException {
		this(new String[] {configLocation}, true, null);
	}

	public AppricationContextGenerator(String[] configLocations, boolean refresh, ApplicationContext parent)
			throws BeansException {

		super(parent);
		setConfigLocations(configLocations);
		if (refresh) {
			refresh();
		}
	}

	@Override
	protected Resource[] getConfigResources() {
		return this.configResources;
	}

}
