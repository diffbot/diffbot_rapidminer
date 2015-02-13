/**
 * 
 */
package com.diffbot.rapidminer.analyze.operator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.diffbot.clients.DiffbotClient;
import com.diffbot.clients.DiffbotClient.ResponseType;
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
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetPrecondition;
import com.rapidminer.operator.ports.quickfix.QuickFix;
import com.rapidminer.operator.text.Document;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeAttribute;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.parameter.PortProvider;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.PortConnectedCondition;

/**
 * Diffbot article operator.
 * 
 * @author Gabor Bakos
 */
public class DiffbotAnalyzeOperator extends Operator {
	/**
	 * 
	 */
	static final String CUSTOM_FIELDS_VALUE = "fields";
	/**
	 * 
	 */
	private static final int DEFAULT_TIMEOUT = 30_000;
	/**
	 * 
	 */
	private static final String MODE = "mode";
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
	private static final String TIMEOUT = "timeout";
	/**
	 * 
	 */
	private static final String VERSION = "version";
	/**
	 * 
	 */
	private static final String LOCATION = "location";
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
	 * @param description
	 *            An {@link OperatorDescription}.
	 */
	public DiffbotAnalyzeOperator(OperatorDescription description) {
		super(description);

		ExampleSetPrecondition precondition = new ExampleSetPrecondition(
				urlInputPort) {
			/**
			 * {@inheritDoc} Checks whether there is a table connected and if
			 * yes, it has an Attribute with "location" role. It provides a
			 * quickfix in case there was no location.
			 */
			@Override
			public void makeAdditionalChecks(ExampleSetMetaData emd)
					throws UndefinedParameterError {
				super.makeAdditionalChecks(emd);
				if (urlInputPort.isConnected()) {
					AttributeMetaData attributeMetaData = emd.getAttributeByName(getParameterAsString(LOCATION));
					if (!attributeMetaData.isNominal()) {
						createError(
								Severity.ERROR,
								Collections.<QuickFix> emptyList(),
								"location.not.nominal");
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
		String fields = getParameterAsString(CUSTOM_FIELDS_VALUE);
		if (!missing(fields)) {
			arguments.put("fields", fields);
		}
		String mode = getParameterAsString(MODE);
		if (!missing(mode)) {
			arguments.put(MODE, mode);
		}
		final DiffbotClient client = new DiffbotClient(
				getParameterAsString(TOKEN),
				Integer.toString(getParameterAsInt(VERSION)));
		if (urlInputPort.isConnected()) {
			IOObject anyDataOrNull = urlInputPort.getAnyDataOrNull();
			if (anyDataOrNull != null) {
				urlInputPort.checkPreconditions();
				ExampleSet exampleSet = (ExampleSet) anyDataOrNull;
				Attribute locationAttribute = exampleSet.getAttributes().get(getParameterAsString(LOCATION));
				List<Document> documents = new ArrayList<>(exampleSet.size());
				for (Example example : exampleSet) {
					String url = example.getNominalValue(locationAttribute);
					try {
						final String result = (String) client.callApi(
								"analyze", ResponseType.String, url, arguments);
						documents.add(new Document(result));
					} catch (IOException e) {
						throw new OperatorException(e.getMessage(), e);
					}
				}
				jsonOutputPort.deliver(new IOObjectCollection<>(documents));
			}
		} else {
			try {
				final String result = (String) client.callApi("analyze",
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
		ParameterTypeInt version = new ParameterTypeInt(VERSION,
				"Diffbot version", 2, 11, 3, true);
		ParameterTypeInt timeout = new ParameterTypeInt(
				TIMEOUT,
				"Timeout on server before terminate the response (in milliseconds)",
				0, Integer.MAX_VALUE, 30_000, true);
		ParameterTypeString customFieldsValue = new ParameterTypeString(
				CUSTOM_FIELDS_VALUE, "Custom 'fields' definition", true, true);
		ParameterTypeStringCategory mode = new ParameterTypeStringCategory(
				MODE,
				"Mode, when set, only the specific automatic API will be used to analyze that specific page type. All other pages will return the default Analyze fields",
				new String[] { "article", "product", "image", "discussion",
						"video" }, "", true);
		ParameterTypeString userAgent = new ParameterTypeString(USER_AGENT,
				"User-Agent header used by Diffbot", true, true);
		ParameterTypeString referrer = new ParameterTypeString(REFERRER,
				"Referer header used by Diffbot", true, true);
		ParameterTypeString cookie = new ParameterTypeString(COOKIE,
				"Cookie header used by Diffbot", true, true);
		ParameterTypeString acceptLanguage = new ParameterTypeString(
				ACCEPT_LANGUAGE, "Accept-Language header used by Diffbot",
				true, true);
		ParameterTypeAttribute location = new ParameterTypeAttribute(
				LOCATION, "Web page locations", urlInputPort);
		final PortProvider pp = new PortProvider() {
			@Override
			public Port getPort() {
				return urlInputPort;
			}
		};
		url.registerDependencyCondition(new PortConnectedCondition(this, pp,
				true, false));
		location.registerDependencyCondition(new PortConnectedCondition(this,
				pp, true, true));
		return Arrays.<ParameterType> asList(token, url, location, version,
				timeout, customFieldsValue, mode, userAgent, referrer, cookie,
				acceptLanguage);
	}
}
