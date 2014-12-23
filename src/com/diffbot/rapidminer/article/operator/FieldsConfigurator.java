/**
 * 
 */
package com.diffbot.rapidminer.article.operator;

import java.util.Arrays;
import java.util.List;

import com.diffbot.rapidminer.PluginInitDiffbot;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.tools.config.Configurator;

/**
 * {@link Configurator} for {@link FieldsConfigurable} of Diffbot article's optional fields.
 * 
 * @author Gabor Bakos
 */
public class FieldsConfigurator extends Configurator<FieldsConfigurable> {

	/**
	 * 
	 */
	static final String QUERY_STRING = "queryString";
	/**
	 * 
	 */
	static final String META = "meta";
	/**
	 * 
	 */
	static final String LINKS = "links";
	/**
	 * 
	 */
	static final String VIDEOS_HEIGHT = "videos_height";
	/**
	 * 
	 */
	static final String VIDEOS_WIDTH = "videos_width";
	/**
	 * 
	 */
	static final String VIDEOS = "videos";
	/**
	 * 
	 */
	static final String IMAGES_HEIGHT = "images_height";
	/**
	 * 
	 */
	static final String IMAGES_WIDTH = "images_width";
	/**
	 * 
	 */
	static final String IMAGES = "images";
	/**
	 * 
	 */
	static final String TAGS = "tags";

	/**
	 * For reflective construction.
	 */
	public FieldsConfigurator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.tools.config.Configurator#getConfigurableClass()
	 */
	@Override
	public Class<FieldsConfigurable> getConfigurableClass() {
		return FieldsConfigurable.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.tools.config.Configurator#getParameterTypes()
	 */
	@Override
	public List<ParameterType> getParameterTypes() {
		ParameterTypeBoolean tags = new ParameterTypeBoolean(
				TAGS,
				"Array of tags/entities, generated from analysis of the extracted text and cross-referenced with DBpedia.",
				false);
		ParameterTypeBoolean images = new ParameterTypeBoolean(IMAGES,
				"Found images", false);
		ParameterTypeBoolean imagesWidth = new ParameterTypeBoolean(
				IMAGES_WIDTH, "Width of image as (re-)sized via browser/CSS", false);
		ParameterTypeBoolean imagesHeight = new ParameterTypeBoolean(
				IMAGES_HEIGHT, "Height of image as (re-)sized via browser/CSS", false);
		ParameterTypeBoolean videos = new ParameterTypeBoolean(VIDEOS,
				"Found videos", false);
		ParameterTypeBoolean videosWidth = new ParameterTypeBoolean(
				VIDEOS_WIDTH, "Raw video width, in pixels", false);
		ParameterTypeBoolean videosHeight = new ParameterTypeBoolean(
				VIDEOS_HEIGHT, "Raw video height, in pixels", false);
		ParameterTypeBoolean links = new ParameterTypeBoolean(
				LINKS,
				"Returns a top-level object (links) containing all hyperlinks found on the page.",
				false);
		ParameterTypeBoolean meta = new ParameterTypeBoolean(
				META,
				"Returns a top-level object (meta) containing the full contents of page meta tags, including sub-arrays for OpenGraph tags, Twitter Card metadata, schema.org microdata, and -- if available -- oEmbed metadata.",
				false);
		ParameterTypeBoolean queryString = new ParameterTypeBoolean(
				QUERY_STRING,
				"Returns any key/value pairs present in the URL querystring. Items without a discrete value will be returned as true.",
				false);

		//Unfortunately cannot create dependencies here, but we would only enable/disable them not make them mandatory.
		return Arrays.<ParameterType> asList(tags, images, imagesWidth,
				imagesHeight, videos, videosWidth, videosHeight, links, meta,
				queryString

		);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.tools.config.Configurator#getTypeId()
	 */
	@Override
	public String getTypeId() {
		return PluginInitDiffbot.FIELDS_TYPE_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.tools.config.Configurator#getI18NBaseKey()
	 */
	@Override
	public String getI18NBaseKey() {
		return PluginInitDiffbot.FIELDS_TYPE_ID;
	}
}
