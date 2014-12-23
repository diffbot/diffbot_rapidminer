/**
 * 
 */
package com.diffbot.rapidminer.article.operator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.diffbot.clients.DiffbotClient;
import com.diffbot.clients.DiffbotClient.ResponseType;
import com.diffbot.rapidminer.PluginInitDiffbot;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.IOObjectCollection;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ProcessSetupError.Severity;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.Port;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetPrecondition;
import com.rapidminer.operator.ports.metadata.MetaDataInfo;
import com.rapidminer.operator.ports.quickfix.ChangeAttributeRoleQuickFix;
import com.rapidminer.operator.ports.quickfix.QuickFix;
import com.rapidminer.operator.text.Document;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.PortProvider;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.BooleanParameterCondition;
import com.rapidminer.parameter.conditions.PortConnectedCondition;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.config.Configurable;
import com.rapidminer.tools.config.ConfigurationException;
import com.rapidminer.tools.config.ConfigurationManager;
import com.rapidminer.tools.config.ParameterTypeConfigurable;

/**
 * Diffbot article operator.
 * 
 * @author Gabor Bakos
 */
public class DiffbotArticleOperator extends Operator {
	/**
	 * 
	 */
	static final String CUSTOM_FIELDS_VALUE = "custom_fields_value";
	/**
	 * 
	 */
	static final String CUSTOM_FIELDS = "custom_fields";
	/**
	 * 
	 */
	private static final int DEFAULT_TIMEOUT = 30_000;
	/**
	 * 
	 */
	private static final String ACCEPT_LANGUAGE = "accept_language";
	/**
	 * 
	 */
	private static final String COOKIE = "cookie";
	/**
	 * 
	 */
	private static final String REFERRER = "referrer";
	/**
	 * 
	 */
	private static final String USER_AGENT = "user_agent";
	/**
	 * 
	 */
	private static final String FIELDS = "fields";
	/**
	 * 
	 */
	private static final String TIMEOUT = "timeout";
	/**
	 * 
	 */
	private static final String VERSION = "version";
	/**
	 * 
	 */
	private static final String PAGING = "paging";
	/**
	 * 
	 */
	private static final String LOCATION_ROLE = "location";
	/**
	 * 
	 */
	private static final String URL = "url";
	/**
	 * 
	 */
	private static final String TOKEN = "token";
	private InputPort urlInputPort = getInputPorts().createPort("urls");
	private OutputPort jsonOutputPort = getOutputPorts().createPort("Diffbot",
			true);

