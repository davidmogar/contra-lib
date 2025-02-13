import org.centos.contra.pipeline.Utils

/**
 * Defines the retry content of a retry metrics message
 * This will merge parameters with the defaults and will validate each parameter
 * @param parameters
 * @return HashMap
 */
def call(Map parameters = [:]) {

    def utils = new Utils()

    def defaults = readJSON text: libraryResource('msgBus-Metric-RetryData-Content.json')

    return { Map runtimeArgs = [:] ->
        // Set defaults that can't go in json file
        parameters['configuration'] = parameters['configuration'] ?: msgBusMetricRetryDataConfigurationContent()()

        parameters = utils.mapMergeQuotes([parameters, runtimeArgs])
        try {
            mergedMessage = utils.mergeBusMessage(parameters, defaults)
        } catch(e) {
            throw new Exception("Creating retryData closure for metrics message failed: " + e)
        }

        // sendCIMessage expects String arguments
        return utils.getMapStringColon(mergedMessage)
    }
}
