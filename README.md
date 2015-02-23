RapidMiner Diffbot Extension
============================

A Diffbot client for RapidMiner 6.1 or above to analyze web pages. It supports the following Diffbot automatic APIs: [analyze](https://www.diffbot.com/dev/docs/analyze/) (general automatic API wrapper) and [article](https://www.diffbot.com/dev/docs/article/) (analysis of [article webpages](http://youtu.be/_dgWacImCRg?t=2m38s); support of it in RapidMiner is experimental).

###Prerequisites
RapidMiner Studio 6.1 with Text Processing. The [Starter](https://rapidminer.com/pricing/) license is sufficient.

### Getting started
1. Install the Diffbot extension from the RapidMiner marketplace (or by copying the [plugin](https://github.com/aborg0/diffbot_rapidminer/releases/download/v_0.1.005/rapidminer-Diffbot.beta.-0.1.005.jar) to the `lib/plugins` folder of RapidMiner Studio or RapidMiner Server). It requires the Text Processing (and the included Cloud Connectivity) plugins.

2. Use the [token you got](http://www.diffbot.com/pricing/) from [Diffbot](http://www.diffbot.com/) to analyze web pages using the operators available under `Text Processing/Diffbot`. The results are presented as JSON documents. You might prefer to use the `JSON To Data` operator to extract information in tabular form.

### Development getting started
1. Checkout [RapidMiner](https://github.com/rapidminer/rapidminer) (e.g. to _~/git/rapidminer_; https://github.com/aborg0/rapidminer/tree/extension_java7 is the preferred branch).

2. Install [RapidMiner Studio 6.1](https://rapidminer.com/products/studio/) (e.g. to _~/rapidminer-studio_).

3. Execute the `./setup.sh` script like this: `RM_SOURCES=$HOME/git/rapidminer RAPIDMINER_HOME=$HOME/rapidminer-studio ./setup.sh`. It will create a folder named RM_61 in the parent folder (required for `ant install`).

4. Build and install your extension by executing the Ant target "install" 

5. Start RapidMiner and check whether your extension has been loaded

##Optional steps
If you prefer, you can update (the file `lib/diffbot-java-1.0-SNAPSHOT.jar`) the diffbot-java version you want to use. The current version was compiled from https://github.com/aborg0/diffbot-java-client/releases/tag/vknime0.1
