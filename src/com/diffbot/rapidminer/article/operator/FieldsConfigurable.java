/*
 * 
 */
package com.diffbot.rapidminer.article.operator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.diffbot.rapidminer.PluginInitDiffbot;
import com.rapidminer.tools.config.AbstractConfigurable;
import static com.diffbot.rapidminer.article.operator.FieldsConfigurator.*;

/**
 * The configurable fields for Diffbot article optional fields.
 * 
 * @author Gabor Bakos
 */
public class FieldsConfigurable extends AbstractConfigurable {
	/** Constant for no optional fields. */
	public static final FieldsConfigurable NO_FIELDS = new FieldsConfigurable(),
			/** Constant for all optional fields. */
			ALL_FIELDS = new FieldsConfigurable();
	static {
		NO_FIELDS.setName("no fields");
		ALL_FIELDS.setName("all fields");
		for (String key : new String[] {TAGS, IMAGES, IMAGES_WIDTH, IMAGES_HEIGHT, VIDEOS, VIDEOS_WIDTH, VIDEOS_HEIGHT, META, LINKS, QUERY_STRING}) {
			NO_FIELDS.setParameter(key, Boolean.toString(false));
			ALL_FIELDS.setParameter(key, Boolean.toString(true));
		}
	}

	/**
	 * Constructor for reflection.
	 */
	public FieldsConfigurable() {
		super();
		
	}

	/* (non-Javadoc)
	 * @see com.rapidminer.tools.config.Configurable#getTypeId()
	 */
	@Override
	public String getTypeId() {
		return PluginInitDiffbot.FIELDS_TYPE_ID;
	}

	/**
	 * @return The value for diffbot "fields" argument.
	 */
	public String getFieldsAsString() {
		Map<String, String> parameters = getParameters();
		boolean hasNonFalse = false;
		for (Entry<String, String> entry : parameters.entrySet()) {
			String val = entry.getValue();
			hasNonFalse |= !Objects.equals(val, Boolean.toString(false));
		}
		if(!hasNonFalse) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		if (isTrue(parameters, FieldsConfigurator.TAGS)) {
			sb.append("tags").append(',');
		}
		if (isTrue(parameters, FieldsConfigurator.IMAGES)) {
			sb.append("images");
			if (isTrue(parameters, FieldsConfigurator.IMAGES_WIDTH)) {
				sb.append('(');
				sb.append("width");
				if (isTrue(parameters, FieldsConfigurator.IMAGES_HEIGHT)) {
					sb.append(',').append("height");
				}
				sb.append(')');
			} else if (isTrue(parameters, FieldsConfigurator.IMAGES_HEIGHT)) {
				sb.append('(');
				sb.append("height");
				sb.append(')');
			}
			sb.append(',');
		}
		if (isTrue(parameters, FieldsConfigurator.VIDEOS)) {
			sb.append("videos");
			if (isTrue(parameters, FieldsConfigurator.VIDEOS_WIDTH)) {
				sb.append('(');
				sb.append("naturalWidth");
				if (isTrue(parameters, FieldsConfigurator.VIDEOS_HEIGHT)) {
					sb.append(',').append("naturalHeight");
				}
				sb.append(')');
			} else if (isTrue(parameters, FieldsConfigurator.VIDEOS_HEIGHT)) {
				sb.append('(');
				sb.append("naturalHeight");
				sb.append(')');
			}
			sb.append(',');
		}
		if (isTrue(parameters, FieldsConfigurator.META)) {
			sb.append("meta");
			sb.append(',');
		}
		if (isTrue(parameters, FieldsConfigurator.LINKS)) {
			sb.append("links");
			sb.append(',');
		}
		if (isTrue(parameters, FieldsConfigurator.QUERY_STRING)) {
			sb.append("querystring");
			sb.append(',');
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	/**
	 * @param parameters
	 * @param key
	 * @return {@code parameters}'s {@code key} value is existing and not {@code false}.
	 */
	private boolean isTrue(Map<String, String> parameters, String key) {
		return parameters.containsKey(key) && !Objects.equals(parameters.get(key), Boolean.toString(false));
	}

	/* (non-Javadoc)
	 * @see com.rapidminer.tools.config.AbstractConfigurable#getShortInfo()
	 */
	@Override
	public String getShortInfo() {
		return "Diffbot article optional fields configurator";
	}
}
