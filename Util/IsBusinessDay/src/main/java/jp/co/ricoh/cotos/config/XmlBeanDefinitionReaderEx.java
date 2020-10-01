package jp.co.ricoh.cotos.config;

import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionStoreException;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import jp.co.ricoh.cotos.commonlib.util.DatasourceProperties;

public class XmlBeanDefinitionReaderEx extends XmlBeanDefinitionReader {

	public XmlBeanDefinitionReaderEx(BeanDefinitionRegistry registry) {
		super(registry);
	}

	@Override
	protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource) throws BeanDefinitionStoreException {
		try {
			Document doc = doLoadDocument(inputSource, resource);
			setProperties(doc);

			return registerBeanDefinitions(doc, resource);
		} catch (BeanDefinitionStoreException ex) {
			throw ex;
		} catch (SAXParseException ex) {
			throw new XmlBeanDefinitionStoreException(resource.getDescription(), "Line " + ex.getLineNumber() + " in XML document from " + resource + " is invalid", ex);
		} catch (SAXException ex) {
			throw new XmlBeanDefinitionStoreException(resource.getDescription(), "XML document from " + resource + " is invalid", ex);
		} catch (ParserConfigurationException ex) {
			throw new BeanDefinitionStoreException(resource.getDescription(), "Parser configuration exception parsing XML from " + resource, ex);
		} catch (IOException ex) {
			throw new BeanDefinitionStoreException(resource.getDescription(), "IOException parsing XML document from " + resource, ex);
		} catch (Throwable ex) {
			throw new BeanDefinitionStoreException(resource.getDescription(), "Unexpected exception parsing XML document from " + resource, ex);
		}
	}

	/**
	 * ymlファイルの値をbean.xmlに反映
	 */
	private void setProperties(Document doc) {
		// yml読み込み
		Map<String, String> hibernateProperties = LoadConfigulation.getHibernateProperties();
		DatasourceProperties datasourceProperties = LoadConfigulation.getDatasourceProperties();

		try {
			NodeList entryNodes = doc.getElementsByTagName("entry");
			NodeList propertyNodes = doc.getElementsByTagName("property");

			// jpaPropertyMap設定
			for (int i = 0; i < entryNodes.getLength(); i++) {
				Node entry = entryNodes.item(i);
				Node key = entry.getAttributes().getNamedItem("key");
				Node value = entry.getAttributes().getNamedItem("value");
				if (key == null) {
					continue;
				}
				switch (key.getNodeValue()) {
				case "hibernate.show_sql":
					value.setNodeValue(hibernateProperties.get("show_sql"));
					break;
				case "hibernate.format_sql":
					value.setNodeValue(hibernateProperties.get("format_sql"));
					break;
				case "hibernate.use_sql_comments":
					value.setNodeValue(hibernateProperties.get("use_sql_comments"));
					break;
				case "hibernate.default_schema":
					value.setNodeValue(hibernateProperties.get("default_schema"));
					break;
				}
			}

			// dataSource設定
			for (int i = 0; i < propertyNodes.getLength(); i++) {
				Node property = propertyNodes.item(i);
				Node name = property.getAttributes().getNamedItem("name");
				Node value = property.getAttributes().getNamedItem("value");
				if (name == null) {
					continue;
				}
				switch (name.getNodeValue()) {
				case "driverClassName":
					value.setNodeValue(datasourceProperties.getDriverClassName());
					break;
				case "url":
					value.setNodeValue(datasourceProperties.getUrl());
					break;
				case "username":
					value.setNodeValue(datasourceProperties.getUsername());
					break;
				case "password":
					value.setNodeValue(datasourceProperties.getPassword());
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