	/**
	 * Creates the diffbot article operator.
	 * 
	 * @param description An {@link OperatorDescription}.
	 */
	public DiffbotArticleOperator(OperatorDescription description) {
		super(description);
		
		ExampleSetPrecondition precondition = new ExampleSetPrecondition(
				urlInputPort) {
			/**
			 * {@inheritDoc}
			 * Checks whether there is a table connected and if yes, it has an Attribute with "location" role.
			 * It provides a quickfix in case there was no location.
			 */
			@Override
			public void makeAdditionalChecks(ExampleSetMetaData emd)
					throws UndefinedParameterError {
				super.makeAdditionalChecks(emd);
				if (urlInputPort.isConnected()) {
					MetaDataInfo containsSpecialAttribute = emd
							.containsSpecialAttribute(LOCATION_ROLE);
					if (containsSpecialAttribute == MetaDataInfo.NO) {
						createError(
								Severity.ERROR,
								emd.containsAttributesWithValueType(
										Ontology.NOMINAL, true) != MetaDataInfo.NO ? Collections
										.singletonList(new ChangeAttributeRoleQuickFix(
												urlInputPort, LOCATION_ROLE,
												"location.quickfix"))
										: Collections.<QuickFix> emptyList(),
								"location.missing");
					}
				}
			}
		};
		precondition.setOptional(true);

		urlInputPort.addPrecondition(precondition);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.operator.Operator#doWork()
	 */
	@Override
	public void doWork() throws OperatorException {
		super.doWork();
		Map<String, String> arguments = new LinkedHashMap<>();
		if (!getParameterAsBoolean(PAGING)) {
			arguments.put("paging", Boolean.toString(false));
		}
		int timeout = getParameterAsInt(TIMEOUT);
		if (timeout != DEFAULT_TIMEOUT) {
			arguments.put("timeout", Integer.toString(timeout));
		}
		String userAgent = getParameterAsString(USER_AGENT);
		if (!missing(userAgent)) {
			arguments.put("X-Forward-User-Agent", userAgent);
		}
		String referrer = getParameterAsString(REFERRER);
		if (!missing(referrer)) {
			arguments.put("X-Forward-Referer", referrer);
		}
		String cookie = getParameterAsString(COOKIE);
		if (!missing(cookie)) {
			arguments.put(COOKIE, cookie);
		}
		String acceptLanguage = getParameterAsString(ACCEPT_LANGUAGE);
		if (!missing(acceptLanguage)) {
			arguments.put("X-Forward-Accept-Language", acceptLanguage);
		}
		String fields = getParameterAsString(FIELDS);

		try {
			if (getParameterAsBoolean(CUSTOM_FIELDS)) {
				String fieldsAsString = getParameterAsString(CUSTOM_FIELDS_VALUE);
				if (!missing(fieldsAsString)) {
					arguments.put("fields", fieldsAsString);
				}
			} else if (!missing(fields)) {
				Configurable configurable = ConfigurationManager.getInstance()
						.lookup(PluginInitDiffbot.FIELDS_TYPE_ID, fields,
								getProcess().getRepositoryAccessor());
				if (configurable instanceof FieldsConfigurable) {
					FieldsConfigurable fieldsConfigurable = (FieldsConfigurable) configurable;
					String fieldsAsString = fieldsConfigurable
							.getFieldsAsString();
					if (fieldsAsString != null) {
						arguments.put("fields", fieldsAsString);
					}
				}
			}
		} catch (ConfigurationException e) {
			throw new RuntimeException("Unknown fields configuration: "
					+ fields + "\n" + e.getMessage(), e);
		}
		final DiffbotClient client = new DiffbotClient(
				getParameterAsString(TOKEN),
				Integer.toString(getParameterAsInt(VERSION)));
		if (urlInputPort.isConnected()) {
			IOObject anyDataOrNull = urlInputPort.getAnyDataOrNull();
			if (anyDataOrNull != null) {
				urlInputPort.checkPreconditions();
				ExampleSet exampleSet = (ExampleSet) anyDataOrNull;
				Attribute locationAttribute = exampleSet.getAttributes()
						.getSpecial(LOCATION_ROLE);
				List<Document> documents = new ArrayList<>(exampleSet.size());
				for (Example example : exampleSet) {
					String url = example.getNominalValue(locationAttribute);
					try {
						final String result = (String) client.callApi(
								"article", ResponseType.String, url, arguments);
						documents.add(new Document(result));
					} catch (IOException e) {
						throw new OperatorException(e.getMessage(), e);
					}
				}
				jsonOutputPort.deliver(new IOObjectCollection<>(documents));
			}
		} else {
			try {
				final String result = (String) client.callApi("article",
						ResponseType.String, getParameterAsString(URL),
						arguments);
				jsonOutputPort.deliver(new Document(result));
			} catch (IOException e) {
				throw new OperatorException(e.getMessage(), e);
			}
		}
	}

	/**
	 * @param string
	 * @return {@code string} is empty or {@code null}.
	 */
	private static boolean missing(String string) {
		return string == null || string.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.operator.Operator#getParameterTypes()
	 */
	@Override
	public List<ParameterType> getParameterTypes() {
		ParameterTypeString token = new ParameterTypeString(TOKEN, "Token",
				false, false);
		ParameterTypeString url = new ParameterTypeString(URL, "URL", false,
				false);
		ParameterTypeBoolean paging = new ParameterTypeBoolean(
				PAGING,
				"Automatically concatenate multipage articles (up to 20 pages of a single article)",
				true);
		ParameterTypeInt version = new ParameterTypeInt(VERSION,
				"Diffbot version", 2, 11, 3, true);
		ParameterTypeInt timeout = new ParameterTypeInt(
				TIMEOUT,
				"Timeout on server before terminate the response (in milliseconds)",
				0, Integer.MAX_VALUE, 30_000, true);
		ParameterTypeConfigurable fields = new ParameterTypeConfigurable(
				FIELDS, "Optional fields", "fields");
		ParameterTypeBoolean customFields = new ParameterTypeBoolean(
				CUSTOM_FIELDS, "Use custom 'fields' definition", false, true);
		ParameterTypeString customFieldsValue = new ParameterTypeString(
				CUSTOM_FIELDS_VALUE, "Custom 'fields' definition", true, true);
		ParameterTypeString userAgent = new ParameterTypeString(USER_AGENT,
				"User-Agent header used by Diffbot", true, true);
		ParameterTypeString referrer = new ParameterTypeString(REFERRER,
				"Referer header used by Diffbot", true, true);
		ParameterTypeString cookie = new ParameterTypeString(COOKIE,
				"Cookie header used by Diffbot", true, true);
		ParameterTypeString acceptLanguage = new ParameterTypeString(
				ACCEPT_LANGUAGE, "Accept-Language header used by Diffbot",
				true, true);
		customFieldsValue
				.registerDependencyCondition(new BooleanParameterCondition(
						this, CUSTOM_FIELDS, true, true));
		fields.registerDependencyCondition(new BooleanParameterCondition(this,
				CUSTOM_FIELDS, true, false));
		url.registerDependencyCondition(new PortConnectedCondition(this,
				new PortProvider() {
					@Override
					public Port getPort() {
						return urlInputPort;
					}
				}, true, false));
		return Arrays.<ParameterType> asList(token, url, paging, version,
				timeout, fields, customFields, customFieldsValue, userAgent,
				referrer, cookie, acceptLanguage);
	}
}
